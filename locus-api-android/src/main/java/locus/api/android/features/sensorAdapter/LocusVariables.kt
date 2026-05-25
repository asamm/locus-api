/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter

/**
 * Curated registry of [LocusVariable]s that adapter apps are allowed to write to in
 * v1. Adapters reference these constants in [SensorValueBatchBuilder.put] calls; the
 * typed shape ensures the `value` parameter matches each Variable's declared `T` at
 * compile time.
 *
 * RefIds are stable integers shared with Locus-core's `Variables.X.id` — same number
 * on both sides of the AIDL boundary. New refIds land here only when the
 * corresponding `Variables.X` ships in Locus-core too.
 *
 * Phase D (inline custom Variables) will add a separate path for adapter-declared
 * refIds that aren't in this list.
 */
object LocusVariables {

    //*************************************************
    // SENSORS — IDs match Locus-core's Variables.SENSOR_*
    //*************************************************

    @JvmField
    val SENSOR_ASSIST_MODE = LocusVariable(1508) { it }

    @JvmField
    val SENSOR_BICYCLE_BATTERY = LocusVariable(1509) { it.toIntOrNull() }

    @JvmField
    val SENSOR_BICYCLE_GEAR = LocusVariable(1511) { it.toIntOrNull() }

    @JvmField
    val SENSOR_CADENCE = LocusVariable(1501) { it.toIntOrNull() }

    @JvmField
    val SENSOR_HEART_RATE = LocusVariable(1500) { it.toIntOrNull() }

    @JvmField
    val SENSOR_HUMIDITY = LocusVariable(1507) { it.toFloatOrNull() }

    @JvmField
    val SENSOR_POWER = LocusVariable(1510) { it.toIntOrNull() }

    @JvmField
    val SENSOR_RANGE = LocusVariable(1512) { it.toFloatOrNull() }

    @JvmField
    val SENSOR_SPEED = LocusVariable(1502) { it.toFloatOrNull() }

    @JvmField
    val SENSOR_STRIDES = LocusVariable(1504) { it.toLongOrNull() }

    @JvmField
    val SENSOR_TEMPERATURE = LocusVariable(1505) { it.toFloatOrNull() }

    //*************************************************
    // LOOKUP
    //*************************************************

    private val byId: Map<Int, LocusVariable<*>> = listOf(
        SENSOR_ASSIST_MODE,
        SENSOR_BICYCLE_BATTERY,
        SENSOR_BICYCLE_GEAR,
        SENSOR_CADENCE,
        SENSOR_HEART_RATE,
        SENSOR_HUMIDITY,
        SENSOR_POWER,
        SENSOR_RANGE,
        SENSOR_SPEED,
        SENSOR_STRIDES,
        SENSOR_TEMPERATURE,
    ).associateBy { it.refId }

    /**
     * Look up the [LocusVariable] for [refId], or `null` for unknown ids.
     * Used by Locus when applying a received [SensorValueBatch] — for each
     * `(refId, stringValue)` pair, Locus resolves the typed LocusVariable and uses
     * its parser to recover the value before dispatching into the DataContainer.
     */
    @JvmStatic
    fun findById(refId: Int): LocusVariable<*>? {
        return byId[refId]
    }
}
