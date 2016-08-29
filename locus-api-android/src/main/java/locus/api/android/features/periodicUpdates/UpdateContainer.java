package locus.api.android.features.periodicUpdates;

import java.io.IOException;

import locus.api.objects.Storable;
import locus.api.objects.extra.ExtraData;
import locus.api.objects.extra.Location;
import locus.api.objects.extra.TrackStats;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

public class UpdateContainer extends Storable {

    // CONSTANTS

    public static final int GUIDE_TYPE_DISABLED                                 = -1;
    public static final int GUIDE_TYPE_WAYPOINT                                 = 1;
    public static final int GUIDE_TYPE_TRACK_GUIDE                              = 2;
    public static final int GUIDE_TYPE_TRACK_NAVIGATION                         = 3;

	// STATE CUSTOM VARIABLES
	
	// is new GPS location available
	protected boolean newMyLocation;
	// is new map center available
	protected boolean newMapCenter;
	// is new zoom level on map
	protected boolean newZoomLevel;
	// is user touching screen
	protected boolean isUserTouching;
	// is GPS/Wi-fi enabled
	protected boolean enabledMyLocation;
	
	// LOCATION, GPS, BASIC VALUES

	// current location (or just container for sensors)
	protected Location locMyLocation;
	// used number of satellites
	protected int gpsSatsUsed;
	// total number of satellites
	protected int gpsSatsAll;
	// current declination value
	protected float declination;
	// current orientation
	protected float orientHeading;
	// -1 * current orientation
	protected float orientHeadingOpposit;
    // current orientation
    protected float orientCourse;
	// pitch orientation (in degrees)
	protected float orientPitch;
	// roll orientation (in degrees)
	protected float orientRoll;
	// GPS shift - (difference between course and heading)
	protected float orientGpsAngle;
	// little bit (last few secs) filtered vertical speed
	protected float speedVertical;
	// slope value in tan(a) values
	protected float slope;
	
	// MAP STUFF
	
	// is map currently visible
	protected boolean mapVisible;
	// current map rotation
	protected float mapRotate;
	// last map center location
	protected Location locMapCenter;
	// location of top-left map corner
	protected Location mapTopLeft;
	// location of bottom-right map corner
	protected Location mapBottomRight;
	// current map zoom level
	protected int mapZoomLevel;
	
	// TRACK RECORDING PART

	// is track record enabled
	protected boolean trackRecRecording;
	// if track record is enabled, is running or paused
	protected boolean trackRecPaused;
    // name of active track recording profile
    protected String trackRecProfileName;
	// complete track statistics
	protected TrackStats trackStats;
	
	// GUIDANCE PART

    // information about type of active guidance
    protected int guideType;
	// name of guiding target
	protected String guideWptName;
	// current guiding location
	protected Location guideWptLoc;
	// distance to target
	protected double guideWptDist;
	// azimuth to target
	protected float guideWptAzim;
	// bearing to target
	protected float guideWptAngle;
	// expected time to target
	protected long guideWptTime;

	// distance from start (in case of guiding along track)
	protected double guideDistFromStart;
	// distance to finish (in case of guiding along track)
	protected double guideDistToFinish;
	// expected time to finish (in case of guiding along track)
	protected long guideTimeToFinish;

    // name of current navigation target point
    protected String guideNavPoint1Name;
    // location of current navigation point
    protected Location guideNavPoint1Loc;
    // distance to current navigation point
    protected double guideNavPoint1Dist;
    // time to current navigation point
    protected long guideNavPoint1Time;
    // action that happen on current navigation point
    protected int guideNavPoint1Action;

    // name of next navigation target point
    protected String guideNavPoint2Name;
    // location of next navigation point
    protected Location guideNavPoint2Loc;
    // distance to next navigation point
    protected double guideNavPoint2Dist;
    // time to next navigation point
    protected long guideNavPoint2Time;
    // action that happen on next navigation point
    protected int guideNavPoint2Action;

	// VARIOUS
	
	// value of current battery in percents (0 - 100)
	protected int deviceBatteryValue;
	// current battery temperature
	protected float deviceBatteryTemperature;

