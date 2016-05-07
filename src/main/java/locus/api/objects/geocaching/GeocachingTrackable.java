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

public class GeocachingTrackable extends Storable {
	
	private static final String TAG = GeocachingTrackable.class.getSimpleName();
	
	// PARAMETERS
	
	/*
	 * ID of trackable. Currently used for GeoKrety web server
	 */
	private long mId;
	/*
	 * name of travel bug
	 */
	private String mName;
	/*
	 * image url to this travel bug
	 */
	private String mImgUrl;
	/*
	 * URL to trackable object. This is very important value, because URL contain TBCode, like this
	 * http://www.geocaching.com/track/details.aspx?tracker=TB4342X
	 */
	private String mSrcDetails;
	
	/* 
	 * original owner of TB
	 */
	private String mOriginalOwner;
	/* 
	 * current owner of TB
	 */
	private String mCurrentOwner;
	/*
	 * time of release to public (long since 1.1.1970 in ms)
	 */
	private long mReleased;
	/*
	 * origin place
	 */
	private String mOrigin;
	/*
	 * goal of this TB
	 */
	private String mGoal;
	/*
	 * details 
	 */
	private String mDetails;
	
	public GeocachingTrackable() {
		super();
	}
	
	public GeocachingTrackable(byte[] data) throws IOException {
		super(data);
	}

    /**************************************************/
    /*                   PARAMETERS                   */
	/**************************************************/
	
	// ID
	
    public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}
	
	// NAME

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		if (name == null) {
			Logger.logW(TAG, "setName(" + name + "), invalid parameter");
			return;
		}
		this.mName = name;
	}
	
	// IMAGE URL

	public String getImgUrl() {
		return mImgUrl;
	}

	public void setImgUrl(String imgUrl) {
		if (imgUrl == null) {
			Logger.logW(TAG, "setImgUrl(" + imgUrl + "), invalid parameter");
			return;
		}
		this.mImgUrl = imgUrl;
	}

	// SOURCE DETAILS
	
	public String getSrcDetails() {
		return mSrcDetails;
	}

	public void setSrcDetails(String srcDetails) {
		if (srcDetails == null) {
			Logger.logW(TAG, "setSrcDetails(" + srcDetails + "), invalid parameter");
			return;
		}
		this.mSrcDetails = srcDetails;
	}
	
	// ORIGINAL OWNER

	public String getOriginalOwner() {
		return mOriginalOwner;
	}

	public void setOriginalOwner(String originalOwner) {
		if (originalOwner == null) {
			Logger.logW(TAG, "setOriginalOwner(" + originalOwner + "), invalid parameter");
			return;
		}
		this.mOriginalOwner = originalOwner;
	}

	// CURRENT OWNER
	
	public String getCurrentOwner() {
		return mCurrentOwner;
	}

	public void setCurrentOwner(String currentOwner) {
		if (currentOwner == null) {
			Logger.logW(TAG, "setCurrentOwner(" + currentOwner + "), invalid parameter");
			return;
		}
		this.mCurrentOwner = currentOwner;
	}

	// RELEASED DATE
	
	public long getReleased() {
		return mReleased;
	}

	public void setReleased(long released) {
		this.mReleased = released;
	}

	// ORIGIN LOCATION
	
	public String getOrigin() {
		return mOrigin;
	}

	public void setOrigin(String origin) {
		if (origin == null) {
			Logger.logW(TAG, "setOrigin(" + origin + "), invalid parameter");
			return;
		}
		this.mOrigin = origin;
	}

	public String getGoal() {
		return mGoal;
	}

	// GOAL
	
	public void setGoal(String goal) {
		if (goal == null) {
			Logger.logW(TAG, "setGoal(" + goal + "), invalid parameter");
			return;
		}
		this.mGoal = goal;
	}

	// DETAILS
	
	public String getDetails() {
		return mDetails;
	}

	public void setDetails(String details) {
		if (details == null) {
			Logger.logW(TAG, "setDetails(" + details + "), invalid parameter");
			return;
		}
		this.mDetails = details;
	}
	
	// OTHER METHODS
	
	public String getTbCode() {
		if (mSrcDetails == null || mSrcDetails.length() == 0) {
			return "";
		}
		if (mSrcDetails.startsWith("http://www.geocaching.com/track/details.aspx?tracker=")) {
			return mSrcDetails.substring(
					"http://www.geocaching.com/track/details.aspx?tracker=".length());
		}
		if (mSrcDetails.startsWith("http://coord.info/")) {
			return mSrcDetails.substring(
					"http://coord.info/".length());
		}
		return "";
	}

	/**************************************************/
    /*                  STORABLE PART                 */
	/**************************************************/
	
	@Override
	protected int getVersion() {
		return 1;
	}

	@Override
	protected void readObject(int version, DataReaderBigEndian dr)
			throws IOException {
		mName = dr.readString();
		mImgUrl = dr.readString();
		mSrcDetails = dr.readString();
		mOriginalOwner = dr.readString();
		mReleased = dr.readLong();
		mOrigin = dr.readString();
		mGoal = dr.readString();
		mDetails = dr.readString();
		if (version > 0) {
			mId = dr.readLong();
			mCurrentOwner = dr.readString();
		}
	}

	@Override
	protected void writeObject(DataWriterBigEndian dw) throws IOException {
		dw.writeString(mName);
		dw.writeString(mImgUrl);
		dw.writeString(mSrcDetails);
		dw.writeString(mOriginalOwner);
		dw.writeLong(mReleased);
		dw.writeString(mOrigin);
		dw.writeString(mGoal);
		dw.writeString(mDetails);
		dw.writeLong(mId);
		dw.writeString(mCurrentOwner);
	}

	@Override
	public void reset() {
		mId = 0L;
		mName = "";
		mImgUrl = "";
		mSrcDetails = "";
		
		mOriginalOwner = "";
		mCurrentOwner = "";
		mReleased = 0L;
		mOrigin = "";
		mGoal = "";
		mDetails = "";
	}
}