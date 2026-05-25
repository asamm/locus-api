/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter.parser

import locus.api.android.features.sensorAdapter.AdapterApi
import locus.api.android.features.sensorAdapter.LocusVariable
import locus.api.android.features.sensorAdapter.LocusVariables

/**
 * Type-safe builder for [SensorValueBatch]. Adapter authors call [put] with a
 * [LocusVariable] reference and a value matching that Variable's `T`; the compiler
 * rejects mismatches (e.g. writing a `Float` to a `LocusVariable<Int>`).
 *
 * The Builder converts each value to its locale-independent string form internally,
 * so adapter authors don't deal with the wire format directly. Conversion uses
 * Kotlin's `Number.toString()` which is locale-stable (always `.` decimal
 * separator); locale-aware formatters never enter the pipeline.
 *
 * Typical usage:
 * ```
 * val batch = SensorValueBatchBuilder(System.currentTimeMillis())
 *     .put(LocusVariables.SENSOR_HEART_RATE, 120)
 *     .put(LocusVariables.SENSOR_HUMIDITY, 55.0f)
 *     .put(LocusVariables.SENSOR_ASSIST_MODE, "TRAIL")
 *     .build()
 * ```
 */
class SensorValueBatchBuilder(private val timestamp: Long) {

    private val values = mutableMapOf<Int, String>()
    private val writeBacks = mutableListOf<CharacteristicWrite>()

    /**
     * Set the value for [variable]. The `T` constraint on the receiver means the
     * compiler rejects type mismatches at the call site — e.g.
     * `put(LocusVariables.SENSOR_HEART_RATE, "abc")` fails because the Variable's
     * `T` is `Int`, not `String`.
     *
     * Re-calling [put] with the same Variable overrides the previous value (the
     * last write wins). Useful when an adapter accumulates partial frames over
     * multiple calls and rebuilds the final batch.
     */
    fun <T : Any> put(variable: LocusVariable<T>, value: T): SensorValueBatchBuilder = apply {
        values[variable.refId] = value.toString()
    }

    /**
     * Schedule a write-back to a characteristic that declares
     * [AdapterApi.CharacteristicMode.WRITE]. Locus dispatches the write after
     * applying this batch's values. Used by BLE protocols that require an ACK frame
     * after each NOTIFY.
     *
     * Copies [bytes] defensively so that mutating the caller's array after this call
     * does not affect the scheduled write — guards against the common pattern of
     * adapters that re-use a per-frame scratch buffer.
     */
    fun writeBack(uuid: String, bytes: ByteArray): SensorValueBatchBuilder = apply {
        writeBacks += CharacteristicWrite(uuid, bytes.copyOf())
    }

    /**
     * Finalise the batch. May be called multiple times; each call returns a snapshot
     * of the current Builder state, leaving the Builder reusable.
     */
    fun build(): SensorValueBatch {
        return SensorValueBatch(
            timestamp = timestamp,
            values = values.toMap(),
            writeBacks = writeBacks.toList(),
        )
    }
}
