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

package com.asamm.locus.api.sample.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;
import android.widget.Toast;

import com.asamm.locus.api.sample.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import locus.api.android.ActionDisplay.ExtraAction;
import locus.api.android.ActionDisplayPoints;
import locus.api.android.ActionDisplayTracks;
import locus.api.android.ActionDisplayVarious;
import locus.api.android.ActionFiles;
import locus.api.android.ActionTools;
import locus.api.android.objects.PackWaypoints;
import locus.api.android.utils.LocusUtils.LocusVersion;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.extra.Circle;
import locus.api.objects.extra.GeoDataExtra;
import locus.api.objects.extra.GeoDataStyle;
import locus.api.objects.extra.Location;
import locus.api.objects.extra.Track;
import locus.api.objects.extra.Waypoint;
import locus.api.objects.geocaching.GeocachingData;
import locus.api.objects.geocaching.GeocachingWaypoint;
import locus.api.utils.Logger;

public class SampleCalls {

	// tag for logger
	private static final String TAG = "SampleCalls";

	// ID for "onDisplay" event
	public static final String EXTRA_ON_DISPLAY_ACTION_ID = "myOnDisplayExtraActionId";
	
	/**************************************************/
	// POINTS PART
	/**************************************************/

	/**
	 * Send single point into Locus application.
	 * @param ctx current context
	 * @throws RequiredVersionMissingException exception in case of missing required app version
	 */
	public static void callSendOnePoint(Context ctx) throws RequiredVersionMissingException {
		// generate pack
		PackWaypoints pw = new PackWaypoints("callSendOnePoint");
		pw.addWaypoint(generateWaypoint(0));

		// send data
		boolean send = ActionDisplayPoints.sendPack(ctx, pw, ExtraAction.IMPORT);
		Logger.logD(TAG, "callSendOnePoint(), " +
				"send:" + send);
	}

	/**
	 * Send more points - LIMIT DATA TO MAX 1000 (really max 1500), more cause troubles. It easy and fast method,
	 * but depend on data size, so intent with lot of geocaches will be really limited.
	 * @param ctx current context
	 * @throws RequiredVersionMissingException exception in case of missing required app version
	 */
	public static void callSendMorePoints(Context ctx) throws RequiredVersionMissingException {
		// generate pack with points
		PackWaypoints pw = new PackWaypoints("callSendMorePoints");
		for (int i = 0; i < 1000; i++) {
			pw.addWaypoint(generateWaypoint(i));
		}

		// send data
		boolean send = ActionDisplayPoints.sendPack(ctx, pw, ExtraAction.IMPORT);
		Logger.logD(TAG, "callSendMorePoints(), " +
				"send:" + send);
	}

	/**
	 * Send single point that will be immediately visible on a map. Point will also contains an icon.
	 * @param ctx current context
	 * @throws RequiredVersionMissingException exception in case of missing required app version
	 */
	public static void callSendOnePointWithIcon(Context ctx) throws RequiredVersionMissingException {
		// prepare pack with point (with icon)
		PackWaypoints pw = new PackWaypoints("callSendOnePointWithIcon");
		pw.setBitmap(BitmapFactory.decodeResource(
				ctx.getResources(), R.drawable.ic_launcher));
		pw.addWaypoint(generateWaypoint(0));

		// send data
		boolean send = ActionDisplayPoints.sendPack(ctx, pw, ExtraAction.CENTER);
		Logger.logD(TAG, "callSendOnePointWithIcon(), " +
				"send:" + send);
	}
	
