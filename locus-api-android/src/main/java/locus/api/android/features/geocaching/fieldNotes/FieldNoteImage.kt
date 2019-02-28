/**
 * Created by menion on 14. 7. 2014.
 * Class is part of Locus project
 */
package locus.api.android.features.geocaching.fieldNotes

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException

/**
 * Container for images attached to geocaching logs.
 */
class FieldNoteImage : Storable() {

    /**
     * ID of image in database
     */
    var id = -1L
    /**
     * ID of parent field note in database
     */
    var fieldNoteId = -1L
    /**
     * Visible caption for image
     */
    var caption = ""
    /**
     * Description for image
     */
    @Deprecated(message = "Description removed in new GC API, so not needed anymore.")
    var description = ""
    /**
     * Image itself, reduced to usable size
     */
    var image: ByteArray? = null

    /**************************************************/
    // STORABLE
    /**************************************************/

    override fun getVersion(): Int {
        return 1
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readLong()
        fieldNoteId = dr.readLong()
        caption = dr.readString()
        description = dr.readString()
        val imgSize = dr.readInt()
        if (imgSize > 0) {
            image = ByteArray(imgSize)
            dr.readBytes(image!!)
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeLong(id)
        dw.writeLong(fieldNoteId)
        dw.writeString(caption)
        dw.writeString(description)
        if (image != null && image!!.isNotEmpty()) {
            dw.writeInt(image!!.size)
            dw.write(image!!)
        } else {
            dw.writeInt(0)
        }
    }
}
