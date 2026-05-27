/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter.parser

import locus.api.android.features.sensorAdapter.AdapterApi
import locus.api.android.features.sensorAdapter.LocusVariable

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Output of [ILocusSensorAdapterParser.parseCharacteristic]: a timestamped bundle of
 * (refId → value) pairs plus optional write-backs. Return `null`, not an empty batch, when a
 * frame is consumed without producing values (e.g. a partial frame buffered for reassembly).
 *
 * Build via [SensorValueBatchBuilder] for the compile-time refId-to-`T` checks; direct
 * construction works but skips them. Values ride the wire as locale-independent strings (full
 * precision for `Int`/`Long`/`String`, shortest-decimal round-trip for `Float`).
 *
 * @property timestamp when the frame was received, ms since epoch — covers every value in the batch
 * @property values refId → stringified value
 * @property writeBacks write-backs Locus performs on the device after applying this batch
 */
@Parcelize
data class SensorValueBatch(
    val timestamp: Long,
    val values: Map<Int, String> = emptyMap(),
    val writeBacks: List<CharacteristicWrite> = emptyList(),
) : Parcelable
