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

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;
import locus.api.utils.Utils;

@SuppressWarnings ("unused")
public class Location extends Storable {

	// tag for logger
    private static final String TAG = "Location";

	// location unique ID
	private long mId;
	// provider for location source
	private String provider;
	// location time
    private long time;

	// latitude of location in WGS coordinates. Field is public because of heavy usage that
	// require fast, even little bit dangerous, access. Be careful.
	public double latitude;
	// longitude of location in WGS coordinates. Field is public because of heavy usage that
	// require fast, even little bit dangerous, access. Be careful.
	public double longitude;

	// flag if altitude is set
    private boolean mHasAltitude;
	// altitude value
    private double mAltitude;

	// container for basic values
    private ExtraBasic mExtraBasic;
    // container for ANT sensor values
    private ExtraSensor mExtraSensor;

	/**
	 * Container for extended data for location class.
	 */
	private static class ExtraBasic implements Cloneable {

    	boolean hasSpeed;
    	float speed;

    	boolean hasBearing;
    	float bearing;
        
    	boolean hasAccuracy;
        float accuracy;
        
        ExtraBasic() {
            hasSpeed = false;
            speed = 0.0f;
            hasBearing = false;
            bearing = 0.0f;
            hasAccuracy = false;
            accuracy = 0.0f;
        }
        
        @Override
        public ExtraBasic clone() {
        	ExtraBasic newExtra = new ExtraBasic();
        	newExtra.hasSpeed = hasSpeed;
        	newExtra.speed = speed;
        	newExtra.hasBearing = hasBearing;
        	newExtra.bearing = bearing;
        	newExtra.hasAccuracy = hasAccuracy;
        	newExtra.accuracy = accuracy;
			return newExtra;
        }
        
        boolean hasData() {
        	return hasSpeed || hasBearing || hasAccuracy;
        }
        
        @Override
        public String toString() {
        	return Utils.toString(ExtraBasic.this, "    ");
        }
    }

	/**
	 * Container for data usually received from sensors.
	 */
	private static class ExtraSensor extends Storable implements Cloneable {

    	boolean hasHr;
    	int hr;

    	boolean hasCadence;
    	int cadence;
        
    	boolean hasSpeed;
        float speed;
        
        boolean hasPower;
        float power;
        
        boolean hasStrides;
        int strides;

        @Deprecated
        private boolean hasBattery;
        @Deprecated
        private int battery;
        
        boolean hasTemperature;
        float temperature;

        public ExtraSensor() {
        	super();
        }
        
        public ExtraSensor(DataReaderBigEndian dr) throws IOException {
        	super(dr);
        }
        
        @Override
        public ExtraSensor clone() {
        	ExtraSensor newExtra = new ExtraSensor();
        	newExtra.hasHr = hasHr;
        	newExtra.hr = hr;
        	newExtra.hasCadence = hasCadence;
        	newExtra.cadence = cadence;
        	newExtra.hasSpeed = hasSpeed;
        	newExtra.speed = speed;
        	newExtra.hasPower = hasPower;
        	newExtra.power = power;
        	newExtra.hasStrides = hasStrides;
        	newExtra.strides = strides;
        	newExtra.hasTemperature = hasTemperature;
        	newExtra.temperature = temperature;
			return newExtra;
        }
        
        boolean hasData() {
        	return hasHr || hasCadence || 
        			hasSpeed || hasPower ||
        			hasStrides || hasTemperature;
        }

		@Override
		protected int getVersion() {
			return 1;
		}

		@Override
		protected void readObject(int version, DataReaderBigEndian dr)
				throws IOException {
			hasHr = dr.readBoolean();
			hr = dr.readInt();
			hasCadence = dr.readBoolean();
			cadence = dr.readInt();
			hasSpeed = dr.readBoolean();
			speed = dr.readFloat();
			hasPower = dr.readBoolean();
			power = dr.readFloat();
			hasStrides = dr.readBoolean();
			strides = dr.readInt();
			hasBattery = dr.readBoolean();
			battery = dr.readInt();
			if (version >= 1) {
				hasTemperature = dr.readBoolean();
				temperature = dr.readFloat();
			}
		}

