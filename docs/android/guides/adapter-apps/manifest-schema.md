# Adapter manifest schema

Two pieces of XML:

1. A `<service>` entry in `AndroidManifest.xml` (intent-filter + permission gate).
2. `res/xml/locus_adapter.xml` — the adapter's **device-type catalog**, pointed at by `<meta-data>`
   on the service. Locus reads it without binding, so the picker is populated pre-bind.

## `AndroidManifest.xml`

```xml
<application … >
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
</application>
```

| Attribute / element | Why |
|---|---|
| `android:exported="true"` | Locus is a separate package; the service must be reachable cross-process. |
| `android:permission="com.asamm.locus.permission.SENSOR_ADAPTER"` | Only Locus (which holds this permission) can bind. |
| `<action android:name="locus.api.android.ACTION_SENSOR_ADAPTER_PARSER" />` | Discovery key — Locus queries `queryIntentServices` on this action. |
| `<meta-data android:name="locus.api.android.SENSOR_ADAPTER" … />` | Points at the device-type catalog XML. |
| `android:icon` (optional) | Picker icon; falls back to the application icon. |

The action, permission, and meta-data key are also constants on `LocusParserAdapterService`
(`ACTION_BIND`, `PERMISSION`, `META_DATA_KEY`).

## `res/xml/locus_adapter.xml`

```xml
<adapter
    xmlns:android="http://schemas.android.com/apk/res/android"
    apiVersion="1"
    id="com.example.bosch/EBikeAdapter"
    displayName="@string/adapter_display_name">

    <!-- One <deviceType> per kind of hardware this adapter knows.
         Multiple physical units share one <deviceType>; Locus persists each
         paired peer separately and identifies it on parseData via
         the deviceId parameter. -->
    <deviceType
        id="bosch-ldi"
        displayName="Bosch LDI"
        icon="@drawable/ic_bosch_ldi"
        connectionType="BT4"
        scanFilter="BoschEBike-">

        <refId variable="SENSOR_HEART_RATE" />
        <refId variable="SENSOR_CADENCE" />
        <refId variable="SENSOR_POWER" />
        <refId variable="SENSOR_BICYCLE_BATTERY" />

        <characteristic
            uuid="0000eb21-eaa2-11e9-81b4-2a2ae2dbcce4"
            mode="NOTIFY" />
    </deviceType>
</adapter>
```

### `<adapter>` attributes

| Attribute | Required | Notes |
|---|---|---|
| `apiVersion` | yes | The `AdapterApi.VERSION` the adapter is built against. Mismatched adapters are filtered out of the picker pre-bind. Bumped on breaking AIDL or XML changes. |
| `id` | yes | Stable adapter identifier. Convention: `{packageName}/{ServiceClassName}`. |
| `displayName` | yes | User-visible name. Use `@string/...` for localization. |

### `<deviceType>` element

One per **kind** of hardware. Multiple physical units share one `<deviceType>` and surface as
separately-paired peers; the adapter learns each peer's id only via the `deviceId` parameter on
`init` / `parseData`.

| Attribute | Required | Notes |
|---|---|---|
| `id` | yes | Stable type identifier. Locus passes this back as `deviceTypeId` to `init`. |
| `displayName` | yes | User-visible name shown in the picker pre-bind. |
| `icon` | no | Per-type picker icon (`@drawable/...`). Falls back to the service/app icon when omitted. |
| `connectionType` | yes | `BT3` (SPP stream), `BT4` (GATT — uses `<characteristic>` children), or `USB` (serial — see USB attributes below). `NET` is reserved in the enum but not driven yet; a `NET` device type is parsed and skipped. |
| `scanFilter` | no | BLE advertisement **name prefix** for pairing scans (BT3 / BT4 only). |
| `scanServiceUuid` | no | BLE **advertised service UUID** for pairing scans (BT4 only). Uses Android's native `ScanFilter.setServiceUuid(...)` — radio-level filtering, more reliable and battery-efficient than name-prefix matching. Use this for vendors that advertise a proprietary service UUID (Brose, Bosch LDI, …). |

Both optional. Both present → AND. Neither → scan-all (same as the built-in HRM picker). Set at least one whenever the vendor publishes a distinctive name or service UUID — faster pairing, less picker noise.

### `<refId>` element (child of `<deviceType>`)

Lists the [`LocusVariable`](../../reference/locus-variables.md) refIds this device type produces.
Use the symbolic name (`SENSOR_HEART_RATE`, …); writes for refIds outside this set are dropped.

### `<characteristic>` element (child of `<deviceType>`, BT4 only)

| Attribute | Required | Notes |
|---|---|---|
| `uuid` | yes | BLE GATT characteristic UUID. |
| `mode` | yes | One of `NOTIFY`, `READ_POLLED`, `WRITE`. |
| `pollIntervalMs` | only for `READ_POLLED` | How often Locus issues the GATT READ. |

Omit `<characteristic>` for non-BT4 device types.

### USB attributes (on `<deviceType connectionType="USB">`)

Carried as attributes on `<deviceType>` itself — no child element.

| Attribute | Required | Notes |
|---|---|---|
| `vendorId` | yes | USB vendor id (16-bit, `0..65535`). Decimal or `0x`-hex. Fed to Locus's CDC-ACM serial prober. |
| `productId` | yes | USB product id (16-bit). |
| `baudRate` | no | Defaults to `4800` (GNSS-typical). |
| `dataBits` | no | 5–8. Defaults to `8`. |
| `stopBits` | no | `1`, `2`, or `3` (= 1.5). Defaults to `1`. |
| `parity` | no | `0` none, `1` odd, `2` even, `3` mark, `4` space. Defaults to `0`. |

A USB device type missing or out-of-range on `vendorId` / `productId` is dropped with a warning.
Locus owns the connection and permission prompt; the adapter app needs no USB host permissions.

## Schema evolution

Add new optional attributes/elements freely — unknown nodes are ignored. Breaking changes ship
under a new `AdapterApi.VERSION`.

## See also

- [`sample-manifest.xml`](sample-manifest.xml) — full annotated example
- [`aidl-contract.md`](aidl-contract.md) — runtime AIDL surface
- [`../../reference/locus-variables.md`](../../reference/locus-variables.md) — curated refId catalogue
