package locus.api.android;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusInfo;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.LocusUtils.LocusVersion;
import locus.api.android.utils.LocusUtils.VersionCode;
import locus.api.android.utils.Utils;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.Storable;
import locus.api.objects.extra.GeoDataExtra;
import locus.api.objects.extra.GeoDataStyle;
import locus.api.objects.extra.Location;
import locus.api.objects.extra.Track;
import locus.api.objects.extra.Waypoint;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;

public class ActionTools {

    // tag for logger
	private static final String TAG = "ActionTools";
	
	/**************************************************/
	// FILE PICKER
	/**************************************************/
	
	/**
	 * Allow to call activity for File pick. You can use Locus picker for this purpose, but
	 * check if Locus version 231 and above are installed <b>isLocusAvailable(context, 231)</b>!
	 * @param activity starting activity that also receive result
	 * @param requestCode request code
	 * @throws ActivityNotFoundException
	 */
	public static void actionPickFile(Activity activity, int requestCode) 
			throws ActivityNotFoundException {
		intentPick("org.openintents.action.PICK_FILE",
				activity, requestCode, null, null);
	}
	
	public static void actionPickFile(Activity activity, int requestCode, 
			String title, String[] filter) throws ActivityNotFoundException {
		intentPick("org.openintents.action.PICK_FILE",
				activity, requestCode, title, filter);
	}
	
	public static void actionPickDir(Activity activity, int requestCode) 
			throws ActivityNotFoundException {
		intentPick("org.openintents.action.PICK_DIRECTORY",
				activity, requestCode, null, null);
	}
	
	public static void actionPickDir(Activity activity, int requestCode,
			String title) throws ActivityNotFoundException {
		intentPick("org.openintents.action.PICK_DIRECTORY",
				activity, requestCode, title, null);
	}
	
	private static void intentPick(String action, Activity activity, int requestCode,
			String title, String[] filter) {
		// create intent
		Intent intent = new Intent(action);
		if (title != null && title.length() > 0) {
			intent.putExtra("org.openintents.extra.TITLE", title);
		}
		if (filter != null && filter.length > 0) {
			intent.putExtra("org.openintents.extra.FILTER", filter);
		}
		
		// execute request
		activity.startActivityForResult(intent, requestCode);
	}

    /**************************************************/
	// LOCATION
    /**************************************************/

    /**
     * Start basic "Pick location" event.
     * @param act current activity
     * @throws RequiredVersionMissingException
     */
	public static void actionPickLocation(Activity act) 
			throws RequiredVersionMissingException {
		if (LocusUtils.isLocusAvailable(act, 235, 235, 0)) {
			Intent intent = new Intent(LocusConst.ACTION_PICK_LOCATION);
			act.startActivity(intent);			
		} else {
			throw new RequiredVersionMissingException(235);
		}
	}

    /**************************************************/
	// NAVIGATION
    /**************************************************/

    /**
     * Intent that starts navigation in Locus app based on defined target.
     * @param act current activity
     * @param name name of target
     * @param latitude latitude of target
     * @param longitude longitude of target
     * @throws RequiredVersionMissingException
     */
	public static void actionStartNavigation(Activity act, 
			String name, double latitude, double longitude) 
			throws RequiredVersionMissingException {
        // check required version
        if (!LocusUtils.isLocusAvailable(act, VersionCode.UPDATE_01)) {
            throw new RequiredVersionMissingException(VersionCode.UPDATE_01);
        }

        // call Locus
    	Intent intent = new Intent(LocusConst.ACTION_NAVIGATION_START);
		if (name != null) {
            intent.putExtra(LocusConst.INTENT_EXTRA_NAME, name);
        }
		intent.putExtra(LocusConst.INTENT_EXTRA_LATITUDE, latitude);
		intent.putExtra(LocusConst.INTENT_EXTRA_LONGITUDE, longitude);
		act.startActivity(intent);
	}

    /**
     * Intent that starts navigation in Locus app based on defined target.
     * @param act current activity
     * @param wpt waypoint - destination
     * @throws RequiredVersionMissingException
     */
	public static void actionStartNavigation(Activity act, Waypoint wpt) 
			throws RequiredVersionMissingException {
        // check required version
        if (!LocusUtils.isLocusAvailable(act, VersionCode.UPDATE_01)) {
            throw new RequiredVersionMissingException(VersionCode.UPDATE_01);
        }

        // call Locus
		Intent intent = new Intent(LocusConst.ACTION_NAVIGATION_START);
		LocusUtils.addWaypointToIntent(intent, wpt);
		act.startActivity(intent);
	}

    /**
     * Intent that starts navigation in Locus to target address.
     * @param act current activity
     * @param address target address
     * @throws RequiredVersionMissingException
     */
    public static void actionStartNavigation(Activity act, String address)
            throws RequiredVersionMissingException {
        // check required version
        if (!LocusUtils.isLocusAvailable(act, VersionCode.UPDATE_08)) {
            throw new RequiredVersionMissingException(VersionCode.UPDATE_08);
        }

        // call Locus
        Intent intent = new Intent(LocusConst.ACTION_NAVIGATION_START);
        intent.putExtra(LocusConst.INTENT_EXTRA_ADDRESS_TEXT, address);
        act.startActivity(intent);
    }

