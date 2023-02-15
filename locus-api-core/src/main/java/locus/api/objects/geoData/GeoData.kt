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
import com.asamm.loggerV2.logW
import locus.api.objects.Storable
import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.styles.GeoDataStyle
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

abstract class GeoData : Storable() {

    /**
     * Privacy settings for certain item.
     */
    enum class Privacy {

        /**
         * Item is private only.
         */
        PRIVATE,

        /**
         * Item is public thanks to public URL.
         */
        SHARED_URL
    }

    // STORABLE PARAMETERS

    /**
     * Unique ID of this object.
     */
    var id: Long = -1L

    /**
     * Name of object, have to be unique
     */
    var name: String = ""

    /**
     * Time the data was created (ms).
     */
    var timeCreated: Long = System.currentTimeMillis()

    /**
     * Time the data was updated for the last time (ms).
     */
    var timeUpdated: Long = timeCreated

    /**
     * Extra data with parameters.
     */
    var extraData: GeoDataExtra? = null

    /**
     * Style for normal state.
     */
    var styleNormal: GeoDataStyle? = null

    /**
     * Style for highlight state.
     */
    var styleHighlight: GeoDataStyle? = null

    /**
     * Define item privacy mode.
     */
    var privacy = Privacy.PRIVATE

    /**
     * Define read-write mode of item. Protected item should not be exported from the Locus World
     * or shared publicly to any place out of Locus scope.
     */
    var protected = false

    // TEMPORARY PARAMETERS
    // not serialized!

    /**
     * Current item state.
     */
    private var state: Byte = 0

    /**
     * Additional temporary storage object. Object is not serialized so transport over API does not work!
     */
    private var tags: Hashtable<String, Any>? = null

    //*************************************************
    // STATE
    //*************************************************

    /**
     * Item enabled/disabled state.
     * "Enabled" state means item will be handled in lifecycle of drawing etc.
     */
    var isEnabled: Boolean
        get() = state and 1.toByte() == 1.toByte()
        set(enabled) = setState(0, enabled)

    /**
     * Flag if item is currently visible (this means enabled and visible), so it should be drawn on the map.
     */
    var isVisible: Boolean
        get() = state and 1.toByte() == 1.toByte() // enabled
                && (state.toInt() shr 1).toByte() and 1.toByte() == 1.toByte() // visible
        set(visible) = setState(1, visible)

    /**
     * Flag if item is currently selected (this means enabled, visible and selected).
     */
    var isSelected: Boolean
        get() = (state and 1.toByte() == 1.toByte() // enabled
                && state.toInt() shr 1 and 1 == 1 // visible
                && state.toInt() shr 2 and 1 == 1) // selected
        set(selected) = setState(2, selected)

    init {
        isEnabled = true
        isVisible = true
        isSelected = false
    }

    /**
     * Set certain state in byte.
     *
     * @param position position of state
     * @param value    value of state
     */
    private fun setState(position: Int, value: Boolean) {
        if (value) {
            this.state = this.state or (1 shl position).toByte()
        } else {
            this.state = this.state and (1 shl position).inv().toByte()
        }
    }

    //*************************************************
    // EXTRA DATA - BASE
    //*************************************************

    @Throws(IOException::class)
    protected fun readExtraData(dr: DataReaderBigEndian) {
        if (dr.readBoolean()) {
            extraData = GeoDataExtra().apply { read(dr) }
        }
    }

    @Throws(IOException::class)
    protected fun writeExtraData(dw: DataWriterBigEndian) {
        extraData
            ?.takeIf { it.count > 0 }
            ?.let {
                dw.writeBoolean(true)
                dw.writeStorable(it)
            } ?: run {
            dw.writeBoolean(false)
        }
    }

