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
 * Sample Locus parser-style adapter for an NMEA GNSS receiver over a serial byte stream — the
 * stream-transport counterpart to [HrmAdapterService]'s BT4 example. One service serves two
 * transports: a Bluetooth Classic (SPP) device type and a USB-serial one. The parser is identical
 * for both — only the manifest `<deviceType connectionType>` differs — which is the point: a stream
 * adapter is transport-independent. It also shows a single app registering more than one adapter
 * service (Locus discovers each independently).
 *
 * A serial stream is undifferentiated bytes — `source` is empty and one [parseData] call carries
 * whatever bytes have arrived since the last: a partial NMEA line, exactly one, or several. The
 * contract puts frame reassembly on the adapter, so this one buffers per device and parses only
 * complete `\n`-terminated lines, carrying any trailing partial into the next call. Don't assume a
 * call equals a line — that's the mistake this sample exists to not make.
 *
 * It exposes speed-over-ground (`SENSOR_SPEED`), parsed from the `RMC` sentence — a GNSS receiver
 * is easy to verify on real hardware and `SENSOR_SPEED` is in the curated catalogue (which has no
 * position refIds).
 */
class NmeaSpeedAdapterService : LocusParserAdapterService() {

    /**
     * Per-device line-reassembly buffers. [parseData] runs on a background thread and may be
     * called concurrently for different paired peers, so every access goes through the map's
     * monitor.
     */
    private val lineBuffers = HashMap<String, StringBuilder>()

    override fun init(deviceId: String, deviceTypeId: String, bindContext: LocusBindContext): Int {
        // both device types (BT3 + USB) use the same NMEA parsing; deviceTypeId is validated here
        // once, so parseData (which gets only deviceId) doesn't re-check it
        return if (deviceTypeId in DEVICE_TYPES_NMEA_SPEED) {
            AdapterApi.INIT_OK
        } else {
            AdapterApi.INIT_ERROR
        }
    }

    override fun parseData(
        deviceId: String,
        source: String,
        bytes: ByteArray,
    ): SensorValueBatch? {
        // Append this chunk to the device's buffer and pull out every complete line; the
        // unterminated remainder (if any) stays buffered for the next call.
        val lines = synchronized(lineBuffers) {
            val buffer = lineBuffers.getOrPut(deviceId) { StringBuilder() }
            buffer.append(String(bytes, Charsets.US_ASCII))
            takeCompleteLines(buffer)
        }
        // Decode outside the lock (pure). Apply the newest valid reading in this batch —
        // SensorValueBatchBuilder is last-write-wins on the refId.
        var speedMps: Float? = null
        for (line in lines) {
            decodeNmeaSpeedMps(line)?.let { speedMps = it }
        }
        val latest = speedMps ?: return null
        return SensorValueBatchBuilder(System.currentTimeMillis())
            .put(LocusVariable.Speed, latest)
            .build()
    }

    override fun shutdown(deviceId: String) {
        // drop only this device's reassembly buffer (the base class already released its write channel)
        synchronized(lineBuffers) {
            lineBuffers.remove(deviceId)
        }
    }

    /**
     * Remove and return every complete `\n`-terminated line from [buffer] (trailing `\r` stripped),
     * leaving any unterminated remainder in place for the next chunk. The caller holds the
     * [lineBuffers] monitor. A remainder that grows past [MAX_LINE_LENGTH] without a newline is
     * dropped, so a garbage or non-line-oriented stream can't grow the buffer without bound.
     */
    private fun takeCompleteLines(buffer: StringBuilder): List<String> {
        val lines = mutableListOf<String>()
        var newline = buffer.indexOf("\n")
        while (newline >= 0) {
            lines += buffer.substring(0, newline).removeSuffix("\r")
            buffer.delete(0, newline + 1)
            newline = buffer.indexOf("\n")
        }
        if (buffer.length > MAX_LINE_LENGTH) {
            buffer.setLength(0)
        }
        return lines
    }

    /**
     * Speed-over-ground in m/s from one NMEA RMC sentence (talker-agnostic: GPRMC / GNRMC / …).
     * RMC field layout: `<talker>RMC,time,status,lat,N,lon,E,sog_knots,…`. Returns null for a
     * non-RMC line, a void fix (status != 'A'), or a missing / unparseable speed field.
     */
    private fun decodeNmeaSpeedMps(line: String): Float? {
        val body = line.trim()
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

        // Same NMEA parser serves both the BT3 and the USB-serial device type; the ids must match
        // the `<deviceType id>` values in res/xml/locus_adapter_nmea.xml.
        private val DEVICE_TYPES_NMEA_SPEED = setOf("bt3-nmea-speed", "usb-nmea-speed")
        private const val KNOTS_TO_MPS = 0.514444f

        // A valid NMEA sentence is <= 82 chars; well past that with no newline means garbage.
        private const val MAX_LINE_LENGTH = 512
    }
}
