/*
 * Copyright 2012, Asamm Software, s. r. o.
 *
 * This file is part of LocusAPI.
 *
 * LocusAPI is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * LocusAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public
 * License along with LocusAPI. If not, see
 * <http://www.gnu.org/licenses/lgpl.html/>.
 */

!! INFO !!
This is version 5 of the 'Location' object. It brings flexibility and reduction of serialized byte array.
Unfortunately it also brings a data loss in cases of usage of serialized objects on older versions.
After team discussion, this version is postponed and will be (probably) used later.


package locus.api.objects.extra

import com.asamm.loggerV2.logE
import locus.api.objects.Storable
import locus.api.utils.ByteByteMap
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import locus.api.utils.LocationCompute
import java.io.IOException
import java.nio.ByteBuffer
import java.security.InvalidParameterException

enum class LocationProvider(val textId: String?) {

    UNDEFINED(null),
    GPS("gps"),
    NETWORK("network")
}

/**
 * Container for the single location related information (simply pack of values at defined time moment).
 */
@Suppress("unused")
class Location() : Storable() {

    //*************************************************
    // CONTAINERS
    //*************************************************

    // BYTES

    private var extraDataBytes: ByteByteMap? = null

    fun getDataBytes(key: Byte): ByteArray? {
        return extraDataBytes?.get(key)
    }

    fun setDataBytes(key: Byte, value: ByteArray?) {
        if (value == null) {
            extraDataBytes?.remove(key)
            if (extraDataBytes?.isEmpty == true) {
                extraDataBytes = null
            }
        } else {
            if (extraDataBytes == null) {
                extraDataBytes = ByteByteMap()
            }
            extraDataBytes?.put(key, value)
        }
    }

    // SHORT

    fun getDataShort(key: Byte): Short? {
        return getDataBytes(key)
            ?.takeIf { it.size == 2 }
            ?.let { ByteBuffer.wrap(it).short }
    }

    fun setDataShort(key: Byte, value: Short?) {
        setDataBytes(
            key = key,
            value = value?.let {
                ByteBuffer.allocate(2).putShort(it).array()
            }
        )
    }

    // INT

    fun getDataInt(key: Byte): Int? {
        return getDataBytes(key)
            ?.takeIf { it.size == 4 }
            ?.let { ByteBuffer.wrap(it).int }
    }

    fun setDataInt(key: Byte, value: Int?) {
        setDataBytes(
            key = key,
            value = value?.let {
                ByteBuffer.allocate(4).putInt(it).array()
            }
        )
    }

    // LONG

    fun getDataLong(key: Byte): Long? {
        return getDataBytes(key)
            ?.takeIf { it.size == 8 }
            ?.let { ByteBuffer.wrap(it).long }
    }

    fun setDataLong(key: Byte, value: Long?) {
        setDataBytes(
            key = key,
            value = value?.let {
                ByteBuffer.allocate(8).putLong(it).array()
            }
        )
    }

    // FLOAT

    fun getDataFloat(key: Byte): Float? {
        return getDataBytes(key)
            ?.takeIf { it.size == 4 }
            ?.let { ByteBuffer.wrap(it).float }
    }

    fun setDataFloat(key: Byte, value: Float?) {
        setDataBytes(
            key = key,
            value = value?.let {
                ByteBuffer.allocate(4).putFloat(it).array()
            }
        )
    }

    // DOUBLE

    fun getDataDouble(key: Byte): Double? {
        return getDataBytes(key)
            ?.takeIf { it.size == 8 }
            ?.let { ByteBuffer.wrap(it).double }
    }

    fun setDataDouble(key: Byte, value: Double?) {
        setDataBytes(
            key = key,
            value = value?.let {
                ByteBuffer.allocate(8).putDouble(it).array()
            }
        )
    }

    // STRING

    fun getDataString(key: Byte): String? {
        return getDataBytes(key)?.toString(Charsets.UTF_8)
    }

