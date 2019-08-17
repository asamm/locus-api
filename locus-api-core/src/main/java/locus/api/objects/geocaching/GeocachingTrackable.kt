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

class GeocachingTrackable : Storable() {

    // PARAMETERS

    /**
     * ID of trackable (currently used for GeoKrety web server).
     */
    var id: Long = 0L
    /**
     * Name of travel bug.
     */
    var name: String = ""
    /**
     * Image url to this travel bug.
     */
    var imgUrl: String = ""
    /**
     * URL to trackable object. This is very important value, because URL contain TBCode, like this
     * http://www.geocaching.com/track/details.aspx?tracker=TB4342X
     */
    var srcDetails: String = ""
    /**
     * Original owner of TB.
     */
    var originalOwner: String = ""
    /**
     * Current owner of TB.
     */
    var currentOwner: String = ""
    /**
     * Time of release to public (long since 1.1.1970 in ms).
     */
    var released: Long = 0L
    /**
     * Defined origin location for trackable.
     */
    var origin: String = ""
    /**
     * Goal of this TB.
     */
    var goal: String = ""
    /**
     * Extra details.
     */
    var details: String = ""

    //*************************************************
    // OTHER METHODS
    //*************************************************

    val tbCode: String
        get() {
            if (srcDetails.isEmpty()) {
                return ""
            }
            var searchText = "://www.geocaching.com/track/details.aspx?tracker="
            if (srcDetails.indexOf(searchText) > 0) {
                return srcDetails.substring(srcDetails.indexOf(searchText) + searchText.length)
            }
            searchText = "://coord.info/"
            return if (srcDetails.indexOf(searchText) > 0) {
                srcDetails.substring(srcDetails.indexOf(searchText) + searchText.length)
            } else ""
        }

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 1
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        name = dr.readString()
        imgUrl = dr.readString()
        srcDetails = dr.readString()
        originalOwner = dr.readString()
        released = dr.readLong()
        origin = dr.readString()
        goal = dr.readString()
        details = dr.readString()

        // V1
        if (version >= 1) {
            id = dr.readLong()
            currentOwner = dr.readString()
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeString(name)
        dw.writeString(imgUrl)
        dw.writeString(srcDetails)
        dw.writeString(originalOwner)
        dw.writeLong(released)
        dw.writeString(origin)
        dw.writeString(goal)
        dw.writeString(details)

        // V1
        dw.writeLong(id)
        dw.writeString(currentOwner)
    }
}