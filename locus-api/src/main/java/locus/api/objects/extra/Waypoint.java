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

import locus.api.objects.GeoData;
import locus.api.objects.Storable;
import locus.api.objects.geocaching.GeocachingData;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;

public class Waypoint extends GeoData {
	
	private static final String TAG = "Waypoint";
	
	/* mLoc of this point */
	private Location loc;
	
	/* additional geoCaching data */
	public GeocachingData gcData;

	public Waypoint(String name, Location loc) {
		super();
		setName(name);
		this.loc = loc;
	}
	
	/**
	 * Empty constructor used for {@link Storable}
	 * <br>
	 * Do not use directly!
	 */
	public Waypoint() {
		this("", new Location(""));
	}
	
	public Waypoint(DataReaderBigEndian dr) throws IOException {
		super(dr);
	}
	
	public Waypoint(byte[] data) throws IOException {
		super(data);
	}
	
    /*******************************************/
    /*             STORABLE PART               */
    /*******************************************/
	
	@Override
	protected int getVersion() {
		return 2;
	}

	@Override
	protected void readObject(int version, DataReaderBigEndian dr) throws IOException {
		setId(dr.readLong());
		name = dr.readString();
		loc = new Location(dr);
		
		// read extra data
		readExtraData(dr);
		readStyles(dr);
		
		// read geocaching
		gcData = readGeocachingData(dr);
		
		// V1
		if (version >= 1) {
			timeCreated = dr.readLong();
		}

		// V2
		if (version >= 2) {
			setReadWriteMode(ReadWriteMode.values()[dr.readInt()]);
		}
	}

	@Override
	protected void writeObject(DataWriterBigEndian dw) throws IOException {
		dw.writeLong(getId());
		dw.writeString(name);
		loc.write(dw);

		// write extra data
		writeExtraData(dw);
		writeStyles(dw);

		// write geocaching data
		writeGeocachingData(dw);
		
		// V1
		dw.writeLong(timeCreated);

		// V2
		dw.writeInt(getReadWriteMode().ordinal());
	}

	@Override
	public void reset() {
		setId(-1);
		name = "";
		loc = null;
		extraData = null;
		styleNormal = null;
		styleHighlight = null;
		gcData = null;
		
		// V1
		timeCreated = System.currentTimeMillis();

		// V2
		setReadWriteMode(ReadWriteMode.READ_WRITE);
	}
	
	public static GeocachingData readGeocachingData(DataReaderBigEndian dr) throws IOException {
		if (dr.readBoolean()) {
			return new GeocachingData(dr);
		} else {
			return null;
		}
	}
	
	private void writeGeocachingData(DataWriterBigEndian dw) throws IOException {
		if (gcData != null) {
			dw.writeBoolean(true);
			gcData.write(dw);
		} else {
			dw.writeBoolean(false);
		}
	}

    /*******************************************/
    /*             GET & SET PART              */
    /*******************************************/
	
	public static final String TAG_EXTRA_CALLBACK = "TAG_EXTRA_CALLBACK";
	public static final String TAG_EXTRA_ON_DISPLAY = "TAG_EXTRA_ON_DISPLAY";
	
	public Location getLocation() {
		return loc;
	}

    public void setLocation(Location loc) {
        // check location
        if (loc == null) {
            Logger.logW(TAG, "setLocation(null), " +
                    "unable to set invalid Location object");
            return;
        }

        // finally set location
        this.loc = loc;
    }
	
	public String getExtraCallback() {
		if (extraData != null) {
			return extraData.getParameter(GeoDataExtra.PAR_INTENT_EXTRA_CALLBACK);
		}
		return null;
	}
	
