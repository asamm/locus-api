package com.asamm.locus.api.sample.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import locus.api.android.ActionTools
import locus.api.android.features.periodicUpdates.PeriodicUpdatesHandler
import locus.api.android.utils.LocusUtils
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.utils.Logger

/**
 * Receiver for periodic updates.
 */
class PeriodicUpdateReceiver : BroadcastReceiver() {

    // BROADCAST EXTENSION

    override fun onReceive(context: Context, intent: Intent?) {
        // check intent
        if (intent == null || intent.action == null) {
            Logger.logW(TAG, "onReceive(" + context + ", " + intent + "), " +
                    "intent is invalid")
            return
        }

        // let handler handle received intent
        if (onUpdateListener != null) {
            PeriodicUpdatesHandler.getInstance().onReceive(context, intent, onUpdateListener)
        }

        // we may for example create something like "live map", that will return back to Locus
        // data just after map move, with following code. Firstly check if there was any
        // movement and also if main map screen is still visible
        //		if (!update.isNewMapCenter() || !update.isMapVisible()) {
        //			return;
        //		}
        //
        //		try {
        //			// sending back few points near received center
        //			Location mapCenter = update.getLocMapCenter();
        //			PackPoints pw = new PackPoints("send_point_silently");
        //			for (int i = 0; i < 10; i++) {
        //				Location loc = new Location(TAG);
        //				loc.setLatitude(mapCenter.getLatitude() + (Math.random() - 0.5) / 100.0);
        //				loc.setLongitude(mapCenter.getLongitude() + (Math.random() - 0.5) / 100.0);
        //
        //				// generate new point and add it to the pack
        //				pw.addWaypoint(new Waypoint("Testing point - " + i, loc));
        //			}
        //
        //			// finally send points
        //			ActionDisplayPoints.sendPackSilent(context, pw, false);
        //		} catch (RequiredVersionMissingException e) {
        //			e.printStackTrace();
        //		}
    }

    companion object {

        // tag for logger
        private const val TAG = "PeriodicUpdateReceiver"

        // STATIC LISTENER

        // registered onUpdate listener
        private var onUpdateListener: PeriodicUpdatesHandler.OnUpdate? = null

        // setup update handler
        init {
            // prepare valid instance of PeriodicUpdate object
            val pu = PeriodicUpdatesHandler.getInstance()

            // set notification of new locations to 10m
            pu.setLocNotificationLimit(10.0)
        }

        /**
         * Register listener for an updates.
         * @param ctx current context
         * @param onUpdateListener listener
         * @throws RequiredVersionMissingException exception in case of invalid Locus Map app
         */
        @Throws(RequiredVersionMissingException::class)
        fun setOnUpdateListener(ctx: Context, onUpdateListener: PeriodicUpdatesHandler.OnUpdate) {
            // register listener
            this.onUpdateListener = onUpdateListener

            // enable/disable receiver
            val lv = LocusUtils.getActiveVersion(ctx)
            if (this.onUpdateListener != null) {
                ActionTools.enablePeriodicUpdatesReceiver(ctx, lv, PeriodicUpdateReceiver::class.java)
            } else {
                ActionTools.disablePeriodicUpdatesReceiver(ctx, lv, PeriodicUpdateReceiver::class.java)
            }
        }
    }
}
