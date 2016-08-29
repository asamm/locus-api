package locus.api.android.features.geocaching.fieldNotes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import locus.api.objects.Storable;
import locus.api.objects.geocaching.GeocachingLog;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

/**
 * Created by menion on 7. 7. 2014.
 * Class is part of Locus project
 */
public class FieldNote extends Storable {

    // ID of log in database
    private long mId;
    // code of cache
    private String mCacheCode;
    // name of cache
    private String mCacheName;
    // type of log
    private int mType;
    // time of this log (in UTC+0)
    private long mTime;
    // note of log defined by user
    private String mNote;
    // flag if log is marked as mFavorite
    private boolean mFavorite;
    // flag if log was already send
    private boolean mLogged;
    // list of attached images
    private List<FieldNoteImage> mImages;

    /**
     * Default empty constructor.
     */
    public FieldNote() {
        super();
    }

    public FieldNote(byte[] data) throws IOException {
        super(data);
    }

    /**************************************************/
    // GET & SET METHODS
    /**************************************************/

    // ID

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    // CACHE CODE

    public String getCacheCode() {
        return mCacheCode;
    }

    public void setCacheCode(String cacheCode) {
        if (cacheCode == null) {
            cacheCode = "";
        }
        this.mCacheCode = cacheCode;
    }

    // CACHE NAME

    public String getCacheName() {
        return mCacheName;
    }

    public void setCacheName(String cacheName) {
        if (cacheName == null) {
            cacheName = "";
        }
        this.mCacheName = cacheName;
    }

    // TYPE

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    // TIME (UTC+0)

    /**
     * Get field note time (in UTC+0).
     * @return UTC time
     */
    public long getTime() {
        return mTime;
    }

    /**
     * Set new time for current field note.
     * @param dateTime new time in ms of UTC+0
     */
    public void setTime(long dateTime) {
        this.mTime = dateTime;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        if (note == null) {
            note = "";
        }
        this.mNote = note;
    }

    // FAVORITE

    public boolean isFavorite() {
        return mFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.mFavorite = favorite;
    }

    // "IS LOGGED" FLAG

    public boolean isLogged() {
        return mLogged;
    }

    public void setLogged(boolean logged) {
        this.mLogged = logged;
    }

    // IMAGES

    public void addImage(FieldNoteImage image) {
        // check image
        if (image == null) {
            throw new IllegalArgumentException("Image not valid");
        }

        // add to list
        mImages.add(image);
    }

	/**
	 * Get iterator for work with attached images.
	 * @return iterator over images
	 */
    public Iterator<FieldNoteImage> getImages() {
        return mImages.iterator();
    }

	/**
	 * Return count of images attached to this log.
	 * @return count of images
	 */
	public int getImagesCount() {
		return mImages.size();
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
        mId = -1L;
        mCacheCode = "";
        mCacheName = "";
        mType = GeocachingLog.CACHE_LOG_TYPE_FOUND;
        mTime = 0L;
        mNote = "";
        mFavorite = false;
        mLogged = false;
        mImages = new ArrayList<>();
    }

    @Override
    protected void readObject(int version, DataReaderBigEndian dr) throws IOException {
        mId = dr.readLong();
        mCacheCode = dr.readString();
        mCacheName = dr.readString();
        mType = dr.readInt();
        mTime = dr.readLong();
        mNote = dr.readString();
        mFavorite = dr.readBoolean();
        mLogged = dr.readBoolean();
        //noinspection unchecked
        mImages = (List<FieldNoteImage>)
                dr.readListStorable(FieldNoteImage.class);
    }

    @Override
    protected void writeObject(DataWriterBigEndian dw) throws IOException {
        dw.writeLong(mId);
        dw.writeString(mCacheCode);
        dw.writeString(mCacheName);
        dw.writeInt(mType);
        dw.writeLong(mTime);
        dw.writeString(mNote);
        dw.writeBoolean(mFavorite);
        dw.writeBoolean(mLogged);
        dw.writeListStorable(mImages);
    }
}