    fun setDataString(key: Byte, value: String?) {
        setDataBytes(key, value?.toByteArray(Charsets.UTF_8))
    }

    //*************************************************
    // CONSTRUCTION
    //*************************************************

    constructor(lat: Double, lon: Double) : this() {
        latitude = lat
        longitude = lon
    }

    constructor(loc: Location) : this() {
        set(loc)
    }

    /**
     * Sets the contents of the location to the values from the given location.
     */
    fun set(loc: Location) {
        id = loc.id
        time = loc.time
        latitude = loc.latitude
        longitude = loc.longitude

        extraDataBytes = null
        loc.extraDataBytes
            ?.takeIf { !it.isEmpty }
            ?.let {
                for (i in 0 until it.size) {
                    setDataBytes(it.keyAt(i), it.valueAt(i))
                }
            }
    }

    //*************************************************
    // VARIABLES
    //*************************************************

    /**
     * Location unique ID.
     */
    var id: Long = -1L

    /**
     * Provider for location source.
     */
    var provider: String?
        get() = getDataString(EXTRA_KEY_PROVIDER)
        set(value) = setDataString(EXTRA_KEY_PROVIDER, value)

    /**
     * UTC time of this location (in ms).
     */
    var time: Long = 0L

    // COORDINATES

    /**
     * Latitude of location in WGS coordinates.
     */
    var latitude: Double = 0.0
        set(value) {
            field = when {
                value < -90.0 -> {
                    logE(tag = TAG) { "setLatitude($value), invalid latitude" }
                    -90.0
                }

                value > 90.0 -> {
                    logE(tag = TAG) { "setLatitude($value), invalid latitude" }
                    90.0
                }

                else -> value
            }
        }

    /**
     * Longitude of location in WGS coordinates.
     */
    var longitude: Double = 0.0
        set(value) {
            // perform checks on range
            var newValue = value
            if (newValue < -180.0) {
                newValue += 360.0
            } else if (newValue > 180.0) {
                newValue -= 360.0
            }

            // set value
            field = newValue
        }

    //*************************************************
    // COMMON LOCATION VALUES
    //*************************************************

    // ALTITUDE

    /**
     * Altitude value of the location (in metres).
     */
    var altitude: Double? = null

    // SPEED

    /**
     * Speed of the device (in metres/second).
     */
    var speed: Float?
        get() = getDataFloat(EXTRA_KEY_SPEED)
        set(value) = setDataFloat(EXTRA_KEY_SPEED, value)

    // BEARING

    /**
     * Direction of travel in degrees East of true North (in degree).
     */
    var bearing: Float?
        get() = getDataFloat(EXTRA_KEY_BEARING)
        set(value) {
            var bearingNew = value
            if (bearingNew != null) {
                while (bearingNew < 0.0f) {
                    bearingNew += 360.0f
                }
                while (bearingNew >= 360.0f) {
                    bearingNew -= 360.0f
                }
            }
            setDataFloat(EXTRA_KEY_BEARING, bearingNew)
        }

    // HORIZONTAL ACCURACY

    /**
     * Horizontal accuracy of the fix (in metres).
     */
    var accuracyHor: Float?
        get() = getDataFloat(EXTRA_KEY_ACCURACY_HOR)
        set(value) = setDataFloat(EXTRA_KEY_ACCURACY_HOR, value)

    // VERTICAL ACCURACY

    /**
     * Vertical accuracy of the fix (in metres).
     */
    var accuracyVer: Float?
        get() = getDataFloat(EXTRA_KEY_ACCURACY_VER)
        set(value) = setDataFloat(EXTRA_KEY_ACCURACY_VER, value)

    // ORIGINAL LATITUDE

    /**
     * Original, unmodified, longitude value.
     */
    var latitudeOriginal: Double?
        get() = getDataDouble(EXTRA_KEY_ORIG_LATITUDE)
        set(value) = setDataDouble(EXTRA_KEY_ORIG_LATITUDE, value)

