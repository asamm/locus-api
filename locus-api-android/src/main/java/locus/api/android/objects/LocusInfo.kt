package locus.api.android.objects

import android.database.Cursor
import android.database.MatrixCursor

import java.io.IOException

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

class LocusInfo : Storable() {

    /**
     * Package name of Locus application.
     */
    var packageName: String = ""
    /**
     * Flag if Locus is currently running.
     */
    var isRunning: Boolean = false
    /**
     * Time of last activity (in ms).
     */
    var lastActive: Long = 0L
    /**
     * ROOT directory of requested Locus application. Is required to check
     * result, because in case, Locus was not at least once properly initialized,
     * this function may return 'null' as a result.
     */
    var rootDir: String = ""
    /**
     * Current defined directory for "backup".
     */
    var rootDirBackup: String = ""
    /**
     * Current defined directory for "export".
     */
    var rootDirExport: String = ""
    /**
     * Current defined directory for "data/geocaching".
     */
    var rootDirGeocaching: String = ""
    /**
     * Current defined directory for "mapItems".
     */
    var rootDirMapItems: String = ""
    /**
     * Current defined directory for "mapsOnline".
     */
    var rootDirMapsOnline: String = ""
    /**
     * Current defined directory for "maps".
     */
    var rootDirMapsPersonal: String = ""
    /**
     * Current defined directory for "mapsVector".
     */
    var rootDirMapsVector: String = ""
    /**
     * Current defined directory for "data/srtm".
     */
    var rootDirSrtm: String = ""

    // PERIODIC UPDATES

    /**
     * Information if periodic updates feature is enabled.
     */
    var isPeriodicUpdatesEnabled: Boolean = false

    // GEOCACHING DATA

    /**
     * Name of owner (usually useful to recognize own caches).
     */
    var gcOwnerName: String = ""

    // UNITS

    /**
     * Current users format for "Altitude" values.
     */
    var unitsFormatAltitude: Int = -1
    /**
     * Current users format for "Angle" values.
     *
     */
    var unitsFormatAngle: Int = -1
    /**
     * Current users format for "Area" values.
     */
    var unitsFormatArea: Int = -1
    /**
     * Current users format for "Energy" values.
     */
    var unitsFormatEnergy: Int = -1
    /**
     * Current users format for "Length" values.
     */
    var unitsFormatLength: Int = -1
    /**
     * Current users format for "Slope" values.
     */
    var unitsFormatSlope: Int = -1
    /**
     * Current users format for "Speed" values.
     */
    var unitsFormatSpeed: Int = -1
    /**
     * Current users format for "Temperature" values.
     */
    var unitsFormatTemperature: Int = -1
    /**
     * Current users format for "Weight" values.
     */
    var unitsFormatWeight: Int = -1

    /**
     * Generate cursor with filled parameters.
     *
     * @return generated cursor
     */
    protected fun create(): Cursor {
        // add ROOT directory
        val c = MatrixCursor(
                arrayOf("key", "value"))

        // core
        c.addRow(arrayOf<Any>(VALUE_PACKAGE_NAME, packageName))
        c.addRow(arrayOf<Any>(VALUE_IS_RUNNING, if (isRunning) "1" else "0"))
        c.addRow(arrayOf(VALUE_LAST_ACTIVE, lastActive))

        // BASIC CONSTANTS

        c.addRow(arrayOf<Any>(VALUE_ROOT_DIR, rootDir))
        c.addRow(arrayOf<Any>(VALUE_ROOT_DIR_BACKUP, rootDirBackup))
        c.addRow(arrayOf<Any>(VALUE_ROOT_DIR_EXPORT, rootDirExport))
        c.addRow(arrayOf<Any>(VALUE_ROOT_DIR_GEOCACHING, rootDirGeocaching))
        c.addRow(arrayOf<Any>(VALUE_ROOT_DIR_MAP_ITEMS, rootDirMapItems))
        c.addRow(arrayOf<Any>(VALUE_ROOT_DIR_MAPS_ONLINE, rootDirMapsOnline))
        c.addRow(arrayOf<Any>(VALUE_ROOT_DIR_MAPS_PERSONAL, rootDirMapsPersonal))
        c.addRow(arrayOf<Any>(VALUE_ROOT_DIR_MAPS_VECTOR, rootDirMapsVector))
        c.addRow(arrayOf<Any>(VALUE_ROOT_DIR_SRTM, rootDirSrtm))

        c.addRow(arrayOf<Any>(VALUE_PERIODIC_UPDATES, if (isPeriodicUpdatesEnabled) "1" else "0"))

        // GEOCACHING DATA

        c.addRow(arrayOf<Any>(VALUE_GEOCACHING_OWNER_NAME, gcOwnerName))

        // UNITS

        c.addRow(arrayOf(VALUE_UNITS_FORMAT_ALTITUDE, unitsFormatAltitude))
        c.addRow(arrayOf(VALUE_UNITS_FORMAT_ANGLE, unitsFormatAngle))
        c.addRow(arrayOf(VALUE_UNITS_FORMAT_AREA, unitsFormatArea))
        c.addRow(arrayOf(VALUE_UNITS_FORMAT_ENERGY, unitsFormatEnergy))
        c.addRow(arrayOf(VALUE_UNITS_FORMAT_LENGTH, unitsFormatLength))
        c.addRow(arrayOf(VALUE_UNITS_FORMAT_SLOPE, unitsFormatSlope))
        c.addRow(arrayOf(VALUE_UNITS_FORMAT_SPEED, unitsFormatSpeed))
        c.addRow(arrayOf(VALUE_UNITS_FORMAT_TEMPERATURE, unitsFormatTemperature))
        c.addRow(arrayOf(VALUE_UNITS_FORMAT_WEIGHT, unitsFormatWeight))

        // return cursor
        return c
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 2
    }

    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        packageName = dr.readString()
        isRunning = dr.readBoolean()

