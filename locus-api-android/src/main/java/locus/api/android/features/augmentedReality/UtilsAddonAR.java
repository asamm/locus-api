package locus.api.android.features.augmentedReality;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import locus.api.android.ActionDisplay;
import locus.api.android.objects.PackWaypoints;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils;
import locus.api.objects.Storable;
import locus.api.objects.extra.Location;
import locus.api.objects.extra.Track;
import locus.api.utils.Logger;

public class UtilsAddonAR {

    // tag for logger
	private static final String TAG = "UtilsAddonAR";

	// PUBLIC PARAMETERS

	// minimum required version of add-on
	public static final int REQUIRED_VERSION = 11;

	// result id of selected point
	public static final String RESULT_WPT_ID = "RESULT_WPT_ID";

	// PRIVATE PARAMETERS

	// intent call to view AR
	private static final String INTENT_VIEW = "locus.api.android.addon.ar.ACTION_VIEW";
	// broadcast intent
	private final static String BROADCAST_DATA = "locus.api.android.addon.ar.NEW_DATA";
	// id used for recognizing if add-on is closed or not. Register receiver in your 
	// starting activity on this number
	private static final int REQUEST_ADDON_AR = 30001;
	
	// extra ident for intent - location
	public static final String EXTRA_LOCATION = "EXTRA_LOCATION";
	// ident of actual guiding item
	public static final String EXTRA_GUIDING_ID = "EXTRA_GUIDING_ID";

	// last used location
	private static Location mLastLocation;

    /**************************************************/
    // STATIC HELPERS
    /**************************************************/

    /**
     * Check if exist new version of AR add-on. Older version used
     * different and deprecated API, so not usable anymore.
     * @return <code>true</code> if valid version is installed
     */
    public static boolean isInstalled(Context context) {
		return LocusUtils.isAppAvailable(context,
				"menion.android.locus.addon.ar", REQUIRED_VERSION);
	}

	/**
	 * Show points in AR add-on application.
	 * @param act current activity
	 * @param data packs of points to display
	 * @param yourLoc current users location
	 * @param guidedWptId ID of point on which is currently active guidance
	 * @return {@code true} if add-on was correctly called
	 */
	public static boolean showPoints(Activity act, List<PackWaypoints> data,
			Location yourLoc, long guidedWptId) {
		if (!isInstalled(act)) {
			Logger.logW(TAG, "missing required version " + REQUIRED_VERSION);
			return false;
		}
		
		// prepare intent
		Intent intent = new Intent(INTENT_VIEW);
		intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_DATA_ARRAY, 
				Storable.getAsBytes(data));
		intent.putExtra(EXTRA_LOCATION, 
				yourLoc.getAsBytes());
		intent.putExtra(EXTRA_GUIDING_ID, guidedWptId);
		
		// check intent firstly
		if (!ActionDisplay.hasData(intent)) {
            Logger.logW(TAG, "Intent 'null' or not contain any data");
			return false;
		}

		// store location
		mLastLocation = yourLoc;

		// finally start activity
		act.startActivityForResult(intent, REQUEST_ADDON_AR);
		return true;
	}

	/**
	 * Update location in currently running instance.
	 * @param context current context
	 * @param loc new users location
	 */
	public static void updateLocation(Context context, Location loc) {
        // do some tests if is really need to send new location
        long timeDiff = loc.getTime() - mLastLocation.getTime();
        double distDiff = loc.distanceTo(mLastLocation);
        double altDiff = Math.abs(loc.getAltitude() - mLastLocation.getAltitude());
        if (timeDiff < 5000 || (distDiff < 5 && altDiff < 10)) {
            return;
        }

        // store location and refresh data
        mLastLocation = loc;
        Intent intent = new Intent(BROADCAST_DATA);
        intent.putExtra(EXTRA_LOCATION, mLastLocation.getAsBytes());

		// TODO fix inexact broadcast intent
        context.sendBroadcast(intent);
	}

	/**
	 * Display tracks in current instance.
	 * @param context current context
	 * @param tracks tracks to display
	 * @throws NoSuchAlgorithmException
	 */
	public static void showTracks(Context context, List<Track> tracks)
			throws NoSuchAlgorithmException {
		throw new NoSuchAlgorithmException("Not yet implemented");
	}
}
