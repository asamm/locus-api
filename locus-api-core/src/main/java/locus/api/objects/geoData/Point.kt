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

import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.extra.Location
import locus.api.objects.geocaching.GeocachingData
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import locus.api.utils.Logger
import java.io.IOException

class Point() : GeoData() {

    /**
     * Location of current point.
     */
    var location: Location = Location()

    /**
     * Additional geoCaching data
     */
    var gcData: GeocachingData? = null

    // EXTRA CALLBACK

    val extraCallback: String?
        get() = getParameter(GeoDataExtra.PAR_INTENT_EXTRA_CALLBACK)

    val extraOnDisplay: String?
        get() = getParameter(GeoDataExtra.PAR_INTENT_EXTRA_ON_DISPLAY)

    // GEOCACHING DATA

    var geocachingData: ByteArray?
        get() {
            return try {
                val dw = DataWriterBigEndian()
                writeGeocachingData(dw)
                dw.toByteArray()
            } catch (e: IOException) {
                Logger.logE(TAG, "getGeocachingData()", e)
                null
            }
        }
        set(data) = try {
            gcData = readGeocachingData(DataReaderBigEndian(data))
        } catch (e: Exception) {
            Logger.logE(TAG, "setGeocachingData($data)", e)
            gcData = null
        }

    /**
     * Create point with known name and it's location.
     */
    constructor(name: String, loc: Location) : this() {
        this.name = name
        this.location = loc
    }

    /**
     * Simply allow set callback value on point. This appear when you click on point
     * and then under last button will be your button. Clicking on it, launch by you,
     * defined intent
     * <br></br><br></br>
     * Do not forget to set this http://developer.android.com/guide/topics/manifest/activity-element.html#exported
     * to your activity, if you'll set callback to other then launcher activity
     *
     * @param btnName         Name displayed on button
     * @param packageName     this value is used for creating intent that
     * will be called in callback (for example com.super.application)
     * @param className       the name of the class inside of com.super.application
     * that implements the component (for example com.super.application.Main)
     * @param returnDataName  String under which data will be stored. Can be
     * retrieved by String data = getIntent.getStringExtra("returnData");
     * @param returnDataValue String under which data will be stored. Can be
     * retrieved by String data = getIntent.getStringExtra("returnData");
     */
    fun setExtraCallback(btnName: String, packageName: String, className: String,
            returnDataName: String, returnDataValue: String) {
        // prepare callback
        val callBack = GeoDataExtra.generateCallbackString(btnName, packageName, className,
                returnDataName, returnDataValue)
        if (callBack.isEmpty()) {
            return
        }

        // generate final text
        val b = StringBuilder()
        b.append(TAG_EXTRA_CALLBACK).append(";")
        b.append(callBack)

        // finally insert parameter
        addParameter(GeoDataExtra.PAR_INTENT_EXTRA_CALLBACK, b.toString())
    }

    /**
     * If you want to remove PAR_INTENT_EXTRA_CALLBACK parametr from Locus database,
     * you need to send "clear" value in updated waypoint back to Locus. After that,
     * Locus will remove this parameter from new stored point.
     * <br></br><br></br>
     * Second alternative, how to remove this callback, is to send new waypoints
     * with forceOverwrite parameter set to `true`, that will overwrite
     * completely all data
     */
    fun removeExtraCallback() {
        addParameter(GeoDataExtra.PAR_INTENT_EXTRA_CALLBACK, "clear")
    }

    /**
     * Extra feature that allow to send to locus only partial point data. When you click on
     * point (in time when small point dialog should appear), locus send intent to your app,
     * you can then fill complete point and send it back to Locus. Clear and clever
     * <br></br><br></br>
     * Do not forget to set this http://developer.android.com/guide/topics/manifest/activity-element.html#exported
     * to your activity, if you'll set callback to other then launcher activity
     *
     * @param packageName     this value is used for creating intent that
     * will be called in callback (for example com.super.application)
     * @param className       the name of the class inside of com.super.application
     * that implements the component (for example com.super.application.Main)
     * @param returnDataName  String under which data will be stored. Can be
     * retrieved by String data = getIntent.getStringExtra("returnData");
     * @param returnDataValue value that will be received when you try to get
     * data from received response
     */
    fun setExtraOnDisplay(packageName: String, className: String,
            returnDataName: String, returnDataValue: String) {
        val sb = StringBuilder()
        sb.append(TAG_EXTRA_ON_DISPLAY).append(";")
        sb.append(packageName).append(";")
        sb.append(className).append(";")
        sb.append(returnDataName).append(";")
        sb.append(returnDataValue).append(";")
        addParameter(GeoDataExtra.PAR_INTENT_EXTRA_ON_DISPLAY, sb.toString())
    }

    /**
     * If you want to remove PAR_INTENT_EXTRA_ON_DISPLAY parameter from Locus database,
     * you need to send "clear" value in updated waypoint back to Locus. After that,
     * Locus will remove this parameter from new stored point.
     * <br></br><br></br>
     * Second alternative, how to remove this callback, is to send new waypoints
     * with forceOverwrite parameter set to `true`, that will overwrite
     * completely all data
     */
    fun removeExtraOnDisplay() {
        addParameter(GeoDataExtra.PAR_INTENT_EXTRA_ON_DISPLAY, "clear")
    }

    //*************************************************
    // STORABLE
    //*************************************************

    override fun getVersion(): Int {
        return 3
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readLong()
        name = dr.readString()
        location = Location().apply { read(dr) }

        // read extra data
        readExtraData(dr)
        readStyles(dr)

        // read geocaching
        gcData = readGeocachingData(dr)

        // V1
        if (version >= 1) {
            timeCreated = dr.readLong()
        }

        // V2
        if (version >= 2) {
            readWriteMode = ReadWriteMode.values()[dr.readInt()]
        }

        // V3
        if (version >= 3) {
            timeUpdated = dr.readLong()
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeLong(id)
        dw.writeString(name)
        location.write(dw)

        // write extra data
        writeExtraData(dw)
        writeStyles(dw)

        // write geocaching data
        writeGeocachingData(dw)

        // V1
        dw.writeLong(timeCreated)

        // V2
        dw.writeInt(readWriteMode.ordinal)

        // V3
        dw.writeLong(timeUpdated)
    }

    @Throws(IOException::class)
    private fun writeGeocachingData(dw: DataWriterBigEndian) {
        gcData?.let {
            dw.writeBoolean(true)
            it.write(dw)
        } ?: {
            dw.writeBoolean(false)
        }()
    }

    companion object {

        // tag for logger
        private const val TAG = "Point"

        // callback parameter
        const val TAG_EXTRA_CALLBACK = "TAG_EXTRA_CALLBACK"

        // extra on-display parameter
        const val TAG_EXTRA_ON_DISPLAY = "TAG_EXTRA_ON_DISPLAY"

        @Throws(IOException::class)
        fun readGeocachingData(dr: DataReaderBigEndian): GeocachingData? {
            return if (dr.readBoolean()) {
                GeocachingData().apply { read(dr) }
            } else {
                null
            }
        }
    }
}
