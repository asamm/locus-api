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
 * Output of [ILocusSensorAdapterParser.parseCharacteristic]: a timestamped bundle
 * of (refId → value) pairs plus optional write-backs. One batch per parsed frame;
 * adapters return `null` (not an empty batch) when a frame was consumed without
 * producing values (e.g. partial frame buffered for reassembly).
 *
 * Values ride the wire as locale-independent strings. Adapter authors do not call
 * `.toString()` directly — they use [SensorValueBatchBuilder.put] with a typed
 * [LocusVariable] reference, and the Builder converts under controlled code. Locus
 * recovers the typed value on receive via the matching [LocusVariable.parser]. The
 * round-trip preserves full precision for `Int`/`Long`/`String` Variables and at
 * least 6 significant digits for `Float`/`Double` Variables — enough for any
 * physical sensor reading; do not use this surface to ferry serialized scientific
 * data.
 *
 * Direct construction of this data class is supported (Locus's own dispatch code
 * uses it) but adapter authors should build via [SensorValueBatchBuilder] for the
 * compile-time refId-to-`T` checks.
 *
 * Payload-shape evolution: this class is frozen for a given [AdapterApi.VERSION].
 * Adding fields is a breaking AIDL change and requires a [AdapterApi.VERSION] bump.
 *
 * @property timestamp when the underlying frame was received, in ms since epoch.
 *   Covers every value in this batch — sensors typically push frames at ~1 Hz so
 *   per-value timestamps would be wire overhead with no precision win.
 * @property values refId → stringified value. See class KDoc for round-trip
 *   guarantees.
 * @property writeBacks optional write-backs Locus should perform on the device after
 *   applying this batch (e.g. an ACK frame for protocols requiring one).
 */
@Parcelize
data class SensorValueBatch(
    val timestamp: Long,
    val values: Map<Int, String> = emptyMap(),
    val writeBacks: List<CharacteristicWrite> = emptyList(),
) : Parcelable