    // ORIGINAL LONGITUDE

    /**
     * Original, unmodified, longitude value.
     */
    var longitudeOriginal: Double?
        get() = getDataDouble(EXTRA_KEY_ORIG_LONGITUDE)
        set(value) = setDataDouble(EXTRA_KEY_ORIG_LONGITUDE, value)

    // ORIGINAL ALTITUDE

    /**
     * Original, unmodified, altitude value.
     */
    var altitudeOriginal: Double?
        get() = getDataDouble(EXTRA_KEY_ORIG_ALTITUDE)
        set(value) = setDataDouble(EXTRA_KEY_ORIG_ALTITUDE, value)

    //*************************************************
    // SENSOR VALUES
    //*************************************************

    // CADENCE

    /**
     * Cadence value.
     */
    var sensorCadence: Short?
        get() = getDataShort(EXTRA_KEY_SENSOR_CADENCE)
        set(value) = setDataShort(EXTRA_KEY_SENSOR_CADENCE, value)

    // HEART RATE

    /**
     * Heart rate value (in BMP).
     */
    var sensorHeartRate: Short?
        get() = getDataShort(EXTRA_KEY_SENSOR_HEART_RATE)
        set(value) = setDataShort(EXTRA_KEY_SENSOR_HEART_RATE, value)

    // SPEED FROM SENSOR

    /**
     * Speed of the device over ground (in meters/second). This speed is defined only when
     * 'speed sensor' is connected and supply valid values.
     */
    var sensorSpeed: Float?
        get() = getDataFloat(EXTRA_KEY_SENSOR_SPEED)
        set(value) = setDataFloat(EXTRA_KEY_SENSOR_SPEED, value)

    // POWER

    /**
     * Power value of the fix (in W).
     */
    var sensorPower: Float?
        get() = getDataFloat(EXTRA_KEY_SENSOR_POWER)
        set(value) = setDataFloat(EXTRA_KEY_SENSOR_POWER, value)

    // STRIDES

    /**
     * The num of strides.
     */
    var sensorStrides: Int?
        get() = getDataInt(EXTRA_KEY_SENSOR_STRIDES)
        set(value) = setDataInt(EXTRA_KEY_SENSOR_STRIDES, value)

    // TEMPERATURE

    /**
     * Temperature value (in degrees).
     */
    var sensorTemperature: Float?
        get() = getDataFloat(EXTRA_KEY_SENSOR_TEMPERATURE)
        set(value) = setDataFloat(EXTRA_KEY_SENSOR_TEMPERATURE, value)

    //*************************************************
    // GNSS META-DATA
    //*************************************************

    // GNSS based parameters are usually defined only for external GNSS devices and are mainly
    // useful for GIS based applications where are heavily used.

    // GNSS QUALITY

    /**
     * Quality of received GNSS location.
     *
     * Value is defined based on the GGA message value
     * https://gpsd.gitlab.io/gpsd/NMEA.html#_gga_global_positioning_system_fix_data
     */
    var gnssQuality: Short?
        get() = getDataShort(EXTRA_KEY_GNSS_QUALITY)
        set(value) = setDataShort(EXTRA_KEY_GNSS_QUALITY, value)

    // GNSS, HDOP

    /**
     * Horizontal dilution of precision for current location.
     *
     * More info: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation).
     */
    var gnssHdop: Float?
        get() = getDataFloat(EXTRA_KEY_GNSS_HDOP)
        set(value) = setDataFloat(EXTRA_KEY_GNSS_HDOP, value)

    // GNSS, VDOP

    /**
     * Vertical dilution of precision for current location.
     *
     * More info: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation).
     */
    var gnssVdop: Float?
        get() = getDataFloat(EXTRA_KEY_GNSS_VDOP)
        set(value) = setDataFloat(EXTRA_KEY_GNSS_VDOP, value)

