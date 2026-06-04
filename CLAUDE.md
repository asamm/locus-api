# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is — and what it isn't

This is the **integration surface** of Locus Map, not the app. It's the contract that third-party
Android apps depend on to read state from Locus, push data into it, or expose a sensor. No map
rendering lives here; nothing in `locus-core` (the actual Locus Map / GIS apps) lives here. Treat
every public class, AIDL interface, Parcelable field, manifest constant, and serialised byte
layout as an external contract — breaking it breaks every third-party app that targets a given
`API_CODE`. Bump versions and update [`CHANGELOG.md`](CHANGELOG.md) on every surface change.

The app-side counterparty (Locus Map / Locus GIS) lives in a separate, private repository.
Conventions from there — Compose, Hilt, Room, clean-architecture layering, library codes,
news-file changelogs — **do not apply here**. This repo is a small, focused library with its
own rules; the topical guides below replace any inherited convention.

## Topical guides — read before working in these areas

When the current task touches one of the rows below, **read the linked file before writing code**.

| Working on… | Read first | Hard rule (summary) |
|---|---|---|
| Sensor adapter framework, manifest XML schema, AIDL contract, refIds | [`docs/android/guides/adapter-apps/`](docs/android/guides/adapter-apps/) | Contract version is [`AdapterApi.VERSION`](locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/AdapterApi.kt) — bump **only** on breaking AIDL or manifest changes. Locus owns BT3/BT4/USB transport; adapter parses bytes via `parseData(deviceId, source, bytes)`. |
| Curated `LocusVariable` refIds (HeartRate, Cadence, Power, …) | [`docs/android/reference/locus-variables.md`](docs/android/reference/locus-variables.md) | Catalogued refIds are stable IDs Locus consumes — adding/renaming requires CHANGELOG entry and matching update on the Locus Map side. |
| Routing / compute-track provider (`ComputeTrackService`, `IComputeTrackService`, route types) | [`docs/android/guides/routing-apps/`](docs/android/guides/routing-apps/) | No contract-version constant — compatibility rests on append-only `Storable` rules for `ComputeTrackParameters` / `Track`. Params/Track ride as `Storable` bytes inside a `ParcelableContainer`, so the AIDL shape never changes. Build routes as `Track.points` (geometry) + `Track.waypoints` (instructions linked by `parameterRteIndex`). |
| Modifying `Storable` subclasses (Point, Track, GeoData, GeocachingData, …) | [`locus-api-core/src/main/java/locus/api/objects/Storable.kt`](locus-api-core/src/main/java/locus/api/objects/Storable.kt) | `getVersion()` is monotonic; **never** remove or reorder fields in `readObject` / `writeObject`. Add new fields only after the current version's last field, behind a `version >= N` check on read. Old clients must still parse new payloads. |
| AIDL interfaces under `locus-api-android/src/main/aidl/**` | [`docs/android/guides/adapter-apps/aidl-contract.md`](docs/android/guides/adapter-apps/aidl-contract.md) (sensor adapter) | AIDL method ordinals are part of the wire format — only append new methods; never reorder or remove. Use new methods on a new interface version when behaviour changes. |
| Intent / extra constants, `LocusConst`, action keys | [`locus-api-android/src/main/java/locus/api/android/utils/LocusConst.kt`](locus-api-android/src/main/java/locus/api/android/utils/LocusConst.kt) | Constants are wire-format strings consumed by released app builds — don't rename, only add. |
| Adding a new "Action…" entry point (point/track display, recording control, etc.) | [`locus-api-android/src/main/java/locus/api/android/ActionBasics.kt`](locus-api-android/src/main/java/locus/api/android/ActionBasics.kt) | Co-locate with related actions under `locus.api.android`; document required `VersionCode` and update CHANGELOG. |

## Build & version

This is a Gradle multi-project with two library modules plus two sample apps.

```bash
# Build everything (libraries + samples)
./gradlew clean assembleDebug

# Library-only builds
./gradlew :locus-api-core:build
./gradlew :locus-api-android:assembleRelease

# Sample apps
./gradlew :samples:android-api-explorer:assembleDebug
./gradlew :samples:android-sensor-adapter:assembleDebug

# Publish to local Maven (for testing against locus-core without a JitPack round-trip)
./gradlew :locus-api-core:publishToMavenLocal :locus-api-android:publishToMavenLocal
```