	/**
	 * Similar to previous method. Every PackWaypoints object have defined icon that is applied on
	 * every points. So if you want to send more points with various icons, you have to define for
	 * every pack specific PackWaypoints object
	 * @param ctx current context
	 */
	public static void callSendMorePointsWithIcons(Context ctx) throws RequiredVersionMissingException {
		// container for pack of points
		List<PackWaypoints> data = new ArrayList<>();

		// prepare first pack
		PackWaypoints pd1 = new PackWaypoints("test01");
		GeoDataStyle es1 = new GeoDataStyle();
		es1.setIconStyle("http://www.googlemapsmarkers.com/v1/009900/", 1.0f);
		pd1.setExtraStyle(es1);
		for (int i = 0; i < 100; i++) {
			pd1.addWaypoint(generateWaypoint(i));
		}
		data.add(pd1);

		// prepare second pack with different icon
		PackWaypoints pd2 = new PackWaypoints("test02");
		GeoDataStyle es2 = new GeoDataStyle();
		es2.setIconStyle("http://www.googlemapsmarkers.com/v1/990000/", 1.0f);
		pd2.setExtraStyle(es2);
		for (int i = 0; i < 100; i++) {
			pd2.addWaypoint(generateWaypoint(i));
		}
		data.add(pd2);

		// send data
		boolean send = ActionDisplayPoints.sendPacks(ctx, data, ExtraAction.CENTER);
		Logger.logD(TAG, "callSendMorePointsWithIcons(), " +
				"send:" + send);
	}

	/**
	 * Display single geocache point on the map.
	 * @param ctx current context
	 * @throws RequiredVersionMissingException exception in case of missing required app version
	 */
	public static void callSendOnePointGeocache(Context ctx) throws RequiredVersionMissingException {
		// prepare geocache
		PackWaypoints pd = new PackWaypoints("callSendOnePointGeocache");
		pd.addWaypoint(generateGeocache(0));

		// send data
		boolean send = ActionDisplayPoints.sendPack(ctx, pd, ExtraAction.CENTER);
		Logger.logD(TAG, "callSendOnePointGeocache(), " +
				"send:" + send);
	}
	
	/**
	 * Send and display more geocaches on the screen at once. Limit here is much more tight! Intent have limit on
	 * data size (around 2MB, so if you want to send more geocaches, don't rather use this method.
	 * @param ctx current context
	 * @throws RequiredVersionMissingException exception in case of missing required app version
	 */
	public static void callSendMorePointsGeocacheIntentMethod(Context ctx) throws RequiredVersionMissingException {
		// prepare geocaches
		PackWaypoints pw = new PackWaypoints("test6");
		for (int i = 0; i < 100; i++) {
			pw.addWaypoint(generateGeocache(i));
		}

		// send data
		boolean send = ActionDisplayPoints.sendPack(ctx, pw, ExtraAction.CENTER);
		Logger.logD(TAG, "callSendMorePointsGeocacheIntentMethod(), " +
				"send:" + send);
	}

	/**
	 * Send more geocaches with method, that store byte[] data in raw file and send locus link to this file.
	 * This method is useful in case of bigger number of caches that already has some troubles with
	 * method over intent.
	 * @param ctx current context
	 */
	public static void callSendMorePointsGeocacheFileMethod(Context ctx) throws RequiredVersionMissingException {
		// get filepath
		File externalDir = ctx.getExternalCacheDir();
		if (externalDir == null || !(externalDir.exists())) {
			Logger.logW(TAG, "problem with obtain of External dir");
			return;
		}

		// prepare data
		PackWaypoints pw = new PackWaypoints("test07");
		for (int i = 0; i < 1000; i++) {
			pw.addWaypoint(generateGeocache(i));
		}
		ArrayList<PackWaypoints> data = new ArrayList<>();
		data.add(pw);

		// send data
		boolean send = ActionDisplayPoints.sendPacksFile(ctx, data,
				new File(externalDir, "testFile.locus").getAbsolutePath(), ExtraAction.CENTER);
		Logger.logD(TAG, "callSendMorePointsGeocacheFileMethod(), " +
				"send:" + send);
	}

