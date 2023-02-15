/****************************************************************************
 *
 * Created by menion on 18/01/2019.
 * Copyright (c) 2019. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/

@file:Suppress("unused")

package locus.api.android

import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import com.asamm.loggerV2.logD
import com.asamm.loggerV2.logE
import com.asamm.loggerV2.logW
import locus.api.android.features.periodicUpdates.UpdateContainer
import locus.api.android.objects.LocusInfo
import locus.api.android.objects.LocusVersion
import locus.api.android.objects.TrackRecordProfileSimple
import locus.api.android.objects.VersionCode
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils
import locus.api.android.utils.closeQuietly
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.extra.Location
import locus.api.objects.geoData.Point
import locus.api.objects.geoData.Track
import org.json.JSONObject
import java.io.InvalidObjectException
import java.util.*

/**
 * New version of "Basic tools" optimized for quick and clear usage.
 * Most of functions are converted from `ActionTools` class.
 */
object ActionBasics {

    // tag for logger
    private const val TAG = "ActionBasics"

    //*************************************************
    // CORE TOOLS
    //*************************************************

    /**
     * Get LocusInfo container with various Locus app parameters.
     *
     * @param ctx current context
     * @param lv required Locus version
     * @return loaded info container or 'null' in case of problem
     */
    fun getLocusInfo(ctx: Context, lv: LocusVersion): LocusInfo? {
        var cursor: Cursor? = null
        try {
            when {
                lv.isVersionValid(VersionCode.UPDATE_13) -> {
                    // get scheme
                    val scheme = getProviderUriData(
                        lv, VersionCode.UPDATE_13,
                        LocusConst.CONTENT_PROVIDER_PATH_DATA + "/" + LocusConst.VALUE_LOCUS_INFO
                    )

                    // execute action
                    val data = queryData(ctx, scheme, null, LocusConst.VALUE_LOCUS_INFO)
                    if (data?.isNotEmpty() == true) {
                        return LocusInfo().apply { read(data) }
                    }
                }
                else -> {
                    // get scheme
                    val scheme = getProviderUriData(
                        lv, VersionCode.UPDATE_01,
                        LocusConst.CONTENT_PROVIDER_PATH_INFO
                    )

                    // get data
                    cursor = queryData(ctx, scheme, null)
                    if (cursor?.moveToFirst() == true) {
                        return LocusInfo.create(cursor)
                    }
                }
            }
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "getLocusInfo($ctx, $lv)" }
        } finally {
            cursor?.closeQuietly()
        }
        return null
    }

    /**
     * Get #UpdateContainer container with current fresh data based on users activity. UpdateContainer
     * is generated in Locus Map max. once per 500ms so higher frequency of request will sometimes
     * return same data.
     *
     * @param ctx current context
     * @param lv  required Locus version
     * @return loaded update container or 'null' in case of problem
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    fun getUpdateContainer(ctx: Context, lv: LocusVersion): UpdateContainer? {
        // get scheme if valid Locus is available
        val scheme = getProviderUriData(
            lv, VersionCode.UPDATE_13,
            LocusConst.CONTENT_PROVIDER_PATH_DATA + "/" + LocusConst.VALUE_UPDATE_CONTAINER
        )

        // execute action
        try {
            val data = queryData(ctx, scheme, null, LocusConst.VALUE_UPDATE_CONTAINER)
            if (data?.isNotEmpty() == true) {
                return UpdateContainer().apply { read(data) }
            }
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "getUpdateContainer($ctx, $lv)" }
        }
        return null
    }

    //*************************************************
    // LOCATION
    //*************************************************

    /**
     * Start basic "Pick location" event.
     *
     * @param act current activity
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    fun actionPickLocation(act: Activity) {
        if (!LocusUtils.isLocusAvailable(act, VersionCode.UPDATE_01)) {
            throw RequiredVersionMissingException(VersionCode.UPDATE_01)
        }

        // call action
        val intent = Intent(LocusConst.ACTION_PICK_LOCATION)
        act.startActivity(intent)
    }

    //*************************************************
    // TRACK RECORDING
    //*************************************************

    // Broadcast receivers do now show app chooser, so it's needed to give
    // them correct name of application package. For this reason, is required
    // LocusVersion object that specify which app will receive it's request

    /**
     * Main call to start track recording over API.
     *
     * @param ctx         current context
     * @param lv          version of Locus used for track record
     * @param profileName name of profile used for record (optional), otherwise last
     * used will be used for recording
     */
    @Throws(RequiredVersionMissingException::class)
    fun actionTrackRecordStart(ctx: Context, lv: LocusVersion, profileName: String? = null) {
        // create basic intent
        val intent = actionTrackRecord(
            LocusConst.ACTION_TRACK_RECORD_START, lv
        )

        // set (optional) recording profile
        if (profileName?.isNotBlank() == true) {
            intent.putExtra(LocusConst.INTENT_EXTRA_TRACK_REC_PROFILE, profileName)
        }

        // sent intent
        LocusUtils.sendBroadcast(ctx, intent, lv)
    }

    /**
     * Pause currently running track recording.
     *
     * @param ctx current context
     * @param lv version of Locus used for track record
     */
    @Throws(RequiredVersionMissingException::class)
    fun actionTrackRecordPause(ctx: Context, lv: LocusVersion) {
        LocusUtils.sendBroadcast(
            ctx,
            actionTrackRecord(LocusConst.ACTION_TRACK_RECORD_PAUSE, lv), lv
        )
    }

    /**
     * Stop currently running track recording.
     *
     * @param ctx current context
     * @param lv version of Locus used for track record
     * @param autoSave `true` to automatically save recording
     */
    @Throws(RequiredVersionMissingException::class)
    fun actionTrackRecordStop(ctx: Context, lv: LocusVersion, autoSave: Boolean) {
        // create intent
        val intent = actionTrackRecord(
            LocusConst.ACTION_TRACK_RECORD_STOP, lv
        )
        intent.putExtra(LocusConst.INTENT_EXTRA_TRACK_REC_AUTO_SAVE, autoSave)

        // sent intent
        LocusUtils.sendBroadcast(ctx, intent, lv)
    }

    // ADD WAYPOINT

    /**
     * Send broadcast to Locus to add a new waypoint to current track record.
     *
     * @param ctx      current context
     * @param lv       version of Locus used for track record
     * @param wptName  optional waypoint name
     * @param autoSave `true` to automatically save waypoint without dialog
     */
    @JvmOverloads
    @Throws(RequiredVersionMissingException::class)
    fun actionTrackRecordAddWpt(
        ctx: Context, lv: LocusVersion,
        wptName: String? = null, autoSave: Boolean = false
    ) {
        LocusUtils.sendBroadcast(
            ctx,
            prepareTrackRecordAddWptIntent(lv, wptName, autoSave), lv
        )
    }

    /**
     * Send broadcast to Locus to add a new waypoint to current track record.
     *
     * @param ctx current context
     * @param lv version of Locus used for track record
     * @param wptName name of waypoint (optional)
     * @param actionAfter action that may happen after (defined in LocusConst class)
     */
    @Throws(RequiredVersionMissingException::class)
    fun actionTrackRecordAddWpt(
        ctx: Context, lv: LocusVersion,
        wptName: String? = null, actionAfter: String
    ) {
        LocusUtils.sendBroadcast(
            ctx,
            prepareTrackRecordAddWptIntent(lv, wptName, false).apply {
                // extra parameter
                putExtra(LocusConst.INTENT_EXTRA_TRACK_REC_ACTION_AFTER, actionAfter)
            }, lv
        )
    }

    /**
     * Prepare intent that adds waypoint to current running track recording system.
     *
     * @param lv version of Locus used for track record
     * @param wptName name of waypoint (optional)
     * @param autoSave `true` to automatically save waypoint without dialog
     */
    private fun prepareTrackRecordAddWptIntent(
        lv: LocusVersion,
        wptName: String?, autoSave: Boolean
    ): Intent {
        return actionTrackRecord(
            LocusConst.ACTION_TRACK_RECORD_ADD_WPT, lv
        ).apply {
            // setup name
            if (wptName?.isNotBlank() == true) {
                putExtra(LocusConst.INTENT_EXTRA_NAME, wptName)
            }

            // setup auto-save option
            putExtra(LocusConst.INTENT_EXTRA_TRACK_REC_AUTO_SAVE, autoSave)
        }
    }

    /**
     * Private function that helps create basic intent that controls Locus.
     *
     * @param action action that should be performed
     * @param lv     version of Locus used for track record
     * @return created ready-to-use intent
     */
    @Throws(RequiredVersionMissingException::class)
    private fun actionTrackRecord(action: String, lv: LocusVersion): Intent {
        // check version (available only in Free/Pro)
        val minVersion = VersionCode.UPDATE_02.vcFree
        if (!LocusUtils.isLocusFreePro(lv, minVersion)) {
            throw RequiredVersionMissingException(minVersion)
        }

        // generate and return intent
        return Intent(action).apply {
            setPackage(lv.packageName)
        }
    }

    //*************************************************
    // TRACK RECORDING PROFILES
    //*************************************************

    /**
     * Get list of available track recording profiles currently defined in app.
     *
     * @param ctx current context
     * @param lv  version of Locus that's asked
     * @return array of profiles, where first item in array is profile ID, second item is profile name
     */
    @Throws(RequiredVersionMissingException::class)
    fun getTrackRecordingProfiles(ctx: Context, lv: LocusVersion)
            : List<TrackRecordProfileSimple> {
        // get scheme if valid Locus is available
        val profiles = ArrayList<TrackRecordProfileSimple>()
        val scheme = getProviderUriData(
            lv, VersionCode.UPDATE_09,
            LocusConst.CONTENT_PROVIDER_PATH_TRACK_RECORD_PROFILE_NAMES
        )

        // get data
        var cursor: Cursor? = null
        try {
            cursor = queryData(ctx, scheme)
            if (cursor == null || !cursor.moveToFirst()) {
                return profiles
            }

            // search in cursor for valid key
            for (i in 0 until cursor.count) {
                cursor.moveToPosition(i)
                val prof = TrackRecordProfileSimple(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getBlob(3)
                )
                profiles.add(prof)
            }
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "getTrackRecordingProfiles($ctx, $lv)" }
        } finally {
            cursor?.closeQuietly()
        }

        // return 'unknown' state
        return profiles
    }

    //*************************************************
    // NAVIGATION
    //*************************************************

    /**
     * Intent that starts navigation in Locus app based on defined target.
     *
     * @param ctx current context
     * @param name name of target
     * @param latitude latitude of target
     * @param longitude longitude of target
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    fun actionStartNavigation(
        ctx: Context,
        name: String?, latitude: Double, longitude: Double
    ) {
        // check required version
        if (!LocusUtils.isLocusAvailable(ctx, VersionCode.UPDATE_01)) {
            throw RequiredVersionMissingException(VersionCode.UPDATE_01)
        }

        // call action
        ctx.startActivity(Intent(LocusConst.ACTION_NAVIGATION_START).apply {
            name?.takeIf { it.isNotBlank() }?.let {
                putExtra(LocusConst.INTENT_EXTRA_NAME, name)
            }
            putExtra(LocusConst.INTENT_EXTRA_LATITUDE, latitude)
            putExtra(LocusConst.INTENT_EXTRA_LONGITUDE, longitude)
        })
    }

    /**
     * Intent that starts navigation in Locus app based on defined target.
     *
     * @param ctx current context
     * @param pt  point - destination
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    fun actionStartNavigation(ctx: Context, pt: Point) {
        // check required version
        if (!LocusUtils.isLocusAvailable(ctx, VersionCode.UPDATE_01)) {
            throw RequiredVersionMissingException(VersionCode.UPDATE_01)
        }

        // call Locus
        ctx.startActivity(Intent(LocusConst.ACTION_NAVIGATION_START).apply {
            IntentHelper.addPointToIntent(this, pt)
        })
    }

    /**
     * Intent that starts navigation on already existing points in Locus app.
     *
     * @param ctx current context
     * @param ptId ID of destination point
     */
    @Throws(RequiredVersionMissingException::class)
    fun actionStartNavigation(ctx: Context, ptId: Long) {
        // check required version
        if (!LocusUtils.isLocusAvailable(ctx, VersionCode.UPDATE_15)) {
            throw RequiredVersionMissingException(VersionCode.UPDATE_15)
        }

        // call Locus
        ctx.startActivity(Intent(LocusConst.ACTION_NAVIGATION_START).apply {
            putExtra(LocusConst.INTENT_EXTRA_ITEM_ID, ptId)
        })
    }

    /**
     * Intent that starts navigation in Locus to target address.
     *
     * @param ctx current context
     * @param address target address
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    fun actionStartNavigation(ctx: Context, address: String) {
        // check required version
        if (!LocusUtils.isLocusAvailable(ctx, VersionCode.UPDATE_08)) {
            throw RequiredVersionMissingException(VersionCode.UPDATE_08)
        }

        // call Locus
        ctx.startActivity(Intent(LocusConst.ACTION_NAVIGATION_START).apply {
            putExtra(LocusConst.INTENT_EXTRA_ADDRESS_TEXT, address)
        })
    }

    //*************************************************
    // GUIDING
    //*************************************************

    /**
     * Intent that starts guiding on custom location.
     *
     * @param ctx current context
     * @param name optional point name
     * @param lat latitude coordinate
     * @param lon longitude coordinate
     */
    @Throws(RequiredVersionMissingException::class)
    fun actionStartGuiding(ctx: Context, name: String?, lat: Double, lon: Double) {
        // check required version
        if (!LocusUtils.isLocusAvailable(ctx, VersionCode.UPDATE_03)) {
            throw RequiredVersionMissingException(VersionCode.UPDATE_03)
        }

        // start action
        ctx.startActivity(Intent(LocusConst.ACTION_GUIDING_START).apply {
            name?.takeIf { it.isNotBlank() }?.let {
                putExtra(LocusConst.INTENT_EXTRA_NAME, name)
            }
            putExtra(LocusConst.INTENT_EXTRA_LATITUDE, lat)
            putExtra(LocusConst.INTENT_EXTRA_LONGITUDE, lon)
        })
    }

    /**
     * Intent that starts guiding on custom point.
     *
     * @param ctx current context
     * @param pt point where to start guiding
     */
    @Throws(RequiredVersionMissingException::class)
    fun actionStartGuiding(ctx: Context, pt: Point) {
        // check required version
        if (!LocusUtils.isLocusAvailable(ctx, VersionCode.UPDATE_03)) {
            throw RequiredVersionMissingException(VersionCode.UPDATE_03)
        }

        // start action
        ctx.startActivity(Intent(LocusConst.ACTION_GUIDING_START).apply {
            IntentHelper.addPointToIntent(this, pt)
        })
    }

    /**
     * Intent that starts guiding on already existing points in Locus app.
     *
     * @param ctx current context
     * @param ptId ID of destination point
     */
    @Throws(RequiredVersionMissingException::class)
    fun actionStartGuiding(ctx: Context, ptId: Long) {
        // check required version
        if (!LocusUtils.isLocusAvailable(ctx, VersionCode.UPDATE_15)) {
            throw RequiredVersionMissingException(VersionCode.UPDATE_15)
        }

        // call Locus
        ctx.startActivity(Intent(LocusConst.ACTION_GUIDING_START).apply {
            putExtra(LocusConst.INTENT_EXTRA_ITEM_ID, ptId)
        })
    }

    //*************************************************
    // WORK WITH POINTS
    //*************************************************

    /**
     * Get full point from Locus database with all possible information, like [GeoDataExtra] object,
     * [locus.api.objects.extra.Location] or [GeoDataStyle] and others
     *
     * @param ctx  current context
     * @param ptId unique ID of point in Locus database
     * @return [Point] or `null` in case of problem
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    fun getPoint(ctx: Context, lv: LocusVersion, ptId: Long): Point? {
        // check version
        val minVersion = VersionCode.UPDATE_01.vcFree
        if (!LocusUtils.isLocusFreePro(lv, minVersion)) {
            throw RequiredVersionMissingException(minVersion)
        }

        // generate cursor
        val scheme = ContentUris.withAppendedId(
            getProviderUriData(
                lv,
                VersionCode.UPDATE_01,
                LocusConst.CONTENT_PROVIDER_PATH_WAYPOINT
            ),
            ptId
        )
        val cursor = queryData(ctx, scheme)
        if (cursor == null || !cursor.moveToFirst()) {
            logD(tag = TAG) { "getPoint($ctx, $lv, $ptId), no such point exists" }
            return null
        }

        // handle result
        try {
            return Point().apply { read(cursor.getBlob(1)) }
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "getPoint($ctx, $ptId)" }
        } finally {
            cursor.closeQuietly()
        }
        return null
    }

    /**
     * Get ID of point stored in Locus internal database. To search for point ID
     * is used it's name. Because search is executed on SQLite database, it is possible
     * to also use wildcards.
     * <br></br><br></br>
     * Examples:
     * <br></br><br></br>
     * 1. search for point that has exact name "Cinema", just write "Cinema" as ptName
     * <br></br>
     * 2. search for point that starts with "Cinema", just write "Cinema%" as ptName
     * <br></br>
     * 3. search for point that contains word "cinema", just write "%cinema%" as ptName
     *
     * @param ctx    current context
     * @param ptName name (or part of name) you search
     * @return array of point ids. Returns `null` in case, any problem happen, or
     * empty array if no result was found
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    fun getPointsId(ctx: Context, lv: LocusVersion, ptName: String): LongArray {
        // check version (available only in Free/Pro)
        val minVersion = VersionCode.UPDATE_03.vcFree
        if (!LocusUtils.isLocusFreePro(lv, minVersion)) {
            throw RequiredVersionMissingException(minVersion)
        }

        // generate cursor
        val scheme = getProviderUriData(
            lv, VersionCode.UPDATE_03,
            LocusConst.CONTENT_PROVIDER_PATH_WAYPOINT
        )
        val cursor = queryData(
            ctx, scheme,
            "getWaypointId", arrayOf(ptName)
        )
        if (cursor == null) {
            logD(tag = TAG) { "getPointId($ctx, $lv, $ptName), point with such name does not exists in database" }
            return LongArray(0)
        }

        // handle result
        try {
            val result = LongArray(cursor.count)
            for (i in 0 until result.size) {
                cursor.moveToPosition(i)
                result[i] = cursor.getLong(0)
            }
            return result
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "getPointId($ctx, $lv, $ptName)" }
        } finally {
            cursor.closeQuietly()
        }
        return LongArray(0)
    }

    /**
     * Get ID of all points that are currently visible (on the map) in certain defined location
     * and it's surrounding area (define by 'radius').
     *
     * @param ctx current context
     * @param lv current active version
     * @param loc base center location
     * @param limit (max) number of points. Value is internally limited to 100 points max.
     * @param maxRadius max distance from defined based location (in meters)
     * @return list of found points
     */
    fun getPointsId(
        ctx: Context, lv: LocusVersion, loc: Location,
        limit: Int = 1, maxRadius: Double = Double.MAX_VALUE
    ): LongArray {
        // check version (available only in Free/Pro)
        val minVersion = VersionCode.UPDATE_15.vcFree
        if (!LocusUtils.isLocusFreePro(lv, minVersion)) {
            throw RequiredVersionMissingException(minVersion)
        }

        // generate cursor
        val scheme = getProviderUriData(
            lv, VersionCode.UPDATE_15,
            LocusConst.CONTENT_PROVIDER_PATH_WAYPOINT
        )
        val sel = JSONObject()
            .put("type", "nearest")
            .put("lon", loc.longitude)
            .put("lat", loc.latitude)
            .put("limit", limit)
            .put("maxRadius", maxRadius)
            .toString()
        val cursor = queryData(ctx, scheme, sel)
        if (cursor == null) {
            logD(tag = TAG) { "getPointsId($ctx, $lv, $loc, $limit, $maxRadius), no points found in area" }
            return LongArray(0)
        }

        // handle result
        try {
            val result = LongArray(cursor.count)
            for (i in 0 until result.size) {
                cursor.moveToPosition(i)
                result[i] = cursor.getLong(0)
            }
            return result
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "getPointsId($ctx, $lv, $loc, $limit, $maxRadius)" }
        } finally {
            cursor.closeQuietly()
        }
        return LongArray(0)
    }

    /**
     * Update point in Locus.
     *
     * @param ctx                current context
     * @param pt                point to update. Do not modify point's ID value, because it's key to update
     * @param forceOverwrite     if set to `true`, new point will completely rewrite all
     * user's data (do not use if necessary). If set to `false`, Locus will handle update based on user's
     * settings (if user have defined "keep values", it will keep it)
     * @param loadAllGcWaypoints allow to force Locus to load all Geocache points (of course
     * if point is Geocache and is visible on map)
     * @return number of affected points
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    fun updatePoint(
        ctx: Context, lv: LocusVersion,
        pt: Point, forceOverwrite: Boolean, loadAllGcWaypoints: Boolean = false
    ): Int {
        // check version (available only in Free/Pro)
        val minVersion = VersionCode.UPDATE_01.vcFree
        if (!LocusUtils.isLocusFreePro(lv, minVersion)) {
            throw RequiredVersionMissingException(minVersion)
        }

        // generate cursor
        val scheme = getProviderUriData(
            lv, VersionCode.UPDATE_01,
            LocusConst.CONTENT_PROVIDER_PATH_WAYPOINT
        )

        // define empty cursor
        val cv = ContentValues().apply {
            put("waypoint", pt.asBytes)
            put("forceOverwrite", forceOverwrite)
            put("loadAllGcWaypoints", loadAllGcWaypoints)
        }
        return ctx.contentResolver.update(scheme, cv, null, null)
    }

    /**
     * Allows to display whole detail screen of certain point.
     *
     * @param ctx   current context
     * @param lv    LocusVersion we call
     * @param ptId ID of points we wants to display
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    fun displayPointScreen(ctx: Context, lv: LocusVersion, ptId: Long) {
        displayPointScreen(ctx, lv, ptId, "")
    }

    /**
     * Allows to display whole detail screen of certain point.
     *
     * @param ctx             current context
     * @param lv              LocusVersion we call
     * @param ptId           ID of points we wants to display
     * @param packageName     this value is used for creating intent that
     * will be called in callback (for example com.super.application)
     * @param className       the name of the class inside of com.super.application
     * that implements the component (for example com.super.application.Main)
     * @param returnDataName  String under which data will be stored. Can be
     * retrieved by String data = getIntent.getStringExtra("returnData");
     * @param returnDataValue String under which data will be stored. Can be
     * retrieved by String data = getIntent.getStringExtra("returnData");
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    fun displayPointScreen(
        ctx: Context, lv: LocusVersion, ptId: Long,
        packageName: String, className: String, returnDataName: String, returnDataValue: String
    ) {
        // prepare callback
        val callback = GeoDataExtra.generateCallbackString(
            "", packageName, className, returnDataName, returnDataValue
        )

        // call intent
        displayPointScreen(ctx, lv, ptId, callback)
    }

    /**
     * Allows to display whole detail screen of certain point.
     *
     * @param ctx      current context
     * @param lv       LocusVersion we call
     * @param ptId    ID of points we wants to display
     * @param callback generated callback (optional)
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    private fun displayPointScreen(ctx: Context, lv: LocusVersion, ptId: Long, callback: String?) {
        // check version (available only in Free/Pro)
        if (!LocusUtils.isLocusFreePro(lv, VersionCode.UPDATE_07.vcFree)) {
            throw RequiredVersionMissingException(VersionCode.UPDATE_07)
        }

        // call intent
        val intent = Intent(LocusConst.ACTION_DISPLAY_POINT_SCREEN)
        intent.putExtra(LocusConst.INTENT_EXTRA_ITEM_ID, ptId)
        if (callback != null && callback.isNotEmpty()) {
            intent.putExtra(Point.TAG_EXTRA_CALLBACK, callback)
        }
        ctx.startActivity(intent)
    }

    //*************************************************
    // WORK WITH TRACKS
    //*************************************************

    /**
     * Get full track from Locus database with all possible information, like
     * [GeoDataExtra] object or [GeoDataStyle] and others
     *
     * @param ctx     current context
     * @param trackId unique ID of track in Locus database
     * @return [locus.api.objects.extra.Track] or *null* in case of problem
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    fun getTrack(ctx: Context, lv: LocusVersion, trackId: Long): Track? {
        // check version
        val minVersion = VersionCode.UPDATE_10.vcFree
        if (!LocusUtils.isLocusFreePro(lv, minVersion)) {
            throw RequiredVersionMissingException(minVersion)
        }

        // generate cursor
        var scheme = getProviderUriData(
            lv, VersionCode.UPDATE_10,
            LocusConst.CONTENT_PROVIDER_PATH_TRACK
        )
        scheme = ContentUris.withAppendedId(scheme, trackId)
        val cursor = queryData(ctx, scheme)
        if (cursor == null || !cursor.moveToFirst()) {
            logW(tag = TAG) { "getTrack($ctx, $lv, $trackId), " + "'cursor' in not valid" }
            return null
        }

        // handle result
        try {
            return Track().apply {
                read(cursor.getBlob(1))
            }
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "getTrack($ctx, $trackId)" }
        } finally {
            cursor.closeQuietly()
        }
        return null
    }

    /**
     * Supported export formats.
     */
    enum class FileFormat {

        FIT, GPX, KML, TCX
    }

    /**
     * Get track exported into file stored in device. Final file where track will be exported receive
     * calling activity as result under defined [requestCode].
     *
     * How to use it:
     * https://github.com/asamm/locus-api/wiki/Work-with-known-track#get-track-in-the-specific-file-format
     *
     * @param act that starts request
     * @param lv current location version we work with
     * @param requestCode code of request
     * @param trackId ID of track we wants to export
     * @param format export format
     * @param formatExtra extra format parameters
     */
    fun getTrackInFormat(
        act: Activity,
        lv: LocusVersion? = LocusUtils.getActiveVersion(act),
        requestCode: Int,
        trackId: Long, format: FileFormat, formatExtra: String = ""
    ) {
        // check version
        val minVersion = VersionCode.UPDATE_16.vcFree
        if (!LocusUtils.isLocusFreePro(lv, minVersion)) {
            throw RequiredVersionMissingException(minVersion)
        }

        // execute request
        act.startActivityForResult(
            prepareTrackInFormatIntent(
                LocusConst.ACTION_GET_TRACK_AS_FILE,
                lv!!, trackId, format, formatExtra
            ), requestCode
        )
    }

