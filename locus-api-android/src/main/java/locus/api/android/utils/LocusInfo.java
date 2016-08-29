package locus.api.android.utils;

import android.database.Cursor;
import android.database.MatrixCursor;

import java.io.IOException;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Utils;

public class LocusInfo extends Storable {
	
	// current Locus package name
	private String mPackageName;
	// flag if Locus is currently running
	private boolean mIsRunning;

	// BASIC CONSTANTS
	
	// absolute path to main Locus directory
	private String mRootDir;
    // path to backup directory
    private String mRootDirBackup;
    // path to export directory
    private String mRootDirExport;
    // path to geocaching directory
    private String mRootDirGeocaching;
    // path to map items directory
    private String mRootDirMapItems;
    // path to maps online directory
    private String mRootDirMapsOnline;
    // path to maps personal directory
    private String mRootDirMapsPersonal;
    // path to maps vector directory
    private String mRootDirMapsVector;
    // path to srtm directory
    private String mRootDirSrtm;

	// flag if periodic updates are enabled
	private boolean mPeriodicUpdatesEnabled;
	
	// GEOCACHING DATA
	
	// name of owner (usually useful to recognize own caches)
	private String mGcOwnerName;
	
	// UNITS
	
	// currently selected units for 'Altitude'
	private int mUnitsFormatAltitude;
	// currently selected units for 'Angles'
	private int mUnitsFormatAngle;
	// currently selected units for 'Areas'
	private int mUnitsFormatArea;
	// currently selected units for 'Length'
	private int mUnitsFormatLength;
	// currently selected units for 'Speed'
	private int mUnitsFormatSpeed;
	// currently selected units for 'Temperature'
	private int mUnitsFormatTemperature;

    /**
     * Basic empty constructor.
     */
	public LocusInfo() {
		super();
	}

    // PACKAGE NAME

    /**
     * Get defined package name of Locus application.
     * @return package name
     */
	public String getPackageName() {
		return mPackageName;
	}

    /**
     * Define package name.
     * @param packageName package name
     */
	protected void setPackageName(String packageName) {
		if (packageName == null) {
			packageName = "";
		}
		this.mPackageName = packageName;
	}

    // RUNNING STATE

    /**
     * Flag if Locus is currently running.
     * @return <code>true</code> if Locus is running
     */
	public boolean isRunning() {
		return mIsRunning;
	}

    /**
     * Set flag if Locus is running or not.
     * @param isRunning <code>true</code> if running
     */
	protected void setRunning(boolean isRunning) {
		this.mIsRunning = isRunning;
	}

    // ROOT DIRECTORY

	/**
	 * Get ROOT directory of requested Locus application. Is required to check
	 * result, because in case, Locus was not at least once properly initialized,
	 * this function may return 'null' as a result.
	 * @return ROOT directory, or 'null' if not yet correctly set
	 */
	public String getRootDirectory() {
		return mRootDir;
	}

	/**
	 * Set current Locus directory.
	 * @param rootDirectory current Locus root directory.
	 */
	protected void setRootDirectory(String rootDirectory) {
		if (rootDirectory == null) {
			rootDirectory = "";
		}
		this.mRootDir = rootDirectory;
	}

    // BACKUP DIRECTORY

    /**
     * Get current defined directory for "backup".
     * @return current defined directory
     */
    public String getRootDirBackup() {
        return mRootDirBackup;
    }

    /**
     * Set current defined directory for "backup".
     * @param dir current directory
     */
    protected void setRootDirBackup(String dir) {
        if (dir == null) {
            dir = "";
        }
        this.mRootDirBackup = dir;
    }

    // EXPORT DIRECTORY

    /**
     * Get current defined directory for "export".
     * @return current defined directory
     */
    public String getRootDirExport() {
        return mRootDirExport;
    }

    /**
     * Set current defined directory for "export".
     * @param dir current directory
     */
    protected void setRootDirExport(String dir) {
        if (dir == null) {
            dir = "";
        }
        this.mRootDirExport = dir;
    }

