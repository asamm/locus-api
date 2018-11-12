package com.asamm.locus.api.sample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import locus.api.android.ActionTools;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.LocusUtils.LocusVersion;
import locus.api.objects.extra.GeoDataExtra;
import locus.api.objects.extra.Point;
import locus.api.utils.Logger;

public class ActivityGeocacheTools extends FragmentActivity {

	private static final String TAG = "ActivityGeocacheTools";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TextView tv = new TextView(this);
		tv.setText("ActivityGeocacheTools - only for receiving Tools actions from cache");
		setContentView(tv);
		
        // finally check intent that started this sample
        checkStartIntent();
	}
	
    private void checkStartIntent() {
        Intent intent = getIntent();
        Logger.logD(TAG, "received intent:" + intent);
        if (intent == null) {
        	return;
        }
        
        // get Locus from intent
        final LocusVersion lv = LocusUtils.createLocusVersion(this, intent);
        if (lv == null) {
        	Logger.logD(TAG, "checkStartIntent(), cannot obtain LocusVersion");
        	return;
        }
        
        if (LocusUtils.isIntentPointTools(intent)) {
        	try {
        		final Point wpt = LocusUtils.handleIntentPointTools(this, intent);
        		if (wpt == null) {
        			Toast.makeText(ActivityGeocacheTools.this, "Wrong INTENT - no point!", Toast.LENGTH_SHORT).show();
        		} else {
        			new AlertDialog.Builder(this).
        			setTitle("Intent - On Point action").
        			setMessage("Received intent with point:\n\n" + wpt.getName() + "\n\nloc:" + wpt.getLocation() + 
        					"\n\ngcData:" + (wpt.gcData == null ? "sorry, but no..." : wpt.gcData.getCacheID())).
        					setNegativeButton("Close", (dialog, which) -> {
								// just do some action on required coordinates
							}).
        					setPositiveButton("Send updated back", (dialog, which) -> {
								// because current test version is registered on geocache data,
								// I'll send as result updated geocache
								try {
									wpt.addParameter(GeoDataExtra.PAR_DESCRIPTION, "UPDATED!");
									wpt.getLocation().setLatitude(wpt.getLocation().getLatitude() + 0.001);
									wpt.getLocation().setLongitude(wpt.getLocation().getLongitude() + 0.001);
									ActionTools.updateLocusWaypoint(ActivityGeocacheTools.this, lv, wpt, false);
									finish();
								} catch (Exception e) {
									Logger.logE(TAG, "isIntentPointTools(), problem with sending new waypoint back", e);
								}
							}).show();
        		}
        	} catch (Exception e) {
        		Logger.logE(TAG, "handle point tools", e);
        	}
        }	
    }
}
