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

package locus.api.android.objects

import android.graphics.Bitmap

import locus.api.android.utils.UtilsBitmap
import locus.api.objects.Storable
import locus.api.objects.extra.GeoDataStyle
import locus.api.objects.extra.Point
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

import java.io.IOException
import java.util.ArrayList

/**
 * Empty constructor used for [Storable]. Do not use directly!
 */
class PackPoints() : Storable() {

    /**
     * Unique name.
     * PackPoints send to Locus with same name (to display), will be overwrite in Locus
     */
    var name: String = ""
        private set

    /**
     * Icon applied to whole PackPoints.
     */
    var extraStyle: GeoDataStyle? = null
    /**
     * Bitmap for this pack.
     */
    var bitmap: Bitmap? = null
    /**
     * List of all points stored in this object.
     */
    private val points: MutableList<Point> = ArrayList()

    /**
     * Add single point into pack.
     *
     * @param pt point to add
     */
    fun addPoint(pt: Point) {
        this.points.add(pt)
    }

    /**
     * Get all points from current pack.
     */
    fun getPoints(): Array<Point> {
        return points.toTypedArray()
    }

    /**
     * Create new pack with defined name.
     *
     * @param uniqueName name of pack
     */
    constructor(uniqueName: String) : this() {
        this.name = uniqueName
    }

    //*************************************************
    // STORABLE
    //*************************************************

    override fun getVersion(): Int {
        return 0
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        name = dr.readString()
        if (dr.readBoolean()) {
            extraStyle = GeoDataStyle().apply { read(dr) }
        }
        bitmap = UtilsBitmap.readBitmap(dr)
        points.clear()
        points.addAll(dr.readListStorable(Point::class.java))
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeString(name)
        if (extraStyle == null) {
            dw.writeBoolean(false)
        } else {
            dw.writeBoolean(true)
            dw.writeStorable(extraStyle!!)
        }
        UtilsBitmap.writeBitmap(dw, bitmap, Bitmap.CompressFormat.PNG)
        dw.writeListStorable(points)
    }
}
