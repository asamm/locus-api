# LocusVariable — curated refIds

The Variables an adapter app can write to. Each maps to a built-in `Variable<T>` on the
Locus-core side (same refId, same `T`).

Defined in
[`LocusVariable.kt`](../../../locus-api-android/src/main/java/locus/api/android/features/sensorAdapter/LocusVariable.kt).
Reference them in code via the typed `LocusVariable` constants, and in your manifest XML
via the matching `variable` name (`<refId variable="SENSOR_HEART_RATE" />`). The
`LocusVariable` constructor is private — you use the listed constants, you can't invent
refIds:

```kotlin
batch.put(LocusVariable.HeartRate, 120)   // ✓ typed: an Int slot rejects a Float at compile time
```

| Kotlin constant | Manifest `variable` | refId | Type | Unit / Range | Notes |
|---|---|---|---|---|---|
| `LocusVariable.HeartRate` | `SENSOR_HEART_RATE` | 1500 | `Int` | bpm, 25–250 | Standard heart-rate monitor. |
| `LocusVariable.Cadence` | `SENSOR_CADENCE` | 1501 | `Int` | rpm, 0–400 | Pedal cadence. |
| `LocusVariable.Speed` | `SENSOR_SPEED` | 1502 | `Float` | m/s, 0–512 | Wheel-derived or GPS-substitute speed. |
| `LocusVariable.Strides` | `SENSOR_STRIDES` | 1504 | `Long` | strides ≥ 0 | Walk-step counter. |
| `LocusVariable.Temperature` | `SENSOR_TEMPERATURE` | 1505 | `Float` | °C, -100..100 | Ambient temperature. |
| `LocusVariable.Humidity` | `SENSOR_HUMIDITY` | 1507 | `Float` | %, 0–100 | Ambient humidity. |
| `LocusVariable.AssistMode` | `SENSOR_ASSIST_MODE` | 1508 | `String` | non-blank | E-bike assist mode label ("ECO", "TRAIL", "TURBO", …). |
| `LocusVariable.BicycleBattery` | `SENSOR_BICYCLE_BATTERY` | 1509 | `Int` | %, 0–100 | E-bike battery state of charge. |
| `LocusVariable.Power` | `SENSOR_POWER` | 1510 | `Int` | W ≥ 0 | Instantaneous rider / motor power. |
| `LocusVariable.BicycleGear` | `SENSOR_BICYCLE_GEAR` | 1511 | `Int` | gear ≥ 0 | Currently-selected gear (Shimano STEPS, etc.). |
| `LocusVariable.Range` | `SENSOR_RANGE` | 1512 | `Float` | m ≥ 0 | Estimated remaining range. |

## Range validation

Locus's `Variable<T>.effectiveValidate` rejects out-of-range writes per the Unit
declared on each Variable. Writes that fail validation are silently dropped on
Locus's side and a warning is logged. Adapters should only emit values within
the declared range.

## If your field isn't listed

Map your data to the closest listed Variable whose semantics fit. An adapter built
against a newer locus-api may also reference refIds an older Locus doesn't know — those
values are dropped on the older Locus without error, and the rest still work.