		@Override
		protected void writeObject(DataWriterBigEndian dw) throws IOException {
			dw.writeBoolean(hasHr);
			dw.writeInt(hr);
			dw.writeBoolean(hasCadence);
			dw.writeInt(cadence);
			dw.writeBoolean(hasSpeed);
			dw.writeFloat(speed);
			dw.writeBoolean(hasPower);
			dw.writeFloat(power);
			dw.writeBoolean(hasStrides);
			dw.writeInt(strides);
			dw.writeBoolean(hasBattery);
			dw.writeInt(battery);
			dw.writeBoolean(hasTemperature);
			dw.writeFloat(temperature);
		}

		@Override
		public void reset() {
        	hasHr = false;
        	hr = 0;
        	hasCadence = false;
        	cadence = 0;
            hasSpeed = false;
            speed = 0.0f;
            hasPower = false;
            power = 0.0f;
            hasStrides = false;
            strides = 0;
            hasBattery = false;
            battery = 0;
            hasTemperature = false;
            temperature = 0;
		}
		
        @Override
        public String toString() {
        	return Utils.toString(ExtraSensor.this, "    ");
        }
    }
    
    /**
     * Constructs a new Location.
     * @param provider the name of the location provider that generated this
     * location fix.
     */
    public Location(String provider) {
        super();
        setProvider(provider);
    }
    
    public Location(String provider, double lat, double lon) {
    	super();
    	setProvider(provider);
        setLatitude(lat);
        setLongitude(lon);
    }

	/**
	 * Empty constructor used for {@link Storable}
	 * <br>
	 * Do not use directly!
	 */
    public Location() {
    	this("");
    }
    
    public Location(DataReaderBigEndian dr) throws IOException {
        super(dr);
    }

    public Location(Location loc) {
        set(loc);
    }
    
    public Location(byte[] data) throws IOException {
        super(data);
    }

    /**
     * Sets the contents of the location to the values from the given location.
	 * @param loc source location object
     */
    public void set(Location loc) {
    	mId = loc.mId;
        provider = loc.provider;
        time = loc.time;
        latitude = loc.latitude;
        longitude = loc.longitude;
        mHasAltitude = loc.hasAltitude();
		mAltitude = loc.getAltitude();

        // set extra basic data
        if (loc.mExtraBasic != null && loc.mExtraBasic.hasData()) {
        	mExtraBasic = loc.mExtraBasic.clone();
            if (!mExtraBasic.hasData()) {
            	mExtraBasic = null;
            }
        } else {
        	mExtraBasic = null;
        }
        
        // set extra ant data
        if (loc.mExtraSensor != null && loc.mExtraSensor.hasData()) {
        	mExtraSensor = loc.mExtraSensor.clone();
            if (!mExtraSensor.hasData()) {
            	mExtraSensor = null;
            }
        } else {
        	mExtraSensor = null;
        }
    }

    /*******************************************/
    /*             OVERWRITE PART              */
    /*******************************************/
    
	@Override
	protected int getVersion() {
		return 2;
	}

	@Override
	protected void readObject(int version, DataReaderBigEndian dr) throws IOException {
		mId = dr.readLong();
		provider = dr.readString();
		time = dr.readLong();
		latitude = dr.readDouble();
		longitude = dr.readDouble();
		mHasAltitude = dr.readBoolean();
		mAltitude = dr.readDouble();

		// red basic data
		if (dr.readBoolean()) {
			mExtraBasic = new ExtraBasic();
			mExtraBasic.hasAccuracy = dr.readBoolean();
			mExtraBasic.accuracy = dr.readFloat();
			mExtraBasic.hasBearing = dr.readBoolean();
			mExtraBasic.bearing = dr.readFloat();
			mExtraBasic.hasSpeed = dr.readBoolean();
			mExtraBasic.speed = dr.readFloat();
	    	if (!mExtraBasic.hasData()) {
	    		mExtraBasic = null;
	    	}
		}
		
		// end VERSION 0
		if (version < 1) {
			return;
		}
	
		// read sensor data
		if (dr.readBoolean()) {
			if (version == 1) {
				readSensorVersion1(dr);
			} else {
				mExtraSensor = new ExtraSensor(dr);
			}
		}
	}
	
