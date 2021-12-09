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

    // BASIC GNSS VALUES

    /**
     * Altitude value of the location (in m). If 'hasData' is false, 0.0f is returned.
     */
    val altitude by lazy { ValueContainerDouble(EXTRA_KEY_ALTITUDE) }

    /**
     * Speed of the device in meters/second.
     */
    val speed by lazy { ValueContainerFloat(EXTRA_KEY_SPEED) }

    /**
     * Direction of travel in degrees East of true North. If 'hasData' is false,
     * 0.0 is returned (in degree).
     */
    val bearing by lazy {
        object : ValueContainerFloat(EXTRA_KEY_BEARING) {

            override fun validateNewValue(value: Float): Float {
                var bearingNew = value
                while (bearingNew < 0.0f) {
                    bearingNew += 360.0f
                }
                while (bearingNew >= 360.0f) {
                    bearingNew -= 360.0f
                }
                return bearingNew
            }
        }
    }

    /**
     * Horizontal accuracy of the fix. If 'hasData' is false, 0.0 is returned (in m).
     */
    val accuracyHor by lazy { ValueContainerFloat(EXTRA_KEY_ACCURACY_HOR) }

    /**
     * Vertical accuracy of the fix. If 'hasData' is false, 0.0 is returned (in m).
     */
    val accuracyVer by lazy { ValueContainerFloat(EXTRA_KEY_ACCURACY_VER) }

    // SENSOR VALUES

    /**
     * Check if object has any recorded sensor data.
     *
     * This method by-pass initializing of inner objects so it is a good practice to check this
     * value before dealing with any sensor data.
     */
    fun hasAnySensorData(): Boolean {
        return extraDataShort.containsKey(EXTRA_KEY_SENSOR_CADENCE.toInt())
                || extraDataShort.containsKey(EXTRA_KEY_SENSOR_HEART_RATE.toInt())
                || extraDataFloat.containsKey(EXTRA_KEY_SENSOR_SPEED.toInt())
                || extraDataFloat.containsKey(EXTRA_KEY_SENSOR_POWER.toInt())
                || extraDataInt.containsKey(EXTRA_KEY_SENSOR_STRIDES.toInt())
                || extraDataFloat.containsKey(EXTRA_KEY_SENSOR_TEMPERATURE.toInt())
    }

    /**
     * Cadence value. If hasCadence() is false, 0 is returned.
     */
    val sensorCadence by lazy { ValueContainerShort(EXTRA_KEY_SENSOR_CADENCE) }

    /**
     * Heart rate value in BMP. If hasSensorHeartRate() is false, 0 is returned.
     */
    val sensorHeartRate by lazy { ValueContainerShort(EXTRA_KEY_SENSOR_HEART_RATE) }

    /**
     * Speed of the device over ground in meters/second. This speed is defined only when
     * 'speed sensor' is connected and supply valid values.
     *
     * If 'hasData' is 'false', 0.0f is returned (in m/s).
     */
    val sensorSpeed by lazy { ValueContainerFloat(EXTRA_KEY_SENSOR_SPEED) }

    /**
     * Power value of the fix in W. If hasSensorPower() is false, 0.0 is returned.
     */
    val sensorPower by lazy { ValueContainerFloat(EXTRA_KEY_SENSOR_POWER) }

    /**
     * The num of strides. If hasSensorStrides() is false, 0 is returned.
     */
    val sensorStrides by lazy { ValueContainerInt(EXTRA_KEY_SENSOR_STRIDES) }

    /**
     * Temperature value. If hasSensorTemperature() is false, 0.0f is returned.
     */
    val sensorTemperature by lazy { ValueContainerFloat(EXTRA_KEY_SENSOR_TEMPERATURE) }

    // GNSS META-DATA

    val gnssQuality: ValueContainerShort
        get() = ValueContainerShort(EXTRA_KEY_GNSS_STATUS)

    /**
     * Horizontal dilution of precision for current location.
     *
     * More info: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation).
     */
    val gnssHdop: ValueContainerFloat
        get() = ValueContainerFloat(EXTRA_KEY_GNSS_HDOP)

    /**
     * Vertical dilution of precision for current location.
     *
     * More info: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation).
     */
    val gnssVdop: ValueContainerFloat
        get() = ValueContainerFloat(EXTRA_KEY_GNSS_VDOP)

    /**
     * Position (3D) dilution of precision for current location.
     *
     * More info: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation).
     */
    val gnssPdop: ValueContainerFloat
        get() = ValueContainerFloat(EXTRA_KEY_GNSS_PDOP)

    /**
     * Number of used satellites used to obtain current location.
     */
    val gnssSatsUsed: ValueContainerShort
        get() = ValueContainerShort(EXTRA_KEY_GNSS_SATS_USED)

    /**
     * Number of used satellites used to obtain current location.
     */
    val gnssSatsVisible: ValueContainerShort
        get() = ValueContainerShort(EXTRA_KEY_GNSS_SATS_VISIBLE)

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
        get() = if (sensorSpeed.hasData) {
            sensorSpeed.value
        } else speed.value

    /**
     * Check if any speed (GPS or from sensors) is stored.
     */
    @Deprecated(message = "Work with speed value directly")
    fun hasSpeedOptimal(): Boolean {
        return speed.hasData || sensorSpeed.hasData
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

    open inner class ValueContainerFloat(id: Byte)
        : ValueContainer<Float>(extraDataFloat, id) {

        override fun getDefaultEmpty(): Float {
            return 0.0f
        }
    }

    inner class ValueContainerDouble(id: Byte)
        : ValueContainer<Double>(extraDataDouble, id) {

        override fun getDefaultEmpty(): Double {
            return 0.0
        }
    }

    //*************************************************
    // TOOLS
    //*************************************************

    /**
     * Remove all attached sensors values.
     */
    fun removeSensorAll() {
        sensorCadence.remove()
        sensorHeartRate.remove()
        sensorPower.remove()
        sensorSpeed.remove()
        sensorStrides.remove()
        sensorTemperature.remove()
    }

    /**
     * Remove all values related to GNSS metadata.
     */
    fun removeGnssAll() {
        gnssQuality.remove()
        gnssHdop.remove()
        gnssVdop.remove()
        gnssPdop.remove()
        gnssSatsUsed.remove()
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
            this.altitude.value = altitude
        }

        // red basic data
        if (dr.readBoolean()) {
            val hasAccuracy = dr.readBoolean()
            val accuracy = dr.readFloat()
            if (hasAccuracy) {
                this.accuracyHor.value = accuracy
            }
            val hasBearing = dr.readBoolean()
            val bearing = dr.readFloat()
            if (hasBearing) {
                this.bearing.value = bearing
            }
            val hasSpeed = dr.readBoolean()
            val speed = dr.readFloat()
            if (hasSpeed) {
                this.speed.value = speed
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
                    sensorCadence.value = extraSensor.cadence.toShort()
                }
                if (extraSensor.hasHr) {
                    sensorHeartRate.value = extraSensor.hr.toShort()
                }
                if (extraSensor.hasPower) {
                    sensorPower.value = extraSensor.power
                }
                if (extraSensor.hasSpeed) {
                    sensorSpeed.value = extraSensor.speed
                }
                if (extraSensor.hasStrides) {
                    sensorStrides.value = extraSensor.strides
                }
                if (extraSensor.hasTemperature) {
                    sensorTemperature.value = extraSensor.temperature
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
        dw.writeBoolean(altitude.hasData)
        dw.writeDouble(altitude.value)

        // write (deprecated) basic data
        if (accuracyHor.hasData || bearing.hasData || speed.hasData) {
            dw.writeBoolean(true)
            dw.writeBoolean(accuracyHor.hasData)
            dw.writeFloat(accuracyHor.value)
            dw.writeBoolean(bearing.hasData)
            dw.writeFloat(bearing.value)
            dw.writeBoolean(speed.hasData)
            dw.writeFloat(speed.value)
        } else {
            dw.writeBoolean(false)
        }

        // write sensors data (version 1+)
        val extraSensor = ExtraSensor().apply {
            if (sensorCadence.hasData) {
                hasCadence = true
                cadence = sensorCadence.value.toInt()
            }
            if (sensorHeartRate.hasData) {
                hasHr = true
                hr = sensorHeartRate.value.toInt()
            }
            if (sensorPower.hasData) {
                hasPower = true
                power = sensorPower.value
            }
            if (sensorSpeed.hasData) {
                hasSpeed = true
                speed = sensorSpeed.value
            }
            if (sensorStrides.hasData) {
                hasStrides = true
                strides = sensorStrides.value
            }
            if (sensorTemperature.hasData) {
                hasTemperature = true
                temperature = sensorTemperature.value
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

        private const val EXTRA_KEY_ALTITUDE = 10.toByte()
        private const val EXTRA_KEY_SPEED = 11.toByte()
        private const val EXTRA_KEY_BEARING = 12.toByte()
        private const val EXTRA_KEY_ACCURACY_HOR = 13.toByte()
        private const val EXTRA_KEY_ACCURACY_VER = 14.toByte()

        private const val EXTRA_KEY_SENSOR_HEART_RATE = 20.toByte()
        private const val EXTRA_KEY_SENSOR_CADENCE = 21.toByte()
        private const val EXTRA_KEY_SENSOR_SPEED = 22.toByte()
        private const val EXTRA_KEY_SENSOR_TEMPERATURE = 23.toByte()
        private const val EXTRA_KEY_SENSOR_POWER = 24.toByte()
        private const val EXTRA_KEY_SENSOR_STRIDES = 25.toByte()

        private const val EXTRA_KEY_GNSS_STATUS = 51.toByte()
        private const val EXTRA_KEY_GNSS_HDOP = 52.toByte()
        private const val EXTRA_KEY_GNSS_VDOP = 53.toByte()
        private const val EXTRA_KEY_GNSS_PDOP = 54.toByte()
        private const val EXTRA_KEY_GNSS_SATS_USED = 55.toByte()
        private const val EXTRA_KEY_GNSS_SATS_VISIBLE = 56.toByte()
    }
}
