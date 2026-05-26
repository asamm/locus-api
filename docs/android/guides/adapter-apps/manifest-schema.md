# Adapter manifest schema

Each adapter app declares its presence to Locus via two pieces of XML:

1. A `<service>` entry in `AndroidManifest.xml` with the right intent-filter and
   permission gate.
2. A `<meta-data>` on that service pointing to a `res/xml/locus_adapter.xml`
   resource describing the adapter's **device-type catalog** — the kinds of
   hardware it knows how to handle. Locus reads this XML without binding the
   service, so the picker can show device types before the user commits.

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

Required attributes:

| Attribute / element | Why |
|---|---|
| `android:exported="true"` | Locus is a separate package — the service must be reachable across process boundaries. |
| `android:permission="com.asamm.locus.permission.SENSOR_ADAPTER"` | Only Locus (which holds this permission) can bind. Locus declares the permission with `protectionLevel="normal"` so third-party adapters can require it without code-signing constraints. |
| `<action android:name="locus.api.android.ACTION_SENSOR_ADAPTER_PARSER" />` | Discovery key — Locus queries `PackageManager.queryIntentServices` on this action to find adapters. |
| `<meta-data android:name="locus.api.android.SENSOR_ADAPTER" … />` | Points Locus at the device-type catalog XML below. |
| `android:icon` (optional) | Adapter icon shown in the picker. Locus loads it via `PackageManager.getServiceInfo(...).loadIcon(pm)` — standard Android drawable resource, no bytes over IPC. Falls back to the application icon when omitted. |

The action / meta-data key / permission constants are also exposed on
[`LocusParserAdapterService`](../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/parser/LocusParserAdapterService.kt)
(`ACTION_BIND`, `META_DATA_KEY`, `PERMISSION`) so you can reference them from
code rather than typing the strings.

## `res/xml/locus_adapter.xml`

```xml
<adapter
    xmlns:android="http://schemas.android.com/apk/res/android"
    apiVersion="1"
    id="com.example.bosch/EBikeAdapter"
    displayName="@string/adapter_display_name">

    <!-- One <deviceType> per kind of hardware this adapter knows.
         Multiple physical units share one <deviceType>; Locus persists each
         paired peer separately and identifies it on parseCharacteristic via
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

The `<adapter>` root carries **all static metadata Locus needs to populate the
picker** — Locus never binds the service just to read this. Bind happens only
when the user starts a sensor session.

### `<adapter>` attributes

| Attribute | Required | Notes |
|---|---|---|
| `apiVersion` | yes | The `AdapterApi.VERSION` value the adapter is built against. Locus rejects adapters whose `apiVersion` it can't speak — entirely pre-bind, so incompatible adapters never appear in the picker. Bumped on any breaking change to either the AIDL surface or the manifest XML format; the two evolve together. |
| `id` | yes | Stable adapter identifier. Convention: `{packageName}/{ServiceClassName}`. |
| `displayName` | yes | User-visible adapter name. Use `@string/...` for localization. |

### `<deviceType>` element

One per **kind** of hardware. Two physical Bosch eBikes paired to the same
adapter share one `<deviceType id="bosch-ldi">` entry and surface at runtime
as two separately-paired peers in Locus (each with its own BLE MAC, both
parsed via the same protocol declared here). Locus tracks per-peer pairing
on its own side; the adapter only learns about a peer via the `deviceId`
parameter on each `parseCharacteristic(...)` call.

| Attribute | Required | Notes |
|---|---|---|
| `id` | yes | Stable type identifier. Locus passes this back to the adapter as the `deviceTypeId` parameter on every `parseCharacteristic` call. |
| `displayName` | yes | User-visible type name shown in the picker pre-bind. |
| `icon` | no | Per-type picker icon — an `@drawable/...` resource in your adapter package. Locus loads it from your resources (no bytes over IPC). Falls back to the service/app icon (`android:icon`) when omitted, so multi-type adapters can give each hardware kind its own glyph. |
| `connectionType` | yes | The transport Locus drives for this device type. Currently `BT4`. |
| `scanFilter` | no | BLE name prefix Locus uses during pairing scans. Omit for scan-by-service-UUID only. |

### `<refId>` element (child of `<deviceType>`)

Lists the [`LocusVariable`](../../reference/locus-variables.md) refIds devices
of this type can produce. Use the symbolic name; Locus resolves it to the
integer refId. Adapter-side writes for refIds outside this declared set are
dropped on Locus's side.

### `<characteristic>` element (child of `<deviceType>`, BT4 only)

| Attribute | Required | Notes |
|---|---|---|
| `uuid` | yes | BLE GATT characteristic UUID. |
| `mode` | yes | One of `NOTIFY`, `READ_POLLED`, `WRITE`. |
| `pollIntervalMs` | only for `READ_POLLED` | How often Locus issues the GATT READ. |

Omit `<characteristic>` for non-BT4 device types.

## Runtime: from device types to paired peers

Locus's flow:

1. Discover adapters via `PackageManager.queryIntentServices` filtered by the
   `locus.api.android.ACTION_SENSOR_ADAPTER_PARSER` action.
2. Parse each adapter's `res/xml/locus_adapter.xml` (id / displayName /
   apiVersion / device-type list) and pull the service icon via
   `loadIcon(pm)` — no service bind. Each `<deviceType icon>` drawable is
   loaded from the adapter's resources at the same time.
3. Show the device-type list in the picker (one row per `<deviceType>` across
   all discovered adapters), each row using its `<deviceType icon>` when set,
   else the adapter's service/app icon.
4. User taps a device type → Locus runs a BLE scan using that type's
   `scanFilter` (BT4 device types) → user picks a BLE peer.
5. Locus persists pairing keyed on `(adapterId, deviceId)`, binds the adapter,
   subscribes to the type's `<characteristic>` declarations, and routes every
   GATT frame to `parseCharacteristic(deviceId, deviceTypeId, charUuid, bytes)`.

So XML covers the **type catalog** (static, pre-bind, no IPC). AIDL covers
**byte-frame parsing** at runtime. Locus owns peer discovery and pairing
persistence — the adapter never enumerates devices.

## Schema evolution

Add new optional attributes / elements freely — Locus's parser ignores unknown
nodes by design, so an adapter manifest written against a newer locus-api still
binds against an older Locus (the new fields just have no effect there).

Breaking changes ship under a new `AdapterApi.VERSION`.

## See also

- [`sample-manifest.xml`](sample-manifest.xml) — full annotated example
- [`aidl-contract.md`](aidl-contract.md) — runtime AIDL surface
- [`../../reference/locus-variables.md`](../../reference/locus-variables.md) — curated refId catalogue
