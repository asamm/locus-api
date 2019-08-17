package locus.api.android.features.mapProvider.data

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

class MapTileRequest : Storable() {

    /*
     * Number of tile X
     */
    var tileX: Int = -1
    /*
     * Number of tile Y
     */
    var tileY: Int = -1
    /*
     * Zoom value for online maps
     */
    var tileZoom: Int = -1

    /*
     * X coordinate of left border of image in current map system
     */
    var mapSystemX1: Double = 0.0
    /*
     * Y coordinate of top border of image in current map system
     */
    var mapSystemY1: Double = 0.0
    /*
     * X coordinate of right border of image in current map system
     */
    var mapSystemX2: Double = 0.0
    /*
     * Y coordinate of bottom border of image in current map system
     */
    var mapSystemY2: Double = 0.0

    //*************************************************
    // STORABLE
    //*************************************************

    override fun getVersion(): Int {
        return 0
    }

    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        tileX = dr.readInt()
        tileY = dr.readInt()
        tileZoom = dr.readInt()

        mapSystemX1 = dr.readDouble()
        mapSystemY1 = dr.readDouble()
        mapSystemX2 = dr.readDouble()
        mapSystemY2 = dr.readDouble()
    }

    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeInt(tileX)
        dw.writeInt(tileY)
        dw.writeInt(tileZoom)

        dw.writeDouble(mapSystemX1)
        dw.writeDouble(mapSystemY1)
        dw.writeDouble(mapSystemX2)
        dw.writeDouble(mapSystemY2)
    }
}