    /**************************************************/
	// GUIDING
    /**************************************************/
	
	public static void actionStartGuiding(Activity act, 
			String name, double latitude, double longitude) 
			throws RequiredVersionMissingException {
		if (LocusUtils.isLocusAvailable(act, 243, 243, 0)) {
			Intent intent = new Intent(LocusConst.ACTION_GUIDING_START);
			if (name != null) {
				intent.putExtra(LocusConst.INTENT_EXTRA_NAME, name);
			}
			intent.putExtra(LocusConst.INTENT_EXTRA_LATITUDE, latitude);
			intent.putExtra(LocusConst.INTENT_EXTRA_LONGITUDE, longitude);
			act.startActivity(intent);			
		} else {
			throw new RequiredVersionMissingException(243);
		}
	}
	
	public static void actionStartGuiding(Activity act, Waypoint wpt) 
			throws RequiredVersionMissingException {
		if (LocusUtils.isLocusAvailable(act, 243, 243, 0)) {
			Intent intent = new Intent(LocusConst.ACTION_GUIDING_START);
			LocusUtils.addWaypointToIntent(intent, wpt);
			act.startActivity(intent);
		} else {
			throw new RequiredVersionMissingException(243);
		}
	}
	
	/**************************************************/
	// WAYPOINTS HANDLING
	/**************************************************/
	
	/**
	 * Get full waypoint from Locus database with all possible information, like 
	 * {@link GeoDataExtra} object, {@link locus.api.objects.extra.Location}
	 *  or {@link GeoDataStyle} and others
	 * @param ctx current context
	 * @param wptId unique ID of waypoint in Locus database
	 * @return {@link locus.api.objects.extra.Waypoint} or {@code null} in case of problem
	 * @throws RequiredVersionMissingException if Locus in required version is missing
	 */
	public static Waypoint getLocusWaypoint(Context ctx, LocusVersion lv, long wptId) 
			throws RequiredVersionMissingException {
		// check version
		int minVersion = VersionCode.UPDATE_01.vcFree;
		if (!LocusUtils.isLocusFreePro(lv, minVersion)) {
			throw new RequiredVersionMissingException(minVersion);
		}
		
		// generate cursor
		Cursor cursor;
		Uri scheme = getContentProviderData(lv, VersionCode.UPDATE_01,
                LocusConst.CONTENT_PROVIDER_PATH_WAYPOINT);
		if (scheme != null) {
            scheme = ContentUris.withAppendedId(scheme, wptId);
			cursor = ctx.getContentResolver().query(scheme,
					null, null, null, null);
		} else {
			throw new RequiredVersionMissingException(minVersion);
		}
		
		// check cursor
		if (cursor == null || !cursor.moveToFirst()) {
			Logger.logW(TAG, "getLocusWaypoint(" + ctx + ", " + wptId + "), " +
					"'cursor' in not valid");
			return null;
		}
		
		// handle result
		try {
			return new Waypoint(cursor.getBlob(1));
		} catch (Exception e) {
			Logger.logE(TAG, "getLocusWaypoint(" + ctx + ", " + wptId + ")", e);
		} finally {
            Utils.closeQuietly(cursor);
		}
		return null;
	}
	
	/**
	 * Get ID of waypoint stored in Locus internal database. To search for waypoint ID
	 * is used it's name. Because search is executed on SQLite database, it is possible
	 * to also use wildcards.
	 * <br><br>
	 * Examples: 
	 * <br><br>
	 * 1. search for point that has exact name "Cinema", just write "Cinema" as wptName
	 * <br>
	 * 2. search for point that starts with "Cinema", just write "Cinema%" as wptName
	 * <br>
	 * 3. search for point that contains word "cinema", just write "%cinema%" as wptName
	 * @param ctx current context
	 * @param wptName name (or part of name) you search
	 * @return array of waypoint ids. Returns <code>null</code> in case, any problem happen, or
	 * empty array if no result was found 
	 * @throws RequiredVersionMissingException if Locus in required version is missing
	 */
	public static long[] getLocusWaypointId(Context ctx, LocusVersion lv, String wptName)
			throws RequiredVersionMissingException {
		// check version (available only in Free/Pro)
		int minVersion = VersionCode.UPDATE_03.vcFree;
		if (!LocusUtils.isLocusFreePro(lv, minVersion)) {
			throw new RequiredVersionMissingException(minVersion);
		}
		
		// generate cursor
		Cursor cursor;
        Uri scheme = getContentProviderData(lv, VersionCode.UPDATE_03,
                LocusConst.CONTENT_PROVIDER_PATH_WAYPOINT);
		if (scheme != null) {
			cursor = ctx.getContentResolver().query(scheme,
					null, "getWaypointId", new String[] {wptName}, null);
		} else {
			throw new RequiredVersionMissingException(minVersion);
		}
		
		// handle result
		long[] result = null;
		try {
			result = new long[cursor.getCount()];
			for (int i = 0, m = result.length; i < m; i++) {
				cursor.moveToPosition(i);
				result[i] = cursor.getLong(0);
			}
		} catch (Exception e) {
			Logger.logE(TAG, "getLocusWaypointId(" + ctx + ", " + wptName + ")", e);
		} finally {
            Utils.closeQuietly(cursor);
		}
		return result;
	}

