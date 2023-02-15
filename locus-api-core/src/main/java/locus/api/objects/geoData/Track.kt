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

package locus.api.objects.geoData

import com.asamm.loggerV2.logE
import locus.api.objects.extra.Location
import locus.api.objects.extra.TrackStats
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException

class Track : GeoData() {

    /**
     * Locations of this track
     */
    var points: MutableList<Location> = arrayListOf()

    /**
     * List containing all track break points. Break point is defined as index of point, after
     * which follow break in track. So break point "1" means, that after second point (point with
     * index 1) follow a break.
     */
    var breaks: MutableList<Int> = arrayListOf()

    /**
     * Serialized (binary) version of breaks.
     */
    var breaksBinary: ByteArray
        get() {
            return DataWriterBigEndian().apply {
                for (i in breaks.indices) {
                    writeInt(breaks[i])
                }
            }.toByteArray()
        }
        set(value) {
            breaks.clear()
            if (value.isEmpty()) {
                return
            }

            try {
                val dr = DataReaderBigEndian(value)
                while (dr.available() > 0) {
                    breaks.add(dr.readInt())
                }
            } catch (e: Exception) {
                logE(tag = TAG, ex = e) { "setBreaksFromData($value)" }
                breaks.clear()
            }
        }


    /**
     * Extra points (also may include routing data)
     */
    var waypoints: MutableList<Point> = arrayListOf()

    /**
     * Flag that indicate whether to use parent folder style if exists.
     */
    var isUseFolderStyle: Boolean = true

    /**
     * Type of activity
     */
    var activityType: Int = 0

    /**
     * Track statistics (generated statistics of track)
     */
    var stats: TrackStats = TrackStats()

    /**
     * Reference to Locus Store item (item ID).
     */
    var storeItemId: Long = -1L

    /**
     * Reference to Locus Store item (version ID)
     */
    var storeVersionId: Long = -1L

    //*************************************************
    // HELPERS
    //*************************************************

    val pointsCount: Int
        get() = points.size

    /**
     * Get point on certain index.
     *
     * @param index point index
     */
    fun getPoint(index: Int): Location {
        return points[index]
    }

    /**
     * Set custom track statistics to current track.
     */
    fun setStats(data: ByteArray) {
        stats = try {
            TrackStats().apply { read(data) }
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "setStats(" + data.contentToString() + ")" }
            TrackStats()
        }
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

    public override fun getVersion(): Int {
        return 8
    }

    @Throws(IOException::class)
    public override fun readObject(version: Int, dr: DataReaderBigEndian) {
        // reset defaults
        stats = TrackStats()

        // read basics
        id = dr.readLong()
        name = dr.readString()

        // load locations
        points = dr.readListStorable(Location::class.java)

        // read breaks
        val breaksSize = dr.readInt()
        if (breaksSize > 0) {
            breaksBinary = dr.readBytes(breaksSize)
        }

        // read waypoints
        waypoints = dr.readListStorable(Point::class.java)

        // read extra part
        readExtraData(dr)
        readStyles(dr)

        // skip old statistics
        dr.readBytes(88)

        // V1
        if (version >= 1) {
            isUseFolderStyle = dr.readBoolean()
        }

        // V2
        if (version >= 2) {
            timeCreated = dr.readLong()
        }

        // V3
        if (version >= 3) {
            stats = TrackStats()
            stats.read(dr)
        }

        // V4
        if (version >= 4) {
            protected = dr.readInt() == 0
        }

        // V5
        if (version >= 5) {
            activityType = dr.readInt()
        }

        // V6
        if (version >= 6) {
            storeItemId = dr.readLong()
            storeVersionId = dr.readLong()
        }

        // V7
        if (version >= 7) {
            timeUpdated = dr.readLong()
        }

        // V8
        if (version >= 8) {
            val privacyValue = dr.readString()
            privacy = Privacy.values().find { it.name == privacyValue }
                ?: privacy
        }
    }

    @Throws(IOException::class)
    public override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeLong(id)
        dw.writeString(name)

        // write locations
        dw.writeListStorable(points)

        // write breaks
        breaksBinary.let {
            dw.writeInt(it.size)
            if (it.isNotEmpty()) {
                dw.write(it)
            }
        }

        // write waypoints
        dw.writeListStorable(waypoints)

        // write extra data
        writeExtraData(dw)
        writeStyles(dw)

        // write block of empty statistics
        dw.write(ByteArray(88) { 0 })

        // V1
        dw.writeBoolean(isUseFolderStyle)

        // V2
        dw.writeLong(timeCreated)

        // V3
        dw.writeStorable(stats)

        // V4
        dw.writeInt(if (protected) 0 else 1)

        // V5
        dw.writeInt(activityType)

        // V6
        dw.writeLong(storeItemId)
        dw.writeLong(storeVersionId)

        // V7
        dw.writeLong(timeUpdated)

        // V8
        dw.writeString(privacy.name)
    }

    companion object {

        // tag for logger
        private const val TAG = "Track"
    }
}
