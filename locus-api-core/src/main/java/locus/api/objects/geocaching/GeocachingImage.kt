package locus.api.objects.geocaching

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException

/**
 * Geocaching image class.
 */
class GeocachingImage : Storable() {

    // PARAMETERS

    /**
     * Name of current image.
     */
    var name: String = ""
    /**
     * Defined description for image
     */
    private var description: String = ""
    /**
     * URL to thumbnail of image. Usable for a quick overview.
     */
    var thumbUrl: String = ""
    /**
     * URL to full image. Usually is better to still use URL to some optimized
     * (mobile) image, then to full version.
     */
    var url: String = ""

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 0
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        name = dr.readString()
        description = dr.readString()
        thumbUrl = dr.readString()
        url = dr.readString()
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeString(name)
        dw.writeString(description)
        dw.writeString(thumbUrl)
        dw.writeString(url)
    }
}
