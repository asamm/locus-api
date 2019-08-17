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

class GeocachingWaypoint : Storable() {

    // PARAMETERS

    /**
     * Code of wpt.
     */
    var code: String = ""
    /**
     * Name of waypoint.
     */
    var name: String = ""
    /**
     * Description (may be HTML code).
     */
    var desc: String = ""
    /**
     * Flag if description was already modified by user.
     */
    var isDescModified: Boolean = false
    /**
     * Type of waypoint (defined above as 'CACHE_WAYPOINT_TYPE_...).
     */
    var type: String = ""
        set(value) {
            if (value.toLowerCase().startsWith("waypoint|")) {
                field = value.substring("waypoint|".length)
            } else {
                field = value
            }
        }
    /**
     * Image URL to this wpt (optional).
     */
    var typeImagePath: String = ""
    /**
     * Longitude of waypoint.
     */
    var lon: Double = 0.0
    /**
     * Latitude of waypoint.
     */
    var lat: Double = 0.0

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 1
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        code = dr.readString()
        name = dr.readString()
        desc = dr.readString()
        type = dr.readString()
        typeImagePath = dr.readString()
        lon = dr.readDouble()
        lat = dr.readDouble()

        // V1
        if (version >= 1) {
            isDescModified = dr.readBoolean()
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeString(code)
        dw.writeString(name)
        dw.writeString(desc)
        dw.writeString(type)
        dw.writeString(typeImagePath)
        dw.writeDouble(lon)
        dw.writeDouble(lat)

        // V1
        dw.writeBoolean(isDescModified)
    }

    companion object {

        /*
         * Changes:
         * May 2014 - due to changes on GroundSpeak side, two Wpts are now renamed.
         * more: http://support.groundspeak.com/index.php?pg=kb.page&id=72
         */
        @Deprecated("")
        const val CACHE_WAYPOINT_TYPE_QUESTION = "Question to Answer"
        const val CACHE_WAYPOINT_TYPE_VIRTUAL_STAGE = "Virtual Stage"
        const val CACHE_WAYPOINT_TYPE_FINAL = "Final Location"
        const val CACHE_WAYPOINT_TYPE_PARKING = "Parking Area"
        const val CACHE_WAYPOINT_TYPE_TRAILHEAD = "Trailhead"
        @Deprecated("")
        const val CACHE_WAYPOINT_TYPE_STAGES = "Stages of a Multicache"
        const val CACHE_WAYPOINT_TYPE_PHYSICAL_STAGE = "Physical Stage"
        const val CACHE_WAYPOINT_TYPE_REFERENCE = "Reference Point"
    }
}