	private void readSensorVersion1(DataReaderBigEndian dr) throws IOException {
		mExtraSensor = new ExtraSensor();
		mExtraSensor.hasHr = dr.readBoolean();
		mExtraSensor.hr = dr.readInt();
		mExtraSensor.hasCadence = dr.readBoolean();
		mExtraSensor.cadence = dr.readInt();
		mExtraSensor.hasSpeed = dr.readBoolean();
		mExtraSensor.speed = dr.readFloat();
		mExtraSensor.hasPower = dr.readBoolean();
		mExtraSensor.power = dr.readFloat();
            
		if (!mExtraSensor.hasData()) {
			mExtraSensor = null;
		}
	}
	
	@Override
	protected void writeObject(DataWriterBigEndian dw) throws IOException {
		dw.writeLong(mId);
		dw.writeString(provider);
		dw.writeLong(time);
		dw.writeDouble(latitude);
		dw.writeDouble(longitude);
		dw.writeBoolean(mHasAltitude);
		dw.writeDouble(mAltitude);

		// write basic data
		if (mExtraBasic == null || !mExtraBasic.hasData()) {
			dw.writeBoolean(false);
		} else {
			dw.writeBoolean(true);
			dw.writeBoolean(mExtraBasic.hasAccuracy);
			dw.writeFloat(mExtraBasic.accuracy);
			dw.writeBoolean(mExtraBasic.hasBearing);
			dw.writeFloat(mExtraBasic.bearing);
			dw.writeBoolean(mExtraBasic.hasSpeed);
			dw.writeFloat(mExtraBasic.speed);
		}
		
		// write ant data (version 1+)
		if (mExtraSensor == null || !mExtraSensor.hasData()) {
			dw.writeBoolean(false);
		} else {
			dw.writeBoolean(true);
			mExtraSensor.write(dw);
		}
	}
	
    @Override
    public void reset() {
    	mId = -1L;
        provider = null;
        time = 0L;
        latitude = 0.0;
        longitude = 0.0;
        mExtraBasic = null;
        mExtraSensor = null;
    }
    
    /**************************************************/
    // GETTER & SETTERS
    /**************************************************/

	// ID

	/**
	 * Get defined ID of current location.
	 * @return unique ID
	 */
	public long getId() {
		return mId;
	}

	/**
	 * Set new ID parameter to current location.
	 * @param mId new ID value
	 */
	public void setId(long mId) {
		this.mId = mId;
	}

	// PROVIDER

    /**
     * Returns the name of the provider that generated this location.
	 * @return name of provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the name of the provider that generated this location.
	 * @param provider name of provider
	 * @return current object
     */
    public Location setProvider(String provider) {
    	if (provider == null) {
    		this.provider = "";
    	} else {
    		this.provider = provider;
    	}
        return this;
    }

	// TIME

    /**
     * Returns the UTC time of this fix, in milliseconds since January 1, 1970.
	 * @return timestamp value
     */
    public long getTime() {
        return time;
    }

    /**
     * Sets the UTC time of this fix, in milliseconds since January 1, 1970.
	 * @param time timestamp value
     */
    public void setTime(long time) {
    	this.time = time;
    }

    // LATITUDE

    /**
     * Returns the latitude of this fix.
	 * @return latitude value (in degrees)
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude of this fix.
	 * @param lat latitude value (in degrees)
	 * @return current object
     */
    public Location setLatitude(double lat) {
		// perform checks on range
        if (lat < -90.0 ) {
            Logger.logE(TAG, "setLatitude(" + lat + "), " +
                    "invalid latitude", new Exception(""));
            lat = -90.0;
        } else if (lat > 90.0) {
            Logger.logE(TAG, "setLatitude(" + lat + "), " +
                    "invalid latitude", new Exception(""));
            lat = 90.0;
        }

		// set value
        this.latitude = lat;
    	return this;
    }

    // LONGITUDE

