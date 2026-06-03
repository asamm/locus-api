# Parser-adapter AIDL contract

The bound-service interface
[`ILocusSensorAdapterParser`](../../../../locus-api-android/src/main/aidl/locus/api/android/features/sensorAdapter/parser/ILocusSensorAdapterParser.aidl)
plus the Parcelable payloads in the same package. Subclass
[`LocusParserAdapterService`](../../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/parser/LocusParserAdapterService.kt)
and you don't write any binder code — it wires the AIDL stub to abstract Kotlin methods.

## Bind lifecycle

1. Locus discovers adapters via `queryIntentServices` on the
   `locus.api.android.ACTION_SENSOR_ADAPTER_PARSER` action.
2. Locus parses each adapter's `<meta-data>` XML pre-bind — id, displayName, apiVersion, the
   `<deviceType>` list, plus the service icon via `loadIcon(pm)`. Adapters with an
   unsupported `apiVersion` are dropped here, before bind. See
   [`manifest-schema.md`](manifest-schema.md).
3. User picks a device type → Locus scans → user picks a peer.
4. Locus binds the service and calls [`init`](#init) — adapter returns `INIT_OK` or a non-OK
   result code (see below).
5. On `INIT_OK`: Locus starts driving the transport (GATT for BT4 per the `<characteristic>`
   declarations; the open SPP / USB-serial stream for BT3 / USB).
6. While paired: every inbound unit (GATT notification or stream chunk) goes to
   [`parseData`](#parsedata).
7. On unpair / disconnect / app shutdown: Locus calls [`shutdown`](#shutdown), then unbinds.

> **NET is reserved.** `AdapterApi.ConnectionType.NET` (read-only TCP stream) exists in the enum
> but Locus does not drive it yet; a `NET` device type is parsed and skipped. v1 ships BT3 /
> BT4 / USB (read+write); NET lands later with no contract change.

There are no `getAvailableDevices` / `getDescriptor` / `getApiVersion` AIDL calls — Locus owns
the device list and all static metadata lives in the manifest XML.

## Methods

### `init`

```kotlin
override fun init(deviceId: String, deviceTypeId: String, bindContext: LocusBindContext): Int {
    return AdapterApi.INIT_OK
}
```

Called once per bound device. `deviceTypeId` is only passed here — keep your own
`deviceId → deviceTypeId` map if later `parseData` calls need it. `bindContext` carries Locus's
`supportedRefIds`, `locusApiVersion`, and package info. On a writable transport you may send a
connect-time handshake from here via `writeData(deviceId, …)`.

| Code | When |
|---|---|
| `INIT_OK` | Ready; Locus starts driving the transport. |
| `INIT_NEED_USER_ACTION` | One-time setup needed (credentials, runtime permission, in-app pairing). Locus launches `getIntentForSettings(deviceId)` and re-tries. |
| `INIT_INCOMPATIBLE_API` | Runtime API mismatch the XML filter didn't catch. |
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

`source` is the characteristic UUID for BT4, an empty string for stream transports. The adapter
owns frame-reassembly state; Locus calls this on a background thread, so be re-entrant per
`deviceId` or hold a `synchronized` section.

Return a [`SensorValueBatch`](../../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/parser/SensorValueBatch.kt)
of parsed `(refId → value)` pairs (optionally with write-backs), or `null` when the data was
consumed without producing values (partial frame buffered). Build via
[`SensorValueBatchBuilder`](../../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/parser/SensorValueBatchBuilder.kt):

```kotlin
SensorValueBatchBuilder(timestamp = System.currentTimeMillis())
    .put(LocusVariable.HeartRate, 120)
    .put(LocusVariable.AssistMode, "TRAIL")
    .build()
```

`put` is typed against the curated [`LocusVariable`](../../reference/locus-variables.md) set —
mismatched value types are a compile error.

#### Write-backs

For control / ACK writes back to the device on a **writable transport** (BT4 / BT3 / USB; dropped
for NET). `target` is the characteristic UUID for BT4 (which must declare
`CharacteristicMode.WRITE` in the manifest); for stream transports pass an empty string. All
writes run on one ordered lane regardless of path.

- **Reactive** — `Builder.writeBack(target, bytes)`; Locus dispatches right after applying the
  batch. Use for responses to received data.
- **Adapter-initiated** — `writeData(deviceId, writes)` on the base class; call any time the
  session is open. Use for connect-time handshakes (from `init`), periodic polls, or event-driven
  commands. No-op when the transport is read-only or the session has ended.

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