    /**
     * Empty constructor.
     */
    public UpdateContainer() {
        super();
    }

    /**************************************************/
	// STATE CUSTOM VARIABLES
    /**************************************************/
	
	/**
	 * Check if exists new location received from GPS/Wi-fi
	 * @return <code>true</code> if exists
	 */
	public boolean isNewMyLocation() {
		return newMyLocation;
	}

	/**
	 * Check if user moved map to new location
	 * @return <code>true</code> if new map center exists
	 */
	public boolean isNewMapCenter() {
		return newMapCenter;
	}

	/**
	 * Check if user zoomed map to new zoom level
	 * @return <code>true</code> if new zoom is set
	 */
	public boolean isNewZoomLevel() {
		return newZoomLevel;
	}

	/**
	 * Indicate if user is currently touching a map screen. It do not indicate
	 * which specific action is doing, only that something is happening.
	 * @return <code>true</code> if user is currently handling with map
	 */
	public boolean isUserTouching() {
		return isUserTouching;
	}

	/**
	 * flag if GPS is currently enabled in Locus
	 * @return <code>true</code> if enabled
	 */
	public boolean isEnabledMyLocation() {
		return enabledMyLocation;
	}

    /**************************************************/
	// LOCATION, GPS, BASIC VALUES
    /**************************************************/
	
	/**
	 * Current location is GPS/Wi-fi enabled. Also contain all sensors values. 
	 * Should be only container for sensor data in case, GPS is off!
	 * @return current {@link Location} or just container for sensors data
	 */
	public Location getLocMyLocation() {
		return locMyLocation;
	}

	/**
	 * Return number of used satellites for GPS fix
	 * @return number of used satellites
	 */
	public int getGpsSatsUsed() {
		return gpsSatsUsed;
	}

	/**
	 * Method return total number of visible satellites
	 * @return number of all visible satellites
	 */
	public int getGpsSatsAll() {
		return gpsSatsAll;
	}

	/**
	 * Return current declination computed from a) current GPS location, or b) last known
	 * location Locus knows
	 * @return current declination (in degrees)
	 */
	public float getDeclination() {
		return declination;
	}

	/**
	 * Return current device orientation. Source for value depend on settings in Locus.
	 * Also this value will be 0 in case, user do not need orientation for any action
	 * @return orientation value (in degrees) or 0 if orientation is not used or known
	 */
	public float getOrientHeading() {
		return orientHeading;
	}

	/**
	 * Return opposite value to {@link #getOrientHeading()}
	 * @return orientation (in degree) or 0 if orientation is not used or known
	 */
	public float getOrientHeadingOpposit() {
		return orientHeadingOpposit;
	}

    /**
     * Return current device course. Source for this value is change in coordinates,
     * or directly GPS values.
     * @return course value (in degrees) or 0 if course is not known
     */
    public float getOrientCourse() {
        return orientCourse;
    }

	/**
	 * Pitch angle of current device. 
	 * For more info see <a href="http://en.wikipedia.org/wiki/Flight_dynamics">wiki</a>
	 * @return pitch angle (in degree) or 0 if not known
	 */
	public float getOrientPitch() {
		return orientPitch;
	}

	/**
	 * Roll angle of current device. 
	 * For more info see <a href="http://en.wikipedia.org/wiki/Flight_dynamics">wiki</a>
	 * @return roll angle (in degree) or 0 if not known
	 */
	public float getOrientRoll() {
		return orientRoll;
	}

	/**
	 * Returns angle between GPS shift and current orientation. This values differ from 0
	 * based on current outside conditions
	 * @return angle (in degrees) or 0 if no shift exists or GPS or sensors are disabled
	 */
	public float getOrientGpsAngle() {
		return orientGpsAngle;
	}
	
	/**
	 * Returns vertical speed from last few (around 5) seconds of enabled GPS
	 * @return vertical speed in m/s
	 */
	public float getSpeedVertical() {
		return speedVertical;
	}
	
	/**
	 * Get current (little filtered) slope value. More about slope, for example at
	 * <a href="http://en.wikipedia.org/wiki/Slope">Wikipedia</a>
	 * @return slope in 0.01%
	 */
	public float getSlope() {
		return slope;
	}
	
