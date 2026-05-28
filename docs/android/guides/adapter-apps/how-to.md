# How to build a Locus sensor adapter

Six steps from empty Android project to your adapter visible in Locus's picker. The example
targets a BT4 heart-rate monitor; BT3 and USB differ only in the manifest XML (step 3).

## 1. Add the dependency

In `build.gradle.kts`:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.asamm.locus-api:locus-api-android:0.10.0")
}
```

## 2. Declare the service in `AndroidManifest.xml`

```xml
<service
    android:name=".MyAdapterService"
    android:exported="true"
    android:icon="@drawable/ic_my_adapter"
    android:permission="com.asamm.locus.permission.SENSOR_ADAPTER">

    <intent-filter>
        <action android:name="locus.api.android.ACTION_SENSOR_ADAPTER_PARSER" />
    </intent-filter>

    <meta-data
        android:name="locus.api.android.SENSOR_ADAPTER"
        android:resource="@xml/locus_adapter" />
</service>
```

The action / permission / meta-data key are also constants on `LocusParserAdapterService`
(`ACTION_BIND`, `PERMISSION`, `META_DATA_KEY`).

## 3. Declare your device types in `res/xml/locus_adapter.xml`

```xml
<adapter
    apiVersion="1"
    id="com.example.myadapter/MyAdapter"
    displayName="My Adapter">

    <deviceType
        id="my-hrm"
        connectionType="BT4"
        displayName="My HRM">

        <refId variable="SENSOR_HEART_RATE" />
        <characteristic uuid="2a37" mode="NOTIFY" />
    </deviceType>
</adapter>
```

- **BT3** — `connectionType="BT3"`, no `<characteristic>`. Stream of bytes.
- **USB** — `connectionType="USB"` + `vendorId="…"` `productId="…"` (decimal or `0x`-hex).
- Full attribute list: [`manifest-schema.md`](manifest-schema.md). Curated refIds: [`locus-variables.md`](../../reference/locus-variables.md).

## 4. Implement the service

```kotlin
class MyAdapterService : LocusParserAdapterService() {

    override fun init(
        deviceId: String,
        deviceTypeId: String,
        bindContext: LocusBindContext,
    ): Int {
        return AdapterApi.INIT_OK
    }

    override fun parseData(
        deviceId: String,
        source: String,
        bytes: ByteArray,
    ): SensorValueBatch? {
        val hr = decodeHeartRate(bytes) ?: return null
        return SensorValueBatchBuilder(System.currentTimeMillis())
            .put(LocusVariable.HeartRate, hr)
            .build()
    }
}
```

- `deviceId` is the paired peer's stable id; `source` is the BT4 characteristic UUID, or `""` for
  stream transports.
- Return `null` when bytes are consumed without producing a value (partial frame).
- Full method semantics: [`aidl-contract.md`](aidl-contract.md).

## 5. Install both apps

Install Locus Map and your adapter app on the same device.

## 6. Pair in Locus

Open Locus's sensor picker → **External adapters** → your device type appears. Tap, scan, pair.
Values land in Locus's dashboards, track recording, and audio coach like any built-in sensor.

## What next

- Multiple device types under one app, multi-transport, write-backs, settings UI:
  [the sample app](../../../samples/android-sensor-adapter).
- Adapter-initiated writes (handshake / poll / event) via `writeData(deviceId, …)`:
  [`aidl-contract.md`](aidl-contract.md#write-backs).
