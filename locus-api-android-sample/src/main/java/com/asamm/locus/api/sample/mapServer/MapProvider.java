package com.asamm.locus.api.sample.mapServer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import locus.api.android.features.mapProvider.MapTileService;
import locus.api.android.features.mapProvider.data.MapConfigLayer;
import locus.api.android.features.mapProvider.data.MapTileRequest;
import locus.api.android.features.mapProvider.data.MapTileResponse;
import locus.api.utils.Logger;
import locus.api.utils.Utils;

public class MapProvider extends MapTileService {

	private static final String TAG = MapProvider.class.getSimpleName();
	
	@Override
	public List<MapConfigLayer> getMapConfigs() {
		Logger.logD("MapProvider", "getMapConfiguration()");
		return generateMapConfig();
	}

	@Override
	public MapTileResponse getMapTile(MapTileRequest request) {
		Logger.logD("MapProvider", "getMapTile(" + request + ")");
		return loadMapTile(request);
	}

	private List<MapConfigLayer> generateMapConfig() {
		List<MapConfigLayer> maps = new ArrayList<MapConfigLayer>();
		maps.add(generateMapConfiguration(0));
		maps.add(generateMapConfiguration(1));
		maps.add(generateMapConfiguration(2));
		return maps;
	}
	
	private MapConfigLayer generateMapConfiguration(int zoom) {
		int mapSize = 1 << (zoom + 8);
		
		// create empty object and set projection (Spherical Mercator)
		MapConfigLayer mapConfig = new MapConfigLayer();
		mapConfig.setProjEpsg(3857);
		
		// set name and description
		mapConfig.setName("OSM MapQuest");
		mapConfig.setDescription("Testing map");
		
		// define size of tiles
		mapConfig.setTileSizeX(256);
		mapConfig.setTileSizeY(256);
		
		// define size of map
		mapConfig.setXmax(mapSize);
		mapConfig.setYmax(mapSize);
		
		// specify zoom level
		mapConfig.setZoom(zoom);
		
		// add at least two calibration points
    	double maxX = 20037508.343;
    	double maxY = 20037508.343;
    	mapConfig.addCalibrationPoint(0, 0, maxY, -maxX);
    	mapConfig.addCalibrationPoint(mapSize, 0, maxY, maxX);
    	mapConfig.addCalibrationPoint(0, mapSize, -maxY, -maxX);
    	mapConfig.addCalibrationPoint(mapSize, mapSize, -maxY, maxX);
    	
    	// return generated map
    	return mapConfig;
	}
	
	private MapTileResponse loadMapTile(MapTileRequest request) {
		MapTileResponse resp = new MapTileResponse();

		// load images
		String fileName = "tile_" + request.getTileX() + "_" + 
				request.getTileY() + "_" + request.getTileZoom() + ".jpg";
		byte[] tileData = loadMapTile(fileName);
		if (tileData == null || tileData.length == 0) {
			resp.setResultCode(MapTileResponse.CODE_NOT_EXISTS);
			return resp;
		}
		
		// convert to bitmap
		Bitmap img = BitmapFactory.decodeByteArray(tileData, 0, tileData.length);
		if (img == null) {
			resp.setResultCode(MapTileResponse.CODE_INTERNAL_ERROR);
			return resp;
		} else {
			resp.setResultCode(MapTileResponse.CODE_VALID);
			resp.setImage(img);
			return resp;
		}
	}
	
    private byte[] loadMapTile(String name) {
    	InputStream is = null;
    	try {
			is = getAssets().open("map_tiles/" + name);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			// read data
			int readed;
			while ((readed = is.read()) != -1) {
				baos.write(readed);
			}
			byte[] data = baos.toByteArray();
			baos.close();
			return data;
		} catch (Exception e) {
			Logger.logE(TAG, "loadMapTile(" + name + "), not exists", e);
			return null;
		} finally {
			Utils.closeStream(is);
		}
    }
}
