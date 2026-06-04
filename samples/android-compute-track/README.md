# Locus routing-provider sample

A minimal, self-contained Android app that acts as a **routing provider** for Locus Map through
the compute-track API. Locus owns the route planner, navigation, and the source picker; Locus
binds this app's service and calls it to turn a list of via-points into a navigable `Track`.

It is a deliberately "dumb" router — between via-points it draws a **wavy** line (a sine wiggle
around the straight segment) instead of following roads, so you can see at a glance the route was
*computed* and isn't just a straight line. The focus is the API contract a real provider must
satisfy. Use it as the starting template: keep the service shell and replace the
geometry-and-maneuver computation in `computeTrack` with a real router (GraphHopper, BRouter, an
online service, …).

## What's in here

| File | Purpose |
|---|---|
| `src/main/java/.../WavyRouteService.kt` | The provider — reports capabilities, builds the geometry, and emits turn-by-turn instructions in `computeTrack`. |
| `src/main/AndroidManifest.xml` | Registers the service with the `ACTION_COMPUTE_TRACK_PROVIDER` intent-filter (discovery is by action alone — no `<meta-data>`, no permission). |
| `src/main/java/.../InfoActivity.kt` | A static info screen — the app itself has no runtime UI. Also the `intentForSettings` target. |

The full contract is documented under
[`docs/android/guides/routing-apps/`](../../docs/android/guides/routing-apps/) in this repo.

## Build & run

From the repo root:

```
./gradlew :samples:android-compute-track:assembleDebug
```

Install it alongside Locus Map, then in Locus open the route planner / navigation → choose the
routing source → **Wavy sample router**, and plan a route between two or more points.

## Using this as a template outside the repo

This module builds against the in-repo `:locus-api-android` project. To lift it out as a
standalone project:

1. In `build.gradle`, drop the `implementation project(':locus-api-android')` line and use the
   published artifact from [JitPack](https://jitpack.io/#asamm/locus-api):
   `implementation 'com.github.asamm.locus-api:locus-api-android:<version>'`.
2. Replace the `PARAM_*_SDK_VERSION` / `KOTLIN_VERSION` / `API_VERSION` property references with
   literal values (they come from this repo's `gradle.properties`).
