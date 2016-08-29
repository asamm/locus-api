package locus.api.android.features.mapProvider.data;

import java.io.IOException;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

public class MapTileRequest extends Storable {

    /*
     * Number of tile X 
     */
    private int mTileX;
    /*
     * Number of tile Y
     */
    private int mTileY;
    /*
     * Zoom value for online maps
     */
    private int mTileZoom;
	
    /*
     * X coordinate of left border of image in current map system 
     */
    private double mMapSystemX1;
    /*
     * Y coordinate of top border of image in current map system
     */
    private double mMapSystemY1;
    /*
     * X coordinate of right border of image in current map system 
     */
    private double mMapSystemX2;
    /*
     * Y coordinate of bottom border of image in current map system 
     */
    private double mMapSystemY2;
    
    // BASIC CONSTRUCTORS
    
	public MapTileRequest() {
		super();
	}
	
	public MapTileRequest(byte[] data) throws IOException {
		super(data);
	}
	
	// GET & SET METHODS
	
	public int getTileX() {
		return mTileX;
	}

	public void setTileX(int tileX) {
		this.mTileX = tileX;
	}

	public int getTileY() {
		return mTileY;
	}

	public void setTileY(int tileY) {
		this.mTileY = tileY;
	}

	public int getTileZoom() {
		return mTileZoom;
	}

	public void setTileZoom(int tileZoom) {
		this.mTileZoom = tileZoom;
	}

	public double getMapSystemX1() {
		return mMapSystemX1;
	}

	public void setMapSystemX1(double mapSystemX1) {
		this.mMapSystemX1 = mapSystemX1;
	}

	public double getMapSystemY1() {
		return mMapSystemY1;
	}

	public void setMapSystemY1(double mapSystemY1) {
		this.mMapSystemY1 = mapSystemY1;
	}

	public double getMapSystemX2() {
		return mMapSystemX2;
	}

	public void setMapSystemX2(double mapSystemX2) {
		this.mMapSystemX2 = mapSystemX2;
	}

	public double getMapSystemY2() {
		return mMapSystemY2;
	}

	public void setMapSystemY2(double mapSystemY2) {
		this.mMapSystemY2 = mapSystemY2;
	}
	
	
	/**************************************************/
	/*                 STORABLE PART                  */
	/**************************************************/
	
	@Override
	protected int getVersion() {
		return 0;
	}

	@Override
	public void reset() {
		mTileX = -1;
		mTileY = -1;
		mTileZoom = -1;
		
		mMapSystemX1 = 0.0;
		mMapSystemY1 = 0.0;
		mMapSystemX2 = 0.0;
		mMapSystemY2 = 0.0;		
	}

	@Override
	protected void readObject(int version, DataReaderBigEndian dr)
			throws IOException {
		mTileX = dr.readInt();
		mTileY = dr.readInt();
		mTileZoom = dr.readInt();
		
		mMapSystemX1 = dr.readDouble();
		mMapSystemY1 = dr.readDouble();
		mMapSystemX2 = dr.readDouble();
		mMapSystemY2 = dr.readDouble();
	}

	@Override
	protected void writeObject(DataWriterBigEndian dw) throws IOException {
		dw.writeInt(mTileX);
		dw.writeInt(mTileY);
		dw.writeInt(mTileZoom);
		
		dw.writeDouble(mMapSystemX1);
		dw.writeDouble(mMapSystemY1);
		dw.writeDouble(mMapSystemX2);
		dw.writeDouble(mMapSystemY2);
	}
}