    // GEOCACHING DIRECTORY

    /**
     * Get current defined directory for "data/geocaching".
     * @return current defined directory
     */
    public String getRootDirGeocaching() {
        return mRootDirGeocaching;
    }

    /**
     * Set current defined directory for "data/geocaching".
     * @param dir current directory
     */
    protected void setRootDirGeocaching(String dir) {
        if (dir == null) {
            dir = "";
        }
        this.mRootDirGeocaching = dir;
    }

    // MAP ITEMS DIRECTORY

    /**
     * Get current defined directory for "mapItems".
     * @return current defined directory
     */
    public String getRootDirMapItems() {
        return mRootDirMapItems;
    }

    /**
     * Set current defined directory for "mapItems".
     * @param dir current directory
     */
    protected void setRootDirMapItems(String dir) {
        if (dir == null) {
            dir = "";
        }
        this.mRootDirMapItems = dir;
    }

    // MAPS ONLINE DIRECTORY

    /**
     * Get current defined directory for "mapsOnline".
     * @return current defined directory
     */
    public String getRootDirMapsOnline() {
        return mRootDirMapsOnline;
    }

    /**
     * Set current defined directory for "mapsOnline".
     * @param dir current directory
     */
    protected void setRootDirMapsOnline(String dir) {
        if (dir == null) {
            dir = "";
        }
        this.mRootDirMapsOnline = dir;
    }

    // MAPS PERSONAL DIRECTORY

    /**
     * Get current defined directory for "maps".
     * @return current defined directory
     */
    public String getRootDirMapsPersonal() {
        return mRootDirMapsPersonal;
    }

    /**
     * Set current defined directory for "maps".
     * @param dir current directory
     */
    protected void setRootDirMapsPersonal(String dir) {
        if (dir == null) {
            dir = "";
        }
        this.mRootDirMapsPersonal = dir;
    }

    // MAPS VECTOR DIRECTORY

    /**
     * Get current defined directory for "mapsVector".
     * @return current defined directory
     */
    public String getRootDirMapsVector() {
        return mRootDirMapsVector;
    }

    /**
     * Set current defined directory for "mapsVector".
     * @param dir current directory
     */
    protected void setRootDirMapsVector(String dir) {
        if (dir == null) {
            dir = "";
        }
        this.mRootDirMapsVector = dir;
    }

    // SRTM DIRECTORY

    /**
     * Get current defined directory for "data/srtm".
     * @return current defined directory
     */
    public String getRootDirSrtm() {
        return mRootDirSrtm;
    }

    /**
     * Set current defined directory for "data/srtm".
     * @param dir current directory
     */
    protected void setRootDirSrtm(String dir) {
        if (dir == null) {
            dir = "";
        }
        this.mRootDirSrtm = dir;
    }

    // PERIODIC UPDATES

    public boolean isPeriodicUpdatesEnabled() {
		return mPeriodicUpdatesEnabled;
	}

	public void setPeriodicUpdatesEnabled(boolean periodicUpdatesEnabled) {
		this.mPeriodicUpdatesEnabled = periodicUpdatesEnabled;
	}

    // GEOCACHING OWNER NAME

	public String getGcOwnerName() {
		return mGcOwnerName;
	}

	public void setGcOwnerName(String gcOwnerName) {
		if (gcOwnerName == null) {
			gcOwnerName = "";
		}
		this.mGcOwnerName = gcOwnerName;
	}

	public int getUnitsFormatAltitude() {
		return mUnitsFormatAltitude;
	}

	public void setUnitsFormatAltitude(int unitsFormatAltitude) {
		this.mUnitsFormatAltitude = unitsFormatAltitude;
	}

	public int getUnitsFormatAngle() {
		return mUnitsFormatAngle;
	}

	public void setUnitsFormatAngle(int unitsFormatAngle) {
		this.mUnitsFormatAngle = unitsFormatAngle;
	}

	public int getUnitsFormatArea() {
		return mUnitsFormatArea;
	}

