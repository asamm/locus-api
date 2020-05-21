package locus.api.objects.geoData

import locus.api.objects.extra.Location
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException

class Circle() : GeoData() {

    /**
     * Center location.
     */
    var location: Location = Location()
        private set

    /**
     * Radius of circle (in m).
     */
    var radius: Float = 0.0f
        private set(value) {
            field = if (value < 0.0f) {
                0.0f
            } else {
                value
            }
        }

    /**
     * Flag if circle should be draw as precise geodetic circle. This will highly improve
     * circle precision for bigger radius (100+ metres), but may affect rendering performance.
     */
    var isDrawPrecise: Boolean = false

    constructor(loc: Location, radius: Float, drawPrecise: Boolean)
            : this() {
        this.location = loc
        this.radius = radius
        this.isDrawPrecise = drawPrecise
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 2
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        // GeoData
        id = dr.readLong()
        name = dr.readString()
        readExtraData(dr)
        readStyles(dr)

        // private
        location = dr.readStorable(Location::class.java)
        radius = dr.readFloat()
        isDrawPrecise = dr.readBoolean()

        // V1
        if (version >= 1) {
            timeCreated = dr.readLong()
        }

        // V2
        if (version >= 2) {
            timeUpdated = dr.readLong()
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        // GeoData
        dw.writeLong(id)
        dw.writeString(name)
        writeExtraData(dw)
        writeStyles(dw)

        // private
        location.write(dw)
        dw.writeFloat(radius)
        dw.writeBoolean(isDrawPrecise)

        // V1
        dw.writeLong(timeCreated)

        // V2
        dw.writeLong(timeUpdated)
    }
}
