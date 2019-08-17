package locus.api.objects.extra

import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

class KmlVec2 {

    var x = 0.5
    var xUnits = Units.FRACTION
    var y = 0.5
    var yUnits = Units.FRACTION

    val asXmlText: String
        get() {
            // insert coordinates
            val sb = StringBuilder()
            sb.append("\t\t\t<hotSpot x=\"").append(x).append("\" y=\"").append(y).append("\"")

            // insert X units
            sb.append(" xunits=\"")
            when (xUnits) {
                Units.FRACTION -> sb.append("fraction")
                Units.PIXELS -> sb.append("pixels")
                Units.INSET_PIXELS -> sb.append("insetPixels")
            }
            sb.append("\"")

            // insert Y units
            sb.append(" yunits=\"")
            when (yUnits) {
                Units.FRACTION -> sb.append("fraction")
                Units.PIXELS -> sb.append("pixels")
                Units.INSET_PIXELS -> sb.append("insetPixels")
            }
            sb.append("\"")

            // close tag and return
            sb.append(" />")
            return sb.toString()
        }

    val copy: KmlVec2
        get() {
            val vec = KmlVec2()
            vec.x = x
            vec.xUnits = xUnits
            vec.y = y
            vec.yUnits = yUnits
            return vec
        }

    enum class Units {
        FRACTION, PIXELS, INSET_PIXELS
    }

    constructor() {}

    constructor(x: Double, xUnits: Units, y: Double, yUnits: Units) {
        this.x = x
        this.xUnits = xUnits
        this.y = y
        this.yUnits = yUnits
    }

    @JvmOverloads
    fun getCoords(sourceWidth: Double, sourceHeight: Double, result: DoubleArray? = DoubleArray(2)): DoubleArray {
        var resultNew = result

        // check container for results
        if (resultNew == null || resultNew.size != 2) {
            resultNew = DoubleArray(2)
        }

        // set X units
        if (xUnits == Units.FRACTION) {
            resultNew[0] = sourceWidth * x
        } else if (xUnits == Units.PIXELS) {
            resultNew[0] = x
        } else if (xUnits == Units.INSET_PIXELS) {
            resultNew[0] = sourceWidth - x
        }

        // set Y units
        if (yUnits == Units.FRACTION) {
            resultNew[1] = sourceHeight * (1.0 - y)
        } else if (yUnits == Units.PIXELS) {
            resultNew[1] = sourceHeight - y
        } else if (yUnits == Units.INSET_PIXELS) {
            resultNew[1] = y
        }

        // return result
        return resultNew
    }

    fun write(dw: DataWriterBigEndian) {
        dw.writeDouble(x)
        dw.writeInt(xUnits.ordinal)
        dw.writeDouble(y)
        dw.writeInt(yUnits.ordinal)
    }

    companion object {

        fun read(dr: DataReaderBigEndian): KmlVec2 {
            val vec = KmlVec2()
            vec.x = dr.readDouble()
            vec.xUnits = Units.values()[dr.readInt()]
            vec.y = dr.readDouble()
            vec.yUnits = Units.values()[dr.readInt()]
            return vec
        }
    }
}
