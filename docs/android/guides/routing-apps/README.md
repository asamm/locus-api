# Locus routing provider apps

A routing provider app computes routes for Locus Map. Locus owns the UI (route planner,
navigation, the source picker); your app exposes a bound service that turns a list of via-points
into a navigable `Track` — geometry plus optional turn-by-turn instructions. The result feeds
Locus's map display, navigation, and recalculation like any built-in router.

This is the **compute-track** API (`ComputeTrackService` / `IComputeTrackService`) — historically
named after the `Track` it returns.

| Topic | Doc |
|---|---|
| **Step-by-step (start here)** | [`how-to.md`](how-to.md) |
| **Working sample app** | [`samples/android-compute-track`](../../../../samples/android-compute-track) — a wavy-line demo router with instructions, manifest, and select-in-Locus steps |
| AIDL service contract | [`aidl-contract.md`](aidl-contract.md) |
| Route types & instruction parameters | [`route-types.md`](route-types.md) |

## How it differs from a sensor adapter

Both are bound AIDL services Locus discovers by an intent action, but the shapes differ:

| | Routing provider | [Sensor adapter](../adapter-apps/) |
|---|---|---|
| Discovery | `ACTION_COMPUTE_TRACK_PROVIDER` intent-filter only | intent-filter **+ `<meta-data>` XML + permission** |
| Static metadata | reported over AIDL at bind (`getTrackTypes`, …) | declared in `res/xml/locus_adapter.xml`, read pre-bind |
| Call direction | Locus calls you once per route (`computeTrack`) | bidirectional, session-scoped, per device |
| Payload | `Storable` bytes in a `ParcelableContainer` | typed `SensorValueBatch` |

## Versioning

There is no separate contract-version constant for this API — it predates the
[`AdapterApi.VERSION`](../adapter-apps/README.md#versioning) scheme. Compatibility rests entirely
on the append-only [`Storable`](../../../../locus-api-core/src/main/java/locus/api/objects/Storable.kt)
rules: `ComputeTrackParameters` and `Track` only ever gain fields (gated by a `version >= N`
read check), and the AIDL methods are append-only. An old Locus build must still parse a `Track`
produced by a newer provider, and vice versa — never reorder or remove serialized fields.
