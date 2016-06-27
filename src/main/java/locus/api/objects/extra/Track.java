/*  
 * Copyright 2012, Asamm Software, s. r. o.
 * 
 * This file is part of LocusAPI.
 * 
 * LocusAPI is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * LocusAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *  
 * You should have received a copy of the Lesser GNU General Public
 * License along with LocusAPI. If not, see 
 * <http://www.gnu.org/licenses/lgpl.html/>.
 */

package locus.api.objects.extra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import locus.api.objects.GeoData;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;

public class Track extends GeoData {

	// tag for logger
	private static final String TAG = "Track";
	
	/**
     * Locations of this track
     */
	List<Location> points;
    // list containing all track break points. Break point is defined as index of point, after
    // which follow break in track. So break point "1" means, that after second point (point with
    // index 1) follow a break.
	List<Integer> breaks;
    /**
     * Extra points (also may include routing data)
     */
	List<Waypoint> waypoints;
	// flag that indicate whether to use parent folder style if exists
	private boolean mUseFolderStyle;
	// type of activity
	private int mActivityType;
    // track statistics (generated statistics of track)
    private TrackStats mStats;

    // CONSTRUCTOR
    
	public Track() {
		super();
	}
	
	public Track(DataReaderBigEndian dr) throws IOException {
		super(dr);
	}
	
	public Track(byte[] data) throws IOException {
		super(data);
	}
	
    /**************************************************/
	// GET & SET
    /**************************************************/
	
	// POINTS
	
	public Location getPoint(int index) {
		return points.get(index);
	}

    public int getPointsCount() {
        return points.size();
    }

	public List<Location> getPoints() {
		return points;
	}
	
	public boolean setPoints(List<Location> points) {
		if (points == null) {
			Logger.logW(TAG, "setPoints(), cannot be null!");
			return false;
		}
		this.points = points;
		return true;
	}
	
	// BREAKS
	
	public List<Integer> getBreaks() {
		return breaks;
	}
	
	public byte[] getBreaksData() {
        DataWriterBigEndian dw;
		try {
            dw = new DataWriterBigEndian();
			for (int i = 0; i < breaks.size(); i++) {
                dw.writeInt(breaks.get(i));
			}
			return dw.toByteArray();
		} catch (Exception e) {
			Logger.logE(TAG, "getBreaksData()", e);
		}
		return new byte[0];
	}

	public void setBreaksData(byte[] data) {
		// check data
		if (data == null || data.length == 0) {
			return;
		}

        // read indexes from data
		try {
			DataReaderBigEndian dr = new DataReaderBigEndian(data);
			breaks.clear();
			while (dr.available() > 0) {
				breaks.add(dr.readInt());
			}
		} catch (Exception e) {
			Logger.logE(TAG, "setBreaksData()", e);
			breaks.clear();
		}
	}
	
	// WAYPOINTS
	
	public Waypoint getWaypoint(int index) {
		return waypoints.get(index);
	}
	
	public List<Waypoint> getWaypoints() {
		return waypoints;
	}
	
	public boolean setWaypoints(List<Waypoint> wpts) {
		if (wpts == null) {
			Logger.logW(TAG, "setWaypoints(), cannot be null!");
			return false;
		}
		this.waypoints = wpts;
		return true;
	}

	// FOLDER STYLE

	/**
	 * Get information if current track use "folder" style.
	 * @return {@code true} to use folder style
	 */
	public boolean isUseFolderStyle() {
		return mUseFolderStyle;
	}

	/**
	 * Set flag to current track, to use folder style.
	 * @param useFolderStyle {@code true} to use folder style
	 */
	public void setUseFolderStyle(boolean useFolderStyle) {
		this.mUseFolderStyle = useFolderStyle;
	}

	// ACTIVITY TYPE

	/**
	 * Get activity type for current track.
	 * @return current activity type
	 */
	public int getActivityType() {
		return mActivityType;
	}

	/**
	 * Set activity type for current track.
	 * @param activityType activity type
	 */
	public void setActivityType(int activityType) {
		mActivityType = activityType;
	}

	// STATISTICS

    /**
     * Get current track statistics.
     * @return track statistics
     */
    public TrackStats getStats() {
        return mStats;
    }

    /**
     * Set custom track statistics to current track.
     * @param stats statistics
     */
    public void setStats(TrackStats stats) {
        // check parameter
        if (stats == null) {
            throw new NullPointerException("setTrackStats(), " +
                    "parameter cannot be null");
        }

        // finally set stats
        this.mStats = stats;
    }

