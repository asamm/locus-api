/*
 * Created by menion on 27.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package com.asamm.locus.api.sample.sensoradapter

import locus.api.android.features.sensorAdapter.AdapterApi
import locus.api.android.features.sensorAdapter.LocusBindContext
import locus.api.android.features.sensorAdapter.LocusVariable
import locus.api.android.features.sensorAdapter.parser.LocusParserAdapterService
import locus.api.android.features.sensorAdapter.parser.SensorValueBatch
import locus.api.android.features.sensorAdapter.parser.SensorValueBatchBuilder

/**
 * Sample Locus parser-style adapter for a Bluetooth Classic (SPP) NMEA GNSS receiver — the
 * stream-transport counterpart to [HrmAdapterService]'s BT4 example. Shows that a single app can
 * register more than one adapter service (Locus discovers each independently), and that a BT3
 * device is an undifferentiated byte stream: there are no characteristics, so `source` is empty
 * and Locus hands us one NMEA line per call.
 *
 * This one exposes speed-over-ground (`SENSOR_SPEED`), parsed from the `RMC` sentence — chosen
 * because a GNSS receiver is easy to verify on real hardware and `SENSOR_SPEED` is in the curated
 * catalogue (the catalogue has no position refIds).
 */
class NmeaSpeedAdapterService : LocusParserAdapterService() {

    override fun init(bindContext: LocusBindContext): Int {
        return AdapterApi.INIT_OK
    }

    override fun parseData(
        deviceId: String,
        deviceTypeId: String,
        source: String,
        bytes: ByteArray,
    ): SensorValueBatch? {
        // BT3 stream: `source` is empty; `bytes` is one NMEA line (Locus reads in line mode).
        if (deviceTypeId != DEVICE_TYPE_NMEA_SPEED) {
            return null
        }
        val speedMps = decodeNmeaSpeedMps(bytes) ?: return null
        return SensorValueBatchBuilder(System.currentTimeMillis())
            .put(LocusVariable.Speed, speedMps)
            .build()
    }

    /**
     * Speed-over-ground in m/s from an NMEA RMC sentence (talker-agnostic: GPRMC / GNRMC / …).
     * RMC field layout: `<talker>RMC,time,status,lat,N,lon,E,sog_knots,…`. Returns null for
     * non-RMC lines, a void fix (status != 'A'), or a missing speed field.
     */
    private fun decodeNmeaSpeedMps(bytes: ByteArray): Float? {
        val body = String(bytes).trim()
            .removePrefix("$")
            .substringBefore('*')
        val fields = body.split(",")
        if (fields.size < 8 || !fields[0].endsWith("RMC")) {
            return null
        }
        if (fields[2] != "A") {
            return null
        }
        val knots = fields[7].toFloatOrNull() ?: return null
        return knots * KNOTS_TO_MPS
    }

    companion object {

        private const val DEVICE_TYPE_NMEA_SPEED = "bt3-nmea-speed"
        private const val KNOTS_TO_MPS = 0.514444f
    }
}
