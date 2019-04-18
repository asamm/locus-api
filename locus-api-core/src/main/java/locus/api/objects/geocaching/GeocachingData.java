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

package locus.api.objects.geocaching;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;
import locus.api.utils.Utils;

/**
 * Container for main Geocaching data
 *
 * @author menion
 */
public class GeocachingData extends Storable {

    // tag for logger
    private static final String TAG = "GeocachingData";

    public static final int CACHE_TYPE_TRADITIONAL = 0;
    public static final int CACHE_TYPE_MULTI = 1;
    public static final int CACHE_TYPE_MYSTERY = 2;
    public static final int CACHE_TYPE_VIRTUAL = 3;
    public static final int CACHE_TYPE_EARTH = 4;
    public static final int CACHE_TYPE_PROJECT_APE = 5;
    public static final int CACHE_TYPE_LETTERBOX = 6;
    public static final int CACHE_TYPE_WHERIGO = 7;
    public static final int CACHE_TYPE_EVENT = 8;
    public static final int CACHE_TYPE_MEGA_EVENT = 9;
    public static final int CACHE_TYPE_CACHE_IN_TRASH_OUT = 10;
    public static final int CACHE_TYPE_GPS_ADVENTURE = 11;
    public static final int CACHE_TYPE_WEBCAM = 12;
    public static final int CACHE_TYPE_LOCATIONLESS = 13;
    public static final int CACHE_TYPE_BENCHMARK = 14;
    public static final int CACHE_TYPE_MAZE_EXHIBIT = 15;
    public static final int CACHE_TYPE_WAYMARK = 16;
    public static final int CACHE_TYPE_GROUNDSPEAK = 17;
    public static final int CACHE_TYPE_LF_EVENT = 18;
    public static final int CACHE_TYPE_LF_CELEBRATION = 19;
    public static final int CACHE_TYPE_GIGA_EVENT = 20;
    public static final int CACHE_TYPE_LAB_CACHE = 21;

    public static final int CACHE_TYPE_UNDEFINED = 100;

    public static final int CACHE_SIZE_NOT_CHOSEN = 0;
    public static final int CACHE_SIZE_MICRO = 1;
    public static final int CACHE_SIZE_SMALL = 2;
    public static final int CACHE_SIZE_REGULAR = 3;
    public static final int CACHE_SIZE_LARGE = 4;
    public static final int CACHE_SIZE_HUGE = 5;
    public static final int CACHE_SIZE_OTHER = 6;

    public static final int CACHE_SOURCE_UNDEFINED = 0;
    public static final int CACHE_SOURCE_GEOCACHING_COM = 1;
    public static final int CACHE_SOURCE_GEOCACHING_HU = 2;
    public static final int CACHE_SOURCE_OPENCACHING = 100;
    public static final int CACHE_SOURCE_OPENCACHING_DE = 101;
    public static final int CACHE_SOURCE_OPENCACHING_ES = 102;
    public static final int CACHE_SOURCE_OPENCACHING_FR = 103;
    public static final int CACHE_SOURCE_OPENCACHING_IT = 104;
    public static final int CACHE_SOURCE_OPENCACHING_NL = 105;
    public static final int CACHE_SOURCE_OPENCACHING_PL = 106;
    public static final int CACHE_SOURCE_OPENCACHING_RO = 107;
    public static final int CACHE_SOURCE_OPENCACHING_UK = 108;
    public static final int CACHE_SOURCE_OPENCACHING_US = 109;
    public static final int CACHE_SOURCE_OPENCACHING_CZ = 110;

    /*
     * INFO
     *
     * all times are in format '2009-09-22T14:16:03.0000000+0200', where important is only first
     * part (Date), so it should looks for example only as '2009-09-22T'. This should work
     *
     * Groundspeak staging API: https://staging.api.groundspeak.com/Live/v6beta/geocaching.svc/help
     */