	/**
	 * Update waypoint in Locus. More in extended
	 * {@link #updateLocusWaypoint(Context, LocusVersion, Waypoint, boolean, boolean)} function.
	 */
	public static int updateLocusWaypoint(Context context, LocusVersion lv,
			Waypoint wpt, boolean forceOverwrite) 
			throws RequiredVersionMissingException {
		return updateLocusWaypoint(context, lv, wpt, forceOverwrite, false);
	}

	/**
	 * Update waypoint in Locus
	 * @param ctx current context
	 * @param wpt waypoint to update. Do not modify waypoint's ID value, because it's key to update
	 * @param forceOverwrite if set to <code>true</code>, new waypoint will completely rewrite all
	 *  user's data (do not use if necessary). If set to <code>false</code>, Locus will handle update based on user's 
	 *  settings (if user have defined "keep values", it will keep it)
	 * @param loadAllGcWaypoints allow to force Locus to load all GeocacheWaypoints (of course
	 * if point is Geocache and is visible on map)
	 * @return number of affected waypoints
	 * @throws RequiredVersionMissingException if Locus in required version is missing
	 */
	public static int updateLocusWaypoint(Context ctx, LocusVersion lv,
			Waypoint wpt, boolean forceOverwrite, boolean loadAllGcWaypoints) 
			throws RequiredVersionMissingException {
		// check version (available only in Free/Pro)
		int minVersion = VersionCode.UPDATE_01.vcFree;
		if (!LocusUtils.isLocusFreePro(lv, minVersion)) {
			throw new RequiredVersionMissingException(minVersion);
		}
		
		// generate cursor
        Uri scheme = getContentProviderData(lv, VersionCode.UPDATE_01,
                LocusConst.CONTENT_PROVIDER_PATH_WAYPOINT);
		if (scheme != null) {
			// define empty cursor
			ContentValues cv = new ContentValues();
			cv.put("waypoint", wpt.getAsBytes());
			cv.put("forceOverwrite", forceOverwrite);
			cv.put("loadAllGcWaypoints", loadAllGcWaypoints);
			return ctx.getContentResolver().update(scheme, cv, null, null);
		} else {
			throw new RequiredVersionMissingException(minVersion);
		}
	}

	/**
	 * Allows to display whole detail screen of certain waypoint.
	 * @param ctx current context
	 * @param lv LocusVersion we call
	 * @param wptId ID of waypoints we wants to display
	 * @throws RequiredVersionMissingException if Locus in required version is missing
	 */
	public static void displayWaypointScreen(Context ctx, LocusVersion lv, long wptId)
			throws RequiredVersionMissingException {
        displayWaypointScreen(ctx, lv, wptId, "");
	}

    /**
     * Allows to display whole detail screen of certain waypoint.
     * @param ctx current context
     * @param lv LocusVersion we call
     * @param wptId ID of waypoints we wants to display
     * @param packageName this value is used for creating intent that
     *  will be called in callback (for example com.super.application)
     * @param className the name of the class inside of com.super.application
     *  that implements the component (for example com.super.application.Main)
     * @param returnDataName String under which data will be stored. Can be
     *  retrieved by String data = getIntent.getStringExtra("returnData");
     * @param returnDataValue String under which data will be stored. Can be
     *  retrieved by String data = getIntent.getStringExtra("returnData");
     * @throws RequiredVersionMissingException
     */
    public static void displayWaypointScreen(Context ctx, LocusVersion lv, long wptId,
            String packageName, String className, String returnDataName, String returnDataValue)
            throws RequiredVersionMissingException {
        // prepare callback
        String callback = GeoDataExtra.generateCallbackString(
                "", packageName, className, returnDataName, returnDataValue);

        // call intent
        displayWaypointScreen(ctx, lv, wptId, callback);
    }

    /**
     * Allows to display whole detail screen of certain waypoint.
     * @param ctx current context
     * @param lv LocusVersion we call
     * @param wptId ID of waypoints we wants to display
     * @param callback generated callback (optional)
     * @throws RequiredVersionMissingException
     */
    private static void displayWaypointScreen(Context ctx, LocusVersion lv, long wptId, String callback)
            throws RequiredVersionMissingException {
        // check version (available only in Free/Pro)
        if (!LocusUtils.isLocusFreePro(lv, VersionCode.UPDATE_07.vcFree)) {
            throw new RequiredVersionMissingException(VersionCode.UPDATE_07);
        }

        // call intent
        Intent intent = new Intent(LocusConst.ACTION_DISPLAY_POINT_SCREEN);
        intent.putExtra(LocusConst.INTENT_EXTRA_ITEM_ID, wptId);
        if (callback != null && callback.length() > 0) {
            intent.putExtra(Waypoint.TAG_EXTRA_CALLBACK, callback);
        }
        ctx.startActivity(intent);
    }

    /**************************************************/
    // TRACKS HANDLING
    /**************************************************/

