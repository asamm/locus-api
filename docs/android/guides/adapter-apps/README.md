# Building a Locus parser adapter app

Adapter apps let your Android app expose a sensor (real or virtual) to Locus Map.
Locus owns the BT4 / USB / ANT transport; your adapter writes only the byte parser
and declares which Locus Variables it produces. The data lands in Locus's
dashboards / track recording / audio coach exactly like a built-in sensor.

This guide covers the **parser-style** adapter API — the simpler of the two
adapter models, the only one shipping in v1. The companion **push-style** API
(for adapters that own their own connection lifecycle and notify Locus when
they produce values) is a separate AIDL surface, deferred until a real
push-style adapter use case drives its design.

The Phase B v1 surface is a curated set of built-in refIds, BT4 transport, one
or more device types per adapter.

## TL;DR

1. Add the `locus-api-android` dependency.
2. Subclass [`LocusParserAdapterService`](../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/parser/LocusParserAdapterService.kt)
   and implement two methods: `init` and `parseCharacteristic`.
3. Declare the service in your `AndroidManifest.xml` with the
   `locus.api.android.ACTION_SENSOR_ADAPTER_PARSER` intent-filter action and
   the `com.asamm.locus.permission.SENSOR_ADAPTER` permission (Locus declares
   it; your adapter requires it). Set `android:icon` on the service for the
   picker icon.
4. Add a `<meta-data>` pointing to a `res/xml/locus_adapter.xml` device-type
   catalog with adapter-level metadata (apiVersion, schemaVersion, id,
   displayName) on the root and one `<deviceType>` per kind of hardware (see
   [`manifest-schema.md`](manifest-schema.md)). Locus reads it all without
   binding the service.
5. Install both Locus Map and your adapter on the same device. Open Locus's
   sensor picker; your adapter's device types appear under "External adapters."

## Walkthroughs

| Topic | Doc |
|---|---|
| Manifest XML schema + sample | [`manifest-schema.md`](manifest-schema.md) |
| AIDL service contract | [`aidl-contract.md`](aidl-contract.md) |
| Curated refIds your adapter can write to | [`../../reference/locus-variables.md`](../../reference/locus-variables.md) |
| Annotated sample manifest XML | [`sample-manifest.xml`](sample-manifest.xml) |

## Minimal adapter (BT4)

```kotlin
class BoschEBikeAdapterService : LocusParserAdapterService() {

    override fun init(bindContext: LocusBindContext): Int {
        // Bind context tells us which refIds Locus understands and the running
        // Locus's version. Bail with INIT_INCOMPATIBLE_API if you detect a
        // finer-grained mismatch the XML apiVersion filter didn't catch.
        return AdapterApi.INIT_OK
    }

    override fun parseCharacteristic(
        deviceId: String,
        deviceTypeId: String,
        charUuid: String,
        bytes: ByteArray,
    ): SensorValueBatch? {
        // Locus owns scanning + GATT lifecycle; your parser only handles bytes.
        // Owns frame-reassembly state. Return null when bytes were consumed
        // without producing values (partial frame).
        if (deviceTypeId != "bosch-ldi") return null
        val decoded = BoschLiveDataDecoder.decode(bytes) ?: return null
        return SensorValueBatchBuilder(System.currentTimeMillis())
            .put(LocusVariables.SENSOR_HEART_RATE, decoded.heartRate)
            .put(LocusVariables.SENSOR_CADENCE, decoded.cadence)
            .put(LocusVariables.SENSOR_POWER, decoded.power)
            .put(LocusVariables.SENSOR_BICYCLE_BATTERY, decoded.batteryPercent)
            .build()
    }
}
```

The `LocusVariables.SENSOR_X` constants are typed: writing a `Float` to a
`LocusVariable<Int>` slot is a compile error, not a runtime surprise.

The `deviceTypeId` parameter matches an `<deviceType id="...">` from your
manifest XML — adapters that handle multiple device types (e.g. Bosch +
Shimano under one adapter) switch on it for protocol-specific parsing.

## What's not in v1

The Phase B v1 surface is intentionally narrow. The following are deferred:

- **Push-style adapter API** — for adapters that own their own connection
  lifecycle (cloud-backed, Android-sensor-backed, web-socket-backed) and notify
  Locus when they produce values. Separate AIDL + base class; lands when a real
  push-style adapter is driving the design.
- **Inline custom Variables** — your adapter can only write to the curated
  [`LocusVariables`](../../reference/locus-variables.md) refIds.
- **Connection types other than BT4** — BT3, USB, ANT, GNSS-NMEA expand the
  parser-style surface in later PRs.
- **Multi-device pairing UX polish** — multi-device adapters work (two paired
  Bosch eBikes = two separate Locus-side pairings sharing the same
  `<deviceType id="bosch-ldi">`), but the picker shows them flat for now.
- **Adapter Variables in FIT / GPX exports** — track recording captures
  adapter data into the dashboard but doesn't yet write it to exported tracks.

## Versioning

The contract version is [`AdapterApi.VERSION`](../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/AdapterApi.kt).
Bumped only when AIDL surface changes are backward-incompatible. Adapters
declare the version they're built against as `<adapter apiVersion="…">` in
their manifest XML; Locus filters incompatible adapters out of the picker
before any bind.

Parcelable payloads (`LocusBindContext`, `SensorValueBatch`,
`CharacteristicWrite`) are frozen for a given `AdapterApi.VERSION` — `@Parcelize`
reads fields positionally and has no length prefix, so adding fields in place
would break adapters built against the old shape. New payload fields ship with
a `VERSION` bump.

If a finer-grained version mismatch slips past the XML filter at runtime, the
adapter's `init()` returns `AdapterApi.INIT_INCOMPATIBLE_API` and Locus skips
the session with a user-facing message.