    // ID of point - optional parameter
    private long mId;
    // REQUIRED
    // whole cache ID from gc.com - so GC...
    private String mCacheID;
    // state of cache - available or disable
    private boolean mAvailable;
    // state of cache - already archived or not
    private boolean mArchived;
    // state of cache - available only for premium members
    private boolean mPremiumOnly;
    // REQUIRED!
    // name of cache visible on all places in Locus as an title
    private String mName;
    // name of person who placed cache (groundspeak:placed_by). It is displayed in Locus when
    // tapped on point or in main GC page
    private String mPlacedBy;
    // name of cache owner (groundspeak:owner). This value is not displayed in locus
    private String mOwner;
    // time when cache was created by owner. This time is recorded when owner register cach
    // on gc.com. On gc.com this date is usually named as "hidden".
    // Time is defined in ms since 1. 1. 1970
    private long mDateHidden;
    // time when was cache published by reviewer. Time is defined in ms since 1. 1. 1970
    private long mDatePublished;
    // time of last update. For example by added log or any change in cache itself.
    // Time is defined in ms since 1. 1. 1970
    private long mDateUpdated;

    // type of a cache, define by constants CACHE_TYPE_X
    private int mType;
    // size of cache container. Size is defined by parameters CACHE_SIZE_X, or by text directly
    private int mContainer;
    // difficulty value - 1.0 - 5.0 (by 0.5)
    private float mDifficulty;
    // terrain value - 1.0 - 5.0 (by 0.5)
    private float mTerrain;
    // name of country, where is cache places
    private String mCountry;
    // name of state, where is cache places
    private String mState;
    // descriptions are now stored in raw GZIPed bytes. This allows to keep smaller size
    // of loaded GeocacheData object and also in cases, we don't need short/long
    // description, it also save quite a lot of CPU (not need to use GZIP)
    private byte[] mDescBytes;
    // length of Short description in mDescBytes array. This parameter is needed
    // for correct storalization
    private int mShortDescLength;
    // encoded hints
    private String mEncodedHints;
    // list of attributes
    public List<GeocachingAttribute> attributes;
    // list of logs
    public List<GeocachingLog> logs;
    // list of travel bugs
    public List<GeocachingTrackable> trackables;
    // list of waypoints
    public List<GeocachingWaypoint> waypoints;
    // user notes
    private String mNotes;
    // flag if cache is 'computed' - have corrected coordinates
    private boolean mComputed;
    // flag if cache is already found by user
    private boolean mFound;
    // uRL for cache itself
    private String mCacheUrl;
    // number of favorite points
    private int mFavoritePoints;

    // V1

    //gcVote - number of votes
    private int mGcVoteNumOfVotes;
    // average (not median) value
    private float mGcVoteAverage;
    // user value for GCVote
    private float mGcVoteUserVote;

    // V2

    // original longitude defined by owner
    private double mLonOriginal;
    // original latitude defined by owner
    private double mLatOriginal;
    // list of attached images
    private List<GeocachingImage> mImages;

    // V3

    // source of cache
    private int mSource;

    /**
     * Main empty constructor
     */
    public GeocachingData() {
        mId = 0;
        mCacheID = "";
        mAvailable = true;
        mArchived = false;
        mPremiumOnly = false;
        mName = "";
        mDateUpdated = 0L;
        mDateHidden = 0L;
        mPlacedBy = "";
        mOwner = "";
        mDatePublished = 0L;
        mType = CACHE_TYPE_TRADITIONAL;
        mContainer = CACHE_SIZE_NOT_CHOSEN;
        mDifficulty = -1.0f;
        mTerrain = -1.0f;
        mCountry = "";
        mState = "";
        mDescBytes = null;
        mShortDescLength = 0;
        mEncodedHints = "";
        attributes = new ArrayList<>();
        logs = new ArrayList<>();
        trackables = new ArrayList<>();
        waypoints = new ArrayList<>();
        mNotes = "";
        mComputed = false;
        mFound = false;
        mCacheUrl = "";
        mFavoritePoints = -1;

        // V1

        mGcVoteNumOfVotes = -1;
        mGcVoteAverage = 0.0f;
        mGcVoteUserVote = 0.0f;

        // V2

        mLonOriginal = 0.0;
        mLatOriginal = 0.0;
        mImages = new ArrayList<>();

        // V3

        mSource = CACHE_SOURCE_UNDEFINED;
    }

    //*************************************************
    // PARAMETERS
    //*************************************************

    // ID

    /**
     * Get current defined ID of cache. This ID is just optional unique identification as long
     * number that has no extra usage in Locus.
     *
     * @return ID of cache
     */
    public long getId() {
        return mId;
    }

