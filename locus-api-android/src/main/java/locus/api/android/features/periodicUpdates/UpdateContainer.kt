package locus.api.android.features.periodicUpdates

import java.io.IOException
import locus.api.objects.Storable
import locus.api.objects.enums.PointRteAction
import locus.api.objects.extra.Location
import locus.api.objects.extra.TrackStats
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

class UpdateContainer : Storable() {

    //*************************************************
    // STATE CUSTOM VARIABLES
    //*************************************************

    /**
     * Flag if exists new location received from GPS/Wi-fi.
     */
    var isNewMyLocation: Boolean = false
    // is new map center available
    /**
     * Flag if user moved map to new location.
     */
    var isNewMapCenter: Boolean = false
    /**
     * Flag if user zoomed map to new zoom level.
     */
    var isNewZoomLevel: Boolean = false
    /**
     * Indicate if user is currently touching a map screen. It do not indicate
     * which specific action is doing, only that something is happening.
     */
    var isUserTouching: Boolean = false
    /**
     * Flag if GPS is currently enabled in Locus.
     */
    var isEnabledMyLocation: Boolean = false

    //*************************************************
    // LOCATION, GPS, BASIC VALUES
    //*************************************************

    /**
     * Current location is GPS/Wi-fi enabled. Also contain all sensors values.
     * Should be only container for sensor data in case, GPS is off anyway even in this case,
     * location will be set to last known location.
     *
     * @return current [Location] or just container for sensors data
     */
    var locMyLocation: Location = Location()
    /**
     * Information if current [.getLocMyLocation] location is valid GPS location
     * with usable accuracy (GPS has correctly computed "fix" value).
     */
    var isGpsLocValid: Boolean = false
    /**
     * Number of used satellites for GPS fix.
     */
    var gpsSatsUsed: Int = 0
    /**
     * Total number of visible satellites.
     */
    var gpsSatsAll: Int = 0
    /**
     * Current declination computed from a) current GPS location, or b) last known location 
     * Locus app knows.
     */
    var declination: Float = 0.0f
    /**
     * Current device orientation. Source for value depend on settings in Locus.
     * Also this value will be 0 in case, user do not need orientation for any action.
     *
     * @return orientation value (in degrees) or 0 if orientation is not used or known
     */
    var orientHeading: Float = 0.0f
    /**
     * Return opposite value to [.getOrientHeading]
     *
     * @return orientation (in degree) or 0 if orientation is not used or known
     */
    var orientHeadingOpposit: Float = 0.0f
    /**
     * Current device course. Source for this value is change in coordinates,
     * or directly GPS values.
     *
     * @return course value (in degrees) or 0 if course is not known
     */
    var orientCourse: Float = 0.0f
    /**
     * Pitch angle of current device.
     * For more info see [wiki](http://en.wikipedia.org/wiki/Flight_dynamics)
     *
     * @return pitch angle (in degree) or 0 if not known
     */
    var orientPitch: Float = 0.0f
    /**
     * Roll angle of current device.
     * For more info see [wiki](http://en.wikipedia.org/wiki/Flight_dynamics)
     *
     * @return roll angle (in degree) or 0 if not known
     */
    var orientRoll: Float = 0.0f
    /**
     * Angle between GPS shift and current orientation. This values differ from 0
     * based on current outside conditions
     *
     * @return angle (in degrees) or 0 if no shift exists or GPS or sensors are disabled
     */
    var orientGpsAngle: Float = 0.0f
    /**
     * Current users pace value (min / km). Computed pace is from highly filtered speed, not from
     * real speed included in current [locMyLocation] object.
     */
    var pace: Float = 0.0f
    /**
     * Vertical speed from last few (around 5) seconds of enabled GPS
     *
     * @return vertical speed in m/s
     */
    var speedVertical: Float = 0.0f
    /**
     * Current (little filtered) slope value. More about slope, for example at
     * [Wikipedia](http://en.wikipedia.org/wiki/Slope)
     *
     * @return slope in 0.01%
     */
    var slope: Float = 0.0f

    //*************************************************
    // MAP STUFF
    //*************************************************

    /**
     * Flag if some screen with map is currently visible
     *
     * @return `true` if map is visible
     */
    var isMapVisible: Boolean = false
    /**
     * Current rotation value of map screen. This value may be same as current
     * orientation or should be value by user manual rotation
     *
     * @return angle (in degrees) as map rotate
     */
    var mapRotate: Float = 0.0f
    /**
     * Even if map is visible or not, this define current map center.
     *
     * @return current map center
     */
    var locMapCenter: Location? = null
    /**
     * Top-left coordinate of current map screen
     *
     * @return max top-left visible location
     */
    var mapTopLeft: Location? = null
    /**
     * Bottom-right coordinate of current map screen
     *
     * @return max bottom-right visible location
     */
    var mapBottomRight: Location? = null
    /**
     * Current map zoom level. This value should not be precise in case of using
     * custom map projections, anyway it's always closest possible value.
     *
     * @return integer as a current zoom level (zoom 8 == whole world (1 tile 256x256px))
     */
    var mapZoomLevel: Int = -1

