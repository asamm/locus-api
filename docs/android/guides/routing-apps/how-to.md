# How to build a Locus routing provider

Five steps from an empty Android project to your router appearing in Locus's routing-source
picker. The full, runnable version of this is
[`samples/android-compute-track`](../../../../samples/android-compute-track).

## 1. Add the dependency

In `build.gradle.kts`:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.asamm.locus-api:locus-api-android:0.10.1")
}
```

## 2. Declare the service in `AndroidManifest.xml`

Discovery is by intent action alone — no `<meta-data>`, no permission. Capabilities are reported
over AIDL at bind time (step 3), not declared here.

```xml
<service
    android:name=".MyRouteService"
    android:exported="true"
    android:label="@string/service_name">

    <intent-filter>
        <action android:name="locus.api.android.ACTION_COMPUTE_TRACK_PROVIDER" />
    </intent-filter>
</service>
```

The action is also `LocusConst.ACTION_SERVICE_COMPUTE_TRACK_PROVIDER`.

> **Older Locus builds (Android 11+ package visibility).** Recent Locus Map builds query
> `ACTION_COMPUTE_TRACK_PROVIDER` directly, so a service-only declaration is discovered out of the
> box. On older builds whose `<queries>` predates that entry, package visibility is per-package and
> a provider whose *only* Locus-facing component is this service stays invisible. For
> back-compatibility, also expose a component matching an action Locus already queries — e.g. an
> Activity with an `INTENT_ITEM_MAIN_FUNCTION` filter — which makes the whole package, service
> included, visible.

## 3. Implement the service

Subclass `ComputeTrackService` — it hosts the AIDL stub and does the byte (de)serialization, so
you implement four capability members plus `computeTrack`:

```kotlin
class MyRouteService : ComputeTrackService() {

    override val attribution: String
        get() = "Routed by <b>My Router</b>"  // basic HTML allowed

    // profiles you support; Locus passes the chosen one back as params.type
    override val trackTypes: IntArray
        get() = intArrayOf(
            GeoDataExtra.VALUE_RTE_TYPE_FOOT_01,
            GeoDataExtra.VALUE_RTE_TYPE_CYCLE,
            GeoDataExtra.VALUE_RTE_TYPE_CAR,
        )

    // 0 = start/end only; override to accept via-points in between
    override val numOfTransitPoints: Int
        get() = 100

    override val intentForSettings: Intent
        get() = Intent(this, InfoActivity::class.java)

    override fun computeTrack(lv: LocusVersion?, params: ComputeTrackParameters): Track? {
        // params.locations is start … (vias) … end, params.type is the chosen profile
        return Track().apply {
            points = computeGeometry(params.locations)        // List<Location>
            parameterRteComputeType = params.type
            if (params.isComputeInstructions) {
                waypoints = computeInstructions(params.locations) // List<Point>
            }
            // No track statistics here — Locus recomputes them from the geometry on receipt.
        }
    }
}
```

Return `null` on any failure — Locus degrades to "no route" and offers another router. Never let
an exception escape the binder. (The base class already wraps `computeTrack` in a catch-all, but
returning `null` deliberately is clearer than relying on it.)

## 4. Build the `Track`

A route is two parallel structures:

- **`Track.points`** — `List<Location>`, the raw geometry Locus draws on the map.
- **`Track.waypoints`** — `List<Point>`, one per maneuver, carrying turn-by-turn instructions.
  Each waypoint sets the maneuver (`parameterRteAction`) and links back to the geometry by
  `parameterRteIndex` (the index in `points` where the maneuver happens).

**Let Locus do the arithmetic.** When it receives the track, Locus recomputes track statistics
(`TrackStats` — total distance, duration) and per-waypoint distance / time from the geometry,
overwriting anything you set. So a provider only owns the geometry, the route type, and the
maneuvers — don't waste cycles computing the rest. (For an ETA, optionally seed
`parameterRteSpeed` per waypoint; Locus derives per-segment time from it.) `parameterRteAction`
is the exception — it is kept verbatim, never re-derived from geometry on the initial route.

Full parameter list and the `PointRteAction` turn catalogue: [`route-types.md`](route-types.md).
The sample's `WavyRouteService.computeTrack` is a complete, commented implementation.

## 5. Install both apps and select the router

Install Locus Map and your app on the same device. In Locus, open the route planner /
navigation, choose the routing source, and pick your service. Plan a route — your `computeTrack`
runs on a Locus background thread and the returned `Track` is shown and made navigable.

## What next

- The full sample with wavy-line geometry + computed instructions:
  [`samples/android-compute-track`](../../../../samples/android-compute-track).
- Method-by-method semantics, threading, the `ParcelableContainer` transport, error handling:
  [`aidl-contract.md`](aidl-contract.md).
