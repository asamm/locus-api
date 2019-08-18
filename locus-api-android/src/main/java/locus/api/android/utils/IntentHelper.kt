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
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.extra.Location
import locus.api.objects.geoData.Point
import locus.api.objects.geoData.Track
import locus.api.utils.Logger

/**
 * Methods useful for handling received results from Locus based app.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object IntentHelper {

    // tag for logger
    private const val TAG = "ResponseHelper"

    //*************************************************
    // MAIN FUNCTIONS
    //*************************************************

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
     * Check if received intent is response on MAIN_FUNCTION_GC intent.
     *
     * @param intent received intent
     * @return `true` if intent is base on MAIN_FUNCTION_GC parameter
     */
    fun isIntentMainFunctionGc(intent: Intent): Boolean {
        return isAction(intent, LocusConst.INTENT_ITEM_MAIN_FUNCTION_GC)
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
            handler: OnIntentReceived) {
        handleIntentGeneral(ctx, intent, LocusConst.INTENT_ITEM_MAIN_FUNCTION, handler)
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
            handler: OnIntentReceived) {
        handleIntentGeneral(ctx, intent, LocusConst.INTENT_ITEM_MAIN_FUNCTION_GC, handler)
    }

    //*************************************************
    // GET LOCATION INTENT
    //*************************************************

    // more info
    // https://github.com/asamm/locus-api/wiki/App-as-location-source

    /**
     * Check if received intent is "Get location" request.
     *
     * In case of positive result, it is necessary to return result with valid location object.
     * Received intent also contains user GPS and Map center location bundled.
     *
     * @param intent received intent.
     */
    fun isIntentGetLocation(intent: Intent): Boolean {
        return isAction(intent, LocusConst.INTENT_ITEM_GET_LOCATION)
    }

    /**
     * Handle received intent from section SEARCH_LIST.
     *
     * @param intent  received intent
     * @param handler handler for events
     * @throws NullPointerException exception if any required data are missing
     */
    @Throws(NullPointerException::class)
    fun handleIntentGetLocation(ctx: Context, intent: Intent, handler: OnIntentReceived) {
        handleIntentGeneral(ctx, intent, LocusConst.INTENT_ITEM_GET_LOCATION, handler)
    }

    /**
     * Return generated location "get location" request.
     *
     * @param act current activity
     * @param name optional name of received location
     * @param loc location object
     */
    fun sendGetLocationData(act: Activity, name: String? = null, loc: Location) {
        act.setResult(Activity.RESULT_OK, Intent().apply {
            if (name?.isNotBlank() == true) {
                putExtra(LocusConst.INTENT_EXTRA_NAME, name)
            }
            putExtra(LocusConst.INTENT_EXTRA_LOCATION, loc.asBytes)
        })
        act.finish()
    }

    //*************************************************
    // POINT TOOLS
    //*************************************************

    // more info
    // https://github.com/asamm/locus-api/wiki/Own-function-in-Point-screen

    /**
     * Check if received intent is result of "Point tools" click.
     *
     * @param intent received intent
     */
    fun isIntentPointTools(intent: Intent): Boolean {
        return isAction(intent, LocusConst.INTENT_ITEM_POINT_TOOLS)
    }

    //*************************************************
    // POINTS TOOLS
    //*************************************************

    // more info
    // https://github.com/asamm/locus-api/wiki/Own-function-in-Points-screen

    /**
     * Check if received intent is result of "Points tools" click.
     *
     * @param intent received intent
     */
    fun isIntentPointsTools(intent: Intent): Boolean {
        return isAction(intent, LocusConst.INTENT_ITEM_POINTS_SCREEN_TOOLS)
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

    // more info
    // https://github.com/asamm/locus-api/wiki/App-as-Search-source

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
    fun handleIntentSearchList(ctx: Context, intent: Intent, handler: OnIntentReceived) {
        handleIntentGeneral(ctx, intent, LocusConst.INTENT_ITEM_SEARCH_LIST, handler)
    }

    //*************************************************
    // PICK LOCATION
    //*************************************************

    /**
     * Check if received intent is result of "Get location" request.
     *
     * @param intent received intent
     * @return `true· if this is expected result
     */
    fun isIntentReceiveLocation(intent: Intent): Boolean {
        return isAction(intent, LocusConst.ACTION_RECEIVE_LOCATION)
    }

    //*************************************************
    // GENERAL INTENT HANDLER
    //*************************************************

    @Throws(NullPointerException::class)
    private fun handleIntentGeneral(ctx: Context, intent: Intent, action: String,
            handler: OnIntentReceived) {
        // check intent itself
        if (!isAction(intent, action)) {
            handler.onFailed()
            return
        }

        // get version and handle received data
        LocusUtils.createLocusVersion(ctx, intent)?.let {
            handler.onReceived(it,
                    getLocationFromIntent(intent, LocusConst.INTENT_EXTRA_LOCATION_GPS),
                    getLocationFromIntent(intent, LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER))
        } ?: {
            handler.onFailed()
        }()
    }

    /**
     * Receiver for basic intents.
     */
    interface OnIntentReceived {
        /**
         * When intent really contain location, result is returned by this function
         *
         * @param lv           version of Locus
         * @param locGps       if gpsEnabled is true, variable contain location, otherwise `null`
         * @param locMapCenter contain current map center location
         */
        fun onReceived(lv: LocusVersion, locGps: Location?, locMapCenter: Location?)

        fun onFailed()
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
    fun isAction(intent: Intent, action: String): Boolean {
        return intent.action?.equals(action) == true
    }

    /**
     * Get ID of item send over intent container.
     *
     * @param intent received intent
     * @return ID of item or `-1` if no ID is defined
     */
    fun getItemId(intent: Intent): Long {
        return intent.getLongExtra(LocusConst.INTENT_EXTRA_ITEM_ID, -1L)
    }

    /**
     * Get list of IDs registered in intent container.
     *
     * @param intent received intent
     * @return ID of items or `null` if no IDs are defined
     */
    fun getItemsId(intent: Intent): LongArray? {
        if (intent.hasExtra(LocusConst.INTENT_EXTRA_ITEMS_ID)) {
            return intent.getLongArrayExtra(LocusConst.INTENT_EXTRA_ITEMS_ID)
        }
        return null
    }

    /**
     * Get location object from received intent.
     *
     * @param intent    received intent
     * @param key name of 'extra' under which should be location stored in intent
     * @return location from intent or 'null' if intent has no location attached
     */
    fun getLocationFromIntent(intent: Intent, key: String): Location? {
        // check if intent has required extra parameter
        if (!intent.hasExtra(key)) {
            return null
        }

        // convert data to valid Location object
        return Location().apply { read(intent.getByteArrayExtra(key)) }
    }

    /**
     * Attach point into intent, that may be send to Locus application.
     *
     * @param intent created intent container
     * @param pt    point to attach
     */
    fun addPointToIntent(intent: Intent, pt: Point) {
        intent.putExtra(LocusConst.INTENT_EXTRA_POINT, pt.asBytes)
    }

    /**
     * Load full point from received "Point intent" > intent that contains pointId or full point.
     *
     * @param ctx current context
     * @param intent received intent
     * @return loaded point object or `null` in case of any problem
     */
    @Throws(RequiredVersionMissingException::class)
    fun getPointFromIntent(ctx: Context, intent: Intent): Point? {
        try {
            // try to load point based on it's ID
            val ptId = getItemId(intent)
            if (ptId >= 0) {
                LocusUtils.createLocusVersion(ctx, intent)?.let {
                    return ActionBasics.getPoint(ctx, it, ptId)
                }
            }

            // try load full included point
            if (intent.hasExtra(LocusConst.INTENT_EXTRA_POINT)) {
                return Point().apply { read(intent.getByteArrayExtra(LocusConst.INTENT_EXTRA_POINT)) }
            }
        } catch (e: Exception) {
            Logger.logE(TAG, "getPointFromIntent($intent)", e)
        }
        return null
    }

    /**
     * Get ID of points defined in intent.
     *
     * @param intent received intent
     * @return list of points ID or null if no IDs are defined
     */
    fun getPointsFromIntent(intent: Intent): LongArray? {
        return getItemsId(intent)
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