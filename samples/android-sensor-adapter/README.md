# Locus sensor-adapter sample (HRM)

A minimal, self-contained Android app that exposes a **standard BLE Heart Rate strap** (Polar,
Wahoo, Garmin, Suunto, …) to Locus Map through the parser-style sensor-adapter SDK. Locus owns the
BT4 scan and GATT lifecycle; this app only decodes the byte frames.

Use it as the starting template for your own adapter: it's one service (~90 lines), one device-type
manifest, and one info screen.

## What's in here

| File | Purpose |
|---|---|
| `src/main/java/.../HrmAdapterService.kt` | The parser — decodes the Heart Rate Measurement characteristic (0x2A37) and emits `SENSOR_HEART_RATE`. |
| `src/main/res/xml/locus_adapter.xml` | Device-type catalogue Locus reads pre-bind (no service bind needed). |
| `src/main/AndroidManifest.xml` | Registers the service with the adapter intent-filter, the `SENSOR_ADAPTER` permission, and the `<meta-data>` pointer to the catalogue. |
| `src/main/java/.../InfoActivity.kt` | A static info screen — the app itself has no runtime UI. |

The full contract is documented under
[`docs/android/guides/adapter-apps/`](../../docs/android/guides/adapter-apps/) in this repo.

## Build & run

From the repo root:

```
./gradlew :samples:android-sensor-adapter:assembleDebug
```

Install it alongside Locus Map, then in Locus open the sensor picker → add sensor →
**External adapters** → **Heart rate monitor**.

## Using this as a template outside the repo

This module builds against the in-repo `:locus-api-android` project. To lift it out as a
standalone project:

1. In `build.gradle`, drop the `implementation project(':locus-api-android')` line and use the
   published artifact from [JitPack](https://jitpack.io/#asamm/locus-api):
   `implementation 'com.github.asamm.locus-api:locus-api-android:<version>'`.
2. Replace the `PARAM_*_SDK_VERSION` / `KOTLIN_VERSION` / `API_VERSION` property references with
   literal values (they come from this repo's `gradle.properties`).
