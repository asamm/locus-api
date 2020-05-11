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

import locus.api.objects.Storable
import locus.api.objects.extra.PointRteAction
import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.styles.GeoDataStyle
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import locus.api.utils.Logger
import locus.api.utils.Utils
import java.io.IOException
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

abstract class GeoData : Storable() {

    /**
     * Define state of item (read-only/writable)
     */
    enum class ReadWriteMode {

        /**
         * Items is in read-only state
         */
        READ_ONLY,
        /**
         * Item is in read-write state
         */
        READ_WRITE
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
     * Current item state.
     */
    private var state: Byte = 0
    /**
     * Define read-write mode of item.
     */
    var readWriteMode = ReadWriteMode.READ_WRITE

    // TEMPORARY PARAMETERS

    /**
     * Additional temporary storage object. Object is not serialized!
     */
    var tag: Any? = null
    private var tags: Hashtable<String, Any>? = null
    /**
     * Temporary variable for sorting.
     */
    var dist: Int = 0

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
                } ?: {
            dw.writeBoolean(false)
        }()
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
                Logger.logE(TAG, "getExtraDataRaw()", e)
                null
            }

        }
        set(data) = try {
            readExtraData(DataReaderBigEndian(data))
        } catch (e: Exception) {
            Logger.logE(TAG, "setExtraDataRaw($data)", e)
            extraData = null
        }

    //*************************************************
    // EXTRA DATA - PARAMETERS
    //*************************************************

    /**
     * Check if container has any extra data parameters.
     */
    fun hasExtraData(): Boolean {
        return extraData != null
    }

    /**
     * Check if extra data exists and if not, create it.
     *
     * @return `true` if container was created
     */
    private fun createExtraData(): Boolean {
        return if (extraData == null) {
            extraData = GeoDataExtra()
            true
        } else {
            false
        }
    }

    /**
     * Do some post-process work after item is added to container.
     *
     * @param added   `true` if item was added
     * @param created `true` if extra data were created before
     * @return `true` if all went well and item is stored
     */
    private fun afterItemAdded(added: Boolean, created: Boolean): Boolean {
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
     * @param param text value of parameter
     */
    fun addParameter(paramId: Int, param: String?): Boolean {
        // check extra data
        val created = createExtraData()

        // add parameter and return result
        return afterItemAdded(extraData!!.addParameter(paramId, param), created)
    }

    /**
     * Add single parameter defined by it's ID into `extraData` container.
     *
     * @param paramId ID of parameter
     * @param param value of parameter
     */
    fun addParameter(paramId: Int, param: ByteArray): Boolean {
        // check extra data
        val created = createExtraData()

        // add parameter and return result
        return afterItemAdded(extraData!!.addParameter(paramId, param), created)
    }

    /**
     * Add single parameter defined by it's ID into `extraData` container.
     *
     * @param paramId ID of parameter
     * @param param value of parameter
     */
    fun addParameter(paramId: Int, value: Int): Boolean {
        // check extra data
        val created = createExtraData()

        // add parameter and return result
        return afterItemAdded(extraData!!.addParameter(paramId, Integer.toString(value)), created)
    }

    /**
     * Add single parameter defined by it's ID into `extraData` container.
     *
     * @param paramId ID of parameter
     * @param param value of parameter
     */
    fun addParameter(paramId: Int, param: Byte): Boolean {
        // check extra data
        val created = createExtraData()

        // add parameter and return result
        return afterItemAdded(extraData!!.addParameter(paramId, param), created)
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

    fun removeParameter(paramId: Int): String? {
        return extraData?.removeParameter(paramId)
    }

    //*************************************************
    // EXTRA DATA - ATTACHMENTS
    //*************************************************

    /**
     * Add "email" to current object.
     *
     * @param email email to add
     */
    fun addEmail(email: String) {
        addEmail(null, email)
    }

    /**
     * Add "email" to current object.
     *
     * @param label label visible in app
     * @param email email to add
     */
    fun addEmail(label: String?, email: String) {
        // check extra data
        val created = createExtraData()

        // add parameter
        afterItemAdded(extraData!!.addAttachment(GeoDataExtra.AttachType.EMAIL, label, email), created)
    }

    // PARAMETER 'PHONE'

    fun addPhone(phone: String) {
        addPhone(null, phone)
    }

    fun addPhone(label: String?, phone: String) {
        // check extra data
        val created = createExtraData()

        // add parameter
        afterItemAdded(extraData!!.addAttachment(GeoDataExtra.AttachType.PHONE, label, phone), created)
    }

    // PARAMETER 'URL'

    fun addUrl(url: String) {
        addUrl(null, url)
    }

    fun addUrl(label: String?, url: String) {
        // check extra data
        val created = createExtraData()

        // add parameter
        afterItemAdded(extraData!!.addAttachment(GeoDataExtra.AttachType.URL, label, url), created)
    }

    // PARAMETER 'AUDIO' ATTACHMENT

    /**
     * Add audio to current object.
     *
     * @param uri uri to add
     * @return `true` if audio was correctly added
     */
    fun addAttachmentAudio(uri: String): Boolean {
        // check extra data
        val created = createExtraData()

        // add parameter and return result
        return afterItemAdded(extraData!!.addAttachment(GeoDataExtra.AttachType.AUDIO, "", uri), created)
    }

    // PARAMETER 'PHOTO' ATTACHMENT

    /**
     * Add photo to current object.
     *
     * @param uri uri to add
     * @return `true` if photo was correctly added
     */
    fun addAttachmentPhoto(uri: String): Boolean {
        // check extra data
        val created = createExtraData()

        // add parameter and return result
        return afterItemAdded(extraData!!.addAttachment(GeoDataExtra.AttachType.PHOTO, "", uri), created)
    }

    // PARAMETER 'VIDEO' ATTACHMENT

    /**
     * Add video to current object.
     *
     * @param uri uri to add
     * @return `true` if video was correctly added
     */
    fun addAttachmentVideo(uri: String): Boolean {
        // check extra data
        val created = createExtraData()

        // add parameter and return result
        return afterItemAdded(extraData!!.addAttachment(GeoDataExtra.AttachType.VIDEO, "", uri), created)
    }

    // PARAMETER 'OTHER' ATTACHMENT

    /**
     * Add video to current object.
     *
     * @param uri uri to add
     * @return `true` if video was correctly added
     */
    fun addAttachmentOther(uri: String): Boolean {
        // check extra data
        val created = createExtraData()

        // add parameter and return result
        return afterItemAdded(extraData!!.addAttachment(GeoDataExtra.AttachType.OTHER, "", uri), created)
    }

    //*************************************************
    // EXTRA DATA - SHORTCUTS
    //*************************************************

    // PARAMETER "SOURCE"

    /**
     * Current defined source parameter.
     *
     * @return source parameter or 'SOURCE_UNKNOWN' if not defined
     */
    var parameterSource: Byte
        get() {
            return extraData?.getParameterRaw(GeoDataExtra.PAR_SOURCE)
                    ?.takeIf { it.size == 1 }
                    ?.let { it[0] }
                    ?: GeoDataExtra.SOURCE_UNKNOWN
        }
        set(source) {
            addParameter(GeoDataExtra.PAR_SOURCE, source)
        }

    /**
     * Check if waypoint source parameter is equal to expected value.
     *
     * @param expectedSource source we are checking
     * @return `true` if waypoint source if same as expected
     */
    fun isParameterSource(expectedSource: Byte): Boolean {
        return parameterSource == expectedSource
    }

    /**
     * Remove existing source parameter.
     */
    fun removeParameterSource() {
        removeParameter(GeoDataExtra.PAR_SOURCE)
    }

    // PARAMETER 'STYLE'

    var parameterStyleName: String
        get() {
            return extraData?.getParameter(GeoDataExtra.PAR_STYLE_NAME)
                    ?: ""
        }
        set(value) {
            addParameter(GeoDataExtra.PAR_STYLE_NAME, value)
        }

    fun removeParameterStyleName() {
        extraData?.removeParameter(GeoDataExtra.PAR_STYLE_NAME)
    }

    // PARAMETER DESCRIPTION

    /**
     * Description parameter attached to this object.
     */
    var parameterDescription: String
        get() {
            return extraData?.getParameterNotNull(GeoDataExtra.PAR_DESCRIPTION)
                    ?: ""
        }
        set(value) {
            addParameter(GeoDataExtra.PAR_DESCRIPTION, value)
        }

    /**
     * Check if item has description parameter.
     *
     * @return `true` if any description exists
     */
    fun hasParameterDescription(): Boolean {
        return parameterDescription.isNotEmpty()
    }

    // RTE BASED PARAMETERS

    /**
     * RTE action defined for current object.
     */
    var parameterRteAction: PointRteAction
        get() = getParameter(GeoDataExtra.PAR_RTE_POINT_ACTION)
                ?.takeIf { it.isNotEmpty() }
                ?.let { PointRteAction.getActionById(Utils.parseInt(it)) }
                ?: PointRteAction.UNDEFINED
        set(value) {
            addParameter(GeoDataExtra.PAR_RTE_POINT_ACTION, value.id)
        }

    /**
     * Get parameter that define index of point in track (index of trackpoint).
     *
     * @return index in track or '-1' if no index is defined
     */
    val paramRteIndex: Int
        get() = getParameter(GeoDataExtra.PAR_RTE_INDEX)
                ?.let { Utils.parseInt(it) }
                ?: -1

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
        } ?: {
            dw.writeBoolean(false)
        }()
        styleHighlight?.let {
            dw.writeBoolean(true)
            dw.writeStorable(it)
        } ?: {
            dw.writeBoolean(false)
        }()
    }

    var styles: ByteArray?
        get() = try {
            DataWriterBigEndian().apply {
                writeStyles(this)
            }.toByteArray()
        } catch (e: IOException) {
            Logger.logE(TAG, "getStylesRaw()", e)
            null
        }
        set(data) = try {
            readStyles(DataReaderBigEndian(data))
        } catch (e: Exception) {
            Logger.logE(TAG, "setExtraStyle($data)", e)
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
    fun getTag(key: String): Any? {
        // check key
        if (key.isEmpty()) {
            Logger.logW(TAG, "getTag(" + key + "), " +
                    "invalid key")
            return null
        }

        // get tag
        return tags?.get(key)
    }

    /**
     * Get all attached keys for tags.
     */
    fun getTagsKeys(): Array<String> {
        return tags?.keys?.toTypedArray()
                ?: emptyArray()
    }

    /**
     * Set new tag/object defined by key.
     *
     * @param key key that define object
     * @param value object itself or 'null' if we wants to remove it
     */
    fun setTag(key: String, value: Any?) {
        // check key
        if (key.isEmpty()) {
            Logger.logW(TAG, "setTag(" + key + "), " +
                    "invalid key")
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

    companion object {

        // tag for logger
        private const val TAG = "GeoData"
    }
}