	public void setUnitsFormatArea(int unitsFormatArea) {
		this.mUnitsFormatArea = unitsFormatArea;
	}

	public int getUnitsFormatLength() {
		return mUnitsFormatLength;
	}

	public void setUnitsFormatLength(int unitsFormatLength) {
		this.mUnitsFormatLength = unitsFormatLength;
	}

	public int getUnitsFormatSpeed() {
		return mUnitsFormatSpeed;
	}

	public void setUnitsFormatSpeed(int unitsFormatSpeed) {
		this.mUnitsFormatSpeed = unitsFormatSpeed;
	}

	public int getUnitsFormatTemperature() {
		return mUnitsFormatTemperature;
	}

	public void setUnitsFormatTemperature(int unitsFormatTemperature) {
		this.mUnitsFormatTemperature = unitsFormatTemperature;
	}

	@Override
	public String toString() {
		return Utils.toString(this);
	}
	
	/**************************************************/
	// HANDLING WITH CURSOR
	/**************************************************/
	
	private static final String VALUE_PACKAGE_NAME =
			"packageName";
	private static final String VALUE_IS_RUNNING =
			"isRunning";

	// BASIC CONSTANTS

    private static final String VALUE_ROOT_DIR =
			"rootDir";
    private static final String VALUE_ROOT_DIR_BACKUP =
            "rootDirBackup";
    private static final String VALUE_ROOT_DIR_EXPORT =
            "rootDirExport";
    private static final String VALUE_ROOT_DIR_GEOCACHING =
            "rootDirGeocaching";
    private static final String VALUE_ROOT_DIR_MAP_ITEMS =
            "rootDirMapItems";
    private static final String VALUE_ROOT_DIR_MAPS_ONLINE =
            "rootDirMapsOnline";
    private static final String VALUE_ROOT_DIR_MAPS_PERSONAL =
            "rootDirMapsPersonal";
    private static final String VALUE_ROOT_DIR_MAPS_VECTOR =
            "rootDirMapsVector";
    private static final String VALUE_ROOT_DIR_SRTM =
            "rootDirSrtm";
	private static final String VALUE_PERIODIC_UPDATES =
			"periodicUpdates";

	// GEOCACHING DATA
	
	private static final String VALUE_GEOCACHING_OWNER_NAME =
			"gcOwnerName";
	
	// UNITS
	
	private static final String VALUE_UNITS_FORMAT_ALTITUDE =
			"unitsFormatAltitude";
	private static final String VALUE_UNITS_FORMAT_ANGLE = 
			"unitsFormatAngle";
	private static final String VALUE_UNITS_FORMAT_AREA =
			"unitsFormatArea";
	private static final String VALUE_UNITS_FORMAT_LENGTH = 
			"unitsFormatLength";
	private static final String VALUE_UNITS_FORMAT_SPEED = 
			"unitsFormatSpeed";
	private static final String VALUE_UNITS_FORMAT_TEMPERATURE =
			"unitsFormatTemperature";
	
