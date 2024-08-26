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

import com.asamm.loggerV2.logD
import com.asamm.loggerV2.logW
import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import locus.api.utils.SparseArrayCompat
import locus.api.utils.Utils
import java.io.IOException
import java.util.*

/**
 * Container with meta-data for GeoData objects.
 */
class GeoDataExtra : Storable() {

    /**
     * Container for text links and attachments.
     */
    class LabelTextContainer {

        /**
         * Visible text label.
         */
        val label: String

        /**
         * Text/value itself.
         */
        val text: String

        val asText: String
            get() = if (label.isNotEmpty()) {
                "$label|$text"
            } else {
                text
            }

        val formattedAsEmail: String
            get() {
                val lab = label.ifEmpty { text }
                return "<a href=\"mailto:$text\">$lab</a>"
            }

        val formattedAsPhone: String
            get() {
                val lab = label.ifEmpty { text }
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
            val lab = label.ifEmpty { text }
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
            logW(tag = TAG) {
                "addParam(" + key + ", " + newValue + "), " +
                        "values 1000 - 1999 reserved!"
            }
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
            logW(tag = TAG) {
                "addParam(" + key + ", " + Arrays.toString(value) + "), " +
                        "values 1000 - 1999 reserved!"
            }
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
    fun getParameterRaw(key: Int): ByteArray? {
        return parameters.get(key)
    }

    /**
     * Get parameter already converted to text representation.
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
        return getParameter(key) ?: ""
    }

    /**
     * Check if certain parameter exists in container.
     *
     * @param key parameter key
     */
    fun hasParameter(key: Int): Boolean {
        return getParameterRaw(key) != null
    }

    /**
     * Remove certain parameter from the container.
     *
     * @param key parameter key
     * @return parameter value or `null` if does not exists
     */
    fun removeParameter(key: Int): String? {
        val value = getParameter(key)
        parameters.remove(key)
        return value
    }

    /**
     * Iterate over all parameters and test if any match condition defined by [contains] method.
     */
    fun searchInParameters(contains: (String) -> Boolean): Boolean {
        // iterate over all data
        for (i in 0 until parameters.size()) {
            val value = parameters.valueAt(i)
            if (value == null || value.isEmpty()) {
                continue
            }

            // perform test
            if (contains(Utils.doBytesToString(value))) {
                return true
            }
        }
        return false
    }

    /**
     * Copy data from other GeoDataExtra container to current object.
     *
     * @param dataNew    new data
     * @param ignoreList list of ID's we wants to ignore during copy task
     */
    fun copyFrom(dataNew: GeoDataExtra, ignoreList: IntArray?) {
        // iterate over all data
        for (i in 0 until dataNew.parameters.size()) {
            val key = dataNew.parameters.keyAt(i)
            var ignore = false
            if (ignoreList != null) {
                for (anIgnoreList in ignoreList) {
                    if (anIgnoreList == key) {
                        ignore = true
                        break
                    }
                }
            }

            // skip ignored or special
            if (ignore || key in 1000..1999) {
                continue
            }

            val value = dataNew.parameters.valueAt(i)
            parameters.put(key, value)
        }

        // add special containers
        for (phone in dataNew.phones) {
            addAttachment(AttachType.PHONE, phone.label, phone.text)
        }
        for (email in dataNew.emails) {
            addAttachment(AttachType.EMAIL, email.label, email.text)
        }
        for (url in dataNew.urls) {
            addAttachment(AttachType.URL, url.label, url.text)
        }
        for (photo in dataNew.photos) {
            addAttachment(AttachType.PHOTO, value = photo)
        }
        for (video in dataNew.videos) {
            addAttachment(AttachType.VIDEO, value = video)
        }
        for (audio in dataNew.audios) {
            addAttachment(AttachType.AUDIO, value = audio)
        }
        for (file in dataNew.otherFiles) {
            addAttachment(AttachType.OTHER, value = file)
        }
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
        internal val max: Int
    ) {

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
    fun addAttachment(type: AttachType, label: String? = null, value: String): Boolean {
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
            "${label.replace("|", "/")}|$text"
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
            @Suppress("ControlFlowWithEmptyBody")
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
        for (element in data) {
            result.add(element.text)
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

        /**
         * Route warning.
         */
        const val SOURCE_ROUTE_WARNING: Byte = 62

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

        // WALK

        // basic routing profile, type "walk"
        const val VALUE_RTE_TYPE_FOOT_01 = 3
        // routing profile usually used for "hiking"
        const val VALUE_RTE_TYPE_FOOT_02 = 10
        // routing profile usually used for "climb" or "mountain hiking"
        const val VALUE_RTE_TYPE_FOOT_03 = 11

        // CYCLE

        // generic "cycle" profile
        const val VALUE_RTE_TYPE_CYCLE = 2
        @Deprecated (
            message = "Use 'VALUE_RTE_TYPE_CYCLE_ROAD' instead. Validate usage because replacement has different ID!"
        )
        const val VALUE_RTE_TYPE_CYCLE_FAST = 4
        @Deprecated (
            message = "Use 'VALUE_RTE_TYPE_CYCLE_ROAD' instead",
            replaceWith = ReplaceWith("VALUE_RTE_TYPE_CYCLE_ROAD")
        )
        const val VALUE_RTE_TYPE_CYCLE_RACING = 9
        const val VALUE_RTE_TYPE_CYCLE_ROAD = 9
        const val VALUE_RTE_TYPE_CYCLE_GRAVEL = 5
        const val VALUE_RTE_TYPE_CYCLE_TOURING = 13
        const val VALUE_RTE_TYPE_CYCLE_MTB = 8
        @Deprecated (
            message = "Use 'VALUE_RTE_TYPE_CYCLE_GRAVEL' instead",
            replaceWith = ReplaceWith("VALUE_RTE_TYPE_CYCLE_GRAVEL")
        )
        const val VALUE_RTE_TYPE_CYCLE_SHORT = 5

        // MOTORIZED VEHICLE

        // generic "car" type
        const val VALUE_RTE_TYPE_CAR = 6
        // "fast car" type, where short time is primary
        const val VALUE_RTE_TYPE_CAR_FAST = 0
        // "eco car" type, where router tries to find good balance between speed & distance
        const val VALUE_RTE_TYPE_CAR_SHORT = 1
        // "motorcycle" type, optimized for two wheels
        const val VALUE_RTE_TYPE_MOTORCYCLE = 7

        // OTHERS

        // routing profile for "cross country skiing"
        const val VALUE_RTE_TYPE_SKI_CROSS_COUNTRY = 12

        /**
         * All possible RTE_TYPES also sorted in correct order.
         */
        val RTE_TYPES_SORTED = intArrayOf(
            VALUE_RTE_TYPE_NO_TYPE,
            VALUE_RTE_TYPE_FOOT_01,
            VALUE_RTE_TYPE_FOOT_02,
            VALUE_RTE_TYPE_FOOT_03,
            VALUE_RTE_TYPE_CYCLE,
            VALUE_RTE_TYPE_CYCLE_FAST,
            VALUE_RTE_TYPE_CYCLE_RACING,
            VALUE_RTE_TYPE_CYCLE_ROAD,
            VALUE_RTE_TYPE_CYCLE_GRAVEL,
            VALUE_RTE_TYPE_CYCLE_TOURING,
            VALUE_RTE_TYPE_CYCLE_SHORT,
            VALUE_RTE_TYPE_CYCLE_MTB,
            VALUE_RTE_TYPE_SKI_CROSS_COUNTRY,
            VALUE_RTE_TYPE_CAR,
            VALUE_RTE_TYPE_CAR_FAST,
            VALUE_RTE_TYPE_CAR_SHORT,
            VALUE_RTE_TYPE_MOTORCYCLE,
        )

        //*************************************************
        // PRIVATE REFERENCES (0 - 29)
        //*************************************************

        /**
         * Object source.
         *
         * "BYTE" in ByteArray
         */
        const val PAR_SOURCE = 0

        /**
         * Private parameter for handling of styles.
         * Use getter/setter directly in GeoData object
         *
         * "STRING" in ByteArray
         */
        const val PAR_STYLE_NAME = 5

        /**
         * Computed are value. Used mainly for tracks
         *
         * "DOUBLE" as String in ByteArray
         */
        const val PAR_AREA_SIZE = 12

        /**
         * Extra data for offline POI database
         *
         * "STRING" (JSON) in ByteArray
         */
        const val PAR_DB_POI_EXTRA_DATA = 13

        /**
         * ID of KML trip to which item belongs
         *
         * "STRING" in ByteArray
         */
        const val PAR_KML_TRIP_ID = 14
        /**
         * Reference to original Google Places item.
         */
        @Deprecated(message = "not supported anymore")
        private const val PAR_GOOGLE_PLACES_REFERENCE = 15

        /**
         * Google Places rating.
         */
        @Deprecated(message = "not supported anymore")
        private const val PAR_GOOGLE_PLACES_RATING = 16

        /**
         * Google places details.
         */
        @Deprecated(message = "not supported anymore")
        private const val PAR_GOOGLE_PLACES_DETAILS = 17

        /**
         * Extra parameters from Locus Store, mostly with custom provider data.
         *
         * "STRING" (JSON) in ByteArray
         */
        const val PAR_STORE_EXTRA = 19

        /**
         * Extra callback parameter used directly by API.
         *
         * "STRING" in ByteArray
         */
        const val PAR_INTENT_EXTRA_CALLBACK = 20

        /**
         * Extra OnDisplay parameter used directly by API.
         *
         * "STRING" in ByteArray
         */
        const val PAR_INTENT_EXTRA_ON_DISPLAY = 21

        //*************************************************
        // PUBLIC VALUES (30 - 49)
        //*************************************************

        /**
         * Item visible description.
         *
         * "STRING" in ByteArray
         */
        const val PAR_DESCRIPTION = 30

        /**
         * Storage for comments (extra tiny description available in GPX files).
         *
         * "STRING" in ByteArray
         */
        const val PAR_COMMENT = 31

        /**
         * Relative path to working dir (for images for example).
         *
         * "STRING" in ByteArray
         */
        const val PAR_RELATIVE_WORKING_DIR = 32

        /**
         * Type (classification) of the item (point).
         *
         * "STRING" in ByteArray
         */
        const val PAR_TYPE = 33

        /**
         * Special code used in Geocache waypoints.
         *
         * "STRING" in ByteArray
         */
        const val PAR_GEOCACHE_CODE = 34

        /**
         * Flag to include item in POI alert feature. If missing, "true" used as default.
         *
         * "BOOLEAN" as String (0,1) in ByteArray
         */
        const val PAR_POI_ALERT_INCLUDE = 35

        /**
         * Separated 2-letter language codes (ISO 639-1) by pipe "|", that define language of
         * the content. Mainly related to text "name", "description" and "comment" values.
         *
         * "STRING" in ByteArray
         */
        const val PAR_LANGUAGE = 36

        // LOCATION PARAMETERS (50 - 59)

        /**
         * Address value - street name.
         *
         * "STRING" in ByteArray
         */
        const val PAR_ADDRESS_STREET = 50

        /**
         * Address value - city name.
         *
         * "STRING" in ByteArray
         */
        const val PAR_ADDRESS_CITY = 51

        /**
         * Address value - name of region.
         *
         * "STRING" in ByteArray
         */
        const val PAR_ADDRESS_REGION = 52

        /**
         * Address value - PSČ, post code number.
         *
         * "STRING" in ByteArray
         */
        const val PAR_ADDRESS_POST_CODE = 53

        /**
         * Address value - name of country.
         *
         * "STRING" in ByteArray
         */
        const val PAR_ADDRESS_COUNTRY = 54

        //*********************************************
        // ROUTE PARAMETERS (100 - 199)
        //*********************************************

        // PARAMETERS FOR NAVIGATION POINTS (WAYPOINT)

        /**
         * Index to the point list.
         * Locus internal variable, **DO NOT SET**
         *
         * "INT" as String in ByteArray
         */
        const val PAR_RTE_INDEX = 100

        /**
         * Distance (in metres) from current navPoint to next
         * Locus internal variable, **DO NOT SET** (float)
         *
         * "FLOAT" as String in ByteArray
         */
        const val PAR_RTE_DISTANCE_F = 101

        /**
         * Time (in sec) from current navPoint to next.
         *
         * "INT" as String in ByteArray
         */
        const val PAR_RTE_TIME_I = 102

        /**
         * Speed (in m/s) from current navPoint to next.
         *
         * "FLOAT" as String in ByteArray
         */
        const val PAR_RTE_SPEED_F = 103

        /**
         * Number of seconds to transition between successive links along
         * the route. These take into account the geometry of the intersection,
         * number of links at the intersection, and types of roads at
         * the intersection. This attempts to estimate the time in seconds it
         * would take for stops, or places where a vehicle must slow to make a turn.
         *
         * "INT" as String in ByteArray
         */
        const val PAR_RTE_TURN_COST = 104

        /**
         * String representation of next street label.
         *
         * "STRING" in ByteArray
         */
        const val PAR_RTE_STREET = 109

        /**
         * Used to determine which type of action should be taken in order to stay on route.
         * Defined as id from `PointRteAction` object.
         *
         * "INT" as String in ByteArray
         */
        const val PAR_RTE_POINT_ACTION = 110

        /**
         * Parameter that define if Via-point should be notified during navigation.
         *
         * "BOOLEAN" as String (0,1) in ByteArray
         */
        const val PAR_RTE_POINT_PASS_PLACE_NOTIFY = 111

        // PARAMETERS FOR NAVIGATION ROUTE

        /**
         * Type of route (car_fast, car_short, cyclo, foot).
         *
         * "INT" as String in ByteArray
         */
        const val PAR_RTE_COMPUTE_TYPE = 120

        /**
         * Roundabout is usually defined from two points. First on enter correctly
         * defined by ACTION 27 - 34, second on exit simply defined by exit angle.
         * In case of usage only exit point, it's need to set this flag.
         *
         * "BOOLEAN" as String (0,1) in ByteArray
         */
        const val PAR_RTE_SIMPLE_ROUNDABOUTS = 121

        /**
         * Configuration of (route) plan as defined in route planner.
         *
         * "STRING" in ByteArray
         */
        const val PAR_RTE_PLAN_DEFINITION = 122

        // EXTRA CONTENT (123 - 129)

        /**
         * Container for max. speeds for the trackpoints.
         *
         * "STRING" (JSON) in ByteArray
         */
        const val PAR_RTE_MAX_SPEEDS = 123

        /**
         * Container for max. speeds for the trackpoints.
         *
         * "STRING" (JSON) in ByteArray
         */
        const val PAR_RTE_WAY_TYPES = 124

        /**
         * Container for track surfaces.
         *
         * "STRING" (JSON) in ByteArray
         */
        const val PAR_RTE_SURFACES = 125

        /**
         * Container for track warnings.
         *
         * "STRING" (JSON) in ByteArray
         */
        const val PAR_RTE_WARNINGS = 126

        //*********************************************
        // OSM BUGS (300 - 309)
        //*********************************************

        /**
         * OpenStreetMap bug/notes ID.
         *
         * "STRING" in ByteArray
         */
        const val PAR_OSM_NOTES_ID = 301

        /**
         * OpenStreetMap bug/notes flag about it's state.
         *
         * "BOOLEAN" as String (0,1) in ByteArray
         */
        const val PAR_OSM_NOTES_CLOSED = 302

        //*********************************************
        // LOPOINTS (310 - 330)
        //*********************************************

        /**
         * Online LoPoint ID value.
         *
         * "LONG" as String in ByteArray
         */
        const val PAR_LOPOINTS_ID = 310

        /**
         * LoPoints labels defined on the server.
         *
         * "STRING" in ByteArray
         */
        const val PAR_LOPOINTS_LABELS = 311

        /**
         * Opening hours String formatted in the OSM format.
         *
         * "STRING" in ByteArray
         */
        const val PAR_LOPOINTS_OPENING_HOURS = 312

        /**
         * Precise point time-zone.
         *
         * "STRING" in ByteArray
         */
        const val PAR_LOPOINTS_TIMEZONE = 313

        /**
         * Extra geometry of the LoPoint in WKB format.
         *
         * "BYTEARRAY"
         */
        const val PAR_LOPOINTS_GEOMETRY = 314

        /**
         * Container for LoMedia objects.
         *
         * "STRING" in ByteArray (JSON)
         */
        const val PAR_LOMEDIA = 315

        /**
         * Container for the LoPoint reviews. This object should not be saved into database.
         *
         * "STRING" in ByteArray (JSON)
         */
        const val PAR_LOPOINT_REVIEWS = 316

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
        fun generateCallbackString(
            name: String, packageName: String,
            className: String, returnDataName: String, returnDataValue: String
        ): String {
            // check parameters
            if (packageName.isEmpty() || className.isEmpty()) {
                logD(tag = TAG) {
                    "generateCallbackString(" + name + ", " + packageName + ", " +
                            className + ", " + returnDataName + ", " + returnDataValue + "), " +
                            "invalid packageName or className parameter"
                }
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
