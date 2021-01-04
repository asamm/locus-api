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
import locus.api.utils.LocationCompute
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import locus.api.utils.Logger
import locus.api.utils.Utils

import java.io.IOException

open class Location() : Storable() {

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

    // ALTITUDE

    /**
     * Flag if altitude is set.
     */
    var hasAltitude: Boolean = false

    /**
     * Altitude value of the location (in m). If [hasAltitude] is false, 0.0f is returned.
     */
    var altitude: Double
        get() = if (hasAltitude) {
            _altitude
        } else 0.0
        set(altitude) {
            this._altitude = altitude
            this.hasAltitude = true
        }

    /**
     * Backing field for altitude value (in m).
     */
    private var _altitude: Double = 0.0

    /**
     * Clears the altitude of this fix. Following this call, hasAltitude() will return false.
     */
    fun removeAltitude() {
        this._altitude = 0.0
        this.hasAltitude = false
    }

    /**
     * Container for basic values.
     */
    private var extraBasic: ExtraBasic? = null
    /**
     * Container for sensor values.
     */
    private var extraSensor: ExtraSensor? = null

    /**
     * Container for extended data for location class.
     */
    private class ExtraBasic internal constructor() : Cloneable {

        internal var hasSpeed: Boolean = false
        internal var speed: Float = 0.toFloat()

        internal var hasBearing: Boolean = false
        internal var bearing: Float = 0.toFloat()

        internal var hasAccuracy: Boolean = false
        internal var accuracy: Float = 0.toFloat()

        init {
            hasSpeed = false
            speed = 0.0f
            hasBearing = false
            bearing = 0.0f
            hasAccuracy = false
            accuracy = 0.0f
        }

        public override fun clone(): ExtraBasic {
            val newExtra = ExtraBasic()
            newExtra.hasSpeed = hasSpeed
            newExtra.speed = speed
            newExtra.hasBearing = hasBearing
            newExtra.bearing = bearing
            newExtra.hasAccuracy = hasAccuracy
            newExtra.accuracy = accuracy
            return newExtra
        }

        internal fun hasData(): Boolean {
            return hasSpeed || hasBearing || hasAccuracy
        }

        override fun toString(): String {
            return Utils.toString(this@ExtraBasic, "    ")
        }
    }

    /**
     * Container for data usually received from sensors.
     */
    private class ExtraSensor : Storable(), Cloneable {

        internal var hasHr: Boolean = false
        internal var hr: Int = 0

        internal var hasCadence: Boolean = false
        internal var cadence: Int = 0

        internal var hasSpeed: Boolean = false
        internal var speed: Float = 0.toFloat()

        internal var hasPower: Boolean = false
        internal var power: Float = 0.toFloat()

        internal var hasStrides: Boolean = false
        internal var strides: Int = 0

        internal var hasTemperature: Boolean = false
        internal var temperature: Float = 0.toFloat()

        init {
            hasHr = false
            hr = 0
            hasCadence = false
            cadence = 0
            hasSpeed = false
            speed = 0.0f
            hasPower = false
            power = 0.0f
            hasStrides = false
            strides = 0
            hasTemperature = false
            temperature = 0f
        }

        public override fun clone(): ExtraSensor {
            val newExtra = ExtraSensor()
            newExtra.hasHr = hasHr
            newExtra.hr = hr
            newExtra.hasCadence = hasCadence
            newExtra.cadence = cadence
            newExtra.hasSpeed = hasSpeed
            newExtra.speed = speed
            newExtra.hasPower = hasPower
            newExtra.power = power
            newExtra.hasStrides = hasStrides
            newExtra.strides = strides
            newExtra.hasTemperature = hasTemperature
            newExtra.temperature = temperature
            return newExtra
        }

