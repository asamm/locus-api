/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter

/**
 * Typed reference to one Locus-side Variable that an adapter is allowed to write to.
 * The generic parameter [T] makes the (refId → value type) mapping a compile-time
 * fact: adapter authors can't write a `Float` to a `Variable<Int>` slot, because the
 * [SensorValueBatchBuilder.put] overload won't accept it.
 *
 * Instances are only created in [LocusVariables] — the `internal` constructor means
 * adapters can't invent new refIds in v1. Phase D (inline custom Variables) adds a
 * public factory if/when that's needed.
 *
 * @property refId stable integer id matching the Locus-core `Variables.X.id` value.
 * @property parser locale-independent String → T parser used by both sides — the
 *   Builder calls `value.toString()` to produce the wire form, Locus calls this
 *   parser to recover the typed value. Returns `null` on parse failure (malformed
 *   adapter output); Locus drops the value with a warning.
 */
class LocusVariable<T : Any> internal constructor(
    val refId: Int,
    internal val parser: (String) -> T?,
)
