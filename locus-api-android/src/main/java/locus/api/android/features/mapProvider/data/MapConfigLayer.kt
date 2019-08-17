package locus.api.android.features.mapProvider.data

import java.io.IOException
import java.util.ArrayList

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

class MapConfigLayer : Storable() {

    // name from file
    var name: String = ""
    // description of file
    var description: String = ""
    // size of tiles in X dimension
    var tileSizeX: Int = 0
    // size of tiles in Y dimension
    var tileSizeY: Int = 0
    // pixel size of whole image - X
    var xmax: Long = 0L
    // pixel size of whole image - Y
    var ymax: Long = 0L
    // zoom value in multiTile map
    var zoom: Int = -1
    // projection EPSG code
    var projEpsg: Int = 0
    // point that define map tiles
    private val mCalPoints: MutableList<CalibrationPoint> = arrayListOf()

    val calibrationPoints: List<CalibrationPoint>
        get() = mCalPoints

    // CALIBRATION POINTS

    fun addCalibrationPoint(x: Double, y: Double, lat: Double, lon: Double) {
        addCalibrationPoint(CalibrationPoint(x, y, lat, lon))
    }

    fun addCalibrationPoint(cp: CalibrationPoint) {
        mCalPoints.add(cp)
    }

    class CalibrationPoint() {

        var x: Double = 0.0
        var y: Double = 0.0
        var lat: Double = 0.0
        var lon: Double = 0.0

        constructor(x: Double, y: Double, lat: Double, lon: Double) : this() {
            this.x = x
            this.y = y
            this.lat = lat
            this.lon = lon
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
        name = dr.readString()
        description = dr.readString()
        tileSizeX = dr.readInt()
        tileSizeY = dr.readInt()
        xmax = dr.readLong()
        ymax = dr.readLong()
        zoom = dr.readInt()
        projEpsg = dr.readInt()

        // load calibration points
        val count = dr.readInt()
        for (i in 0 until count) {
            addCalibrationPoint(
                    dr.readDouble(), dr.readDouble(),
                    dr.readDouble(), dr.readDouble())
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        // save parameters
        dw.writeString(name)
        dw.writeString(description)
        dw.writeInt(tileSizeX)
        dw.writeInt(tileSizeY)
        dw.writeLong(xmax)
        dw.writeLong(ymax)
        dw.writeInt(zoom)
        dw.writeInt(projEpsg)

        // save calibration points
        dw.writeInt(mCalPoints.size)
        for (i in mCalPoints.indices) {
            val cal = mCalPoints[i]
            dw.writeDouble(cal.x)
            dw.writeDouble(cal.y)
            dw.writeDouble(cal.lat)
            dw.writeDouble(cal.lon)
        }
    }
}
