/*
 * Copyright 2011, Asamm Software, s.r.o.
 *
 * This file is part of LocusAddonPublicLib.
 *
 * LocusAddonPublicLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LocusAddonPublicLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LocusAddonPublicLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package locus.api.objects.geocaching

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException

/**
 * This Object is container for cache Logs.
 * <br></br><br></br>
 * Useful pages with list:<br></br>
 *
 *  * [German list](http://www.gc-reviewer.de/hilfe-tipps-und-tricks/logtypen/)
 *
 *
 * @author menion
 */
class GeocachingLog : Storable() {

    /**
     * Unique ID of a log.
     */
    var id: Long = 0L
    /**
     * Type of log defined by CACHE_LOG_TYPE_X parameter.
     */
    var type: Int = CACHE_LOG_TYPE_UNKNOWN
    /**
     * Time when log was created (in ms).
     */
    var date: Long = 0L
    /**
     * Name of 'finder'.
     */
    var finder: String = ""
    /**
     * ID of the 'finder'.
     */
    var findersId: Long = FINDERS_ID_UNDEFINED
    /**
     * Amount of already found caches by current 'finder'.
     */
    var findersFound: Int = 0
    /**
     * Text of log itself.
     */
    var logText: String = ""
    /**
     * List of attached images.
     */
    private var _images: MutableList<GeocachingImage> = arrayListOf()
    /**
     * Longitude coordinate to this log. Value is in WGS84 format.
     */
    var cooLon: Double = 0.0
    /**
     * Latitude coordinate to this log. Value is in WGS84 format.
     */
    var cooLat: Double = 0.0

    // IMAGES

    /**
     * Iterator over already attached images.
     */
    val images: Iterator<GeocachingImage>
        get() = _images.iterator()

    /**
     * Add image to current list of images attached to this log.
     *
     * @param image image to add
     */
    fun addImage(image: GeocachingImage) {
        this._images.add(image)
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 2
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readLong()
        type = dr.readInt()
        date = dr.readLong()
        finder = dr.readString()
        findersFound = dr.readInt()
        logText = dr.readString()

        // V1
        if (version >= 1) {
            _images = dr.readListStorable(GeocachingImage::class.java)
        }

        // V2
        if (version >= 2) {
            findersId = dr.readLong()
            cooLon = dr.readDouble()
            cooLat = dr.readDouble()
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeLong(id)
        dw.writeInt(type)
        dw.writeLong(date)
        dw.writeString(finder)
        dw.writeInt(findersFound)
        dw.writeString(logText)

        // V1
        dw.writeListStorable(_images)

        // V2
        dw.writeLong(findersId)
        dw.writeDouble(cooLon)
        dw.writeDouble(cooLat)
    }

    companion object {

        // LOG TYPES

        const val CACHE_LOG_TYPE_UNKNOWN = -1
        const val CACHE_LOG_TYPE_FOUND = 0
        const val CACHE_LOG_TYPE_NOT_FOUND = 1
        const val CACHE_LOG_TYPE_WRITE_NOTE = 2
        const val CACHE_LOG_TYPE_NEEDS_MAINTENANCE = 3
        const val CACHE_LOG_TYPE_OWNER_MAINTENANCE = 4
        const val CACHE_LOG_TYPE_PUBLISH_LISTING = 5
        const val CACHE_LOG_TYPE_ENABLE_LISTING = 6
        const val CACHE_LOG_TYPE_TEMPORARILY_DISABLE_LISTING = 7
        const val CACHE_LOG_TYPE_UPDATE_COORDINATES = 8
        const val CACHE_LOG_TYPE_ANNOUNCEMENT = 9
        const val CACHE_LOG_TYPE_WILL_ATTEND = 10
        const val CACHE_LOG_TYPE_ATTENDED = 11
        const val CACHE_LOG_TYPE_POST_REVIEWER_NOTE = 12
        const val CACHE_LOG_TYPE_NEEDS_ARCHIVED = 13
        const val CACHE_LOG_TYPE_WEBCAM_PHOTO_TAKEN = 14
        const val CACHE_LOG_TYPE_RETRACT_LISTING = 15
        const val CACHE_LOG_TYPE_ARCHIVE = 16
        const val CACHE_LOG_TYPE_UNARCHIVE = 17
        const val CACHE_LOG_TYPE_PERMANENTLY_ARCHIVED = 18

        // flag that ID of finder is not defined
        const val FINDERS_ID_UNDEFINED: Long = 0L
    }
}