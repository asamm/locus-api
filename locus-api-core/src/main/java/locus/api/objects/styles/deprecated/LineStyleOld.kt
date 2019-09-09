package locus.api.objects.styles.deprecated

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

import locus.api.objects.styles.GeoDataStyle.Companion.COLOR_DEFAULT
import locus.api.objects.styles.GeoDataStyle.Companion.WHITE

/**
 * Old Line-style system.
 */
internal class LineStyleOld : Storable() {

    // KML styles
    internal var color = COLOR_DEFAULT
    // width of line [px | m]
    internal var width = 1.0f
    internal var gxOuterColor = COLOR_DEFAULT
    internal var gxOuterWidth = 0.0f
    /**
     * Not used. Instead of using this parameter, use [.width] and define
     * [.units] to Units.METRES
     */
    @Deprecated("")
    internal var gxPhysicalWidth = 0.0f
    internal var gxLabelVisibility = false

    // Locus extension
    internal var colorStyle = ColorStyle.SIMPLE
    internal var units = Units.PIXELS
    internal var lineType = LineType.NORMAL
    internal var drawOutline = false
    internal var colorOutline = WHITE

    // temporary helper to convert old widths. Because of this, we increased version of object to V2,
    // so it's clear if value is in DPI or PX values.
    private var objectVersion: Int = 0

    /**
     * Special color style for a lines.
     */
    internal enum class ColorStyle {
        // simple coloring
        SIMPLE,
        // coloring by speed value
        BY_SPEED,
        // coloring (relative) by altitude value
        BY_ALTITUDE,
        // coloring (relative) by accuracy
        BY_ACCURACY,
        // coloring (relative) by speed change
        BY_SPEED_CHANGE,
        // coloring (relative) by slope
        BY_SLOPE_REL,
        // coloring (relative) by heart rate value
        BY_HRM,
        // coloring (relative) by cadence
        BY_CADENCE,
        // coloring (absolute) by slope
        BY_SLOPE_ABS
    }

    /**
     * Used units for line width.
     */
    internal enum class Units {
        PIXELS, METRES
    }

    /**
     * Type how line is presented to user.
     */
    internal enum class LineType {
        NORMAL, DOTTED, DASHED_1, DASHED_2, DASHED_3,
        SPECIAL_1, SPECIAL_2, SPECIAL_3,
        ARROW_1, ARROW_2, ARROW_3,
        CROSS_1, CROSS_2
    }

    /**
     * Create new line style object.
     */
    init {
        objectVersion = getVersion()
    }

    // STORABLE

    override fun getVersion(): Int {
        return 2
    }

    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        objectVersion = version
        color = dr.readInt()
        width = dr.readFloat()
        gxOuterColor = dr.readInt()
        gxOuterWidth = dr.readFloat()
        gxPhysicalWidth = dr.readFloat()
        gxLabelVisibility = dr.readBoolean()

        val cs = dr.readInt()
        if (cs < ColorStyle.values().size) {
            colorStyle = ColorStyle.values()[cs]
        }
        val un = dr.readInt()
        if (un < Units.values().size) {
            units = Units.values()[un]
        }
        val lt = dr.readInt()
        if (lt < LineType.values().size) {
            lineType = LineType.values()[lt]
        }

        // V1
        if (version >= 1) {
            drawOutline = dr.readBoolean()
            colorOutline = dr.readInt()
        }
    }

    override fun writeObject(dw: DataWriterBigEndian) {
        // no write task is done
    }
}