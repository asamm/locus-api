# Locus sensor adapter apps

Adapter apps expose a sensor (real or virtual) to Locus Map. Locus owns the transport — BT3 / SPP,
BT4 / GATT, or USB-serial — your adapter declares its device types and parses bytes. Values land in
Locus's dashboards / track recording / audio coach like any built-in sensor.

| Topic | Doc |
|---|---|
| **Step-by-step (start here)** | [`how-to.md`](how-to.md) |
| **Working sample app** | [`samples/android-sensor-adapter`](../../../../samples/android-sensor-adapter) — BT4 HRM + BT3/USB NMEA GNSS, with manifests, parsers, and pair-in-Locus steps |
| Manifest XML schema | [`manifest-schema.md`](manifest-schema.md) |
| AIDL service contract | [`aidl-contract.md`](aidl-contract.md) |
| Curated refIds you can write to | [`../../reference/locus-variables.md`](../../reference/locus-variables.md) |
| Annotated sample manifest | [`sample-manifest.xml`](sample-manifest.xml) |

## Versioning

The contract version is [`AdapterApi.VERSION`](../../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/AdapterApi.kt),
bumped only on breaking AIDL or manifest changes. Adapters declare the version they target as
`<adapter apiVersion="…">`; Locus filters incompatible adapters out of the picker pre-bind. A
finer-grained runtime mismatch can be reported by returning `AdapterApi.INIT_INCOMPATIBLE_API`
from `init`.
