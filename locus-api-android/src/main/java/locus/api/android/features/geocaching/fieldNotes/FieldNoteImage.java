package locus.api.android.features.geocaching.fieldNotes;

import java.io.IOException;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

/**
 * Created by menion on 14. 7. 2014.
 * Class is part of Locus project
 */
public class FieldNoteImage extends Storable {

    /**
     * ID of image in database
     */
    private long mId;
    /**
     * ID of parent field note in database
     */
    private long mFieldNoteId;
    /**
     * Visible caption for image
     */
    private String mCaption;
    /**
     * Description for image
     */
    private String mDescription;
    /**
     * Image itself, reduced to usable size
     */
    private byte[] mImage;

    public FieldNoteImage() {

    }

    /**************************************************/
    /*                GET & SET METHODS               */
    /**************************************************/

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public long getFieldNoteId() {
        return mFieldNoteId;
    }

    public void setFieldNoteId(long fieldNoteId) {
        this.mFieldNoteId = fieldNoteId;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        if (caption == null) {
            caption = "";
        }
        this.mCaption = caption;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        if (description == null) {
            description = "";
        }
        this.mDescription = description;
    }

    public byte[] getImage() {
        return mImage;
    }

    public void setImage(byte[] image) {
        this.mImage = image;
    }

    /**************************************************/
    /*                 STORABLE PART                  */
    /**************************************************/

    @Override
    protected int getVersion() {
        return 0;
    }

    @Override
    public void reset() {
        mId = -1L;
        mFieldNoteId = -1L;
        mCaption = "";
        mDescription = "";
        mImage = null;
    }

    @Override
    protected void readObject(int version, DataReaderBigEndian dr) throws IOException {
        mId = dr.readLong();
        mFieldNoteId = dr.readLong();
        mCaption = dr.readString();
        mDescription = dr.readString();
        int imgSize = dr.readInt();
        if (imgSize > 0) {
            mImage = new byte[imgSize];
            dr.readBytes(mImage);
        }
    }

    @Override
    protected void writeObject(DataWriterBigEndian dw) throws IOException {
        dw.writeLong(mId);
        dw.writeLong(mFieldNoteId);
        dw.writeString(mCaption);
        dw.writeString(mDescription);
        if (mImage != null && mImage.length > 0) {
            dw.writeInt(mImage.length);
            dw.write(mImage);
        } else {
            dw.writeInt(0);
        }
    }
}