	public static LocusInfo create(Cursor cursor) {
		// check cursor
		if (cursor == null || cursor.getCount() == 0) {
			return null;
		}
		
		// iterate over all items
		LocusInfo info = new LocusInfo();
		for (int i = 0; i < cursor.getCount(); i++)  {
			cursor.moveToPosition(i);
			String key = cursor.getString(0);
            switch (key) {
                case VALUE_PACKAGE_NAME:
                    info.mPackageName = cursor.getString(1);
                    break;
                case VALUE_IS_RUNNING:
                    info.mIsRunning = cursor.getInt(1) == 1;
                    break;
                case VALUE_ROOT_DIR:
                    info.mRootDir = cursor.getString(1);
                    break;
                case VALUE_ROOT_DIR_BACKUP:
                    info.mRootDirBackup = cursor.getString(1);
                    break;
                case VALUE_ROOT_DIR_EXPORT:
                    info.mRootDirExport = cursor.getString(1);
                    break;
                case VALUE_ROOT_DIR_GEOCACHING:
                    info.mRootDirGeocaching = cursor.getString(1);
                    break;
                case VALUE_ROOT_DIR_MAP_ITEMS:
                    info.mRootDirMapItems = cursor.getString(1);
                    break;
                case VALUE_ROOT_DIR_MAPS_ONLINE:
                    info.mRootDirMapsOnline = cursor.getString(1);
                    break;
                case VALUE_ROOT_DIR_MAPS_PERSONAL:
                    info.mRootDirMapsPersonal = cursor.getString(1);
                    break;
                case VALUE_ROOT_DIR_MAPS_VECTOR:
                    info.mRootDirMapsVector = cursor.getString(1);
                    break;
                case VALUE_ROOT_DIR_SRTM:
                    info.mRootDirSrtm = cursor.getString(1);
                    break;
                case VALUE_PERIODIC_UPDATES:
                    info.mPeriodicUpdatesEnabled = cursor.getInt(1) == 1;
                    break;
                case VALUE_GEOCACHING_OWNER_NAME:
                    info.mGcOwnerName = cursor.getString(1);
                    break;
                case VALUE_UNITS_FORMAT_ALTITUDE:
                    info.mUnitsFormatAltitude = cursor.getInt(1);
                    break;
                case VALUE_UNITS_FORMAT_ANGLE:
                    info.mUnitsFormatAngle = cursor.getInt(1);
                    break;
                case VALUE_UNITS_FORMAT_AREA:
                    info.mUnitsFormatArea = cursor.getInt(1);
                    break;
                case VALUE_UNITS_FORMAT_LENGTH:
                    info.mUnitsFormatLength = cursor.getInt(1);
                    break;
                case VALUE_UNITS_FORMAT_SPEED:
                    info.mUnitsFormatSpeed = cursor.getInt(1);
                    break;
                case VALUE_UNITS_FORMAT_TEMPERATURE:
                    info.mUnitsFormatTemperature = cursor.getInt(1);
                    break;
            }
		}
		return info;
	}

    /**
     * Generate cursor with filled parameters.
     * @return generated cursor
     */
	protected Cursor create() {
		// add ROOT directory
		MatrixCursor c = new MatrixCursor(
				new String[] {"key", "value"});
		
		// core
		c.addRow(new Object[] {VALUE_PACKAGE_NAME, 
				mPackageName});
		c.addRow(new Object[] {VALUE_IS_RUNNING, 
				mIsRunning ? "1" : "0"});

		// BASIC CONSTANTS
		
		c.addRow(new Object[] {
                VALUE_ROOT_DIR, mRootDir});
        c.addRow(new Object[] {
                VALUE_ROOT_DIR_BACKUP, mRootDirBackup});
        c.addRow(new Object[] {
                VALUE_ROOT_DIR_EXPORT, mRootDirExport});
        c.addRow(new Object[] {
                VALUE_ROOT_DIR_GEOCACHING, mRootDirGeocaching});
        c.addRow(new Object[] {
                VALUE_ROOT_DIR_MAP_ITEMS, mRootDirMapItems});
        c.addRow(new Object[] {
                VALUE_ROOT_DIR_MAPS_ONLINE, mRootDirMapsOnline});
        c.addRow(new Object[] {
                VALUE_ROOT_DIR_MAPS_PERSONAL, mRootDirMapsPersonal});
        c.addRow(new Object[] {
                VALUE_ROOT_DIR_MAPS_VECTOR, mRootDirMapsVector});
        c.addRow(new Object[] {
                VALUE_ROOT_DIR_SRTM, mRootDirSrtm});

        c.addRow(new Object[] {VALUE_PERIODIC_UPDATES,
				mPeriodicUpdatesEnabled ? "1" : "0"});
		
		// GEOCACHING DATA
		
		c.addRow(new Object[] {VALUE_GEOCACHING_OWNER_NAME, 
				mGcOwnerName});
		
		// UNITS
		
		c.addRow(new Object[] {VALUE_UNITS_FORMAT_ALTITUDE, 
				mUnitsFormatAltitude});
		c.addRow(new Object[] {VALUE_UNITS_FORMAT_ANGLE, 
				mUnitsFormatAngle});
		c.addRow(new Object[] {VALUE_UNITS_FORMAT_AREA, 
				mUnitsFormatArea});
		c.addRow(new Object[] {VALUE_UNITS_FORMAT_LENGTH, 
				mUnitsFormatLength});
		c.addRow(new Object[] {VALUE_UNITS_FORMAT_SPEED, 
				mUnitsFormatSpeed});
		c.addRow(new Object[] {VALUE_UNITS_FORMAT_TEMPERATURE, 
				mUnitsFormatTemperature});
		
		// return cursor
		return c;
	}

