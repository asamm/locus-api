package locus.api.android.features.augmentedReality

import android.app.Activity
import android.content.Context
import android.content.Intent
import locus.api.android.ActionDisplayVarious
import locus.api.android.objects.PackPoints
import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils
import locus.api.objects.Storable
import locus.api.objects.extra.Location
import locus.api.objects.geoData.Track
import locus.api.utils.Logger
import java.security.NoSuchAlgorithmException

object UtilsAddonAR {

    // tag for logger
    private const val TAG = "UtilsAddonAR"

    // minimum required version of add-on
    const val REQUIRED_VERSION = 11

    // result id of selected point
    const val RESULT_WPT_ID = "RESULT_WPT_ID"

    // PRIVATE PARAMETERS

    // intent call to view AR
    private const val INTENT_VIEW = "locus.api.android.addon.ar.ACTION_VIEW"
    // broadcast intent
    private const val BROADCAST_DATA = "locus.api.android.addon.ar.NEW_DATA"
    // id used for recognizing if add-on is closed or not. Register receiver in your
    // starting activity on this number
    private const val REQUEST_ADDON_AR = 30001

    // extra identification for intent - location
    const val EXTRA_LOCATION = "EXTRA_LOCATION"
    // identification of actual guiding item
    const val EXTRA_GUIDING_ID = "EXTRA_GUIDING_ID"

    // last used location
    private var mLastLocation: Location? = null

    //*************************************************
    // STATIC HELPERS
    //*************************************************

    /**
     * Check if exist new version of AR add-on. Older version used
     * different and deprecated API, so not usable anymore.
     *
     * @return `true` if valid version is installed
     */
    fun isInstalled(context: Context): Boolean {
        return LocusUtils.isAppAvailable(context,
                "menion.android.locus.addon.ar", REQUIRED_VERSION)
    }

    /**
     * Show points in AR add-on application.
     *
     * @param act         current activity
     * @param data        packs of points to display
     * @param yourLoc     current users location
     * @param guidedWptId ID of point on which is currently active guidance
     * @return `true` if add-on was correctly called
     */
    fun showPoints(act: Activity, data: List<PackPoints>,
            yourLoc: Location, guidedWptId: Long): Boolean {
        if (!isInstalled(act)) {
            Logger.logW(TAG, "missing required version $REQUIRED_VERSION")
            return false
        }

        // prepare intent
        val intent = Intent(INTENT_VIEW)
        intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_DATA_ARRAY,
                Storable.getAsBytes(data))
        intent.putExtra(EXTRA_LOCATION,
                yourLoc.asBytes)
        intent.putExtra(EXTRA_GUIDING_ID, guidedWptId)

        // check intent firstly
        if (!ActionDisplayVarious.hasData(intent)) {
            Logger.logW(TAG, "Intent 'null' or not contain any data")
            return false
        }

        // store location
        mLastLocation = yourLoc

        // finally start activity
        act.startActivityForResult(intent, REQUEST_ADDON_AR)
        return true
    }

    /**
     * Update location in currently running instance.
     *
     * @param context current context
     * @param loc     new users location
     */
    fun updateLocation(context: Context, loc: Location) {
        // do some tests if is really need to send new location
        val timeDiff = loc.time - mLastLocation!!.time
        val distDiff = loc.distanceTo(mLastLocation!!).toDouble()
        val altDiff = Math.abs(loc.altitude - mLastLocation!!.altitude)
        if (timeDiff < 5000 || distDiff < 5 && altDiff < 10) {
            return
        }

        // store location and refresh data
        mLastLocation = loc
        val intent = Intent(BROADCAST_DATA)
        intent.putExtra(EXTRA_LOCATION, mLastLocation!!.asBytes)

        // TODO fix inexact broadcast intent
        context.sendBroadcast(intent)
    }

    /**
     * Display tracks in current instance.
     *
     * @param context current context
     * @param tracks  tracks to display
     */
    @Throws(NoSuchAlgorithmException::class)
    fun showTracks(context: Context, tracks: List<Track>) {
        throw NoSuchAlgorithmException("Not yet implemented")
    }
}
