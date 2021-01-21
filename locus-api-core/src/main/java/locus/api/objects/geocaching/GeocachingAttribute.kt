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

import java.io.IOException
import java.util.Hashtable

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian

/**
 * Class for holding geocaching attributes
 * <br></br><br></br>
 * Every instance holds just one attribute, defined by it's unique ID number. If
 * you want to set correct value, use constructor that allow set directly by number
 * or by attribute URL
 * <br></br><br></br>
 *
 * @author menion
 */
class GeocachingAttribute() : Storable() {

    /**
     * Current ID of attribute. This ID also contains positive/negative flag.
     */
    var id: Int = -1

    /**
     * Real ID of attribute that match ID's used by Groundspeak.
     */
    val idReal: Int
        get() = id % 100

    /**
     * Is current attribute value positive.
     */
    val isPositive: Boolean
        get() = id > 100

    init {
        id = -1
    }

    /**
     * Create attribute container.
     *
     * @param id       ID of attribute
     * @param positive `true` to make attribute positive
     */
    constructor(id: Int, positive: Boolean) : this() {
        if (!positive) {
            this.id = id
        } else {
            this.id = id + 100
        }
    }

    /**
     * Create attribute container based on certain uri value.
     *
     * @param url url value
     */
    constructor(url: String) : this() {
        if (url.isNotEmpty()) {
            val imgName = url.substring(url.lastIndexOf("/" + 1), url.lastIndexOf("-"))
            id = attrIds[imgName]
                    ?: return
            if (url.contains("-yes.")) {
                id += 100
            }
        }
    }

    //*************************************************/
    // STORABLE PART
    //*************************************************/

    override fun getVersion(): Int {
        return 0
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readInt()
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeInt(id)
    }

    companion object {

        private val attrIds by lazy {
            Hashtable<String, Int>().apply {
                this["dogs"] = 1
                this["fee"] = 2
                this["rappelling"] = 3
                this["boat"] = 4
                this["scuba"] = 5
                this["kids"] = 6
                this["onehour"] = 7
                this["scenic"] = 8
                this["hiking"] = 9
                this["climbing"] = 10
                this["wading"] = 11
                this["swimming"] = 12
                this["available"] = 13
                this["night"] = 14
                this["winter"] = 15
                //this.put("camping", 16);
                this["poisonoak"] = 17
                this["snakes"] = 18
                this["ticks"] = 19
                this["mine"] = 20
                this["cliff"] = 21
                this["hunting"] = 22
                this["danger"] = 23
                this["wheelchair"] = 24
                this["parking"] = 25
                this["public"] = 26
                this["water"] = 27
                this["restrooms"] = 28
                this["phone"] = 29
                this["picnic"] = 30
                this["camping"] = 31
                this["bicycles"] = 32
                this["motorcycles"] = 33
                this["quads"] = 34
                this["jeeps"] = 35
                this["snowmobiles"] = 36
                this["horses"] = 37
                this["campfires"] = 38
                this["thorn"] = 39
                this["stealth"] = 40
                this["stroller"] = 41
                this["firstaid"] = 42
                this["cow"] = 43
                this["flashlight"] = 44
                this["landf"] = 45
                this["rv"] = 46
                this["field_puzzle"] = 47
                this["UV"] = 48
                this["snowshoes"] = 49
                this["skiis"] = 50
                this["s-tool"] = 51
                this["nightcache"] = 52
                this["parkngrab"] = 53
                this["AbandonedBuilding"] = 54
                this["hike_short"] = 55
                this["hike_med"] = 56
                this["hike_long"] = 57
                this["fuel"] = 58
                this["food"] = 59
                this["wirelessbeacon"] = 60
                this["partnership"] = 61
                this["seasonal"] = 62
                this["touristOK"] = 63
                this["treeclimbing"] = 64
                this["frontyard"] = 65
                this["teamwork"] = 66
                this["geotour"] = 67
                this["bonus"] = 69
                this["power"] = 70
                this["challenge"] = 71
                this["checker"] = 72
            }
        }
    }
}
