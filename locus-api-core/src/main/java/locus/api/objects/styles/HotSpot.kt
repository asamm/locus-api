package locus.api.objects.styles

import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

data class HotSpot(
    val x: Double = 0.5,
    val xUnits: Units = Units.FRACTION,
    val y: Double = 0.5,
    val yUnits: Units = Units.FRACTION,
) {

    enum class Units {
        FRACTION, PIXELS, INSET_PIXELS
    }

    /**
     * Compute reference coordinates of certain image based on the defined hotSpot parameters.
     */
    fun HotSpot.getCoords(sourceWidth: Double, sourceHeight: Double, result: DoubleArray? = DoubleArray(2)): DoubleArray {
        var resultNew = result

        // check container for results
        if (resultNew == null || resultNew.size != 2) {
            resultNew = DoubleArray(2)
        }

        // set X units
        when (xUnits) {
            Units.FRACTION -> {
                resultNew[0] = sourceWidth * x
            }
            Units.PIXELS -> {
                resultNew[0] = x
            }
            Units.INSET_PIXELS -> {
                resultNew[0] = sourceWidth - x
            }
        }

        // set Y units
        when (yUnits) {
            Units.FRACTION -> {
                resultNew[1] = sourceHeight * (1.0 - y)
            }
            Units.PIXELS -> {
                resultNew[1] = sourceHeight - y
            }
            Units.INSET_PIXELS -> {
                resultNew[1] = y
            }
        }

        // return result
        return resultNew
    }

    companion object {

        val HOT_STOP_BOTTOM_CENTER = HotSpot(
            x = 0.5,
            xUnits = Units.FRACTION,
            y = 0.0,
            yUnits = Units.FRACTION
        )

        val HOT_STOP_CENTER_CENTER = HotSpot(
            x = 0.5,
            xUnits = Units.FRACTION,
            y = 0.5,
            yUnits = Units.FRACTION
        )

        val HOT_STOP_TOP_LEFT = HotSpot(
            x = 0.0,
            xUnits = Units.FRACTION,
            y = 1.0,
            yUnits = Units.FRACTION
        )

        // "STORABLE"

        fun read(dr: DataReaderBigEndian): HotSpot {
            val x = dr.readDouble()
            val xUnits = Units.values()[dr.readInt()]
            val y = dr.readDouble()
            val yUnits = Units.values()[dr.readInt()]

            // optimization
            if (x == HOT_STOP_BOTTOM_CENTER.x
                && xUnits == HOT_STOP_BOTTOM_CENTER.xUnits
                && y == HOT_STOP_BOTTOM_CENTER.y
                && yUnits == HOT_STOP_BOTTOM_CENTER.yUnits
            ) {
                return HOT_STOP_BOTTOM_CENTER
            }
            if (x == HOT_STOP_CENTER_CENTER.x
                && xUnits == HOT_STOP_CENTER_CENTER.xUnits
                && y == HOT_STOP_CENTER_CENTER.y
                && yUnits == HOT_STOP_CENTER_CENTER.yUnits
            ) {
                return HOT_STOP_CENTER_CENTER
            }

            // return custom hotStop
            return HotSpot(
                x = dr.readDouble(),
                xUnits = Units.values()[dr.readInt()],
                y = dr.readDouble(),
                yUnits = Units.values()[dr.readInt()]
            )
        }

        fun HotSpot.write(dw: DataWriterBigEndian) {
            dw.writeDouble(x)
            dw.writeInt(xUnits.ordinal)
            dw.writeDouble(y)
            dw.writeInt(yUnits.ordinal)
        }
    }
}
