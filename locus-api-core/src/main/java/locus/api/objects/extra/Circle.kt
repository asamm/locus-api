package locus.api.objects.extra

import java.io.IOException
import java.io.InvalidObjectException

import locus.api.objects.GeoData
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

class Circle() : GeoData() {

    // center location
    var location: Location? = null
        private set
    // radius of circle
    var radius: Float = 0.0f
        private set
    // draw as precise geodetic circle
    var isDrawPrecise: Boolean = false

    @Throws(IOException::class)
    constructor(loc: Location, radius: Float, drawPrecise: Boolean) : this() {
        this.location = loc
        this.radius = radius
        this.isDrawPrecise = drawPrecise
        checkData()
    }

    @Throws(InvalidObjectException::class)
    private fun checkData() {
        if (location == null) {
            throw InvalidObjectException("Location cannot be 'null'")
        }
        // store radius
        if (radius <= 0.0f) {
            throw InvalidObjectException("radius have to be bigger then 0")
        }
    }

    // STORABLE PART

    override fun getVersion(): Int {
        return 1
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readLong()
        name = dr.readString()
        readExtraData(dr)
        readStyles(dr)

        // PRIVATE PART

        location = dr.readStorable(Location::class.java)
        radius = dr.readFloat()
        isDrawPrecise = dr.readBoolean()

        // V1
        if (version >= 1) {
            timeCreated = dr.readLong()
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeLong(id)
        dw.writeString(name)
        writeExtraData(dw)
        writeStyles(dw)

        // PRIVATE PART

        location!!.write(dw)
        dw.writeFloat(radius)
        dw.writeBoolean(isDrawPrecise)

        // V1
        dw.writeLong(timeCreated)
    }
}
