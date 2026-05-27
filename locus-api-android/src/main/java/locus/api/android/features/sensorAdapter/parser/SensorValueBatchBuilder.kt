/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter.parser

import locus.api.android.features.sensorAdapter.AdapterApi
import locus.api.android.features.sensorAdapter.LocusVariable

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
 *     .put(LocusVariable.HeartRate, 120)
 *     .put(LocusVariable.Humidity, 55.0f)
 *     .put(LocusVariable.AssistMode, "TRAIL")
 *     .build()
 * ```
 */
class SensorValueBatchBuilder(private val timestamp: Long) {

    private val values = mutableMapOf<Int, String>()
    private val writeBacks = mutableListOf<AdapterWrite>()

    /**
     * Set the value for [variable]. The `T` constraint on the receiver means the
     * compiler rejects type mismatches at the call site — e.g.
     * `put(LocusVariable.HeartRate, "abc")` fails because the Variable's
     * `T` is `Int`, not `String`.
     *
     * Re-calling [put] with the same Variable overrides the previous value (the
     * last write wins). Useful when an adapter accumulates partial frames over
     * multiple calls and rebuilds the final batch.
     */
    fun <T : Any> put(variable: LocusVariable<T>, value: T): SensorValueBatchBuilder {
        values[variable.refId] = value.toString()
        return this
    }

    /**
     * Schedule a write-back to the device. Locus dispatches it after applying this batch's
     * values, on writable transports only (BT4 / BT3 / USB — not read-only NET). [target] is the
     * characteristic UUID for BT4 (must declare [AdapterApi.CharacteristicMode.WRITE]); pass an
     * empty string for stream transports (the single open stream).
     *
     * Copies [bytes] defensively so that mutating the caller's array after this call
     * does not affect the scheduled write — guards against the common pattern of
     * adapters that re-use a per-frame scratch buffer.
     */
    fun writeBack(target: String, bytes: ByteArray): SensorValueBatchBuilder {
        writeBacks += AdapterWrite(target, bytes.copyOf())
        return this
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
