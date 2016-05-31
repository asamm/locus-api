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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

/**
 * This Object is container for cache Logs.
 * <br><br>
 * Useful pages with list:<br>
 * <ul>
 * <li><a href="http://www.gc-reviewer.de/hilfe-tipps-und-tricks/logtypen/">German list</a></li>
 * </ul>
 *  
 * @author menion
 * 
 */
public class GeocachingLog extends Storable {
	
	// LOG TYPES
	
	public static final int CACHE_LOG_TYPE_UNKNOWN                              = -1;
	public static final int CACHE_LOG_TYPE_FOUND                                = 0;
	public static final int CACHE_LOG_TYPE_NOT_FOUND                            = 1;
	public static final int CACHE_LOG_TYPE_WRITE_NOTE                           = 2;
	public static final int CACHE_LOG_TYPE_NEEDS_MAINTENANCE                    = 3;
	public static final int CACHE_LOG_TYPE_OWNER_MAINTENANCE                    = 4;
	public static final int CACHE_LOG_TYPE_PUBLISH_LISTING                      = 5;
	public static final int CACHE_LOG_TYPE_ENABLE_LISTING                       = 6;
	public static final int CACHE_LOG_TYPE_TEMPORARILY_DISABLE_LISTING          = 7;
	public static final int CACHE_LOG_TYPE_UPDATE_COORDINATES                   = 8;
	public static final int CACHE_LOG_TYPE_ANNOUNCEMENT                         = 9;
	public static final int CACHE_LOG_TYPE_WILL_ATTEND                          = 10;
	public static final int CACHE_LOG_TYPE_ATTENDED                             = 11;
	public static final int CACHE_LOG_TYPE_POST_REVIEWER_NOTE                   = 12;
	public static final int CACHE_LOG_TYPE_NEEDS_ARCHIVED                       = 13;
	public static final int CACHE_LOG_TYPE_WEBCAM_PHOTO_TAKEN                   = 14;
	public static final int CACHE_LOG_TYPE_RETRACT_LISTING                      = 15;
	public static final int CACHE_LOG_TYPE_ARCHIVE                              = 16;
	public static final int CACHE_LOG_TYPE_UNARCHIVE                            = 17;
	public static final int CACHE_LOG_TYPE_PERMANENTLY_ARCHIVED					= 18;

    public static final long FINDERS_ID_UNDEFINED                               = 0;

	// PARAMETERS
	
	/*
	 * Unique ID of log
	 */
	private long mId;
	/*
	 * Type of log defined by CACHE_LOG_TYPE_X parameter
	 */
	private int mType;
	/*
	 * Time when log was created.
	 * Time is defined in ms since 1. 1. 1970
	 */
	private long mDate;
	/*
	 * Name of 'finder'
	 */
	private String mFinder;
    /**
     * ID of the 'finder'
     */
    private long mFindersId;
	/*
	 * Total number of found caches by founder
	 */
	private int mFindersFound;
	/*
	 * Text of log itself
	 */
	private String mLogText;
	/*
	 * List of attached images
	 */
	private List<GeocachingImage> mImages;
    /**
     * Longitude defined by user.
     */
    private double mCooLon;
    /**
     * Latitude defined by user.
     */
    private double mCooLat;

    /**
     * Basic empty constructor
     */
	public GeocachingLog() {
		super();
	}
	
    /**************************************************/
    // PARAMETERS
	/**************************************************/
	
	// ID
	
	public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}

	// TYPE
	
	public int getType() {
		return mType;
	}

	public void setType(int type) {
		this.mType = type;
	}
	
	// DATE

	public long getDate() {
		return mDate;
	}

	public void setDate(long date) {
		this.mDate = date;
	}

	// FINDER

    /**
     * Get name of cache finder.
     * @return name of finder
     */
	public String getFinder() {
		return mFinder;
	}

    /**
     * Set name of the finder.
     * @param finder name of finder
     */
	public void setFinder(String finder) {
		this.mFinder = finder;
	}

    // FINDER ID

    /**
     * Get defined ID of finder.
     * @return current defined ID of finder
     */
    public long getFindersId() {
        return mFindersId;
    }

    /**
     * Set new ID to current finder.
     * @param finderId ID of finder
     */
    public void setFindersId(long finderId) {
        this.mFindersId = finderId;
    }

	// FINDER COUNT

    /**
     * Get amount of already found caches by current 'finder'.
     * @return amount of found caches
     */
	public int getFindersFound() {
		return mFindersFound;
	}

    /**
     * Set amount of already found caches by current finder.
     * @param finderFound amount of found caches
     */
	public void setFindersFound(int finderFound) {
		this.mFindersFound = finderFound;
	}

	// LOG TEXT
	
	public String getLogText() {
		return mLogText;
	}

	public void setLogText(String logText) {
		this.mLogText = logText;
	}
	
	// IMAGES

    /**
     * Add image to current list of images attached to this log.
     * @param image image to add
     */
	public void addImage(GeocachingImage image) {
		this.mImages.add(image);
	}

    /**
     * Get iterator over already attached images.
     * @return iterator over all images
     */
	public Iterator<GeocachingImage> getImages() {
		return mImages.iterator();
	}

    /**
     * Get defined longitude coordinate.
     * @return longitude
     */
    public double getCooLon() {
        return mCooLon;
    }

    /**
     * Set longitude coordinate to this log. Value is in WGS84 format.
     * @param lon new longitude coordinate (-180.0, +180.0)
     */
    public void setCooLon(double lon) {
        this.mCooLon = lon;
    }

    /**
     * Get defined latitude coordinate.
     * @return latitude
     */
    public double getCooLat() {
        return mCooLat;
    }

    /**
     * Set latitude coordinate to this log. Value is in WGS84 format.
     * @param lat new latitude coordinate (-90.0, +90.0)
     */
    public void setCooLat(double lat) {
        this.mCooLat = lat;
    }

    /**************************************************/
    // STORABLE PART
	/**************************************************/
	
	@Override
	protected int getVersion() {
		return 2;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void readObject(int version, DataReaderBigEndian dr)
			throws IOException {
		mId = dr.readLong();
		mType = dr.readInt();
		mDate = dr.readLong();
		mFinder = dr.readString();
		mFindersFound = dr.readInt();
		mLogText = dr.readString();
		
		// V1
		if (version >= 1) {
			mImages = (List<GeocachingImage>) 
					dr.readListStorable(GeocachingImage.class);
		}

        // V2
        if (version >= 2) {
            mFindersId = dr.readLong();
            mCooLon = dr.readDouble();
            mCooLat = dr.readDouble();
        }
	}

	@Override
	protected void writeObject(DataWriterBigEndian dw) throws IOException {
		dw.writeLong(mId);
		dw.writeInt(mType);
		dw.writeLong(mDate);
		dw.writeString(mFinder);
		dw.writeInt(mFindersFound);
		dw.writeString(mLogText);
		
		// V1
		dw.writeListStorable(mImages);

        // V2
        dw.writeLong(mFindersId);
        dw.writeDouble(mCooLon);
        dw.writeDouble(mCooLat);
	}

	@Override
	public void reset() {
		mId = 0;
		mType = CACHE_LOG_TYPE_UNKNOWN;
		mDate = 0L;
		mFinder = "";
		mFindersFound = 0;
		mLogText = "";
		
		// V1
		mImages = new ArrayList<>();

        // V2
        mFindersId = FINDERS_ID_UNDEFINED;
        mCooLon = 0.0;
        mCooLat = 0.0;
	}
}