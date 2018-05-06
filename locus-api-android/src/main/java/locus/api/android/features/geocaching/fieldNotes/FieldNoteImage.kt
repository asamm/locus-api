package locus.api.android.features.geocaching.fieldNotes

import java.io.IOException

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

/**
 * Created by menion on 14. 7. 2014.
 * Class is part of Locus project
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
    var description = ""
    /**
     * Image itself, reduced to usable size
     */
    var image: ByteArray? = null

    /**************************************************/
    // STORABLE
    /**************************************************/

    override fun getVersion(): Int {
        return 0
    }

    override fun reset() {
        id = -1L
        fieldNoteId = -1L
        caption = ""
        description = ""
        image = null
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
        if (image != null && image!!.size > 0) {
            dw.writeInt(image!!.size)
            dw.write(image!!)
        } else {
            dw.writeInt(0)
        }
    }
}
