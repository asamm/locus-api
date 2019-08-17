/****************************************************************************
 *
 * Created by menion on 17.08.2019.
 * Copyright (c) 2019. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/

package locus.api.objects.styles

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException

class BalloonStyle : Storable() {

    enum class DisplayMode {
        DEFAULT, HIDE
    }

    var bgColor: Int = GeoDataStyle.WHITE
    var textColor: Int = GeoDataStyle.BLACK
    var text: String = ""
    var displayMode: DisplayMode = DisplayMode.DEFAULT

    //*************************************************
    // STORABLE
    //*************************************************

    override fun getVersion(): Int {
        return 0
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        bgColor = dr.readInt()
        textColor = dr.readInt()
        text = dr.readString()
        val mode = dr.readInt()
        if (mode < DisplayMode.values().size) {
            displayMode = DisplayMode.values()[mode]
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeInt(bgColor)
        dw.writeInt(textColor)
        dw.writeString(text)
        dw.writeInt(displayMode.ordinal)
    }
}