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
import locus.api.objects.styles.HotSpot.Companion.write
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException

class IconStyle : Storable() {

    /**
     * Tint color for the icon.
     */
    var color: Int = GeoDataStyle.COLOR_DEFAULT

    /**
     * Current defined scale.
     * 1.0f means base no-scale value.
     */
    var scale: Float = 1.0f
        set(value) {
            if (value != 0.0f) {
                field = value
                this.scaleCurrent = value
            }
        }

    /**
     * Orientation of the icon [in degrees] around hotSpot.
     */
    var heading: Float = 0.0f
    @Deprecated("do not use directly")
    var iconHref: String? = null

    /**
     * HotSpot for the icon.
     */
    var hotSpot: HotSpot = HotSpot.HOT_STOP_BOTTOM_CENTER

    // temporary variables for Locus usage that are not serialized
    // and are for private Locus usage only
    var icon: Any? = null
    var iconW: Int = -1
    var iconH: Int = -1
    var scaleCurrent: Float = 1.0f

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 0
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        color = dr.readInt()
        scale = dr.readFloat()
        heading = dr.readFloat()
        iconHref = dr.readString()
        hotSpot = HotSpot.read(dr)
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeInt(color)
        dw.writeFloat(scale)
        dw.writeFloat(heading)
        dw.writeString(iconHref)
        hotSpot.write(dw)
    }
}