    /**
     * Set new ID to cache.
     *
     * @param id ID of cache
     */
    public void setId(long id) {
        this.mId = id;
    }

    // CACHE ID & SOURCE

    /**
     * Get cache ID (cache code) of current cache.
     *
     * @return cache code
     */
    public String getCacheID() {
        return mCacheID;
    }

    /**
     * Set new cache code to cache.
     *
     * @param cacheID new cache ID (code)
     */
    public void setCacheID(String cacheID) {
        // check cache ID
        if (cacheID == null || cacheID.length() == 0) {
            Logger.logW(TAG, "setCacheId(" + cacheID + "), " +
                    "invalid cache ID");
            return;
        }

        // define source and set values
        int source = CACHE_SOURCE_UNDEFINED;
        String testCode = cacheID.trim().toUpperCase();
        if (testCode.startsWith("GC")) {
            source = CACHE_SOURCE_GEOCACHING_COM;
        } else if (testCode.startsWith("OB")) {
            source = CACHE_SOURCE_OPENCACHING_NL;
        } else if (testCode.startsWith("OK")) {
            source = CACHE_SOURCE_OPENCACHING_UK;
        } else if (testCode.startsWith("OP")) {
            source = CACHE_SOURCE_OPENCACHING_PL;
        } else if (testCode.startsWith("OU")) {
            source = CACHE_SOURCE_OPENCACHING_US;
        } else if (testCode.startsWith("OZ")) {
            source = CACHE_SOURCE_OPENCACHING_CZ;
        } else if (testCode.startsWith("O")) {
            source = CACHE_SOURCE_OPENCACHING;
        }

        // finally set cache ID
        setCacheID(cacheID, source);
    }

    /**
     * Set new cache code and it's source.
     *
     * @param cacheID ID of cache
     * @param source  source from where this cache comes from
     */
    public void setCacheID(String cacheID, int source) {
        // check cache ID
        if (cacheID == null || cacheID.length() == 0) {
            Logger.logW(TAG, "setCacheId(" + cacheID + ", " + source + "), " +
                    "invalid cache ID");
            return;
        }

        // store values
        this.mCacheID = cacheID;
        setSource(source);
    }

    /**
     * Get source of current cache.
     *
     * @return cache source
     */
    public int getSource() {
        return mSource;
    }

    /**
     * Define source of this cache as constant value. Be aware, that this value is automatically set when
     * setup a cacheID, so use this method after the {@link #setCacheID(String, int)} function is used.
     *
     * @param source source of cache
     */
    public void setSource(int source) {
        this.mSource = source;
    }

    // AVAILABLE

    public boolean isAvailable() {
        return mAvailable;
    }

    public void setAvailable(boolean available) {
        this.mAvailable = available;
    }

    // ARCHIVED

    public boolean isArchived() {
        return mArchived;
    }

    public void setArchived(boolean archived) {
        this.mArchived = archived;
    }

    // PREMIUM ONLY

    /**
     * Flag if cache is only for a premium members.
     *
     * @return <code>true</code> if cache is only for a premium members
     */
    public boolean isPremiumOnly() {
        return mPremiumOnly;
    }

    /**
     * Set a flag, if cache is only for a premium members.
     *
     * @param premiumOnly <code>true</code> to set cache only for a premium
     */
    public void setPremiumOnly(boolean premiumOnly) {
        this.mPremiumOnly = premiumOnly;
    }

    // NAME

    /**
     * Get public, visible name of cache.
     *
     * @return name of cache
     */
    public String getName() {
        return mName;
    }

    /**
     * Set new name of current cache.
     *
     * @param name new name of cache
     */
    public void setName(String name) {
        if (name != null && name.length() > 0) {
            this.mName = name;
        }
    }

    // PLACED BY

    /**
     * Get name of person, who placed cache.
     *
     * @return user that placed cache
     */
    public String getPlacedBy() {
        return mPlacedBy;
    }

    /**
     * Set user that placed cache.
     *
     * @param placedBy user that placed cache
     */
    public void setPlacedBy(String placedBy) {
        if (placedBy != null && placedBy.length() > 0) {
            this.mPlacedBy = placedBy;
        }
    }

    // OWNER