	/**
	 * Display single point with special "onClick" event. Such point when shown, will call back to this application.
	 * You may use this for loading extra data. So you send simple point and when show, you display extra information.
	 * @param ctx current context
	 * @throws RequiredVersionMissingException exception in case of missing required app version
	 */
	public static void callSendOnePointWithCallbackOnDisplay(Context ctx) throws RequiredVersionMissingException {
		// prepare data
		PackWaypoints pd = new PackWaypoints("test2");
		Waypoint p = generateWaypoint(0);
		p.setExtraOnDisplay(
				"com.asamm.locus.api.sample",
				"com.asamm.locus.api.sample.MainActivity",
				EXTRA_ON_DISPLAY_ACTION_ID,
				"id01");
		pd.addWaypoint(p);

		// send point
		boolean send = ActionDisplayPoints.sendPack(ctx, pd, ExtraAction.CENTER);
		Logger.logD(TAG, "callSendOnePointWithCallbackOnDisplay(), " +
				"send:" + send);
	}

    /**
     * Allows to search for waypoint in Locus database, based on the waypoint's ID.
     * @param ctx current context
     * @param activeLocus current active Locus
     */
    public static void callRequestPointIdByName(final Context ctx, final LocusVersion activeLocus) {
        final EditText etName = new EditText(ctx);

        new AlertDialog.Builder(ctx).
                setTitle("Search for waypoints").
                setView(etName).
                setMessage("Write name of waypoint you want to find. You may use '%' before or after " +
                        "name as wildcards. \n\nRead more at description of \'ActionTools.getLocusWaypointId\'").
                setPositiveButton("Search", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
						// get defined name of point
                        String name = etName.getText().toString();
                        if (name.length() == 0) {
                            Toast.makeText(ctx, "Invalid text to search", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // start search
                        try {
                            long[] wpts = ActionTools.getLocusWaypointId(ctx, activeLocus, name);
                            Toast.makeText(ctx, "Found wpts: \'" + Arrays.toString(wpts) +
                                    "\'", Toast.LENGTH_LONG).show();
                        } catch (RequiredVersionMissingException e) {
                            Toast.makeText(ctx, "Invalid Locus version", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }).show();
    }

    /**
     * Allows to display main 'Point screen' for a waypoint, defined by it's ID value.
     * @param ctx current context
     * @param activeLocus current active Locus
     * @param pointId ID of point from Locus database
     */
    public static void callRequestDisplayPointScreen(Context ctx, LocusVersion activeLocus,
			long pointId) throws RequiredVersionMissingException {
		// call special intent with rquest to return back
		ActionTools.displayWaypointScreen(ctx, activeLocus, pointId,
				"com.asamm.locus.api.sample",
				"com.asamm.locus.api.sample.MainActivity",
				"myKey",
				"myValue");
    }
	
	/**************************************************/
	// TRACKS PART
	/**************************************************/

	/**
	 * Send (display) single track on Locus map.
	 * @param ctx current context
	 * @throws RequiredVersionMissingException exception in case of missing required app version
	 */
	public static void callSendOneTrack(Context ctx) throws RequiredVersionMissingException {
		// prepare data
		Track track = generateTrack(50, 15);

		// send data
		boolean send = ActionDisplayTracks.sendTrack(ctx, track, ExtraAction.CENTER);
		Logger.logD(TAG, "callSendOneTrack(), " +
				"send:" + send);
	}

	/**
	 * Send multiple tracks at once to Locus.
	 * @param ctx current context
	 */
	public static void callSendMultipleTracks(Context ctx) throws RequiredVersionMissingException {
		// prepare data
		ArrayList<Track> tracks = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			Track track = generateTrack(50 - i * 0.1, 15);
			tracks.add(track);
		}

		// send data
		boolean send = ActionDisplayTracks.sendTracks(ctx, tracks, ExtraAction.CENTER);
		Logger.logD(TAG, "callSendMultipleTracks(" + ctx + "), " +
				"send:" + send);
	}
	
	/**************************************************/
	// TOOLS PART
	/**************************************************/
	
	public static void callSendFileToSystem(Context ctx) {
		boolean send = ActionFiles.importFileSystem(ctx, getTempGpxFile());
		Logger.logD(TAG, "callSendFileToSystem(" + ctx + "), " +
				"send:" + send);
	}
	
	public static void callSendFileToLocus(Context ctx, LocusVersion lv) {
		boolean send = ActionFiles.importFileLocus(ctx, lv, getTempGpxFile(), false);
		Logger.logD(TAG, "callSendFileToLocus(" + ctx + "), " +
				"send:" + send);
	}

	/**
	 * Send request on a location. This open Locus "Location picker" and allow to choose
	 * location from supported sources. Result will be delivered to activity as response
	 * @param act current activity
	 * @throws RequiredVersionMissingException exception in case of missing required app version
	 */
	public static void pickLocation(Activity act)
			throws RequiredVersionMissingException {
		ActionTools.actionPickLocation(act);
	}
	
	public static String getRootDirectory(Context ctx, LocusVersion lv) {
		try {
			return ActionTools.getLocusInfo(ctx, lv).getRootDirectory();
		} catch (RequiredVersionMissingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void showCircles(FragmentActivity activity) throws Exception {
		ArrayList<Circle> circles = new ArrayList<Circle>();

		Circle c0 = new Circle(new Location("c1", 50.15, 15), 10000000, true);
		c0.styleNormal = new GeoDataStyle();
		c0.styleNormal.setPolyStyle(Color.argb(50, Color.red(Color.RED),
				Color.green(Color.RED), Color.blue(Color.RED)), true, true);
		circles.add(c0);

		Circle c1 = new Circle(new Location("c1", 50, 15), 1000);
		c1.styleNormal = new GeoDataStyle();
		c1.styleNormal.setLineStyle(Color.BLUE, 2);
		circles.add(c1);

		Circle c2 = new Circle(new Location("c2", 50.1, 15), 1500);
		c2.styleNormal = new GeoDataStyle();
		c2.styleNormal.setLineStyle(Color.RED, 3);
		circles.add(c2);

		Circle c3 = new Circle(new Location("c1", 50.2, 15), 2000);
		c3.styleNormal = new GeoDataStyle();
		c3.styleNormal.setLineStyle(Color.GREEN, 4);
		c3.styleNormal.setPolyStyle(Color.LTGRAY, true, true);
		circles.add(c3);

		Circle c4 = new Circle(new Location("c1", 50.3, 15), 1500);
		c4.styleNormal = new GeoDataStyle();
		c4.styleNormal.setLineStyle(Color.MAGENTA, 0);
		c4.styleNormal.setPolyStyle(
				Color.argb(100, Color.red(Color.MAGENTA),
						Color.green(Color.MAGENTA), Color.blue(Color.MAGENTA)),
				true, true);
		circles.add(c4);

		// send data
		boolean send = ActionDisplayVarious.sendCirclesSilent(activity, circles, true);
		Logger.logD(TAG, "showCircles(), " +
				"send:" + send);
	}

	/**
	 * Check if found version is running.
	 * @param ctx current context
	 * @param lv received Locus version
	 * @return {@code true} if app is running
	 */
	public static boolean isRunning(Context ctx, LocusVersion lv) {
		try {
			return ActionTools.getLocusInfo(ctx, lv).isRunning();
		} catch (RequiredVersionMissingException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Check if periodic updates are enabled.
	 * @param ctx current context
	 * @param lv received Locus version
	 * @return {@code true} if updates are enabled
	 */
	public static boolean isPeriodicUpdateEnabled(Context ctx, LocusVersion lv) {
		try {
			return ActionTools.getLocusInfo(ctx, lv).isPeriodicUpdatesEnabled();
		} catch (RequiredVersionMissingException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**************************************************/
	// PRIVATE TOOLS
	/**************************************************/

	/**
	 * Generate random point.
	 * @param id ID of point we wants to generate
	 * @return generated point
	 */
    public static Waypoint generateWaypoint(int id) {
		// create one simple point with location
		Location loc = new Location(TAG);
		loc.setLatitude(Math.random() + 50.0);
		loc.setLongitude(Math.random() + 14.0);
		return new Waypoint("Testing point - " + id, loc);
    }

	/**
	 * Generate geocache point.
	 * @param id ID of point we wants to generate
	 * @return generated geocache
	 */
    private static Waypoint generateGeocache(int id) {
		// generate basic point
    	Waypoint wpt = generateWaypoint(id);
    	
    	// generate new geocaching data
    	GeocachingData gcData = new GeocachingData();
    	
    	// fill data with variables
    	gcData.setCacheID("GC2Y0RJ"); // REQUIRED
    	gcData.setName("Kokotín"); // REQUIRED
    	
    	// rest is optional so fill as you want - should work
    	gcData.setType((int) (Math.random() * 13));
    	gcData.setOwner("Menion1");
    	gcData.setPlacedBy("Menion2");
    	gcData.setDifficulty(3.5f);
    	gcData.setTerrain(3.5f);
    	gcData.setContainer(GeocachingData.CACHE_SIZE_LARGE);
    	String longDesc = "";
    	for (int i = 0; i < 5; i++) {
    		longDesc += "Oh, what a looooooooooooooooooooooooong description, never imagine it could be sooo<i>oooo</i>long!<br /><br />Oh, what a looooooooooooooooooooooooong description, never imagine it could be sooo<i>oooo</i>long!";
    	}
    	gcData.setDescriptions(
    			"bla bla, this is some short description also with <b>HTML tags</b>", true,
    			longDesc, false);
    	
    	// add one waypoint
    	GeocachingWaypoint gcWpt = new GeocachingWaypoint();
    	gcWpt.setName("Just an waypoint");
    	gcWpt.setDesc("Description of waypoint");
    	gcWpt.setLon(Math.random() + 14.0);
    	gcWpt.setLat(Math.random() + 50.0);
    	gcWpt.setType(GeocachingWaypoint.CACHE_WAYPOINT_TYPE_PARKING);
    	gcData.waypoints.add(gcWpt);
    	
    	// set data and return point
    	wpt.gcData = gcData;
    	return wpt;
    }

	/**
	 * Generate fictive track from defined start location.
	 * @param startLat start latitude
	 * @param startLon start longitude
	 * @return generated track
	 */
    private static Track generateTrack(double startLat, double startLon) {
		Track track = new Track();
		track.setName("track from API (" + startLat + "|" + startLon + ")");
		track.addParameter(GeoDataExtra.PAR_DESCRIPTION, "simple track bla bla bla ...");
		
		// set style
		GeoDataStyle style = new GeoDataStyle();
		style.setLineStyle(GeoDataStyle.LineStyle.ColorStyle.SIMPLE,
				Color.CYAN, 7.0f,
				GeoDataStyle.LineStyle.Units.PIXELS);
		track.styleNormal = style;
		
		// generate points
		double lat = startLat;
		double lon = startLon;
		ArrayList<Location> locs = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			lat += ((Math.random() - 0.5) * 0.01);
			lon += (Math.random() * 0.001);
			Location loc = new Location(TAG);
			loc.setLatitude(lat);
			loc.setLongitude(lon);
			locs.add(loc);
		}
		track.setPoints(locs);
		
		// set some points as highlighted wpts
		ArrayList<Waypoint> pts = new ArrayList<>();
		pts.add(new Waypoint("p1", locs.get(100)));
		pts.add(new Waypoint("p2", locs.get(300)));
		pts.add(new Waypoint("p3", locs.get(800)));
		track.setWaypoints(pts);
		return track;
    }

	/**
	 * Temporary file used for testing of import feature.
	 * @return GPX file
	 */
	private static File getTempGpxFile() {
		return new File(Environment.getExternalStorageDirectory().getPath(), "temporary_path.gpx");
	}
}
