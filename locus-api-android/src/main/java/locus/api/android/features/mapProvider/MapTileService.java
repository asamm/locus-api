package locus.api.android.features.mapProvider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import locus.api.android.features.mapProvider.data.MapConfigLayer;
import locus.api.android.features.mapProvider.data.MapTileRequest;
import locus.api.android.features.mapProvider.data.MapTileResponse;
import locus.api.utils.Logger;

/**
 *
 *  IN PREPARATION - NOT YET FULLY WORKING!!
 *
 */
public abstract class MapTileService extends Service {

	private static final String TAG = MapTileService.class.getSimpleName();
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

    private final IMapTileService.Stub mBinder = new IMapTileService.Stub() {

		@Override
		public MapDataContainer getMapConfigs() throws RemoteException {
			List<MapConfigLayer> configs = MapTileService.this.
					getMapConfigs();
			if (configs == null || configs.size() == 0) {
				Logger.logW(TAG, "getMapConfigs(), invalid configs");
				return new MapDataContainer(new ArrayList<MapConfigLayer>());
			} else {
				return new MapDataContainer(configs);
			}
		}

		@Override
		public MapDataContainer getMapTile(MapDataContainer request)
				throws RemoteException {
			// check request
			if (request == null || !request.isValid(
					MapDataContainer.DATA_TYPE_TILE_REQUEST)) {
				Logger.logW(TAG, "getMapTile(" + request + "), invalid request");
				MapTileResponse resp = new MapTileResponse();
				resp.setResultCode(MapTileResponse.CODE_INVALID_REQUEST);
				return new MapDataContainer(resp);
			}
			
			// handle request
			MapTileResponse response = MapTileService.this.
					getMapTile(request.getTileRequest());
			if (response == null) {
				Logger.logW(TAG, "getMapTile(" + request + "), invalid response");
				MapTileResponse resp = new MapTileResponse();
				resp.setResultCode(MapTileResponse.CODE_INTERNAL_ERROR);
				return new MapDataContainer(resp);
			} else {
				return new MapDataContainer(response);
			}
		}
    };
    
    // ABSTRACT PART
    
    public abstract List<MapConfigLayer> getMapConfigs();
    
    public abstract MapTileResponse getMapTile(MapTileRequest request);
}
