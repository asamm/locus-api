/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter.parser

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import locus.api.android.features.sensorAdapter.AdapterApi

/**
 * One write-back returned in [SensorValueBatch.writeBacks], for protocols that need an ACK /
 * control write to the device. Locus performs it after applying the batch, but only when the
 * device type's transport is writable (BT4 / BT3 / USB — not the read-only NET transport); a
 * write for a non-writable transport is dropped.
 *
 * Not a `data class`: the explicit `equals`/`hashCode` use `ByteArray.contentEquals` so writes
 * compare by payload, not array identity.
 *
 * @property target where to write — the characteristic UUID for BT4 (must declare
 *   [AdapterApi.CharacteristicMode.WRITE]); empty string for stream transports (the single open
 *   stream)
 * @property bytes raw bytes to write — must be non-empty
 */
@Parcelize
class AdapterWrite(
    val target: String,
    val bytes: ByteArray,
) : Parcelable {

    init {
        require(bytes.isNotEmpty()) { "AdapterWrite.bytes must be non-empty" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is AdapterWrite) {
            return false
        }
        if (target != other.target) {
            return false
        }
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return 31 * target.hashCode() + bytes.contentHashCode()
    }

    override fun toString(): String {
        return "AdapterWrite(target=$target, bytes.size=${bytes.size})"
    }
}
