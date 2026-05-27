# Parser-adapter AIDL contract

The contract between Locus and a parser-style adapter app is the
[`ILocusSensorAdapterParser`](../../../locus-api-android/src/main/aidl/locus/api/android/features/sensorAdapter/parser/ILocusSensorAdapterParser.aidl)
bound service interface plus the Parcelable payload classes in the same
package. The [`LocusParserAdapterService`](../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/parser/LocusParserAdapterService.kt)
base class wires the AIDL stub to abstract Kotlin methods so you don't write
any binder code.

This page walks the surface call-by-call.

## Bind lifecycle

1. Locus discovers your adapter via `PackageManager.queryIntentServices` filtered
   by the `locus.api.android.ACTION_SENSOR_ADAPTER_PARSER` action.
2. Locus reads your service's `<meta-data>` XML and parses the device-type
   catalog **without binding** — id, displayName, apiVersion,
   and the `<deviceType>` list all come from XML; the icon comes from the
   service's `android:icon` via `loadIcon(pm)`. Adapters whose `apiVersion`
   Locus can't speak are dropped here, before any bind. See
   [`manifest-schema.md`](manifest-schema.md).
3. User taps a device type in the picker → Locus runs the BLE scan with the
   `<deviceType>`'s `scanFilter` → user picks an instance (BLE peer).
4. Locus calls `bindService` on your adapter.
5. Locus calls [`init(bindContext)`](#init) — adapter returns `INIT_OK` or a
   non-OK result code with semantics described below.
6. On `INIT_OK`: Locus persists pairing and starts driving the BLE GATT stack
   per the `<deviceType>`'s `<characteristic>` declarations.
7. While paired: every GATT NOTIFY / READ response is handed to your
   [`parseCharacteristic(...)`](#parsecharacteristic).
8. On unpair / app shutdown: Locus calls [`shutdown()`](#shutdown), then unbinds.

Note: there's no `getAvailableDevices()` AIDL call. Locus owns the device list
— it scans BLE for new pairings and tracks paired peers in its own
pairing-persistence layer.

There's also no `getDescriptor()` or `getApiVersion()` AIDL call — all static
metadata is declared in the manifest XML so the picker can be populated
without binding any adapter.

## Methods

### `init`

```kotlin
override fun init(bindContext: LocusBindContext): Int {
    // bindContext.supportedRefIds — which Locus Variables this Locus build understands
    // bindContext.locusApiVersion — the adapter API version Locus speaks
    // bindContext.locusPackageName / locusVersionName — running Locus identification
    return AdapterApi.INIT_OK
}
```

Return one of:

| Code | When |
|---|---|
| `INIT_OK` | Adapter is ready. Locus starts driving the transport. |
| `INIT_NEED_USER_ACTION` | Adapter needs one-time setup (credentials, runtime permission, in-app device pairing). Locus launches `getIntentForSettings()` and re-tries `init` afterwards. |
| `INIT_INCOMPATIBLE_API` | Adapter can't operate against this Locus's API version. Locus surfaces an error and skips. (XML pre-filtering catches most cases; this is the runtime fallback when the adapter detects a finer-grained mismatch.) |
| `INIT_ERROR` | Generic failure. Prefer the specific codes above when applicable. |

### `parseCharacteristic`

```kotlin
override fun parseCharacteristic(
    deviceId: String,
    deviceTypeId: String,
    charUuid: String,
    bytes: ByteArray,
): SensorValueBatch? {
    // deviceId       — the BLE MAC (or adapter-internal token) of the connected peer
    // deviceTypeId   — matches a <deviceType id="..."> from your manifest XML;
    //                  branch on this if your adapter supports multiple device types
    // charUuid       — which characteristic the frame came from
    // bytes          — raw payload, as Locus received it from the GATT stack
}
```

Return either:

- A
  [`SensorValueBatch`](../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/parser/SensorValueBatch.kt)
  containing parsed `(refId → value)` pairs, optionally with write-backs.
- `null` to indicate the frame was consumed without producing values
  (e.g. partial frame buffered for reassembly).

Adapters own frame-reassembly state. Locus calls this on a background thread;
your parser should be re-entrant per `deviceId` or hold a `synchronized`
section.

Build the batch via
[`SensorValueBatchBuilder`](../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/parser/SensorValueBatchBuilder.kt):

```kotlin
SensorValueBatchBuilder(timestamp = System.currentTimeMillis())
    .put(LocusVariable.HeartRate, 120)        // Int — matches T
    .put(LocusVariable.Humidity, 55.0f)        // Float — matches T
    .put(LocusVariable.AssistMode, "TRAIL")   // String — matches T
    .build()
```

The Builder's typed `put` rejects type mismatches at compile time. The
Variables you're allowed to write to are the curated set in
[`LocusVariable`](../../reference/locus-variables.md).

#### Write-backs

Some BLE protocols require an ACK frame after each NOTIFY. Schedule the write
via `Builder.writeBack(uuid, bytes)`; Locus dispatches it after applying the
batch. The target characteristic must declare
`AdapterApi.CharacteristicMode.WRITE` in the manifest.

### `getIntentForSettings`

Returns an `Intent` Locus launches when the user taps "Settings" on the
adapter's picker row, or `null` for adapters with no settings UI.

Used in the `INIT_NEED_USER_ACTION` flow: if your adapter needs credentials /
permissions / in-app pairing before it can work, return that flow's Activity
intent here. Locus relaunches `init` after the Activity returns.

**Locus-side hardening** (notes for the locus-core implementer, not the
adapter author): the returned `Intent` crosses the binder fully serialized —
component, action, data URI, extras, flags. Before launching, Locus must
re-anchor it to the adapter's package and strip cross-package grants:

```kotlin
val intent = adapter.getIntentForSettings() ?: return
intent.setPackage(adapterPackageName)        // pin to the adapter's package
intent.component?.let {
    require(it.packageName == adapterPackageName)
}
intent.flags = intent.flags and
    Intent.FLAG_GRANT_READ_URI_PERMISSION.inv() and
    Intent.FLAG_GRANT_WRITE_URI_PERMISSION.inv() and
    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION.inv() and
    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION.inv()
startActivity(intent)
```

Without this, a malicious or buggy adapter could return an Intent targeting a
different package's exported activity, or grant itself URI permissions on
`content://` data it doesn't own.

The base class's default returns `null`. Override only when you have a UI to
host.

### `shutdown`

Called when the user unpairs or Locus shuts down. Release per-pairing state —
parser buffers, in-flight callbacks, background coroutines.

The base class's default is a no-op. Override only for adapters that hold
resources beyond the bind lifecycle.