    /**
     * Returns the longitude of this fix.
	 * @return longitude value (in degrees)
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude of this fix.
	 * @param lon longitude value (in degrees)
	 * @return current object
     */
    public Location setLongitude(double lon) {
		// perform checks on range
        if (lon < -180.0) {
            lon += 360.0;
        } else if (lon > 180.0) {
            lon -= 360.0;
        }

		// set value
        this.longitude = lon;
    	return this;
    }

    // ALTITUDE

    /**
     * Returns true if this fix contains altitude information, false otherwise.
	 * @return {@code true} if location has altitude
     */
    public boolean hasAltitude() {
        return mHasAltitude;
    }

    /**
     * Returns the altitude of this fix. If {@link #hasAltitude} is false, 0.0f is returned.
	 * @return altitude value (in metres) or 0.0 if altitude is not defined
     */
    public double getAltitude() {
		if (hasAltitude()) {
			return mAltitude;
		}
        return 0.0;
    }

    /**
     * Sets the altitude of this fix. Following this call, hasAltitude() will return true.
	 * @param altitude altitude value (in metres)
     */
    public void setAltitude(double altitude) {
    	this.mAltitude = altitude;
    	this.mHasAltitude = true;
    }

    /**
     * Clears the altitude of this fix. Following this call, hasAltitude() will return false.
     */
    public void removeAltitude() {
    	this.mAltitude = 0.0f;
    	this.mHasAltitude = false;
    }

    /**************************************************/
    // BASIC EXTRA DATA
    /**************************************************/
    
    // SPEED
    
    /**
     * Returns true if this fix contains speed information, false otherwise.
	 * The default implementation returns false.
	 * @return {@code true} if location has defined speed value
     */
    public boolean hasSpeed() {
		return mExtraBasic != null &&
				mExtraBasic.hasSpeed;
	}

    /**
     * Returns the speed of the device over ground in meters/second. If hasSpeed() is false, 0.0f is returned.
	 * @return speed value (in metres/sec)
     */
    public float getSpeed() {
    	if (hasSpeed()) {
    		return mExtraBasic.speed;
    	}
    	return 0.0f;
    }

    /**
     * Sets the speed of this fix, in meters/second.  Following this call, hasSpeed() will return true.
	 * @param speed speed value (in metres/sec)
     */
    public void setSpeed(float speed) {
		// check container
    	if (mExtraBasic == null) {
    		mExtraBasic = new ExtraBasic();
    	}

		// set value
    	mExtraBasic.speed = speed;
    	mExtraBasic.hasSpeed = true;
    }

    /**
     * Clears the speed of this fix.  Following this call, hasSpeed()
     * will return false.
     */
    public void removeSpeed() {
		// check container
    	if (mExtraBasic == null) {
    		return;
    	}

		// remove parameter
    	mExtraBasic.speed = 0.0f;
    	mExtraBasic.hasSpeed = false;
    	checkExtraBasic();
    }

    // BEARING
    
    /**
     * Returns true if the location is able to report bearing information, false otherwise.
	 * @return {@code true} if location has bearing value
     */
    public boolean hasBearing() {
		return mExtraBasic != null &&
				mExtraBasic.hasBearing;
	}

    /**
     * Returns the direction of travel in degrees East of true North. If hasBearing() is false, 0.0 is returned.
	 * @return bearing value (in degrees)
     */
    public float getBearing() {
    	if (hasBearing()) {
    		return mExtraBasic.bearing;
    	}
    	return 0.0f;
    }

    /**
     * Sets the bearing of this fix.  Following this call, hasBearing() will return true.
	 * @param bearing bearing value (in degrees)
     */
    public void setBearing(float bearing) {
        while (bearing < 0.0f) {
            bearing += 360.0f;
        }
        while (bearing >= 360.0f) {
            bearing -= 360.0f;
        }
        
        if (mExtraBasic == null) {
    		mExtraBasic = new ExtraBasic();
        }
    	mExtraBasic.bearing = bearing;
        mExtraBasic.hasBearing = true;
    }

    /**
     * Clears the bearing of this fix.  Following this call, hasBearing()
     * will return false.
     */
    public void removeBearing() {
		// check data
    	if (mExtraBasic == null) {
    		return;
    	}

		// remove parameter
    	mExtraBasic.bearing = 0.0f;
    	mExtraBasic.hasBearing = false;
    	checkExtraBasic();
    }

