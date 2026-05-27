/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter.parser

import locus.api.android.features.sensorAdapter.AdapterApi
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * One write-back returned in [SensorValueBatch.writeBacks], for BLE protocols that need an
 * ACK / control write after a NOTIFY. Locus writes it to the characteristic after applying the
 * batch; that characteristic must declare [AdapterApi.CharacteristicMode.WRITE].
 *
 * Not a `data class`: the explicit `equals`/`hashCode` use `ByteArray.contentEquals` so writes
 * compare by payload, not array identity.
 *
 * @property uuid characteristic UUID to write to
 * @property bytes raw bytes to write — must be non-empty
 */
@Parcelize
class CharacteristicWrite(
    val uuid: String,
    val bytes: ByteArray,
) : Parcelable {

    init {
        require(bytes.isNotEmpty()) { "CharacteristicWrite.bytes must be non-empty" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is CharacteristicWrite) {
            return false
        }
        if (uuid != other.uuid) {
            return false
        }
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return 31 * uuid.hashCode() + bytes.contentHashCode()
    }

    override fun toString(): String {
        return "CharacteristicWrite(uuid=$uuid, bytes.size=${bytes.size})"
    }
}
