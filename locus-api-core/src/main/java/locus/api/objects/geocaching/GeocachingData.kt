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

package locus.api.objects.geocaching

import com.asamm.loggerV2.logE
import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import locus.api.utils.Utils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Container for main Geocaching data
 *
 * INFO
 *
 * all times are in format '2009-09-22T14:16:03.0000000+0200', where important is only first
 * part (Date), so it should looks for example only as '2009-09-22T'. This should work
 *
 * Groundspeak staging API: https://staging.api.groundspeak.com/Live/v6beta/geocaching.svc/help
 *
 * @author menion
 */
class GeocachingData : Storable() {

    /**
     * Current defined ID of cache. This ID is just optional unique identification as long
     * number that has no extra usage in Locus.
     */
    var id: Long = 0L

    /**
     * Whole cache ID from gc.com - so GC...
     * REQUIRED
     */
    var cacheID: String = ""
        set(value) {
            if (value.isBlank()) {
                return
            }
            field = value
            val testCode = value.trim { it <= ' ' }.uppercase()
            this.source = when {
                testCode.startsWith("GC") -> CACHE_SOURCE_GEOCACHING_COM
                testCode.startsWith("OB") -> CACHE_SOURCE_OPENCACHING_NL
                testCode.startsWith("OK") -> CACHE_SOURCE_OPENCACHING_UK
                testCode.startsWith("OP") -> CACHE_SOURCE_OPENCACHING_PL
                testCode.startsWith("OU") -> CACHE_SOURCE_OPENCACHING_US
                testCode.startsWith("OZ") -> CACHE_SOURCE_OPENCACHING_CZ
                testCode.startsWith("O") -> CACHE_SOURCE_OPENCACHING
                else -> CACHE_SOURCE_UNDEFINED
            }
        }

    /**
     * State of cache - available or disable.
     */
    var isAvailable: Boolean = true

    /**
     * State of cache - already archived or not.
     */
    var isArchived: Boolean = false

    /**
     * Flag if cache is only for a premium members.
     */
    var isPremiumOnly: Boolean = false

    /**
     * Name of cache visible on all places in Locus as an title.
     * REQUIRED!
     */
    var name: String = ""

    /**
     * Name of person who placed cache (groundspeak:placed_by). It is displayed in Locus when
     * tapped on point or in main GC page.
     */
    var placedBy: String = ""

    /**
     * Name of cache owner (groundspeak:owner). This value is not displayed in Locus.
     */
    var owner: String = ""

    /**
     * Get date/time, when owner create/hide cache on a gc.com web page.
     */
    var dateHidden: Long = 0L

    /**
     * Date/time, when owner published cache for users. Usually most important date.
     */
    var datePublished: Long = 0L

    /**
     * Date/time, when cache was updated for the last time, on gc.com server.
     */
    var dateUpdated: Long = 0L

    /**
     * Type of a cache, define by constants CACHE_TYPE_X.
     */
    var type: Int = CACHE_TYPE_UNDEFINED

    /**
     * Size of cache container. Size is defined by parameters CACHE_SIZE_X, or by text directly.
     */
    var container: Int = CACHE_SIZE_NOT_CHOSEN

    /**
     * Difficulty value - 1.0 - 5.0 (by 0.5).
     */
    var difficulty: Float = -1.0f

    /**
     * Terrain value - 1.0 - 5.0 (by 0.5).
     */
    var terrain: Float = -1.0f

    /**
     * Name of country, where is cache places.
     */
    var country: String = ""

    /**
     * Name of state, where is cache places.
     */
    var state: String = ""

    /**
     * Descriptions are now stored in raw GZIPed bytes. This allows to keep smaller size
     * of loaded GeocacheData object and also in cases, we don't need short/long
     * description, it also save quite a lot of CPU (not need to use GZIP)
     */
    private var descBytes: ByteArray? = null

    /**
     * Length of Short description in descBytes array. This parameter is needed
     * for correct storalization.
     */
    private var shortDescLength: Int = 0

    /**
     * Encoded hints.
     */
    var encodedHints: String = ""

    /**
     * List of attributes.
     */
    var attributes: MutableList<GeocachingAttribute> = arrayListOf()

    /**
     * List of logs.
     */
    var logs: MutableList<GeocachingLog> = arrayListOf()

    /**
     * List of travel bugs.
     */
    var trackables: MutableList<GeocachingTrackable> = arrayListOf()

    /**
     * List of waypoints.
     */
    var waypoints: MutableList<GeocachingWaypoint> = arrayListOf()

