/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Payload passed by Locus to the adapter's `init(...)` AIDL call. Carries the running
 * Locus's identification plus the set of [LocusVariable] refIds Locus understands —
 * the adapter uses the refId list to decide which of the refIds declared in its
 * manifest XML to actually emit at runtime.
 *
 * @property locusApiVersion the [AdapterApi.VERSION] Locus speaks. Compared by the
 *   adapter against [AdapterApi.VERSION] in its own locus-api dependency. A finer-
 *   grained mismatch the XML `apiVersion` filter let through surfaces as
 *   [AdapterApi.INIT_INCOMPATIBLE_API].
 * @property locusPackageName package name of the Locus app issuing the bind
 *   (e.g. `menion.android.locus.pro`). Useful for adapters that whitelist Locus flavors.
 * @property locusVersionName user-facing version of the Locus app (e.g. `4.34.1.1`).
 * @property supportedRefIds refIds Locus understands. Adapters should only emit values
 *   for refIds present here; values for unknown refIds are dropped on Locus's side.
 *
 * Payload-shape evolution: this class is frozen for a given [AdapterApi.VERSION].
 * Adding fields is a breaking AIDL change and requires a [AdapterApi.VERSION] bump
 * so that incompatible adapters are filtered out before they marshal a mismatched
 * payload.
 */
@Parcelize
data class LocusBindContext(
    val locusApiVersion: Int,
    val locusPackageName: String,
    val locusVersionName: String,
    val supportedRefIds: List<Int>,
) : Parcelable
