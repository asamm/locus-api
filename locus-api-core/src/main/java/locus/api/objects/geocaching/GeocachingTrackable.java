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

    // ID of trackable. Currently used for GeoKrety web server
    private long mId;
    // name of travel bug
    private String mName;
    // image url to this travel bug
    private String mImgUrl;
    // URL to trackable object. This is very important value, because URL contain TBCode, like this
    // http://www.geocaching.com/track/details.aspx?tracker=TB4342X
    private String mSrcDetails;
    // original owner of TB
    private String mOriginalOwner;
    // current owner of TB
    private String mCurrentOwner;
    // time of release to public (long since 1.1.1970 in ms)
    private long mReleased;
    // origin place
    private String mOrigin;
    // goal of this TB
    private String mGoal;
    // details
    private String mDetails;

    public GeocachingTrackable() {
        mName = "";
        mImgUrl = "";
        mSrcDetails = "";

        mOriginalOwner = "";
        mReleased = 0L;
        mOrigin = "";
        mGoal = "";
        mDetails = "";

        // V1

        mId = 0L;
        mCurrentOwner = "";
    }

    //*************************************************
    // GET & SET
    //*************************************************

    // ID

    /**
     * Get current ID of trackable item.
     *
     * @return item ID
     */
    public long getId() {
        return mId;
    }

    /**
     * Set new ID to trackable item.
     *
     * @param id new ID
     */
    public void setId(long id) {
        this.mId = id;
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

    // IMAGE URL

    public String getImgUrl() {
        return mImgUrl;
    }

    public void setImgUrl(String imgUrl) {
        if (imgUrl == null) {
            Logger.logD(TAG, "setImgUrl(), empty parameter");
            imgUrl = "";
        }
        this.mImgUrl = imgUrl;
    }

    // SOURCE DETAILS

    public String getSrcDetails() {
        return mSrcDetails;
    }

    public void setSrcDetails(String srcDetails) {
        if (srcDetails == null) {
            Logger.logD(TAG, "setSrcDetails(), empty parameter");
            srcDetails = "";
        }
        this.mSrcDetails = srcDetails;
    }

    // ORIGINAL OWNER

    public String getOriginalOwner() {
        return mOriginalOwner;
    }

    public void setOriginalOwner(String originalOwner) {
        if (originalOwner == null) {
            Logger.logD(TAG, "setOriginalOwner(), empty parameter");
            originalOwner = "";
        }
        this.mOriginalOwner = originalOwner;
    }

    // CURRENT OWNER

    public String getCurrentOwner() {
        return mCurrentOwner;
    }

    public void setCurrentOwner(String currentOwner) {
        if (currentOwner == null) {
            Logger.logD(TAG, "setCurrentOwner(), empty parameter");
            currentOwner = "";
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

    /**
     * Get defined origin location for trackable.
     *
     * @return trackable origin
     */
    public String getOrigin() {
        return mOrigin;
    }

    /**
     * Set new origin value for trackable.
     *
     * @param origin origin
     */
    public void setOrigin(String origin) {
        if (origin == null) {
            Logger.logD(TAG, "setOrigin(), empty parameter");
            origin = "";
        }
        this.mOrigin = origin;
    }

    // GOAL

    /**
     * Get goal of current trackable.
     *
     * @return trackable's goal
     */
    public String getGoal() {
        return mGoal;
    }

    /**
     * Set goal to current trackable.
     *
     * @param goal new goal
     */
    public void setGoal(String goal) {
        if (goal == null) {
            Logger.logD(TAG, "setGoal(), empty parameter");
            goal = "";
        }
        this.mGoal = goal;
    }

    // DETAILS

    /**
     * Get details of current trackable.
     *
     * @return details
     */
    public String getDetails() {
        return mDetails;
    }

    /**
     * Set details of current trackable.
     *
     * @param details details
     */
    public void setDetails(String details) {
        if (details == null) {
            Logger.logD(TAG, "setDetails(), empty parameter");
            details = "";
        }
        this.mDetails = details;
    }

    // OTHER METHODS

    public String getTbCode() {
        if (mSrcDetails == null || mSrcDetails.length() == 0) {
            return "";
        }
        String searchText = "://www.geocaching.com/track/details.aspx?tracker=";
        if (mSrcDetails.indexOf(searchText) > 0) {
            return mSrcDetails.substring(mSrcDetails.indexOf(searchText) + searchText.length());
        }
        searchText = "://coord.info/";
        if (mSrcDetails.indexOf(searchText) > 0) {
            return mSrcDetails.substring(mSrcDetails.indexOf(searchText) + searchText.length());
        }
        return "";
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

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

        // V1

        if (version >= 1) {
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

        // V1

        dw.writeLong(mId);
        dw.writeString(mCurrentOwner);
    }
}