    // GNSS, PDOP

    /**
     * Position (3D) dilution of precision for current location.
     *
     * More info: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation).
     */
    var gnssPdop: Float?
        get() = getDataFloat(EXTRA_KEY_GNSS_PDOP)
        set(value) = setDataFloat(EXTRA_KEY_GNSS_PDOP, value)

    // GNSS, NUMBER OF USED SATS

    /**
     * Number of used satellites used to obtain current location.
     */
    var gnssSatsUsed: Short?
        get() = getDataShort(EXTRA_KEY_GNSS_SATS_USED)
        set(value) = setDataShort(EXTRA_KEY_GNSS_SATS_USED, value)

    // GNSS, NUMBER OF VISIBLE SATS

    /**
     * Number of visible satellites at the moment of observation.
     */
    var gnssSatsVisible: Short?
        get() = getDataShort(EXTRA_KEY_GNSS_SATS_VISIBLE)
        set(value) = setDataShort(EXTRA_KEY_GNSS_SATS_VISIBLE, value)

    /**
     * NTRIP mount point identificator.
     */
    var gnssNtripMountPoint: String?
        get() = getDataString(EXTRA_KEY_GNSS_NTRIP_MOUNTPOINT)
        set(value) = setDataString(EXTRA_KEY_GNSS_NTRIP_MOUNTPOINT, value)

    /**
     * UTC time of observation start that created current location object (in ms).
     */
    var gnssObservationTimeStart: Long?
        get() = getDataLong(EXTRA_KEY_GNSS_OBSERVATION_TIME_START)
        set(value) = setDataLong(EXTRA_KEY_GNSS_OBSERVATION_TIME_START, value)

    /**
     * UTC time of observation end that created current location object (in ms).
     */
    var gnssObservationTimeEnd: Long?
        get() = getDataLong(EXTRA_KEY_GNSS_OBSERVATION_TIME_END)
        set(value) = setDataLong(EXTRA_KEY_GNSS_OBSERVATION_TIME_END, value)

    /**
     * Age of the RTK messages that created this location object (in ms).
     */
    var gnssDiffMessageAge: Long?
        get() = getDataLong(EXTRA_KEY_GNSS_DIFF_MESSAGE_AGE)
        set(value) = setDataLong(EXTRA_KEY_GNSS_DIFF_MESSAGE_AGE, value)

    /**
     * Custom metadata about GNSS device that created this location object and also
     * extra observation metadata.
     *
     * Used primarily for Locus GIS & it's flavors.
     */
    var gisMetadata: ByteArray?
        get() = getDataBytes(EXTRA_KEY_GIS_METADATA)
        set(value) = setDataBytes(EXTRA_KEY_GIS_METADATA, value)

    //*************************************************
    // EXTRA SPECIAL VARIABLES
    //*************************************************

    /**
     * Number of measurements (observations) that created this location.
     */
    var extraNumOfObservations: Short?
        get() = getDataShort(EXTRA_KEY_NUM_OF_OBSERVATIONS)
        set(value) = setDataShort(EXTRA_KEY_NUM_OF_OBSERVATIONS, value)

    /**
     * Offset of the hardware antenna phase center (in m).
     * Variable [altitude] should already contain correct reduced value.
     * Variable [altitudeOriginal] should contain original measured value.
     */
    var extraAntennaPhaseCenterOffset: Float?
        get() = getDataFloat(EXTRA_KEY_ANTENNA_PHASE_CENTER_OFFSET)
        set(value) = setDataFloat(EXTRA_KEY_ANTENNA_PHASE_CENTER_OFFSET, value)

    /**
     * Height of the pole during measurement (in m).
     * Variable [altitude] should already contain correct reduced value.
     * Variable [altitudeOriginal] should contain original measured value.
     */
    var extraPoleHeight: Float?
        get() = getDataFloat(EXTRA_KEY_POLE_HEIGHT)
        set(value) = setDataFloat(EXTRA_KEY_POLE_HEIGHT, value)