    //*************************************************
    // TRACK RECORDING
    //*************************************************

    /**
     * Flag if track recording is currently active. It do not info about it's state
     * (paused, running), just if it is enabled at all.
     *
     * @return `true` if track recording is active
     */
    var isTrackRecRecording: Boolean = false
    /**
     * Flag if track recording is currently active or paused. Firstly check if recording is running
     * before testing if is paused.
     *
     * @return `true` if track recording is paused
     */
    var isTrackRecPaused: Boolean = false
    /**
     * Name of the active recording profile.
     *
     * @return profile name
     */
    var trackRecProfileName: String = ""
    /**
     * Complete track statistics. This container is available only when [isTrackRecRecording]
     * returns `true`.
     *
     * @return track statistics
     */
    var trackRecStats: TrackStats? = null

    //*************************************************
    // GUIDANCE PART
    //*************************************************

    /**
     * Get current guiding type.
     */
    var guideType: Int = GUIDE_TYPE_DISABLED
    /**
     * ID of current target.
     */
    var guideTargetId: Long = -1L
    /**
     * Name of guiding target.
     */
    var guideWptName: String = ""
    /**
     * Current guiding location.
     */
    var guideWptLoc: Location? = null
    /**
     * Distance to target.
     */
    var guideWptDist: Double = 0.0
    /**
     * Azimuth to target.
     */
    var guideWptAzim: Float = 0.0f
    /**
     * Bearing to target.
     */
    var guideWptAngle: Float = 0.0f
    /**
     * Expected time to target.
     */
    var guideWptTime: Long = 0L

    /**
     * Flag if active track guidance is valid.
     */
    var guideValid: Boolean = true
    /**
     * Distance from start (in case of guiding along track).
     */
    var guideDistFromStart: Double = 0.0
    /**
     * Distance to finish (in case of guiding along track).
     */
    var guideDistToFinish: Double = 0.0
    /**
     * Expected time to finish (in case of guiding along track).
     */
    var guideTimeToFinish: Long = 0L

    /**
     * Name of current navigation target point.
     */
    var guideNavPoint1Name: String = ""
    /**
     * Location of current navigation point.
     */
    var guideNavPoint1Loc: Location? = null
    /**
     * Distance to current navigation point.
     */
    var guideNavPoint1Dist: Double = 0.0
    /**
     * Time to current navigation point.
     */
    var guideNavPoint1Time: Long = 0L
    /**
     * Action that happen on current navigation point.
     */
    var guideNavPoint1Action: PointRteAction = PointRteAction.UNDEFINED
    /**
     * Extra information for navigation point.
     */
    var guideNavPoint1Extra: String = ""

    /**
     * Name of next navigation target point.
     */
    var guideNavPoint2Name: String = ""
    /**
     * Location of next navigation point.
     */
    var guideNavPoint2Loc: Location? = null
    /**
     * Distance to next navigation point.
     */
    var guideNavPoint2Dist: Double = 0.0
    /**
     * Time to next navigation point.
     */
    var guideNavPoint2Time: Long = 0L
    /**
     * Action that happen on next navigation point.
     */
    var guideNavPoint2Action: PointRteAction = PointRteAction.UNDEFINED
    /**
     * Extra information for navigation point.
     */
    var guideNavPoint2Extra: String = ""

    //*************************************************
    // VARIOUS
    //*************************************************

    /**
     * ID of active visible dashboard.
     */
    var activeDashboardId: String = ""
    /**
     * ID of active running Live tracking.
     */
    var activeLiveTrackId: String = ""
    /**
     * Battery value of current device
     *
     * @return value of current battery in percents (0 - 100)
     */
    var deviceBatteryValue: Int = 0
    /**
     * Current battery temperature
     *
     * @return temperature of current device battery in °C
     */
    var deviceBatteryTemperature: Float = 0.0f

    //*************************************************/
    // TOOLS
    //*************************************************/

    /**
     * Flag is guidance is enabled.
     *
     * @return `true` if guiding is enabled
     */
    val isGuideEnabled: Boolean
        get() = guideType != GUIDE_TYPE_DISABLED

    /**
     * Generate container for guiding parameters to single point. In case, this guiding is not
     * active, result of this call is `null`.
     *
     * @return guiding container or `null` if this guiding is not enabled
     */
    val contentGuidePoint: UpdateContainerGuidePoint?
        get() = if (guideType != GUIDE_TYPE_WAYPOINT) {
            null
        } else UpdateContainerGuidePoint(guideType,
                guideTargetId, guideWptName, guideWptLoc!!, guideWptDist,
                guideWptAzim, guideWptAngle, guideWptTime)

