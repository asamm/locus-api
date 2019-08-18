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

/**
 * Container for label style.
 */
class LabelStyle : Storable() {

    /**
     * Color defined for a label.
     */
    var color: Int = GeoDataStyle.COLOR_DEFAULT
    /**
     * Scale defined for a label.
     */
    var scale: Float = 1.0f
        set(value) {
            field = if (value < 0.0f) {
                0.0f
            } else {
                value
            }
        }

    //*************************************************
    // STORABLE
    //*************************************************

    override fun getVersion(): Int {
        return 0
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        color = dr.readInt()
        scale = dr.readFloat()
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeInt(color)
        dw.writeFloat(scale)
    }
}