    /**
     * GSM signal strength at certain moment (in %).
     */
    var extraGsmSignalStrength: Int?
        get() = getDataInt(EXTRA_KEY_GSM_SIGNAL_STRENGTH)
        set(value) = setDataInt(EXTRA_KEY_GSM_SIGNAL_STRENGTH, value)

    //*************************************************
    // BASIC EXTRA DATA
    //*************************************************

    // SPEED, OPTIMAL

    /**
     * Get stored speed useful for display to users or for compute with some operations, based on best
     * available speed. If speed from sensors is stored (more precise) it is returned. Otherwise
     * basic GPS speed is returned.
     *
     * @return speed for display purpose
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated(message = "Work with speed value directly")
    val speedOptimal: Float?
        get() = sensorSpeed ?: speed

    //*************************************************
    // TOOLS
    //*************************************************

    /**
     * Remove all attached sensors values.
     */
    fun removeSensorAll() {
        sensorCadence = null
        sensorHeartRate = null
        sensorPower = null
        sensorSpeed = null
        sensorStrides = null
        sensorTemperature = null
    }

    /**
     * Save current latitude/longitude/altitude values to the 'original' fields.
     */
    fun saveCurrentToOriginal() {
        latitudeOriginal = latitude
        longitudeOriginal = longitude
        altitudeOriginal = altitude
    }

    override fun toString(): String {
        return "Location [" +
                "time: $time, " +
                "lon: $longitude, " +
                "lat: $latitude, " +
                "alt: $altitude]"
    }

    // COMPUTATIONS

    /**
     * Returns the approximate distance in meters between this
     * location and the given location.  Distance is defined using
     * the WGS84 ellipsoid.
     *
     * @param dest the destination location
     * @return the approximate distance in meters
     */
    fun distanceTo(dest: Location): Float {
        val com = LocationCompute(this)
        return com.distanceTo(dest)
    }

    /**
     * Returns the approximate initial bearing in degrees East of true
     * North when traveling along the shortest path between this
     * location and the given location.  The shortest path is defined
     * using the WGS84 ellipsoid.  Locations that are (nearly)
     * antipodal may produce meaningless results.
     *
     * @param dest the destination location
     * @return the initial bearing in degrees
     */
    fun bearingTo(dest: Location): Float {
        val com = LocationCompute(this)
        return com.bearingTo(dest)
    }

    /**
     * Compute bearing and distance values at once
     *
     * @param dest the destination location
     * @return array with float[0] - distance (in metres),
     * float[1] - bearing (in degree)
     */
    fun distanceAndBearingTo(dest: Location): FloatArray {
        val com = LocationCompute(this)
        return floatArrayOf(com.distanceTo(dest), com.bearingTo(dest))
    }

    //*************************************************
    // STORABLE
    //*************************************************

