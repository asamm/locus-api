package locus.api.android.features.periodicUpdates

import android.content.Intent

import java.io.IOException

import locus.api.android.utils.IntentHelper
import locus.api.objects.enums.PointRteAction
import locus.api.objects.extra.TrackStats

/**
 * Deprecated helper method used by no longer supported [PeriodicUpdatesHandler].
 */
@Deprecated (message = "Use directly `ActionBasics.getUpdateContainer()")
object PeriodicUpdatesFiller {

    /**
     * Function that handle received intent and generate UpdateContainer object with data.
     *
     * @param i  received intent
     * @param pu current instance of update handler
     * @return container with data
     */
    fun intentToUpdate(i: Intent, pu: PeriodicUpdatesHandler): UpdateContainer {
        // prepare data container
        val update = UpdateContainer()

        // LOCATION, GPS, BASIC VALUES

        // check current GPS/network location
        update.isEnabledMyLocation = i.getBooleanExtra(
                PeriodicUpdatesConst.VAR_B_MY_LOCATION_ON, false)
        update.isNewMyLocation = false
        update.locMyLocation = IntentHelper.getLocationFromIntent(
                i, PeriodicUpdatesConst.VAR_LOC_MY_LOCATION)
                ?: update.locMyLocation
        if (update.isEnabledMyLocation) {
            // check if location is updated
            if (pu.mLastGps == null || pu.mLastGps.distanceTo(
                            update.locMyLocation) > pu.mLocMinDistance) {
                pu.mLastGps = update.locMyLocation
                update.isNewMyLocation = true
            }
        }

        // get basic variables
        update.gpsSatsUsed = i.getIntExtra(
                PeriodicUpdatesConst.VAR_I_GPS_SATS_USED, 0)
        update.gpsSatsAll = i.getIntExtra(
                PeriodicUpdatesConst.VAR_I_GPS_SATS_ALL, 0)
        update.declination = i.getFloatExtra(
                PeriodicUpdatesConst.VAR_F_DECLINATION, 0.0f)
        update.speedVertical = i.getFloatExtra(
                PeriodicUpdatesConst.VAR_F_SPEED_VERTICAL, 0.0f)
        update.slope = i.getFloatExtra(
                PeriodicUpdatesConst.VAR_F_SLOPE, 0.0f)

        update.orientGpsAngle = i.getFloatExtra(
                PeriodicUpdatesConst.VAR_F_ORIENT_GPS_ANGLE, 0.0f)

        update.orientHeading = i.getFloatExtra(
                PeriodicUpdatesConst.VAR_F_ORIENT_HEADING, 0.0f)
        update.orientHeadingOpposit = i.getFloatExtra(
                PeriodicUpdatesConst.VAR_F_ORIENT_HEADING_OPPOSIT, 0.0f)
        update.orientCourse = i.getFloatExtra(
                PeriodicUpdatesConst.VAR_F_ORIENT_COURSE, 0.0f)

        update.orientPitch = i.getFloatExtra(
                PeriodicUpdatesConst.VAR_F_ORIENT_PITCH, 0.0f)
        update.orientRoll = i.getFloatExtra(
                PeriodicUpdatesConst.VAR_F_ORIENT_ROLL, 0.0f)


        // MAP STUFF

        update.isMapVisible = i.getBooleanExtra(
                PeriodicUpdatesConst.VAR_B_MAP_VISIBLE, false)
        update.isNewMapCenter = false
        update.locMapCenter = IntentHelper.getLocationFromIntent(
                i, PeriodicUpdatesConst.VAR_LOC_MAP_CENTER)
        if (pu.mLastMapCenter == null || pu.mLastMapCenter.distanceTo(
                        update.locMapCenter) > pu.mLocMinDistance) {
            pu.mLastMapCenter = update.locMapCenter
            update.isNewMapCenter = true
        }

        // check MAP
        update.mapTopLeft = IntentHelper.getLocationFromIntent(
                i, PeriodicUpdatesConst.VAR_LOC_MAP_BBOX_TOP_LEFT)
        update.mapBottomRight = IntentHelper.getLocationFromIntent(
                i, PeriodicUpdatesConst.VAR_LOC_MAP_BBOX_BOTTOM_RIGHT)
        update.mapZoomLevel = i.getIntExtra(
                PeriodicUpdatesConst.VAR_I_MAP_ZOOM_LEVEL, 0)
        update.isNewZoomLevel = update.mapZoomLevel != pu.mLastZoomLevel
        pu.mLastZoomLevel = update.mapZoomLevel
        update.mapRotate = i.getFloatExtra(
                PeriodicUpdatesConst.VAR_F_MAP_ROTATE, 0.0f)
        update.isUserTouching = i.getBooleanExtra(
                PeriodicUpdatesConst.VAR_B_MAP_USER_TOUCHES, false)

        // TRACK RECORDING PART

        update.isTrackRecRecording = i.getBooleanExtra(
                PeriodicUpdatesConst.VAR_B_REC_RECORDING, false)
        if (update.isTrackRecRecording) {
            update.isTrackRecPaused = i.getBooleanExtra(
                    PeriodicUpdatesConst.VAR_B_REC_PAUSED, false)
            update.trackRecProfileName = i.getStringExtra(
                    PeriodicUpdatesConst.VAR_S_REC_PROFILE_NAME)
            // read track statistics
            try {
                val data = i.getByteArrayExtra(PeriodicUpdatesConst.VAR_L_REC_TRACK_STATS)
                if (data != null && data.isNotEmpty()) {
                    update.trackRecStats = TrackStats()
                    update.trackRecStats!!.read(data)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            // load old values for compatibility reasons with old Locus versions,
            // that do not provide "TrackStats" directly
            if (update.trackRecStats == null) {
                update.trackRecStats = TrackStats()
                update.trackRecStats!!.totalLength = i.getDoubleExtra(
                        PeriodicUpdatesConst.VAR_D_REC_DIST, 0.0).toFloat()
                update.trackRecStats!!.eleNegativeDistance = i.getDoubleExtra(
                        PeriodicUpdatesConst.VAR_D_REC_DIST_DOWNHILL, 0.0).toFloat()
                update.trackRecStats!!.elePositiveDistance = i.getDoubleExtra(
                        PeriodicUpdatesConst.VAR_D_REC_DIST_UPHILL, 0.0).toFloat()
                update.trackRecStats!!.altitudeMin = i.getFloatExtra(
                        PeriodicUpdatesConst.VAR_F_REC_ALT_MIN, 0.0f)
                update.trackRecStats!!.altitudeMax = i.getFloatExtra(
                        PeriodicUpdatesConst.VAR_F_REC_ALT_MAX, 0.0f)
                update.trackRecStats!!.eleNegativeHeight = i.getFloatExtra(
                        PeriodicUpdatesConst.VAR_F_REC_ALT_DOWNHILL, 0.0f)
                update.trackRecStats!!.elePositiveHeight = i.getFloatExtra(
                        PeriodicUpdatesConst.VAR_F_REC_ALT_UPHILL, 0.0f)
                update.trackRecStats!!.eleTotalAbsHeight = i.getFloatExtra(
                        PeriodicUpdatesConst.VAR_F_REC_ALT_CUMULATIVE, 0.0f)
                update.trackRecStats!!.totalTime = i.getLongExtra(
                        PeriodicUpdatesConst.VAR_L_REC_TIME, 0L)
                update.trackRecStats!!.totalTimeMove = i.getLongExtra(
                        PeriodicUpdatesConst.VAR_L_REC_TIME_MOVE, 0L)
                update.trackRecStats!!.speedMax = i.getFloatExtra(
                        PeriodicUpdatesConst.VAR_F_REC_SPEED_MAX, 0.0f)
                update.trackRecStats!!.numOfPoints = i.getIntExtra(
                        PeriodicUpdatesConst.VAR_I_REC_POINTS, 0)
            }
        }

        // GUIDING PART

        update.guideType = i.getIntExtra(
                PeriodicUpdatesConst.VAR_I_GUIDE_TYPE, UpdateContainer.GUIDE_TYPE_DISABLED)

        // load guiding if not disabled
        if (update.guideType != UpdateContainer.GUIDE_TYPE_DISABLED) {
            update.guideWptName = i.getStringExtra(
                    PeriodicUpdatesConst.VAR_S_GUIDE_WPT_NAME)
            update.guideWptLoc = IntentHelper.getLocationFromIntent(
                    i, PeriodicUpdatesConst.VAR_LOC_GUIDE_WPT)
            update.guideWptDist = i.getDoubleExtra(
                    PeriodicUpdatesConst.VAR_D_GUIDE_WPT_DIST, 0.0)
            update.guideWptAzim = i.getFloatExtra(
                    PeriodicUpdatesConst.VAR_F_GUIDE_WPT_AZIM, 0.0f)
            update.guideWptAngle = i.getFloatExtra(
                    PeriodicUpdatesConst.VAR_F_GUIDE_WPT_ANGLE, 0.0f)
            update.guideWptTime = i.getLongExtra(
                    PeriodicUpdatesConst.VAR_L_GUIDE_WPT_TIME, 0L)

            // TRACK PART

            update.guideDistFromStart = i.getDoubleExtra(
                    PeriodicUpdatesConst.VAR_D_GUIDE_DIST_FROM_START, 0.0)
            update.guideDistToFinish = i.getDoubleExtra(
                    PeriodicUpdatesConst.VAR_D_GUIDE_DIST_TO_FINISH, 0.0)
            update.guideTimeToFinish = i.getLongExtra(
                    PeriodicUpdatesConst.VAR_L_GUIDE_TIME_TO_FINISH, 0L)
            update.guideValid = i.getBooleanExtra(
                    PeriodicUpdatesConst.VAR_L_GUIDE_VALID, true)

            // get first navigation point
            update.guideNavPoint1Loc = IntentHelper.getLocationFromIntent(
                    i, PeriodicUpdatesConst.VAR_LOC_GUIDE_NAV_POINT1_LOC)
            if (update.guideNavPoint1Loc != null) {
                update.guideNavPoint1Name = i.getStringExtra(
                        PeriodicUpdatesConst.VAR_S_GUIDE_NAV_POINT1_NAME)
                update.guideNavPoint1Dist = i.getDoubleExtra(
                        PeriodicUpdatesConst.VAR_D_GUIDE_NAV_POINT1_DIST, 0.0)
                update.guideNavPoint1Time = i.getLongExtra(
                        PeriodicUpdatesConst.VAR_L_GUIDE_NAV_POINT1_TIME, 0L)
                val action1 = i.getIntExtra(
                        PeriodicUpdatesConst.VAR_L_GUIDE_NAV_POINT1_ACTION,
                        PointRteAction.UNDEFINED.id)
                update.guideNavPoint1Action = PointRteAction.getActionById(action1)
            }

            // get second navigation point
            update.guideNavPoint2Loc = IntentHelper.getLocationFromIntent(
                    i, PeriodicUpdatesConst.VAR_LOC_GUIDE_NAV_POINT2_LOC)
            if (update.guideNavPoint2Loc != null) {
                update.guideNavPoint2Name = i.getStringExtra(
                        PeriodicUpdatesConst.VAR_S_GUIDE_NAV_POINT2_NAME)
                update.guideNavPoint2Dist = i.getDoubleExtra(
                        PeriodicUpdatesConst.VAR_D_GUIDE_NAV_POINT2_DIST, 0.0)
                update.guideNavPoint2Time = i.getLongExtra(
                        PeriodicUpdatesConst.VAR_L_GUIDE_NAV_POINT2_TIME, 0L)
                val action2 = i.getIntExtra(
                        PeriodicUpdatesConst.VAR_L_GUIDE_NAV_POINT2_ACTION,
                        PointRteAction.UNDEFINED.id)
                update.guideNavPoint2Action = PointRteAction.getActionById(action2)
            }
        }

        // VARIOUS

        update.activeDashboardId = i.getStringExtra(
                PeriodicUpdatesConst.VAR_S_ACTIVE_DASHBOARD_ID)
        update.activeLiveTrackId = i.getStringExtra(
                PeriodicUpdatesConst.VAR_S_ACTIVE_LIVE_TRACK_ID)
        update.deviceBatteryValue = i.getIntExtra(
                PeriodicUpdatesConst.VAR_I_DEVICE_BATTERY_VALUE, 0)
        update.deviceBatteryTemperature = i.getFloatExtra(
                PeriodicUpdatesConst.VAR_F_DEVICE_BATTERY_TEMPERATURE, 0.0f)

        // return filled result
        return update
    }

    //*************************************************/
    // INSERT DATA TO INTENT (LOCUS TASK)
    //*************************************************/

    /**
     * Insert data from handler into existing intent.
     *
     * @param action action of intent
     * @param cont   update container with parameters
     * @return generated intent
     */
    fun updateToIntent(action: String, cont: UpdateContainer): Intent {
        val i = Intent(action)

        // main statistics and angles
        addValuesBasicLocation(cont, i)

        // map values
        addValuesMap(cont, i)

        // add track recording feature
        addValuesTrackRecording(cont, i)

        // guiding stuff
        addValuesGuiding(cont, i)

        // other various variables
        addValuesVarious(cont, i)

        // return result
        return i
    }

    private fun addValuesBasicLocation(cont: UpdateContainer, i: Intent) {
        // is GPS enabled or not
        i.putExtra(PeriodicUpdatesConst.VAR_B_MY_LOCATION_ON,
                cont.isEnabledMyLocation)

        // add current user location
        i.putExtra(PeriodicUpdatesConst.VAR_LOC_MY_LOCATION,
                cont.locMyLocation!!.asBytes)

        // add basic variables
        i.putExtra(PeriodicUpdatesConst.VAR_I_GPS_SATS_USED,
                cont.gpsSatsUsed)
        i.putExtra(PeriodicUpdatesConst.VAR_I_GPS_SATS_ALL,
                cont.gpsSatsAll)
        i.putExtra(PeriodicUpdatesConst.VAR_F_DECLINATION,
                cont.declination)
        i.putExtra(PeriodicUpdatesConst.VAR_F_SPEED_VERTICAL,
                cont.speedVertical)
        i.putExtra(PeriodicUpdatesConst.VAR_F_SLOPE,
                cont.slope)

        i.putExtra(PeriodicUpdatesConst.VAR_F_ORIENT_GPS_ANGLE,
                cont.orientGpsAngle)

        i.putExtra(PeriodicUpdatesConst.VAR_F_ORIENT_HEADING,
                cont.orientHeading)
        i.putExtra(PeriodicUpdatesConst.VAR_F_ORIENT_HEADING_OPPOSIT,
                cont.orientHeadingOpposit)
        i.putExtra(PeriodicUpdatesConst.VAR_F_ORIENT_COURSE,
                cont.orientCourse)

        i.putExtra(PeriodicUpdatesConst.VAR_F_ORIENT_PITCH,
                cont.orientPitch)
        i.putExtra(PeriodicUpdatesConst.VAR_F_ORIENT_ROLL,
                cont.orientRoll)
    }

    private fun addValuesMap(cont: UpdateContainer, i: Intent) {
        i.putExtra(PeriodicUpdatesConst.VAR_B_MAP_VISIBLE,
                cont.isMapVisible)
        i.putExtra(PeriodicUpdatesConst.VAR_LOC_MAP_CENTER,
                cont.locMapCenter!!.asBytes)

        i.putExtra(PeriodicUpdatesConst.VAR_LOC_MAP_BBOX_TOP_LEFT,
                cont.mapTopLeft!!.asBytes)
        i.putExtra(PeriodicUpdatesConst.VAR_LOC_MAP_BBOX_BOTTOM_RIGHT,
                cont.mapBottomRight!!.asBytes)
        i.putExtra(PeriodicUpdatesConst.VAR_I_MAP_ZOOM_LEVEL,
                cont.mapZoomLevel)
        i.putExtra(PeriodicUpdatesConst.VAR_F_MAP_ROTATE,
                cont.mapRotate)
        i.putExtra(PeriodicUpdatesConst.VAR_B_MAP_USER_TOUCHES,
                cont.isUserTouching)
    }

    private fun addValuesTrackRecording(cont: UpdateContainer, i: Intent) {
        i.putExtra(PeriodicUpdatesConst.VAR_B_REC_RECORDING,
                cont.isTrackRecRecording)
        i.putExtra(PeriodicUpdatesConst.VAR_B_REC_PAUSED,
                cont.isTrackRecPaused)

        // prepare some variables
        if (cont.isTrackRecRecording) {
            i.putExtra(PeriodicUpdatesConst.VAR_S_REC_PROFILE_NAME,
                    cont.trackRecProfileName)
            i.putExtra(PeriodicUpdatesConst.VAR_L_REC_TRACK_STATS,
                    cont.trackRecStats!!.asBytes)

            // because of compatibility with older add-ons, store also stats in base values
            i.putExtra(PeriodicUpdatesConst.VAR_D_REC_DIST,
                    cont.trackRecStats!!.totalLength.toDouble())
            i.putExtra(PeriodicUpdatesConst.VAR_D_REC_DIST_DOWNHILL,
                    cont.trackRecStats!!.eleNegativeDistance.toDouble())
            i.putExtra(PeriodicUpdatesConst.VAR_D_REC_DIST_UPHILL,
                    cont.trackRecStats!!.elePositiveDistance.toDouble())
            i.putExtra(PeriodicUpdatesConst.VAR_F_REC_ALT_MIN,
                    cont.trackRecStats!!.altitudeMin)
            i.putExtra(PeriodicUpdatesConst.VAR_F_REC_ALT_MAX,
                    cont.trackRecStats!!.altitudeMax)
            i.putExtra(PeriodicUpdatesConst.VAR_F_REC_ALT_DOWNHILL,
                    cont.trackRecStats!!.eleNegativeHeight)
            i.putExtra(PeriodicUpdatesConst.VAR_F_REC_ALT_UPHILL,
                    cont.trackRecStats!!.elePositiveHeight)
            i.putExtra(PeriodicUpdatesConst.VAR_F_REC_ALT_CUMULATIVE,
                    cont.trackRecStats!!.eleTotalAbsHeight)
            i.putExtra(PeriodicUpdatesConst.VAR_L_REC_TIME,
                    cont.trackRecStats!!.totalTime)
            i.putExtra(PeriodicUpdatesConst.VAR_L_REC_TIME_MOVE,
                    cont.trackRecStats!!.totalTimeMove)
            i.putExtra(PeriodicUpdatesConst.VAR_F_REC_SPEED_AVG,
                    cont.trackRecStats!!.getSpeedAverage(false))
            i.putExtra(PeriodicUpdatesConst.VAR_F_REC_SPEED_AVG_MOVE,
                    cont.trackRecStats!!.getSpeedAverage(true))
            i.putExtra(PeriodicUpdatesConst.VAR_F_REC_SPEED_MAX,
                    cont.trackRecStats!!.speedMax)
            i.putExtra(PeriodicUpdatesConst.VAR_I_REC_POINTS,
                    cont.trackRecStats!!.numOfPoints)
        }
    }

    private fun addValuesGuiding(cont: UpdateContainer, i: Intent) {
        i.putExtra(PeriodicUpdatesConst.VAR_I_GUIDE_TYPE,
                cont.guideType)

        // continue only if guiding is enabled
        if (cont.guideType == UpdateContainer.GUIDE_TYPE_DISABLED) {
            return
        }

        // insert basic data
        i.putExtra(PeriodicUpdatesConst.VAR_S_GUIDE_WPT_NAME,
                cont.guideWptName)
        i.putExtra(PeriodicUpdatesConst.VAR_LOC_GUIDE_WPT,
                cont.guideWptLoc!!.asBytes)
        i.putExtra(PeriodicUpdatesConst.VAR_D_GUIDE_WPT_DIST,
                cont.guideWptDist)
        i.putExtra(PeriodicUpdatesConst.VAR_F_GUIDE_WPT_AZIM,
                cont.guideWptAzim)
        i.putExtra(PeriodicUpdatesConst.VAR_F_GUIDE_WPT_ANGLE,
                cont.guideWptAngle)
        i.putExtra(PeriodicUpdatesConst.VAR_L_GUIDE_WPT_TIME,
                cont.guideWptTime)

        // TRACK PART

        i.putExtra(PeriodicUpdatesConst.VAR_D_GUIDE_DIST_FROM_START,
                cont.guideDistFromStart)
        i.putExtra(PeriodicUpdatesConst.VAR_D_GUIDE_DIST_TO_FINISH,
                cont.guideDistToFinish)
        i.putExtra(PeriodicUpdatesConst.VAR_L_GUIDE_TIME_TO_FINISH,
                cont.guideTimeToFinish)
        i.putExtra(PeriodicUpdatesConst.VAR_L_GUIDE_VALID,
                cont.guideValid)

        // first navigation waypoint
        val guideTrack = cont.contentGuideTrack
        if (guideTrack?.navPointFirst != null) {
            i.putExtra(PeriodicUpdatesConst.VAR_LOC_GUIDE_NAV_POINT1_LOC,
                    guideTrack.navPointFirst.location.asBytes)
            i.putExtra(PeriodicUpdatesConst.VAR_S_GUIDE_NAV_POINT1_NAME,
                    guideTrack.navPointFirst.name)
            i.putExtra(PeriodicUpdatesConst.VAR_D_GUIDE_NAV_POINT1_DIST,
                    guideTrack.navPointFirst.distance)
            i.putExtra(PeriodicUpdatesConst.VAR_L_GUIDE_NAV_POINT1_TIME,
                    guideTrack.navPointFirst.time)
            i.putExtra(PeriodicUpdatesConst.VAR_L_GUIDE_NAV_POINT1_ACTION,
                    guideTrack.navPointFirst.action.id)
        }

        // second navigation waypoint
        if (guideTrack?.navPointSecond != null) {
            i.putExtra(PeriodicUpdatesConst.VAR_LOC_GUIDE_NAV_POINT2_LOC,
                    guideTrack.navPointSecond.location.asBytes)
            i.putExtra(PeriodicUpdatesConst.VAR_S_GUIDE_NAV_POINT2_NAME,
                    guideTrack.navPointSecond.name)
            i.putExtra(PeriodicUpdatesConst.VAR_D_GUIDE_NAV_POINT2_DIST,
                    guideTrack.navPointSecond.distance)
            i.putExtra(PeriodicUpdatesConst.VAR_L_GUIDE_NAV_POINT2_TIME,
                    guideTrack.navPointSecond.time)
            i.putExtra(PeriodicUpdatesConst.VAR_L_GUIDE_NAV_POINT2_ACTION,
                    guideTrack.navPointSecond.action.id)
        }
    }

    private fun addValuesVarious(cont: UpdateContainer, i: Intent) {
        i.putExtra(PeriodicUpdatesConst.VAR_S_ACTIVE_DASHBOARD_ID,
                cont.activeDashboardId)
        i.putExtra(PeriodicUpdatesConst.VAR_S_ACTIVE_LIVE_TRACK_ID,
                cont.activeLiveTrackId)
        i.putExtra(PeriodicUpdatesConst.VAR_I_DEVICE_BATTERY_VALUE,
                cont.deviceBatteryValue)
        i.putExtra(PeriodicUpdatesConst.VAR_F_DEVICE_BATTERY_TEMPERATURE,
                cont.deviceBatteryTemperature)
    }
}
