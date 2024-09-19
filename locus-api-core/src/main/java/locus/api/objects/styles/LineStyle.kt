/**
 * Created by menion on 19/10/2017.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.objects.styles

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException
import java.util.*


class LineStyle : Storable() {

    /**
     * Type how line is presented to user.
     */
    enum class Symbol {
        DOTTED,
        DASHED_1,
        DASHED_2,
        DASHED_3,
        SPECIAL_1,
        SPECIAL_2,
        SPECIAL_3,
        ARROW_1,
        ARROW_2,
        ARROW_3,
        CROSS_1,
        CROSS_2
    }

    /**
     * Special color style for a lines.
     */
    enum class Coloring {
        // simple coloring
        SIMPLE,
        // coloring by speed value
        BY_SPEED,
        // coloring (relative) by speed change
        BY_SPEED_CHANGE,
        // coloring (relative) by altitude value
        BY_ALTITUDE,
        // coloring (relative) by slope
        BY_SLOPE,
        // coloring (relative) by accuracy
        BY_ACCURACY,
        // coloring (relative) by heart rate value
        BY_HRM,
        // coloring (relative) by cadence
        BY_CADENCE
    }

    /**
     * Used units for line width.
     */
    enum class Units {
        PIXELS,
        METRES
    }

    // PARAMETERS

    // flag to draw background
    var drawBase: Boolean = true
    // base background color
    var colorBase: Int = COLOR_BLACK
    // flag to draw symbol
    var drawSymbol: Boolean = false
    // symbol coloring
    var colorSymbol: Int = COLOR_WHITE
    // selected symbol
    var symbol: Symbol = Symbol.DOTTED
    // coloring style
    var coloring: Coloring = Coloring.SIMPLE
    // coloring parameters
    private val coloringParams: Hashtable<String, String> = Hashtable()
    // width of line [px | m]
    var width: Float = 1.0f
    // width units
    var units: Units = Units.PIXELS
    // flag to draw outline
    var drawOutline: Boolean = false
    // color of outline
    var colorOutline: Int = COLOR_WHITE
    // flag if track should be closed and filled
    var drawFill: Boolean = false
    // color of fill
    var colorFill: Int = COLOR_WHITE

    //*************************************************
    // TOOLS
    //*************************************************

    /**
     * Check if any valid "draw" parameter is defined.
     *
     * @return `true` if anything to draw is set
     */
    val isDrawDefined: Boolean
        get() = drawBase || drawSymbol || drawFill

    // COLORING PARAMETERS

    /**
     * Get value for certain coloring parameter.
     *
     * @param key key of parameter
     * @return parameter value
     */
    fun getColoringParam(key: String): String? {
        // check key
        return if (key.isEmpty()) {
            null
        } else {
            coloringParams[key]
        }
    }

    /**
     * Put valid coloring parameter into container.
     *
     * @param key   key of parameter
     * @param value parameter value
     * @return current object
     */
    fun setColoringParam(key: String, value: String?) {
        // check params
        if (key.isEmpty()) {
            return
        }

        // store value
        if (value == null) {
            coloringParams.remove(key)
        } else {
            coloringParams[key] = value
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
        drawBase = dr.readBoolean()
        colorBase = dr.readInt()
        drawSymbol = dr.readBoolean()
        colorSymbol = dr.readInt()
        symbol = Symbol.valueOf(dr.readString())
        coloring = Coloring.valueOf(dr.readString())
        val size = dr.readInt()
        for (i in 0 until size) {
            coloringParams[dr.readString()] = dr.readString()
        }
        width = dr.readFloat()
        units = Units.valueOf(dr.readString())
        drawOutline = dr.readBoolean()
        colorOutline = dr.readInt()
        drawFill = dr.readBoolean()
        colorFill = dr.readInt()
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeBoolean(drawBase)
        dw.writeInt(colorBase)
        dw.writeBoolean(drawSymbol)
        dw.writeInt(colorSymbol)
        dw.writeString(symbol.name)
        dw.writeString(coloring.name)
        dw.writeInt(coloringParams.size)
        for (key in coloringParams.keys) {
            dw.writeString(key)
            dw.writeString(coloringParams[key])
        }
        dw.writeFloat(width)
        dw.writeString(units.name)
        dw.writeBoolean(drawOutline)
        dw.writeInt(colorOutline)
        dw.writeBoolean(drawFill)
        dw.writeInt(colorFill)
    }

    companion object {

        // white color
        private const val COLOR_WHITE = -0x1
        // black color
        private const val COLOR_BLACK = -0x1000000

        // parameters for storing extra meta information for line coloring
        var KEY_CP_ALTITUDE_MANUAL = "alt_man"
        var KEY_CP_ALTITUDE_MANUAL_MIN = "alt_man_min"
        var KEY_CP_ALTITUDE_MANUAL_MAX = "alt_man_max"
        var KEY_CP_SLOPE_MANUAL = "slo_man"
        var KEY_CP_SLOPE_MANUAL_MIN = "slo_man_min"
        var KEY_CP_SLOPE_MANUAL_MAX = "slo_man_max"
    }
}
