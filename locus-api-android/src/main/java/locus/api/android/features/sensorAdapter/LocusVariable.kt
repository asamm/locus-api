/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter

/**
 * Curated catalogue of Variables an adapter is allowed to write to, modelled
 * as a sealed hierarchy so consumers get exhaustive `when` over the set at compile
 * time. Each nested `object` carries a stable [refId] (matching the Locus-core
 * `Variables.X.id` value) plus a locale-independent `String → T` parser.
 *
 * Adapter authors reference these directly:
 *
 * ```
 * batch.put(LocusVariable.HeartRate, 120)
 * batch.put(LocusVariable.Speed, 7.4f)
 * ```
 *
 * The sealed parent constructor is private — adapters can't invent new refIds; they
 * reference the listed constants only.
 *
 * Three names per Variable:
 *
 * - **Kotlin identifier** (e.g. `HeartRate`) — PascalCase, idiomatic. Used in
 *   `LocusVariable.HeartRate` references.
 * - **Manifest XML name** (e.g. `SENSOR_HEART_RATE`) — SCREAMING_SNAKE, carried on each
 *   object as [xmlName] and written in `res/xml/locus_adapter.xml` as
 *   `<refId variable="SENSOR_HEART_RATE" />`. Looked up at parse time via [findByName].
 * - **refId** (e.g. `1500`) — integer wire format on the AIDL boundary. Round-trips
 *   `Map<Int, String>` in `SensorValueBatch.values`. Looked up via [findById].
 *
 * New built-in Variables added here need a matching nested object plus an entry in the
 * [all] list; both [findById] and [findByName] derive from [all].
 */
sealed class LocusVariable<T : Any>(
    val refId: Int,
    val xmlName: String,
    private val parser: (String) -> T?,
) {

    /**
     * Recover the typed value from its locale-independent string wire form. Used by
     * Locus when applying a received `SensorValueBatch` — Locus picks the typed
     * `SensorValue` slot for each refId and writes [parse]'s result. Returns `null`
     * on parse failure; Locus drops the value with a warning. Adapter authors never
     * call this directly — `SensorValueBatchBuilder.put` does the symmetric
     * `value.toString()` for them.
     */
    fun parse(value: String): T? {
        return parser(value)
    }

    //*************************************************
    // CURATED CATALOGUE
    //*************************************************

    /**
     * Current e-bike assist mode as a free-form label (e.g. `"TRAIL"`, `"BOOST"`,
     * `"TOUR"`). Vendor-specific terminology — Locus shows the string verbatim.
     */
    object AssistMode : LocusVariable<String>(1508, "SENSOR_ASSIST_MODE", { it })

    /** Bicycle battery state of charge in percent (0–100). */
    object BicycleBattery : LocusVariable<Int>(1509, "SENSOR_BICYCLE_BATTERY", String::toIntOrNull)

    /** Selected gear number on bikes that expose it (e.g. Shimano STEPS, 1-based). */
    object BicycleGear : LocusVariable<Int>(1511, "SENSOR_BICYCLE_GEAR", String::toIntOrNull)

    /** Cadence in revolutions per minute. */
    object Cadence : LocusVariable<Int>(1501, "SENSOR_CADENCE", String::toIntOrNull)

    /** Heart rate in beats per minute. */
    object HeartRate : LocusVariable<Int>(1500, "SENSOR_HEART_RATE", String::toIntOrNull)

    /** Relative humidity in percent (0–100). */
    object Humidity : LocusVariable<Float>(1507, "SENSOR_HUMIDITY", String::toFloatOrNull)

    /** Instantaneous power in watts. */
    object Power : LocusVariable<Int>(1510, "SENSOR_POWER", String::toIntOrNull)

    /** Estimated remaining range in metres (e-bike, electric vehicle). */
    object Range : LocusVariable<Float>(1512, "SENSOR_RANGE", String::toFloatOrNull)

    /** Speed in metres per second. */
    object Speed : LocusVariable<Float>(1502, "SENSOR_SPEED", String::toFloatOrNull)

    /** Stride count, monotonically increasing per pairing session. */
    object Strides : LocusVariable<Long>(1504, "SENSOR_STRIDES", String::toLongOrNull)

    /** Temperature in degrees Celsius. */
    object Temperature : LocusVariable<Float>(1505, "SENSOR_TEMPERATURE", String::toFloatOrNull)

    //*************************************************
    // LOOKUP
    //*************************************************

    companion object {

        /**
         * Every built-in variable in declaration order. Useful for Locus-side code
         * that needs the full set without enumerating each by name (e.g. building
         * `LocusBindContext.supportedRefIds`).
         *
         * `by lazy` matters: the parent's `<clinit>` runs the first time *any* nested
         * object is touched. If `all` were eagerly initialised here it would walk the
         * `listOf(…)` and hit the nested object that's currently mid-`<clinit>` as a
         * null INSTANCE (recursive same-thread re-entry returns the not-yet-assigned
         * field value), producing a `NullPointerException` in `byId.associateBy`.
         * Deferring evaluation until first call site means every nested object is
         * fully initialised before we read its `refId`.
         */
        val all: List<LocusVariable<*>> by lazy {
            listOf(
                AssistMode,
                BicycleBattery,
                BicycleGear,
                Cadence,
                HeartRate,
                Humidity,
                Power,
                Range,
                Speed,
                Strides,
                Temperature,
            )
        }

        private val byId: Map<Int, LocusVariable<*>> by lazy { all.associateBy { it.refId } }

        /**
         * Look up by [refId]. Used by Locus when applying a received `SensorValueBatch`
         * — for each `(refId, stringValue)` pair, Locus resolves the typed variable
         * and uses its parser to recover the value.
         */
        fun findById(refId: Int): LocusVariable<*>? {
            return byId[refId]
        }

        /**
         * Look up by the symbolic [xmlName] used in adapter manifest XML
         * (`<refId variable="SENSOR_HEART_RATE" />`). Returns null for names not in
         * the curated catalogue.
         */
        fun findByName(name: String): LocusVariable<*>? {
            return all.firstOrNull { it.xmlName == name }
        }
    }
}