        // BASIC CONSTANTS

        rootDir = dr.readString()
        rootDirBackup = dr.readString()
        rootDirExport = dr.readString()
        rootDirGeocaching = dr.readString()
        rootDirMapItems = dr.readString()
        rootDirMapsOnline = dr.readString()
        rootDirMapsPersonal = dr.readString()
        rootDirMapsVector = dr.readString()
        rootDirSrtm = dr.readString()

        isPeriodicUpdatesEnabled = dr.readBoolean()

        // GEOCACHING DATA

        gcOwnerName = dr.readString()

        // UNITS

        unitsFormatAltitude = dr.readInt()
        unitsFormatAngle = dr.readInt()
        unitsFormatArea = dr.readInt()
        unitsFormatLength = dr.readInt()
        unitsFormatSpeed = dr.readInt()
        unitsFormatTemperature = dr.readInt()

        // V1
        if (version >= 1) {
            unitsFormatEnergy = dr.readInt()
            unitsFormatSlope = dr.readInt()
            unitsFormatWeight = dr.readInt()
        }

        // V2
        if (version >= 2) {
            lastActive = dr.readLong()
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeString(packageName)
        dw.writeBoolean(isRunning)

        // BASIC CONSTANTS

        dw.writeString(rootDir)
        dw.writeString(rootDirBackup)
        dw.writeString(rootDirExport)
        dw.writeString(rootDirGeocaching)
        dw.writeString(rootDirMapItems)
        dw.writeString(rootDirMapsOnline)
        dw.writeString(rootDirMapsPersonal)
        dw.writeString(rootDirMapsVector)
        dw.writeString(rootDirSrtm)

        dw.writeBoolean(isPeriodicUpdatesEnabled)

        // GEOCACHING DATA

        dw.writeString(gcOwnerName)

        // UNITS

        dw.writeInt(unitsFormatAltitude)
        dw.writeInt(unitsFormatAngle)
        dw.writeInt(unitsFormatArea)
        dw.writeInt(unitsFormatLength)
        dw.writeInt(unitsFormatSpeed)
        dw.writeInt(unitsFormatTemperature)

        // V1
        dw.writeInt(unitsFormatEnergy)
        dw.writeInt(unitsFormatSlope)
        dw.writeInt(unitsFormatWeight)

        // V2
        dw.writeLong(lastActive)
    }

