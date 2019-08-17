/*
 * Copyright 2012, Asamm Software, s. r. o.
 *
 * This file is part of LocusAPI.
 *
 * LocusAPI is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * LocusAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public
 * License along with LocusAPI. If not, see
 * <http://www.gnu.org/licenses/lgpl.html/>.
 */

package locus.api.objects.extra

import java.io.IOException
import java.util.ArrayList
import java.util.Arrays

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import locus.api.utils.Logger
import locus.api.utils.SparseArrayCompat
import locus.api.utils.Utils

class GeoDataExtra : Storable() {

    class LabelTextContainer {

        val label: String
        val text: String

        val asText: String
            get() = if (label.isNotEmpty()) {
                "$label|$text"
            } else {
                text
            }

        val formattedAsEmail: String
            get() {
                val lab = if (label.isEmpty()) text else label
                return "<a href=\"mailto:$text\">$lab</a>"
            }

        val formattedAsPhone: String
            get() {
                val lab = if (label.isEmpty()) text else label
                return "<a href=\"tel:$text\">$lab</a>"
            }

        constructor(value: String) {
            if (value.contains("|")) {
                val index = value.indexOf("|")
                this.label = value.substring(0, index)
                this.text = value.substring(index + 1)
            } else {
                this.label = ""
                this.text = value
            }
        }

        constructor(label: String?, text: String) {
            if (label == null) {
                this.label = ""
            } else {
                this.label = label
            }
            this.text = text
        }

        fun getFormattedAsUrl(checkProtocol: Boolean): String {
            val lab = if (label.isEmpty()) text else label
            var url = text
            if (checkProtocol && !url.contains("://")) {
                url = "http://$url"
            }
            return "<a href=\"$url\" target=\"_blank\">$lab</a>"
        }
    }

    /**
     * table for additional parameters
     */
    internal var parameters = SparseArrayCompat<ByteArray>()

    val count: Int
        get() = parameters.size()

    //*************************************************
    // HANDLERS PART
    //*************************************************

    /**
     * Add a single parameter to container, defined by ID and it's text representation.
     *
     * @param key   key value
     * @param value value itself
     * @return `true` if parameter was correctly added
     */
    fun addParameter(key: Int, value: String?): Boolean {
        // check on 'null' value
        var newValue: String = value
                ?: return false

        // remove previous parameter
        removeParameter(key)

        // trim new value and insert into table
        newValue = newValue.trim { it <= ' ' }
        if (newValue.isEmpty()) {
            return false
        }

        // check keys
        if (key in 1001..1999) {
            Logger.logW(TAG, "addParam(" + key + ", " + newValue + "), " +
                    "values 1000 - 1999 reserved!")
            return false
        }

        // finally insert value
        parameters.put(key, Utils.doStringToBytes(newValue))
        return true
    }

    /**
     * Add a single parameter to container, defined by ID and it's byte value.
     *
     * @param key   key value
     * @param value value itself
     * @return `true` if parameter was correctly added
     */
    fun addParameter(key: Int, value: Byte): Boolean {
        return addParameter(key, byteArrayOf(value))
    }

    /**
     * Add a single parameter to container, defined by ID and it's byte array representation.
     *
     * @param key   key value
     * @param value value itself
     * @return `true` if parameter was correctly added
     */
    fun addParameter(key: Int, value: ByteArray?): Boolean {
        // remove previous parameter
        removeParameter(key)

        // trim new value and insert into table
        if (value == null || value.isEmpty()) {
            return false
        }

        // check keys
        if (key in 1001..1999) {
            Logger.logW(TAG, "addParam(" + key + ", " + Arrays.toString(value) + "), " +
                    "values 1000 - 1999 reserved!")
            return false
        }

        // finally insert value
        parameters.put(key, value)
        return true
    }

    /**
     * Return Raw data from storage. Do not modify these data directly. For
     * fast access is provided original array, not any copy!
     *
     * @param key key ID
     * @return raw parameters data
     */
    fun getParameterRaw(key: Int): ByteArray {
        return parameters.get(key)
    }

