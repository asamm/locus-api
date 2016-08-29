// IMapTileService.aidl
package locus.api.android.features.mapProvider;

import locus.api.android.features.mapProvider.MapDataContainer;

interface IMapTileService {

    MapDataContainer getMapConfigs();
    
    MapDataContainer getMapTile(in MapDataContainer request);
}
