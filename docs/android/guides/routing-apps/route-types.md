# Route types & instruction parameters

Reference for the values a routing provider reads and writes. All constants live in
[`GeoDataExtra`](../../../../locus-api-core/src/main/java/locus/api/objects/extra/GeoDataExtra.kt);
turn actions live in
[`PointRteAction`](../../../../locus-api-core/src/main/java/locus/api/objects/extra/PointRteAction.kt).
The `parameter*` accessors below are extension properties in
[`GeoDataHelper.kt`](../../../../locus-api-core/src/main/java/locus/api/objects/geoData/GeoDataHelper.kt) —
prefer them over raw `addParameter(key, …)` calls.

## Route profiles (`VALUE_RTE_TYPE_*`)

Returned from `getTrackTypes()` and passed back in `ComputeTrackParameters.type`. The integer
ids are a stable wire contract — return only the ones your router actually supports.

| Constant | Id | Group |
|---|---:|---|
| `VALUE_RTE_TYPE_FOOT_01` | 3 | walk |
| `VALUE_RTE_TYPE_FOOT_02` | 10 | hiking |
| `VALUE_RTE_TYPE_FOOT_03` | 11 | mountain hiking / climb |
| `VALUE_RTE_TYPE_CYCLE` | 2 | generic cycle |
| `VALUE_RTE_TYPE_CYCLE_ROAD` | 9 | road |
| `VALUE_RTE_TYPE_CYCLE_GRAVEL` | 5 | gravel |
| `VALUE_RTE_TYPE_CYCLE_TOURING` | 13 | touring |
| `VALUE_RTE_TYPE_CYCLE_MTB` | 8 | mountain bike |
| `VALUE_RTE_TYPE_CAR` | 6 | generic car |
| `VALUE_RTE_TYPE_CAR_FAST` | 0 | car, time-optimized |
| `VALUE_RTE_TYPE_CAR_SHORT` | 1 | car, balanced |
| `VALUE_RTE_TYPE_MOTORCYCLE` | 7 | motorcycle |
| `VALUE_RTE_TYPE_SKI_CROSS_COUNTRY` | 12 | cross-country skiing |

`VALUE_RTE_TYPE_NO_TYPE` (100) is the "unspecified" fallback; `VALUE_RTE_TYPE_GENERATED` (-1)
marks a navigation-generated route. The canonical display order is `RTE_TYPES_SORTED`.

> **Deprecated profiles** — all replaced as noted, but mind the ids:
> - `VALUE_RTE_TYPE_CYCLE_RACING` (`9`) → `VALUE_RTE_TYPE_CYCLE_ROAD` — same id (`9`), pure alias.
> - `VALUE_RTE_TYPE_CYCLE_SHORT` (`5`) → `VALUE_RTE_TYPE_CYCLE_GRAVEL` — same id (`5`), pure alias.
> - `VALUE_RTE_TYPE_CYCLE_FAST` (`4`) → `VALUE_RTE_TYPE_CYCLE_ROAD` (`9`) — **different id**; this is
>   a remap, not an alias, so a stored `4` is not equal to the replacement.

## Track-level parameters

Set on the `Track`:

| Accessor | Key | Meaning |
|---|---|---|
| `Track.parameterRteComputeType` | `PAR_RTE_COMPUTE_TYPE` (120) | the profile the route was computed for (echo `params.type`) |

`Track.stats` (a `TrackStats`: `totalLength`, `totalTime`, `numOfPoints`, …) is **recomputed by
Locus** from the geometry when the track is received — anything you set is overwritten, so don't
bother filling it.

## Navigation waypoint parameters

A turn-by-turn instruction is a `Point` in `Track.waypoints`, positioned at the maneuver location.
Only two of its parameters survive into the route — the rest are recomputed or derived by Locus:

| Accessor | Key | Provider sets it? |
|---|---|---|
| `Point.parameterRteAction` | `PAR_RTE_POINT_ACTION` (110) | **Yes** — the maneuver (`PointRteAction`, see below); kept verbatim, never re-derived from geometry on the initial route |
| `Point.parameterRteIndex` | `PAR_RTE_INDEX` (100) | **Yes** — index into `Track.points` where the maneuver occurs; Locus validates it against the waypoint location and uses it as the primary waypoint→trackpoint match |
| `Point.parameterRteDistance` | `PAR_RTE_DISTANCE_F` (101) | No — recomputed from the geometry (overwritten) |
| `Point.parameterRteTime` | `PAR_RTE_TIME_I` (102) | Optional — derived from speed + distance; set it (or speed) only as an ETA seed |
| `Point.parameterRteSpeed` | `PAR_RTE_SPEED_F` (103) | Optional — ETA seed; Locus derives time from it |
| `addParameter(PAR_RTE_STREET, …)` | `PAR_RTE_STREET` (109) | Optional — next street / road name |

`PAR_RTE_INDEX` is the crux: `points` is pure geometry, `waypoints` carries the instructions, and
the index ties each instruction to the geometry vertex where it fires.

## Turn actions (`PointRteAction`)

`parameterRteAction` takes a `PointRteAction`. Common values:

| Action | When |
|---|---|
| `CONTINUE_STRAIGHT` | carry on / departure |
| `LEFT_SLIGHT` / `LEFT` / `LEFT_SHARP` | left turns by severity |
| `RIGHT_SLIGHT` / `RIGHT` / `RIGHT_SHARP` | right turns by severity |
| `U_TURN` / `U_TURN_LEFT` / `U_TURN_RIGHT` | reversals |
| `ROUNDABOUT_EXIT_1` … `ROUNDABOUT_EXIT_8` | roundabout, by exit number — `getActionRoundabout(exitNo)` |
| `ARRIVE_DEST` / `ARRIVE_DEST_LEFT` / `ARRIVE_DEST_RIGHT` | destination reached |
| `PASS_PLACE` | via-point passed, no turn |
| `NO_MANEUVER` / `UNDEFINED` | none / fallback |

Two helpers turn a geometry into actions without a lookup table:

- `PointRteAction.getActionByAngle(angle)` — maps a `0..360°` heading change to the right turn
  severity (`< 30°` straight, right turns up to `180°`, left turns above). The sample uses this
  at each via-point.
- `PointRteAction.getActionRoundabout(exitNo)` — the roundabout action for exit `1..8`.

See `WavyRouteService.buildInstructions` in
[`samples/android-compute-track`](../../../../samples/android-compute-track) for a working
example of all of the above.
