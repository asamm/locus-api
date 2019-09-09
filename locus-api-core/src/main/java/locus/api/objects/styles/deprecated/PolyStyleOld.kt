package locus.api.objects.styles.deprecated

import locus.api.objects.Storable
import locus.api.objects.styles.GeoDataStyle.Companion.COLOR_DEFAULT
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

/**
 * Old polygon style.
 */
internal class PolyStyleOld : Storable() {

    internal var color = COLOR_DEFAULT
    internal var fill = true
    internal var outline = true

    override fun getVersion(): Int {
        return 0
    }

    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        color = dr.readInt()
        fill = dr.readBoolean()
        outline = dr.readBoolean()
    }

    override fun writeObject(dw: DataWriterBigEndian) {
        // no write task is done
    }
}