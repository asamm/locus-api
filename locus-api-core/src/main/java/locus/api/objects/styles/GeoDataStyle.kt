/*
 * Copyright 2012, Asamm Software, s. r. o.
 *
 * This file is part of LocusAPI.
 *
 * LocusAPI is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * LocusAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public
 * License along with LocusAPI. If not, see
 * <http://www.gnu.org/licenses/lgpl.html/>.
 */

package locus.api.objects.styles

import com.asamm.loggerV2.logW
import locus.api.objects.Storable
import locus.api.objects.styles.deprecated.LineStyleOld
import locus.api.objects.styles.deprecated.OldStyleHelper
import locus.api.objects.styles.deprecated.PolyStyleOld
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException


/**
 * Create new instance of style container.
 */
class GeoDataStyle() : Storable() {

    /**
     * ID in style tag.
     */
    var id: String = ""

    /**
     * name of the style.
     */
    var name: String = ""

    /**
     * Style for icons.
     */
    var iconStyle: IconStyle? = null

    /**
     * Style of label.
     */
    var labelStyle: LabelStyle? = null

    /**
     * Style for line and polygons.
     */
    var lineStyle: LineStyle? = null

    //*************************************************
    // SETTERS & GETTERS
    //*************************************************

    /**
     * Get style Url for an icon.
     *
     * @return style Url/href parameter
     */
    val iconStyleIconUrl: String?
        get() = if (iconStyle == null) {
            null
        } else {
            iconStyle!!.iconHref
        }

    /**
     * Create new instance of style container with defined name.
     *
     * @param name name of style container
     */
    constructor(name: String?) : this() {

        // set name
        if (name != null) {
            this.name = name
        }
    }

    fun setIconStyle(iconUrl: String, scale: Float) {
        setIconStyle(iconUrl, COLOR_DEFAULT, 0.0f, scale)
    }

    fun setIconStyle(iconUrl: String, color: Int, heading: Float, scale: Float) {
        // set style
        iconStyle = IconStyle().apply {
            this.iconHref = iconUrl
            this.color = color
            this.heading = heading
            this.scale = scale
        }

        // set hot spot
        setIconStyleHotSpot(HOTSPOT_BOTTOM_CENTER)
    }

    @Deprecated (message = "Set hotSpot directly to the IconStyle")
    fun setIconStyleHotSpot(hotspot: Int) {
        setIconStyleHotSpot(
            when (hotspot) {
                HOTSPOT_TOP_LEFT -> HotSpot.HOT_STOP_TOP_LEFT
                HOTSPOT_CENTER_CENTER -> HotSpot.HOT_STOP_CENTER_CENTER
                else -> HotSpot.HOT_STOP_BOTTOM_CENTER
            }
        )
    }

    @Deprecated (message = "Set hotSpot directly to the IconStyle")
    fun setIconStyleHotSpot(hotSpot: HotSpot) {
        if (iconStyle == null) {
            logW(tag = TAG) {
                "setIconStyleHotSpot($hotSpot), " +
                        "initialize IconStyle before settings hotSpot or hotSpot is null!"
            }
            return
        }

        // set hotSpot
        iconStyle!!.hotSpot = hotSpot
    }

    /**
     * Set parameters for style that draw a lines.
     *
     * @param color color of lines
     * @param width width of lines in pixels
     */
    fun setLineStyle(color: Int, width: Float) {
        // check if style exists
        if (lineStyle == null) {
            lineStyle = LineStyle()
        }

        // set parameters
        lineStyle!!.colorBase = color
        lineStyle!!.width = width
    }

    /**
     * Set line style for drawing a polygons.
     *
     * @param color color of inner area
     */
    fun setPolyStyle(color: Int) {
        if (lineStyle == null) {
            lineStyle = LineStyle().apply {
                this.drawBase = false
            }
        }
        lineStyle?.drawFill = true
        lineStyle?.colorFill = color
    }

    //*************************************************
    // STORABLE
    //*************************************************

    override fun getVersion(): Int {
        return 2
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        // read core
        id = dr.readString()
        name = dr.readString()

        // ignore old version, not compatible anymore
        if (version == 0) {
            return
        }

        // read styles
        var lineStyleOld: LineStyleOld? = null
        var polyStyleOld: PolyStyleOld? = null
        try {
            if (dr.readBoolean()) {
                // removed
                readUnknownObject(dr)
            }
            if (dr.readBoolean()) {
                iconStyle = read(IconStyle::class.java, dr)
            }
            if (dr.readBoolean()) {
                labelStyle = read(LabelStyle::class.java, dr)
            }
            if (dr.readBoolean()) {
                lineStyleOld = read(LineStyleOld::class.java, dr)
            }
            if (dr.readBoolean()) {
                // removed
                readUnknownObject(dr)
            }
            if (dr.readBoolean()) {
                polyStyleOld = read(PolyStyleOld::class.java, dr)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // convert old style to new system
        lineStyle = OldStyleHelper.convertToNewLineStyle(lineStyleOld, polyStyleOld)

        // V2
        if (version >= 2) {
            if (dr.readBoolean()) {
                lineStyle = LineStyle().apply { read(dr) }
            }
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        // write core
        dw.writeString(id)
        dw.writeString(name)

        // balloon style (removed)
        dw.writeBoolean(false)

        // icon style
        if (iconStyle == null) {
            dw.writeBoolean(false)
        } else {
            dw.writeBoolean(true)
            iconStyle!!.write(dw)
        }

        // label style
        if (labelStyle == null) {
            dw.writeBoolean(false)
        } else {
            dw.writeBoolean(true)
            labelStyle!!.write(dw)
        }

        // line style (removed)
        dw.writeBoolean(false)

        // list style (removed)
        dw.writeBoolean(false)

        // poly style (removed)
        dw.writeBoolean(false)

        // V2
        if (lineStyle == null) {
            dw.writeBoolean(false)
        } else {
            dw.writeBoolean(true)
            lineStyle!!.write(dw)
        }
    }

    companion object {

        // tag for logger
        private const val TAG = "GeoDataStyle"

        @Deprecated (message = "Set hotSpot directory to the IconStyle")
        const val HOTSPOT_BOTTOM_CENTER = 0
        @Deprecated (message = "Set hotSpot directory to the IconStyle")
        const val HOTSPOT_TOP_LEFT = 1
        @Deprecated (message = "Set hotSpot directory to the IconStyle")
        const val HOTSPOT_CENTER_CENTER = 2

        // STYLES

        // shortcut to simple black color
        const val BLACK = -0x1000000

        // shortcut to simple white color
        const val WHITE = -0x1

        // default basic color
        const val COLOR_DEFAULT = WHITE
    }
}