	// MAP STUFF

	/**
	 * Check if some screen with map is currently visible
	 * @return <code>true</code> if map is visible
	 */
	public boolean isMapVisible() {
		return mapVisible;
	}

	/**
	 * Return current rotation value of map screen. This value may be same as current 
	 * orientation or should be value by user manual rotation
	 * @return angle (in degrees) as map rotate
	 */
	public float getMapRotate() {
		return mapRotate;
	}

	/**
	 * Even if map is visible or not, this function return current map center
	 * @return current map center
	 */
	public Location getLocMapCenter() {
		return locMapCenter;
	}

	/**
	 * Top-left coordinate of current map screen
	 * @return max top-left visible location
	 */
	public Location getMapTopLeft() {
		return mapTopLeft;
	}

	/**
	 * Bottom-right coordinate of current map screen
	 * @return max bottom-right visible location
	 */
	public Location getMapBottomRight() {
		return mapBottomRight;
	}

	/**
	 * Return current map zoom level. This value should not be precise in case of using
	 * custom map projections, anyway it's always closest possible value
	 * @return integer as a current zoom level (zoom 8 == whole world (1 tile 256x256px))
	 */
	public int getMapZoomLevel() {
		return mapZoomLevel;
	}

    /**************************************************/
	// TRACK RECORDING PART
    /**************************************************/

    /**
     * Check if track recording is currently active. It do not info about it's state
     * (paused, running), just if it is enabled at all.
     * @return <code>true</code> if track recording is active
     */
    public boolean isTrackRecRecording() {
        return trackRecRecording;
    }

	/**
	 * Check if track recording is currently active or paused.
	 * @return <code>true</code> if track recording is paused
	 */
	public boolean isTrackRecPaused() {
		return trackRecPaused;
	}

	/**
	 * Get current active profile name.
	 * @return profile name
	 */
	public String getTrackRecProfileName() {
		return trackRecProfileName;
	}

	/**
	 * Get complete track statistics.
	 * @return track statistics
	 */
	public TrackStats getTrackRecStats() {
		return trackStats;
	}

    /**************************************************/
    // GUIDANCE PART
    /**************************************************/

    /**
     * Flag is guidance is enabled.
     * @return <code>true</code> if guiding is enabled
     */
    public boolean isGuideEnabled() {
        return getGuideType() != GUIDE_TYPE_DISABLED;
    }

    /**
     * Get current guiding type.
     * @return guiding type
     */
    public int getGuideType() {
        return guideType;
    }

    /**
     * Generate guiding container for waypoint.
     * @return guiding container or 'null' if guiding is not enabled
     */
    public GuideTypeWaypoint getGuideTypeWaypoint() {
        // check if guiding is enabled
        if (guideType != GUIDE_TYPE_WAYPOINT) {
            return null;
        }

        // return generated container
        return new GuideTypeWaypoint();
    }

    /**
     * Generate guiding container for track.
     * @return guiding container or 'null' if guiding is not enabled
     */
    public GuideTypeTrack getGuideTypeTrack() {
        // check if guiding is enabled
        if (guideType != GUIDE_TYPE_TRACK_GUIDE &&
                guideType != GUIDE_TYPE_TRACK_NAVIGATION) {
            return null;
        }

        // return generated container
        return new GuideTypeTrack();
    }

    /**
     * Container for basic data same for all guiding types.
     */
    private class GuideTypeBasic {

        /**
         * Get name of target point. In case of waypoint guide, it is waypoint itself, during
         * guiding/navigation, it is point where current guiding arrow points.
         * @return name of target
         */
        public String getTargetName() {
            return guideWptName;
        }

        /**
         * Get location of target point.
         * @return target location
         */
        public Location getTargetLoc() {
            return guideWptLoc;
        }

        /**
         * Get distance to target point.
         * @return distance to target in metres
         */
        public double getTargetDist() {
            return guideWptDist;
        }

        /**
         * Get azimuth from current location to target point.
         * @return azimuth to waypoint in degrees
         */
        public float getTargetAzim() {
            return guideWptAzim;
        }

