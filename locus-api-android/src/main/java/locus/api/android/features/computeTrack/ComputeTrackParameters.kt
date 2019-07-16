/**
 * Created by menion on 10. 7. 2014.
 * Class is part of Locus project
 */
package locus.api.android.features.computeTrack

import locus.api.objects.Storable
import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.extra.Location
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException

/**
 * Container for parameters needed to compute route over [ComputeTrackService] instance.
 *
 * Empty constructor for 'Storable' class. Do not use directly
 */
@Suppress("MemberVisibilityCanBePrivate")
class ComputeTrackParameters() : Storable() {

    /**
     * Type of track that should be compute.
     */
    var type: Int = GeoDataExtra.VALUE_RTE_TYPE_CAR
        private set
    /**
     * Flag if user wants with navigation orders.
     */
    var isComputeInstructions: Boolean = true
    /**
     * Direction angle of movement at first point [°].
     */
    var currentDirection: Float = 0.0f
        set(value) {
            field = value
            hasDirection = true
        }
    /**
     * Flag if parameters has defined starting direction.
     */
    var hasDirection: Boolean = false
        private set
    /**
     * List of defined via-points (location)
     */
    var locations: Array<Location> = arrayOf()
        private set

    /**
     * Create new parameters for track compute.
     *
     * @param type type of track to compute
     * @param locs list of defined points over which route should be computed
     */
    constructor(type: Int, locs: Array<Location>) : this() {
        this.type = type
        if (locs.size < 2) {
            throw IllegalArgumentException("'locs' parameter" + " cannot be 'null' or smaller then 2")
        }
        this.locations = locs
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 1
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        type = dr.readInt()
        isComputeInstructions = dr.readBoolean()
        currentDirection = dr.readFloat()

        // read locations
        val size = dr.readInt()
        val locs = arrayListOf<Location>()
        for (i in 0 until size) {
            locs.add(Location().apply { read(dr) })
        }
        locations = locs.toTypedArray()

        // V1
        if (version >= 1) {
            hasDirection = dr.readBoolean()
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeInt(type)
        dw.writeBoolean(isComputeInstructions)
        dw.writeFloat(currentDirection)

        // write locations
        dw.writeInt(locations.size)
        for (mLoc in locations) {
            dw.writeStorable(mLoc)
        }

        // V1
        dw.writeBoolean(hasDirection)
    }
}