    override fun getVersion(): Int {
        return 5
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readLong()
        provider = dr.readString().takeIf { it.isNotBlank() }
        time = dr.readLong()
        latitude = dr.readDouble()
        longitude = dr.readDouble()
        val hasAltitude = dr.readBoolean()
        val altitude = dr.readDouble()
        if (hasAltitude) {
            this.altitude = altitude
        }

        // read basic data
        if (dr.readBoolean()) {
            val hasAccuracy = dr.readBoolean()
            val accuracy = dr.readFloat()
            if (hasAccuracy) {
                this.accuracyHor = accuracy
            }
            val hasBearing = dr.readBoolean()
            val bearing = dr.readFloat()
            if (hasBearing) {
                this.bearing = bearing
            }
            val hasSpeed = dr.readBoolean()
            val speed = dr.readFloat()
            if (hasSpeed) {
                this.speed = speed
            }
        }

        // V1
        if (version >= 1) {
            // read sensor data
            if (dr.readBoolean()) {
                // read data from storage
                val extraSensor = if (version == 1) {
                    ExtraSensor().apply {
                        hasHr = dr.readBoolean()
                        hr = dr.readInt()
                        hasCadence = dr.readBoolean()
                        cadence = dr.readInt()
                        hasSpeed = dr.readBoolean()
                        speed = dr.readFloat()
                        hasPower = dr.readBoolean()
                        power = dr.readFloat()
                    }
                } else {
                    ExtraSensor().apply { read(dr) }
                }

                // map values to new system
                if (extraSensor.hasCadence) {
                    sensorCadence = extraSensor.cadence.toShort()
                }
                if (extraSensor.hasHr) {
                    sensorHeartRate = extraSensor.hr.toShort()
                }
                if (extraSensor.hasPower) {
                    sensorPower = extraSensor.power
                }
                if (extraSensor.hasSpeed) {
                    sensorSpeed = extraSensor.speed
                }
                if (extraSensor.hasStrides) {
                    sensorStrides = extraSensor.strides
                }
                if (extraSensor.hasTemperature) {
                    sensorTemperature = extraSensor.temperature
                }
            }
        }

        // V3
        var size: Byte
        if (version >= 3) {
            extraDataBytes = null
            size = dr.readByte()
            for (i in 0 until size) {
                setDataShort(dr.readByte(), dr.readShort())
            }
            size = dr.readByte()
            for (i in 0 until size) {
                setDataInt(dr.readByte(), dr.readInt())
            }
            size = dr.readByte()
            for (i in 0 until size) {
                setDataFloat(dr.readByte(), dr.readFloat())
            }
            size = dr.readByte()
            for (i in 0 until size) {
                setDataDouble(dr.readByte(), dr.readDouble())
            }
        }

        // V4
        if (version >= 4) {
            size = dr.readByte()
            for (i in 0 until size) {
                setDataLong(dr.readByte(), dr.readLong())
            }
            size = dr.readByte()
            for (i in 0 until size) {
                setDataString(dr.readByte(), dr.readString())
            }
        }

        // V5
        if (version >= 5) {
            val dataBytesCount = dr.readByte()
            if (dataBytesCount > 0) {
                val typeId = dr.readByte()
                val type = DataType.entries.find { it.id == typeId }
                    ?: throw InvalidParameterException("Unknown type id: $typeId")

                // read all data
                for (i in 0 until dataBytesCount) {
                    val key = dr.readByte()
                    val valueSize = when (type) {
                        DataType.BYTE -> dr.readByte().toUByte().toInt()
                        DataType.SHORT -> dr.readShort().toUShort().toInt()
                        DataType.INT -> dr.readInt()
                    }

                    // read and set data
                    val value = dr.readBytes(valueSize)
                    setDataBytes(key, value)
                }
            }
        }

        // remove un-used keys
        setDataBytes(EXTRA_KEY_ALTITUDE, null)
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeLong(id)
        dw.writeString(provider)
        dw.writeLong(time)
        dw.writeDouble(latitude)
        dw.writeDouble(longitude)
        dw.writeBoolean(altitude != null)
        dw.writeDouble(altitude ?: 0.0)

        // 'accuracyHor', 'bearing' and 'speed', removed
        dw.writeBoolean(false)

        // V1
        // sensors data, removed
        dw.writeBoolean(false)

        // V3
        // extraDataShort, removed
        dw.writeByte(0.toByte())
        // extraDataInt, removed
        dw.writeByte(0.toByte())
        // extraDataFloat, removed
        dw.writeByte(0.toByte())
        // extraDataDouble, removed
        dw.writeByte(0.toByte())

        // V4
        // extraDataLong, removed
        dw.writeByte(0.toByte())
        // extraDataString, removed
        dw.writeByte(0.toByte())

        // V5
        extraDataBytes
            ?.takeIf { !it.isEmpty }
            ?.let { data ->
                // detect biggest size of the container
                val maxSize = data.map { key, value -> value }.maxOf { it.size }
                val requiredType = when {
                    maxSize > Short.MAX_VALUE -> DataType.INT
                    maxSize > Byte.MAX_VALUE -> DataType.SHORT
                    else -> DataType.BYTE
                }

                // write number of items & format
                dw.writeByte(data.size)
                dw.writeByte(requiredType.id)

                // write all data
                for (i in 0 until data.size) {
                    dw.writeByte(data.keyAt(i))
                    val value = data.valueAt(i)
                    when (requiredType) {
                        DataType.BYTE -> dw.writeByte(value.size.toByte())
                        DataType.SHORT -> dw.writeShort(value.size)
                        DataType.INT -> dw.writeInt(value.size)
                    }
                    dw.write(value)
                }
            }
            ?: run {
                dw.writeByte(0.toByte())
            }
    }