	/**
	 * Set custom track statistics to current track.
	 * @param data statistics data
	 */
	public void setStats(byte[] data) {
		try {
			TrackStats stats = new TrackStats(data);
			setStats(stats);
		} catch (Exception e) {
			Logger.logE(TAG, "setStats(" + Arrays.toString(data) + ")", e);
			this.mStats = new TrackStats();
		}
	}

	/**************************************************/
	// STORABLE PART
	/**************************************************/

	@Override
	public int getVersion() {
		return 5;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readObject(int version, DataReaderBigEndian dr) throws IOException {
		id = dr.readLong();
		name = dr.readString();

		// load locations
		points = (List<Location>) dr.readListStorable(Location.class);

		// read breaks
		int breaksSize = dr.readInt();
		if (breaksSize > 0) {
			setBreaksData(dr.readBytes(breaksSize));
		}

		// read waypoints
		waypoints = (List<Waypoint>) dr.readListStorable(Waypoint.class);

		// read extra part
		readExtraData(dr);
		readStyles(dr);

		// old deprecated statistics
		// clear previous values
		mStats.reset();

		// read all old data
		mStats.setNumOfPoints(dr.readInt());
		mStats.setStartTime(dr.readLong());
		mStats.setStopTime(dr.readLong());

		mStats.setTotalLength(dr.readFloat());
		mStats.setTotalLengthMove(dr.readFloat());
		mStats.setTotalTime(dr.readLong());
		mStats.setTotalTimeMove(dr.readLong());
		mStats.setSpeedMax(dr.readFloat());

		mStats.setAltitudeMax(dr.readFloat());
		mStats.setAltitudeMin(dr.readFloat());

		mStats.setEleNeutralDistance(dr.readFloat());
		mStats.setEleNeutralHeight(dr.readFloat());
		mStats.setElePositiveDistance(dr.readFloat());
		mStats.setElePositiveHeight(dr.readFloat());
		mStats.setEleNegativeDistance(dr.readFloat());
		mStats.setEleNegativeHeight(dr.readFloat());
		mStats.setEleTotalAbsDistance(dr.readFloat());
		mStats.setEleTotalAbsHeight(dr.readFloat());

		// V1

		if (version >= 1) {
			mUseFolderStyle = dr.readBoolean();
		}

		// V2

		if (version >= 2) {
			timeCreated = dr.readLong();
		}

		// V3

		if (version >= 3) {
			mStats = new TrackStats(dr);
		}

		// V4

		if (version >= 4) {
			setReadWriteMode(ReadWriteMode.values()[dr.readInt()]);
		}

		// V5

		if (version >= 5) {
			mActivityType = dr.readInt();
		}
	}

	@Override
	public void writeObject(DataWriterBigEndian dw) throws IOException {
		dw.writeLong(id);
		dw.writeString(name);

		// write locations
		dw.writeListStorable(points);

		// write breaks
		byte[] breaksData = getBreaksData();
		dw.writeInt(breaksData.length);
		if (breaksData.length > 0) {
			dw.write(breaksData);
		}

		// write waypoints
		dw.writeListStorable(waypoints);

		// write extra data
		writeExtraData(dw);
		writeStyles(dw);

		// write old statistics for reader below version 3
		dw.writeInt(0);
		dw.writeLong(0L);
		dw.writeLong(0L);

		dw.writeFloat(0.0f);
		dw.writeFloat(0.0f);
		dw.writeLong(0L);
		dw.writeLong(0L);
		dw.writeFloat(0.0f);

		dw.writeFloat(0.0f);
		dw.writeFloat(0.0f);

		dw.writeFloat(0.0f);
		dw.writeFloat(0.0f);
		dw.writeFloat(0.0f);
		dw.writeFloat(0.0f);
		dw.writeFloat(0.0f);
		dw.writeFloat(0.0f);
		dw.writeFloat(0.0f);
		dw.writeFloat(0.0f);

		// V1

		dw.writeBoolean(mUseFolderStyle);

		// V2

		dw.writeLong(timeCreated);

		// V3

		dw.writeStorable(mStats);

		// V4

		dw.writeInt(getReadWriteMode().ordinal());

		// V5

		dw.writeInt(mActivityType);
	}

	@Override
	public void reset() {
		id = -1;
		name = "";
		points = new ArrayList<>();
		breaks = new ArrayList<>();
		waypoints = new ArrayList<>();
		extraData = null;
		styleNormal = null;
		styleHighlight = null;

		// V1

		mUseFolderStyle = true;

		// V2

		timeCreated = System.currentTimeMillis();

		// V3

		mStats = new TrackStats();

		// V4

		setReadWriteMode(ReadWriteMode.READ_WRITE);

		// V5

		mActivityType = 0;
	}
}
