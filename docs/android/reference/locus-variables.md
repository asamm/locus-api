# LocusVariables — curated refIds

The integer refIds adapter apps are allowed to write to in Phase B v1. Each entry
maps to a built-in `Variable<T>` on the Locus-core side (same refId number, same
`T`).

Defined in
[`LocusVariables.kt`](../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/LocusVariables.kt).
Reference these from your adapter via the `@JvmField` constants, not by hard-
coding refIds:

```kotlin
batch.put(LocusVariables.SENSOR_HEART_RATE, 120)   // ✓
batch.put(LocusVariable<Int>(1500) { … }, 120)     // compile error — internal ctor
```

## Sensor refIds (v1)

| LocusVariable | refId | Type | Unit / Range | Notes |
|---|---|---|---|---|
| `SENSOR_HEART_RATE` | 1500 | `Int` | bpm, 25–250 | Standard heart-rate monitor. |
| `SENSOR_CADENCE` | 1501 | `Int` | rpm, 0–400 | Pedal cadence. |
| `SENSOR_SPEED` | 1502 | `Float` | m/s, 0–512 | Wheel-derived or GPS-substitute speed. |
| `SENSOR_STRIDES` | 1504 | `Long` | strides ≥ 0 | Walk-step counter. |
| `SENSOR_TEMPERATURE` | 1505 | `Float` | °C, -100..100 | Ambient temperature. |
| `SENSOR_HUMIDITY` | 1507 | `Float` | %, 0–100 | Ambient humidity. |
| `SENSOR_ASSIST_MODE` | 1508 | `String` | non-blank | E-bike assist mode label ("ECO", "TRAIL", "TURBO", …). |
| `SENSOR_BICYCLE_BATTERY` | 1509 | `Int` | %, 0–100 | E-bike battery state of charge. |
| `SENSOR_POWER` | 1510 | `Int` | W ≥ 0 | Instantaneous rider / motor power. |
| `SENSOR_BICYCLE_GEAR` | 1511 | `Int` | gear ≥ 0 | Currently-selected gear (Shimano STEPS, etc.). |
| `SENSOR_RANGE` | 1512 | `Float` | m ≥ 0 | Estimated remaining range. |

## Range validation

Locus's `Variable<T>.effectiveValidate` rejects out-of-range writes per the Unit
declared on each Variable. Writes that fail validation are silently dropped on
Locus's side and a warning is logged. Adapters should only emit values within
the declared range.

## Forward compatibility

New refIds land here only when the corresponding `Variables.X` ships in
Locus-core. Adapters built against a newer locus-api can declare refIds that
older Locus builds don't know about — those values are dropped on the older
Locus's side without error, so the adapter still works for the refIds the
older Locus does understand.

## Deferred — Phase D

Adapter-declared custom refIds (for fields not in this curated list) are
deferred to Phase D. Until then, adapter apps that need fields not represented
here should either:

- Map their data into the closest existing refId where the semantics fit, or
- Wait for Phase D and accept that this is when the feature ships.
