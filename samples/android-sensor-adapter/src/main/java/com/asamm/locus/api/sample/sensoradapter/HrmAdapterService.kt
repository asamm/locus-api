/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package com.asamm.locus.api.sample.sensoradapter

import android.content.Intent
import locus.api.android.features.sensorAdapter.AdapterApi
import locus.api.android.features.sensorAdapter.LocusBindContext
import locus.api.android.features.sensorAdapter.LocusVariable
import locus.api.android.features.sensorAdapter.parser.LocusParserAdapterService
import locus.api.android.features.sensorAdapter.parser.SensorValueBatch
import locus.api.android.features.sensorAdapter.parser.SensorValueBatchBuilder

/**
 * Sample Locus parser-style adapter that exposes any standard BLE Heart Rate
 * Service strap (Polar, Wahoo, Garmin, Suunto, …) to Locus Map via the adapter
 * SDK. Demonstrates the minimal adapter shape: one device type, one
 * NOTIFY characteristic, one refId.
 *
 * Real adapters target sensors Locus doesn't already support natively — HRM is
 * here because every developer building against the SDK can verify it on real
 * hardware. For the equivalent native Locus implementation see
 * `libSensorsV2/src/main/java/com/asamm/android/sensors/bluetooth/bt4/devices/Bt4SensorHrm.kt`
 * in the locus-core repo.
 *
 * Adapter-author responsibilities:
 *
 * - Manifest XML in `res/xml/locus_adapter_hrm.xml` declares the adapter's
 *   `apiVersion` + device-type catalogue. Locus reads this without binding.
 * - `<service>` entry in `AndroidManifest.xml` declares the intent-filter
 *   action, custom permission, meta-data pointer to the manifest XML, and
 *   `android:icon` for the picker.
 * - Parser implementation (this class) decodes each GATT frame into a
 *   [SensorValueBatch] of typed (refId → value) pairs.
 */
class HrmAdapterService : LocusParserAdapterService() {

    override fun init(bindContext: LocusBindContext): Int {
        // No init-time work — this adapter is stateless across binds. Adapters that
        // need credentials / runtime permissions / in-app pairing return
        // AdapterApi.INIT_NEED_USER_ACTION and host the flow in getIntentForSettings().
        return AdapterApi.INIT_OK
    }

    override fun parseData(
        deviceId: String,
        deviceTypeId: String,
        source: String,
        bytes: ByteArray,
    ): SensorValueBatch? {
        // Locus only ever calls us with our own declared device type / characteristic, but the
        // defensive checks document the contract for adapter authors copying this sample. For BT4
        // `source` is the characteristic UUID.
        if (deviceTypeId != DEVICE_TYPE_HRM) {
            return null
        }
        if (!source.equals(CHAR_HRM_MEASUREMENT, ignoreCase = true)) {
            return null
        }
        val heartRate = decodeHeartRate(bytes) ?: return null
        return SensorValueBatchBuilder(System.currentTimeMillis())
            .put(LocusVariable.HeartRate, heartRate)
            .build()
    }

    /**
     * Optional. Locus launches this when the user taps "Settings" on the adapter's picker
     * row (and in the [AdapterApi.INIT_NEED_USER_ACTION] flow). This sample has no real
     * settings, so it just opens the info screen. Adapters with no settings UI can drop this
     * override entirely — the base class returns `null`.
     */
    override fun getIntentForSettings(): Intent {
        return Intent(this, InfoActivity::class.java)
    }

    /**
     * Decode a standard BLE Heart Rate Measurement frame.
     *
     * Wire layout (spec: Bluetooth SIG, Heart Rate Service 1.0 §3.1):
     * - byte 0: flags. Bit 0 = HR value format (0 = UINT8, 1 = UINT16).
     * - byte 1+: HR value, format per the flag. Optional energy-expended / RR
     *   intervals follow; not consumed here.
     *
     * Returns null for malformed frames (length too short for declared format,
     * or a 0 BPM reading which the spec treats as "no contact").
     */
    private fun decodeHeartRate(bytes: ByteArray): Int? {
        if (bytes.isEmpty()) {
            return null
        }
        val isUint16 = (bytes[0].toInt() and 0x01) != 0
        val hr = if (isUint16) {
            if (bytes.size < 3) {
                return null
            }
            (bytes[1].toInt() and 0xFF) or ((bytes[2].toInt() and 0xFF) shl 8)
        } else {
            if (bytes.size < 2) {
                return null
            }
            bytes[1].toInt() and 0xFF
        }
        return hr.takeIf { it > 0 }
    }

    companion object {

        private const val DEVICE_TYPE_HRM = "ble-hrm"
        private const val CHAR_HRM_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb"
    }
}
