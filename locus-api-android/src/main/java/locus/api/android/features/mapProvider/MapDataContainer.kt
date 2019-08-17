package locus.api.android.features.mapProvider

import android.os.Parcel
import android.os.Parcelable

import locus.api.android.features.mapProvider.data.MapConfigLayer
import locus.api.android.features.mapProvider.data.MapTileRequest
import locus.api.android.features.mapProvider.data.MapTileResponse
import locus.api.objects.Storable
import locus.api.utils.Logger

import java.io.IOException

class MapDataContainer : Parcelable {

    // PRIVATE VARIABLES

    // current data type
    private var mDataType: Int = 0

    // container for map config
    var mapConfigurations: List<MapConfigLayer>? = null
        private set
    // container with request for map tile
    var tileRequest: MapTileRequest? = null
        private set
    // container with response
    var tileResponse: MapTileResponse? = null
        private set

    constructor(mapConfigs: List<MapConfigLayer>) {
        this.mDataType = DATA_TYPE_CONFIGURATION
        this.mapConfigurations = mapConfigs
    }

    constructor(tileRequest: MapTileRequest) {
        this.mDataType = DATA_TYPE_TILE_REQUEST
        this.tileRequest = tileRequest
    }

    constructor(tileResponse: MapTileResponse) {
        this.mDataType = DATA_TYPE_TILE_RESPONSE
        this.tileResponse = tileResponse
    }

    /**
     * Check if container contains valid data.
     *
     * @return `true` if data are valid, otherwise returns
     * `false`
     */
    fun isValid(requestedType: Int): Boolean {
        // check types
        if (mDataType != requestedType) {
            Logger.logW(TAG, "isValid(" + requestedType + "), " +
                    "invalid type:" + mDataType)
            return false
        }

        // check by types
        return when (mDataType) {
            DATA_TYPE_CONFIGURATION -> mapConfigurations != null && mapConfigurations!!.isNotEmpty()
            DATA_TYPE_TILE_REQUEST -> tileRequest != null
            DATA_TYPE_TILE_RESPONSE -> tileResponse != null
            else -> false
        }
    }

    // PARCELABLE PART

    private constructor(`in`: Parcel) {
        try {
            readFromParcel(`in`)
        } catch (e: IOException) {
            mDataType = DATA_TYPE_UNDEFINED
            Logger.logE(TAG, "DataTransporter($`in`)", e)
        }

    }

    @Throws(IOException::class)
    private fun readFromParcel(`in`: Parcel) {
        // read type of data in Parcel
        mDataType = `in`.readInt()

        // read map config
        when (mDataType) {
            DATA_TYPE_CONFIGURATION -> {
                val data = ByteArray(`in`.readInt())
                `in`.readByteArray(data)
                mapConfigurations = Storable.readList(MapConfigLayer::class.java, data)
            }
            DATA_TYPE_TILE_REQUEST -> {
                val data = ByteArray(`in`.readInt())
                `in`.readByteArray(data)
                tileRequest = MapTileRequest()
                tileRequest!!.read(data)
            }
            DATA_TYPE_TILE_RESPONSE -> {
                val data = ByteArray(`in`.readInt())
                `in`.readByteArray(data)
                tileResponse = MapTileResponse()
                tileResponse!!.read(data)
            }
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // write data type
        dest.writeInt(mDataType)

        // write config
        when (mDataType) {
            DATA_TYPE_CONFIGURATION -> writeObject(dest, Storable.getAsBytes(mapConfigurations!!)!!)
            DATA_TYPE_TILE_REQUEST -> writeObject(dest, tileRequest!!.asBytes!!)
            DATA_TYPE_TILE_RESPONSE -> writeObject(dest, tileResponse!!.asBytes!!)
        }
    }

    private fun writeObject(dest: Parcel, data: ByteArray) {
        dest.writeInt(data.size)
        dest.writeByteArray(data)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        private val TAG = MapDataContainer::class.java.simpleName

        // TYPE PARAMETERS

        // unknown (undefined) data object
        private const val DATA_TYPE_UNDEFINED = 0
        /*
        * Map configuration
        */
        const val DATA_TYPE_CONFIGURATION = 1
        /*
        * Request with parameters that define tile
        */
        const val DATA_TYPE_TILE_REQUEST = 2
        /*
        * Response with tile data or information about state
        */
        const val DATA_TYPE_TILE_RESPONSE = 3

        @JvmField
        val CREATOR: Parcelable.Creator<MapDataContainer> = object : Parcelable.Creator<MapDataContainer> {
            override fun createFromParcel(parcel: Parcel): MapDataContainer {
                return MapDataContainer(parcel)
            }

            override fun newArray(size: Int): Array<MapDataContainer?> {
                return arrayOfNulls(size)
            }
        }
    }
}
