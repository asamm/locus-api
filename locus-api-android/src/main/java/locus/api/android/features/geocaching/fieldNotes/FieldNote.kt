package locus.api.android.features.geocaching.fieldNotes

import locus.api.objects.Storable
import locus.api.objects.geocaching.GeocachingLog
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException

/**
 * Created by menion on 7. 7. 2014.
 * Class is part of Locus project
 */
class FieldNote : Storable() {

    // ID of log in database
    var id = -1L
    // code of cache
    var cacheCode = ""
    // name of cache
    var cacheName = ""
    // type of log
    var type = GeocachingLog.CACHE_LOG_TYPE_FOUND
    // time of this log (in UTC+0)
    var time = 0L
    // note of log defined by user
    var note = ""
    // flag if log is marked as mFavorite
    var isFavorite = false
    // flag if log was already send
    var isLogged = false
    // list of attached images
    var images: MutableList<FieldNoteImage> = arrayListOf()
        private set

    /**************************************************/
    // STORABLE
    /**************************************************/

    override fun getVersion(): Int {
        return 1
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readLong()
        cacheCode = dr.readString()
        cacheName = dr.readString()
        type = dr.readInt()
        time = dr.readLong()
        note = dr.readString()
        isFavorite = dr.readBoolean()
        isLogged = dr.readBoolean()
        images = dr.readListStorable(FieldNoteImage::class.java)
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeLong(id)
        dw.writeString(cacheCode)
        dw.writeString(cacheName)
        dw.writeInt(type)
        dw.writeLong(time)
        dw.writeString(note)
        dw.writeBoolean(isFavorite)
        dw.writeBoolean(isLogged)
        dw.writeListStorable(images)
    }
}