        /**
         * Get angle to current point. This angle is computed as current azimuth to waypoint
         * minus current user heading.
         * @return angle to target in degrees
         */
        public float getTargetAngle() {
            return guideWptAngle;
        }

        /**
         * Return expected time to current close waypoint (in case of track,
         * to closest track point).
         * @return long as time in ms
         */
        public long getTargetTime() {
            return guideWptTime;
        }
    }

    /**
     * Container for guiding to single waypoint.
     */
    public class GuideTypeWaypoint extends GuideTypeBasic {

        /**
         * Empty constructor.
         */
        private GuideTypeWaypoint() {}
    }

    /**
     * Container for guiding along track.
     */
    public class GuideTypeTrack extends GuideTypeBasic {

        /**
         * Empty constructor.
         */
        private GuideTypeTrack() {}

        /**
         * Get distance from start of track to current place.
         * @return distance in metres
         */
        public double getDistFromStart() {
            return guideDistFromStart;
        }

        /**
         * Get distance from current place to end of track.
         * @return distance in metres
         */
        public double getDistToFinish() {
            return guideDistToFinish;
        }

        /**
         * Return expected time to finish (in case of guiding along track).
         * @return long as time in ms
         */
        public long getTimeToFinish() {
            return guideTimeToFinish;
        }

        // NAVIGATION STUFF

        /**
         * Check if container has current navigation point.
         * @return <code>true</code> if current navpoint is defined
         */
        public boolean hasNavPoint1() {
            return getNavPoint1Loc() != null;
        }

        /**
         * Return name of current navigation point.
         * @return name of navigation point
         */
        public String getNavPoint1Name() {
            return guideNavPoint1Name;
        }

        /**
         * Return location of current navigation point.
         * @return location of navigation point
         */
        public Location getNavPoint1Loc() {
            return guideNavPoint1Loc;
        }

        /**
         * Return distance from current location to current navigation point.
         * @return distance in metres
         */
        public double getNavPoint1Dist() {
            return guideNavPoint1Dist;
        }

        /**
         * Return time from current location to current navigation point.
         * @return time in seconds
         */
        public double getNavPoint1Time() {
            return guideNavPoint1Time;
        }

        /**
         * Return navigation action for current navigation point.
         * @return navigation action defined in ExtraData.POINT_RTE_ACTION_
         */
        public int getNavPoint1Action() {
            return guideNavPoint1Action;
        }

        /**
         * Check if container has next navigation point.
         * @return <code>true</code> if next navpoint is defined
         */
        public boolean hasNavPoint2() {
            return getNavPoint2Loc() != null;
        }

        /**
         * Return name of next navigation point.
         * @return name of navigation point
         */
        public String getNavPoint2Name() {
            return guideNavPoint2Name;
        }

        /**
         * Return location of next navigation point.
         * @return location of navigation point
         */
        public Location getNavPoint2Loc() {
            return guideNavPoint2Loc;
        }

        /**
         * Return distance from current location to next navigation point.
         * @return distance in metres
         */
        public double getNavPoint2Dist() {
            return guideNavPoint2Dist;
        }

        /**
         * Return time from current location to next navigation point.
         * @return time in seconds
         */
        public double getNavPoint2Time() {
            return guideNavPoint2Time;
        }

        /**
         * Return navigation action for next navigation point.
         * @return navigation action defined in ExtraData.POINT_RTE_ACTION_
         */
        public int getNavPoint2Action() {
            return guideNavPoint2Action;
        }

    }

    /**************************************************/
    // VARIOUS
    /**************************************************/

    /**
     * Get battery value of current device
     * @return value of current battery in percents (0 - 100)
     */
    public int getDeviceBatteryValue() {
        return deviceBatteryValue;
    }

    /**
     * Get battery temperature
     * @return temperature of current device battery in °C
     */
    public float getDeviceBatteryTemperature() {
        return deviceBatteryTemperature;
    }

    /**************************************************/
    // STORABLE PART
    /**************************************************/

    @Override
    protected int getVersion() {
        return 0;
    }

