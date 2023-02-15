package com.asamm.locus.api.sample

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.widget.Toast
import com.asamm.locus.api.sample.utils.SampleCalls
import com.asamm.loggerV2.logD
import com.asamm.loggerV2.logE
import com.asamm.loggerV2.logW
import locus.api.android.ActionBasics
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusUtils
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.extra.Location

/**
 * Created by menion on 29/08/2016.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
object MainIntentHandler {

    // tag for logger
    private const val TAG = "MainActivityIntentHandler"

    /**
     * Handle received intent in main activity.
     *
     * @param act    main app activity
     * @param intent received intent
     */
    fun handleStartIntent(act: MainActivity, intent: Intent?) {
        // check intent
        logD(tag = TAG) { "received intent: $intent" }
        if (intent == null) {
            return
        }

        // handle intent
        try {
            if (IntentHelper.isIntentGetLocation(intent)) {
                // handle received intent to pick a location
                handleGetLocation(act)
            } else if (IntentHelper.isIntentPointTools(intent)) {
                // event performed if user tap on your app icon in tools menu of 'Point'
                handlePointToolsMenu(act, intent)
            } else if (IntentHelper.isIntentTrackTools(intent)) {
                // event performed if user tap on your app icon in tools menu of 'Track'
                handleTrackToolsMenu(act, intent)
            } else if (IntentHelper.isIntentMainFunction(intent)) {
                // event called when you insert your app into main menu and user tapped on it
                handleMainMenuClick(act, intent)
            } else if (IntentHelper.isIntentSearchList(intent)) {
                // another call on your app registered in menu. In this case in search menu
                handleMenuSearchClick(act, intent)
            } else if (IntentHelper.isIntentPointsTools(intent)) {
                // you may also register application into context menu of big 'Point manager' screen.
                // It appears at bottom menu and react on tap sends ID's of all selected points.
                handlePointManagerMenuClick(act, intent)
            } else if (intent.hasExtra(SampleCalls.EXTRA_ON_DISPLAY_ACTION_ID)) {
                // handle intent from context menu of previously send point
                val value = intent.getStringExtra(SampleCalls.EXTRA_ON_DISPLAY_ACTION_ID)

                // now create full point version and send it back for returned value
                val pt = SampleCalls.generateWaypoint(0)
                pt.name = "Improved version!"
                pt.addParameter(
                    GeoDataExtra.PAR_DESCRIPTION,
                    "Extra description to ultra improved point!, received value:$value"
                )

                // return data
                val retIntent = LocusUtils.prepareResultExtraOnDisplayIntent(pt, true)
                act.setResult(Activity.RESULT_OK, retIntent)
                act.finish()
                // or you may set RESULT_CANCEL if you don't have improved version of Point, then locus
                // just show current available version
            } else if (intent.hasExtra(SampleCalls.EXTRA_CALLBACK_ID)) {
                // handle intent from context menu of previously send point
                val value = intent.getStringExtra(SampleCalls.EXTRA_CALLBACK_ID)
                AlertDialog.Builder(act)
                    .setTitle("Intent - extraCallback")
                    .setMessage("Received intent with extra: $value")
                    .setPositiveButton("Close") { _, _ -> }
                    .show()
            } else if (IntentHelper.isIntentReceiveLocation(intent)) {
                // at this moment we check if returned intent contains location we previously
                // requested from Locus
                val pt = IntentHelper.getPointFromIntent(act, intent)
                if (pt != null) {
                    AlertDialog.Builder(act)
                        .setTitle("Intent - PickLocation")
                        .setMessage(
                            "Received intent with point:\n\n" + pt.name + "\n\nloc:" + pt.location +
                                    "\n\ngcData:" + if (pt.gcData == null) "sorry, but no..." else pt.gcData!!.cacheID
                        )
                        .setPositiveButton("Close") { _, _ -> }
                        .show()
                } else {
                    logW(tag = TAG) { "request PickLocation, canceled" }
                }
            }
        } catch (e: Exception) {
            logE(tag = TAG) { "handleStartIntent($act, $intent)" }
        }

    }

    private fun handleGetLocation(act: MainActivity) {
        AlertDialog.Builder(act)
            .setTitle("Intent - Get location")
            .setMessage("By pressing OK, dialog disappear and to Locus will be returned some location!")
            .setPositiveButton("OK") { _, _ ->
                IntentHelper.sendGetLocationData(act,
                    "Non-sense Loc ;)",
                    Location().apply {
                        latitude = Math.random() * 85
                        longitude = Math.random() * 180
                    })
            }
            .show()
    }

    @Throws(RequiredVersionMissingException::class)
    private fun handlePointToolsMenu(act: MainActivity, intent: Intent) {
        val pt = IntentHelper.getPointFromIntent(act, intent)
        if (pt == null) {
            Toast.makeText(act, "Wrong INTENT - no point!", Toast.LENGTH_SHORT).show()
        } else {
            AlertDialog.Builder(act)
                .setTitle("Intent - On Point action")
                .setMessage(
                    "Received intent with point:\n\n" + pt.name + "\n\nloc:" + pt.location +
                            "\n\ngcData:" + if (pt.gcData == null) "sorry, but no..." else pt.gcData!!.cacheID
                ).setNegativeButton("Close") { _, _ ->
                    // just do some action on required coordinates
                }
                .setPositiveButton("Send updated back") { _, _ ->
                    // get Locus from intent
                    val lv = LocusUtils.createLocusVersion(act, intent)
                    if (lv == null) {
                        logD(tag = TAG) { "checkStartIntent(), cannot obtain LocusVersion" }
                        return@setPositiveButton
                    }

                    // because current test version is registered on geocache data,
                    // I'll send as result updated geocache
                    try {
                        // set new parameters
                        pt.addParameter(GeoDataExtra.PAR_DESCRIPTION, "UPDATED!")
                        pt.location.latitude = pt.location.latitude + 0.001
                        pt.location.longitude = pt.location.longitude + 0.001
                        ActionBasics.updatePoint(act, lv, pt, false)
                        act.finish()
                    } catch (e: Exception) {
                        logE(tag = TAG, ex = e) {
                            "isIntentPointTools(), problem with sending new waypoint back"
                        }
                    }
                }
                .show()
        }
    }

    /**
     * Handle click on "share" button in bottom tools menu in the single track.
     *
     * @param act current activity
     * @param intent received intent
     */
    private fun handleTrackToolsMenu(act: MainActivity, intent: Intent) {
        // get and check track
        val track = IntentHelper.getTrackFromIntent(act, intent)
        if (track == null) {
            Toast.makeText(act, "Wrong INTENT - no track!", Toast.LENGTH_SHORT).show()
            return
        }

        // display result of request
        AlertDialog.Builder(act)
            .setTitle("Intent - On Track action")
            .setMessage(
                "Received intent with track:\n\n" + track.name + "\n\n" +
                        "desc:" + track.parameterDescription
            )
            .setNeutralButton("Get as GPX") { _, _ ->
                // V1 > get track over activity. Result is handled in activityResult and async export
                // is nicely visible in progress dialog directly in Locus Map.
                // In this case, handled in MainActivity.onActivityResult().
                try {
                    ActionBasics.getTrackInFormat(
                        act,
                        requestCode = MainActivity.RC_GET_TRACK_IN_FORMAT,
                        trackId = track.id,
                        format = ActionBasics.FileFormat.GPX,
                        formatExtra = "{ attachments: true }"
                    )
                } catch (e: RequiredVersionMissingException) {
                    logE(tag = TAG, ex = e) { "" }
                    Toast.makeText(act, "Current Locus Map version is too old", Toast.LENGTH_SHORT)
                        .show()
                }

//                    // V2 > get track over broadcast. Result is handled in defined Broadcast receiver
//                    // and async export should handled in add-on.
//                    // In this case, handled in OnTrackExportedReceiver.
//                    try {
//                        ActionBasics.getTrackInFormat(act,
//                                resultHandler = "com.asamm.api.locus.ON_TRACK_EXPORTED",
//                                resultExtra = "extra_track_1",
//                                trackId = track.getId(),
//                                format = ActionBasics.FileFormat.GPX,
//                                formatExtra = "{ attachments: true }")
//                    } catch (e: RequiredVersionMissingException) {
//                        Logger.logE(TAG, "", e)
//                        Toast.makeText(act, "Current Locus Map version is too old", Toast.LENGTH_SHORT).show()
//                    }
            }
            .setNegativeButton("Close") { _, _ -> }
            .show()
    }

    private fun handleMainMenuClick(act: MainActivity, intent: Intent) {
        IntentHelper.handleIntentMainFunction(act, intent,
            object : IntentHelper.OnIntentReceived {

                override fun onReceived(
                    lv: LocusVersion,
                    locGps: Location?,
                    locMapCenter: Location?
                ) {
                    AlertDialog.Builder(act)
                        .setTitle("Intent - Main function")
                        .setMessage("GPS location:$locGps\n\nmapCenter:$locMapCenter")
                        .setPositiveButton("Close") { _, _ -> }
                        .show()
                }

                override fun onFailed() {
                    Toast.makeText(act, "Wrong INTENT!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun handleMenuSearchClick(act: MainActivity, intent: Intent) {
        IntentHelper.handleIntentSearchList(act, intent,
            object : IntentHelper.OnIntentReceived {

                override fun onReceived(
                    lv: LocusVersion,
                    locGps: Location?,
                    locMapCenter: Location?
                ) {
                    AlertDialog.Builder(act).setTitle("Intent - Search list")
                        .setMessage("GPS location:$locGps\n\nmapCenter:$locMapCenter")
                        .setPositiveButton("Close") { _, _ -> }
                        .show()
                }

                override fun onFailed() {
                    Toast.makeText(act, "Wrong INTENT!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun handlePointManagerMenuClick(act: MainActivity, intent: Intent) {
        val pointIds = IntentHelper.getPointsFromIntent(intent)
        if (pointIds == null || pointIds.isEmpty()) {
            AlertDialog.Builder(act)
                .setTitle("Intent - Points screen (Tools)")
                .setMessage("Problem with loading waypointIds")
                .setPositiveButton("Close") { _, _ -> }
                .show()
        } else {
            AlertDialog.Builder(act)
                .setTitle("Intent - Points screen (Tools)")
                .setMessage("Loaded from file, points:" + pointIds.size)
                .setPositiveButton("Load all now") { _, _ ->
                    // get Locus from intent
                    val lv = LocusUtils.createLocusVersion(act, intent)
                    if (lv == null) {
                        logD(tag = TAG) { "checkStartIntent(), cannot obtain LocusVersion" }
                        return@setPositiveButton
                    }

                    // finally load points
                    loadPointsFromLocus(act, lv, pointIds)
                    act.finish()
                }.show()
        }
    }

    private fun loadPointsFromLocus(act: MainActivity, lv: LocusVersion, ptsIds: LongArray?) {
        if (ptsIds == null || ptsIds.isEmpty()) {
            Toast.makeText(act, "No points to load", Toast.LENGTH_SHORT).show()
            return
        }

        for (wptId in ptsIds) {
            try {
                val pt = ActionBasics.getPoint(act, lv, wptId)
                if (pt != null) {
                    logD(tag = TAG) { "loadPointsFromLocus(), wptId:" + wptId + ", vs:" + pt.id }
                    // do some modifications
                    pt.addParameter(GeoDataExtra.PAR_DESCRIPTION, "UPDATED!")
                    pt.location.latitude = pt.location.latitude + 0.001
                    pt.location.longitude = pt.location.longitude + 0.001

                    // update waypoint in Locus database
                    if (ActionBasics.updatePoint(act, lv, pt, false) == 1) {
                        Toast.makeText(
                            act, "Loaded and updated (" + pt.name + ")",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            act, "Loaded, but problem with update (" + pt.name + ")",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(act, "Waypoint: $wptId, not loaded", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                logE(tag = TAG, ex = e) { "loadPointsFromLocus($ptsIds)" }
            }

        }
    }
}