    /**
     * Get full track from Locus database with all possible information, like
     * {@link GeoDataExtra} object
     *  or {@link GeoDataStyle} and others
     * @param ctx current context
     * @param trackId unique ID of track in Locus database
     * @return {@link locus.api.objects.extra.Track} or <i>null</i> in case of problem
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    public static Track getLocusTrack(Context ctx, LocusVersion lv, long trackId)
            throws RequiredVersionMissingException {
        // check version
        int minVersion = VersionCode.UPDATE_10.vcFree;
        if (!LocusUtils.isLocusFreePro(lv, minVersion)) {
            throw new RequiredVersionMissingException(minVersion);
        }

        // generate cursor
        Cursor cursor;
        Uri scheme = getContentProviderData(lv, VersionCode.UPDATE_10,
                LocusConst.CONTENT_PROVIDER_PATH_TRACK);
        if (scheme != null) {
            scheme = ContentUris.withAppendedId(scheme, trackId);
            cursor = ctx.getContentResolver().query(scheme,
                    null, null, null, null);
        } else {
            throw new RequiredVersionMissingException(minVersion);
        }

        // check cursor
        if (cursor == null || !cursor.moveToFirst()) {
            Logger.logW(TAG, "getLocusTrack(" + ctx + ", " + trackId + "), " +
                    "'cursor' in not valid");
            return null;
        }

        // handle result
        try {
            return new Track(cursor.getBlob(1));
        } catch (Exception e) {
            Logger.logE(TAG, "getLocusTrack(" + ctx + ", " + trackId + ")", e);
        } finally {
            Utils.closeQuietly(cursor);
        }
        return null;
    }

    /**************************************************/
    // TRACK RECORDING
    /**************************************************/

    // Broadcast receivers do now show app chooser, so it's needed to give
    // them correct name of application package. For this reason, is required
    // LocusVersion object that specify which app will receive it's request

    /**
     * Main call to start track recording over API.
     * @param ctx current context
     * @param lv version of Locus used for track record
     * @throws RequiredVersionMissingException
     */
    public static void actionTrackRecordStart(Context ctx, LocusVersion lv)
            throws RequiredVersionMissingException {
        actionTrackRecordStart(ctx, lv, null);
    }

    /**
     * Main call to start track recording over API.
     * @param ctx current context
     * @param lv version of Locus used for track record
     * @param profileName name of profile used for record (optional), otherwise last
     *                    used will be used for recording
     * @throws RequiredVersionMissingException
     */
    public static void actionTrackRecordStart(Context ctx, LocusVersion lv, String profileName)
            throws RequiredVersionMissingException {
        // create basic intent
        Intent intent = actionTrackRecord(
                LocusConst.ACTION_TRACK_RECORD_START, lv);

        // set (optional) recording profile
        if (profileName != null && profileName.length() > 0) {
            intent.putExtra(LocusConst.INTENT_EXTRA_TRACK_REC_PROFILE, profileName);
        }

        // finally execute intent
        ctx.sendBroadcast(intent);
    }

    public static void actionTrackRecordPause(Context ctx, LocusVersion lv)
            throws RequiredVersionMissingException {
        ctx.sendBroadcast(actionTrackRecord(
                LocusConst.ACTION_TRACK_RECORD_PAUSE, lv));
    }

    public static void actionTrackRecordStop(Context ctx, LocusVersion lv, boolean autoSave)
            throws RequiredVersionMissingException {
        Intent intent = actionTrackRecord(
                LocusConst.ACTION_TRACK_RECORD_STOP, lv);
        intent.putExtra(LocusConst.INTENT_EXTRA_TRACK_REC_AUTO_SAVE, autoSave);
        ctx.sendBroadcast(intent);
    }

    // ADD WAYPOINT

    /**
     * Send broadcast to Locus to add a new waypoint to current track record.
     * @param ctx current context
     * @param lv version of Locus used for track record
     * @throws RequiredVersionMissingException
     */
    public static void actionTrackRecordAddWpt(Context ctx, LocusVersion lv)
            throws RequiredVersionMissingException {
        actionTrackRecordAddWpt(ctx, lv, false);
    }

    /**
     * Send broadcast to Locus to add a new waypoint to current track record.
     * @param ctx current context
     * @param lv version of Locus used for track record
     * @param autoSave <code>true</code> to automatically save waypoint without dialog
     * @throws RequiredVersionMissingException
     */
    public static void actionTrackRecordAddWpt(Context ctx, LocusVersion lv, boolean autoSave)
            throws RequiredVersionMissingException {
        actionTrackRecordAddWpt(ctx, lv, null, autoSave);
    }

    /**
     * Send broadcast to Locus to add a new waypoint to current track record.
     * @param ctx current context
     * @param lv version of Locus used for track record
     * @param wptName optional waypoint name
     * @param autoSave <code>true</code> to automatically save waypoint without dialog
     * @throws RequiredVersionMissingException
     */
    public static void actionTrackRecordAddWpt(Context ctx, LocusVersion lv,
            String wptName, boolean autoSave) throws RequiredVersionMissingException {
        Intent intent = actionTrackRecord(
                LocusConst.ACTION_TRACK_RECORD_ADD_WPT, lv);
        if (wptName != null && wptName.length() > 0) {
            intent.putExtra(LocusConst.INTENT_EXTRA_NAME, wptName);
        }
        intent.putExtra(LocusConst.INTENT_EXTRA_TRACK_REC_AUTO_SAVE, autoSave);
        ctx.sendBroadcast(intent);
    }