    @Override
    public void reset() {

        // STATE CUSTOM VARIABLES

        newMyLocation = false;
        newMapCenter = false;
        newZoomLevel = false;
        isUserTouching = false;
        enabledMyLocation = false;

        // LOCATION, GPS, BASIC VALUES

        locMyLocation= null;
        gpsSatsUsed = 0;
        gpsSatsAll = 0;
        declination = 0.0f;
        orientHeading = 0.0f;
        orientHeadingOpposit = 0.0f;
        orientCourse = 0.0f;
        orientPitch = 0.0f;
        orientRoll = 0.0f;
        orientGpsAngle = 0.0f;
        speedVertical = 0.0f;
        slope = 0.0f;

        // MAP STUFF

        mapVisible = false;
        mapRotate = 0.0f;
        locMapCenter = null;
        mapTopLeft = null;
        mapBottomRight = null;
        mapZoomLevel = -1;

        // TRACK RECORDING PART

        trackRecRecording = false;
        trackRecPaused = false;
        trackRecProfileName = "";
		trackStats = null;

        // GUIDING PART

        guideType = GUIDE_TYPE_DISABLED;
        guideWptName = null;
        guideWptLoc = null;
        guideWptDist = 0.0;
        guideWptAzim = 0.0f;
        guideWptAngle = 0.0f;
        guideWptTime = 0L;
        guideDistFromStart = 0.0;
        guideDistToFinish = 0.0;
        guideTimeToFinish = 0L;
        guideNavPoint1Name = "";
        guideNavPoint1Loc = null;
        guideNavPoint1Dist = 0.0;
        guideNavPoint1Time = 0L;
		guideNavPoint1Action = ExtraData.VALUE_RTE_ACTION_NO_MANEUVER;
        guideNavPoint2Name = "";
        guideNavPoint2Loc = null;
        guideNavPoint2Dist = 0.0;
        guideNavPoint2Time = 0L;
		guideNavPoint2Action = ExtraData.VALUE_RTE_ACTION_NO_MANEUVER;

        // VARIOUS

        deviceBatteryValue = 0;
        deviceBatteryTemperature = 0.0f;
    }

    @Override
    protected void readObject(int version, DataReaderBigEndian dr) throws IOException {

        // STATE CUSTOM VARIABLES

        newMyLocation = dr.readBoolean();
        newMapCenter = dr.readBoolean();
        newZoomLevel = dr.readBoolean();
        isUserTouching = dr.readBoolean();
        enabledMyLocation = dr.readBoolean();

        // LOCATION, GPS, BASIC VALUES

        locMyLocation = readLocation(dr);
        gpsSatsUsed = dr.readInt();
        gpsSatsAll = dr.readInt();
        declination = dr.readFloat();
        orientHeading = dr.readFloat();
        orientHeadingOpposit = dr.readFloat();
        orientCourse = dr.readFloat();
        orientPitch = dr.readFloat();
        orientRoll = dr.readFloat();
        orientGpsAngle = dr.readFloat();
        speedVertical = dr.readFloat();
        slope = dr.readFloat();

        // MAP STUFF

        mapVisible = dr.readBoolean();
        mapRotate = dr.readFloat();
        locMapCenter = readLocation(dr);
        mapTopLeft = readLocation(dr);
        mapBottomRight = readLocation(dr);
        mapZoomLevel = dr.readInt();

        // TRACK RECORDING PART

        trackRecRecording = dr.readBoolean();
        trackRecPaused = dr.readBoolean();
        trackRecProfileName = dr.readString();
		if (dr.readBoolean()) {
			trackStats = new TrackStats();
			trackStats.read(dr);
		}

        // GUIDING PART

        guideType = dr.readInt();
        guideWptName = dr.readString();
        guideWptLoc = readLocation(dr);
        guideWptDist = dr.readDouble();
        guideWptAzim = dr.readFloat();
        guideWptAngle = dr.readFloat();
        guideWptTime = dr.readLong();
        guideDistFromStart = dr.readDouble();
        guideDistToFinish = dr.readDouble();
        guideTimeToFinish = dr.readLong();
        guideNavPoint1Name = dr.readString();
        guideNavPoint1Loc = readLocation(dr);
        guideNavPoint1Dist = dr.readDouble();
        guideNavPoint1Time = dr.readLong();
		guideNavPoint1Action = dr.readInt();
        guideNavPoint2Name = dr.readString();
        guideNavPoint2Loc = readLocation(dr);
        guideNavPoint2Dist = dr.readDouble();
        guideNavPoint2Time = dr.readLong();
		guideNavPoint2Action = dr.readInt();

        // VARIOUS

        deviceBatteryValue = dr.readInt();
        deviceBatteryTemperature = dr.readFloat();
    }