    // ACCURACY
    
    /**
     * Returns true if the provider is able to report accuracy information,
     * false otherwise.  The default implementation returns false.
	 * @return {@code true} is location has defined accuracy
     */
    public boolean hasAccuracy() {
		return mExtraBasic != null &&
				mExtraBasic.hasAccuracy;
	}

    /**
     * Returns the accuracy of the fix. If hasAccuracy() is false, 0.0 is returned.
	 * @return accuracy value (in metres)
     */
    public float getAccuracy() {
    	if (hasAccuracy()) {
    		return mExtraBasic.accuracy;
    	}
    	return 0.0f;
    }

    /**
     * Sets the accuracy of this fix.  Following this call, hasAccuracy() will return true.
	 * @param accuracy accuracy value (in metres)
     */
    public void setAccuracy(float accuracy) {
    	if (mExtraBasic == null) {
    		mExtraBasic = new ExtraBasic();
    	}
    	mExtraBasic.accuracy = accuracy;
    	mExtraBasic.hasAccuracy = true;
    }

    /**
     * Clears the accuracy of this fix.  Following this call, hasAccuracy() will return false.
     */
    public void removeAccuracy() {
		// check container
    	if (mExtraBasic == null) {
    		return;
    	}

		// remove data
    	mExtraBasic.accuracy = 0.0f;
    	mExtraBasic.hasAccuracy = false;
    	checkExtraBasic();
    }

	// TOOLS

	/**
	 * Check extra data container and remove it if no data exists.
	 */
	private void checkExtraBasic() {
    	if (!mExtraBasic.hasData()) {
    		mExtraBasic = null;
    	}
    }
    
    /**************************************************/
    // EXTRA SENSORS DATA
    /**************************************************/
    
    // HEART RATE
    
    /**
     * Returns true if the provider is able to report Heart rate information, false otherwise.
	 * The default implementation returns false.
	 * @return {@code true} if location has HRM sensor data
     */
    public boolean hasSensorHeartRate() {
		return mExtraSensor != null &&
				mExtraSensor.hasHr;
	}

    /**
     * Returns the Heart rate value in BMP. If hasSensorHeartRate() is false, 0.0 is returned.
	 * @return heart rate sensor value (in BPM)
     */
    public int getSensorHeartRate() {
    	if (hasSensorHeartRate()) {
    		return mExtraSensor.hr;
    	}
    	return 0;
    }

    /**
     * Sets the Heart rate of this fix. Following this call, hasSensorHeartRate() will return true.
	 * @param heartRate heart rate value (in BPM)
     */
    public void setSensorHeartRate(int heartRate) {
    	if (mExtraSensor == null) {
    		mExtraSensor = new ExtraSensor();
    	}
    	mExtraSensor.hr = heartRate;
    	mExtraSensor.hasHr = true;
    }

    /**
     * Clears the accuracy of this fix.  Following this call, hasSensorHeartRate()
     * will return false.
     */
    public void removeSensorHeartRate() {
		// check container
    	if (mExtraSensor == null) {
    		return;
    	}

		// remove parameter
    	mExtraSensor.hr = 0;
    	mExtraSensor.hasHr = false;
    	checkExtraSensor();
    }
    
    // CADENCE
    
    /**
     * Returns true if the provider is able to report cadence information, false otherwise.
	 * The default implementation returns false.
	 * @return {@code true} if location has defined cadence value
     */
    public boolean hasSensorCadence() {
		return mExtraSensor != null &&
				mExtraSensor.hasCadence;
	}

    /**
     * Returns the cadence value. If hasCadence() is false, 0 is returned.
	 * @return cadence value
     */
    public int getSensorCadence() {
    	if (hasSensorCadence()) {
    		return mExtraSensor.cadence;
    	}
    	return 0;
    }

    /**
     * Sets the cadence of this fix.  Following this call, hasCadence() will return true.
	 * @param cadence cadence value
     */
    public void setSensorCadence(int cadence) {
		// check container
    	if (mExtraSensor == null) {
    		mExtraSensor = new ExtraSensor();
    	}

		// set parameter
    	mExtraSensor.cadence = cadence;
    	mExtraSensor.hasCadence = true;
    }

