package com.asamm.locus.api.sample.receivers;

import locus.api.android.ActionDisplayPoints;
import locus.api.android.objects.PackWaypoints;
import locus.api.android.features.periodicUpdates.PeriodicUpdatesHandler;
import locus.api.android.features.periodicUpdates.UpdateContainer;
import locus.api.android.utils.LocusUtils.LocusVersion;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.extra.Location;
import locus.api.objects.extra.Waypoint;
import locus.api.utils.Logger;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class PeriodicUpdateReceiver extends BroadcastReceiver {

	// tag for logger
	private static final String TAG = "PeriodicUpdateReceiver";
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		// check intent
		if (intent == null || intent.getAction() == null) {
			Logger.logW(TAG, "onReceive(" + context + ", " + intent + "), " +
					"intent is invalid");
			return;
		}
		
		// get valid instance of PeriodicUpdate object
		PeriodicUpdatesHandler pu = PeriodicUpdatesHandler.getInstance();

		// set notification of new locations to 10m
		pu.setLocNotificationLimit(10.0);
		
		// handle event
		pu.onReceive(context, intent, new PeriodicUpdatesHandler.OnUpdate() {
			
			@Override
			public void onIncorrectData() {
				Toast.makeText(context, "onIncorrectData()", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onUpdate(LocusVersion locusVersion, UpdateContainer update) {
				Log.i(TAG, "onUpdate(" + locusVersion + ", " + update + ")");
				
				// sending data back to locus based on events if new map center and map is visible!
				if (!update.isNewMapCenter() || !update.isMapVisible())
					return;
				
				Toast.makeText(context, "ZoomLevel:" + update.getMapZoomLevel(), Toast.LENGTH_LONG).show();
				
				try {
					// sending back few points near received
					Location mapCenter = update.getLocMapCenter();
					PackWaypoints pw = new PackWaypoints("send_point_silently");
					for (int i = 0; i < 10; i++) {
						Location loc = new Location(TAG);
						loc.setLatitude(mapCenter.getLatitude() + (Math.random() - 0.5) / 100.0);
						loc.setLongitude(mapCenter.getLongitude() + (Math.random() - 0.5) / 100.0);
						// point name determine if 
						pw.addWaypoint(new Waypoint("Testing point - " + i, loc));
					}

					ActionDisplayPoints.sendPackSilent(context, pw, false);
				} catch (RequiredVersionMissingException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
