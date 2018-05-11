/*
 * Created by menion on 06/05/2018.
 * This code is part of Locus project from Asamm Software, s. r. o.
 * Copyright (C) 2018
 */

package locus.api.android.features.geocaching.fieldNotes

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

class TrackableLog : Storable() {

    // ITEM PARAMETERS

    /**
     * Trackable item code.
     */
    var tbCode = ""
    /**
     * Trackable item name.
     */
    var name = ""
    /**
     * Trackable item visible small icon.
     */
    var icon = ""

    // LOG PARAMETERS

    /**
     * ID of item in database
     */
    var id = -1L
    /**
     * GC code of parent cache.
     */
    var cacheCode = ""
    /**
     * Action we wants to perform on item.
     */
    var action = 0
    /**
     * Recorded tracking code.
     */
    var trackingCode = ""
    /**
     * Time when record was created.
     */
    var time = 0L
    /**
     * Users optional note.
     */
    var note = ""
    /**
     * Flag if log was already send.
     */
    var isLogged = false

    /**************************************************/
    // STORABLE
    /**************************************************/

    override fun getVersion(): Int {
        return 0
    }

    override fun reset() {
        tbCode = ""
        name = ""
        icon = ""
        id = -1L
        cacheCode = ""
        action = 0
        trackingCode = ""
        time = 0L
        note = ""
        isLogged = false
    }

    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        tbCode = dr.readString()
        name = dr.readString()
        icon = dr.readString()
        id = dr.readLong()
        cacheCode = dr.readString()
        action = dr.readInt()
        trackingCode = ""
        time = dr.readLong()
        note = ""
        isLogged = dr.readBoolean()
    }

    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeString(tbCode)
        dw.writeString(name)
        dw.writeString(icon)
        dw.writeLong(id)
        dw.writeString(cacheCode)
        dw.writeInt(action)
        dw.writeString(trackingCode)
        dw.writeLong(time)
        dw.writeString(note)
        dw.writeBoolean(isLogged)
    }
}