    /**
     * Clears the cadence of this fix.  Following this call, hasCadence() will return false.
     */
    public void removeSensorCadence() {
		// check container
    	if (mExtraSensor == null) {
    		return;
    	}

		// reove parameter
    	mExtraSensor.cadence = 0;
    	mExtraSensor.hasCadence = false;
    	checkExtraSensor();
    }
    
    // SPEED
    
    /**
     * Returns true if the provider is able to report speed value, false otherwise.
	 * The default implementation returns false.
	 * @return {@code true} if location has defined sensor speed
     */
    public boolean hasSensorSpeed() {
		return mExtraSensor != null &&
				mExtraSensor.hasSpeed;
	}

    /**
     * Returns the speed of the fix in meters per sec. If hasSensorSpeed() is false, 0.0 is returned.
	 * @return sensor speed value (in metres/sec)
     */
    public float getSensorSpeed() {
    	if (hasSensorSpeed()) {
    		return mExtraSensor.speed;
    	}
    	return 0.0f;
    }

    /**
     * Sets the speed of this fix.  Following this call, hasSensorSpeed() will return true.
	 * @param speed sensor speed (in metres/sec)
     */
    public void setSensorSpeed(float speed) {
    	if (mExtraSensor == null) {
    		mExtraSensor = new ExtraSensor();
    	}
    	mExtraSensor.speed = speed;
    	mExtraSensor.hasSpeed = true;
    }

    /**
     * Clears the speed of this fix.  Following this call, hasSensorSpeed() will return false.
     */
    public void removeSensorSpeed() {
		// check container
    	if (mExtraSensor == null) {
    		return;
    	}

		// remove parameter
    	mExtraSensor.speed = 0.0f;
    	mExtraSensor.hasSpeed = false;
    	checkExtraSensor();
    }
    
    // POWER
    
    /**
     * Returns true if the provider is able to report power value, false otherwise.
	 * The default implementation returns false.
	 * @return {@code true} is location has defined power value
     */
    public boolean hasSensorPower() {
		return mExtraSensor != null &&
				mExtraSensor.hasPower;
	}

    /**
     * Returns the power of the fix in W. If hasSensorPower() is false, 0.0 is returned.
	 * @return power value (in Watts)
     */
    public float getSensorPower() {
    	if (hasSensorPower()) {
    		return mExtraSensor.power;
    	}
    	return 0.0f;
    }

    /**
     * Sets the power of this fix.  Following this call, hasSensorPower() will return true.
	 * @param power power value (in Watts)
     */
    public void setSensorPower(float power) {
    	if (mExtraSensor == null) {
    		mExtraSensor = new ExtraSensor();
    	}
    	mExtraSensor.power = power;
    	mExtraSensor.hasPower = true;
    }

    /**
     * Clears the power of this fix.  Following this call, hasSensorPower()
     * will return false.
     */
    public void removeSensorPower() {
		// check container
    	if (mExtraSensor == null) {
    		return;
    	}

		// remove parameter
    	mExtraSensor.power = 0.0f;
    	mExtraSensor.hasPower = false;
    	checkExtraSensor();
    }
    
    // STRIDES
    
    /**
     * Returns true if the provider is able to report strides value, false otherwise.
	 * The default implementation returns false.
	 * @return {@code true} if location has stride parameter
     */
    public boolean hasSensorStrides() {
		return mExtraSensor != null &&
				mExtraSensor.hasStrides;
	}

    /**
     * Returns the num of strides. If hasSensorStrides() is false, 0 is returned.
	 * @return number of strides since begin of track recording
     */
    public int getSensorStrides() {
    	if (hasSensorStrides()) {
    		return mExtraSensor.strides;
    	}
    	return 0;
    }

    /**
     * Sets the num of strides. Following this call, hasSensorStrides() will return true.
	 * @param strides number of strides
     */
    public void setSensorStrides(int strides) {
    	if (mExtraSensor == null) {
    		mExtraSensor = new ExtraSensor();
    	}
    	mExtraSensor.strides = strides;
    	mExtraSensor.hasStrides = true;
    }

