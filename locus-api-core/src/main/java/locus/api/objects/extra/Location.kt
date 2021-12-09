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

package locus.api.objects.extra

import locus.api.objects.Storable
import locus.api.utils.*
import java.io.IOException

/**
 * Container for location related information.
 *
 * Class is made open because of WhereYouGo request:
 * https://github.com/asamm/locus-api/issues/30
 */
open class Location() : Storable() {

    // CONTAINERS

    /**
     * Container for integer based extra data.
     */
    private var extraDataShort = SparseArrayCompat<Short>(0)

    /**
     * Container for integer based extra data.
     */
    private var extraDataInt = SparseArrayCompat<Int>(0)

    /**
     * Container for float based extra data.
     */
    private var extraDataFloat = SparseArrayCompat<Float>(0)

    /**
     * Container for double based extra data.
     */
    private var extraDataDouble = SparseArrayCompat<Double>(0)

    // VARIABLES

    /**
     * Location unique ID.
     */
    var id: Long = -1L

    /**
     * Provider for location source.
     */
    var provider: String = ""

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
                    Logger.logE(TAG, "setLatitude($value), invalid latitude")
                    -90.0
                }
                value > 90.0 -> {
                    Logger.logE(TAG, "setLatitude($value), invalid latitude")
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
    // BASIC GNSS VALUES
    //*************************************************

    // ALTITUDE

    /**
     * Altitude value of the location (in m). If 'hasData' is false, 0.0f is returned.
     */
    var altitude: Double
        get() {
            return extraDataDouble.get(EXTRA_KEY_ALTITUDE, 0.0)
        }
        set(value) {
            extraDataDouble.put(EXTRA_KEY_ALTITUDE, value)
        }

    val hasAltitude: Boolean
        get() = extraDataDouble.containsKey(EXTRA_KEY_ALTITUDE)

    fun removeAltitude() {
        extraDataDouble.remove(EXTRA_KEY_ALTITUDE)
    }

    // SPEED

    /**
     * Speed of the device in meters/second.
     */
    var speed: Float
        get() {
            return extraDataFloat.get(EXTRA_KEY_SPEED, 0.0f)
        }
        set(value) {
            extraDataFloat.put(EXTRA_KEY_SPEED, value)
        }

    val hasSpeed: Boolean
        get() = extraDataFloat.containsKey(EXTRA_KEY_SPEED)

    fun removeSpeed() {
        extraDataFloat.remove(EXTRA_KEY_SPEED)
    }

    // BEARING

    /**
     * Direction of travel in degrees East of true North. If 'hasData' is false,
     * 0.0 is returned (in degree).
     */
    var bearing: Float
        get() {
            return extraDataFloat.get(EXTRA_KEY_BEARING, 0.0f)
        }
        set(value) {
            var bearingNew = value
            while (bearingNew < 0.0f) {
                bearingNew += 360.0f
            }
            while (bearingNew >= 360.0f) {
                bearingNew -= 360.0f
            }
            extraDataFloat.put(EXTRA_KEY_BEARING, bearingNew)
        }

    val hasBearing: Boolean
        get() = extraDataFloat.containsKey(EXTRA_KEY_BEARING)

    fun removeBearing() {
        extraDataFloat.remove(EXTRA_KEY_BEARING)
    }

    // HORIZONTAL ACCURACY

    /**
     * Horizontal accuracy of the fix. If 'hasData' is false, 0.0 is returned (in m).
     */
    var accuracyHor: Float
        get() {
            return extraDataFloat.get(EXTRA_KEY_ACCURACY_HOR, 0.0f)
        }
        set(value) {
            extraDataFloat.put(EXTRA_KEY_ACCURACY_HOR, value)
        }

    val hasAccuracyHor: Boolean
        get() = extraDataFloat.containsKey(EXTRA_KEY_ACCURACY_HOR)

    fun removeAccuracyHor() {
        extraDataFloat.remove(EXTRA_KEY_ACCURACY_HOR)
    }

    // VERTICAL ACCURACY

    /**
     * Vertical accuracy of the fix. If 'hasData' is false, 0.0 is returned (in m).
     */
    var accuracyVer: Float
        get() {
            return extraDataFloat.get(EXTRA_KEY_ACCURACY_VER, 0.0f)
        }
        set(value) {
            extraDataFloat.put(EXTRA_KEY_ACCURACY_VER, value)
        }

    val hasAccuracyVer: Boolean
        get() = extraDataFloat.containsKey(EXTRA_KEY_ACCURACY_VER)

    fun removeAccuracyVer() {
        extraDataFloat.remove(EXTRA_KEY_ACCURACY_VER)
    }

    //*************************************************
    // SENSOR VALUES
    //*************************************************

    // CADENCE

    /**
     * Cadence value. If hasCadence() is false, 0 is returned.
     */
    var sensorCadence: Short
        get() {
            return extraDataShort.get(EXTRA_KEY_SENSOR_CADENCE, 0)
        }
        set(value) {
            extraDataShort.put(EXTRA_KEY_SENSOR_CADENCE, value)
        }

    val hasSensorCadence: Boolean
        get() = extraDataShort.containsKey(EXTRA_KEY_SENSOR_CADENCE)

    fun removeSensorCadence() {
        extraDataShort.remove(EXTRA_KEY_SENSOR_CADENCE)
    }

    // HEART RATE

    /**
     * Heart rate value in BMP. If hasSensorHeartRate() is false, 0 is returned.
     */
    var sensorHeartRate: Short
        get() {
            return extraDataShort.get(EXTRA_KEY_SENSOR_HEART_RATE, 0)
        }
        set(value) {
            extraDataShort.put(EXTRA_KEY_SENSOR_HEART_RATE, value)
        }

    val hasSensorHeartRate: Boolean
        get() = extraDataShort.containsKey(EXTRA_KEY_SENSOR_HEART_RATE)

    fun removeSensorHeartRate() {
        extraDataShort.remove(EXTRA_KEY_SENSOR_HEART_RATE)
    }

    // SPEED FROM SENSOR

    /**
     * Speed of the device over ground in meters/second. This speed is defined only when
     * 'speed sensor' is connected and supply valid values.
     *
     * If 'hasData' is 'false', 0.0f is returned (in m/s).
     */
    var sensorSpeed: Float
        get() {
            return extraDataFloat.get(EXTRA_KEY_SENSOR_SPEED, 0.0f)
        }
        set(value) {
            extraDataFloat.put(EXTRA_KEY_SENSOR_SPEED, value)
        }

    val hasSensorSpeed: Boolean
        get() = extraDataFloat.containsKey(EXTRA_KEY_SENSOR_SPEED)

    fun removeSensorSpeed() {
        extraDataFloat.remove(EXTRA_KEY_SENSOR_SPEED)
    }

    // POWER

    /**
     * Power value of the fix in W. If hasSensorPower() is false, 0.0 is returned.
     */
    var sensorPower: Float
        get() {
            return extraDataFloat.get(EXTRA_KEY_SENSOR_POWER, 0.0f)
        }
        set(value) {
            extraDataFloat.put(EXTRA_KEY_SENSOR_POWER, value)
        }

    val hasSensorPower: Boolean
        get() = extraDataFloat.containsKey(EXTRA_KEY_SENSOR_POWER)

    fun removeSensorPower() {
        extraDataFloat.remove(EXTRA_KEY_SENSOR_POWER)
    }

    // STRIDES

    /**
     * The num of strides. If hasSensorStrides() is false, 0 is returned.
     */
    var sensorStrides: Int
        get() {
            return extraDataInt.get(EXTRA_KEY_SENSOR_STRIDES, 0)
        }
        set(value) {
            extraDataInt.put(EXTRA_KEY_SENSOR_STRIDES, value)
        }

    val hasSensorStrides: Boolean
        get() = extraDataInt.containsKey(EXTRA_KEY_SENSOR_STRIDES)

    fun removeSensorStrides() {
        extraDataInt.remove(EXTRA_KEY_SENSOR_STRIDES)
    }

    // TEMPERATURE

    /**
     * Temperature value. If hasSensorTemperature() is false, 0.0f is returned.
     */
    var sensorTemperature: Float
        get() {
            return extraDataFloat.get(EXTRA_KEY_SENSOR_TEMPERATURE, 0.0f)
        }
        set(value) {
            extraDataFloat.put(EXTRA_KEY_SENSOR_TEMPERATURE, value)
        }

    val hasSensorTemperature: Boolean
        get() = extraDataFloat.containsKey(EXTRA_KEY_SENSOR_TEMPERATURE)

    fun removeSensorTemperature() {
        extraDataFloat.remove(EXTRA_KEY_SENSOR_TEMPERATURE)
    }

    //*************************************************
    // GNSS META-DATA
    //*************************************************

    // GNSS QUALITY

    var gnssQuality: Short
        get() {
            return extraDataShort.get(EXTRA_KEY_GNSS_QUALITY, 0)
        }
        set(value) {
            extraDataShort.put(EXTRA_KEY_GNSS_QUALITY, value)
        }

    val hasGnssQuality: Boolean
        get() = extraDataShort.containsKey(EXTRA_KEY_GNSS_QUALITY)

    fun removeGnssQuality() {
        extraDataShort.remove(EXTRA_KEY_GNSS_QUALITY)
    }

    // GNSS, HDOP

    /**
     * Horizontal dilution of precision for current location.
     *
     * More info: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation).
     */
    var gnssHdop: Float
        get() {
            return extraDataFloat.get(EXTRA_KEY_GNSS_HDOP, 0.0f)
        }
        set(value) {
            extraDataFloat.put(EXTRA_KEY_GNSS_HDOP, value)
        }

    val hasGnssHdop: Boolean
        get() = extraDataFloat.containsKey(EXTRA_KEY_GNSS_HDOP)

    fun removeGnssHdop() {
        extraDataFloat.remove(EXTRA_KEY_GNSS_HDOP)
    }

    // GNSS, VDOP

    /**
     * Vertical dilution of precision for current location.
     *
     * More info: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation).
     */
    var gnssVdop: Float
        get() {
            return extraDataFloat.get(EXTRA_KEY_GNSS_VDOP, 0.0f)
        }
        set(value) {
            extraDataFloat.put(EXTRA_KEY_GNSS_VDOP, value)
        }

    val hasGnssVdop: Boolean
        get() = extraDataFloat.containsKey(EXTRA_KEY_GNSS_VDOP)

    fun removeGnssVdop() {
        extraDataFloat.remove(EXTRA_KEY_GNSS_VDOP)
    }

    // GNSS, PDOP

    /**
     * Position (3D) dilution of precision for current location.
     *
     * More info: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation).
     */
    var gnssPdop: Float
        get() {
            return extraDataFloat.get(EXTRA_KEY_GNSS_PDOP, 0.0f)
        }
        set(value) {
            extraDataFloat.put(EXTRA_KEY_GNSS_PDOP, value)
        }

    val hasGnssPdop: Boolean
        get() = extraDataFloat.containsKey(EXTRA_KEY_GNSS_PDOP)

    fun removeGnssPdop() {
        extraDataFloat.remove(EXTRA_KEY_GNSS_PDOP)
    }

    // GNSS, NUMBER OF USED SATS

    /**
     * Number of used satellites used to obtain current location.
     */
    var gnssSatsUsed: Short
        get() {
            return extraDataShort.get(EXTRA_KEY_GNSS_SATS_USED, 0)
        }
        set(value) {
            extraDataShort.put(EXTRA_KEY_GNSS_SATS_USED, value)
        }

    val hasGnssSatsUsed: Boolean
        get() = extraDataShort.containsKey(EXTRA_KEY_GNSS_SATS_USED)

    fun removeGnssSatsUsed() {
        extraDataShort.remove(EXTRA_KEY_GNSS_SATS_USED)
    }

    // GNSS, NUMBER OF VISIBLE SATS

    /**
     * Number of used satellites used to obtain current location.
     */
    var gnssSatsVisible: Short
        get() {
            return extraDataShort.get(EXTRA_KEY_GNSS_SATS_VISIBLE, 0)
        }
        set(value) {
            extraDataShort.put(EXTRA_KEY_GNSS_SATS_VISIBLE, value)
        }

    val hasGnssSatsVisible: Boolean
        get() = extraDataShort.containsKey(EXTRA_KEY_GNSS_SATS_VISIBLE)

    fun removeGnssSatsVisible() {
        extraDataShort.remove(EXTRA_KEY_GNSS_SATS_VISIBLE)
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
        provider = loc.provider
        time = loc.time
        latitude = loc.latitude
        longitude = loc.longitude

        // set extra data
        extraDataShort.putAll(loc.extraDataShort)
        extraDataInt.putAll(loc.extraDataInt)
        extraDataFloat.putAll(loc.extraDataFloat)
        extraDataDouble.putAll(loc.extraDataDouble)
    }

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
    @Deprecated(message = "Work with speed value directly")
    val speedOptimal: Float
        get() = if (hasSensorSpeed) {
            sensorSpeed
        } else speed

    /**
     * Check if any speed (GPS or from sensors) is stored.
     */
    @Deprecated(message = "Work with speed value directly")
    fun hasSpeedOptimal(): Boolean {
        return hasSpeed || hasSensorSpeed
    }

    //*************************************************
    // CONTAINERS
    //*************************************************

    abstract class ValueContainer<T> constructor(
            private val dataContainer: SparseArrayCompat<T>,
            private val id: Byte) {

        val hasData: Boolean
            get() = dataContainer.containsKey(id.toInt())

        var value: T
            get() {
                return dataContainer.get(id.toInt(), getDefaultEmpty())
            }
            set(value) {
                val validatedValue = validateNewValue(value)
                dataContainer.put(id.toInt(), validatedValue)
            }

        /**
         * Get default empty value.
         */
        internal abstract fun getDefaultEmpty(): T

        /**
         * Validate received value.
         */
        internal open fun validateNewValue(value: T): T {
            return value
        }

        fun doIfValid(action: (T) -> Unit) {
            if (hasData) {
                action(value)
            }
        }

        fun remove() {
            dataContainer.remove(id.toInt())
        }
    }

    inner class ValueContainerShort(id: Byte)
        : ValueContainer<Short>(extraDataShort, id) {

        override fun getDefaultEmpty(): Short {
            return 0
        }
    }

    inner class ValueContainerInt(id: Byte)
        : ValueContainer<Int>(extraDataInt, id) {

        override fun getDefaultEmpty(): Int {
            return 0
        }
    }


    //*************************************************
    // TOOLS
    //*************************************************

    /**
     * Remove all attached sensors values.
     */
    fun removeSensorAll() {
        removeSensorCadence()
        removeSensorHeartRate()
        removeSensorPower()
        removeSensorSpeed()
        removeSensorStrides()
        removeSensorTemperature()
    }

    /**
     * Remove all values related to GNSS metadata.
     */
    fun removeGnssAll() {
        removeGnssQuality()
        removeGnssHdop()
        removeGnssVdop()
        removeGnssPdop()
        removeGnssSatsUsed()
        removeGnssSatsVisible()
    }

    override fun toString(): String {
        return "Location [" +
                "tag: $provider, " +
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
        return 3
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readLong()
        provider = dr.readString()
        time = dr.readLong()
        latitude = dr.readDouble()
        longitude = dr.readDouble()
        val hasAltitude = dr.readBoolean()
        val altitude = dr.readDouble()
        if (hasAltitude) {
            this.altitude = altitude
        }

        // red basic data
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
        if (version >= 3) {
            extraDataShort.clear()
            var size = dr.readByte()
            for (i in 0 until size) {
                extraDataShort.put(dr.readByte().toInt(), dr.readShort())
            }
            extraDataInt.clear()
            size = dr.readByte()
            for (i in 0 until size) {
                extraDataInt.put(dr.readByte().toInt(), dr.readInt())
            }
            extraDataFloat.clear()
            size = dr.readByte()
            for (i in 0 until size) {
                extraDataFloat.put(dr.readByte().toInt(), dr.readFloat())
            }
            extraDataDouble.clear()
            size = dr.readByte()
            for (i in 0 until size) {
                extraDataDouble.put(dr.readByte().toInt(), dr.readDouble())
            }
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeLong(id)
        dw.writeString(provider)
        dw.writeLong(time)
        dw.writeDouble(latitude)
        dw.writeDouble(longitude)
        dw.writeBoolean(hasAltitude)
        dw.writeDouble(altitude)

        // write (deprecated) basic data
        if (hasAccuracyHor || hasBearing || hasSpeed) {
            dw.writeBoolean(true)
            dw.writeBoolean(hasAccuracyHor)
            dw.writeFloat(accuracyHor)
            dw.writeBoolean(hasBearing)
            dw.writeFloat(bearing)
            dw.writeBoolean(hasSpeed)
            dw.writeFloat(speed)
        } else {
            dw.writeBoolean(false)
        }

        // write sensors data (version 1+)
        val extraSensor = ExtraSensor().apply {
            if (hasSensorCadence) {
                hasCadence = true
                cadence = sensorCadence.toInt()
            }
            if (hasSensorHeartRate) {
                hasHr = true
                hr = sensorHeartRate.toInt()
            }
            if (hasSensorPower) {
                hasPower = true
                power = sensorPower
            }
            if (hasSensorSpeed) {
                hasSpeed = true
                speed = sensorSpeed
            }
            if (hasSensorStrides) {
                hasStrides = true
                strides = sensorStrides
            }
            if (hasSensorTemperature) {
                hasTemperature = true
                temperature = sensorTemperature
            }
        }
        extraSensor
                .takeIf { it.hasData() }
                ?.let {
                    dw.writeBoolean(true)
                    it.write(dw)
                } ?: {
            dw.writeBoolean(false)
        }()

        // V3
        dw.writeByte(extraDataShort.size().toByte())
        for (i in 0 until extraDataShort.size()) {
            dw.writeByte(extraDataShort.keyAt(i).toByte())
            dw.writeShort(extraDataShort.valueAt(i).toInt())
        }
        dw.writeByte(extraDataInt.size().toByte())
        for (i in 0 until extraDataInt.size()) {
            dw.writeByte(extraDataInt.keyAt(i).toByte())
            dw.writeInt(extraDataInt.valueAt(i))
        }
        dw.writeByte(extraDataFloat.size().toByte())
        for (i in 0 until extraDataFloat.size()) {
            dw.writeByte(extraDataFloat.keyAt(i).toByte())
            dw.writeFloat(extraDataFloat.valueAt(i))
        }
        dw.writeByte(extraDataDouble.size().toByte())
        for (i in 0 until extraDataDouble.size()) {
            dw.writeByte(extraDataDouble.keyAt(i).toByte())
            dw.writeDouble(extraDataDouble.valueAt(i))
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

        private const val EXTRA_KEY_ALTITUDE = 10
        private const val EXTRA_KEY_SPEED = 11
        private const val EXTRA_KEY_BEARING = 12
        private const val EXTRA_KEY_ACCURACY_HOR = 13
        private const val EXTRA_KEY_ACCURACY_VER = 14

        private const val EXTRA_KEY_SENSOR_HEART_RATE = 20
        private const val EXTRA_KEY_SENSOR_CADENCE = 21
        private const val EXTRA_KEY_SENSOR_SPEED = 22
        private const val EXTRA_KEY_SENSOR_TEMPERATURE = 23
        private const val EXTRA_KEY_SENSOR_POWER = 24
        private const val EXTRA_KEY_SENSOR_STRIDES = 25

        private const val EXTRA_KEY_GNSS_QUALITY = 51
        private const val EXTRA_KEY_GNSS_HDOP = 52
        private const val EXTRA_KEY_GNSS_VDOP = 53
        private const val EXTRA_KEY_GNSS_PDOP = 54
        private const val EXTRA_KEY_GNSS_SATS_USED = 55
        private const val EXTRA_KEY_GNSS_SATS_VISIBLE = 56
    }
}