    /**
     * Send broadcast to Locus to add a new waypoint to current track record.
     * @param ctx current context
     * @param lv version of Locus used for track record
     * @param wptName nameof waypoint
     * @param actionAfter action that may happen after (defined in LocusConst class)
     * @throws RequiredVersionMissingException
     */
    public static void actionTrackRecordAddWpt(Context ctx, LocusVersion lv,
            String wptName, String actionAfter) throws RequiredVersionMissingException {
        // generate basic intent
        Intent intent = actionTrackRecord(
                LocusConst.ACTION_TRACK_RECORD_ADD_WPT, lv);
        if (wptName != null && wptName.length() > 0) {
            intent.putExtra(LocusConst.INTENT_EXTRA_NAME, wptName);
        }

        // autosave is always disabled
        intent.putExtra(LocusConst.INTENT_EXTRA_TRACK_REC_AUTO_SAVE, false);

        // extra parameter
        intent.putExtra(LocusConst.INTENT_EXTRA_TRACK_REC_ACTION_AFTER, actionAfter);
        ctx.sendBroadcast(intent);
    }

    /**
     * Private function that helps create basic intent that controls Locus.
     * @param action action that should be performed
     * @param lv version of Locus used for track record
     * @return created ready-to-use intent
     * @throws RequiredVersionMissingException
     */
    private static Intent actionTrackRecord(String action, LocusVersion lv)
            throws RequiredVersionMissingException {
        // check version (available only in Free/Pro)
        int minVersion = VersionCode.UPDATE_02.vcFree;
        if (!LocusUtils.isLocusFreePro(lv, minVersion)) {
            throw new RequiredVersionMissingException(minVersion);
        }

        // generate and return intent
        Intent intent = new Intent(action);
        intent.setPackage(lv.getPackageName());
        return intent;
    }

    /**************************************************/
    // TRACK RECORDING PROFILES
    /**************************************************/

    /**
     * Simple container for track recording profiles.
     */
    public static class TrackRecordProfileSimple extends Storable {

        private long mId;
        private String mName;
        private String mDesc;
        private byte[] mImg;

        /**
         * Empty constructor because of 'Storable'.
         */
        @SuppressWarnings("unused")
        public TrackRecordProfileSimple() {
            super();
        }

        /**
         * Private constructor for track record profile.
         * @param id ID of profile
         * @param name name of profile
         * @param desc description of profile
         * @param img image for profile
         */
        private TrackRecordProfileSimple(long id, String name, String desc, byte[] img) {
            super();
            this.mId = id;
            this.mName = name == null ? "" : name;
            this.mDesc = desc == null ? "" : desc;
            this.mImg = img;
        }

        /**
         * Get current profile ID.
         * @return profile ID
         */
        public long getId() {
            return mId;
        }

        /**
         * Get current profile name.
         * @return name of profile
         */
        public String getName() {
            return mName;
        }

        /**
         * Get profile generated description.
         * @return profile description
         */
        public String getDesc() {
            return mDesc;
        }

        /**
         * Get current profile icon. Icon may be converted to bitmap object
         * thanks to 'Utils.getBitmap()' function.
         * @return icon or 'null' if not defined or other problem happen
         */
        public byte[] getIcon() {
            return mImg;
        }

        // STORABLE PART

        @Override
        protected int getVersion() {
            return 0;
        }

        @Override
        public void reset() {
            mId = 0L;
            mName = "";
            mDesc = "";
            mImg = null;
        }

        @Override
        protected void readObject(int version, DataReaderBigEndian dr) throws IOException {
            mId = dr.readLong();
            mName = dr.readString();
            mDesc = dr.readString();
            int imgSize = dr.readInt();
            if (imgSize > 0) {
                mImg = new byte[imgSize];
                dr.readBytes(mImg);
            }
        }

        @Override
        protected void writeObject(DataWriterBigEndian dw) throws IOException {
            dw.writeLong(mId);
            dw.writeString(mName);
            dw.writeString(mDesc);
            int imgSize = mImg != null ? mImg.length : 0;
            dw.writeInt(imgSize);
            if (imgSize > 0) {
                dw.write(mImg);
            }
        }
    }