        internal fun hasData(): Boolean {
            return hasHr || hasCadence ||
                    hasSpeed || hasPower ||
                    hasStrides || hasTemperature
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

        override fun toString(): String {
            return Utils.toString(this@ExtraSensor, "    ")
        }
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
     *
     * @param loc source location object
     */
    fun set(loc: Location) {
        id = loc.id
        provider = loc.provider
        time = loc.time
        latitude = loc.latitude
        longitude = loc.longitude
        hasAltitude = loc.hasAltitude
        _altitude = loc._altitude

        // set extra basic data
        if (loc.extraBasic != null && loc.extraBasic!!.hasData()) {
            extraBasic = loc.extraBasic!!.clone()
            if (!extraBasic!!.hasData()) {
                extraBasic = null
            }
        } else {
            extraBasic = null
        }

        // set extra ant data
        if (loc.extraSensor != null && loc.extraSensor!!.hasData()) {
            extraSensor = loc.extraSensor!!.clone()
            if (!extraSensor!!.hasData()) {
                extraSensor = null
            }
        } else {
            extraSensor = null
        }
    }

    //*************************************************
    // STORABLE
    //*************************************************

    override fun getVersion(): Int {
        return 2
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readLong()
        provider = dr.readString()
        time = dr.readLong()
        latitude = dr.readDouble()
        longitude = dr.readDouble()
        hasAltitude = dr.readBoolean()
        _altitude = dr.readDouble()

        // red basic data
        if (dr.readBoolean()) {
            extraBasic = ExtraBasic().apply {
                hasAccuracy = dr.readBoolean()
                accuracy = dr.readFloat()
                hasBearing = dr.readBoolean()
                bearing = dr.readFloat()
                hasSpeed = dr.readBoolean()
                speed = dr.readFloat()
            }.takeIf { it.hasData() }
        }

        // V1
        if (version >= 1) {
            // read sensor data
            if (dr.readBoolean()) {
                if (version == 1) {
                    readSensorVersion1(dr)
                } else {
                    extraSensor = ExtraSensor().apply { read(dr) }
                }
            }
        }
    }

    private fun readSensorVersion1(dr: DataReaderBigEndian) {
        extraSensor = ExtraSensor().apply {
            hasHr = dr.readBoolean()
            hr = dr.readInt()
            hasCadence = dr.readBoolean()
            cadence = dr.readInt()
            hasSpeed = dr.readBoolean()
            speed = dr.readFloat()
            hasPower = dr.readBoolean()
            power = dr.readFloat()
        }.takeIf { it.hasData() }
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

        // write basic data
        extraBasic
                ?.takeIf { it.hasData() }
                ?.let {
                    dw.writeBoolean(true)
                    dw.writeBoolean(it.hasAccuracy)
                    dw.writeFloat(it.accuracy)
                    dw.writeBoolean(it.hasBearing)
                    dw.writeFloat(it.bearing)
                    dw.writeBoolean(it.hasSpeed)
                    dw.writeFloat(it.speed)
                } ?: {
            dw.writeBoolean(false)
        }()

        // write ant data (version 1+)
        extraSensor
                ?.takeIf { it.hasData() }
                ?.let {
                    dw.writeBoolean(true)
                    it.write(dw)
                } ?: {
            dw.writeBoolean(false)
        }()
    }

    //*************************************************
    // BASIC EXTRA DATA
    //*************************************************

    // SPEED

    /**
     * Check if location contains speed information.
     */
    fun hasSpeed(): Boolean {
        return extraBasic?.hasSpeed == true
    }

    /**
     * Speed of the device over ground in meters/second. If hasSpeed() is false,
     * 0.0f is returned (in m/s).
     */
    var speed: Float
        get() = if (hasSpeed()) {
            extraBasic!!.speed
        } else 0.0f
        set(speed) {
            if (extraBasic == null) {
                extraBasic = ExtraBasic()
            }
            extraBasic!!.speed = speed
            extraBasic!!.hasSpeed = true
        }

    /**
     * Check if at least some speed is stored.
     *
     * @return `true` if speed is stored in this object
     */
    fun hasSpeedOptimal(): Boolean {
        return hasSpeed() || hasSensorSpeed()
    }

    /**
     * Get stored speed useful for display to users or for compute with some operations, based on best
     * available speed. If speed from sensors is stored (more precise) it is returned. Otherwise
     * basic GPS speed is returned.
     *
     * @return speed for display purpose
     */
    val speedOptimal: Float
        get() = if (hasSensorSpeed()) {
            sensorSpeed
        } else speed

    /**
     * Clears the speed of this fix.  Following this call, hasSpeed() will return false.
     */
    fun removeSpeed() {
        // check container
        if (extraBasic == null) {
            return
        }

        // remove parameter
        extraBasic!!.speed = 0.0f
        extraBasic!!.hasSpeed = false
        checkExtraBasic()
    }

    // BEARING

    /**
     * Direction of travel in degrees East of true North. If hasBearing() is false,
     * 0.0 is returned (in degree).
     */
    var bearing: Float
        get() = if (hasBearing()) {
            extraBasic!!.bearing
        } else 0.0f
        set(bearing) {
            var bearingNew = bearing
            while (bearingNew < 0.0f) {
                bearingNew += 360.0f
            }
            while (bearingNew >= 360.0f) {
                bearingNew -= 360.0f
            }

            // set value
            if (extraBasic == null) {
                extraBasic = ExtraBasic()
            }
            extraBasic!!.bearing = bearingNew
            extraBasic!!.hasBearing = true
        }

    /**
     * Check if the location is able to report bearing information.
     */
    fun hasBearing(): Boolean {
        return extraBasic?.hasBearing == true
    }

    /**
     * Clears the bearing of this fix.  Following this call, hasBearing()
     * will return false.
     */
    fun removeBearing() {
        // check data
        if (extraBasic == null) {
            return
        }

        // remove parameter
        extraBasic!!.bearing = 0.0f
        extraBasic!!.hasBearing = false
        checkExtraBasic()
    }

    // ACCURACY

    /**
     * Accuracy of the fix. If hasAccuracy() is false, 0.0 is returned (in m).
     */
    var accuracy: Float
        get() = if (hasAccuracy()) {
            extraBasic!!.accuracy
        } else 0.0f
        set(accuracy) {
            if (extraBasic == null) {
                extraBasic = ExtraBasic()
            }
            extraBasic!!.accuracy = accuracy
            extraBasic!!.hasAccuracy = true
        }

    /**
     * Returns true if the provider is able to report accuracy information,
     * false otherwise.  The default implementation returns false.
     *
     * @return `true` is location has defined accuracy
     */
    fun hasAccuracy(): Boolean {
        return extraBasic?.hasAccuracy == true
    }

    /**
     * Clears the accuracy of this fix.  Following this call, hasAccuracy() will return false.
     */
    fun removeAccuracy() {
        // check container
        if (extraBasic == null) {
            return
        }

        // remove data
        extraBasic!!.accuracy = 0.0f
        extraBasic!!.hasAccuracy = false
        checkExtraBasic()
    }

    // TOOLS

    /**
     * Check extra data container and remove it if no data exists.
     */
    private fun checkExtraBasic() {
        if (!extraBasic!!.hasData()) {
            extraBasic = null
        }
    }

    //*************************************************
    // EXTRA SENSORS DATA
    //*************************************************

    // HEART RATE

    /**
     * Heart rate value in BMP. If hasSensorHeartRate() is false, 0.0 is returned.
     */
    var sensorHeartRate: Int
        get() = if (hasSensorHeartRate()) {
            extraSensor!!.hr
        } else 0
        set(heartRate) {
            if (extraSensor == null) {
                extraSensor = ExtraSensor()
            }
            extraSensor!!.hasHr = true
            extraSensor!!.hr = heartRate
        }

    /**
     * Returns true if the provider is able to report Heart rate information, false otherwise.
     * The default implementation returns false.
     */
    fun hasSensorHeartRate(): Boolean {
        return extraSensor?.hasHr == true
    }

    /**
     * Clears the accuracy of this fix.  Following this call, hasSensorHeartRate()
     * will return false.
     */
    fun removeSensorHeartRate() {
        extraSensor?.let {
            it.hasHr = false
            it.hr = 0
            checkExtraSensor()
        }
    }

    // CADENCE

    /**
     * Cadence value. If hasCadence() is false, 0 is returned.
     */
    var sensorCadence: Int
        get() = if (hasSensorCadence()) {
            extraSensor!!.cadence
        } else 0
        set(cadence) {
            if (extraSensor == null) {
                extraSensor = ExtraSensor()
            }
            extraSensor!!.hasCadence = true
            extraSensor!!.cadence = cadence
        }

    /**
     * Returns true if the provider is able to report cadence information, false otherwise.
     * The default implementation returns false.
     */
    fun hasSensorCadence(): Boolean {
        return extraSensor?.hasCadence == true
    }

    /**
     * Clears the cadence of this fix.  Following this call, hasCadence() will return false.
     */
    fun removeSensorCadence() {
        extraSensor?.let {
            it.hasCadence = false
            it.cadence = 0
            checkExtraSensor()
        }
    }

    // SPEED

    /**
     * Speed of the fix in meters per sec. If hasSensorSpeed() is false, 0.0 is returned.
     */
    var sensorSpeed: Float
        get() = if (hasSensorSpeed()) {
            extraSensor!!.speed
        } else 0.0f
        set(speed) {
            if (extraSensor == null) {
                extraSensor = ExtraSensor()
            }
            extraSensor!!.hasSpeed = true
            extraSensor!!.speed = speed
        }

    /**
     * Returns true if the provider is able to report speed value, false otherwise.
     * The default implementation returns false.
     */
    fun hasSensorSpeed(): Boolean {
        return extraSensor?.hasSpeed == true
    }

    /**
     * Clears the speed of this fix.  Following this call, hasSensorSpeed() will return false.
     */
    fun removeSensorSpeed() {
        extraSensor?.let {
            it.hasSpeed = false
            it.speed = 0.0f
            checkExtraSensor()
        }
    }

    // POWER

    /**
     * Power value of the fix in W. If hasSensorPower() is false, 0.0 is returned.
     *
     * @return power value (in Watts)
     */
    var sensorPower: Float
        get() = if (hasSensorPower()) {
            extraSensor!!.power
        } else 0.0f
        set(power) {
            if (extraSensor == null) {
                extraSensor = ExtraSensor()
            }
            extraSensor!!.hasPower = true
            extraSensor!!.power = power
        }

    /**
     * Returns true if the provider is able to report power value, false otherwise.
     * The default implementation returns false.
     */
    fun hasSensorPower(): Boolean {
        return extraSensor?.hasPower == true
    }

    /**
     * Clears the power of this fix.  Following this call, hasSensorPower()
     * will return false.
     */
    fun removeSensorPower() {
        extraSensor?.let {
            it.hasPower = false
            it.power = 0.0f
            checkExtraSensor()
        }
    }

    // STRIDES

    /**
     * The num of strides. If hasSensorStrides() is false, 0 is returned.
     */
    var sensorStrides: Int
        get() = if (hasSensorStrides()) {
            extraSensor!!.strides
        } else 0
        set(strides) {
            if (extraSensor == null) {
                extraSensor = ExtraSensor()
            }
            extraSensor!!.hasStrides = true
            extraSensor!!.strides = strides
        }

    /**
     * Returns true if the provider is able to report strides value, false otherwise.
     * The default implementation returns false.
     */
    fun hasSensorStrides(): Boolean {
        return extraSensor?.hasStrides == true
    }

    /**
     * Clears the num of strides. Following this call, hasSensorStrides() will return false.
     */
    fun removeSensorStrides() {
        extraSensor?.let {
            it.hasStrides = false
            it.strides = 0
            checkExtraSensor()
        }
    }

    // TEMPERATURE

    /**
     * Temperature value. If hasSensorTemperature() is false, 0.0f is returned.
     */
    var sensorTemperature: Float
        get() = if (hasSensorTemperature()) {
            extraSensor!!.temperature
        } else 0.0f
        set(temperature) {
            if (extraSensor == null) {
                extraSensor = ExtraSensor()
            }
            extraSensor!!.hasTemperature = true
            extraSensor!!.temperature = temperature
        }

    /**
     * Returns true if the provider is able to report temperature value, false otherwise.
     * The default implementation returns false.
     */
    fun hasSensorTemperature(): Boolean {
        return extraSensor?.hasTemperature == true
    }

    /**
     * Clears the temperature value. Following this call, hasSensorTemperature()
     * will return false.
     */
    fun removeSensorTemperature() {
        extraSensor?.let {
            it.hasTemperature = false
            it.temperature = 0.0f
            checkExtraSensor()
        }
    }

    // TOOLS

    private fun checkExtraSensor() {
        if (extraSensor?.hasData() != true) {
            extraSensor = null
        }
    }

    /**
     * Clear all attached sensors values.
     */
    fun removeSensorAll() {
        extraSensor = null
    }

    //*************************************************
    // UTILS PART
    //*************************************************

    override fun toString(): String {
        return "Location [" +
                "tag: $provider, " +
                "time: $time, " +
                "lon: $longitude, " +
                "lat: $latitude, " +
                "alt: $altitude]"
    }

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

    companion object {

        // tag for logger
        private const val TAG = "Location"
    }
}