	/**
	 * Simply allow set callback value on point. This appear when you click on point
	 * and then under last button will be your button. Clicking on it, launch by you,
	 * defined intent
	 * <br><br>
	 * Do not forget to set this http://developer.android.com/guide/topics/manifest/activity-element.html#exported
	 * to your activity, if you'll set callback to other then launcher activity
	 * @param btnName Name displayed on button
	 * @param packageName this value is used for creating intent that
	 *  will be called in callback (for example com.super.application)
	 * @param className the name of the class inside of com.super.application
	 *  that implements the component (for example com.super.application.Main)
	 * @param returnDataName String under which data will be stored. Can be
	 *  retrieved by String data = getIntent.getStringExtra("returnData");
	 * @param returnDataValue String under which data will be stored. Can be
	 *  retrieved by String data = getIntent.getStringExtra("returnData");
	 */
	public void setExtraCallback(String btnName, String packageName, String className,
			String returnDataName, String returnDataValue) {
        // prepare callback
        String callBack = GeoDataExtra.generateCallbackString(btnName, packageName, className,
                returnDataName, returnDataValue);
        if (callBack.length() == 0) {
            return;
        }

        // generate final text
		StringBuilder b = new StringBuilder();
		b.append(TAG_EXTRA_CALLBACK).append(";");
		b.append(callBack).append(";");

        // finally insert parameter
		addParameter(GeoDataExtra.PAR_INTENT_EXTRA_CALLBACK, b.toString());
	}
	
	/**
	 * If you want to remove PAR_INTENT_EXTRA_CALLBACK parametr from Locus database,
	 * you need to send "clear" value in updated waypoint back to Locus. After that,
	 * Locus will remove this parameter from new stored point.
	 * <br><br>
	 * Second alternative, how to remove this callback, is to send new waypoints
	 * with forceOverwrite parameter set to <code>true</code>, that will overwrite
	 * completely all data 
	 */
	public void removeExtraCallback() {
		addParameter(GeoDataExtra.PAR_INTENT_EXTRA_CALLBACK, "clear");
	}
	
	public String getExtraOnDisplay() {
		if (extraData != null) {
			return extraData.getParameter(GeoDataExtra.PAR_INTENT_EXTRA_ON_DISPLAY);
		}
		return null;
	}
	
	/**
	 * Extra feature that allow to send to locus only partial point data. When you click on
	 * point (in time when small point dialog should appear), locus send intent to your app,
	 * you can then fill complete point and send it back to Locus. Clear and clever
	 * <br><br>
	 * Do not forget to set this http://developer.android.com/guide/topics/manifest/activity-element.html#exported
	 * to your activity, if you'll set callback to other then launcher activity
	 * 
	 * @param packageName this value is used for creating intent that
	 *  will be called in callback (for example com.super.application)
	 * @param className the name of the class inside of com.super.application
	 *  that implements the component (for example com.super.application.Main)
	 * @param returnDataName String under which data will be stored. Can be
	 *  retrieved by String data = getIntent.getStringExtra("returnData");
	 * @param returnDataValue value that will be received when you try to get
     *  data from received response
	 */
	public void setExtraOnDisplay(String packageName, String className,
			String returnDataName, String returnDataValue) {
		StringBuilder sb = new StringBuilder();
		sb.append(TAG_EXTRA_ON_DISPLAY).append(";");
		sb.append(packageName).append(";");
		sb.append(className).append(";");
		sb.append(returnDataName).append(";");
		sb.append(returnDataValue).append(";");
		addParameter(GeoDataExtra.PAR_INTENT_EXTRA_ON_DISPLAY, sb.toString());
	}
	
	/**
	 * If you want to remove PAR_INTENT_EXTRA_ON_DISPLAY parameter from Locus database,
	 * you need to send "clear" value in updated waypoint back to Locus. After that,
	 * Locus will remove this parameter from new stored point.
	 * <br><br>
	 * Second alternative, how to remove this callback, is to send new waypoints
	 * with forceOverwrite parameter set to <code>true</code>, that will overwrite
	 * completely all data 
	 */
	public void removeExtraOnDisplay() {
		addParameter(GeoDataExtra.PAR_INTENT_EXTRA_ON_DISPLAY, "clear");
	}

	public byte[] getGeocachingData() {
		try {
			DataWriterBigEndian dw = new DataWriterBigEndian();
			writeGeocachingData(dw);
			return dw.toByteArray();
		} catch (IOException e) {
			Logger.logE(TAG, "getGeocachingData()", e);
			return null;
		}
	}
	
	public void setGeocachingData(byte[] data) {
		try {
			gcData = readGeocachingData(new DataReaderBigEndian(data));
		} catch (Exception e) {
			Logger.logE(TAG, "setGeocachingData(" + data + ")", e);
			gcData = null;
		}
	}
}
