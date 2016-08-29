package locus.api.android.features.mapProvider;

import java.io.IOException;
import java.util.List;

import locus.api.android.features.mapProvider.data.MapConfigLayer;
import locus.api.android.features.mapProvider.data.MapTileRequest;
import locus.api.android.features.mapProvider.data.MapTileResponse;
import locus.api.objects.Storable;
import locus.api.utils.Logger;
import android.os.Parcel;
import android.os.Parcelable;

public class MapDataContainer implements Parcelable {

	private static final String TAG = MapDataContainer.class.getSimpleName();
	
	// TYPE PARAMETERS
	
	// unknown (undefined) data object
	private static final int DATA_TYPE_UNDEFINED = 0;
	/*
	 * Map configuration
	 */
	public static final int DATA_TYPE_CONFIGURATION = 1;
	/*
	 * Request with parameters that define tile
	 */
	public static final int DATA_TYPE_TILE_REQUEST = 2;
	/*
	 * Response with tile data or information about state
	 */
	public static final int DATA_TYPE_TILE_RESPONSE = 3;
	
	// PRIVATE VARIABLES
	
	// current data type
	private int mDataType;
	
	// container for map config
	private List<MapConfigLayer> mMapConfigs;
	// container with request for map tile
	private MapTileRequest mMapTileRequest;
	// container with response
	private MapTileResponse mMapTileResponse;
	
	public MapDataContainer(List<MapConfigLayer> mapConfigs) {
		this.mDataType = DATA_TYPE_CONFIGURATION;
		this.mMapConfigs = mapConfigs;
	}
	
	public MapDataContainer(MapTileRequest tileRequest) {
		this.mDataType = DATA_TYPE_TILE_REQUEST;
		this.mMapTileRequest = tileRequest;
	}
	
	public MapDataContainer(MapTileResponse tileResponse) {
		this.mDataType = DATA_TYPE_TILE_RESPONSE;
		this.mMapTileResponse = tileResponse;
	}
	
	/**
	 * Check if container contains valid data.
	 * @return <code>true</code> if data are valid, otherwise returns
	 * <code>false</code>
	 */
	public boolean isValid(int requestedType) {
		// check types
		if (mDataType != requestedType) {
			Logger.logW(TAG, "isValid(" + requestedType + "), " +
					"invalid type:" + mDataType);
			return false;
		}
		
		// check by types
		if (mDataType == DATA_TYPE_CONFIGURATION) {
			return mMapConfigs != null && mMapConfigs.size() > 0;
		} else if (mDataType == DATA_TYPE_TILE_REQUEST) {
			return mMapTileRequest != null;
		} else if (mDataType == DATA_TYPE_TILE_RESPONSE) {
			return mMapTileResponse != null;
		}
		return false;
	}
	
	public List<MapConfigLayer> getMapConfigurations() {
		return mMapConfigs;
	}
	
	public MapTileRequest getTileRequest() {
		return mMapTileRequest;
	}
	
	public MapTileResponse getTileResponse() {
		return mMapTileResponse;
	}
	
	// PARCELABLE PART
	
    private MapDataContainer(Parcel in) {
    	try {
    		readFromParcel(in);	
    	} catch (IOException e) {
    		mDataType = DATA_TYPE_UNDEFINED;
    		Logger.logE(TAG, "DataTransporter(" + in + ")", e);
    	}
    }
    
    @SuppressWarnings("unchecked")
	private void readFromParcel(Parcel in) throws IOException {
    	// read type of data in Parcel
        mDataType = in.readInt();
        
        // read map config
        if (mDataType == DATA_TYPE_CONFIGURATION) {
        	byte[] data = new byte[in.readInt()];
        	in.readByteArray(data);
        	mMapConfigs = (List<MapConfigLayer>) 
        			Storable.readList(MapConfigLayer.class, data);
        } else if (mDataType == DATA_TYPE_TILE_REQUEST) {
        	byte[] data = new byte[in.readInt()];
        	in.readByteArray(data);
        	mMapTileRequest = new MapTileRequest(data);
        } else if (mDataType == DATA_TYPE_TILE_RESPONSE) {
        	byte[] data = new byte[in.readInt()];
        	in.readByteArray(data);
        	mMapTileResponse = new MapTileResponse(data);
        }
    }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// write data type
		dest.writeInt(mDataType);
		
		// write config
		if (mDataType == DATA_TYPE_CONFIGURATION) {
			writeObject(dest, Storable.getAsBytes(mMapConfigs));
		} else if (mDataType == DATA_TYPE_TILE_REQUEST) {
			writeObject(dest, mMapTileRequest.getAsBytes());
		} else if (mDataType == DATA_TYPE_TILE_RESPONSE) {
			writeObject(dest, mMapTileResponse.getAsBytes());
		}
	}
	
	private void writeObject(Parcel dest, byte[] data) {
		dest.writeInt(data.length);
		dest.writeByteArray(data);	
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

    public static final Parcelable.Creator<MapDataContainer> CREATOR = new
    		Parcelable.Creator<MapDataContainer>() {
      
    	public MapDataContainer createFromParcel(Parcel in) {
            return new MapDataContainer(in);
        }

        public MapDataContainer[] newArray(int size) {
            return new MapDataContainer[size];
        }
    };
}
