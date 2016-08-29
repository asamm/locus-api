/*  
 * Copyright 2011, Asamm Software, s.r.o.
 * 
 * This file is part of LocusAddonPublicLib.
 * 
 * LocusAddonPublicLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * LocusAddonPublicLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with LocusAddonPublicLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package locus.api.objects.geocaching;

import java.io.IOException;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;

public class GeocachingWaypoint extends Storable {

	// tag for logger
	private static final String TAG = "GeocachingWaypoint";

	/*
	 * Changes:
	 * May 2014 - due to changes on GroundSpeak side, two Wpts are now renamed.
	 * 		more: http://support.groundspeak.com/index.php?pg=kb.page&id=72
	 */
	@Deprecated
	public static final String CACHE_WAYPOINT_TYPE_QUESTION = 
			"Question to Answer";
	public static final String CACHE_WAYPOINT_TYPE_VIRTUAL_STAGE = 
			"Virtual Stage";
	public static final String CACHE_WAYPOINT_TYPE_FINAL =
			"Final Location";
	public static final String CACHE_WAYPOINT_TYPE_PARKING = 
			"Parking Area";
	public static final String CACHE_WAYPOINT_TYPE_TRAILHEAD = 
			"Trailhead";
	@Deprecated
	public static final String CACHE_WAYPOINT_TYPE_STAGES = 
			"Stages of a Multicache";
	public static final String CACHE_WAYPOINT_TYPE_PHYSICAL_STAGE = 
			"Physical Stage";
	public static final String CACHE_WAYPOINT_TYPE_REFERENCE =
			"Reference Point";
	
	// PARAMETERS
	
	// code of wpt
	private String mCode;
	// name of waypoint
	private String mName;
	// description (may be HTML code)
	private String mDesc;
    // flag if description was already modified by user
    private boolean mDescModified;
	// type of waypoint (defined above as 'CACHE_WAYPOINT_TYPE_...)
	private String mType;
	// image URL to this wpt (optional)
	private String mTypeImagePath;
	// longitude of waypoint
	private double mLon;
	// latitude of waypoint
	private double mLat;

    /**
     * Empty constructor.
     */
	public GeocachingWaypoint() {
		super();
	}
	
    /**************************************************/
    // PARAMETERS
	/**************************************************/
	
	// CODE
	
	public String getCode() {
		return mCode;
	}
	
	public void setCode(String code) {
		if (code == null) {
			Logger.logD(TAG, "setCode(), empty parameter");
			code = "";
		}
		this.mCode = code;
	}
	
	// NAME
	
	public String getName() {
		return mName;
	}

	public void setName(String name) {
		if (name == null) {
			Logger.logD(TAG, "setName(), empty parameter");
			name = "";
		}
		this.mName = name;
	}
	
	// DESC

    /**
     * Get description of waypoint.
     * @return description
     */
	public String getDesc() {
		return mDesc;
	}

    /**
     * Set new description to current waypoint.
     * @param desc new description
     */
	public void setDesc(String desc) {
		if (desc == null) {
			Logger.logD(TAG, "setDesc(), empty parameter");
			desc = "";
		}
		this.mDesc = desc;
	}

    // DESCRIPTION MODIFIED

    /**
     * Check if description was already modified by user.
     * @return <code>true</code> if already modified by user
     */
    public boolean isDescModified() {
        return mDescModified;
    }

    /**
     * Set description modified as user.
     * @param modified <code>true</code> as modified
     */
    public void setDescModified(boolean modified) {
        this.mDescModified = modified;
    }

	// IMAGE PATH
	
	public String getTypeImagePath() {
		return mTypeImagePath;
	}

	public void setTypeImagePath(String typeImagePath) {
		if (typeImagePath == null) {
			Logger.logD(TAG, "setTypeImagePath(), empty parameter");
			typeImagePath = "";
		}
		this.mTypeImagePath = typeImagePath;
	}
	
	// LON

	public double getLon() {
		return mLon;
	}
	
	public void setLon(double lon) {
		this.mLon = lon;
	}

	// LAT
	
	public double getLat() {
		return mLat;
	}

	public void setLat(double lat) {
		this.mLat = lat;
	}
	
	// TYPE
	
	public String getType() {
		return mType;
	}
	
	public void setType(String type) {
		if (type == null) {
			Logger.logD(TAG, "setType(), empty parameter");
			type = "";
		}
		
		// improve text
		if (type.toLowerCase().startsWith("waypoint|")) {
			type = type.substring("waypoint|".length());
		}
		this.mType = type;
	}

    /**************************************************/
    // STORABLE PART
	/**************************************************/
	
	@Override
	protected int getVersion() {
		return 1;
	}

	@Override
	protected void readObject(int version, DataReaderBigEndian dr)
			throws IOException {
		mCode = dr.readString();
		mName = dr.readString();
		mDesc = dr.readString();
		mType = dr.readString();
		mTypeImagePath = dr.readString();
		mLon = dr.readDouble();
		mLat = dr.readDouble();

        // V1

        if (version >= 1) {
            mDescModified = dr.readBoolean();
        }
	}

	@Override
	protected void writeObject(DataWriterBigEndian dw) throws IOException {
		dw.writeString(mCode);
		dw.writeString(mName);
		dw.writeString(mDesc);
		dw.writeString(mType);
		dw.writeString(mTypeImagePath);
		dw.writeDouble(mLon);
		dw.writeDouble(mLat);

        // V1

        dw.writeBoolean(mDescModified);
	}

	@Override
	public void reset() {
		mCode = "";
		mName = "";
		mDesc = "";
		mType = "";
		mTypeImagePath = "";
		mLon = 0.0;
		mLat = 0.0;

        // V1

        mDescModified = false;
	}
}