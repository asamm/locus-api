package com.asamm.locus.api.sample.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import locus.api.android.ActionBasics
import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils
import locus.api.android.utils.LocusUtils.LocusVersion
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.utils.Logger

class PointChangedReceiver : BroadcastReceiver() {

    override fun onReceive(ctx: Context, intent: Intent?) {
        // check intent
        if (intent?.action == null) {
            Logger.logW(TAG, "onReceive(" + ctx + ", " + intent + "), " +
                    "intent is invalid")
            return
        }

        // get data from intent. Code of geocache is optional parameter, that is
        // available only in case, edited point was geocache. This is useful for cases,
        // you want handle just caches, nothing more
        val pointId = intent.getLongExtra(
                LocusConst.INTENT_EXTRA_ITEM_ID, -1L)
        val name = intent.getStringExtra(
                LocusConst.INTENT_EXTRA_NAME)
        var gcCode: String? = null
        if (intent.hasExtra(LocusConst.INTENT_EXTRA_GEOCACHE_CODE)) {
            gcCode = intent.getStringExtra(
                    LocusConst.INTENT_EXTRA_GEOCACHE_CODE)
        }

        // handle received data
        LocusUtils.createLocusVersion(ctx, intent)?.let {
            handleReceivedPoint(ctx, it, pointId, name, gcCode)
        } ?: {
            Logger.logW(TAG, "onReceive($ctx, $intent), " +
                    "unable to obtain valid Locus version")
        }()
    }

    private fun handleReceivedPoint(ctx: Context, lv: LocusVersion,
            pointId: Long, pointName: String, cacheCode: String?): Boolean {

        // get full point from Locus
        try {
            val wpt = ActionBasics.getPoint(ctx, lv, pointId)
            if (wpt == null) {
                Logger.logE(TAG, "handleReceivedPoint(" + ctx + ", " +
                        pointId + ", " + pointName + ", " + cacheCode + "), " +
                        "problem with loading of waypoint")
                return false
            }

            // notify about loaded point by Toast. We can't display dialog, because there
            // is no guarantee, application is running
            Toast.makeText(ctx, "Point changed:$wpt", Toast.LENGTH_LONG).show()

            // now handle waypoint
            // TODO
            return true
        } catch (e: RequiredVersionMissingException) {
            // error if required Locus version is not available
            Logger.logE(TAG, "handleReceivedPoint(" + ctx + ", " +
                    pointId + ", " + pointName + ", " + cacheCode + ")", e)
            return false
        }
    }

    companion object {

        private const val TAG = "PointChangedReceiver"
    }
}