    /**
     * Generate container for guiding parameters along the track. In case, this guiding is not
     * active, result of this call is `null`.
     *
     * @return guiding container or `null` if this guiding is not enabled
     */
    val contentGuideTrack: UpdateContainerGuideTrack?
        get() {
            // check if guiding is enabled
            if (guideType != GUIDE_TYPE_TRACK_GUIDE && guideType != GUIDE_TYPE_TRACK_NAVIGATION) {
                return null
            }

            // generate first navigation point
            var navPoint1: UpdateContainerGuideTrack.NavPoint? = null
            if (guideNavPoint1Loc != null) {
                navPoint1 = UpdateContainerGuideTrack.NavPoint(
                        guideNavPoint1Name, guideNavPoint1Loc!!, guideNavPoint1Action,
                        guideNavPoint1Dist, guideNavPoint1Time, guideNavPoint1Extra)
            }

            // generate second navigation point
            var navPoint2: UpdateContainerGuideTrack.NavPoint? = null
            if (guideNavPoint2Loc != null) {
                navPoint2 = UpdateContainerGuideTrack.NavPoint(
                        guideNavPoint2Name, guideNavPoint2Loc!!, guideNavPoint2Action,
                        guideNavPoint2Dist, guideNavPoint2Time, guideNavPoint2Extra)
            }

            // return generated container
            return UpdateContainerGuideTrack(guideType,
                    guideTargetId, guideWptName, guideWptLoc!!, guideWptDist,
                    guideWptAzim, guideWptAngle, guideWptTime,
                    guideValid, guideDistFromStart, guideDistToFinish, guideTimeToFinish,
                    navPoint1, navPoint2)
        }

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 5
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {

        // STATE CUSTOM VARIABLES

        isNewMyLocation = dr.readBoolean()
        isNewMapCenter = dr.readBoolean()
        isNewZoomLevel = dr.readBoolean()
        isUserTouching = dr.readBoolean()
        isEnabledMyLocation = dr.readBoolean()

        // LOCATION, GPS, BASIC VALUES

        locMyLocation = readLocation(dr) ?: locMyLocation
        gpsSatsUsed = dr.readInt()
        gpsSatsAll = dr.readInt()
        declination = dr.readFloat()
        orientHeading = dr.readFloat()
        orientHeadingOpposit = dr.readFloat()
        orientCourse = dr.readFloat()
        orientPitch = dr.readFloat()
        orientRoll = dr.readFloat()
        orientGpsAngle = dr.readFloat()
        speedVertical = dr.readFloat()
        slope = dr.readFloat()

        // MAP STUFF

        isMapVisible = dr.readBoolean()
        mapRotate = dr.readFloat()
        locMapCenter = readLocation(dr)
        mapTopLeft = readLocation(dr)
        mapBottomRight = readLocation(dr)
        mapZoomLevel = dr.readInt()

        // TRACK RECORDING PART

        isTrackRecRecording = dr.readBoolean()
        isTrackRecPaused = dr.readBoolean()
        trackRecProfileName = dr.readString()
        if (dr.readBoolean()) {
            trackRecStats = TrackStats()
            trackRecStats!!.read(dr)
        }

        // GUIDING PART

        guideType = dr.readInt()
        guideWptName = dr.readString()
        guideWptLoc = readLocation(dr)
        guideWptDist = dr.readDouble()
        guideWptAzim = dr.readFloat()
        guideWptAngle = dr.readFloat()
        guideWptTime = dr.readLong()
        guideDistFromStart = dr.readDouble()
        guideDistToFinish = dr.readDouble()
        guideTimeToFinish = dr.readLong()
        guideNavPoint1Name = dr.readString()
        guideNavPoint1Loc = readLocation(dr)
        guideNavPoint1Dist = dr.readDouble()
        guideNavPoint1Time = dr.readLong()
        guideNavPoint1Action = PointRteAction.getActionById(dr.readInt())
        guideNavPoint2Name = dr.readString()
        guideNavPoint2Loc = readLocation(dr)
        guideNavPoint2Dist = dr.readDouble()
        guideNavPoint2Time = dr.readLong()
        guideNavPoint2Action = PointRteAction.getActionById(dr.readInt())

        // VARIOUS

        deviceBatteryValue = dr.readInt()
        deviceBatteryTemperature = dr.readFloat()

        // V1
        if (version >= 1) {
            guideValid = dr.readBoolean()
        }

        // V2
        if (version >= 2) {
            activeDashboardId = dr.readString()
            activeLiveTrackId = dr.readString()
        }

        // V3
        if (version >= 3) {
            guideTargetId = dr.readLong()
        }

        // V4
        if (version >= 4) {
            isGpsLocValid = dr.readBoolean()
        }

        // V5
        if (version >= 5) {
            guideNavPoint1Extra = dr.readString()
            guideNavPoint2Extra = dr.readString()
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {

        // STATE CUSTOM VARIABLES

        dw.writeBoolean(isNewMyLocation)
        dw.writeBoolean(isNewMapCenter)
        dw.writeBoolean(isNewZoomLevel)
        dw.writeBoolean(isUserTouching)
        dw.writeBoolean(isEnabledMyLocation)

        // LOCATION, GPS, BASIC VALUES

        writeLocation(dw, locMyLocation)
        dw.writeInt(gpsSatsUsed)
        dw.writeInt(gpsSatsAll)
        dw.writeFloat(declination)
        dw.writeFloat(orientHeading)
        dw.writeFloat(orientHeadingOpposit)
        dw.writeFloat(orientCourse)
        dw.writeFloat(orientPitch)
        dw.writeFloat(orientRoll)
        dw.writeFloat(orientGpsAngle)
        dw.writeFloat(speedVertical)
        dw.writeFloat(slope)

        // MAP STUFF

        dw.writeBoolean(isMapVisible)
        dw.writeFloat(mapRotate)
        writeLocation(dw, locMapCenter)
        writeLocation(dw, mapTopLeft)
        writeLocation(dw, mapBottomRight)
        dw.writeInt(mapZoomLevel)

        // TRACK RECORDING PART

        dw.writeBoolean(isTrackRecRecording)
        dw.writeBoolean(isTrackRecPaused)
        dw.writeString(trackRecProfileName)
        if (trackRecStats != null) {
            dw.writeBoolean(true)
            dw.writeStorable(trackRecStats!!)
        } else {
            dw.writeBoolean(false)
        }

        // GUIDING PART

        dw.writeInt(guideType)
        dw.writeString(guideWptName)
        writeLocation(dw, guideWptLoc)
        dw.writeDouble(guideWptDist)
        dw.writeFloat(guideWptAzim)
        dw.writeFloat(guideWptAngle)
        dw.writeLong(guideWptTime)
        dw.writeDouble(guideDistFromStart)
        dw.writeDouble(guideDistToFinish)
        dw.writeLong(guideTimeToFinish)
        dw.writeString(guideNavPoint1Name)
        writeLocation(dw, guideNavPoint1Loc)
        dw.writeDouble(guideNavPoint1Dist)
        dw.writeLong(guideNavPoint1Time)
        dw.writeInt(guideNavPoint1Action.id)
        dw.writeString(guideNavPoint2Name)
        writeLocation(dw, guideNavPoint2Loc)
        dw.writeDouble(guideNavPoint2Dist)
        dw.writeLong(guideNavPoint2Time)
        dw.writeInt(guideNavPoint2Action.id)

        // VARIOUS

        dw.writeInt(deviceBatteryValue)
        dw.writeFloat(deviceBatteryTemperature)

        // V1
        dw.writeBoolean(guideValid)

        // V2
        dw.writeString(activeDashboardId)
        dw.writeString(activeLiveTrackId)

        // V3
        dw.writeLong(guideTargetId)

        // V4
        dw.writeBoolean(isGpsLocValid)

        // V5
        dw.writeString(guideNavPoint1Extra)
        dw.writeString(guideNavPoint2Extra)
    }

    /**
     * Read location from reader.
     *
     * @param dr reader
     * @return location or throw exception
     */
    @Throws(IOException::class)
    private fun readLocation(dr: DataReaderBigEndian): Location? {
        // check existence
        val exists = dr.readBoolean()
        if (!exists) {
            return null
        }

        // finally read location
        try {
            return dr.readStorable(Location::class.java)
        } catch (e: InstantiationException) {
            throw IOException(e.message)
        } catch (e: IllegalAccessException) {
            throw IOException(e.message)
        } catch (e: IOException) {
            throw IOException(e.message)
        }

    }

    /**
     * Write location to container.
     *
     * @param dw writer
     */
    @Throws(IOException::class)
    private fun writeLocation(dw: DataWriterBigEndian, loc: Location?) {
        if (loc == null) {
            dw.writeBoolean(false)
        } else {
            dw.writeBoolean(true)
            dw.writeStorable(loc)
        }
    }

    companion object {

        // CONSTANTS

        const val GUIDE_TYPE_DISABLED = -1
        const val GUIDE_TYPE_WAYPOINT = 1
        const val GUIDE_TYPE_TRACK_GUIDE = 2
        const val GUIDE_TYPE_TRACK_NAVIGATION = 3
    }
}
