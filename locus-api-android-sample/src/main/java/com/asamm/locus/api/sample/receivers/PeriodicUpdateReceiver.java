package com.asamm.locus.api.sample.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import locus.api.android.ActionTools;
import locus.api.android.features.periodicUpdates.PeriodicUpdatesHandler;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.LocusUtils.LocusVersion;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.utils.Logger;

public class PeriodicUpdateReceiver extends BroadcastReceiver {

	// tag for logger
	private static final String TAG = "PeriodicUpdateReceiver";

	// STATIC LISTENER

	// registered onUpdate listener
	private static PeriodicUpdatesHandler.OnUpdate mOnUpdateListener;

	// setup update handler
	static {
		// prepare valid instance of PeriodicUpdate object
		PeriodicUpdatesHandler pu = PeriodicUpdatesHandler.getInstance();

		// set notification of new locations to 10m
		pu.setLocNotificationLimit(10.0);
	}

	/**
	 * Register listener for an updates.
	 * @param ctx current context
	 * @param onUpdateListener listener
	 * @throws RequiredVersionMissingException exception in case of invalid Locus Map app
	 */
	public static void setOnUpdateListener(Context ctx, PeriodicUpdatesHandler.OnUpdate onUpdateListener)
			throws RequiredVersionMissingException {
		// register listener
		mOnUpdateListener = onUpdateListener;

		// enable/disable receiver
		LocusVersion lv = LocusUtils.getActiveVersion(ctx);
		if (mOnUpdateListener != null) {
			ActionTools.enablePeriodicUpdatesReceiver(ctx, lv, PeriodicUpdateReceiver.class);
		} else {
			ActionTools.disablePeriodicUpdatesReceiver(ctx, lv, PeriodicUpdateReceiver.class);
		}
	}

	// BROADCAST EXTENSION

	@Override
	public void onReceive(final Context context, Intent intent) {
		// check intent
		if (intent == null || intent.getAction() == null) {
			Logger.logW(TAG, "onReceive(" + context + ", " + intent + "), " +
					"intent is invalid");
			return;
		}

		// let handler handle received intent
		if (mOnUpdateListener != null) {
			PeriodicUpdatesHandler.getInstance().
					onReceive(context, intent, mOnUpdateListener);
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
}
