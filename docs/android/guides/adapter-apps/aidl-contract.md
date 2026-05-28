# Parser-adapter AIDL contract

The bound-service interface
[`ILocusSensorAdapterParser`](../../../locus-api-android/src/main/aidl/locus/api/android/features/sensorAdapter/parser/ILocusSensorAdapterParser.aidl)
plus the Parcelable payloads in the same package. Subclass
[`LocusParserAdapterService`](../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/parser/LocusParserAdapterService.kt)
and you don't write any binder code — it wires the AIDL stub to abstract Kotlin methods.

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

There are no `getAvailableDevices()` / `getDescriptor()` / `getApiVersion()` AIDL calls. Locus
owns the device list and persistence; all static metadata is in the manifest XML so the picker is
populated pre-bind.

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

Return an `Intent` Locus launches when the user taps "Settings" on `deviceId`'s picker row, or
`null` if your adapter has no settings UI. Also used in the `INIT_NEED_USER_ACTION` flow — if the
adapter needs credentials / permissions / in-app pairing first, return that Activity's intent;
Locus relaunches `init` after it returns. Base class default: `null`.

### `shutdown`

```kotlin
override fun shutdown(deviceId: String) {
    // release only this device's state — buffers, timers, handles
}
```

Called once per bound device on unpair / disconnect / app shutdown. The base class drops that
device's write channel before this runs; default is otherwise a no-op. Override to release your
own per-device state (buffers, timers, handles).
