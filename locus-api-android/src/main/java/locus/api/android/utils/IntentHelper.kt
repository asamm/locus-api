/****************************************************************************
 *
 * Created by menion on 07/02/2019.
 * Copyright (c) 2019. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/

package locus.api.android.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import locus.api.android.ActionBasics
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.extra.Location
import locus.api.objects.extra.Point
import locus.api.objects.extra.Track
import locus.api.utils.Logger

/**
 * Methods useful for handling received results from Locus based app.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object IntentHelper {

    // tag for logger
    private const val TAG = "ResponseHelper"

    //*************************************************
    // GET LOCATION INTENT
    //*************************************************

    interface OnIntentGetLocation {
        /**
         * Handle received request
         *
         * @param locGps       if GPS is enabled, location is included (may be null)
         * @param locMapCenter center location of displayed map (may be null)
         */
        fun onReceived(locGps: Location?, locMapCenter: Location?)

        /**
         * If intent is not INTENT_GET_LOCATION intent or other problem occur
         */
        fun onFailed()
    }

    /*
	   Add POI from your application
	  -------------------------------
	   - on places where is location needed, you may add link to your application. So for example, when
	   you display Edit point dialog, you may see next to coordinates button "New". This is list of
	   location sources and also place when your application appear with this method

		1. register intent-filter for your activity

		<intent-filter>
			<action android:name="locus.api.android.INTENT_ITEM_GET_LOCATION" />
			<category android:name="android.intent.category.DEFAULT" />
		</intent-filter>

		2. register intent receiver in your application

		if (getIntent().getAction().equals(LocusConst.INTENT_GET_POINT)) {
			// get some data here and finally return value back, more below
		}
	 */
    fun isIntentGetLocation(intent: Intent): Boolean {
        return isAction(intent, LocusConst.INTENT_ITEM_GET_LOCATION)
    }

    @Throws(NullPointerException::class)
    fun handleIntentGetLocation(context: Context, intent: Intent,
            handler: OnIntentGetLocation) {
        // check intent itself
        if (!isIntentGetLocation(intent)) {
            handler.onFailed()
            return
        }

        // variables that may be obtain from intent
        handler.onReceived(
                LocusUtils.getLocationFromIntent(intent, LocusConst.INTENT_EXTRA_LOCATION_GPS),
                LocusUtils.getLocationFromIntent(intent, LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER))
    }

    fun sendGetLocationData(activity: Activity, name: String, loc: Location) {
        val intent = Intent()
        // string value name - OPTIONAL
        if (name.isNotBlank()) {
            intent.putExtra(LocusConst.INTENT_EXTRA_NAME, name)
        }
        intent.putExtra(LocusConst.INTENT_EXTRA_LOCATION, loc.asBytes)
        activity.setResult(Activity.RESULT_OK, intent)
        activity.finish()
    }

    //*************************************************
    // POINT TOOLS
    //*************************************************

    /*
	   Add action under point sub-menu
	  -------------------------------
	   - when you tap on any point on map or in Point screen, under share bottom button, are functions for
	   calling to some external application. Under this menu appear also your application. If you want specify
	   action only on your points displayed in Locus, use 'setExtraCallback' function on 'Point' object instead
	   of this. It has same functionality but allow displaying only on your 'own' points.

	   1. register intent-filter for your activity

		<intent-filter>
			<action android:name="locus.api.android.INTENT_ITEM_POINT_TOOLS" />
			<category android:name="android.intent.category.DEFAULT" />
		</intent-filter>

		- extra possibility to act only on geocache point
		<intent-filter>
			<action android:name="locus.api.android.INTENT_ITEM_POINT_TOOLS" />
			<category android:name="android.intent.category.DEFAULT" />

			<data android:scheme="locus" />
			<data android:path="menion.android.locus/point_geocache" />
		</intent-filter>

	   2. register intent receiver in your application or use functions below

		if (isIntentOnPointAction(intent) {
			Waypoint wpt = LocusUtils.getWaypointFromIntent(intent);
        	if (wpt == null) {
        		... problem
        	} else {
         		... handle waypoint
        	}
		}
	 */

    /**
     * Check if received intent is result of "Point tools" click.
     *
     * @param intent received intent
     */
    fun isIntentPointTools(intent: Intent): Boolean {
        return isAction(intent, LocusConst.INTENT_ITEM_POINT_TOOLS)
    }

    //*************************************************
    // TRACK TOOLS
    //*************************************************

    // more info
    // https://github.com/asamm/locus-api/wiki/Own-function-in-Track-screen

    /**
     * Check if received intent is result of "Track tools" click.
     *
     * @param intent received intent
     */
    fun isIntentTrackTools(intent: Intent): Boolean {
        return isAction(intent, LocusConst.INTENT_ITEM_TRACK_TOOLS)
    }

    //*************************************************
    // SEARCH SCREEN
    //*************************************************

    /*
	   Add action under MAIN function menu or SEARCH list
	  -------------------------------------
	   - when you display menu->functions, your application appear here. Also you application (activity) may
	    be added to right quick menu. Application will be called with current map center coordinates

	   1. register intent-filter for your activity

		<intent-filter>
			<action android:name="locus.api.android.INTENT_ITEM_MAIN_FUNCTION" />
			<category android:name="android.intent.category.DEFAULT" />
		</intent-filter>

		<intent-filter>
			<action android:name="locus.api.android.INTENT_ITEM_SEARCH_LIST" />
			<category android:name="android.intent.category.DEFAULT" />
		</intent-filter>

    2. register intent receiver in your application

		if (isIntentMainFunction(LocusConst.INTENT_ITEM_MAIN_FUNCTION)) {
			// more below ...
		}
	 */


    /**
     * Check if received intent is response on MAIN_FUNCTION intent.
     *
     * @param intent received intent
     * @return `true` if intent is base on MAIN_FUNCTION parameter
     */
    fun isIntentMainFunction(intent: Intent): Boolean {
        return isAction(intent, LocusConst.INTENT_ITEM_MAIN_FUNCTION)
    }

    /**
     * Handle received intent from section MAIN_FUNCTION.
     *
     * @param intent  received intent
     * @param handler handler for events
     * @throws NullPointerException exception if any required data are missing
     */
    @Throws(NullPointerException::class)
    fun handleIntentMainFunction(ctx: Context, intent: Intent,
            handler: OnIntentMainFunction) {
        handleIntentMenuItem(ctx, intent, handler, LocusConst.INTENT_ITEM_MAIN_FUNCTION)
    }

    /**
     * Check if received intent is response on MAIN_FUNCTION_GC intent.
     *
     * @param intent received intent
     * @return `true` if intent is base on MAIN_FUNCTION_GC parameter
     */
    fun isIntentMainFunctionGc(intent: Intent): Boolean {
        return isAction(intent, LocusConst.INTENT_ITEM_MAIN_FUNCTION_GC)
    }

    /**
     * Handle received intent from section MAIN_FUNCTION_GC.
     *
     * @param intent  received intent
     * @param handler handler for events
     * @throws NullPointerException exception if any required data are missing
     */
    @Throws(NullPointerException::class)
    fun handleIntentMainFunctionGc(ctx: Context, intent: Intent,
            handler: OnIntentMainFunction) {
        handleIntentMenuItem(ctx, intent, handler, LocusConst.INTENT_ITEM_MAIN_FUNCTION_GC)
    }

    /**
     * Check if received intent is response on SEARCH_LIST intent.
     *
     * @param intent received intent
     * @return `true` if intent is base on SEARCH_LIST parameter
     */
    fun isIntentSearchList(intent: Intent): Boolean {
        return isAction(intent, LocusConst.INTENT_ITEM_SEARCH_LIST)
    }

    /**
     * Handle received intent from section SEARCH_LIST.
     *
     * @param intent  received intent
     * @param handler handler for events
     * @throws NullPointerException exception if any required data are missing
     */
    @Throws(NullPointerException::class)
    fun handleIntentSearchList(ctx: Context, intent: Intent,
            handler: OnIntentMainFunction) {
        handleIntentMenuItem(ctx, intent, handler, LocusConst.INTENT_ITEM_SEARCH_LIST)
    }

    @Throws(NullPointerException::class)
    private fun handleIntentMenuItem(ctx: Context, intent: Intent,
            handler: OnIntentMainFunction, item: String) {
        // check intent itself
        if (!isAction(intent, item)) {
            handler.onFailed()
            return
        }

        // get version and handle received data
        LocusUtils.createLocusVersion(ctx, intent)?.let {
            handler.onReceived(it,
                    LocusUtils.getLocationFromIntent(intent, LocusConst.INTENT_EXTRA_LOCATION_GPS),
                    LocusUtils.getLocationFromIntent(intent, LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER))
        } ?: {
            handler.onFailed()
        }()
    }

    interface OnIntentMainFunction {
        /**
         * When intent really contain location, result is returned by this function
         *
         * @param lv           version of Locus
         * @param locGps       if gpsEnabled is true, variable contain location, otherwise `null`
         * @param locMapCenter contain current map center location
         */
        fun onReceived(lv: LocusUtils.LocusVersion, locGps: Location, locMapCenter: Location)

        fun onFailed()
    }


    fun isIntentPointsScreenTools(intent: Intent): Boolean {
        return isAction(intent, LocusConst.INTENT_ITEM_POINTS_SCREEN_TOOLS)
    }

    fun handleIntentPointsScreenTools(intent: Intent): LongArray? {
        var waypointIds: LongArray? = null
        if (intent.hasExtra(LocusConst.INTENT_EXTRA_ITEMS_ID)) {
            waypointIds = intent.getLongArrayExtra(LocusConst.INTENT_EXTRA_ITEMS_ID)
        }
        return waypointIds
    }

    /*
	   Pick location from Locus
	  -------------------------------
	   - this feature can be used to obtain location from Locus, from same dialog (locus usually pick location).
	   Because GetLocation dialog, used in Locus need to have already initialized whole core of Locus, this dialog
	   cannot be called directly, but needs to be started from Main map screen. This screen have anyway flag
	   android:launchMode="singleTask", so there is no possibility to use startActivityForResult in this way.

	   Be careful with this function, because Locus will after "pick location" action, call new intent with
	   ON_LOCATION_RECEIVE action, which will start your activity again without "singleTask" or similar flag

	   Current functionality can be created by

	   1. register intent-filter for your activity

		<intent-filter>
			<action android:name="locus.api.android.ACTION_RECEIVE_LOCATION" />
			<category android:name="android.intent.category.DEFAULT" />
		</intent-filter>

		2. register intent receiver in your application

		check sample application, where this functionality is implemented

	 */

    fun isIntentReceiveLocation(intent: Intent): Boolean {
        return isAction(intent, LocusConst.ACTION_RECEIVE_LOCATION)
    }

    //*************************************************
    // HANDY FUNCTIONS
    //*************************************************

    /**
     * Check if received intent contains required action.
     *
     * @param intent received intent
     * @param action action that we expect
     * @return `true` if intent is valid and contains required action
     */
    private fun isAction(intent: Intent, action: String): Boolean {
        return intent.action?.equals(action) == true
    }

    /**
     * Get ID of item send over Intent container.
     *
     * @param intent received intent
     * @return ID of item or `-1` if no ID is defined
     */
    fun getItemId(intent: Intent): Long {
        return intent.getLongExtra(LocusConst.INTENT_EXTRA_ITEM_ID, -1L)
    }

    /**
     * Load full point from received "Point intent" > intent that contains pointId.
     *
     * @param ctx current context
     * @param intent received intent
     * @return loaded point object or `null` in case of any problem
     */
    @Throws(RequiredVersionMissingException::class)
    fun getPointFromIntent(ctx: Context, intent: Intent): Point? {
        val ptId = getItemId(intent)
        if (ptId >= 0) {
            LocusUtils.createLocusVersion(ctx, intent)?.let {
                return ActionBasics.getPoint(ctx, it, ptId)
            }
        }
        Logger.logD(TAG, "handleIntentPointTools($ctx, $intent), " +
                "unable to obtain point $ptId")
        return null
    }

    /**
     * Load full track from received "Track intent" > intent that contains trackId.
     *
     * @param ctx current context
     * @param intent received intent
     * @return loaded track object or `null` in case of any problem
     */
    @Throws(RequiredVersionMissingException::class)
    fun getTrackFromIntent(ctx: Context, intent: Intent): Track? {
        val trackId = getItemId(intent)
        if (trackId >= 0) {
            LocusUtils.createLocusVersion(ctx, intent)?.let {
                return ActionBasics.getTrack(ctx, it, trackId)
            }
        }
        Logger.logD(TAG, "handleIntentTrackTools($ctx, $intent), " +
                "unable to obtain track $trackId")
        return null
    }
}