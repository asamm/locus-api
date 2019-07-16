/**
 * Created by menion on 10. 7. 2014.
 * Class is part of Locus project
 */
package locus.api.android.objects

import android.os.Parcel
import android.os.Parcelable

/**
 * Nice parcelable implementation that is able to carry byte array between consumers.
 */
class ParcelableContainer : Parcelable {

    /**
     * Inner byte array
     */
    var data: ByteArray? = null
        private set

    constructor(data: ByteArray?) {
        // check data
        if (data == null) {
            throw IllegalArgumentException("'data' cannot 'null'")
        }

        // store data container
        this.data = data
    }

    private constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    override fun describeContents(): Int {
        return 0
    }

    private fun readFromParcel(`in`: Parcel) {
        data = ByteArray(`in`.readInt())
        `in`.readByteArray(data)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // write data type
        dest.writeInt(data!!.size)
        dest.writeByteArray(data)
    }

    companion object CREATOR : Parcelable.Creator<ParcelableContainer> {
        override fun createFromParcel(parcel: Parcel): ParcelableContainer {
            return ParcelableContainer(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableContainer?> {
            return arrayOfNulls(size)
        }
    }
}