    /**
     * Get list of available track recording profiles currently defined in app.
     * @param ctx current context
     * @param lv version of Locus that's asked
     * @return array of profiles, where first item in array is profile ID, second item is profile name
     * @throws RequiredVersionMissingException
     */
    public static List<TrackRecordProfileSimple> getTrackRecordingProfiles(
            Context ctx, LocusVersion lv) throws RequiredVersionMissingException {
        // get scheme if valid Locus is available
        Cursor cursor;
        Uri scheme = getContentProviderData(lv, VersionCode.UPDATE_09,
                LocusConst.CONTENT_PROVIDER_PATH_TRACK_RECORD_PROFILE_NAMES);
        if (scheme != null) {
            cursor = ctx.getContentResolver().query(scheme,
                    null, null, null, null);
        } else {
            throw new RequiredVersionMissingException(VersionCode.UPDATE_09);
        }

        // handle result
        List<TrackRecordProfileSimple> profiles = new ArrayList<>();
        try {
            // search in cursor for valid key
            for (int i = 0; i < cursor.getCount(); i++)  {
                cursor.moveToPosition(i);
                TrackRecordProfileSimple prof = new TrackRecordProfileSimple(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getBlob(3));
                profiles.add(prof);
            }
        } catch (Exception e) {
            Logger.logE(TAG, "getItemPurchaseState(" + ctx + ", " + lv + ")", e);
        } finally {
            Utils.closeQuietly(cursor);
        }

        // return 'unknown' state
        return profiles;
    }

	/**************************************************/
	// WMS FUNCTIONS
	/**************************************************/
	
	/*
	  Add own WMS map
	  ------------------------------------
	  - this feature allow 3rd party application, add web address directly to list of WMS services in
	  Map Manager screen / WMS tab
	 */
	
	public static void callAddNewWmsMap(Context context, String wmsUrl)
			throws RequiredVersionMissingException, InvalidObjectException {
		// check availability and start action
		if (!LocusUtils.isLocusAvailable(context, VersionCode.UPDATE_01)) {
			throw new RequiredVersionMissingException(VersionCode.UPDATE_01);
		}
		if (TextUtils.isEmpty(wmsUrl)) {
			throw new InvalidObjectException("WMS Url address \'" + wmsUrl + "\', is not valid!");
		}
		
		// call intent with WMS url
		Intent intent = new Intent(LocusConst.ACTION_ADD_NEW_WMS_MAP);
		intent.putExtra(LocusConst.INTENT_EXTRA_ADD_NEW_WMS_MAP_URL, wmsUrl);
		context.startActivity(intent);
	}
	
	/**************************************************/
	// INFO FUNCTIONS
	/**************************************************/
	
	/**
	 * Use getLocusInfo() instead
	 */
	@Deprecated
	public static String getLocusRootDirectory(Context context) 
			throws RequiredVersionMissingException {
		LocusInfo locusInfo = getLocusInfoData(context);
		if (locusInfo != null) {
			return locusInfo.getRootDirectory();
		} else {
			return null;
		}
	}

	/**
	 * Use getLocusInfo() instead
	 */
	@Deprecated
	public static boolean isPeriodicUpdatesEnabled(Context context) 
			throws RequiredVersionMissingException {
		LocusInfo locusInfo = getLocusInfoData(context);
		return locusInfo != null &&
				locusInfo.isPeriodicUpdatesEnabled();
	}
	
	private static LocusInfo getLocusInfoData(Context ctx)
			throws RequiredVersionMissingException {
		return getLocusInfo(ctx, LocusUtils.createLocusVersion(ctx));
	}
	
	/**
	 * Return complete information about required LocusVersion. LocusInfo object
	 * contains all main parameters of existing Locus installation, together with
	 * some user preferences etc. More in LocusInfo object
	 * @param ctx current context
	 * @param lv version of Locus that's asked
	 * @return {@link LocusInfo} object or <code>null</code> if problem happen. It's 
	 * always required to check that return value is correct!
	 * @throws RequiredVersionMissingException
	 */
	public static LocusInfo getLocusInfo(Context ctx, LocusVersion lv)
			throws RequiredVersionMissingException {
		// get scheme if valid Locus is available
		Cursor cursor;
        Uri scheme = getContentProviderData(lv, VersionCode.UPDATE_01,
                LocusConst.CONTENT_PROVIDER_PATH_INFO);
		if (scheme == null) {
			Logger.logD(TAG, "getLocusInfo(" + ctx + ", " + lv + "), invalid version");
			throw new RequiredVersionMissingException(VersionCode.UPDATE_01);
		}
		
		// generate cursor
		cursor = ctx.getContentResolver().query(scheme,
					null, null, null, null);
		
		// handle result
		try {
			return LocusInfo.create(cursor);
		} catch (Exception e) {
			Logger.logE(TAG, "getLocusInfo(" + ctx + ", " + lv + ")", e);
		} finally {
			Utils.closeQuietly(cursor);
		}
		return null;
	}

    /**************************************************/
    // CONTENT OF LOCUS STORE
    /**************************************************/