    /**
     * User notes defined locally. Modify only directly by the user, not over any API or 3rd party service.
     */
    var notesLocal: String = ""
        set(value) {
            field = value
            notesLocalUpdatedAt = System.currentTimeMillis()
        }

    /**
     * Time when local notes were updated for the last time.
     */
    var notesLocalUpdatedAt: Long = 0L
        private set

    /**
     * External notes loaded online or over any 3rd party app, GPX, etc.
     */
    var notesExternal: String = ""
        set(value) {
            field = value
            notesExternalUpdatedAt = System.currentTimeMillis()
        }

    /**
     * Time when external notes were updated for the last time.
     */
    var notesExternalUpdatedAt: Long = 0L
        private set

    /**
     * Flag if cache is 'computed' - have corrected coordinates.
     */
    var isComputed: Boolean = false

    /**
     * Flag if cache is already found by user.
     */
    var isFound: Boolean = false

    /**
     * Stored URL to cache itself. Keep in mind that this value may not be defined or
     * may include various formats. Suggested is to use [.getCacheUrlFull] method,
     * that should return valid URL.
     */
    var cacheUrl: String = ""

    /**
     * Number of favorite points attached to current cache.
     */
    var favoritePoints: Int = -1

    // V1

    /**
     * GcVote - number of votes.
     */
    var gcVoteNumOfVotes: Int = -1

    /**
     * Average (not median) value.
     */
    var gcVoteAverage: Float = 0.0f

    /**
     * User value for GCVote.
     */
    var gcVoteUserVote: Float = 0.0f

    // V2

    /**
     * Original longitude defined by owner.
     */
    var lonOriginal: Double = 0.0

    /**
     * Original latitude defined by owner.
     */
    var latOriginal: Double = 0.0

    /**
     * List of attached images.
     */
    var images: MutableList<GeocachingImage> = arrayListOf()

    // V3

    /**
     * Define source of this cache as constant value. Be aware, that this value is automatically set when
     * setup a cacheID, so use this method after the [.setCacheID] function is used.
     */
    var source: Int = CACHE_SOURCE_UNDEFINED

    //*************************************************
    // HELPERS
    //*************************************************

    /**
     * Cache container size in text form.
     */
    val containerText: String
        get() {
            when (container) {
                CACHE_SIZE_MICRO -> return "Micro"
                CACHE_SIZE_SMALL -> return "Small"
                CACHE_SIZE_REGULAR -> return "Regular"
                CACHE_SIZE_LARGE -> return "Large"
                CACHE_SIZE_HUGE -> return "Huge"
                CACHE_SIZE_NOT_CHOSEN -> return "Not chosen"
                CACHE_SIZE_OTHER -> return "Other"
            }
            return ""
        }

    fun setContainer(value: String) {
        container = when {
            value.equals("Micro", ignoreCase = true) -> CACHE_SIZE_MICRO
            value.equals("Small", ignoreCase = true) -> CACHE_SIZE_SMALL
            value.equals("Regular", ignoreCase = true) -> CACHE_SIZE_REGULAR
            value.equals("Large", ignoreCase = true) -> CACHE_SIZE_LARGE
            value.equals("Huge", ignoreCase = true) -> CACHE_SIZE_HUGE
            value.equals("Other", ignoreCase = true) -> CACHE_SIZE_OTHER
            else -> CACHE_SIZE_NOT_CHOSEN
        }
    }

    /**
     * Cache descriptions as pair of <short, long> description.
     */
    val descriptions: Array<String>
        get() {
            // prepare container
            val res = arrayOf("", "")
            if (descBytes == null || descBytes!!.isEmpty()) {
                // return empty texts if no desc exists
                return res
            }

            // prepare input stream
            var zis: GZIPInputStream? = null
            try {
                zis = GZIPInputStream(
                    ByteArrayInputStream(descBytes!!), 10240
                )

                // read short description
                val result = Utils.doBytesToString(zis.readBytes())
                if (shortDescLength > 0) {
                    res[0] = result.substring(0, shortDescLength)
                }

                // read long description
                res[1] = result.substring(shortDescLength)
            } catch (e: IOException) {
                logE(tag = TAG, ex = e) { "" }
                res[0] = ""
                res[1] = ""
            } finally {
                Utils.closeStream(zis)
            }

            // return result
            return res
        }