    /**
     * Get parameter already converted to text represenation.
     *
     * @param key parameter key
     */
    fun getParameter(key: Int): String? {
        val data = parameters.get(key)
        return if (data != null) {
            Utils.doBytesToString(data)
        } else {
            null
        }
    }

    /**
     * Get parameter from private container. Result is always not-null.
     *
     * @param key parameterID to obtain value for
     * @return parameter value
     */
    fun getParameterNotNull(key: Int): String {
        val par = getParameter(key)
        return par ?: ""
    }

    fun hasParameter(key: Int): Boolean {
        return parameters.get(key) != null
    }

    fun removeParameter(key: Int): String? {
        val value = getParameter(key)
        parameters.remove(key)
        return value
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 0
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        val size = dr.readInt()
        parameters.clear()
        for (i in 0 until size) {
            val key = dr.readInt()
            parameters.put(key, dr.readBytes(dr.readInt()))
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeInt(parameters.size())
        for (i in 0 until parameters.size()) {
            val key = parameters.keyAt(i)
            dw.writeInt(key)
            val data = parameters.valueAt(i)
            dw.writeInt(data.size)
            if (data.isNotEmpty()) {
                dw.write(data)
            }
        }
    }

    //*************************************************
    // ATTACHMENTS
    //*************************************************

    /**
     * Type of attached object.
     */
    enum class AttachType constructor(
            // minimal value in storage
            internal val min: Int,
            // maximal allowed value in storage
            internal val max: Int) {

        PHONE(1000, 1099),
        EMAIL(1100, 1199),
        URL(1200, 1299),
        PHOTO(1300, 1399),
        VIDEO(1400, 1499),
        AUDIO(1500, 1599),
        OTHER(1800, 1999)
    }

    // use `getAttachments`
    val phones: List<LabelTextContainer>
        get() = getAttachments(AttachType.PHONE)
    // use `getAttachments`
    val emails: List<LabelTextContainer>
        get() = getAttachments(AttachType.EMAIL)
    // use `getAttachments`
    val urls: List<LabelTextContainer>
        get() = getAttachments(AttachType.URL)
    // use `getAttachments`
    val photos: List<String>
        get() = convertToTexts(getAttachments(AttachType.PHOTO))
    // use `getAttachments`
    val videos: List<String>
        get() = convertToTexts(getAttachments(AttachType.VIDEO))
    // use `getAttachments`
    val audios: List<String>
        get() = convertToTexts(getAttachments(AttachType.AUDIO))
    // use `getAttachments`
    val otherFiles: List<String>
        get() = convertToTexts(getAttachments(AttachType.OTHER))

    // PRIVATE AND SPECIAL TOOLS

    val allAttachments: List<String>
        get() {
            val result = ArrayList<String>()
            result.addAll(convertToTexts(getAttachments(AttachType.PHOTO)))
            result.addAll(convertToTexts(getAttachments(AttachType.AUDIO)))
            result.addAll(convertToTexts(getAttachments(AttachType.VIDEO)))
            result.addAll(convertToTexts(getAttachments(AttachType.OTHER)))
            return result
        }

    val allAttachmentsCount: Int
        get() = allAttachments.size

    /**
     * Add attachment of certain type into container.
     *
     * @param type attachment type
     * @param label (optional) item label
     * @param value item value itself
     * @return `true` if correctly added
     */
    fun addAttachment(type: AttachType, label: String?, value: String): Boolean {
        return addToStorage(label, value, type.min, type.max)
    }

    /**
     * Get all attachments of certain type.
     *
     * @param type attachment type
     * @return list of all attachments
     */
    fun getAttachments(type: AttachType): List<LabelTextContainer> {
        return getFromStorage(type.min, type.max)
    }

    /**
     * Remove attachment defined by its value, from storage.
     *
     * @param type attachment type
     * @param value value of attachment
     * @return `true` if attachment was removed
     */
    fun removeAttachment(type: AttachType, value: String): Boolean {
        return removeFromStorage(value, type.min, type.max)
    }

    /**
     * Remove all attachments of certain type.
     *
     * @param type attachment type
     */
    fun removeAllAttachments(type: AttachType) {
        removeAllFromStorage(type.min, type.max)
    }

    private fun addToStorage(label: String?, text: String, rangeFrom: Int, rangeTo: Int): Boolean {
        // check text
        if (text.isEmpty()) {
            return false
        }

        // create item
        val item = if (label?.isNotEmpty() == true) {
            "$label|$text"
        } else {
            text
        }

        // store item
        for (key in rangeFrom..rangeTo) {
            val value = getParameter(key)
            if (value == null) {
                parameters.put(key, Utils.doStringToBytes(item))
                return true
            } else if (value.equals(item, ignoreCase = true)) {
                // item already exists
                return false
            }
        }
        return false
    }

    private fun getFromStorage(rangeFrom: Int, rangeTo: Int): List<LabelTextContainer> {
        val data = ArrayList<LabelTextContainer>()
        for (key in rangeFrom..rangeTo) {
            val value = getParameter(key)
            if (value == null || value.isEmpty()) {
                continue
            }

            // extract data
            data.add(LabelTextContainer(value))
        }
        return data
    }

    private fun removeFromStorage(item: String?, rangeFrom: Int, rangeTo: Int): Boolean {
        // check text
        if (item == null || item.isEmpty()) {
            return false
        }

        for (key in rangeFrom..rangeTo) {
            val value = getParameter(key)
            if (value == null) {
                // no item
            } else if (value.endsWith(item)) {
                parameters.remove(key)
                return true
            }  // some other item already included, move to next index
        }
        return false
    }

    private fun removeAllFromStorage(rangeFrom: Int, rangeTo: Int) {
        for (i in rangeFrom..rangeTo) {
            parameters.remove(i)
        }
    }

    private fun convertToTexts(data: List<LabelTextContainer>): List<String> {
        val result = ArrayList<String>()
        for (i in 0 until data.size) {
            result.add(data[i].text)
        }
        return result
    }

    companion object {

        // tag for logger
        private const val TAG = "GeoDataExtra"

        //*************************************************
        // 'PAR_SOURCE'
        //*************************************************

        /**
         * Source unknown or undefined
         */
        const val SOURCE_UNKNOWN: Byte = 48 // "0"

        // POINTS

        /**
         * Special point for parking service
         */
        const val SOURCE_PARKING_SERVICE: Byte = 49 // "1"
        /**
         * Additional waypoint for geocache
         */
        const val SOURCE_GEOCACHING_WAYPOINT: Byte = 50 // "2"
        /**
         * Temporary point on map (not stored in database)
         */
        const val SOURCE_MAP_TEMP: Byte = 51 // "3"
        /**
         * Waypoint on route, location with some more values
         */
        const val SOURCE_ROUTE_WAYPOINT: Byte = 52 // "4"
        /**
         * Only location on route
         */
        const val SOURCE_ROUTE_LOCATION: Byte = 53 // "5"
        /**
         * Point coming from OpenStreetBugs
         */
        const val SOURCE_OPENSTREETBUGS: Byte = 55 // "7"
        /**
         * Temporary item do not display on map
         */
        const val SOURCE_INVISIBLE: Byte = 56 // "8"
        /**
         * Items automatically loaded from OSM POI database
         */
        const val SOURCE_POI_OSM_DB: Byte = 57 // "9"
        /**
         * Items loaded from Munzee service
         */
        const val SOURCE_MUNZEE: Byte = 58 // ":"
        /**
         * Items loaded from Live-tracking service
         */
        const val SOURCE_LIVE_TRACKING: Byte = 59
        /**
         * Blocked area point for navigation and routing
         */
        const val SOURCE_NAVI_BLOCKED_AREA: Byte = 60
        /**
         * Point generated from map "selection point"
         */
        const val SOURCE_MAP_SELECTION: Byte = 61

        // TRACKS/ROUTES

        /**
         * Track/Route recorded by recording service.
         */
        const val SOURCE_ROUTE_RECORD: Byte = 70
        /**
         * Route imported by "Import" function.
         */
        const val SOURCE_ROUTE_IMPORT: Byte = 71
        /**
         * Route planned by Route planner or generated by navigation system.
         */
        const val SOURCE_ROUTE_PLANED: Byte = 72

        //*************************************************
        // 'PAR_RTE_COMPUTE_TYPE'
        //*************************************************

        const val VALUE_RTE_TYPE_GENERATED = -1

        const val VALUE_RTE_TYPE_NO_TYPE = 100
        const val VALUE_RTE_TYPE_CAR = 6
        const val VALUE_RTE_TYPE_CAR_FAST = 0
        const val VALUE_RTE_TYPE_CAR_SHORT = 1
        const val VALUE_RTE_TYPE_MOTORCYCLE = 7
        const val VALUE_RTE_TYPE_CYCLE = 2
        const val VALUE_RTE_TYPE_CYCLE_FAST = 4
        const val VALUE_RTE_TYPE_CYCLE_SHORT = 5
        const val VALUE_RTE_TYPE_CYCLE_MTB = 8
        const val VALUE_RTE_TYPE_CYCLE_RACING = 9
        // basic routing profile, type "walk"
        const val VALUE_RTE_TYPE_FOOT_01 = 3
        // routing profile usually used for "hiking"
        const val VALUE_RTE_TYPE_FOOT_02 = 10
        // routing profile usually used for "climb" or "mountain hiking"
        const val VALUE_RTE_TYPE_FOOT_03 = 11

        /**
         * All possible RTE_TYPES also sorted in correct order.
         */
        val RTE_TYPES_SORTED = intArrayOf(
                VALUE_RTE_TYPE_NO_TYPE,
                VALUE_RTE_TYPE_CAR,
                VALUE_RTE_TYPE_CAR_FAST,
                VALUE_RTE_TYPE_CAR_SHORT,
                VALUE_RTE_TYPE_MOTORCYCLE,
                VALUE_RTE_TYPE_CYCLE,
                VALUE_RTE_TYPE_CYCLE_FAST,
                VALUE_RTE_TYPE_CYCLE_SHORT,
                VALUE_RTE_TYPE_CYCLE_MTB,
                VALUE_RTE_TYPE_CYCLE_RACING,
                VALUE_RTE_TYPE_FOOT_01,
                VALUE_RTE_TYPE_FOOT_02,
                VALUE_RTE_TYPE_FOOT_03)

        //*************************************************
        // PRIVATE REFERENCES (0 - 29)
        //*************************************************

        const val PAR_SOURCE = 0

        /**
         * Private parameter for handling of styles.
         * Use getter/setter directly in GeoData object
         */
        const val PAR_STYLE_NAME = 5
        /**
         * Computed are value. Used mainly for tracks
         */
        const val PAR_AREA_SIZE = 12
        /**
         * Extra data for offline POI database
         */
        const val PAR_DB_POI_EXTRA_DATA = 13
        /**
         * ID of KML trip to which item belongs
         */
        const val PAR_KML_TRIP_ID = 14
        /**
         * Reference to original Google Places item.
         */
        const val PAR_GOOGLE_PLACES_REFERENCE = 15
        /**
         * Google Places rating.
         */
        const val PAR_GOOGLE_PLACES_RATING = 16
        /**
         * Google places details.
         */
        const val PAR_GOOGLE_PLACES_DETAILS = 17

        const val PAR_INTENT_EXTRA_CALLBACK = 20
        const val PAR_INTENT_EXTRA_ON_DISPLAY = 21

        //*************************************************
        // PUBLIC VALUES (30 - 49)
        //*************************************************

        /**
         * `STRING` Item visible description.
         */
        const val PAR_DESCRIPTION = 30
        /**
         * `STRING` Storage for comments (extra tiny description available in GPX files).
         */
        const val PAR_COMMENT = 31
        /**
         * `STRING` Relative path to working dir (for images for example).
         */
        const val PAR_RELATIVE_WORKING_DIR = 32
        /**
         * `INT` Type (classification) of the item (point).
         */
        const val PAR_TYPE = 33
        /**
         * `STRING` Special code used in Geocache waypoints.
         */
        const val PAR_GEOCACHE_CODE = 34
        /**
         * `BOOLEAN` Flag to include item in POI alert feature. If missing, "true" used as default.
         */
        const val PAR_POI_ALERT_INCLUDE = 35

        // LOCATION PARAMETERS (50 - 59)

        // street name
        const val PAR_ADDRESS_STREET = 50
        // city name
        const val PAR_ADDRESS_CITY = 51
        // name of region
        const val PAR_ADDRESS_REGION = 52
        // PSČ, post code number
        const val PAR_ADDRESS_POST_CODE = 53
        // name of country
        const val PAR_ADDRESS_COUNTRY = 54

        // ROUTE PARAMETERS (100 - 199)

        // PARAMETERS FOR NAVIGATION POINTS (WAYPOINT)

        /**
         * Index to point list
         * <br></br>
         * Locus internal variable, **DO NOT SET**
         */
        const val PAR_RTE_INDEX = 100
        /**
         * Distance (in metres) from current navPoint to next
         * <br></br>
         * Locus internal variable, **DO NOT SET** (float)
         */
        const val PAR_RTE_DISTANCE_F = 101
        /**
         * time (in sec) from current navPoint to next (integer)
         */
        const val PAR_RTE_TIME_I = 102
        /**
         * speed (in m/s) from current navPoint to next (float)
         */
        const val PAR_RTE_SPEED_F = 103
        /**
         * Number of seconds to transition between successive links along
         * the route. These take into account the geometry of the intersection,
         * number of links at the intersection, and types of roads at
         * the intersection. This attempts to estimate the time in seconds it
         * would take for stops, or places where a vehicle must slow to make a turn.
         */
        const val PAR_RTE_TURN_COST = 104
        /**
         * String representation of next street label
         */
        const val PAR_RTE_STREET = 109
        /**
         * used to determine which type of action should be taken in order to stay on route
         */
        const val PAR_RTE_POINT_ACTION = 110

        // PARAMETERS FOR NAVIGATION ROUTE (TRACK)

        /**
         * type of route (car_fast, car_short, cyclo, foot)
         */
        const val PAR_RTE_COMPUTE_TYPE = 120
        /**
         * Roundabout is usually defined from two points. First on enter correctly
         * defined by ACTION 27 - 34, second on exit simply defined by exit angle.
         * In case of usage only exit point, it's need to set this flag
         */
        const val PAR_RTE_SIMPLE_ROUNDABOUTS = 121
        /**
         * Configuration of (route) plan as defined in route planner.
         */
        const val PAR_RTE_PLAN_DEFINITION = 122

        // OSM BUGS (300 - 309)
        const val PAR_OSM_NOTES_ID = 301
        const val PAR_OSM_NOTES_CLOSED = 302

        //*************************************************
        // CALL BACK HELPERS
        //*************************************************

        /**
         * Generate string, that may be used as response information for Locus.
         *
         * @param name            name at start of string
         * @param packageName     package name
         * @param className       class name
         * @param returnDataName  variable name
         * @param returnDataValue variable value
         * @return generated text
         */
        fun generateCallbackString(name: String, packageName: String,
                className: String, returnDataName: String, returnDataValue: String): String {
            // check parameters
            if (packageName.isEmpty() || className.isEmpty()) {
                Logger.logD(TAG, "generateCallbackString(" + name + ", " + packageName + ", " +
                        className + ", " + returnDataName + ", " + returnDataValue + "), " +
                        "invalid packageName or className parameter")
                return ""
            }

            // return generated result
            return name + ";" +
                    packageName + ";" +
                    className + ";" +
                    returnDataName + ";" +
                    returnDataValue
        }
    }
}
