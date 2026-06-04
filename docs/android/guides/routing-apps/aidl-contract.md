# Compute-track AIDL contract

The bound-service interface
[`IComputeTrackService`](../../../../locus-api-android/src/main/aidl/locus/api/android/features/computeTrack/IComputeTrackService.aidl)
plus the `Storable` payloads it carries. Subclass
[`ComputeTrackService`](../../../../locus-api-android/src/main/java/locus/api/android/features/computeTrack/ComputeTrackService.kt)
and you write no binder code — it wires the AIDL stub to abstract Kotlin members and handles the
byte (de)serialization.

## Bind lifecycle

1. Locus discovers providers via `queryIntentServices` on the
   `locus.api.android.ACTION_COMPUTE_TRACK_PROVIDER` action. There is no `<meta-data>` to parse —
   all metadata comes over AIDL after bind.
2. User opens the routing-source picker. For each provider Locus binds the service and reads
   `getAttribution` / `getTrackTypes` / `getNumOfTransitPoints` to populate the row.
3. User plans a route → Locus calls `computeTrack` (on a **background thread**), once per route
   request. Multi-segment plans call it once per segment.
4. The binding is cached and reused across requests; Locus unbinds when the source is no longer
   needed.

## The `ParcelableContainer` transport

`computeTrack` does not pass `ComputeTrackParameters` / `Track` directly over AIDL. Both are
serialized with the [`Storable`](../../../../locus-api-core/src/main/java/locus/api/objects/Storable.kt)
big-endian byte protocol and wrapped in a
[`ParcelableContainer`](../../../../locus-api-android/src/main/java/locus/api/android/objects/ParcelableContainer.kt)
— a Parcelable that carries nothing but a `byte[]`:

```
ComputeTrackParameters --asBytes--> ParcelableContainer --AIDL--> read() --> Track --asBytes--> ParcelableContainer
```

This is the key design decision: the AIDL signature never changes shape as fields are added to
the parameters or the track. Versioning lives entirely inside the `Storable` stream, so an old
Locus and a new provider (or vice-versa) stay wire-compatible. `ComputeTrackService` hides the
container entirely — you only ever see `ComputeTrackParameters` in and `Track` out.

## Methods

### `getAttribution`

```kotlin
override val attribution: String
    get() = "Routed by <b>My Router</b>"
```

Visible credit for the route source. Basic HTML tags are rendered.

### `getTrackTypes`

```kotlin
override val trackTypes: IntArray
    get() = intArrayOf(GeoDataExtra.VALUE_RTE_TYPE_FOOT_01, GeoDataExtra.VALUE_RTE_TYPE_CAR)
```

Profiles you support, as `GeoDataExtra.VALUE_RTE_TYPE_*` ids. Locus shows these in the picker and
passes the chosen one back as `ComputeTrackParameters.type`. Catalogue:
[`route-types.md`](route-types.md).

### `getNumOfTransitPoints`

```kotlin
override val numOfTransitPoints: Int
    get() = 100
```

How many via-points (between start and end) the provider accepts. Defaults to `0` — start/end
only. **Override to advertise via-point support.**

> In earlier releases this member was a final `val` and could not be overridden — a provider that
> supported via-points had no way to say so. It is now `open`. If you target an older
> `locus-api-android` that still has it `final`, you cannot raise it above `0`.

### `getIntentForSettings`

```kotlin
override val intentForSettings: Intent
    get() = Intent(this, SettingsActivity::class.java)
```

Intent Locus launches for the provider's settings screen. The member is non-null, so a provider
with no settings must still return *something* (point it at an info Activity, as the sample does).

### `computeTrack`

```kotlin
override fun computeTrack(lv: LocusVersion?, params: ComputeTrackParameters): Track? {
    …
    return track  // or null on failure
}
```

The one method that does work. Called on a background thread, once per route request.

- `lv` — which Locus instance asked (resolved by the base class via
  `LocusUtils.getActiveVersion`; `null` short-circuits to a `null` result before your code runs).
- `params` — see below.
- **Return** the computed `Track`, or `null` to signal failure. Locus then offers another router.
  Don't throw; the base class catches and logs, but returning `null` deliberately is clearer.

#### `ComputeTrackParameters`

| Field | Meaning |
|---|---|
| `type` | Chosen profile (a `VALUE_RTE_TYPE_*` from your `trackTypes`). |
| `locations` | `Array<Location>` — start, optional via-points, end (always ≥ 2). |
| `isComputeInstructions` | `true` if Locus wants turn-by-turn waypoints, not just geometry. |
| `currentDirection` / `hasDirection` | User's current heading [°] at the start, when known — use it to choose the first maneuver (e.g. a U-turn if the route heads behind the user). |

#### Building the returned `Track`

See [`how-to.md` step 4](how-to.md#4-build-the-track) and [`route-types.md`](route-types.md). In
short: fill `points` (geometry), set `parameterRteComputeType`, and optionally add `waypoints`
(instructions — the maneuver `parameterRteAction` plus `parameterRteIndex` linking each to a
`points` index). Do **not** bother filling `stats` or per-waypoint distance / time: Locus
recomputes them from the geometry when it validates the received track and overwrites whatever you
set. `parameterRteAction` is the one waypoint field kept verbatim.