**No test suite ships with this repo.** There is no `:test` task and no `src/test/` tree —
verification is done downstream (in locus-core's tests and in the sample apps). Don't add
ceremony tests "for the sake of CI"; if a behaviour is worth testing, the right place is
locus-core's instrumentation suite, which actually exercises the IPC.

**Versions** live in [`gradle.properties`](gradle.properties):

| Property | Meaning |
|---|---|
| `API_CODE` | Monotonic int. Bump on **every release** — third-party apps gate features on `>= N`. |
| `API_VERSION` | Semver string used as the published Maven version and JitPack tag. |
| `PARAM_MIN_SDK_VERSION` | Currently 23 — **don't raise without strong reason**, third-party apps may target older minSdks than Locus Map itself. |
| `PARAM_COMPILE_SDK_VERSION` / `PARAM_TARGET_SDK_VERSION` | Track current Locus Map values. |
| `KOTLIN_VERSION`, `ANDROID_PLUGIN_GRADLE` | Conservative — apps consuming the AAR pick up our Kotlin stdlib transitively, so a major bump is a coordinated change. |

JDK 17. Kotlin 2.x. AGP 8.x.

## Architecture — the big picture

Two-module split, both published to JitPack under `com.github.asamm.locus-api`:

| Module | What | Depends on |
|---|---|---|
| **`locus-api-core`** | Pure Kotlin/Java domain objects + binary serialisation. No Android. Suitable for JVM/KMP. Contains `Storable`, `GeoData`/`Point`/`Track`, `GeocachingData`, `LineStyle`/`IconStyle`/`HotSpot`, `Location`, big-endian Data Reader/Writer, byte-value maps. | `kotlin-stdlib`, `logger-asamm` |
| **`locus-api-android`** | Android-specific façade: AIDL contracts, Parcelable transport, `Action*` entry points, sensor adapter framework, intent helpers, periodic-update receivers, map-tile / compute-track services. | `:locus-api-core` (api), `androidx.annotation` |

Apps depending on the library almost always pull `locus-api-android` — it transitively re-exports
`locus-api-core` as `api`. Pure-JVM consumers (rare) can take `locus-api-core` alone.

### How third-party apps actually talk to Locus

Two transport mechanisms live side by side. Don't conflate them:

1. **Parcel-encoded Intents / ContentProvider / `PackPoints`** — short, fire-and-forget messages:
   "display these points", "start recording", "open this point on the map". Code under
   `locus.api.android.Action*` and `locus.api.android.objects.PackPoints`. The on-wire format is
   `Storable.write`/`Storable.read` bytes wrapped in a Parcelable container
   ([`ParcelableContainer`](locus-api-android/src/main/java/locus/api/android/objects/ParcelableContainer.kt))
   so that adding fields on either side never breaks the Parcelable size budget.

2. **Bound AIDL services** — duplex, session-scoped IPC. Used for: compute-track providers, map
   tile providers, and the sensor adapter framework. Each lives in
   `locus.api.android.features.<area>` with its `*.aidl` peer.

The **`Storable`** pattern is the spine: subclasses override `getVersion()`, `readObject`,
`writeObject`. **Never reorder or remove fields** — only append, gated by `version >= N`. Old
Locus Map builds must still parse new objects produced by newer adapters and vice versa.

### Sensor adapter framework (shipped 0.10.0 — most recent surface addition)

Third-party apps can expose a sensor (real BLE strap, USB-NMEA GNSS dongle, virtual computed
metric, …) to Locus. The contract is:

- App declares device types in `res/xml/locus_adapter.xml` — root `<adapter apiVersion id displayName>`,
  one `<deviceType>` per kind of hardware (BT3/SPP, BT4/GATT, USB-serial).
- App provides a `LocusParserAdapterService` (subclass) implementing `ILocusSensorAdapterParser`.
- Locus binds the service, owns the transport, and calls `init` / `parseData(deviceId, source, bytes)` /
  `getIntentForSettings` / `shutdown`, all per-device.
- Adapter pushes typed values via `SensorValueBatchBuilder.put<HeartRate>(…)` etc. — refIds are
  curated in `docs/android/reference/locus-variables.md`.
- A reverse `ILocusSensorWriteChannel` lets the adapter initiate writes (handshakes, polls).

Full developer flow in [`docs/android/guides/adapter-apps/how-to.md`](docs/android/guides/adapter-apps/how-to.md).
Working sample in [`samples/android-sensor-adapter`](samples/android-sensor-adapter) (BT4 HRM
plus BT3/USB NMEA GNSS).

## ProGuard / R8

Apps consuming the API must keep API object class names. Document this in any user-facing setup
note; the README carries the canonical snippet:

```
-keep class locus.api.objects.** { *; }
```

The library itself does **not** ship a `consumer-rules.pro` — keep rules are the consumer's
responsibility.

## Release flow (maintainers)

1. Bump `API_CODE` (+1) and `API_VERSION` (semver) in `gradle.properties`.
2. Add a [`CHANGELOG.md`](CHANGELOG.md) block above the previous version's — `### Added` /
   `### Changed` / `### Fixed`, kept-a-changelog style.
3. Commit on `master`.
4. Tag with bare semver: `git tag 0.10.x && git push origin 0.10.x`.

JitPack picks up the tag automatically on the next dependent build — no GitHub Actions
workflow, no Nexus push for normal releases. The `nexus-staging` plugin in the root build is
historical and not part of the day-to-day flow.

## Kotlin & doc style

- **Function bodies:** use block body (`{ return … }`) whenever the body contains any function
  call. Expression body (`= …`) is only for trivial property-like accessors with no calls.
- **Guard clauses:** always use braces, even single-line. Split multi-condition guards
  (`&&` / `||`) one condition per line.
- **Named arguments:** keep them in the same order as the declared parameters unless there's a
  specific reason to reorder.
- **KDoc:** always multi-line block form (`/** … */`), even for a single sentence. 1 sentence
  for a function or property; up to 3–4 sentences for a complex class or module entry point.
  Omit entirely when nothing non-obvious remains. No `@param`/`@return` for self-evident names.
  Never `@author`/`@since`/`@version` — git blame covers that.
- **Constructor params:** never put `/** … */` inline on a constructor parameter. Document each
  one as an `@param <name>` line inside the class-level KDoc instead.
- **Comments:** placed on the line above the declaration, never end-of-line. One clause, rarely
  two sentences; state the gotcha and stop.
- **Java still present.** A handful of files (`DataReaderBigEndian.java`,
  `DataWriterBigEndian.java`, `SparseArrayCompat.java`, `MatrixCursor.java`) remain Java for
  binary-compat / JVM reasons. Leave them alone unless the change is the point of the task.
- **Markdown in `docs/`** wraps at ~100 chars (read in editors too). PR descriptions, GitHub
  releases, helpdesk replies are reflowed prose — don't pre-wrap those.

## Commit messages

Category prefix per logical change; for multiple related changes in one commit, list each on its
own line (or separated by ` - ` on one line):

| Prefix   | Use for                                         |
|----------|-------------------------------------------------|
| `- fix:` | Bug fix                                         |
| `- chg:` | Change / refactor / behaviour tweak / doc update |
| `- add:` | New feature, new file, new capability           |
| `- var:` | Various or version bump                         |

Don't use Conventional Commits (`feat:`, `perf:`, `docs:`). Don't insert `Co-Authored-By` lines
in regular commits.

**PR titles** have no prefix — start with a capital letter and describe the change directly.
The `fix:` / `chg:` / `add:` prefixes are for commit messages only.

**Branch naming:** `feature/<slug>`, `fix/<slug>`, `chore/<slug>` (always `feature/` in full,
never `feat/`). Lowercase-hyphen slug.

## Git & confirmation rules

- **Never run `git commit`, `git push`, or `git push --force` without explicit confirmation.**
  Show what would be committed (files, diff summary, message) and wait for a clear "yes" first.
  No exceptions — not when wrapping up, not when creating a PR, not when the change looks safe.
  Each commit needs its own confirmation; a "yes" doesn't roll over to the next one.
- **Never run multiple `git` commands in parallel.** `.git/index.lock` is a single lock and
  concurrent operations fail on Windows. Chain with `&&` or run sequentially.