    /**
     * Deprecated container for sensors data used only to keep compatibility with older
     * location object versions.
     */
    private class ExtraSensor : Storable() {

        var hasHr: Boolean = false
        var hr: Int = 0

        var hasCadence: Boolean = false
        var cadence: Int = 0

        var hasSpeed: Boolean = false
        var speed: Float = 0.0f

        var hasPower: Boolean = false
        var power: Float = 0.0f

        var hasStrides: Boolean = false
        var strides: Int = 0

        var hasTemperature: Boolean = false
        var temperature: Float = 0.0f

        fun hasData(): Boolean {
            return hasHr || hasCadence || hasSpeed
                    || hasPower || hasStrides || hasTemperature
        }

        override fun getVersion(): Int {
            return 1
        }

        @Throws(IOException::class)
        override fun readObject(version: Int, dr: DataReaderBigEndian) {
            hasHr = dr.readBoolean()
            hr = dr.readInt()
            hasCadence = dr.readBoolean()
            cadence = dr.readInt()
            hasSpeed = dr.readBoolean()
            speed = dr.readFloat()
            hasPower = dr.readBoolean()
            power = dr.readFloat()
            hasStrides = dr.readBoolean()
            strides = dr.readInt()
            dr.readBoolean() // hasBattery
            dr.readInt() // battery
            if (version >= 1) {
                hasTemperature = dr.readBoolean()
                temperature = dr.readFloat()
            }
        }

        @Throws(IOException::class)
        override fun writeObject(dw: DataWriterBigEndian) {
            dw.writeBoolean(hasHr)
            dw.writeInt(hr)
            dw.writeBoolean(hasCadence)
            dw.writeInt(cadence)
            dw.writeBoolean(hasSpeed)
            dw.writeFloat(speed)
            dw.writeBoolean(hasPower)
            dw.writeFloat(power)
            dw.writeBoolean(hasStrides)
            dw.writeInt(strides)
            dw.writeBoolean(false) // hasBattery
            dw.writeInt(0) // battery
            dw.writeBoolean(hasTemperature)
            dw.writeFloat(temperature)
        }
    }

