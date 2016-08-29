package locus.api.android.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by menion on 10. 7. 2014.
 * Class is part of Locus project
 */
public class ParcelableContainer implements Parcelable {

    // stored data container
    private byte[] mData;

    public ParcelableContainer(byte[] data) {
        // check data
        if (data == null) {
            throw new IllegalArgumentException("'data' cannot 'null'");
        }

        // store data container
        this.mData = data;
    }

    private ParcelableContainer(Parcel in) {
        readFromParcel(in);
    }

    /**
     * Return inner byte array
     * @return
     */
    public byte[] getData() {
        return mData;
    }

    // PARCELABLE PART

    public static final Parcelable.Creator<ParcelableContainer> CREATOR = new
            Parcelable.Creator<ParcelableContainer>() {

                public ParcelableContainer createFromParcel(Parcel in) {
                    return new ParcelableContainer(in);
                }

                public ParcelableContainer[] newArray(int size) {
                    return new ParcelableContainer[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        mData = new byte[in.readInt()];
        in.readByteArray(mData);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // write data type
        dest.writeInt(mData.length);
        dest.writeByteArray(mData);
    }
}