    fun setDescriptions(
        shortDesc: String, shortInHtml: Boolean,
        longDesc: String, longInHtml: Boolean
    ): Boolean {
        // store descriptions
        try {
            val baos = ByteArrayOutputStream()
            val zos = GZIPOutputStream(baos)

            zos.write(Utils.doStringToBytes(shortDesc))
            zos.write(Utils.doStringToBytes(longDesc))
            zos.close()

            // store parameters
            descBytes = baos.toByteArray()
            shortDescLength = shortDesc.length
            return true
        } catch (e: IOException) {
            logE(tag = TAG, ex = e) {
                "setDescription(" +
                        shortDesc + ", " + shortInHtml + ", " +
                        longDesc + ", " + longInHtml + ")"
            }
            descBytes = null
            shortDescLength = 0
            return false
        }
    }

    /**
     * Get modified URL to cache listing on web page, useful for direct use.
     */
    val cacheUrlFull: String
        get() {
            // if cache is from Groundspeak, return "coord.info" url
            if (source == CACHE_SOURCE_GEOCACHING_COM) {
                return "https://coord.info/$cacheID"
            }

            // check defined URL
            return cacheUrl.ifEmpty {
                "https://www.geocaching.com/seek/cache_details.aspx?wp=$cacheID"
            }
        }

    //*************************************************
    // UTILS
    //*************************************************

    /**
     * Check if cache is valid.
     *
     * This means it has:
     * - valid 'cache ID' (GC code)
     * - valid 'name'
     * - valid 'type'
     */
    val isCacheValid: Boolean
        get() = cacheID.isNotEmpty()
                && name.isNotEmpty()
                && type != CACHE_TYPE_UNDEFINED

    /**
     * Set type of the cache from the String representation.
     */
    fun setType(type: String) {
        this.type = getTypeAsInt(type)
    }

