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
6. On `INIT_OK`: Locus persists pairing and starts driving the transport — for BT4, the GATT
   stack per the `<deviceType>`'s `<characteristic>` declarations; for stream transports, the
   open SPP / USB-serial / TCP stream.
7. While paired: every inbound unit (GATT notification for BT4, byte-stream chunk for BT3/USB/NET)
   is handed to your [`parseData(...)`](#parsedata).
8. On unpair / app shutdown: Locus calls [`shutdown()`](#shutdown), then unbinds.

> **NET is reserved.** `AdapterApi.ConnectionType.NET` (read-only TCP stream) exists in the enum
> and the contract is transport-neutral, but Locus does not drive NET yet — a `NET` device type is
> parsed and skipped. v1 ships BT3 / BT4 / USB (read **and** write); NET lands later with no
> contract change. Build against BT3 / BT4 / USB for now.

Note: there's no `getAvailableDevices()` AIDL call. Locus owns the device list
— it scans BLE for new pairings and tracks paired peers in its own
pairing-persistence layer.

There's also no `getDescriptor()` or `getApiVersion()` AIDL call — all static
metadata is declared in the manifest XML so the picker can be populated
without binding any adapter.

## Methods

### `init`

```kotlin
override fun init(deviceId: String, deviceTypeId: String, bindContext: LocusBindContext): Int {
    return AdapterApi.INIT_OK
}
```

`deviceId` / `deviceTypeId` identify the device this bind is for — keep a `deviceId → deviceTypeId`
map if you need the type in later `parseData` calls (they carry only `deviceId`). `bindContext`
carries Locus's `supportedRefIds`, `locusApiVersion`, and package / version. On a writable transport
you may send a connect-time handshake here: `writeData(deviceId, listOf(AdapterWrite(cmdUuid, enableCmd)))`.

Return one of:

| Code | When |
|---|---|
| `INIT_OK` | Adapter is ready. Locus starts driving the transport. |
| `INIT_NEED_USER_ACTION` | Adapter needs one-time setup (credentials, runtime permission, in-app device pairing). Locus launches `getIntentForSettings(deviceId)` and re-tries `init` afterwards. |
| `INIT_INCOMPATIBLE_API` | Adapter can't operate against this Locus's API version. Locus surfaces an error and skips. (XML pre-filtering catches most cases; this is the runtime fallback when the adapter detects a finer-grained mismatch.) |
| `INIT_ERROR` | Generic failure. Prefer the specific codes above when applicable. |

### `parseData`

```kotlin
override fun parseData(
    deviceId: String,
    source: String,
    bytes: ByteArray,
): SensorValueBatch? {
    …
}
```

`source` is the characteristic UUID for BT4, the empty string for stream transports (BT3 / USB).
`deviceTypeId` was established at `init` — keep a `deviceId → deviceTypeId` map if you branch on
protocol. Return either:

- A
  [`SensorValueBatch`](../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/parser/SensorValueBatch.kt)
  containing parsed `(refId → value)` pairs, optionally with write-backs.
- `null` to indicate the data was consumed without producing values
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

Some protocols require a control / ACK write to the device. There are two ways to write, both gated
to **writable transports** (BT4 / BT3 / USB — never read-only NET; a write for a non-writable
transport is dropped). `target` is the characteristic UUID for BT4 (which must declare
`AdapterApi.CharacteristicMode.WRITE` in the manifest); for stream transports pass an empty string —
the bytes go to the single open stream. Writes from both paths run on one ordered lane.

**Reactive** — return writes alongside parsed values: `Builder.writeBack(target, bytes)`; Locus
dispatches them right after applying the batch. Use this for ACKs / responses to received data.

**Adapter-initiated** — call `writeData(deviceId, writes)` (provided by `LocusParserAdapterService`)
any time the session is open, independent of `parseData`. Use this for a connect-time handshake
(from `init`), a periodic poll on your own timer, or an event-driven command — anything that
shouldn't wait for inbound data. No-op if the transport is read-only or the session has ended.

### `getIntentForSettings`

Returns an `Intent` Locus launches when the user taps "Settings" on
`deviceId`'s picker row, or `null` for adapters with no settings UI.

Used in the `INIT_NEED_USER_ACTION` flow: if your adapter needs credentials /
permissions / in-app pairing before it can work, return that flow's Activity
intent here. Locus relaunches `init` after the Activity returns.

**Locus-side hardening** (notes for the locus-core implementer, not the
adapter author): the returned `Intent` crosses the binder fully serialized —
component, action, data URI, extras, flags. Before launching, Locus must
re-anchor it to the adapter's package and strip cross-package grants:

```kotlin
val intent = adapter.getIntentForSettings(deviceId) ?: return
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

```kotlin
override fun shutdown(deviceId: String) {
    // release only this device's state — buffers, timers, handles
}
```

Called once per bound device when the user unpairs, the device disconnects, or Locus shuts down —
`deviceId` matches `init` / `parseData`, so a multi-device adapter releases just that device's
state. The base class drops that device's write channel before this runs.

The base class's default is a no-op. Override only for adapters that hold per-device resources.
