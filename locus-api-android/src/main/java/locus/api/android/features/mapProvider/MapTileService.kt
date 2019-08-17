package locus.api.android.features.mapProvider

import android.app.Service
import android.content.Intent
import android.os.IBinder
import locus.api.android.features.mapProvider.data.MapConfigLayer
import locus.api.android.features.mapProvider.data.MapTileRequest
import locus.api.android.features.mapProvider.data.MapTileResponse
import locus.api.utils.Logger
import java.util.*

/**
 * IN PREPARATION - NOT YET FULLY WORKING!!
 */
abstract class MapTileService : Service() {

    private val mBinder = object : IMapTileService.Stub() {

        override fun getMapConfigs(): MapDataContainer {
            val configs = this@MapTileService.mapConfigs
            return if (configs == null || configs.isEmpty()) {
                Logger.logW(TAG, "getMapConfigs(), invalid configs")
                MapDataContainer(ArrayList())
            } else {
                MapDataContainer(configs)
            }
        }

        override fun getMapTile(request: MapDataContainer?): MapDataContainer {
            // check request
            if (request == null || !request.isValid(
                            MapDataContainer.DATA_TYPE_TILE_REQUEST)) {
                Logger.logW(TAG, "getMapTile($request), invalid request")
                val resp = MapTileResponse()
                resp.resultCode = MapTileResponse.CODE_INVALID_REQUEST
                return MapDataContainer(resp)
            }

            // handle request
            val response = this@MapTileService.getMapTile(request.tileRequest!!)
            return if (response == null) {
                Logger.logW(TAG, "getMapTile($request), invalid response")
                val resp = MapTileResponse()
                resp.resultCode = MapTileResponse.CODE_INTERNAL_ERROR
                MapDataContainer(resp)
            } else {
                MapDataContainer(response)
            }
        }
    }

    // ABSTRACT PART

    abstract val mapConfigs: List<MapConfigLayer>?

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    abstract fun getMapTile(request: MapTileRequest): MapTileResponse?

    companion object {

        private val TAG = "MapTileService"
    }
}