    /**************************************************/
    // STORABLE PART
    /**************************************************/

    @Override
    protected int getVersion() {
        return 0;
    }

    @Override
    public void reset() {
        mPackageName = "";
        mIsRunning = false;

        // BASIC CONSTANTS

        mRootDir = "";
        mRootDirBackup = "";
        mRootDirExport = "";
        mRootDirGeocaching = "";
        mRootDirMapItems = "";
        mRootDirMapsOnline = "";
        mRootDirMapsPersonal = "";
        mRootDirMapsVector = "";
        mRootDirSrtm = "";

        mPeriodicUpdatesEnabled = false;

        // GEOCACHING DATA

        mGcOwnerName = "";

        // UNITS

        mUnitsFormatAltitude = -1;
        mUnitsFormatAngle = -1;
        mUnitsFormatArea = -1;
        mUnitsFormatLength = -1;
        mUnitsFormatSpeed = -1;
        mUnitsFormatTemperature = -1;
    }

    @Override
    protected void readObject(int version, DataReaderBigEndian dr) throws IOException {
        mPackageName = dr.readString();
        mIsRunning = dr.readBoolean();

        // BASIC CONSTANTS

        mRootDir = dr.readString();
        mRootDirBackup = dr.readString();
        mRootDirExport = dr.readString();
        mRootDirGeocaching = dr.readString();
        mRootDirMapItems = dr.readString();
        mRootDirMapsOnline = dr.readString();
        mRootDirMapsPersonal = dr.readString();
        mRootDirMapsVector = dr.readString();
        mRootDirSrtm = dr.readString();

        mPeriodicUpdatesEnabled = dr.readBoolean();

        // GEOCACHING DATA

        mGcOwnerName = dr.readString();

        // UNITS

        mUnitsFormatAltitude = dr.readInt();
        mUnitsFormatAngle = dr.readInt();
        mUnitsFormatArea = dr.readInt();
        mUnitsFormatLength = dr.readInt();
        mUnitsFormatSpeed = dr.readInt();
        mUnitsFormatTemperature = dr.readInt();
    }

    @Override
    protected void writeObject(DataWriterBigEndian dw) throws IOException {
        dw.writeString(mPackageName);
        dw.writeBoolean(mIsRunning);

        // BASIC CONSTANTS

        dw.writeString(mRootDir);
        dw.writeString(mRootDirBackup);
        dw.writeString(mRootDirExport);
        dw.writeString(mRootDirGeocaching);
        dw.writeString(mRootDirMapItems);
        dw.writeString(mRootDirMapsOnline);
        dw.writeString(mRootDirMapsPersonal);
        dw.writeString(mRootDirMapsVector);
        dw.writeString(mRootDirSrtm);

        dw.writeBoolean(mPeriodicUpdatesEnabled);

        // GEOCACHING DATA

        dw.writeString(mGcOwnerName);

        // UNITS

        dw.writeInt(mUnitsFormatAltitude);
        dw.writeInt(mUnitsFormatAngle);
        dw.writeInt(mUnitsFormatArea);
        dw.writeInt(mUnitsFormatLength);
        dw.writeInt(mUnitsFormatSpeed);
        dw.writeInt(mUnitsFormatTemperature);
    }
}
