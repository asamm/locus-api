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