//    /**
//     * Get track exported into file stored in device. Final file where track will be exported receive
//     * BroadcastReceiver defined you add-on manifest under [resultHandler] action.
//     *
//     * How to use it:
//     * https://github.com/asamm/locus-api/wiki/Work-with-known-track#get-track-in-the-specific-file-format
//     *
//     * For now disabled due to problem with work with FileProvider over Broadcast
//     * https://stackoverflow.com/questions/24982210/android-using-uri-permissions-with-broadcast
//     *
//     * @param ctx context that starts request
//     * @param lv current location version we work with
//     * @param resultHandler receiver action that receive result
//     * @param resultExtra extra payload parameter that will return back to caller
//     * @param trackId ID of track we wants to export
//     * @param format export format
//     * @param formatExtra extra format parameters
//     */
//    fun getTrackInFormat(ctx: Context,
//            lv: LocusVersion? = LocusUtils.getActiveVersion(ctx),
//            resultHandler: String, resultExtra: String = "",
//            trackId: Long, format: FileFormat, formatExtra: String = "") {
//        // check version
//        val minVersion = VersionCode.UPDATE_16.vcFree
//        if (!LocusUtils.isLocusFreePro(lv, minVersion)) {
//            throw RequiredVersionMissingException(minVersion)
//        }
//
//        // execute request
//        ctx.sendBroadcast(prepareTrackInFormatIntent(
//                LocusConst.ACTION_GET_TRACK_AS_FILE_BR,
//                lv!!, trackId, format, formatExtra).apply {
//            putExtra("resultHandler", resultHandler)
//            putExtra("resultExtra", resultExtra)
//        })
//    }

    /**
     * Prepare intent for "get track in certain format" request.
     *
     * @param lv current location version we work with
     * @param trackId ID of track we wants to export
     * @param format export format
     * @param formatExtra extra format parameters
     */
    private fun prepareTrackInFormatIntent(
        action: String, lv: LocusVersion,
        trackId: Long, format: FileFormat, formatExtra: String = ""
    ): Intent {
        return Intent(action).apply {
            setPackage(lv.packageName)
            putExtra("trackId", trackId)
            putExtra("format", format.name.lowercase())
            putExtra("formatExtra", formatExtra)
        }
    }

    //*************************************************
    // WMS FUNCTIONS
    //*************************************************

    /**
     * Add WMS map call allow 3rd party application, add web address directly to list of WMS
     * services in Map Manager screen / WMS tab
     *
     * @param context current context
     * @param wmsUrl Url address to WMS service
     */
    @Throws(RequiredVersionMissingException::class, InvalidObjectException::class)
    fun callAddNewWmsMap(context: Context, wmsUrl: String) {
        // check availability and start action
        if (!LocusUtils.isLocusAvailable(context, VersionCode.UPDATE_01)) {
            throw RequiredVersionMissingException(VersionCode.UPDATE_01)
        }
        if (TextUtils.isEmpty(wmsUrl)) {
            throw InvalidObjectException("WMS Url address \'$wmsUrl\', is not valid!")
        }

        // call intent with WMS url
        val intent = Intent(LocusConst.ACTION_ADD_NEW_WMS_MAP)
        intent.putExtra(LocusConst.INTENT_EXTRA_ADD_NEW_WMS_MAP_URL, wmsUrl)
        context.startActivity(intent)
    }

    //*************************************************
    // CONTENT OF LOCUS STORE
    //*************************************************

    /**
     * Allows to check if item with known ID is already purchased by user.
     *
     * @param ctx current context
     * @param lv version of Locus that's asked
     * @param itemId know ID of item
     * @return ItemPurchaseState state of purchase
     */
    @Throws(RequiredVersionMissingException::class)
    fun getItemPurchaseState(ctx: Context, lv: LocusVersion, itemId: Long): Int {
        // get scheme if valid Locus is available
        var scheme = getProviderUriData(
            lv, VersionCode.UPDATE_06,
            LocusConst.CONTENT_PROVIDER_PATH_ITEM_PURCHASE_STATE
        )
        scheme = ContentUris.withAppendedId(scheme, itemId)

        // get data
        var cursor: Cursor? = null
        try {
            cursor = queryData(ctx, scheme, null)
            if (cursor?.moveToFirst() != true) {
                return LocusConst.PURCHASE_STATE_UNKNOWN
            }

            // search for a valid key
            for (i in 0 until cursor.count) {
                cursor.moveToPosition(i)
                val key = cursor.getString(0)
                if (key == "purchaseState") {
                    return cursor.getInt(1)
                }
            }
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "getItemPurchaseState($ctx, $lv, $itemId)" }
        } finally {
            cursor?.closeQuietly()
        }

        // return 'unknown' state
        return LocusConst.PURCHASE_STATE_UNKNOWN
    }

    /**
     * Start Locus and display certain item from Store defined by it's unique ID.
     *
     * @param ctx    current context
     * @param lv     known LocusVersion
     * @param itemId known item ID
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    fun displayLocusStoreItemDetail(ctx: Context, lv: LocusVersion?, itemId: Long) {
        // check if application is available
        if (lv == null || !lv.isVersionValid(VersionCode.UPDATE_12)) {
            logW(tag = TAG) { "displayLocusStoreItemDetail(), " + "invalid Locus version" }
            throw RequiredVersionMissingException(VersionCode.UPDATE_12)
        }

        // call Locus
        ctx.startActivity(Intent(LocusConst.ACTION_DISPLAY_STORE_ITEM).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(LocusConst.INTENT_EXTRA_ITEM_ID, itemId)
        })
    }

    //*************************************************
    // WORK WITH CONTENT PROVIDERS
    //*************************************************

    /**
     * Query data from defined Uri.
     *
     * @param ctx current context
     * @param uri Uri to load data from
     * @return valid cursor with data or 'null' in case of empty or invalid cursor
     */
    fun queryData(
        ctx: Context,
        uri: Uri,
        selection: String? = null,
        args: Array<String>? = null
    ): Cursor? {
        // generate cursor
        val cursor = ctx.contentResolver.query(uri, null, selection, args, null)
        if (cursor == null || cursor.count == 0) {
            logE(tag = TAG) {
                "queryData(" + ctx + ", " + uri + "), " +
                        "invalid or empty cursor received"
            }
            return null
        }
        return cursor
    }

    /**
     * Query data from defined Uri and return loaded byte array content.
     *
     * @param ctx     current context
     * @param uri     Uri to load data from
     * @param keyName key under which we expect received data
     * @return valid cursor with data or 'null' in case of empty or invalid cursor
     */
    private fun queryData(ctx: Context, uri: Uri, selection: String?, keyName: String): ByteArray? {
        var cursor: Cursor? = null
        try {
            // get cursor data
            cursor = queryData(ctx, uri, selection)
            if (cursor == null || !cursor.moveToFirst()) {
                return null
            }

            // handle query
            val key = cursor.getString(0)
            if (key == keyName) {
                return cursor.getBlob(1)
            }
        } finally {
            cursor?.closeQuietly()
        }

        // no data loaded
        logW(tag = TAG) {
            "queryData(" + ctx + ", " + uri + ", " + keyName + "), " +
                    "received data does not contains required key"
        }
        return null
    }

    /**
     * Get Uri to certain content in Locus Map data system.
     *
     * @param lv         Locus version we request to
     * @param requiredVc required minimal Locus version
     * @param path       path to data
     * @return generated Uri
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    private fun getProviderUriData(lv: LocusVersion, requiredVc: VersionCode, path: String): Uri {
        return getProviderUri(
            lv, requiredVc,
            LocusConst.CONTENT_PROVIDER_AUTHORITY_DATA,
            path
        )
    }

    /**
     * Get Uri to certain content in Locus Map geocaching system.
     *
     * @param lv         Locus version we request to
     * @param requiredVc required minimal Locus version
     * @param path       path to data
     * @return generated Uri
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    fun getProviderUrlGeocaching(lv: LocusVersion, requiredVc: VersionCode, path: String): Uri {
        return getProviderUri(
            lv, requiredVc,
            LocusConst.CONTENT_PROVIDER_AUTHORITY_GEOCACHING,
            path
        )
    }

    /**
     * Get Uri to certain content in Locus Map app.
     *
     * @param lv         Locus version we request to
     * @param requiredVc required minimal Locus version
     * @param provider   provider for data
     * @param path       path to data
     * @return generated Uri
     * @throws RequiredVersionMissingException if Locus in required version is missing
     */
    @Throws(RequiredVersionMissingException::class)
    fun getProviderUri(
        lv: LocusVersion, requiredVc: VersionCode,
        provider: String, path: String
    ): Uri {
        // check URI parts ( should not happen, just check )
        if (provider.isEmpty() || path.isEmpty()) {
            logW(tag = TAG) { "getProviderUri(), " + "invalid 'authority' or 'path'parameters" }
            throw RequiredVersionMissingException(requiredVc)
        }

        // check if application is available
        if (!lv.isVersionValid(requiredVc)) {
            logW(tag = TAG) { "getProviderUri(), " + "invalid Locus version" }
            throw RequiredVersionMissingException(requiredVc)
        }

        // generate content provider by type
        val sb = StringBuilder()
        when {
            lv.isVersionFree -> sb.append("content://menion.android.locus.free")
            lv.isVersionPro -> sb.append("content://menion.android.locus.pro")
            lv.isVersionGis -> sb.append("content://menion.android.locus.gis")
            else -> {
                logW(tag = TAG) {
                    "getProviderUri(), " +
                            "unknown Locus version:" + lv
                }
                throw RequiredVersionMissingException(requiredVc)
            }
        }

        // finish URI
        return Uri.parse(sb.append(".").append(provider).append("/").append(path).toString())
    }
}
