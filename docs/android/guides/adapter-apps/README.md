# Building a Locus parser adapter app

Adapter apps let your Android app expose a sensor (real or virtual) to Locus Map.
Locus owns the transport — Bluetooth Classic (BT3 / SPP), Bluetooth LE (BT4 / GATT),
or USB-serial — and your adapter writes only the byte parser and declares which Locus
Variables it produces. The data lands in Locus's dashboards / track recording / audio
coach exactly like a built-in sensor.

Your adapter declares one or more device types, each producing values for a curated set
of built-in Locus Variables over its transport. For BT4, Locus drives the GATT lifecycle
and hands you each characteristic's bytes; for stream transports (BT3 / USB) it hands you
raw stream bytes. Locus handles discovery, scanning / device selection, the connection
lifecycle, and routing parsed values into its dashboards.

## TL;DR

1. Add the `locus-api-android` dependency (JitPack):
   `implementation("com.github.asamm.locus-api:locus-api-android:<version>")`.
2. Subclass [`LocusParserAdapterService`](../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/parser/LocusParserAdapterService.kt)
   and implement two methods: `init` and `parseData`.
3. Declare the service in your `AndroidManifest.xml` with the
   `locus.api.android.ACTION_SENSOR_ADAPTER_PARSER` intent-filter action and
   the `com.asamm.locus.permission.SENSOR_ADAPTER` permission (Locus declares
   it; your adapter requires it). Set `android:icon` on the service for the
   picker icon.
4. Add a `<meta-data>` pointing to a `res/xml/locus_adapter.xml` device-type
   catalog with adapter-level metadata (apiVersion, id, displayName) on the
   root and one `<deviceType>` per kind of hardware (see
   [`manifest-schema.md`](manifest-schema.md)). Locus reads it all without
   binding the service.
5. Install both Locus Map and your adapter on the same device. Open Locus's
   sensor picker; your adapter's device types appear under "External adapters."

## Walkthroughs

| Topic | Doc |
|---|---|
| **Working sample app (start here)** | [`samples/android-sensor-adapter`](../../../samples/android-sensor-adapter) — two complete services: a BT4 HRM and a BT3 + USB NMEA-speed GNSS, with manifests, parsers, and build/pair steps in its own README |
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

    override fun parseData(
        deviceId: String,
        deviceTypeId: String,
        source: String,
        bytes: ByteArray,
    ): SensorValueBatch? {
        // Locus owns scanning + transport lifecycle; your parser only handles bytes.
        // `source` is the characteristic UUID for BT4, empty for stream transports.
        // Owns frame-reassembly state. Return null when bytes were consumed
        // without producing values (partial frame).
        if (deviceTypeId != "bosch-ldi") return null
        val decoded = BoschLiveDataDecoder.decode(bytes) ?: return null
        return SensorValueBatchBuilder(System.currentTimeMillis())
            .put(LocusVariable.HeartRate, decoded.heartRate)
            .put(LocusVariable.Cadence, decoded.cadence)
            .put(LocusVariable.Power, decoded.power)
            .put(LocusVariable.BicycleBattery, decoded.batteryPercent)
            .build()
    }
}
```

The `LocusVariable.X` constants are typed: writing a `Float` to a
`LocusVariable<Int>` slot is a compile error, not a runtime surprise.

The `deviceTypeId` parameter matches an `<deviceType id="...">` from your
manifest XML — adapters that handle multiple device types (e.g. Bosch +
Shimano under one adapter) switch on it for protocol-specific parsing.

## Versioning

The contract version is [`AdapterApi.VERSION`](../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/AdapterApi.kt).
Bumped only when AIDL surface changes are backward-incompatible. Adapters
declare the version they're built against as `<adapter apiVersion="…">` in
their manifest XML; Locus filters incompatible adapters out of the picker
before any bind.

Parcelable payloads (`LocusBindContext`, `SensorValueBatch`,
`AdapterWrite`) are frozen for a given `AdapterApi.VERSION` — `@Parcelize`
reads fields positionally and has no length prefix, so adding fields in place
would break adapters built against the old shape. New payload fields ship with
a `VERSION` bump.

If a finer-grained version mismatch slips past the XML filter at runtime, the
adapter's `init()` returns `AdapterApi.INIT_INCOMPATIBLE_API` and Locus skips
the session with a user-facing message.
