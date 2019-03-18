package com.asamm.locus.api.sample.mapServer

import android.graphics.BitmapFactory
import locus.api.android.features.mapProvider.MapTileService
import locus.api.android.features.mapProvider.data.MapConfigLayer
import locus.api.android.features.mapProvider.data.MapTileRequest
import locus.api.android.features.mapProvider.data.MapTileResponse
import locus.api.utils.Logger
import locus.api.utils.Utils
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

/**
 * Service that provide map data to Locus Map application.
 *
 * Service is not yet fully working, so consider this as "work in progress".
 */
class MapProvider : MapTileService() {

    override fun getMapConfigs(): List<MapConfigLayer> {
        Logger.logD("MapProvider", "getMapConfiguration()")
        return generateMapConfig()
    }

    override fun getMapTile(request: MapTileRequest): MapTileResponse {
        Logger.logD("MapProvider", "getMapTile($request)")
        return loadMapTile(request)
    }

    private fun generateMapConfig(): List<MapConfigLayer> {
        val maps = ArrayList<MapConfigLayer>()
        maps.add(generateMapConfiguration(0))
        maps.add(generateMapConfiguration(1))
        maps.add(generateMapConfiguration(2))
        return maps
    }

    private fun generateMapConfiguration(zoom: Int): MapConfigLayer {
        val mapSize = 1 shl zoom + 8

        // create empty object and set projection (Spherical Mercator)
        val mapConfig = MapConfigLayer()
        mapConfig.projEpsg = 3857

        // set name and description
        mapConfig.name = "OSM MapQuest"
        mapConfig.description = "Testing map"

        // define size of tiles
        mapConfig.tileSizeX = 256
        mapConfig.tileSizeY = 256

        // define size of map
        mapConfig.xmax = mapSize.toLong()
        mapConfig.ymax = mapSize.toLong()

        // specify zoom level
        mapConfig.zoom = zoom

        // add at least two calibration points
        val maxX = 20037508.343
        val maxY = 20037508.343
        mapConfig.addCalibrationPoint(0.0, 0.0, maxY, -maxX)
        mapConfig.addCalibrationPoint(mapSize.toDouble(), 0.0, maxY, maxX)
        mapConfig.addCalibrationPoint(0.0, mapSize.toDouble(), -maxY, -maxX)
        mapConfig.addCalibrationPoint(mapSize.toDouble(), mapSize.toDouble(), -maxY, maxX)

        // return generated map
        return mapConfig
    }

    private fun loadMapTile(request: MapTileRequest): MapTileResponse {
        val resp = MapTileResponse()

        // load images
        val fileName = "tile_" + request.tileX + "_" +
                request.tileY + "_" + request.tileZoom + ".jpg"
        val tileData = loadMapTile(fileName)
        if (tileData == null || tileData.size == 0) {
            resp.resultCode = MapTileResponse.CODE_NOT_EXISTS
            return resp
        }

        // convert to bitmap
        val img = BitmapFactory.decodeByteArray(tileData, 0, tileData.size)
        if (img == null) {
            resp.resultCode = MapTileResponse.CODE_INTERNAL_ERROR
            return resp
        } else {
            resp.resultCode = MapTileResponse.CODE_VALID
            resp.image = img
            return resp
        }
    }

    private fun loadMapTile(name: String): ByteArray? {
        var input: InputStream? = null
        try {
            input = assets.open("map_tiles/$name")
            return ByteArrayOutputStream()
                    .apply { input.copyTo(this) }
                    .toByteArray()
        } catch (e: Exception) {
            Logger.logE(TAG, "loadMapTile($name), not exists", e)
            return null
        } finally {
            Utils.closeStream(input)
        }
    }

    companion object {

        // tag for logger
        private const val TAG = "MapProvider"
    }
}
