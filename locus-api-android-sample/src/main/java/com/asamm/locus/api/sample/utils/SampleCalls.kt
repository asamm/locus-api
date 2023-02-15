/*
 * Copyright 2011, Asamm soft, s.r.o.
 *
 * This file is part of LocusAddonPublicLibSample.
 *
 * LocusAddonPublicLibSample is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LocusAddonPublicLibSample is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LocusAddonPublicLibSample.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.asamm.locus.api.sample.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.asamm.locus.api.sample.R
import com.asamm.loggerV2.logD
import com.asamm.loggerV2.logI
import locus.api.android.ActionBasics
import locus.api.android.ActionDisplayPoints
import locus.api.android.ActionDisplayVarious
import locus.api.android.ActionFiles
import locus.api.android.features.sendToApp.SendMode
import locus.api.android.features.sendToApp.SendToAppHelper
import locus.api.android.features.sendToApp.tracks.SendTrack
import locus.api.android.features.sendToApp.tracks.SendTracks
import locus.api.android.objects.LocusVersion
import locus.api.android.objects.PackPoints
import locus.api.android.objects.VersionCode
import locus.api.android.utils.LocusUtils
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.extra.Location
import locus.api.objects.geoData.Circle
import locus.api.objects.geoData.Point
import locus.api.objects.geoData.Track
import locus.api.objects.geocaching.GeocachingData
import locus.api.objects.geocaching.GeocachingWaypoint
import locus.api.objects.styles.GeoDataStyle
import java.io.File
import java.util.*

object SampleCalls {

    // tag for logger
    private const val TAG = "SampleCalls"

    // ID for "onDisplay" event
    const val EXTRA_ON_DISPLAY_ACTION_ID = "myOnDisplayExtraActionId"
    const val EXTRA_CALLBACK_ID = "extraCallbackId"

    /**
     * Temporary file used for testing of import feature.
     */
    private fun getTempGpxFile(
        ctx: Context,
        @Suppress("SameParameterValue") content: String
    ): File {
        return File(ctx.externalCacheDir, "temporary_path.gpx").apply {
            writeText(content)
        }
    }

    //*************************************************
    // BASIC CHECKS
    //*************************************************

    /**
     * Write stats about app into log.
     *
     * @param ctx current context
     */
    fun callDisplayLocusMapInfo(ctx: Context) {
        // iterate over versions
        logI(tag = TAG) { "Locus versions:" }
        for (version in LocusUtils.getAvailableVersions(ctx)) {
            logI(tag = TAG) { "  version: $version" }
        }

        // active version
        logI(tag = TAG) { "Active version:" }
        logI(tag = TAG) { "  version: " + LocusUtils.getActiveVersion(ctx) }

        // notify
        Toast.makeText(ctx, "Check log for result", Toast.LENGTH_SHORT).show()
    }

    fun getRootDirectory(ctx: Context, lv: LocusVersion): String? {
        return try {
            ActionBasics.getLocusInfo(ctx, lv)!!.rootDir
        } catch (e: RequiredVersionMissingException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Check if found version is running.
     *
     * @param ctx current context
     * @param lv  received Locus version
     * @return `true` if app is running
     */
    fun isRunning(ctx: Context, lv: LocusVersion): Boolean {
        return try {
            ActionBasics.getLocusInfo(ctx, lv)!!.isRunning
        } catch (e: RequiredVersionMissingException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Check if periodic updates are enabled.
     *
     * @param ctx current context
     * @param lv  received Locus version
     * @return `true` if updates are enabled
     */
    fun isPeriodicUpdateEnabled(ctx: Context, lv: LocusVersion): Boolean {
        return try {
            ActionBasics.getLocusInfo(ctx, lv)!!.isPeriodicUpdatesEnabled
        } catch (e: RequiredVersionMissingException) {
            e.printStackTrace()
            false
        }
    }

    //*************************************************
    // DISPLAY POINTS
    //*************************************************

    /**
     * Send single point into Locus application.
     *
     * @param ctx current context
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    @Throws(RequiredVersionMissingException::class)
    fun callSendOnePoint(ctx: Context) {
        // generate pack
        val pw = PackPoints("callSendOnePoint")
        pw.addPoint(generateWaypoint(0))

        // send data
        val send = ActionDisplayPoints.sendPack(ctx, pw, ActionDisplayVarious.ExtraAction.IMPORT)
        logD(tag = TAG) {
            "callSendOnePoint(), " +
                    "send:" + send
        }
    }

    /**
     * Send more points - LIMIT DATA TO MAX 1000 (really max 1500), more cause troubles. It easy and fast method,
     * but depend on data size, so intent with lot of geocaches will be really limited.
     *
     * @param ctx current context
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    @Throws(RequiredVersionMissingException::class)
    fun callSendMorePoints(ctx: Context) {
        // generate pack with points
        val pw = PackPoints("callSendMorePoints")
        for (i in 0..999) {
            pw.addPoint(generateWaypoint(i))
        }

        // send data
        val send = ActionDisplayPoints.sendPack(ctx, pw, ActionDisplayVarious.ExtraAction.IMPORT)
        logD(tag = TAG) { "callSendMorePoints(), send:$send" }
    }

    /**
     * Send single point that will be immediately visible on a map. Point will also contains an icon.
     *
     * @param ctx current context
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    @Throws(RequiredVersionMissingException::class)
    fun callSendOnePointWithIcon(ctx: Context) {
        // prepare pack with point (with icon)
        val pw = PackPoints("callSendOnePointWithIcon")
        pw.bitmap = BitmapFactory.decodeResource(
            ctx.resources, R.drawable.ic_launcher
        )
        pw.addPoint(generateWaypoint(0))

        // send data
        val send = ActionDisplayPoints.sendPack(ctx, pw, ActionDisplayVarious.ExtraAction.CENTER)
        logD(tag = TAG) { "callSendOnePointWithIcon(), send:$send" }
    }

    /**
     * Similar to previous method. Every PackPoints object have defined icon that is applied on
     * every points. So if you want to send more points with various icons, you have to define for
     * every pack specific PackPoints object
     *
     * @param ctx current context
     */
    @Throws(RequiredVersionMissingException::class)
    fun callSendMorePointsWithIcons(ctx: Context) {
        // container for pack of points
        val data = ArrayList<PackPoints>()

        // prepare first pack
        val pd1 = PackPoints("test01")
        val es1 = GeoDataStyle()
        es1.setIconStyle("http://www.googlemapsmarkers.com/v1/009900/", 1.0f)
        pd1.extraStyle = es1
        for (i in 0..99) {
            pd1.addPoint(generateWaypoint(i))
        }
        data.add(pd1)

        // prepare second pack with different icon
        val pd2 = PackPoints("test02")
        val es2 = GeoDataStyle()
        es2.setIconStyle("http://www.googlemapsmarkers.com/v1/990000/", 1.0f)
        pd2.extraStyle = es2
        for (i in 0..99) {
            pd2.addPoint(generateWaypoint(i))
        }
        data.add(pd2)

        // send data
        val send = ActionDisplayPoints.sendPacks(ctx, data, ActionDisplayVarious.ExtraAction.CENTER)
        logD(tag = TAG) { "callSendMorePointsWithIcons(), send:$send" }
    }

    /**
     * Display single geocache point on the map.
     *
     * @param ctx current context
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    @Throws(RequiredVersionMissingException::class)
    fun callSendOnePointGeocache(ctx: Context) {
        // prepare geocache
        val pd = PackPoints("callSendOnePointGeocache")
        pd.addPoint(generateGeocache(0))

        // send data
        val send = ActionDisplayPoints.sendPack(ctx, pd, ActionDisplayVarious.ExtraAction.CENTER)
        logD(tag = TAG) { "callSendOnePointGeocache(), send:$send" }
    }

    /**
     * Send and display more geocaches on the screen at once. Limit here is much more tight! Intent have limit on
     * data size (around 2MB, so if you want to send more geocaches, don't rather use this method.
     *
     * @param ctx current context
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    @Throws(RequiredVersionMissingException::class)
    fun callSendMorePointsGeocacheIntentMethod(ctx: Context) {
        // prepare geocaches
        val pw = PackPoints("test6")
        for (i in 0..99) {
            pw.addPoint(generateGeocache(i))
        }

        // send data
        val send = ActionDisplayPoints.sendPack(ctx, pw, ActionDisplayVarious.ExtraAction.CENTER)
        logD(tag = TAG) { "callSendMorePointsGeocacheIntentMethod(), send:$send" }
    }

    /**
     * Send more geocaches with method, that store byte[] data in raw file and send locus link to this file.
     * This method is useful in case of bigger number of caches that already has some troubles with
     * method over intent.
     *
     * @param ctx current context
     */
    @Throws(RequiredVersionMissingException::class)
    fun callSendMorePointsGeocacheFileMethod(ctx: Context) {
        val version = LocusUtils.getActiveVersion(ctx) ?: return

        // get file to share
        val file = SendToAppHelper.getCacheFile(ctx)

        // prepare data
        val pw = PackPoints("test07")
        for (i in 0..999) {
            pw.addPoint(generateGeocache(i))
        }
        val data = ArrayList<PackPoints>()
        data.add(pw)

        // send data
        val send = if (version.isVersionValid(VersionCode.UPDATE_15)) {
            // send file via FileProvider, you don't need WRITE_EXTERNAL_STORAGE permission for this
            val uri = FileProvider.getUriForFile(
                ctx,
                ctx.getString(R.string.file_provider_authority),
                file
            )
            ActionDisplayPoints.sendPacksFile(ctx, version, data, file, uri)
        } else {
            // send file old way, you need WRITE_EXTERNAL_STORAGE permission for this
            ActionDisplayPoints.sendPacksFile(ctx, version, data, file)
        }
        logD(tag = TAG) { "callSendMorePointsGeocacheFileMethod(), send: $send" }
    }

    /**
     * Display single point with special "onClick" event. Such point when shown, will call back to this application.
     * You may use this for loading extra data. So you send simple point and when show, you display extra information.
     *
     * @param ctx current context
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    @Throws(RequiredVersionMissingException::class)
    fun callSendOnePointWithCallbackOnDisplay(ctx: Context) {
        // prepare data
        val pd = PackPoints("test2")
        val p = generateWaypoint(0)
        p.setExtraOnDisplay(
            "com.asamm.locus.api.sample",
            "com.asamm.locus.api.sample.MainActivity",
            EXTRA_ON_DISPLAY_ACTION_ID,
            "id01"
        )
        pd.addPoint(p)

        // send point
        val send = ActionDisplayPoints.sendPack(ctx, pd, ActionDisplayVarious.ExtraAction.CENTER)
        logD(tag = TAG) { "callSendOnePointWithCallbackOnDisplay(), send:$send" }
    }

    /**
     * Display single point with special "onClick" event. Such point when shown, will call back to this application.
     * You may use this for loading extra data. So you send simple point and when show, you display extra information.
     *
     * @param ctx current context
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    @Throws(RequiredVersionMissingException::class)
    fun callSendOnePointWithExtraCallback(ctx: Context) {
        // prepare data
        val pd = PackPoints("test3")
        val p = generateWaypoint(0)
        p.setExtraCallback(
            "My button",
            "com.asamm.locus.api.sample",
            "com.asamm.locus.api.sample.MainActivity",
            EXTRA_CALLBACK_ID,
            "id01"
        )
        pd.addPoint(p)

        // send point
        val send = ActionDisplayPoints.sendPack(ctx, pd, ActionDisplayVarious.ExtraAction.CENTER)
        logD(tag = TAG) { "callSendOnePointWithExtraCallback(), send:$send" }
    }

    /**
     * Allows to search for waypoint in Locus database, based on the waypoint's ID.
     *
     * @param ctx         current context
     * @param activeLocus current active Locus
     */
    fun callRequestPointIdByName(ctx: Context, activeLocus: LocusVersion) {
        // reference to field
        val etName = EditText(ctx)

        // display dialog
        AlertDialog.Builder(ctx)
            .setTitle("Search for waypoints")
            .setView(etName)
            .setMessage(
                "Write name of waypoint you want to find. You may use '%' before or after " + "name as wildcards. \n\n" +
                        "Read more at description of \'ActionTools.getLocusWaypointId\'"
            )
            .setPositiveButton("Search") { _, _ ->
                // get defined name of point
                val name = etName.text.toString()
                if (name.isEmpty()) {
                    Toast.makeText(ctx, "Invalid text to search", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                // start search
                try {
                    val pts = ActionBasics.getPointsId(ctx, activeLocus, name)
                    Toast.makeText(
                        ctx, "Found pts: \'" + pts.contentToString() + "\'",
                        Toast.LENGTH_LONG
                    )
                        .show()
                } catch (e: RequiredVersionMissingException) {
                    Toast.makeText(ctx, "Invalid Locus version", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
            .show()
    }

    /**
     * Allows to display main 'Point screen' for a waypoint, defined by it's ID value.
     *
     * @param ctx         current context
     * @param activeLocus current active Locus
     * @param pointId     ID of point from Locus database
     */
    @Throws(RequiredVersionMissingException::class)
    fun callRequestDisplayPointScreen(
        ctx: Context, activeLocus: LocusVersion,
        pointId: Long
    ) {
        // call special intent with request to return back
        ActionBasics.displayPointScreen(
            ctx, activeLocus, pointId,
            "com.asamm.locus.api.sample",
            "com.asamm.locus.api.sample.MainActivity",
            "myKey",
            "myValue"
        )
    }

    //*************************************************
    // DISPLAY TRACKS
    //*************************************************

    /**
     * Send (display) single track on the Locus Map map.
     *
     * @param ctx current context
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    @Throws(RequiredVersionMissingException::class)
    fun callSendOneTrack(ctx: Context) {
        // prepare data. Create a huge track that may be send only over fileUri (over-limit of
        // basic intent 1MB)
        val track = generateTrack(50.0, 15.0, 100000)

        // get file to share
        val file = SendToAppHelper.getCacheFile(ctx)
        // prepare file Uri: share via FileProvider. You don't need WRITE_EXTERNAL_STORAGE permission for this!
        val uri =
            FileProvider.getUriForFile(ctx, ctx.getString(R.string.file_provider_authority), file)

        // send data the app. 'SendMode' define core behavior how Locus app handle received data
        val sendResult = SendTrack(SendMode.Basic(), track)
            .sendOverFile(ctx, cacheFile = file, cacheFileUri = uri)
        logD(tag = TAG) { "callSendOneTrack(), send:$sendResult" }
    }

    /**
     * Send multiple tracks at once to Locus.
     *
     * @param ctx current context
     */
    @Throws(RequiredVersionMissingException::class)
    fun callSendMultipleTracks(ctx: Context) {
        // prepare data
        val tracks = ArrayList<Track>()
        for (i in 0..4) {
            val track = generateTrack(50 - i * 0.1, 15.0)
            tracks.add(track)
        }

        // send data
        val send = SendTracks(SendMode.Basic(centerOnData = true), tracks)
            .send(ctx)
        //ActionDisplayTracks.sendTracks(ctx, tracks, ActionDisplayVarious.ExtraAction.CENTER)
        logD(tag = TAG) { "callSendMultipleTracks($ctx), send:$send" }
    }

    //*************************************************
    // GUIDANCE / NAVIGATION
    //*************************************************

    /**
     * Start guidance on point closest to certain coordinates. Useful in case, we know name and coordinates
     * of points we previously send to app, but do not know it's real ID (which is common case if point
     * was send into app with one of 'ActionDisplayPoints' method.
     */
    fun startGuidanceToNearestPoint(ctx: Context, lv: LocusVersion) {
        val pointsId = ActionBasics.getPointsId(
            ctx, lv,
            Location(14.5, 50.6),
            1000
        )
        if (pointsId.isEmpty()) {
            logD(tag = TAG) { "startGuidanceToNearestPoint($ctx), no valid point in range" }
        } else {
            // search for specific point
            for (pointId in pointsId) {
                val pt = ActionBasics.getPoint(ctx, lv, pointId)
                if (pt?.name?.equals("It is my point") == true) {
                    ActionBasics.actionStartGuiding(ctx, pt.id)
                    return
                }
            }
            logD(tag = TAG) { "startGuidanceToNearestPoint($ctx), required points not found" }
        }
    }

    //*************************************************
    // VARIOUS TOOLS
    //*************************************************

    fun callSendFileToSystem(ctx: Context) {
        val file = getTempGpxFile(ctx, "<xml></xml>")

        // generate Uri over FileProvider
        val uri =
            FileProvider.getUriForFile(ctx, ctx.getString(R.string.file_provider_authority), file)

        // send request for "display"
        val send = ActionFiles.importFileSystem(ctx, uri, ActionFiles.getMimeType(file))
        logD(tag = TAG) { "callSendFileToSystem($ctx), send: $send, file: $file, uri: $uri" }
    }

    /**
     * Send certain file directly to Locus Map application for handling.
     */
    fun callSendFileToLocus(ctx: Context, lv: LocusVersion) {
        val file = getTempGpxFile(ctx, "<xml></xml>")

        // generate Uri over FileProvider
        val uri =
            FileProvider.getUriForFile(ctx, ctx.getString(R.string.file_provider_authority), file)

        // send request for "display"
        val send = ActionFiles.importFileLocus(ctx, uri, ActionFiles.getMimeType(file), lv, false)
        logD(tag = TAG) { "callSendFileToLocus($ctx, $lv), send: $send, file: $file, uri: $uri" }
    }

    /**
     * Send request on a location. This open Locus "Location picker" and allow to choose
     * location from supported sources. Result will be delivered to activity as response
     *
     * @param act current activity
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    @Throws(RequiredVersionMissingException::class)
    fun pickLocation(act: Activity) {
        ActionBasics.actionPickLocation(act)
    }

    @Throws(Exception::class)
    fun showCircles(activity: FragmentActivity) {
        val circles = ArrayList<Circle>()

        val c0 = Circle(Location(50.15, 15.0), 10000000f, true)
        c0.styleNormal = GeoDataStyle()

        c0.styleNormal!!.setPolyStyle(
            Color.argb(
                50, Color.red(Color.RED),
                Color.green(Color.RED), Color.blue(Color.RED)
            )
        )
        circles.add(c0)

        val c1 = Circle(Location(50.0, 15.0), 1000f, false)
        c1.styleNormal = GeoDataStyle()
        c1.styleNormal!!.setLineStyle(Color.BLUE, 2f)
        circles.add(c1)

        val c2 = Circle(Location(50.1, 15.0), 1500f, false)
        c2.styleNormal = GeoDataStyle()
        c2.styleNormal!!.setLineStyle(Color.RED, 3f)
        circles.add(c2)

        val c3 = Circle(Location(50.2, 15.0), 2000f, false)
        c3.styleNormal = GeoDataStyle()
        c3.styleNormal!!.setLineStyle(Color.GREEN, 4f)
        c3.styleNormal!!.setPolyStyle(Color.LTGRAY)
        circles.add(c3)

        val c4 = Circle(Location(50.3, 15.0), 1500f, false)
        c4.styleNormal = GeoDataStyle()
        c4.styleNormal!!.setLineStyle(Color.MAGENTA, 0f)
        c4.styleNormal!!.setPolyStyle(
            Color.argb(
                100, Color.red(Color.MAGENTA),
                Color.green(Color.MAGENTA), Color.blue(Color.MAGENTA)
            )
        )
        circles.add(c4)

        // send data
        val send = ActionDisplayVarious.sendCirclesSilent(activity, circles, true)
        logD(tag = TAG) { "showCircles(), send:$send" }
    }

    //*************************************************
    // PRIVATE TOOLS
    //*************************************************

    /**
     * Generate random point.
     *
     * @param id ID of point we wants to generate
     * @return generated point
     */
    fun generateWaypoint(id: Int): Point {
        // create one simple point with location
        val loc = Location()
        loc.latitude = Math.random() + 50.0
        loc.longitude = Math.random() + 14.0
        return Point("Testing point - $id", loc)
    }

    /**
     * Generate geocache point.
     *
     * @param id ID of point we wants to generate
     * @return generated geocache
     */
    private fun generateGeocache(id: Int): Point {
        // generate basic point
        val wpt = generateWaypoint(id)

        // generate new geocaching data
        val gcData = GeocachingData()

        // fill data with variables
        gcData.cacheID = "GC2Y0RJ" // REQUIRED
        gcData.name = "Kokotín" // REQUIRED

        // rest is optional so fill as you want - should work
        gcData.type = (Math.random() * 13).toInt()
        gcData.owner = "Menion1"
        gcData.placedBy = "Menion2"
        gcData.difficulty = 3.5f
        gcData.terrain = 3.5f
        gcData.container = GeocachingData.CACHE_SIZE_LARGE
        var longDesc = ""
        for (i in 0..4) {
            longDesc += "Oh, what a looooooooooooooooooooooooong description, never imagine it could be sooo<i>oooo</i>long!<br /><br />Oh, what a looooooooooooooooooooooooong description, never imagine it could be sooo<i>oooo</i>long!"
        }
        gcData.setDescriptions(
            "bla bla, this is some short description also with <b>HTML tags</b>", true,
            longDesc, false
        )

        // add one waypoint
        val gcWpt = GeocachingWaypoint()
        gcWpt.name = "Just an waypoint"
        gcWpt.desc = "Description of waypoint"
        gcWpt.lon = Math.random() + 14.0
        gcWpt.lat = Math.random() + 50.0
        gcWpt.type = GeocachingWaypoint.CACHE_WAYPOINT_TYPE_PARKING
        gcData.waypoints.add(gcWpt)

        // set data and return point
        wpt.gcData = gcData
        return wpt
    }

    /**
     * Generate fictive track from defined start location.
     *
     * @param startLat start latitude
     * @param startLon start longitude
     * @return generated track
     */
    private fun generateTrack(startLat: Double, startLon: Double, numOfPoints: Int = 1000): Track {
        val track = Track()
        track.name = "track from API ($startLat|$startLon)"
        track.addParameter(GeoDataExtra.PAR_DESCRIPTION, "simple track bla bla bla ...")

        // set style
        val style = GeoDataStyle()
        style.setLineStyle(Color.CYAN, 7.0f)
        track.styleNormal = style

        // generate points
        var lat = startLat
        var lon = startLon
        val locs = ArrayList<Location>()
        for (i in 0 until numOfPoints) {
            lat += (Math.random() - 0.5) * 0.01
            lon += Math.random() * 0.001
            val loc = Location()
            loc.latitude = lat
            loc.longitude = lon
            locs.add(loc)
        }
        track.points = locs

        // set some points as highlighted wpts
        val pts = ArrayList<Point>()
        pts.add(Point("p1", locs[100]))
        pts.add(Point("p2", locs[300]))
        pts.add(Point("p3", locs[800]))
        track.waypoints = pts
        return track
    }
}
