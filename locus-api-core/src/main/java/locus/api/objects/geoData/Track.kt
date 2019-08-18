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

import locus.api.objects.extra.Location
import locus.api.objects.extra.TrackStats
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import locus.api.utils.Logger

import java.io.IOException
import java.util.Arrays

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

    val pointsCount: Int
        get() = points.size

    /**
     * Get point on certain index.
     *
     * @param point index
     */
    fun getPoint(index: Int) : Location {
        return points[index]
    }

    /**
     * Set custom track statistics to current track.
     */
    fun setStats(data: ByteArray) {
        stats = try {
            TrackStats().apply { read(data) }
        } catch (e: Exception) {
            Logger.logE(TAG, "setStats(" + Arrays.toString(data) + ")", e)
            TrackStats()
        }
    }

    // CUSTOM HANDLING

    var breaksData: ByteArray
        get() {
            val dw: DataWriterBigEndian
            try {
                dw = DataWriterBigEndian()
                for (i in breaks.indices) {
                    dw.writeInt(breaks[i])
                }
                return dw.toByteArray()
            } catch (e: Exception) {
                Logger.logE(TAG, "getBreaksData()", e)
            }

            return ByteArray(0)
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
                Logger.logE(TAG, "setBreaksData()", e)
                breaks.clear()
            }
        }

    //*************************************************
    // STORABLE PART
    //*************************************************

    public override fun getVersion(): Int {
        return 6
    }

    @Throws(IOException::class)
    public override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readLong()
        name = dr.readString()

        // load locations
        points = dr.readListStorable(Location::class.java)

        // read breaks
        val breaksSize = dr.readInt()
        if (breaksSize > 0) {
            breaksData = dr.readBytes(breaksSize)
        }

        // read waypoints
        waypoints = dr.readListStorable(Point::class.java)

        // read extra part
        readExtraData(dr)
        readStyles(dr)

        // old deprecated statistics
        // clear previous values
        stats = TrackStats()

        // read all old data
        stats.numOfPoints = dr.readInt()
        stats.startTime = dr.readLong()
        stats.stopTime = dr.readLong()

        stats.totalLength = dr.readFloat()
        stats.totalLengthMove = dr.readFloat()
        stats.totalTime = dr.readLong()
        stats.totalTimeMove = dr.readLong()
        stats.speedMax = dr.readFloat()

        stats.altitudeMax = dr.readFloat()
        stats.altitudeMin = dr.readFloat()

        stats.eleNeutralDistance = dr.readFloat()
        stats.eleNeutralHeight = dr.readFloat()
        stats.elePositiveDistance = dr.readFloat()
        stats.elePositiveHeight = dr.readFloat()
        stats.eleNegativeDistance = dr.readFloat()
        stats.eleNegativeHeight = dr.readFloat()
        dr.readFloat() // eleTotalAbsDistance
        dr.readFloat() // eleTotalAbsHeight

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
            readWriteMode = ReadWriteMode.values()[dr.readInt()]
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
    }

    @Throws(IOException::class)
    public override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeLong(id)
        dw.writeString(name)

        // write locations
        dw.writeListStorable(points)

        // write breaks
        dw.writeInt(breaksData.size)
        if (breaksData.isNotEmpty()) {
            dw.write(breaksData)
        }

        // write waypoints
        dw.writeListStorable(waypoints)

        // write extra data
        writeExtraData(dw)
        writeStyles(dw)

        // write old statistics for reader below version 3
        dw.writeInt(0)
        dw.writeLong(0L)
        dw.writeLong(0L)

        dw.writeFloat(0.0f)
        dw.writeFloat(0.0f)
        dw.writeLong(0L)
        dw.writeLong(0L)
        dw.writeFloat(0.0f)

        dw.writeFloat(0.0f)
        dw.writeFloat(0.0f)

        dw.writeFloat(0.0f)
        dw.writeFloat(0.0f)
        dw.writeFloat(0.0f)
        dw.writeFloat(0.0f)
        dw.writeFloat(0.0f)
        dw.writeFloat(0.0f)
        dw.writeFloat(0.0f)
        dw.writeFloat(0.0f)

        // V1
        dw.writeBoolean(isUseFolderStyle)

        // V2
        dw.writeLong(timeCreated)

        // V3
        dw.writeStorable(stats)

        // V4
        dw.writeInt(readWriteMode.ordinal)

        // V5
        dw.writeInt(activityType)

        // V6
        dw.writeLong(storeItemId)
        dw.writeLong(storeVersionId)
    }

    companion object {

        // tag for logger
        private const val TAG = "Track"
    }
}
