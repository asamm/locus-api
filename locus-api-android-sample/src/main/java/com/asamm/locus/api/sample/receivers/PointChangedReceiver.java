package com.asamm.locus.api.sample.receivers;

import locus.api.android.ActionTools;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.LocusUtils.LocusVersion;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.extra.Point;
import locus.api.utils.Logger;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PointChangedReceiver extends BroadcastReceiver {

	private static final String TAG = PointChangedReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		// check intent
		if (intent == null || intent.getAction() == null) {
			Logger.logW(TAG, "onReceive(" + ctx + ", " + intent + "), " +
					"intent is invalid");
			return;
		}
		
		// get data from intent. Code of geocache is optional parameter, that is 
		// available only in case, edited point was geocache. This is useful for cases,
		// you want handle just caches, nothing more
		long pointId = intent.getLongExtra(
				LocusConst.INTENT_EXTRA_ITEM_ID, -1L);
		String name = intent.getStringExtra(
				LocusConst.INTENT_EXTRA_NAME);
		String gcCode = null;
		if (intent.hasExtra(LocusConst.INTENT_EXTRA_GEOCACHE_CODE)) {
			gcCode = intent.getStringExtra(
					LocusConst.INTENT_EXTRA_GEOCACHE_CODE);
		}
		
		// get Locus that sends this intent
		LocusVersion lv = LocusUtils.createLocusVersion(ctx, intent);
		
		// handle received data
		handleReceivedPoint(ctx, lv, pointId, name, gcCode);
	}
	
	private boolean handleReceivedPoint(Context ctx, LocusVersion lv,
			long pointId, String pointName, String cacheCode) {

		// get full point from Locus
		try {
			Point wpt = ActionTools.getLocusWaypoint(ctx, lv, pointId);
			if (wpt == null) {
				Logger.logE(TAG, "handleReceivedPoint(" + ctx + ", " + 
						pointId + ", " + pointName + ", " + cacheCode + "), " +
						"problem with loading of waypoint");
				return false;
			}
			
			// notify about loaded point by Toast. We can't display dialog, becuase there
			// is no guarantee, application is running
			Toast.makeText(ctx, "Point changed:" + wpt, Toast.LENGTH_LONG).show();			

			// now handle waypoint
			// TODO
			return true;
		} catch (RequiredVersionMissingException e) {
			// error if required Locus version is not available
			Logger.logE(TAG, "handleReceivedPoint(" + ctx + ", " + 
					pointId + ", " + pointName + ", " + cacheCode + ")", e);
			return false;
		}
	}
}