    /**
     * Get owner of current cache.
     *
     * @return cache owner
     */
    public String getOwner() {
        return mOwner;
    }

    /**
     * Define owner of current cache.
     *
     * @param owner cache owner
     */
    public void setOwner(String owner) {
        if (owner != null && owner.length() > 0) {
            this.mOwner = owner;
        }
    }

    // DATE CREATED/HIDDEN

    /**
     * Get date/time, when owner create/hide cache on a gc.com web page.
     *
     * @return time when cache was hidden [in ms]
     */
    public long getDateHidden() {
        return mDateHidden;
    }

    /**
     * Set new date/time as time when owner create/hide a cache on gc.com web page.
     *
     * @param dateHidden time when cache was hidden [is ms]
     */
    public void setDateHidden(long dateHidden) {
        this.mDateHidden = dateHidden;
    }

    // PUBLISHED TIME

    /**
     * Get date/time, when owner published cache for users. Usually most important date.
     *
     * @return time when cache was published
     */
    public long getDatePublished() {
        return mDatePublished;
    }

    /**
     * Set date/time when owner published cache for users.
     *
     * @param hidden time when cache was published (in ms since 1.1.1970)
     */
    public void setDatePublished(long hidden) {
        this.mDatePublished = hidden;
    }

    // LAST UPDATED

    /**
     * Get date/time, when cache was updated for the last time, on gc.com server.
     *
     * @return time when cache was updated for last time
     */
    public long getDateUpdated() {
        return mDateUpdated;
    }

    /**
     * Set date/time when cache was updated for the last time, on gc.com server.
     *
     * @param lastUpdated time when cache was updated for last time (in ms since 1.1.1970)
     */
    public void setDateUpdated(long lastUpdated) {
        this.mDateUpdated = lastUpdated;
    }

    // CACHE TYPE

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public void setType(String type) {
        this.mType = getTypeAsInt(type);
    }

    // CONTAINER SIZE

    public int getContainer() {
        return mContainer;
    }

    public String getContainerText() {
        switch (mContainer) {
            case CACHE_SIZE_MICRO:
                return "Micro";
            case CACHE_SIZE_SMALL:
                return "Small";
            case CACHE_SIZE_REGULAR:
                return "Regular";
            case CACHE_SIZE_LARGE:
                return "Large";
            case CACHE_SIZE_HUGE:
                return "Huge";
            case CACHE_SIZE_NOT_CHOSEN:
                return "Not chosen";
            case CACHE_SIZE_OTHER:
                return "Other";
        }
        return null;
    }

    public void setContainer(int container) {
        this.mContainer = container;
    }

    public void setContainer(String container) {
        if (container.equalsIgnoreCase("Micro")) {
            setContainer(CACHE_SIZE_MICRO);
        } else if (container.equalsIgnoreCase("Small")) {
            setContainer(CACHE_SIZE_SMALL);
        } else if (container.equalsIgnoreCase("Regular")) {
            setContainer(CACHE_SIZE_REGULAR);
        } else if (container.equalsIgnoreCase("Large")) {
            setContainer(CACHE_SIZE_LARGE);
        } else if (container.equalsIgnoreCase("Huge")) {
            setContainer(CACHE_SIZE_HUGE);
        } else if (container.equalsIgnoreCase("Not chosen")) {
            setContainer(CACHE_SIZE_NOT_CHOSEN);
        } else if (container.equalsIgnoreCase("Other")) {
            setContainer(CACHE_SIZE_OTHER);
        }
    }

    // DIFFICULTY

    public float getDifficulty() {
        return mDifficulty;
    }

    public void setDifficulty(float difficulty) {
        this.mDifficulty = difficulty;
    }

    // TERRAIN

    public float getTerrain() {
        return mTerrain;
    }

    public void setTerrain(float terrain) {
        this.mTerrain = terrain;
    }