    @Override
    protected void writeObject(DataWriterBigEndian dw) throws IOException {

        // STATE CUSTOM VARIABLES

        dw.writeBoolean(newMyLocation);
        dw.writeBoolean(newMapCenter);
        dw.writeBoolean(newZoomLevel);
        dw.writeBoolean(isUserTouching);
        dw.writeBoolean(enabledMyLocation);

        // LOCATION, GPS, BASIC VALUES

        writeLocation(dw, locMyLocation);
        dw.writeInt(gpsSatsUsed);
        dw.writeInt(gpsSatsAll);
        dw.writeFloat(declination);
        dw.writeFloat(orientHeading);
        dw.writeFloat(orientHeadingOpposit);
        dw.writeFloat(orientCourse);
        dw.writeFloat(orientPitch);
        dw.writeFloat(orientRoll);
        dw.writeFloat(orientGpsAngle);
        dw.writeFloat(speedVertical);
        dw.writeFloat(slope);

        // MAP STUFF

        dw.writeBoolean(mapVisible);
        dw.writeFloat(mapRotate);
        writeLocation(dw, locMapCenter);
        writeLocation(dw, mapTopLeft);
        writeLocation(dw, mapBottomRight);
        dw.writeInt(mapZoomLevel);

        // TRACK RECORDING PART

        dw.writeBoolean(trackRecRecording);
        dw.writeBoolean(trackRecPaused);
        dw.writeString(trackRecProfileName);
		if (trackStats != null) {
			dw.writeBoolean(true);
			dw.writeStorable(trackStats);
		} else {
			dw.writeBoolean(false);
		}

        // GUIDING PART

        dw.writeInt(guideType);
        dw.writeString(guideWptName);
        writeLocation(dw, guideWptLoc);
        dw.writeDouble(guideWptDist);
        dw.writeFloat(guideWptAzim);
        dw.writeFloat(guideWptAngle);
        dw.writeLong(guideWptTime);
        dw.writeDouble(guideDistFromStart);
        dw.writeDouble(guideDistToFinish);
        dw.writeLong(guideTimeToFinish);
        dw.writeString(guideNavPoint1Name);
        writeLocation(dw, guideNavPoint1Loc);
        dw.writeDouble(guideNavPoint1Dist);
        dw.writeLong(guideNavPoint1Time);
		dw.writeInt(guideNavPoint1Action);
        dw.writeString(guideNavPoint2Name);
		writeLocation(dw, guideNavPoint2Loc);
        dw.writeDouble(guideNavPoint2Dist);
        dw.writeLong(guideNavPoint2Time);
		dw.writeInt(guideNavPoint2Action);

        // VARIOUS

        dw.writeInt(deviceBatteryValue);
		dw.writeFloat(deviceBatteryTemperature);
	}

    /**
     * Read location from reader.
     * @param dr reader
     * @return location or throw exception
     * @throws IOException
     */
    private Location readLocation(DataReaderBigEndian dr) throws IOException {
        // check existence
        boolean exists = dr.readBoolean();
        if (!exists) {
            return null;
        }

        // finally read location
        try {
            return (Location) dr.readStorable(Location.class);
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Write location to container.
     * @param dw writer
     * @throws IOException
     */
    private void writeLocation(DataWriterBigEndian dw, Location loc) throws IOException {
        if (loc == null) {
            dw.writeBoolean(false);
        } else {
            dw.writeBoolean(true);
            dw.writeStorable(loc);
        }
    }
}