	/**
	 * Allows to check if item with known ID is already purchased by user.
	 * @param ctx current context
	 * @param lv version of Locus that's asked
	 * @param itemId know ID of item
	 * @return ItemPurchaseState state of purcahse
	 * @throws RequiredVersionMissingException
	 */
	public static int getItemPurchaseState(Context ctx, LocusVersion lv, long itemId)
			throws RequiredVersionMissingException {
		// get scheme if valid Locus is available
		Cursor cursor;
		Uri scheme = getContentProviderData(lv, VersionCode.UPDATE_06,
				LocusConst.CONTENT_PROVIDER_PATH_ITEM_PURCHASE_STATE);
		if (scheme != null) {
			scheme = ContentUris.withAppendedId(scheme, itemId);
			cursor = ctx.getContentResolver().query(scheme,
					null, null, null, null);
		} else {
			throw new RequiredVersionMissingException(VersionCode.UPDATE_06);
		}

		// handle result
		try {
			// search in cursor for valid key
			for (int i = 0; i < cursor.getCount(); i++)  {
				cursor.moveToPosition(i);
				String key = cursor.getString(0);
				if (key.equals("purchaseState")) {
					return cursor.getInt(1);
				}
			}
		} catch (Exception e) {
			Logger.logE(TAG, "getItemPurchaseState(" + ctx + ", " + lv + ")", e);
		} finally {
			Utils.closeQuietly(cursor);
		}

		// return 'unknown' state
		return LocusConst.PURCHASE_STATE_UNKNOWN;
	}

    /**
     * Start Locus and display certain item from Store defined by it's unique ID.
     * @param ctx current context
     * @param lv known LocusVersion
     * @param itemId known item ID
     * @throws RequiredVersionMissingException
     */
    public static void displayLocusStoreItemDetail(Context ctx, LocusVersion lv, long itemId)
            throws RequiredVersionMissingException {
        // check if application is available
        if (lv == null || !lv.isVersionValid(VersionCode.UPDATE_12)) {
            Logger.logW(TAG, "displayLocusStoreItemDetail(), " +
                    "invalid Locus version");
            throw new RequiredVersionMissingException(VersionCode.UPDATE_12);
        }

        // call Locus
        Intent intent = new Intent(LocusConst.ACTION_DISPLAY_STORE_ITEM);
        intent.putExtra(LocusConst.INTENT_EXTRA_ITEM_ID, itemId);
        ctx.startActivity(intent);
    }

	/**************************************************/
	// MAP PREVIEW
	/**************************************************/

    /**
     * Result container for screenshot request.
     */
	public static class BitmapLoadResult extends Storable {

        // loaded image
		private byte[] mImg;
        // number of not yet loaded tiles
        private int mNotYetLoadedTiles;

        /**
         * Empty constructor.
         */
        public BitmapLoadResult() {
            super();
        }

        /**
         * Private constructor.
         * @param img loaded image
         * @param notYetLoadedTiles number of tiles
         */
		private BitmapLoadResult(byte[] img, int notYetLoadedTiles) {
            super();
			this.mImg = img;
			this.mNotYetLoadedTiles = notYetLoadedTiles;
		}

        /**
         * Check if loaded result has valid image.
         * @return <code>true</code> if image is valid
         */
		public boolean isValid() {
			return mImg != null;
		}

        /**
         * Get current loaded image tile.
         * @return loaded image
         */
        public byte[] getImageB() {
            return mImg;
        }

        public Bitmap getImage() {
            return BitmapFactory.decodeByteArray(mImg, 0, mImg.length);
        }

        /**
         * Get number of missing (not yet loaded) tiles.
         * @return number of tiles
         */
        public int getNumOfNotYetLoadedTiles() {
            return mNotYetLoadedTiles;
        }

        // STORABLE PART

        @Override
        protected int getVersion() {
            return 0;
        }

        @Override
        public void reset() {
            mImg = null;
            mNotYetLoadedTiles = 0;
        }

        @Override
        protected void readObject(int version, DataReaderBigEndian dr) throws IOException {
            int size = dr.readInt();
            if (size > 0) {
                mImg = new byte[size];
                dr.readBytes(mImg);
                mNotYetLoadedTiles = dr.readInt();
            }
        }

        @Override
        protected void writeObject(DataWriterBigEndian dw) throws IOException {
            if (mImg == null || mImg.length == 0) {
                dw.writeInt(0);
            } else {
                dw.writeInt(mImg.length);
                dw.write(mImg);
            }
            dw.writeInt(mNotYetLoadedTiles);
        }
    }

    /**
     * Get preview of current map screen.
     * @param ctx current context
     * @param lv LocusVersion container
     * @param locCenter location of center
     * @param zoomValue zoom level
     * @param widthPx required width in pixels
     * @param heightPx required height in pixels
     * @return generated result
     * @throws RequiredVersionMissingException
     */
	public static BitmapLoadResult getMapPreview(Context ctx, LocusVersion lv, 
			Location locCenter, int zoomValue, int widthPx, int heightPx, boolean tinyMode)
			throws RequiredVersionMissingException {
		// get scheme if valid Locus is available
		Cursor cursor;
        Uri scheme = getContentProviderData(lv, VersionCode.UPDATE_04,
                LocusConst.CONTENT_PROVIDER_PATH_MAP_PREVIEW);
		if (scheme == null) {
			throw new RequiredVersionMissingException(VersionCode.UPDATE_04);
		}
		
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append("lon=").append(locCenter.getLongitude()).append(",");
		sbQuery.append("lat=").append(locCenter.getLatitude()).append(",");
		sbQuery.append("zoom=").append(zoomValue).append(",");
		sbQuery.append("width=").append(widthPx).append(",");
		sbQuery.append("height=").append(heightPx).append(",");
        sbQuery.append("tinyMode=").append(tinyMode ? 1 : 0);
		
		// generate cursor
		cursor = ctx.getContentResolver().query(scheme,
					null, sbQuery.toString(), null, null);
				
		// handle result
		try {
            byte[] img = null;
			int notYetLoadedTiles = 0;
			for (int i = 0; i < cursor.getCount(); i++)  {
				cursor.moveToPosition(i);
				String key = new String(cursor.getBlob(0));
				byte[] data = cursor.getBlob(1);
				if (key.equals(LocusConst.VALUE_MAP_PREVIEW)) {
					img = data;
				} else if (key.equals(LocusConst.VALUE_MAP_PREVIEW_MISSING_TILES)) {
					notYetLoadedTiles = Utils.parseInt(new String(data));
				}
			}

			// return result
			return new BitmapLoadResult(img, notYetLoadedTiles);
		} catch (Exception e) {
			Logger.logE(TAG, "getMapPreview()", e);
			return new BitmapLoadResult(null, 0);
		} finally {
            Utils.closeQuietly(cursor);
		}
	}

