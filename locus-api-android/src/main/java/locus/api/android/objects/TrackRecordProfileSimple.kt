/****************************************************************************
 *
 * Created by menion on 13/03/2019.
 * Copyright (c) 2019. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/

package locus.api.android.objects

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException

/**
 * Simple container for track recording profiles.
 *
 * To obtain list of all available profiles defined in certain Locus Map application,
 * use ActionBasics.getTrackRecordingProfiles]
 */
class TrackRecordProfileSimple() : Storable() {

    /**
     * Profile ID.
     */
    var id: Long = 0L
        private set
    /**
     * Readable profile name.
     */
    var name: String = ""
        private set
    /**
     * Profile generated description.
     */
    var desc: String = ""
        private set
    /**
     * Current profile icon. Icon may be converted to bitmap object
     * thanks to 'Utils.getBitmap()' function.
     */
    var icon: ByteArray? = null
        private set

    /**
     * Generate object from known values.
     */
    internal constructor(id: Long, name: String, desc: String, icon: ByteArray?): this() {
        this.id = id
        this.name = name
        this.desc = desc
        this.icon = icon
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 0
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readLong()
        name = dr.readString()
        desc = dr.readString()
        val imgSize = dr.readInt()
        if (imgSize > 0) {
            icon = ByteArray(imgSize)
            dr.readBytes(icon!!)
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeLong(id)
        dw.writeString(name)
        dw.writeString(desc)
        val imgSize = if (icon != null) icon!!.size else 0
        dw.writeInt(imgSize)
        if (imgSize > 0) {
            dw.write(icon!!)
        }
    }
}