    /**
     * Extra data serialized into byte array.
     *
     * @return serialized extra data or 'null' if data doesn't exists
     */
    var extraDataRaw: ByteArray?
        get() {
            return try {
                val dw = DataWriterBigEndian()
                writeExtraData(dw)
                dw.toByteArray()
            } catch (e: IOException) {
                logE(tag = TAG, ex = e) { "getExtraDataRaw()" }
                null
            }

        }
        set(data) = try {
            readExtraData(DataReaderBigEndian(data))
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "setExtraDataRaw($data)" }
            extraData = null
        }

    //*************************************************
    // EXTRA DATA - PARAMETERS
    //*************************************************

    /**
     * Add parameter into attached [extraData] container.
     * In case, container does not exists, it is created and also validated after "add" operation.
     *
     * @param addEvent event that fill [extraData] container
     * @return `true` in case, parameter was correctly added
     */
    internal fun addParameter(addEvent: GeoDataExtra.() -> Boolean): Boolean {
        // check extra data and create if does not exists
        val created = if (extraData == null) {
            extraData = GeoDataExtra()
            true
        } else {
            false
        }

        // add parameter
        val added = addEvent(extraData!!)

        // add parameter and return result
        return if (added) {
            true
        } else {
            if (created) {
                extraData = null
            }
            false
        }
    }

    /**
     * Add single parameter defined by it's ID into `extraData` container.
     *
     * @param paramId ID of parameter
     * @param param value of parameter
     */
    fun addParameter(paramId: Int, param: Byte): Boolean {
        return addParameter {
            addParameter(paramId, param)
        }
    }

    /**
     * Add single parameter defined by it's ID into `extraData` container.
     *
     * @param paramId ID of parameter
     * @param param value of parameter
     */
    fun addParameter(paramId: Int, param: ByteArray): Boolean {
        return addParameter {
            addParameter(paramId, param)
        }
    }

    /**
     * Return parameter stored in extraData container.
     *
     * @param paramId ID of parameter
     * @return loaded value (length() bigger then 0) or 'null' in case, parameter do not exists
     */
    fun getParameter(paramId: Int): String? {
        return extraData?.getParameter(paramId)
    }

    /**
     * Add single parameter defined by it's ID into `extraData` container.
     *
     * @param paramId ID of parameter
     * @param param text value of parameter
     */
    fun addParameter(paramId: Int, param: String?): Boolean {
        return addParameter {
            addParameter(paramId, param)
        }
    }

    /**
     * Get parameter in raw original format.
     *
     * @param paramId ID of parameter
     * @return parameter value in byte[] or `null` if no such parameter is stored
     */
    fun getParameterRaw(paramId: Int): ByteArray? {
        return extraData?.getParameterRaw(paramId)
    }

    /**
     * Check if current object has parameter defined by it's ID.
     *
     * @param paramId ID of parameter
     * @return `true` if non-empty parameter exists
     */
    fun hasParameter(paramId: Int): Boolean {
        return extraData?.hasParameter(paramId) ?: false
    }

    /**
     * Remove parameter from storage.
     *
     * @param paramId ID of parameter
     * @return parameter value
     */
    fun removeParameter(paramId: Int): String? {
        return extraData?.removeParameter(paramId)
    }

    //*************************************************
    // STYLES
    //*************************************************

    @Throws(IOException::class)
    protected fun readStyles(dr: DataReaderBigEndian) {
        if (dr.readBoolean()) {
            styleNormal = GeoDataStyle().apply { read(dr) }
        }
        if (dr.readBoolean()) {
            styleHighlight = GeoDataStyle().apply { read(dr) }
        }
    }

    @Throws(IOException::class)
    protected fun writeStyles(dw: DataWriterBigEndian) {
        styleNormal?.let {
            dw.writeBoolean(true)
            dw.writeStorable(it)
        } ?: run {
            dw.writeBoolean(false)
        }
        styleHighlight?.let {
            dw.writeBoolean(true)
            dw.writeStorable(it)
        } ?: run {
            dw.writeBoolean(false)
        }
    }

    var styles: ByteArray?
        get() = try {
            DataWriterBigEndian().apply {
                writeStyles(this)
            }.toByteArray()
        } catch (e: IOException) {
            logE(tag = TAG, ex = e) { "getStylesRaw()" }
            null
        }
        set(data) = try {
            readStyles(DataReaderBigEndian(data))
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "setExtraStyle($data)" }
            extraData = null
        }

    //*************************************************
    // TAGS
    //*************************************************

    /**
     * Get tag attached to object, defined by "key".
     *
     * @param key key value that defined object
     * @return required object otherwise 'null'
     */
    @JvmOverloads
    fun getTag(key: String = TAG_KEY_DEFAULT): Any? {
        // check key
        if (key.isEmpty()) {
            logW(tag = TAG) { "getTag($key), invalid key" }
            return null
        }

        // get tag
        return tags?.get(key)
    }

    /**
     * Set new tag/object under base key.
     */
    fun setTag(value: Any?) {
        setTag(key = TAG_KEY_DEFAULT, value = value)
    }

    /**
     * Set new tag/object defined by key.
     *
     * @param key key that define object
     * @param value object itself or 'null' if we wants to remove it
     */
    fun setTag(key: String = TAG_KEY_DEFAULT, value: Any?) {
        // check key
        if (key.isEmpty()) {
            logW(tag = TAG) { "setTag($key), invalid key" }
            return
        }

        // set tag
        if (value == null) {
            tags?.remove(key)
        } else {
            if (tags == null) {
                tags = Hashtable()
            }
            tags!![key] = value
        }
    }

    /**
     * Get all attached keys for tags.
     */
    fun getTagsKeys(): Array<String> {
        return tags?.keys?.toTypedArray()
            ?: emptyArray()
    }

    companion object {

        // tag for logger
        private const val TAG = "GeoData"

        // default key for "empty" tag value
        private const val TAG_KEY_DEFAULT = "default"
    }
}