    companion object {

        // tag for logger
        private const val TAG = "Location"

        private const val EXTRA_KEY_PROVIDER: Byte = 9
        // not used anymore, altitude is handled directly
        private const val EXTRA_KEY_ALTITUDE: Byte = 10
        private const val EXTRA_KEY_SPEED: Byte = 11
        private const val EXTRA_KEY_BEARING: Byte = 12
        private const val EXTRA_KEY_ACCURACY_HOR: Byte = 13
        private const val EXTRA_KEY_ACCURACY_VER: Byte = 14

        private const val EXTRA_KEY_ORIG_LATITUDE: Byte = 15
        private const val EXTRA_KEY_ORIG_LONGITUDE: Byte = 16
        private const val EXTRA_KEY_ORIG_ALTITUDE: Byte = 17

        private const val EXTRA_KEY_SENSOR_HEART_RATE: Byte = 20
        private const val EXTRA_KEY_SENSOR_CADENCE: Byte = 21
        private const val EXTRA_KEY_SENSOR_SPEED: Byte = 22
        private const val EXTRA_KEY_SENSOR_TEMPERATURE: Byte = 23
        private const val EXTRA_KEY_SENSOR_POWER: Byte = 24
        private const val EXTRA_KEY_SENSOR_STRIDES: Byte = 25

        private const val EXTRA_KEY_GNSS_QUALITY: Byte = 51
        private const val EXTRA_KEY_GNSS_HDOP: Byte = 52
        private const val EXTRA_KEY_GNSS_VDOP: Byte = 53
        private const val EXTRA_KEY_GNSS_PDOP: Byte = 54
        private const val EXTRA_KEY_GNSS_SATS_USED: Byte = 55
        private const val EXTRA_KEY_GNSS_SATS_VISIBLE: Byte = 56
        private const val EXTRA_KEY_GNSS_NTRIP_MOUNTPOINT: Byte = 57
        private const val EXTRA_KEY_GNSS_OBSERVATION_TIME_START: Byte = 58
        private const val EXTRA_KEY_GNSS_OBSERVATION_TIME_END: Byte = 59
        private const val EXTRA_KEY_GNSS_DIFF_MESSAGE_AGE: Byte = 60
        private const val EXTRA_KEY_GIS_METADATA: Byte = 61

        private const val EXTRA_KEY_NUM_OF_OBSERVATIONS: Byte = 69
        private const val EXTRA_KEY_ANTENNA_PHASE_CENTER_OFFSET: Byte = 70
        private const val EXTRA_KEY_POLE_HEIGHT: Byte = 71
        private const val EXTRA_KEY_GSM_SIGNAL_STRENGTH: Byte = 72

        // internal method to visually verify IDs
        private fun validateIds(id: Byte) {
            when (id) {
                EXTRA_KEY_PROVIDER,
                EXTRA_KEY_ALTITUDE,
                EXTRA_KEY_SPEED,
                EXTRA_KEY_BEARING,
                EXTRA_KEY_ACCURACY_HOR,
                EXTRA_KEY_ACCURACY_VER,
                EXTRA_KEY_ORIG_LATITUDE,
                EXTRA_KEY_ORIG_LONGITUDE,
                EXTRA_KEY_ORIG_ALTITUDE,
                EXTRA_KEY_SENSOR_HEART_RATE,
                EXTRA_KEY_SENSOR_CADENCE,
                EXTRA_KEY_SENSOR_SPEED,
                EXTRA_KEY_SENSOR_TEMPERATURE,
                EXTRA_KEY_SENSOR_POWER,
                EXTRA_KEY_SENSOR_STRIDES,
                EXTRA_KEY_GNSS_QUALITY,
                EXTRA_KEY_GNSS_HDOP,
                EXTRA_KEY_GNSS_VDOP,
                EXTRA_KEY_GNSS_PDOP,
                EXTRA_KEY_GNSS_SATS_USED,
                EXTRA_KEY_GNSS_SATS_VISIBLE,
                EXTRA_KEY_GNSS_NTRIP_MOUNTPOINT,
                EXTRA_KEY_GNSS_OBSERVATION_TIME_START,
                EXTRA_KEY_GNSS_OBSERVATION_TIME_END,
                EXTRA_KEY_GNSS_DIFF_MESSAGE_AGE,
                EXTRA_KEY_GIS_METADATA,
                EXTRA_KEY_NUM_OF_OBSERVATIONS,
                EXTRA_KEY_ANTENNA_PHASE_CENTER_OFFSET,
                EXTRA_KEY_POLE_HEIGHT,
                EXTRA_KEY_GSM_SIGNAL_STRENGTH -> {
                }
            }
        }
    }
}

enum class DataType(val id: Byte) {

    BYTE(1.toByte()),
    SHORT(2.toByte()),
    INT(3.toByte()),
}