    companion object {

        //*************************************************
        // HANDLING WITH CURSOR
        //*************************************************

        private const val VALUE_PACKAGE_NAME = "packageName"
        private const val VALUE_IS_RUNNING = "isRunning"
        private const val VALUE_LAST_ACTIVE = "lastActive"

        // BASIC CONSTANTS

        private const val VALUE_ROOT_DIR = "rootDir"
        private const val VALUE_ROOT_DIR_BACKUP = "rootDirBackup"
        private const val VALUE_ROOT_DIR_EXPORT = "rootDirExport"
        private const val VALUE_ROOT_DIR_GEOCACHING = "rootDirGeocaching"
        private const val VALUE_ROOT_DIR_MAP_ITEMS = "rootDirMapItems"
        private const val VALUE_ROOT_DIR_MAPS_ONLINE = "rootDirMapsOnline"
        private const val VALUE_ROOT_DIR_MAPS_PERSONAL = "rootDirMapsPersonal"
        private const val VALUE_ROOT_DIR_MAPS_VECTOR = "rootDirMapsVector"
        private const val VALUE_ROOT_DIR_SRTM = "rootDirSrtm"
        private const val VALUE_PERIODIC_UPDATES = "periodicUpdates"

        // GEOCACHING DATA

        private const val VALUE_GEOCACHING_OWNER_NAME = "gcOwnerName"

        // UNITS

        private const val VALUE_UNITS_FORMAT_ALTITUDE = "unitsFormatAltitude"
        private const val VALUE_UNITS_FORMAT_ANGLE = "unitsFormatAngle"
        private const val VALUE_UNITS_FORMAT_AREA = "unitsFormatArea"
        private const val VALUE_UNITS_FORMAT_ENERGY = "unitsFormatEnergy"
        private const val VALUE_UNITS_FORMAT_LENGTH = "unitsFormatLength"
        private const val VALUE_UNITS_FORMAT_SLOPE = "unitsFormatSlope"
        private const val VALUE_UNITS_FORMAT_SPEED = "unitsFormatSpeed"
        private const val VALUE_UNITS_FORMAT_TEMPERATURE = "unitsFormatTemperature"
        private const val VALUE_UNITS_FORMAT_WEIGHT = "unitsFormatWeight"

        /**
         * Create LocusInfo from received cursor.
         *
         * @param cursor received cursor
         * @return LocusInfo object
         */
        fun create(cursor: Cursor?): LocusInfo? {
            // check cursor
            if (cursor == null || cursor.count == 0) {
                return null
            }

            // iterate over all items
            val info = LocusInfo()
            for (i in 0 until cursor.count) {
                cursor.moveToPosition(i)
                when (cursor.getString(0)) {
                    VALUE_PACKAGE_NAME -> info.packageName = cursor.getString(1)
                    VALUE_IS_RUNNING -> info.isRunning = cursor.getInt(1) == 1
                    VALUE_LAST_ACTIVE -> info.lastActive = cursor.getLong(1)
                    VALUE_ROOT_DIR -> info.rootDir = cursor.getString(1)
                    VALUE_ROOT_DIR_BACKUP -> info.rootDirBackup = cursor.getString(1)
                    VALUE_ROOT_DIR_EXPORT -> info.rootDirExport = cursor.getString(1)
                    VALUE_ROOT_DIR_GEOCACHING -> info.rootDirGeocaching = cursor.getString(1)
                    VALUE_ROOT_DIR_MAP_ITEMS -> info.rootDirMapItems = cursor.getString(1)
                    VALUE_ROOT_DIR_MAPS_ONLINE -> info.rootDirMapsOnline = cursor.getString(1)
                    VALUE_ROOT_DIR_MAPS_PERSONAL -> info.rootDirMapsPersonal = cursor.getString(1)
                    VALUE_ROOT_DIR_MAPS_VECTOR -> info.rootDirMapsVector = cursor.getString(1)
                    VALUE_ROOT_DIR_SRTM -> info.rootDirSrtm = cursor.getString(1)
                    VALUE_PERIODIC_UPDATES -> info.isPeriodicUpdatesEnabled = cursor.getInt(1) == 1
                    VALUE_GEOCACHING_OWNER_NAME -> info.gcOwnerName = cursor.getString(1)
                    VALUE_UNITS_FORMAT_ALTITUDE -> info.unitsFormatAltitude = cursor.getInt(1)
                    VALUE_UNITS_FORMAT_ANGLE -> info.unitsFormatAngle = cursor.getInt(1)
                    VALUE_UNITS_FORMAT_AREA -> info.unitsFormatArea = cursor.getInt(1)
                    VALUE_UNITS_FORMAT_ENERGY -> info.unitsFormatEnergy = cursor.getInt(1)
                    VALUE_UNITS_FORMAT_LENGTH -> info.unitsFormatLength = cursor.getInt(1)
                    VALUE_UNITS_FORMAT_SLOPE -> info.unitsFormatSlope = cursor.getInt(1)
                    VALUE_UNITS_FORMAT_SPEED -> info.unitsFormatSpeed = cursor.getInt(1)
                    VALUE_UNITS_FORMAT_TEMPERATURE -> info.unitsFormatTemperature = cursor.getInt(1)
                    VALUE_UNITS_FORMAT_WEIGHT -> info.unitsFormatWeight = cursor.getInt(1)
                }
            }
            return info
        }
    }
}