    /**
     * Check if owner, country, state or description contains certain defined text.
     *
     * @param text text that we search
     */
    fun containsInData(text: String): Boolean {
        // base texts
        if (owner.contains(text, ignoreCase = true)) {
            return true
        }
        if (country.contains(text, ignoreCase = true)) {
            return true
        }
        if (state.contains(text, ignoreCase = true)) {
            return true
        }

        // check descriptions
        val desc = descriptions
        return desc[0].contains(text, ignoreCase = true)
                || desc[1].contains(text, ignoreCase = true)
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 4
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readLong()
        // set cache ID over "set" method, to also correctly set source parameter
        cacheID = dr.readString()
        isAvailable = dr.readBoolean()
        isArchived = dr.readBoolean()
        isPremiumOnly = dr.readBoolean()
        name = dr.readString()
        dateUpdated = dr.readLong()
        dateHidden = dr.readLong()
        placedBy = dr.readString()
        owner = dr.readString()
        datePublished = dr.readLong()
        type = dr.readInt()
        container = dr.readInt()
        difficulty = dr.readFloat()
        terrain = dr.readFloat()
        country = dr.readString()
        state = dr.readString()

        // total length
        val size = dr.readInt()
        // length of short description
        shortDescLength = dr.readInt()
        // read raw data
        if (size > 0) {
            descBytes = dr.readBytes(size)
        }

        // read rest
        encodedHints = dr.readString()
        attributes = dr.readListStorable(GeocachingAttribute::class.java)
        logs = dr.readListStorable(GeocachingLog::class.java)
        trackables = dr.readListStorable(GeocachingTrackable::class.java)
        waypoints = dr.readListStorable(GeocachingWaypoint::class.java)
        notesLocal = dr.readString()
        isComputed = dr.readBoolean()
        isFound = dr.readBoolean()
        cacheUrl = dr.readString()
        favoritePoints = dr.readInt()

        // V1
        if (version >= 1) {
            gcVoteNumOfVotes = dr.readInt()
            gcVoteAverage = dr.readFloat()
            gcVoteUserVote = dr.readFloat()
        }

        // V2
        if (version >= 2) {
            lonOriginal = dr.readDouble()
            latOriginal = dr.readDouble()
            images = dr.readListStorable(GeocachingImage::class.java)
        }

        // V3
        if (version >= 3) {
            source = dr.readInt()
        }

        // V4
        if (version >= 4) {
            notesLocalUpdatedAt = dr.readLong()
            notesExternal = dr.readString()
            notesExternalUpdatedAt = dr.readLong()
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeLong(id)
        dw.writeString(cacheID)
        dw.writeBoolean(isAvailable)
        dw.writeBoolean(isArchived)
        dw.writeBoolean(isPremiumOnly)
        dw.writeString(name)
        dw.writeLong(dateUpdated)
        dw.writeLong(dateHidden)
        dw.writeString(placedBy)
        dw.writeString(owner)
        dw.writeLong(datePublished)
        dw.writeInt(type)
        dw.writeInt(container)
        dw.writeFloat(difficulty)
        dw.writeFloat(terrain)
        dw.writeString(country)
        dw.writeString(state)

        // write listings
        if (descBytes == null || descBytes!!.isEmpty()) {
            // total length
            dw.writeInt(0)
            // length of short description
            dw.writeInt(0)
        } else {
            // total length
            dw.writeInt(descBytes!!.size)
            // length of short description
            dw.writeInt(shortDescLength)
            dw.write(descBytes)
        }

        // write rest
        dw.writeString(encodedHints)
        dw.writeListStorable(attributes)
        dw.writeListStorable(logs)
        dw.writeListStorable(trackables)
        dw.writeListStorable(waypoints)
        dw.writeString(notesLocal)
        dw.writeBoolean(isComputed)
        dw.writeBoolean(isFound)
        dw.writeString(cacheUrl)
        dw.writeInt(favoritePoints)

        // V1
        dw.writeInt(gcVoteNumOfVotes)
        dw.writeFloat(gcVoteAverage)
        dw.writeFloat(gcVoteUserVote)

        // V2
        dw.writeDouble(lonOriginal)
        dw.writeDouble(latOriginal)
        dw.writeListStorable(images)

        // V3
        dw.writeInt(source)

        // V4
        dw.writeLong(notesLocalUpdatedAt)
        dw.writeString(notesExternal)
        dw.writeLong(notesExternalUpdatedAt)
    }

    companion object {

        // tag for logger
        private const val TAG = "GeocachingData"

        // GEOCACHE TYPES
        // https://api.groundspeak.com/documentation#geocache-types

        /**
         * Traditional cache, API Id: 2
         */
        const val CACHE_TYPE_TRADITIONAL = 0

        /**
         * Multi-cache, API Id: 3
         */
        const val CACHE_TYPE_MULTI = 1

        /**
         * Mystery/Unknown cache, API Id: 8
         */
        const val CACHE_TYPE_MYSTERY = 2

        /**
         * Virtual cache, API Id: 4
         */
        const val CACHE_TYPE_VIRTUAL = 3

        /**
         * Earthcache, API Id: 137
         */
        const val CACHE_TYPE_EARTH = 4

        /**
         * Project A.P.E., API Id: 9
         */
        const val CACHE_TYPE_PROJECT_APE = 5

        /**
         * Letterbox Hybrid cache, API Id: 5
         */
        const val CACHE_TYPE_LETTERBOX = 6

        /**
         * Wherigo, API Id: 1858
         */
        const val CACHE_TYPE_WHERIGO = 7

        /**
         * Event cache, API Id: 6
         */
        const val CACHE_TYPE_EVENT = 8

        /**
         * Mega-event, API Id: 453
         */
        const val CACHE_TYPE_MEGA_EVENT = 9

        /**
         * Cache In Trash Out Event, API Id: 13
         */
        const val CACHE_TYPE_CACHE_IN_TRASH_OUT = 10

        /**
         * GPS Adventures Exhibit, API Id: 1304
         */
        const val CACHE_TYPE_GPS_ADVENTURE = 11

        /**
         * Webcam, API Id: 11
         */
        const val CACHE_TYPE_WEBCAM = 12

        /**
         * Locationless (Reverse) Cache, API Id: 12
         */
        const val CACHE_TYPE_LOCATIONLESS = 13

        /**
         * Benchmark, not published over API
         */
        const val CACHE_TYPE_BENCHMARK = 14

        /**
         * Maze Exhibit, not published over API
         */
        const val CACHE_TYPE_MAZE_EXHIBIT = 15

        /**
         * Waymark, not published over API
         */
        const val CACHE_TYPE_WAYMARK = 16

        @Deprecated(
            message = "No longer user cache type/name",
            replaceWith = ReplaceWith("CACHE_TYPE_GC_HQ")
        )
        const val CACHE_TYPE_GROUNDSPEAK = 17

        /**
         * Geocaching HQ, API Id: 3773
         * Previously "Groundspeak"
         */
        const val CACHE_TYPE_GC_HQ = 17

        @Deprecated(
            message = "No longer user cache type/name",
            replaceWith = ReplaceWith("CACHE_TYPE_COMMUNITY_CELEBRATION")
        )
        const val CACHE_TYPE_LF_EVENT = 18

        /**
         * Community Celebration Event, API Id: 3653
         * Previously "Lost And Found Event"
         */
        const val CACHE_TYPE_COMMUNITY_CELEBRATION = 18

        @Deprecated(
            message = "No longer user cache type/name",
            replaceWith = ReplaceWith("CACHE_TYPE_GC_HQ_CELEBRATION")
        )
        const val CACHE_TYPE_LF_CELEBRATION = 19

        /**
         * Geocaching HQ Celebration, API Id: 3774
         * Previously "Lost And Found Celebration"
         */
        const val CACHE_TYPE_GC_HQ_CELEBRATION = 19

        /**
         * Giga-event, API Id: 7005
         */
        const val CACHE_TYPE_GIGA_EVENT = 20

        /**
         * Lab cache, not published over API
         */
        const val CACHE_TYPE_LAB_CACHE = 21

        /**
         * Geocaching HQ Block Party, API Id: 4738
         */
        const val CACHE_TYPE_GC_HQ_BLOCK_PARTY = 22

        /**
         * Incorrect/undefined type of geocache. Such point should not be considered as geocache
         * at all.
         */
        const val CACHE_TYPE_UNDEFINED = -1

        const val CACHE_SIZE_NOT_CHOSEN = 0
        const val CACHE_SIZE_MICRO = 1
        const val CACHE_SIZE_SMALL = 2
        const val CACHE_SIZE_REGULAR = 3
        const val CACHE_SIZE_LARGE = 4
        const val CACHE_SIZE_HUGE = 5
        const val CACHE_SIZE_OTHER = 6

        const val CACHE_SOURCE_UNDEFINED = 0
        const val CACHE_SOURCE_GEOCACHING_COM = 1
        const val CACHE_SOURCE_GEOCACHING_HU = 2
        const val CACHE_SOURCE_OPENCACHING = 100
        const val CACHE_SOURCE_OPENCACHING_DE = 101
        const val CACHE_SOURCE_OPENCACHING_ES = 102
        const val CACHE_SOURCE_OPENCACHING_FR = 103
        const val CACHE_SOURCE_OPENCACHING_IT = 104
        const val CACHE_SOURCE_OPENCACHING_NL = 105
        const val CACHE_SOURCE_OPENCACHING_PL = 106
        const val CACHE_SOURCE_OPENCACHING_RO = 107
        const val CACHE_SOURCE_OPENCACHING_UK = 108
        const val CACHE_SOURCE_OPENCACHING_US = 109
        const val CACHE_SOURCE_OPENCACHING_CZ = 110

        fun getTypeAsString(type: Int): String {
            when (type) {
                CACHE_TYPE_TRADITIONAL -> return "Traditional Cache"
                CACHE_TYPE_MULTI -> return "Multi-Cache"
                CACHE_TYPE_MYSTERY -> return "Unknown Cache" // "Mystery Cache"
                CACHE_TYPE_VIRTUAL -> return "Virtual Cache"
                CACHE_TYPE_EARTH -> return "EarthCache"
                CACHE_TYPE_PROJECT_APE -> return "Project APE Cache"
                CACHE_TYPE_LETTERBOX -> return "Letterbox"
                CACHE_TYPE_WHERIGO -> return "Wherigo Cache"
                CACHE_TYPE_EVENT -> return "Event Cache"
                CACHE_TYPE_MEGA_EVENT -> return "Mega-Event Cache"
                CACHE_TYPE_CACHE_IN_TRASH_OUT -> return "Cache In Trash Out Event"
                CACHE_TYPE_GPS_ADVENTURE -> return "GPS Adventure"
                CACHE_TYPE_WEBCAM -> return "Webcam Cache"
                CACHE_TYPE_LOCATIONLESS -> return "Location-less"
                CACHE_TYPE_BENCHMARK -> return "Benchmark"
                CACHE_TYPE_MAZE_EXHIBIT -> return "Maze Exhibit"
                CACHE_TYPE_WAYMARK -> return "Waymark"
                CACHE_TYPE_COMMUNITY_CELEBRATION -> return "L&F Event"
                CACHE_TYPE_GC_HQ -> return "Groundspeak"
                CACHE_TYPE_GC_HQ_CELEBRATION -> return "L&F Celebration"
                CACHE_TYPE_GIGA_EVENT -> return "Giga-Event Cache"
                CACHE_TYPE_LAB_CACHE -> return "Lab Cache"
                else -> return "Geocache"
            }
        }

        /**
         * Get type of cache based on it's text representation.
         *
         * @param type type as text
         * @return code of cache type
         */
        fun getTypeAsInt(type: String): Int {
            var typeNew = type
            // check text
            if (typeNew.isBlank()) {
                return CACHE_TYPE_UNDEFINED
            }

            // split if contains unwanted data
            if (typeNew.startsWith("Geocache|")) {
                typeNew = typeNew.substring("Geocache|".length)
            }

            // handle type
            return if (typeNew.equals("Traditional Cache", ignoreCase = true)) {
                CACHE_TYPE_TRADITIONAL
            } else if (typeNew.equals("Multi-cache", ignoreCase = true)) {
                CACHE_TYPE_MULTI
            } else if (typeNew.equals("Mystery Cache", ignoreCase = true) ||
                typeNew.equals("Unknown Cache", ignoreCase = true) ||
                typeNew.equals("Mystery/Puzzle Cache", ignoreCase = true)
            ) {
                CACHE_TYPE_MYSTERY
            } else if (typeNew.equals("Project APE Cache", ignoreCase = true)
                || typeNew.equals("Project A.P.E. Cache", ignoreCase = true)
            ) {
                CACHE_TYPE_PROJECT_APE
            } else if (typeNew.equals("Letterbox Hybrid", ignoreCase = true)
                || typeNew.equals("Letterbox", ignoreCase = true)
            ) {
                CACHE_TYPE_LETTERBOX
            } else if (typeNew.equals("Wherigo", ignoreCase = true)
                || typeNew.equals("Wherigo cache", ignoreCase = true)
            ) {
                CACHE_TYPE_WHERIGO
            } else if (typeNew.equals("Event Cache", ignoreCase = true)) {
                CACHE_TYPE_EVENT
            } else if (typeNew.equals("Mega-Event Cache", ignoreCase = true)) {
                CACHE_TYPE_MEGA_EVENT
            } else if (typeNew.equals("Cache In Trash Out Event", ignoreCase = true)) {
                CACHE_TYPE_CACHE_IN_TRASH_OUT
            } else if (typeNew.equals("EarthCache", ignoreCase = true)) {
                CACHE_TYPE_EARTH
            } else if (typeNew.lowercase().startsWith("gps adventures")) {
                CACHE_TYPE_GPS_ADVENTURE
            } else if (typeNew.equals("Virtual Cache", ignoreCase = true)) {
                CACHE_TYPE_VIRTUAL
            } else if (typeNew.equals("Webcam Cache", ignoreCase = true)) {
                CACHE_TYPE_WEBCAM
            } else if (typeNew.equals("Locationless Cache", ignoreCase = true)) {
                CACHE_TYPE_LOCATIONLESS
            } else if (typeNew.equals("Benchmark", ignoreCase = true)) {
                CACHE_TYPE_BENCHMARK
            } else if (typeNew.equals("Maze Exhibit", ignoreCase = true)) {
                CACHE_TYPE_MAZE_EXHIBIT
            } else if (typeNew.equals("Waymark", ignoreCase = true)) {
                CACHE_TYPE_WAYMARK
            } else if (typeNew.equals("Groundspeak", ignoreCase = true)) {
                CACHE_TYPE_GC_HQ
            } else if (typeNew.equals("L&F Event", ignoreCase = true)) {
                CACHE_TYPE_COMMUNITY_CELEBRATION
            } else if (typeNew.equals("L&F Celebration", ignoreCase = true)) {
                CACHE_TYPE_GC_HQ_CELEBRATION
            } else if (typeNew.equals("Giga-Event Cache", ignoreCase = true)) {
                CACHE_TYPE_GIGA_EVENT
            } else if (typeNew.equals("Lab Cache", ignoreCase = true)) {
                CACHE_TYPE_LAB_CACHE
            } else {
                CACHE_TYPE_UNDEFINED
            }
        }

        /**
         * Check if certain type of cache is type "Event".
         *
         * @param type type of cache to test
         * @return `true` if cache is event type
         */
        fun isEventCache(type: Int): Boolean {
            return type == CACHE_TYPE_EVENT
                    || type == CACHE_TYPE_MEGA_EVENT
                    || type == CACHE_TYPE_GIGA_EVENT
                    || type == CACHE_TYPE_GPS_ADVENTURE
                    || type == CACHE_TYPE_CACHE_IN_TRASH_OUT
        }
    }
}
