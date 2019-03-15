/*
 * Copyright 2011, Asamm Software, s.r.o.
 *
 * This file is part of LocusAddonPublicLib.
 *
 * LocusAddonPublicLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LocusAddonPublicLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LocusAddonPublicLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package locus.api.android.utils;

public final class LocusConst {
    private LocusConst() {
    }

    /******************
     * these intent are used for extending functionality of Locus. All description is
     * in 'LocusUtils' class
     ******************/

    //*************************************************
    // ITEMS IN MENUS
    //*************************************************

    /**
     * This Filter add your activity into list of main functions
     */
    public static final String INTENT_ITEM_MAIN_FUNCTION = "locus.api.android.INTENT_ITEM_MAIN_FUNCTION";

    /**
     * This Filter add your activity into list of main functions. More precisely into section
     * called "Geocaching".
     */
    public static final String INTENT_ITEM_MAIN_FUNCTION_GC = "locus.api.android.INTENT_ITEM_MAIN_FUNCTION_GC";

    /**
     * Add your activity in list of "Location sources"
     */
    public static final String INTENT_ITEM_GET_LOCATION = "locus.api.android.INTENT_ITEM_GET_LOCATION";

    /**
     * Add your activity in sub-menu in waypoint dialog (last button > tools)
     */
    public static final String INTENT_ITEM_POINT_TOOLS = "locus.api.android.INTENT_ITEM_POINT_TOOLS";

    /**
     * This Filter add your activity into list of tools in Points screen
     */
    public static final String INTENT_ITEM_POINTS_SCREEN_TOOLS = "locus.api.android.INTENT_ITEM_POINTS_SCREEN_TOOLS";

    /**
     * Add your activity in sub-menu in track dialog (last button > tools)
     */
    public static final String INTENT_ITEM_TRACK_TOOLS = "locus.api.android.INTENT_ITEM_TRACK_TOOLS";

    /**
     * This Filter add your activity into list of search options in Locus "Search" function
     */
    public static final String INTENT_ITEM_SEARCH_LIST = "locus.api.android.INTENT_ITEM_SEARCH_LIST";

    //*************************************************
    // ACTIONS (CALL OF ACTIVITY)
    //*************************************************

    /**
     * Intent that allow to send WMS url link directly into Locus
     * Available since Locus 2.4.1 (code 216)
     */
    public static final String ACTION_ADD_NEW_WMS_MAP = "locus.api.android.ACTION_ADD_NEW_WMS_MAP";

    /**
     * Request on Locus to display whole detail screen of certain Point. Call of this feature is
     * defined in ActionTools class.
     */
    public static final String ACTION_DISPLAY_POINT_SCREEN = "locus.api.android.ACTION_DISPLAY_POINT_SCREEN";

    /**
     * Request on Locus to display whole detail screen of certain Point. Call of this feature is
     * defined in ActionTools class.
     */
    public static final String ACTION_DISPLAY_STORE_ITEM = "locus.api.android.ACTION_DISPLAY_STORE_ITEM";

    /**
     * Start guidance.
     */
    public static final String ACTION_GUIDING_START = "locus.api.android.ACTION_GUIDING_START";

    /**
     * Request on Log field notes online.
     */
    public static final String ACTION_LOG_FIELD_NOTES = "locus.api.android.LOG_FIELD_NOTES";

    /**
     * Start navigation.
     */
    public static final String ACTION_NAVIGATION_START = "locus.api.android.ACTION_NAVIGATION_START";

    /**
     * Intent used for getting location from Locus to your application. This one, is used just to start
     * Locus with this request.
     */
    public static final String ACTION_PICK_LOCATION = "locus.api.android.ACTION_PICK_LOCATION";

    /**
     * Intent used for getting track in specific format from Locus app
     *
     * Usage: `ActionBasics.getTrackInFormat()`
     */
    public static final String ACTION_GET_TRACK_AS_FILE_ACT = "com.asamm.locus.api.GET_TRACK_AS_FILE_ACT";

    // ACTIONS FROM LOCUS TO API

    /**
     * Action used for receiving Location from Locus
     */
    public static final String ACTION_RECEIVE_LOCATION = "locus.api.android.ACTION_RECEIVE_LOCATION";

    //*************************************************
    // ACTIONS > DATA HANDLING
    //*************************************************

    /**
     * Basic intent used for display data. Use API for creating intent and not directly
     */
    public static final String ACTION_DISPLAY_DATA = "locus.api.android.ACTION_DISPLAY_DATA";

    /**
     * Used for sending data to Locus. These data should be small (and fast).
     * <br><br>
     * Content is same as with sending data by INTENT_DISPLAY_DATA anyway think mainly on best user
     * experience!! So suggestion is to send intent filled by EXTRA_POINTS_DATA or EXTRA_POINTS_DATA_ARRAY.
     * These are fastest methods and should be enough for sending useful amount of informations
     * <br><br>
     * Also, these data will have automatically EXTRA_CALL_IMPORT set to false, so they'll be directly
     * displayed on map without possibility for import! Also after display, map will not be centered.
     */
    public static final String ACTION_DISPLAY_DATA_SILENTLY = "locus.api.android.ACTION_DISPLAY_DATA_SILENTLY";

    /**
     * Basic intent used for hiding previously displayed items. This method allow to hide only temp items!
     */
    public static final String ACTION_REMOVE_DATA_SILENTLY = "locus.api.android.ACTION_REMOVE_DATA_SILENTLY";

    //*************************************************
    // ACTIONS (CALL OVER BROADCASTS)
    //*************************************************

    // TRACK RECORDING

    /**
     * start track record
     */
    public static final String ACTION_TRACK_RECORD_START = "locus.api.android.ACTION_TRACK_RECORD_START";

    /**
     * pause track record
     */
    public static final String ACTION_TRACK_RECORD_PAUSE = "locus.api.android.ACTION_TRACK_RECORD_PAUSE";

    /**
     * stop track record
     */
    public static final String ACTION_TRACK_RECORD_STOP = "locus.api.android.ACTION_TRACK_RECORD_STOP";

    /**
     * add waypoint
     */
    public static final String ACTION_TRACK_RECORD_ADD_WPT = "locus.api.android.ACTION_TRACK_RECORD_ADD_WPT";

    // VARIOUS

    /**
     * Refresh active listeners for periodic udpates. This task is required if you register
     * broadcast received manually in code. Then Locus needs information that another listener is
     * ready for "listening".
     */
    public static final String ACTION_REFRESH_PERIODIC_UPDATE_LISTENERS = "com.asamm.locus.ACTION_REFRESH_PERIODIC_UPDATE_LISTENERS";

    /**
     * Intent used for getting track in specific format from Locus app
     *
     * Usage: `ActionBasics.getTrackInFormat()`
     */
    public static final String ACTION_GET_TRACK_AS_FILE_BR = "com.asamm.locus.api.GET_TRACK_AS_FILE_BR";

    // LIVE TRACKING

    /**
     * Stop live tracking service.
     */
    public static final String ACTION_LIVE_TRACKING_STOP = "com.asamm.locus.ACTION_LIVE_TRACKING_STOP";

    //*************************************************
    // BROADCAST RECEIVERS
    //*************************************************

    /**
     * Used for receiving update container from Locus.
     */
    public static final String ACTION_PERIODIC_UPDATE = "locus.api.android.ACTION_PERIODIC_UPDATE";

    /**
     * Used for receiving events about changed point in Locus. These intent are
     * send as BroadcastIntents, so you need to register your app as BroadcastReceiver.
     * <br><br >
     * <b>Intent contain</b>:<br>
     * INTENT_EXTRA_ITEM_ID - ID of waypoint in Locus<br>
     * INTENT_EXTRA_NAME - name of point<br>
     * INTENT_EXTRA_GEOCACHE_CODE - GC code of point (this parameter is optional)<br>
     */
    public static final String ACTION_POINT_CHANGED = "locus.api.android.ACTION_POINT_CHANGED";

    /**
     * Every service that extends {@link locus.api.android.features.mapProvider.MapTileService} service,
     * have to also include this action into it's manifest. This cause Locus to register
     * defined service as another map provider. (info: not yet ready)
     */
    public static final String ACTION_SERVICE_MAP_PROVIDER = "locus.api.android.ACTION_MAP_PROVIDER";

    /**
     * Every service that extends {@link locus.api.android.features.computeTrack.ComputeTrackService} service,
     * have to also include this action into it's manifest. This cause Locus to register
     * defined service as another track compute provider.
     */
    public static final String ACTION_SERVICE_COMPUTE_TRACK_PROVIDER = "locus.api.android.ACTION_COMPUTE_TRACK_PROVIDER";

    //*************************************************
    // EXTRAS
    /**************************************************/

    // VARIOUS

    // location send from Locus, current GPS position
    public static final String INTENT_EXTRA_LOCATION_GPS = "INTENT_EXTRA_LOCATION_GPS";
    // location send from Locus, map center
    public static final String INTENT_EXTRA_LOCATION_MAP_CENTER = "INTENT_EXTRA_LOCATION_MAP_CENTER";
    // package name of Locus that sends this intent
    public static final String INTENT_EXTRA_PACKAGE_NAME = "INTENT_EXTRA_PACKAGE_NAME";
    // extra parameter to enable confirmation dialog for certain intent
    public static final String INTENT_EXTRA_CONFIRMATION = "INTENT_EXTRA_CONFIRMATION";

    /**
     * name send back in response to GET_LOCATION intent
     */
    public static final String INTENT_EXTRA_NAME = "INTENT_EXTRA_NAME";

    /**
     * Code of Geocache in format GCXXXX
     */
    public static final String INTENT_EXTRA_GEOCACHE_CODE = "INTENT_EXTRA_GEOCACHE_CODE";

    /**
     * location send back in response to GET_LOCATION intent
     */
    public static final String INTENT_EXTRA_LOCATION = "INTENT_EXTRA_LOCATION";

    /**
     * one PointData object, send over intent
     */
    public static final String INTENT_EXTRA_POINTS_DATA = "INTENT_EXTRA_POINTS_DATA";

    /**
     * array of PointData objects, send over intent
     */
    public static final String INTENT_EXTRA_POINTS_DATA_ARRAY = "INTENT_EXTRA_POINTS_DATA_ARRAY";

    /**
     * sends points data serialized as byte[] through file stored on SD card
     */
    public static final String INTENT_EXTRA_POINTS_FILE_PATH = "INTENT_EXTRA_POINTS_FILE_PATH";

    /**
     * sends points data serialized as byte[] through file shared via {@code FileProvider}
     */
    public static final String INTENT_EXTRA_POINTS_FILE_URI = "INTENT_EXTRA_POINTS_FILE_URI";

    /**
     * Sends one single track to Locus
     */
    public static final String INTENT_EXTRA_TRACKS_SINGLE = "INTENT_EXTRA_TRACKS_SINGLE";

    /**
     * Sends multiple tracks to Locus
     */
    public static final String INTENT_EXTRA_TRACKS_MULTI = "INTENT_EXTRA_TRACKS_MULTI";

    /**
     * one PointData object, send over intent
     */
    public static final String INTENT_EXTRA_CIRCLES_MULTI = "INTENT_EXTRA_CIRCLES_MULTI";

    /**
     * extra data - latitude, used in rare cases for application that start Locus not from API
     */
    public static final String INTENT_EXTRA_ADDRESS_TEXT = "INTENT_EXTRA_ADDRESS_TEXT";

    /**
     * Extra parameter that set if data should be firstly imported. This is used in intent
     * that sends also
     */
    public static final String INTENT_EXTRA_CALL_IMPORT = "INTENT_EXTRA_CALL_IMPORT";

    /**
     * Extra parameter that set if Locus should center on incoming data or not.
     */
    public static final String INTENT_EXTRA_CENTER_ON_DATA = "INTENT_EXTRA_CENTER_ON_DATA";

    /**
     * If you set to any point "setExtraOnDisplay" callback, then when Locus display points and
     * ask for extended version, return result as Point object included in extra place in intent
     */
    public static final String INTENT_EXTRA_POINT = "INTENT_EXTRA_POINT";

    /**
     * Optional boolean value in returning intent. Settings to true, Locus will overwrite point
     * in database. If you want to call "setExtraOnDisplay" next time, don't forget to set it
     * in updated waypoint!
     */
    public static final String INTENT_EXTRA_POINT_OVERWRITE = "INTENT_EXTRA_POINT_OVERWRITE";

    /**
     * Extra content when adding new WMS map directly to Locus
     */
    public static final String INTENT_EXTRA_ADD_NEW_WMS_MAP_URL = "INTENT_EXTRA_ADD_NEW_WMS_MAP_URL";

    /**
     * Single item in intent. ID is represented as <code>long</code> value.
     */
    public static final String INTENT_EXTRA_ITEM_ID = "INTENT_EXTRA_ITEM_ID";

    /**
     * list of IDs of points, received by INTENT_ITEM_POINTS_SCREEN_TOOLS
     */
    public static final String INTENT_EXTRA_ITEMS_ID = "INTENT_EXTRA_ITEMS_ID";

    /**
     * extra data - latitude, used in rare cases for application that start Locus not from API
     */
    public static final String INTENT_EXTRA_LATITUDE = "INTENT_EXTRA_LATITUDE";

    /**
     * extra data - longitude, used in rare cases for application that start Locus not from API
     */
    public static final String INTENT_EXTRA_LONGITUDE = "INTENT_EXTRA_LONGITUDE";

    /**
     * profile for track record. Use this extra value for <b>ACTION_TRACK_RECORD_START</b>. Value should
     * contain String with name of required profile. If profile is no valid, current values will
     * remain set in Locus.
     */
    public static final String INTENT_EXTRA_TRACK_REC_PROFILE = "INTENT_EXTRA_TRACK_REC_PROFILE";

    /**
     * boolean value to determine if recorded track/or waypoint should be automatically saved,
     * so no dialog with parameters before save will display. Default <code>false</code>.
     * This extra parameter is used with <b>ACTION_TRACK_RECORD_STOP</b> and
     * <b>ACTION_TRACK_RECORD_ADD_WPT</b> broadcast.
     */
    public static final String INTENT_EXTRA_TRACK_REC_AUTO_SAVE = "INTENT_EXTRA_TRACK_REC_AUTO_SAVE";

    public static final String INTENT_EXTRA_TRACK_REC_ACTION_AFTER =
            "INTENT_EXTRA_TRACK_REC_ACTION_AFTER";
    /**
     * Sends multiple tracks to Locus
     */
    public static final String INTENT_EXTRA_START_NAVIGATION = "INTENT_EXTRA_START_NAVIGATION";

    /**
     * List of ids (of field notes), that should be handled
     */
    public static final String INTENT_EXTRA_FIELD_NOTES_IDS = "INTENT_EXTRA_FIELD_NOTES_IDS";
    /**
     * True/False value if we wants create a log or just field note
     */
    public static final String INTENT_EXTRA_FIELD_NOTES_CREATE_LOG = "INTENT_EXTRA_FIELD_NOTES_CREATE_LOG";

    /**
     * Error message as a result of any operation.
     */
    public static final String INTENT_EXTRA_ERROR = "INTENT_EXTRA_ERROR";

    //*************************************************
    // CONTENT PROVIDER INFO
    //*************************************************

    // PROVIDERS AUTHORITIES

    /**
     * Provider for various Locus data
     */
    public static final String CONTENT_PROVIDER_AUTHORITY_DATA = "LocusDataProvider";
    /**
     * Currently special provider for geocaching stuff (only FieldNotes for now)
     */
    public static final String CONTENT_PROVIDER_AUTHORITY_GEOCACHING = "GeocachingDataProvider";
    // path for provider of map previews
    public static final String CONTENT_PROVIDER_AUTHORITY_MAP_TOOLS = "MapTools";

    // PROVIDER PATHS

    /**
     * Path for basic information cursor
     */
    public static final String CONTENT_PROVIDER_PATH_DATA = "data";
    /**
     * Path for basic information cursor
     */
    public static final String CONTENT_PROVIDER_PATH_INFO = "info";
    /**
     * Path for question on item purchase state
     */
    public static final String CONTENT_PROVIDER_PATH_ITEM_PURCHASE_STATE = "itemPurchaseState";
    /**
     * Path for renderer map preview
     */
    public static final String CONTENT_PROVIDER_PATH_MAP_PREVIEW = "mapPreview";
    /**
     * Path for retrieving tracks from application.
     */
    public static final String CONTENT_PROVIDER_PATH_TRACK = "track";
    /**
     * Path for retrieving waypoints from application.
     */
    public static final String CONTENT_PROVIDER_PATH_WAYPOINT = "waypoint";
    /**
     * Path for question on item purchase state
     */
    public static final String CONTENT_PROVIDER_PATH_TRACK_RECORD_PROFILE_NAMES = "trackRecordProfileNames";

    // value for LocusInfo object
    public static final String VALUE_LOCUS_INFO = "locusInfo";
    // generated map preview
    public static final String VALUE_MAP_PREVIEW = "mapPreview";
    // number of tiles that still needs to render to have map full
    public static final String VALUE_MAP_PREVIEW_MISSING_TILES = "mapPreviewMissingTiles";
    // value for UpdateContainer object
    public static final String VALUE_UPDATE_CONTAINER = "updateContainer";

    // TRACK RECORDING - ADD WAYPOINT

    // basic waypoint
    public static final String VALUE_TRK_REC_ADD_WAYPOINT_BASIC = "basic";
    // basic waypoint where audio record is started immediately
    public static final String VALUE_TRK_REC_ADD_WAYPOINT_AUDIO = "audio";
    // basic waypoint where took a photo is started immediately
    public static final String VALUE_TRK_REC_ADD_WAYPOINT_PHOTO = "photo";
    // basic waypoint where video record is started immediately
    public static final String VALUE_TRK_REC_ADD_WAYPOINT_VIDEO = "video";

    //*************************************************
    // STATE FOR PURCHASED ITEMS
    //*************************************************

    /**
     * User has purchased valid version of certain item.
     */
    public static final int PURCHASE_STATE_PURCHASED = 1;
    /**
     * User do not have valid purchase.
     */
    public static final int PURCHASE_STATE_NOT_PURCHASED = 2;
    /**
     * Unknown state. This may happen in case, users profile is not cached.
     */
    public static final int PURCHASE_STATE_UNKNOWN = 0;
}
