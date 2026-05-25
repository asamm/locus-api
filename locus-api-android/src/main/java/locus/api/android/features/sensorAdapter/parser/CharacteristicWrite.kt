/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter.parser

import locus.api.android.features.sensorAdapter.AdapterApi
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * One write-back instruction returned alongside parsed values in
 * [SensorValueBatch.writeBacks]. Used by BLE protocols that require an ACK /
 * control write after each NOTIFY (e.g. some heart-rate trainers, e-bike control
 * buses). Locus dispatches each write to the matching characteristic on the device
 * after the batch is applied; the target characteristic must declare
 * [AdapterApi.CharacteristicMode.WRITE].
 *
 * Not a `data class` — Kotlin's generated `equals` / `hashCode` would use reference
 * equality for the `bytes` array; the explicit overrides below use
 * `ByteArray.contentEquals` instead so two writes with the same UUID + payload
 * compare equal.
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
