package locus.api.android.features.mapProvider.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

public class MapConfigLayer extends Storable {

    // name from file
    private String mName;
    // description of file
    private String mDescription;
    // size of tiles in X dimension
    private int mTileSizeX;
    // size of tiles in Y dimension
    private int mTileSizeY;
    // pixel size of whole image - X
    private long mXmax;
    // pixel size of whole image - Y
    private long mYmax;
    // zoom value in multiTile map
    private int mZoom;
    // projection EPSG code
    private int mProjEpsg; 
    // point that define map tiles
    private List<CalibrationPoint> mCalPoints;
    
    // BASIC CONSTRUCTORS
    
	public MapConfigLayer() {
		super();
	}
	
	public MapConfigLayer(byte[] data) throws IOException {
		super(data);
	}
	
	// GET & SET METHODS
	
	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

	public int getTileSizeX() {
		return mTileSizeX;
	}

	public void setTileSizeX(int tileSizeX) {
		this.mTileSizeX = tileSizeX;
	}

	public int getTileSizeY() {
		return mTileSizeY;
	}

	public void setTileSizeY(int tileSizeY) {
		this.mTileSizeY = tileSizeY;
	}

	public long getXmax() {
		return mXmax;
	}

	public void setXmax(long xmax) {
		this.mXmax = xmax;
	}

	public long getYmax() {
		return mYmax;
	}

	public void setYmax(long ymax) {
		this.mYmax = ymax;
	}

	public int getZoom() {
		return mZoom;
	}

	public void setZoom(int zoom) {
		this.mZoom = zoom;
	}

	public int getProjEpsg() {
		return mProjEpsg;
	}

	public void setProjEpsg(int projEpsg) {
		this.mProjEpsg = projEpsg;
	}
	
	// CALIBRATION POINTS
	
    public void addCalibrationPoint(double x, double y, double lat, double lon) {
        addCalibrationPoint(new CalibrationPoint(x, y, lat, lon));
    }
 
	public void addCalibrationPoint(CalibrationPoint cp) {
		mCalPoints.add(cp);
	}
	
	public List<CalibrationPoint> getCalibrationPoints() {
		return mCalPoints;
	}
	
	public static class CalibrationPoint {
	
	    public double x;
	    public double y;
	    public double lat;
	    public double lon;
	    
	    public CalibrationPoint() {
	        x = 0;
	        y = 0;
	        lat = 0;
	        lon = 0;
	    }
	    
	    public CalibrationPoint(double x, double y, double lat, double lon) {
	    	this();
	        this.x = x;
	        this.y = y;
	        this.lat = lat;
	        this.lon = lon;
	    }
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
        mName = "";
        mDescription = "";
        mTileSizeX = 0;
        mTileSizeY = 0;
        mXmax = 0;
        mYmax = 0;
        mZoom = -1;
        mProjEpsg = 0;
        mCalPoints = new ArrayList<CalibrationPoint>();
	}

	@Override
	protected void readObject(int version, DataReaderBigEndian dr)
			throws IOException {
        mName = dr.readString();
        mDescription = dr.readString();
        mTileSizeX = dr.readInt();
        mTileSizeY = dr.readInt();
        mXmax = dr.readLong();
        mYmax = dr.readLong();
        mZoom = dr.readInt();
        mProjEpsg = dr.readInt();
        
        // load calibration points
        int count = dr.readInt();
        for (int i = 0; i < count; i++) {
        	addCalibrationPoint(
        			dr.readDouble(), dr.readDouble(),
        			dr.readDouble(), dr.readDouble());
        }
	}

	@Override
	protected void writeObject(DataWriterBigEndian dw) throws IOException {
    	// save parameters
		dw.writeString(mName);
		dw.writeString(mDescription);
    	dw.writeInt(mTileSizeX);
    	dw.writeInt(mTileSizeY);
    	dw.writeLong(mXmax);
        dw.writeLong(mYmax);
        dw.writeInt(mZoom);
    	dw.writeInt(mProjEpsg);
    	
        // save calibration points
		dw.writeInt(mCalPoints.size());
        for (int i = 0; i < mCalPoints.size(); i++) {
        	CalibrationPoint cal = mCalPoints.get(i);
        	dw.writeDouble(cal.x);
        	dw.writeDouble(cal.y);
        	dw.writeDouble(cal.lat);
        	dw.writeDouble(cal.lon);
        }
	}
}
