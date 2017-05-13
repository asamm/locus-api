package com.asamm.locus.api.sample.receivers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.asamm.locus.api.sample.MainActivity;
import com.asamm.locus.api.sample.utils.SampleCalls;

import locus.api.android.ActionTools;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.extra.GeoDataExtra;
import locus.api.objects.extra.Location;
import locus.api.objects.extra.Track;
import locus.api.objects.extra.Waypoint;
import locus.api.utils.Logger;

/**
 * Created by menion on 29/08/2016.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
public class MainActivityIntentHandler {

	// tag for logger
	private static final String TAG = "MainActivityIntentHandler";

	/**
	 * Handle received intent in main activity.
	 * @param act main app activity
	 * @param intent received intent
	 */
	public static void handleStartIntent(MainActivity act, Intent intent) {
		// check intent
		Logger.logD(TAG, "received intent:" + intent);
		if (intent == null) {
			return;
		}

		// handle intent
		try {
			if (LocusUtils.isIntentGetLocation(intent)) {
				// handle received intent to pick a location
				handleGetLocation(act);
			} else if (LocusUtils.isIntentPointTools(intent)) {
				// event performed if user tap on your app icon in tools menu of 'Point'
				handlePointToolsMenu(act, intent);
			} else if (LocusUtils.isIntentTrackTools(intent)) {
				// event performed if user tap on your app icon in tools menu of 'Track'
				handleTrackToolsMenu(act, intent);
			} else if (LocusUtils.isIntentMainFunction(intent)) {
				// event called when you insert your app into main menu and user tapped on it
				handleMainMenuClick(act, intent);
			} else if (LocusUtils.isIntentSearchList(intent)) {
				// another call on your app registered in menu. In this case in search menu
				handleMenuSearchClick(act, intent);
			} else if (LocusUtils.isIntentPointsScreenTools(intent)) {
				// you may also register application into context menu of big 'Point manager' screen.
				// It appears at bottom menu and react on tap sends ID's of all selected points.
				handlePointManagerMenuClick(act, intent);
			} else if (intent.hasExtra(SampleCalls.EXTRA_ON_DISPLAY_ACTION_ID)) {
				// handle intent from context menu of previously send point
				String value = intent.getStringExtra(SampleCalls.EXTRA_ON_DISPLAY_ACTION_ID);

				// now create full point version and send it back for returned value
				Waypoint wpt = SampleCalls.generateWaypoint(0);
				wpt.setName("Improved version!");
				wpt.addParameter(GeoDataExtra.PAR_DESCRIPTION,
						"Extra description to ultra improved point!, received value:" + value);

				// return data
				Intent retInent = LocusUtils.prepareResultExtraOnDisplayIntent(wpt, true);
				act.setResult(Activity.RESULT_OK, retInent);
				act.finish();
				// or you may set RESULT_CANCEL if you don't have improved version of Point, then locus
				// just show current available version
			} else if (LocusUtils.isIntentReceiveLocation(intent)) {
				// at this moment we check if returned intent contains location we previously
				// requested from Locus
				Waypoint wpt = LocusUtils.getWaypointFromIntent(intent);
				if (wpt != null) {
					new AlertDialog.Builder(act).
							setTitle("Intent - PickLocation").
							setMessage("Received intent with point:\n\n" + wpt.getName() + "\n\nloc:" + wpt.getLocation() +
									"\n\ngcData:" + (wpt.gcData == null ? "sorry, but no..." : wpt.gcData.getCacheID())).
							setPositiveButton("Close", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {}
							}).show();
				} else {
					Logger.logW(TAG, "request PickLocation, canceled");
				}
			}
		} catch (Exception e) {
			Logger.logE(TAG, "handleStartIntent(" + act + ", " + intent + ")");
		}
	}

	private static void handleGetLocation(final MainActivity act) {
		new AlertDialog.Builder(act).
				setTitle("Intent - Get location").
				setMessage("By pressing OK, dialog disappear and to Locus will be returned some location!").
				setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Location loc = new Location("Unknown source");
						loc.setLatitude(Math.random() * 85);
						loc.setLongitude(Math.random() * 180);
						if (!LocusUtils.sendGetLocationData(act, "Non sence Loc ;)", loc)) {
							Toast.makeText(act, "Wrong data to send!", Toast.LENGTH_SHORT).show();
						}
					}
				}).show();
	}

	private static void handlePointToolsMenu(final MainActivity act, final Intent intent)
			throws RequiredVersionMissingException {
		final Waypoint wpt = LocusUtils.handleIntentPointTools(act, intent);
		if (wpt == null) {
			Toast.makeText(act, "Wrong INTENT - no point!", Toast.LENGTH_SHORT).show();
		} else {
			new AlertDialog.Builder(act).
					setTitle("Intent - On Point action").
					setMessage("Received intent with point:\n\n" + wpt.getName() + "\n\nloc:" + wpt.getLocation() +
							"\n\ngcData:" + (wpt.gcData == null ? "sorry, but no..." : wpt.gcData.getCacheID())).
					setNegativeButton("Close", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// just do some action on required coordinates
						}
					}).
					setPositiveButton("Send updated back", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// get Locus from intent
							LocusUtils.LocusVersion lv = LocusUtils.createLocusVersion(act, intent);
							if (lv == null) {
								Logger.logD(TAG, "checkStartIntent(), cannot obtain LocusVersion");
								return;
							}

							// because current test version is registered on geocache data,
							// I'll send as result updated geocache
							try {
								// set new parameters
								wpt.addParameter(GeoDataExtra.PAR_DESCRIPTION, "UPDATED!");
								wpt.getLocation().setLatitude(wpt.getLocation().getLatitude() + 0.001);
								wpt.getLocation().setLongitude(wpt.getLocation().getLongitude() + 0.001);
								ActionTools.updateLocusWaypoint(act, lv, wpt, false);
								act.finish();
							} catch (Exception e) {
								Logger.logE(TAG, "isIntentPointTools(), problem with sending new waypoint back", e);
							}
						}
					}).show();
		}
	}

	private static void handleTrackToolsMenu(final MainActivity act, Intent intent)
			throws RequiredVersionMissingException {
		final Track track = LocusUtils.handleIntentTrackTools(act, intent);
		if (track == null) {
			Toast.makeText(act, "Wrong INTENT - no track!", Toast.LENGTH_SHORT).show();
		} else {
			new AlertDialog.Builder(act).
					setTitle("Intent - On Track action").
					setMessage("Received intent with track:\n\n" + track.getName() + "\n\ndesc:" + track.getParameterDescription()).
					setNegativeButton("Close", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// just do some action on required coordinates
						}
					}).show();
		}
	}

	private static void handleMainMenuClick(final MainActivity act, Intent intent) {
		LocusUtils.handleIntentMainFunction(act, intent,
				new LocusUtils.OnIntentMainFunction() {

					@Override
					public void onReceived(LocusUtils.LocusVersion lv, Location locGps, Location locMapCenter) {
						new AlertDialog.Builder(act).
								setTitle("Intent - Main function").
								setMessage("GPS location:" + locGps + "\n\nmapCenter:" + locMapCenter).
								setPositiveButton("Close", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {}
								}).show();
					}

					@Override
					public void onFailed() {
						Toast.makeText(act, "Wrong INTENT!", Toast.LENGTH_SHORT).show();
					}
				});
	}

	private static void handleMenuSearchClick(final MainActivity act, Intent intent) {
		LocusUtils.handleIntentSearchList(act, intent,
				new LocusUtils.OnIntentMainFunction() {

					@Override
					public void onReceived(LocusUtils.LocusVersion lv, Location locGps, Location locMapCenter) {
						new AlertDialog.Builder(act).
								setTitle("Intent - Search list").
								setMessage("GPS location:" + locGps + "\n\nmapCenter:" + locMapCenter).
								setPositiveButton("Close", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								}).show();
					}

					@Override
					public void onFailed() {
						Toast.makeText(act, "Wrong INTENT!", Toast.LENGTH_SHORT).show();
					}
				});
	}

	private static void handlePointManagerMenuClick(final MainActivity act, final Intent intent) {
		final long[] waypointIds = LocusUtils.handleIntentPointsScreenTools(intent);
		if (waypointIds == null || waypointIds.length == 0) {
			new AlertDialog.Builder(act).
					setTitle("Intent - Points screen (Tools)").
					setMessage("Problem with loading waypointIds").
					setPositiveButton("Close", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {}
					}).show();
		} else {
			new AlertDialog.Builder(act).
					setTitle("Intent - Points screen (Tools)").
					setMessage("Loaded from file, points:" + waypointIds.length).
					setPositiveButton("Load all now", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// get Locus from intent
							LocusUtils.LocusVersion lv = LocusUtils.createLocusVersion(act, intent);
							if (lv == null) {
								Logger.logD(TAG, "checkStartIntent(), cannot obtain LocusVersion");
								return;
							}

							// finally load points
							loadPointsFromLocus(act, lv, waypointIds);
							act.finish();
						}
					}).show();
		}
	}

	private static void loadPointsFromLocus(MainActivity act, LocusUtils.LocusVersion lv, long[] wptsIds) {
		if (wptsIds == null || wptsIds.length == 0) {
			Toast.makeText(act, "No points to load", Toast.LENGTH_SHORT).show();
			return;
		}

		for (long wptId : wptsIds) {
			try {
				Waypoint wpt = ActionTools.getLocusWaypoint(act, lv, wptId);
				if (wpt != null) {
					Logger.logD(TAG, "loadPointsFromLocus(), wptId:" + wptId + ", vs:" + wpt.id);
					// do some modifications
					wpt.addParameter(GeoDataExtra.PAR_DESCRIPTION, "UPDATED!");
					wpt.getLocation().setLatitude(wpt.getLocation().getLatitude() + 0.001);
					wpt.getLocation().setLongitude(wpt.getLocation().getLongitude() + 0.001);

					// update waypoint in Locus database
					if (ActionTools.updateLocusWaypoint(act, lv, wpt, false) == 1) {
						Toast.makeText(act, "Loaded and updated (" + wpt.getName() + ")",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(act, "Loaded, but problem with update (" + wpt.getName() + ")",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(act, "Waypoint: " + wptId + ", not loaded", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				Logger.logE(TAG, "loadPointsFromLocus(" + wptsIds + ")", e);
			}
		}
	}
}
