/*
 * Created by menion on 06/05/2018.
 * This code is part of Locus project from Asamm Software, s. r. o.
 * Copyright (C) 2018
 */

package locus.api.android.features.geocaching.fieldNotes

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

class FieldNoteItem : Storable() {

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
    var action = 0
    /**
     * Trackable item code.
     */
    var code = ""
//    /**
//     * Trackable item tracking code.
//     */
//    var trackingCode = ""
    /**
     * Trackable item name.
     */
    var name = ""
    /**
     * Trackable item visible small icon.
     */
    var icon = ""


    /**************************************************/
    // STORABLE
    /**************************************************/

    override fun getVersion(): Int {
        return 0
    }

    override fun reset() {
        id = -1L
        fieldNoteId = -1L
        action = 0
        code = ""
        name = ""
        icon = ""
    }

    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readLong()
        fieldNoteId = dr.readLong()
        action = dr.readInt()
        code = dr.readString()
        name = dr.readString()
        icon = dr.readString()
    }

    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeLong(id)
        dw.writeLong(fieldNoteId)
        dw.writeInt(action)
        dw.writeString(code)
        dw.writeString(name)
        dw.writeString(icon)
    }
}