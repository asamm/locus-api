/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Payload passed to the adapter's `init(...)`. Carries the running Locus's identification plus
 * the [LocusVariable] refIds it understands; the adapter emits only refIds in [supportedRefIds].
 * The device the bind is for arrives as the separate `deviceId` / `deviceTypeId` `init` arguments.
 *
 * @property locusApiVersion the [AdapterApi.VERSION] Locus speaks; a mismatch the XML filter let
 *   through is reported via [AdapterApi.INIT_INCOMPATIBLE_API]
 * @property locusPackageName package name of the Locus app issuing the bind
 * @property locusVersionName user-facing Locus version (e.g. `4.34.1.1`)
 * @property supportedRefIds refIds Locus understands; values for others are dropped
 */
@Parcelize
data class LocusBindContext(
    val locusApiVersion: Int,
    val locusPackageName: String,
    val locusVersionName: String,
    val supportedRefIds: List<Int>,
) : Parcelable