    /**************************************************/
    // WORK WITH DYNAMICALLY REGISTERED PERIODIC UPDATES RECEIVER
    /**************************************************/

    /**
     * Enable updates receiver for PeriodicUpdates. Use this function in case, you do NOT have
     * defined received in manifest file and instead, you want to enable/disable received
     * dynamically at runtime.
     * @param ctx current context
     * @param lv Locus version to work with
     * @param receiver class that will be registered as receiver
     */
    public static void enablePeriodicUpdatesReceiver(Context ctx, LocusVersion lv,
            Class<? extends BroadcastReceiver> receiver) throws RequiredVersionMissingException {
        Logger.logD(TAG, "enableReceiver(" + ctx + ")");
        PackageManager pm = ctx.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(ctx, receiver),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        // notify about change
        refreshPeriodicUpdateListeners(ctx, lv);
    }

    /**
     * Disable updates receiver for PeriodicUpdates. Use this function in case, you do NOT have
     * defined received in manifest file and instead, you want to enable/disable received
     * dynamically at runtime.
     * @param ctx current context
     * @param lv Locus version to work with
     * @param receiver class that will be registered as receiver
     */
    public static void disablePeriodicUpdatesReceiver(Context ctx, LocusVersion lv,
            Class<? extends BroadcastReceiver> receiver) throws RequiredVersionMissingException {
        Logger.logD(TAG, "disableReceiver(" + ctx + ")");
        PackageManager pm = ctx.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(ctx, receiver),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        // notify about change
        refreshPeriodicUpdateListeners(ctx, lv);
    }

    /**
     * Send broadcast to refresh internal list of periodic update listeners.
     * @param ctx current context
     * @param lv expected target locus
     * @throws RequiredVersionMissingException
     */
    private static void refreshPeriodicUpdateListeners(Context ctx, LocusVersion lv)
            throws RequiredVersionMissingException {
        // check version (available only in Free/Pro)
        if (!LocusUtils.isLocusFreePro(lv, VersionCode.UPDATE_01.vcFree)) {
            throw new RequiredVersionMissingException(VersionCode.UPDATE_01);
        }

        // call intent
        Intent intent = new Intent(LocusConst.ACTION_REFRESH_PERIODIC_UPDATE_LISTENERS);
        intent.setPackage(lv.getPackageName());
        ctx.sendBroadcast(intent);
    }

    /**************************************************/
    // WORK WITH CONTENT PROVIDERS
    /**************************************************/

    private static Uri getContentProviderData(LocusVersion lv, VersionCode vc, String path) {
        return getContentProviderUri(lv, vc,
                LocusConst.CONTENT_PROVIDER_AUTHORITY_DATA,
                path);
    }

    public static Uri getContentProviderGeocaching(LocusVersion lv, VersionCode vc, String path) {
        return getContentProviderUri(lv, vc,
                LocusConst.CONTENT_PROVIDER_AUTHORITY_GEOCACHING,
                path);
    }

    private static Uri getContentProviderUri(LocusVersion lv, VersionCode vc,
            String provider, String path) {
        // check URI parts
        if (provider == null || provider.length() == 0 ||
                path == null || path.length() == 0) {
            Logger.logW(TAG, "getContentProviderUri(), " +
                    "invalid 'authority' or 'path'parameters");
            return null;
        }

        // check if application is available
        if (lv == null || vc == null || !lv.isVersionValid(vc)) {
            Logger.logW(TAG, "getContentProviderUri(), " +
                    "invalid Locus version");
            return null;
        }

        // generate content provider by type
        StringBuilder sb = new StringBuilder();
        if (lv.isVersionFree()) {
            sb.append("content://menion.android.locus.free");
        } else if (lv.isVersionPro()) {
            sb.append("content://menion.android.locus.pro");
        } else if (lv.isVersionGis()) {
            sb.append("content://menion.android.locus.gis");
        } else {
            Logger.logW(TAG, "getContentProviderUri(), " +
                    "unknown Locus version:" + lv);
            return null;
        }

        // finish URI
        return Uri.parse(sb.append(".").append(provider).
                append("/").append(path).toString());
    }
}
