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

public class Location extends Storable {

    private static final String TAG = Location.class.getSimpleName();

	// location unique ID
	private long id;
	// provider for location source
	private String provider;
	// location time
    private long time;
	
	// latitude of location in WGS coordinates
	public double latitude;
	// longitude of location in WGS coordinates
	public double longitude;

	// flag if altitude is set
    private boolean hasAltitude;
	// altitude value
    private double altitude;

	// container for basic values
    private ExtraBasic extraBasic;
    // container for ANT sensor values
    private ExtraSensor extraSensor;
    
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
	 * <br />
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
     */
    public void set(Location l) {
    	id = l.id;
        provider = new String(l.provider);
        time = l.time;
        latitude = l.latitude;
        longitude = l.longitude;
        hasAltitude = l.hasAltitude();
        altitude = l.getAltitude();

        // set extra basic data
        if (l.extraBasic != null && l.extraBasic.hasData()) {
        	extraBasic = l.extraBasic.clone();
            if (!extraBasic.hasData()) {
            	extraBasic = null;
            }
        } else {
        	extraBasic = null;
        }
        
        // set extra ant data
        if (l.extraSensor != null && l.extraSensor.hasData()) {
        	extraSensor = l.extraSensor.clone();
            if (!extraSensor.hasData()) {
            	extraSensor = null;
            }
        } else {
        	extraSensor = null;
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
		id = dr.readLong();
		provider = dr.readString();
		time = dr.readLong();
		latitude = dr.readDouble();
		longitude = dr.readDouble();
		hasAltitude = dr.readBoolean();
		altitude = dr.readDouble();

		// red basic data
		if (dr.readBoolean()) {
			extraBasic = new ExtraBasic();
			extraBasic.hasAccuracy = dr.readBoolean();
			extraBasic.accuracy = dr.readFloat();
			extraBasic.hasBearing = dr.readBoolean();
			extraBasic.bearing = dr.readFloat();
			extraBasic.hasSpeed = dr.readBoolean();
			extraBasic.speed = dr.readFloat();
	    	if (!extraBasic.hasData()) {
	    		extraBasic = null;
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
				extraSensor = new ExtraSensor(dr);
			}
		}
	}
	
	private void readSensorVersion1(DataReaderBigEndian dr) throws IOException {
		extraSensor = new ExtraSensor();
		extraSensor.hasHr = dr.readBoolean();
		extraSensor.hr = dr.readInt();
		extraSensor.hasCadence = dr.readBoolean();
		extraSensor.cadence = dr.readInt();
		extraSensor.hasSpeed = dr.readBoolean();
		extraSensor.speed = dr.readFloat();
		extraSensor.hasPower = dr.readBoolean();
		extraSensor.power = dr.readFloat();
            
		if (!extraSensor.hasData()) {
			extraSensor = null;
		}
	}
	
	@Override
	protected void writeObject(DataWriterBigEndian dw) throws IOException {
		dw.writeLong(id);
		dw.writeString(provider);
		dw.writeLong(time);
		dw.writeDouble(latitude);
		dw.writeDouble(longitude);
		dw.writeBoolean(hasAltitude);
		dw.writeDouble(altitude);

		// write basic data
		if (extraBasic == null || !extraBasic.hasData()) {
			dw.writeBoolean(false);
		} else {
			dw.writeBoolean(true);
			dw.writeBoolean(extraBasic.hasAccuracy);
			dw.writeFloat(extraBasic.accuracy);
			dw.writeBoolean(extraBasic.hasBearing);
			dw.writeFloat(extraBasic.bearing);
			dw.writeBoolean(extraBasic.hasSpeed);
			dw.writeFloat(extraBasic.speed);
		}
		
		// write ant data (version 1+)
		if (extraSensor == null || !extraSensor.hasData()) {
			dw.writeBoolean(false);
		} else {
			dw.writeBoolean(true);
			extraSensor.write(dw);
		}
	}
	
    @Override
    public void reset() {
    	id = -1L;
        provider = null;
        time = 0L;
        latitude = 0.0;
        longitude = 0.0;
        extraBasic = null;
        extraSensor = null;
    }
    
    /**************************************************/
    /*                 GETTER & SETTERS               */
    /**************************************************/
    
	public long getId() {
		return id;
	}

	public void setId(long mId) {
		this.id = mId;
	}
	
    /**
     * Returns the name of the provider that generated this fix,
     * or null if it is not associated with a provider.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the name of the provider that generated this fix.
     */
    public Location setProvider(String provider) {
    	if (provider == null) {
    		this.provider = "";
    	} else {
    		this.provider = provider;
    	}
        return this;
    }

    /**
     * Returns the UTC time of this fix, in milliseconds since January 1,
     * 1970.
     */
    public long getTime() {
        return time;
    }

    /**
     * Sets the UTC time of this fix, in milliseconds since January 1,
     * 1970.
     */
    public void setTime(long time) {
    	this.time = time;
    }

    // LATITUDE

    /**
     * Returns the latitude of this fix.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude of this fix.
     */
    public Location setLatitude(double lat) {
        if (lat < -90.0 ) {
            Logger.logE(TAG, "setLatitude(" + lat + "), " +
                    "invalid latitude", new Exception(""));
            lat = -90.0;
        } else if (lat > 90.0) {
            Logger.logE(TAG, "setLatitude(" + lat + "), " +
                    "invalid latitude", new Exception(""));
            lat = 90.0;
        }
        this.latitude = lat;
    	return this;
    }

    // LONGITUDE

    /**
     * Returns the longitude of this fix.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude of this fix.
     */
    public Location setLongitude(double lon) {
        if (lon < -180.0) {
            lon += 360.0;
        } else if (lon > 180.0) {
            lon -= 360.0;
        }
        this.longitude = lon;
    	return this;
    }

    // ALTITUDE

    /**
     * Returns true if this fix contains altitude information, false
     * otherwise.
     */
    public boolean hasAltitude() {
        return hasAltitude;
    }

    /**
     * Returns the altitude of this fix.  If {@link #hasAltitude} is false,
     * 0.0f is returned.
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     * Sets the altitude of this fix.  Following this call,
     * hasAltitude() will return true.
     */
    public void setAltitude(double altitude) {
    	this.altitude = altitude;
    	this.hasAltitude = true;
    }

    /**
     * Clears the altitude of this fix.  Following this call,
     * hasAltitude() will return false.
     */
    public void removeAltitude() {
    	this.altitude = 0.0f;
    	this.hasAltitude = false;
    }

    /**************************************************/
    // BASIC EXTRA DATA
    /**************************************************/
    
    // SPEED
    
    /**
     * Returns true if this fix contains speed information, false
     * otherwise.  The default implementation returns false.
     */
    public boolean hasSpeed() {
    	if (extraBasic == null) {
    		return false;
    	}
        return extraBasic.hasSpeed;
    }

    /**
     * Returns the speed of the device over ground in meters/second.
     * If hasSpeed() is false, 0.0f is returned.
     */
    public float getSpeed() {
    	if (hasSpeed()) {
    		return extraBasic.speed;
    	}
    	return 0.0f;
    }

    /**
     * Sets the speed of this fix, in meters/second.  Following this
     * call, hasSpeed() will return true.
     */
    public void setSpeed(float speed) {
    	if (extraBasic == null) {
    		extraBasic = new ExtraBasic();
    	}
    	extraBasic.speed = speed;
    	extraBasic.hasSpeed = true;
    }

    /**
     * Clears the speed of this fix.  Following this call, hasSpeed()
     * will return false.
     */
    public void removeSpeed() {
    	if (extraBasic == null) {
    		return;
    	}
    	extraBasic.speed = 0.0f;
    	extraBasic.hasSpeed = false;
    	checkExtraBasic();
    }

    // BEARING
    
    /**
     * Returns true if the provider is able to report bearing information,
     * false otherwise.  The default implementation returns false.
     */
    public boolean hasBearing() {
    	if (extraBasic == null) {
    		return false;
    	}
        return extraBasic.hasBearing;
    }

    /**
     * Returns the direction of travel in degrees East of true
     * North. If hasBearing() is false, 0.0 is returned.
     */
    public float getBearing() {
    	if (hasBearing()) {
    		return extraBasic.bearing;
    	}
    	return 0.0f;
    }

    /**
     * Sets the bearing of this fix.  Following this call, hasBearing()
     * will return true.
     */
    public void setBearing(float bearing) {
        while (bearing < 0.0f) {
            bearing += 360.0f;
        }
        while (bearing >= 360.0f) {
            bearing -= 360.0f;
        }
        
        if (extraBasic == null) {
    		extraBasic = new ExtraBasic();
        }
    	extraBasic.bearing = bearing;
        extraBasic.hasBearing = true;
    }

    /**
     * Clears the bearing of this fix.  Following this call, hasBearing()
     * will return false.
     */
    public void removeBearing() {
    	if (extraBasic == null) {
    		return;
    	}
    	extraBasic.bearing = 0.0f;
    	extraBasic.hasBearing = false;
    	checkExtraBasic();
    }

    // ACCURACY
    
    /**
     * Returns true if the provider is able to report accuracy information,
     * false otherwise.  The default implementation returns false.
     */
    public boolean hasAccuracy() {
    	if (extraBasic == null) {
    		return false;
    	}
        return extraBasic.hasAccuracy;
    }

    /**
     * Returns the accuracy of the fix in meters. If hasAccuracy() is false,
     * 0.0 is returned.
     */
    public float getAccuracy() {
    	if (hasAccuracy()) {
    		return extraBasic.accuracy;
    	}
    	return 0.0f;
    }

    /**
     * Sets the accuracy of this fix.  Following this call, hasAccuracy()
     * will return true.
     */
    public void setAccuracy(float accuracy) {
    	if (extraBasic == null) {
    		extraBasic = new ExtraBasic();
    	}
    	extraBasic.accuracy = accuracy;
    	extraBasic.hasAccuracy = true;
    }

    /**
     * Clears the accuracy of this fix.  Following this call, hasAccuracy()
     * will return false.
     */
    public void removeAccuracy() {
    	if (extraBasic == null) {
    		return;
    	}
    	extraBasic.accuracy = 0.0f;
    	extraBasic.hasAccuracy = false;
    	checkExtraBasic();
    }
    
    private void checkExtraBasic() {
    	if (!extraBasic.hasData()) {
    		extraBasic = null;
    	}
    }
    
    /**************************************************/
    // EXTRA SENSORS DATA
    /**************************************************/
    
    // HEART RATE
    
    /**
     * Returns true if the provider is able to report Heart rate information,
     * false otherwise.  The default implementation returns false.
     */
    public boolean hasSensorHeartRate() {
    	if (extraSensor == null) {
    		return false;
    	}
        return extraSensor.hasHr;
    }

    /**
     * Returns the Heart rate value in BMP. If hasSensorHeartRate() is false,
     * 0.0 is returned.
     */
    public int getSensorHeartRate() {
    	if (hasSensorHeartRate()) {
    		return extraSensor.hr;
    	}
    	return 0;
    }

    /**
     * Sets the Heart rate of this fix. Following this call, hasSensorHeartRate()
     * will return true.
     */
    public void setSensorHeartRate(int heartRate) {
    	if (extraSensor == null) {
    		extraSensor = new ExtraSensor();
    	}
    	extraSensor.hr = heartRate;
    	extraSensor.hasHr = true;
    }

    /**
     * Clears the accuracy of this fix.  Following this call, hasSensorHeartRate()
     * will return false.
     */
    public void removeSensorHeartRate() {
    	if (extraSensor == null) {
    		return;
    	}
    	extraSensor.hr = 0;
    	extraSensor.hasHr = false;
    	checkExtraSensor();
    }
    
    // CADENCE
    
    /**
     * Returns true if the provider is able to report cadence information,
     * false otherwise.  The default implementation returns false.
     */
    public boolean hasSensorCadence() {
    	if (extraSensor == null) {
    		return false;
    	}
        return extraSensor.hasCadence;
    }

    /**
     * Returns the cadence value. If hasCadence() is false, 0 is returned.
     */
    public int getSensorCadence() {
    	if (hasSensorCadence()) {
    		return extraSensor.cadence;
    	}
    	return 0;
    }

    /**
     * Sets the cadence of this fix.  Following this call, hasCadence()
     * will return true.
     */
    public void setSensorCadence(int cadence) {
    	if (extraSensor == null) {
    		extraSensor = new ExtraSensor();
    	}
    	extraSensor.cadence = cadence;
    	extraSensor.hasCadence = true;
    }

    /**
     * Clears the cadence of this fix.  Following this call, hasCadence()
     * will return false.
     */
    public void removeSensorCadence() {
    	if (extraSensor == null) {
    		return;
    	}
    	extraSensor.cadence = 0;
    	extraSensor.hasCadence = false;
    	checkExtraSensor();
    }
    
    // SPEED
    
    /**
     * Returns true if the provider is able to report speed value,
     * false otherwise.  The default implementation returns false.
     */
    public boolean hasSensorSpeed() {
    	if (extraSensor == null) {
    		return false;
    	}
        return extraSensor.hasSpeed;
    }

    /**
     * Returns the speed of the fix in meters per sec. If hasSensorSpeed() is false,
     * 0.0 is returned.
     */
    public float getSensorSpeed() {
    	if (hasSensorSpeed()) {
    		return extraSensor.speed;
    	}
    	return 0.0f;
    }

    /**
     * Sets the speed of this fix.  Following this call, hasSensorSpeed()
     * will return true.
     */
    public void setSensorSpeed(float speed) {
    	if (extraSensor == null) {
    		extraSensor = new ExtraSensor();
    	}
    	extraSensor.speed = speed;
    	extraSensor.hasSpeed = true;
    }

    /**
     * Clears the speed of this fix.  Following this call, hasSensorSpeed()
     * will return false.
     */
    public void removeSensorSpeed() {
    	if (extraSensor == null) {
    		return;
    	}
    	extraSensor.speed = 0.0f;
    	extraSensor.hasSpeed = false;
    	checkExtraSensor();
    }
    
    // POWER
    
    /**
     * Returns true if the provider is able to report power value,
     * false otherwise.  The default implementation returns false.
     */
    public boolean hasSensorPower() {
    	if (extraSensor == null) {
    		return false;
    	}
        return extraSensor.hasPower;
    }

    /**
     * Returns the power of the fix in W. If hasSensorPower() is false,
     * 0.0 is returned.
     */
    public float getSensorPower() {
    	if (hasSensorPower()) {
    		return extraSensor.power;
    	}
    	return 0.0f;
    }

    /**
     * Sets the power of this fix.  Following this call, hasSensorPower()
     * will return true.
     */
    public void setSensorPower(float power) {
    	if (extraSensor == null) {
    		extraSensor = new ExtraSensor();
    	}
    	extraSensor.power = power;
    	extraSensor.hasPower = true;
    }

    /**
     * Clears the power of this fix.  Following this call, hasSensorPower()
     * will return false.
     */
    public void removeSensorPower() {
    	if (extraSensor == null) {
    		return;
    	}
    	extraSensor.power = 0.0f;
    	extraSensor.hasPower = false;
    	checkExtraSensor();
    }
    
    // STRIDES
    
    /**
     * Returns true if the provider is able to report strides value,
     * false otherwise.  The default implementation returns false.
     */
    public boolean hasSensorStrides() {
    	if (extraSensor == null) {
    		return false;
    	}
        return extraSensor.hasStrides;
    }

    /**
     * Returns the num of strides. If hasSensorStrides() is false,
     * 0 is returned.
     */
    public int getSensorStrides() {
    	if (hasSensorStrides()) {
    		return extraSensor.strides;
    	}
    	return 0;
    }

    /**
     * Sets the num of strides. Following this call, hasSensorStrides()
     * will return true.
     */
    public void setSensorStrides(int strides) {
    	if (extraSensor == null) {
    		extraSensor = new ExtraSensor();
    	}
    	extraSensor.strides = strides;
    	extraSensor.hasStrides = true;
    }

    /**
     * Clears the num of strides. Following this call, hasSensorStrides()
     * will return false.
     */
    public void removeSensorStrides() {
    	if (extraSensor == null) {
    		return;
    	}
    	extraSensor.strides = 0;
    	extraSensor.hasStrides = false;
    	checkExtraSensor();
    }
    
    // TEMPERATURE
    
    /**
     * Returns true if the provider is able to report temperature value,
     * false otherwise.  The default implementation returns false.
     */
    public boolean hasSensorTemperature() {
    	if (extraSensor == null) {
    		return false;
    	}
        return extraSensor.hasTemperature;
    }

    /**
     * Returns the temperature value. If hasSensorTemperature() is false,
     * 0.0f is returned.
     */
    public float getSensorTemperature() {
    	if (hasSensorTemperature()) {
    		return extraSensor.temperature;
    	}
    	return 0.0f;
    }

    /**
     * Sets the temperature value. Following this call, hasSensorTemperature()
     * will return true.
     */
    public void setSensorTemperature(float temperature) {
    	if (extraSensor == null) {
    		extraSensor = new ExtraSensor();
    	}
    	extraSensor.temperature = temperature;
    	extraSensor.hasTemperature = true;
    }

    /**
     * Clears the temperature value. Following this call, hasSensorTemperature()
     * will return false.
     */
    public void removeSensorTemperature() {
    	if (extraSensor == null) {
    		return;
    	}
    	extraSensor.temperature = 0.0f;
    	extraSensor.hasTemperature = false;
    	checkExtraSensor();
    }
    
    // PRIVATE
    
    private void checkExtraSensor() {
    	if (!extraSensor.hasData()) {
    		extraSensor = null;
    	}
    }
    
    /**************************************************/
    // UTILS PART
    /**************************************************/
    
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
     * @return <code>true</code> if speed is stored in this object
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
    
    /**************************************************/
    /*                      UTILS                     */
    /**************************************************/
    
    @Override
    public String toString() {
        return "Location [" +
                "tag:" + provider + ", " +
                "lon:" + longitude + ", " +
                "lat:" + latitude + ", " +
                "alt:" + altitude + "]";
//    	return Utils.toString(this, "");
    }

    /**
     * Clear all attached sensors values.
     */
	public void removeSensorAll() {
		extraSensor = null;
	}
}