    /**
     * Clears the num of strides. Following this call, hasSensorStrides() will return false.
     */
    public void removeSensorStrides() {
		// check container
    	if (mExtraSensor == null) {
    		return;
    	}

		// remove parameter
    	mExtraSensor.strides = 0;
    	mExtraSensor.hasStrides = false;
    	checkExtraSensor();
    }
    
    // TEMPERATURE
    
    /**
     * Returns true if the provider is able to report temperature value, false otherwise.
	 * The default implementation returns false.
	 * @return {@code true} is sensor has temperature
     */
    public boolean hasSensorTemperature() {
		return mExtraSensor != null &&
				mExtraSensor.hasTemperature;
	}

    /**
     * Returns the temperature value. If hasSensorTemperature() is false, 0.0f is returned.
	 * @return temperature value (in °C)
     */
    public float getSensorTemperature() {
    	if (hasSensorTemperature()) {
    		return mExtraSensor.temperature;
    	}
    	return 0.0f;
    }

    /**
     * Sets the temperature value. Following this call, hasSensorTemperature() will return true.
	 * @param temperature temperature value (in °C)
     */
    public void setSensorTemperature(float temperature) {
		// check container
    	if (mExtraSensor == null) {
    		mExtraSensor = new ExtraSensor();
    	}

		// set parameter
    	mExtraSensor.temperature = temperature;
    	mExtraSensor.hasTemperature = true;
    }

    /**
     * Clears the temperature value. Following this call, hasSensorTemperature()
     * will return false.
     */
    public void removeSensorTemperature() {
		// check container
    	if (mExtraSensor == null) {
    		return;
    	}

		// remove parameter
    	mExtraSensor.temperature = 0.0f;
    	mExtraSensor.hasTemperature = false;
    	checkExtraSensor();
    }
    
    // PRIVATE
    
    private void checkExtraSensor() {
    	if (!mExtraSensor.hasData()) {
    		mExtraSensor = null;
    	}
    }
    
    /**************************************************/
    // UTILS PART
    /**************************************************/

	@Override
	public String toString() {
		return "Location [" +
				"tag:" + provider + ", " +
				"lon:" + longitude + ", " +
				"lat:" + latitude + ", " +
				"alt:" + mAltitude + "]";
	}

	/**
     * Returns the approximate distance in meters between this
     * location and the given location.  Distance is defined using
     * the WGS84 ellipsoid.
     *
     * @param dest the destination location
     * @return the approximate distance in meters
     */
    public float distanceTo(Location dest) {
    	LocationCompute com = new LocationCompute(this);
    	return com.distanceTo(dest);
    }

    /**
     * Returns the approximate initial bearing in degrees East of true
     * North when traveling along the shortest path between this
     * location and the given location.  The shortest path is defined
     * using the WGS84 ellipsoid.  Locations that are (nearly)
     * antipodal may produce meaningless results.
     *
     * @param dest the destination location
     * @return the initial bearing in degrees
     */
    public float bearingTo(Location dest) {
    	LocationCompute com = new LocationCompute(this);
    	return com.bearingTo(dest);
    }
    
    /**
     * Compute bearing and distance values at once
     * @param dest the destination location
     * @return array with float[0] - distance (in metres),
     *  float[1] - bearing (in degree)
     */
    public float[] distanceAndBearingTo(Location dest) {
    	LocationCompute com = new LocationCompute(this);
    	return new float[] {com.distanceTo(dest), com.bearingTo(dest)};
    }

    /**
     * Check if at least some speed is stored.
     * @return {@code true} if speed is stored in this object
     */
    public boolean hasSpeedToDisplay() {
        return hasSpeed() || hasSensorSpeed();
    }

    /**
     * Get stored speed useful for display to users. If speed from sensors is stored (more
     * precise) it is returned. Otherwise basic GPS speed is returned.
     * @return speed for display purpose
     */
    public float getSpeedToDisplay() {
        if (hasSensorSpeed()) {
            return getSensorSpeed();
        }
        return getSpeed();
    }

    /**
     * Clear all attached sensors values.
     */
	public void removeSensorAll() {
		mExtraSensor = null;
	}
}