    // COUNTRY

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String country) {
        if (country != null && country.length() > 0) {
            this.mCountry = country;
        }
    }

    // STATE

    public String getState() {
        return mState;
    }

    public void setState(String state) {
        if (state != null && state.length() > 0) {
            this.mState = state;
        }
    }

    // DESCRIPTIONS

    public String[] getDescriptions() {
        // prepare container
        String[] res = new String[]{"", ""};

        // return empty texts if no desc exists
        if (mDescBytes == null || mDescBytes.length == 0) {
            return res;
        }

        GZIPInputStream zis = null;
        try {
            // prepare input stream
            zis = new GZIPInputStream(
                    new ByteArrayInputStream(mDescBytes), 10240);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // read data
            int dataRead;
            byte[] buffer = new byte[1024];
            while ((dataRead = zis.read(buffer)) != -1) {
                baos.write(buffer, 0, dataRead);
            }
            String result = Utils.doBytesToString(baos.toByteArray());

            // read short description
            if (mShortDescLength > 0) {
                res[0] = result.substring(0, mShortDescLength);
            }

            // read long description
            res[1] = result.substring(mShortDescLength);
        } catch (IOException e) {
            Logger.logE(TAG, "", e);
            res[0] = "";
            res[1] = "";
        } finally {
            Utils.closeStream(zis);
        }

        // return result
        return res;
    }

    public boolean setDescriptions(String shortDesc, boolean shortInHtml,
            String longDesc, boolean longInHtml) {
        // fix short description
        if (shortDesc == null) {
            shortDesc = "";
        }

        // fix long description
        if (longDesc == null) {
            longDesc = "";
        }

        // store descriptions
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream zos = new GZIPOutputStream(baos);

            zos.write(Utils.doStringToBytes(shortDesc));
            zos.write(Utils.doStringToBytes(longDesc));
            zos.close();

            // store parameters
            mDescBytes = baos.toByteArray();
            mShortDescLength = shortDesc.length();
            return true;
        } catch (IOException e) {
            Logger.logE(TAG, "setDescription(" + shortDesc + ", " + shortInHtml + ", " +
                    longDesc + ", " + longInHtml + ")", e);
            mDescBytes = null;
            mShortDescLength = 0;
            return false;
        }
    }

    // ENCODED HINTS

    public String getEncodedHints() {
        return mEncodedHints;
    }

    public void setEncodedHints(String hints) {
        if (hints != null && hints.length() > 0) {
            this.mEncodedHints = hints;
        }
    }

    // NOTES

    /**
     * Get current users notes.
     *
     * @return notes
     */
    public String getNotes() {
        return mNotes;
    }

    /**
     * Set new notes to current container.
     *
     * @param notes custom users notes
     */
    public void setNotes(String notes) {
        if (notes == null) {
            return;
        }
        this.mNotes = notes;
    }

    // COMPUTED

    public boolean isComputed() {
        return mComputed;
    }

    public void setComputed(boolean computed) {
        this.mComputed = computed;
    }

    // FOUND

    public boolean isFound() {
        return mFound;
    }

    public void setFound(boolean found) {
        this.mFound = found;
    }

    // CACHE URL

    /**
     * Get stored URL to cache itself. Keep in mind that this value may not be defined or
     * may include various formats. Suggested is to use {@link #getCacheUrlFull()} method,
     * that should return valid URL.
     *
     * @return stored cache url parameter.
     */
    public String getCacheUrl() {
        return mCacheUrl;
    }

    /**
     * Set Cache URL parameters for later display of cache web page or other tasks.
     *
     * @param url url to cache page
     */
    public void setCacheUrl(String url) {
        if (url != null && url.length() > 0) {
            mCacheUrl = url;
        }
    }

    /**
     * Get modified URL to cache listing on web page, useful for direct use.
     *
     * @return URL to cache listing
     */
    public String getCacheUrlFull() {
        // if cache is from Groundspeak, return "coord.info" url
        if (getSource() == CACHE_SOURCE_GEOCACHING_COM) {
            return "http://coord.info/" + mCacheID;
        }

        // check defined URL
        if (mCacheUrl != null && mCacheUrl.length() > 0) {
            return mCacheUrl;
        }

        // generate basic URL
        return "http://www.geocaching.com/seek/cache_details.aspx?wp=" + mCacheID;
    }

    // FAVORITE POINTS

    /**
     * Get number of favorite points attached to current cache.
     *
     * @return number of favorite points
     */
    public int getFavoritePoints() {
        return mFavoritePoints;
    }

    /**
     * Set number of favorite points.
     *
     * @param favoritePoints number of favorite points
     */
    public void setFavoritePoints(int favoritePoints) {
        this.mFavoritePoints = favoritePoints;
    }

    // GC VOTE - NUMBER OF VOTES

    public int getGcVoteNumOfVotes() {
        return mGcVoteNumOfVotes;
    }

    public void setGcVoteNumOfVotes(int gcVoteNumOfVotes) {
        this.mGcVoteNumOfVotes = gcVoteNumOfVotes;
    }

    // GC VOTE - AVERAGE

    public float getGcVoteAverage() {
        return mGcVoteAverage;
    }

    public void setGcVoteAverage(float gcVoteAverage) {
        this.mGcVoteAverage = gcVoteAverage;
    }

    // GC VOTE - USER VOTE

    public float getGcVoteUserVote() {
        return mGcVoteUserVote;
    }

    public void setGcVoteUserVote(float gcVoteUserVote) {
        this.mGcVoteUserVote = gcVoteUserVote;
    }

    // LONGITUDE ORIGINAL

    public double getLonOriginal() {
        return mLonOriginal;
    }

    public void setLonOriginal(double lonOriginal) {
        this.mLonOriginal = lonOriginal;
    }

    // LATITUDE ORIGINAL

    public double getLatOriginal() {
        return mLatOriginal;
    }

    public void setLatOriginal(double latOriginal) {
        this.mLatOriginal = latOriginal;
    }

    // IMAGES

    public void addImage(GeocachingImage image) {
        this.mImages.add(image);
    }

    public Iterator<GeocachingImage> getImages() {
        return mImages.iterator();
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

    @Override
    protected int getVersion() {
        return 3;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void readObject(int version, DataReaderBigEndian dr)
            throws IOException {
        mId = dr.readLong();
        // set cache ID over "set" method, to also correctly set source parameter
        setCacheID(dr.readString());
        mAvailable = dr.readBoolean();
        mArchived = dr.readBoolean();
        mPremiumOnly = dr.readBoolean();
        mName = dr.readString();
        mDateUpdated = dr.readLong();
        mDateHidden = dr.readLong();
        mPlacedBy = dr.readString();
        mOwner = dr.readString();
        mDatePublished = dr.readLong();
        mType = dr.readInt();
        mContainer = dr.readInt();
        mDifficulty = dr.readFloat();
        mTerrain = dr.readFloat();
        mCountry = dr.readString();
        mState = dr.readString();

        // total length
        int size = dr.readInt();
        // length of short description
        mShortDescLength = dr.readInt();
        // read raw data
        if (size > 0) {
            mDescBytes = dr.readBytes(size);
        }

        // read rest
        mEncodedHints = dr.readString();
        attributes = dr.readListStorable(GeocachingAttribute.class);
        logs = dr.readListStorable(GeocachingLog.class);
        trackables = dr.readListStorable(GeocachingTrackable.class);
        waypoints = dr.readListStorable(GeocachingWaypoint.class);
        mNotes = dr.readString();
        mComputed = dr.readBoolean();
        mFound = dr.readBoolean();
        mCacheUrl = dr.readString();
        mFavoritePoints = dr.readInt();

        // V1

        if (version >= 1) {
            mGcVoteNumOfVotes = dr.readInt();
            mGcVoteAverage = dr.readFloat();
            mGcVoteUserVote = dr.readFloat();
        }

        // V2

        if (version >= 2) {
            mLonOriginal = dr.readDouble();
            mLatOriginal = dr.readDouble();
            mImages = dr.readListStorable(GeocachingImage.class);
        }

        // V3

        if (version >= 3) {
            mSource = dr.readInt();
        }
    }

    @Override
    protected void writeObject(DataWriterBigEndian dw) throws IOException {
        dw.writeLong(mId);
        dw.writeString(mCacheID);
        dw.writeBoolean(mAvailable);
        dw.writeBoolean(mArchived);
        dw.writeBoolean(mPremiumOnly);
        dw.writeString(mName);
        dw.writeLong(mDateUpdated);
        dw.writeLong(mDateHidden);
        dw.writeString(mPlacedBy);
        dw.writeString(mOwner);
        dw.writeLong(mDatePublished);
        dw.writeInt(mType);
        dw.writeInt(mContainer);
        dw.writeFloat(mDifficulty);
        dw.writeFloat(mTerrain);
        dw.writeString(mCountry);
        dw.writeString(mState);

        // write listings
        if (mDescBytes == null || mDescBytes.length == 0) {
            // total length
            dw.writeInt(0);
            // length of short description
            dw.writeInt(0);
        } else {
            // total length
            dw.writeInt(mDescBytes.length);
            // length of short description
            dw.writeInt(mShortDescLength);
            dw.write(mDescBytes);
        }

        // write rest
        dw.writeString(mEncodedHints);
        dw.writeListStorable(attributes);
        dw.writeListStorable(logs);
        dw.writeListStorable(trackables);
        dw.writeListStorable(waypoints);
        dw.writeString(mNotes);
        dw.writeBoolean(mComputed);
        dw.writeBoolean(mFound);
        dw.writeString(mCacheUrl);
        dw.writeInt(mFavoritePoints);

        // V1

        dw.writeInt(mGcVoteNumOfVotes);
        dw.writeFloat(mGcVoteAverage);
        dw.writeFloat(mGcVoteUserVote);

        // V2

        dw.writeDouble(mLonOriginal);
        dw.writeDouble(mLatOriginal);
        dw.writeListStorable(mImages);

        // V3

        dw.writeInt(mSource);
    }

    //*************************************************
    // VARIOUS UTILS
    //*************************************************

    public boolean isCacheValid() {
        return mCacheID.length() > 0 && mName.length() > 0;
    }

    public void sortTrackables() {
        // check content
        if (trackables.size() <= 1) {
            return;
        }

        // finally sort
        Collections.sort(trackables, new Comparator<GeocachingTrackable>() {
            public int compare(GeocachingTrackable object1,
                    GeocachingTrackable object2) {
                return object1.getName().compareTo(object2.getName());
            }
        });
    }

    public static String getTypeAsString(int type) {
        switch (type) {
            case CACHE_TYPE_TRADITIONAL:
                return "Traditional Cache";
            case CACHE_TYPE_MULTI:
                return "Multi-Cache";
            case CACHE_TYPE_MYSTERY:
                return "Unknown Cache"; // "Mystery Cache"
            case CACHE_TYPE_VIRTUAL:
                return "Virtual Cache";
            case CACHE_TYPE_EARTH:
                return "EarthCache";
            case CACHE_TYPE_PROJECT_APE:
                return "Project APE Cache";
            case CACHE_TYPE_LETTERBOX:
                return "Letterbox";
            case CACHE_TYPE_WHERIGO:
                return "Wherigo Cache";
            case CACHE_TYPE_EVENT:
                return "Event Cache";
            case CACHE_TYPE_MEGA_EVENT:
                return "Mega-Event Cache";
            case CACHE_TYPE_CACHE_IN_TRASH_OUT:
                return "Cache In Trash Out Event";
            case CACHE_TYPE_GPS_ADVENTURE:
                return "GPS Adventure";
            case CACHE_TYPE_WEBCAM:
                return "Webcam Cache";
            case CACHE_TYPE_LOCATIONLESS:
                return "Location-less";
            case CACHE_TYPE_BENCHMARK:
                return "Benchmark";
            case CACHE_TYPE_MAZE_EXHIBIT:
                return "Maze Exhibit";
            case CACHE_TYPE_WAYMARK:
                return "Waymark";
            case CACHE_TYPE_GROUNDSPEAK:
                return "Groundspeak";
            case CACHE_TYPE_LF_EVENT:
                return "L&F Event";
            case CACHE_TYPE_LF_CELEBRATION:
                return "L&F Celebration";
            case CACHE_TYPE_GIGA_EVENT:
                return "Giga-Event Cache";
            case CACHE_TYPE_LAB_CACHE:
                return "Lab Cache";
            default:
                return "Geocache";
        }
    }

    /**
     * Get type of cache based on it's text representation.
     *
     * @param type type as text
     * @return code of cache type
     */
    public static int getTypeAsInt(String type) {
        // check text
        if (type == null || type.length() == 0) {
            return CACHE_TYPE_UNDEFINED;
        }

        // split if contains unwanted data
        if (type.startsWith("Geocache|")) {
            type = type.substring("Geocache|".length());
        }

        // handle type
        if (type.equalsIgnoreCase("Traditional Cache")) {
            return CACHE_TYPE_TRADITIONAL;
        } else if (type.equalsIgnoreCase("Multi-cache")) {
            return CACHE_TYPE_MULTI;
        } else if (type.equalsIgnoreCase("Mystery Cache") ||
                type.equalsIgnoreCase("Unknown Cache") ||
                type.equalsIgnoreCase("Mystery/Puzzle Cache")) {
            return CACHE_TYPE_MYSTERY;
        } else if (type.equalsIgnoreCase("Project APE Cache") ||
                type.equalsIgnoreCase("Project A.P.E. Cache")) {
            return CACHE_TYPE_PROJECT_APE;
        } else if (type.equalsIgnoreCase("Letterbox Hybrid") ||
                type.equalsIgnoreCase("Letterbox")) {
            return CACHE_TYPE_LETTERBOX;
        } else if (type.equalsIgnoreCase("Wherigo") ||
                type.equalsIgnoreCase("Wherigo cache")) {
            return CACHE_TYPE_WHERIGO;
        } else if (type.equalsIgnoreCase("Event Cache")) {
            return CACHE_TYPE_EVENT;
        } else if (type.equalsIgnoreCase("Mega-Event Cache")) {
            return CACHE_TYPE_MEGA_EVENT;
        } else if (type.equalsIgnoreCase("Cache In Trash Out Event")) {
            return CACHE_TYPE_CACHE_IN_TRASH_OUT;
        } else if (type.equalsIgnoreCase("EarthCache")) {
            return CACHE_TYPE_EARTH;
        } else if (type.toLowerCase().startsWith("gps adventures")) {
            return CACHE_TYPE_GPS_ADVENTURE;
        } else if (type.equalsIgnoreCase("Virtual Cache")) {
            return CACHE_TYPE_VIRTUAL;
        } else if (type.equalsIgnoreCase("Webcam Cache")) {
            return CACHE_TYPE_WEBCAM;
        } else if (type.equalsIgnoreCase("Locationless Cache")) {
            return CACHE_TYPE_LOCATIONLESS;
        } else if (type.equalsIgnoreCase("Benchmark")) {
            return CACHE_TYPE_BENCHMARK;
        } else if (type.equalsIgnoreCase("Maze Exhibit")) {
            return CACHE_TYPE_MAZE_EXHIBIT;
        } else if (type.equalsIgnoreCase("Waymark")) {
            return CACHE_TYPE_WAYMARK;
        } else if (type.equalsIgnoreCase("Groundspeak")) {
            return CACHE_TYPE_GROUNDSPEAK;
        } else if (type.equalsIgnoreCase("L&F Event")) {
            return CACHE_TYPE_LF_EVENT;
        } else if (type.equalsIgnoreCase("L&F Celebration")) {
            return CACHE_TYPE_LF_CELEBRATION;
        } else if (type.equalsIgnoreCase("Giga-Event Cache")) {
            return CACHE_TYPE_GIGA_EVENT;
        } else if (type.equalsIgnoreCase("Lab Cache")) {
            return CACHE_TYPE_LAB_CACHE;
        } else {
            return CACHE_TYPE_UNDEFINED;
        }
    }

    /**
     * Check if certain type of cache is type "Event".
     *
     * @param type type of cache to test
     * @return <code>true</code> if cache is event type
     */
    public static boolean isEventCache(int type) {
        return type == CACHE_TYPE_EVENT ||
                type == CACHE_TYPE_MEGA_EVENT ||
                type == CACHE_TYPE_GIGA_EVENT ||
                type == CACHE_TYPE_GPS_ADVENTURE ||
                type == CACHE_TYPE_CACHE_IN_TRASH_OUT;
    }

    /**
     * Check if owner, country, state or description contains certain defined text.
     *
     * @param text text that we search
     * @return <code>true</code> if text is in this cache
     */
    public boolean containsInData(String text) {
        if (mOwner.toLowerCase().contains(text)) {
            return true;
        }
        if (mCountry.toLowerCase().contains(text)) {
            return true;
        }
        if (mState.toLowerCase().contains(text)) {
            return true;
        }

        // check descriptions
        String[] desc = getDescriptions();
        return desc[0].toLowerCase().contains(text) ||
                desc[1].toLowerCase().contains(text);
    }
}
