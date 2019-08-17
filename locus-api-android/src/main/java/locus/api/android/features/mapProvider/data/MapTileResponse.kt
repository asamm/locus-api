package locus.api.android.features.mapProvider.data

import android.graphics.Bitmap

import locus.api.android.utils.UtilsBitmap
import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

class MapTileResponse : Storable() {

    /*
     * Result code that indicate result of whole operation
     */
    var resultCode: Int = CODE_UNKNOWN
    /*
     * Image itself
     */
    var image: Bitmap? = null

    //*************************************************
    // STORABLE
    //*************************************************

    override fun getVersion(): Int {
        return 0
    }

    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        resultCode = dr.readInt()

        // icon
        val size = dr.readInt()
        if (size > 0) {
            val data = dr.readBytes(size)
            image = UtilsBitmap.getBitmap(data)
        } else {
            image = null
        }
    }

    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeInt(resultCode)

        // icon
        if (image == null) {
            dw.writeInt(0)
        } else {
            val data = UtilsBitmap.getBitmap(image!!, Bitmap.CompressFormat.PNG)
            if (data == null || data.isEmpty()) {
                dw.writeInt(0)
            } else {
                dw.writeInt(data.size)
                dw.write(data)
            }
        }
    }

    companion object {

        const val CODE_UNKNOWN = 0
        const val CODE_VALID = 1

        const val CODE_INVALID_REQUEST = 2
        const val CODE_NOT_EXISTS = 3
        const val CODE_INTERNAL_ERROR = 4
    }
}
