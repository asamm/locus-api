<p align="center">
    <a href="#what-this-is">What this is</a> | 
    <a href="#quick-start">Quick start</a> | 
    <a href="#scope">Scope</a> | 
    <a href="#sensor-adapter-apps">Sensor adapter apps</a> | 
    <a href="#documentation">Documentation</a> | 
    <a href="#release-maintainers">Release</a>
</p>

# Locus API

The integration surface for [Locus Map](https://www.locusmap.app) on Android — the contract third-party apps depend on to read state from Locus, push data into it, or expose a sensor.

[![Latest version](https://jitpack.io/v/asamm/locus-api.svg)](https://jitpack.io/#asamm/locus-api)

## What this is

A bound-service IPC + Parcelable data model. Three things you'd use it for:

- **Read / control Locus Map** — query active recording, points, tracks, user units; trigger actions like "start recording" or "open this point".
- **Push data in** — send points / tracks for import; post field notes.
- **Expose a sensor** — implement the [sensor adapter contract](#sensor-adapter-apps) so your app's sensor data feeds Locus's dashboards, recording and audio coach the same way a built-in BLE strap does.

## Quick start

Add JitPack to `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}
```

Add the dependency:

```kotlin
dependencies {
    // Android apps — pulls Locus API Core transitively
    implementation("com.github.asamm.locus-api:locus-api-android:0.10.1")

    // Pure Java / JVM, no Android (rare)
    // implementation("com.github.asamm.locus-api:locus-api-core:0.10.1")
}
```

Groovy DSL works the same: `maven { url 'https://jitpack.io' }` + `implementation 'com.github.asamm.locus-api:locus-api-android:0.10.1'`.

## Scope

| | |
|---|---|
| **Module split** | `locus-api-core` (pure Kotlin, KMP-friendly) + `locus-api-android` (Android-specific bindings, AIDL, Parcelables) |
| **What's IN** | Object transport (points / tracks / field notes), recording / navigation control, periodic updates, units, map previews, sensor adapter contract |
| **What's OUT** | Map rendering (this is not a Google Maps API substitute); standalone use (Locus Map must be installed) |

## Sensor adapter apps

Third-party Android apps can expose a sensor (real or virtual) to Locus Map. Locus owns the BT3 / BT4 / USB transport; your adapter declares its device types in `res/xml/locus_adapter.xml` and parses incoming bytes. Values land in dashboards, track recording and audio coach exactly like a built-in sensor.

- **Build from empty project →** [`docs/android/guides/adapter-apps/how-to.md`](docs/android/guides/adapter-apps/how-to.md) (six steps).
- **Working sample →** [`samples/android-sensor-adapter`](samples/android-sensor-adapter) (BT4 HRM + BT3 / USB NMEA GNSS).
- **Reference →** [`docs/android/guides/adapter-apps/`](docs/android/guides/adapter-apps/) — manifest schema, AIDL contract, curated refIds.

## Documentation

Documentation lives in this repo under [`docs/`](docs/) (versioned with the library, kept in sync via PR review). Some older notes still live on the [GitHub wiki](https://github.com/asamm/locus-api/wiki) and migrate into `docs/` as the relevant features are next touched — when both exist, prefer `docs/`.

| Path | Contents |
|---|---|
| [`docs/android/`](docs/android/) | Android-specific surface — AIDL contracts, manifest conventions, sensor adapter framework |
| [`docs/android/reference/`](docs/android/reference/) | Stable references — curated `LocusVariable` refIds |
| [`CHANGELOG.md`](CHANGELOG.md) | Per-release surface changes |
| `docs/kmp/` *(planned)* | KMP-shared types when the migration lands |

## ProGuard

When using R8 / ProGuard, keep API objects from being renamed:

```
-keep class locus.api.objects.** { *; }
```

## Release (maintainers)

1. Bump `API_CODE` (monotonic int) + `API_VERSION` (semver string) in `gradle.properties`.
2. Add a `CHANGELOG.md` entry above the previous version's block.
3. Commit on `master`.
4. Tag with bare semver (`git tag 0.10.2 && git push origin 0.10.2`). JitPack picks it up automatically on the next dependent build — no GitHub Actions workflow needed.
