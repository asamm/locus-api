# Locus API documentation

Developer documentation for the Locus public API surface — the contracts third-party
apps depend on to talk to Locus Map.

The tree mirrors the per-platform module split: AIDL services, parcelables, and
Android-specific bindings live under `android/`; the KMP-shared core (when its
docs land) will live under `kmp/`; iOS later under `ios/`.

| Subdirectory | What's in it |
|---|---|
| [`android/`](android/) | Android-specific surface — AIDL services (sensor adapter, compute-track, map-tile), parcelables, manifest conventions |
| `kmp/`                 | _placeholder_ — KMP-shared types (Storable / `Location` / `Track` / `GeoDataExtra`) when the migration lands |
| `ios/`                 | _placeholder_ — iOS bindings when they arrive |

The repo-level [`README.md`](../README.md) covers building, publishing, and
versioning. The [`CHANGELOG.md`](../CHANGELOG.md) tracks per-release surface
changes.
