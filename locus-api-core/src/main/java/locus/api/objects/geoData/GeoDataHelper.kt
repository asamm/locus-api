@file:Suppress("unused")

package locus.api.objects.geoData

import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.extra.PointRteAction
import locus.api.utils.Utils

//*************************************************
// PRIVATE REFERENCES (0 - 29)
//*************************************************

// PAR_SOURCE

/**
 * Current defined source parameter.
 *
 * @return source parameter or 'SOURCE_UNKNOWN' if not defined
 */
var GeoData.parameterSource: Byte
    get() {
        return getParameterRaw(GeoDataExtra.PAR_SOURCE)
            ?.takeIf { it.size == 1 }
            ?.let { it[0] }
            ?: GeoDataExtra.SOURCE_UNKNOWN
    }
    set(source) {
        if (source != GeoDataExtra.SOURCE_UNKNOWN) {
            addParameter(GeoDataExtra.PAR_SOURCE, source)
        } else {
            removeParameter(GeoDataExtra.PAR_SOURCE)
        }
    }

/**
 * Check if waypoint source parameter is equal to expected value.
 *
 * @param expectedSource source we are checking
 * @return `true` if waypoint source if same as expected
 */
fun GeoData.isParameterSource(expectedSource: Byte): Boolean {
    return parameterSource == expectedSource
}

// PAR_STYLE_NAME

var GeoData.parameterStyleName: String
    get() {
        return getParameter(GeoDataExtra.PAR_STYLE_NAME)
            ?: ""
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_STYLE_NAME, value)
    }

fun GeoData.removeParameterStyleName() {
    removeParameter(GeoDataExtra.PAR_STYLE_NAME)
}

//*************************************************
// PUBLIC VALUES (30 - 49)
//*************************************************

// PAR_DESCRIPTION

/**
 * Description parameter attached to this object.
 */
var GeoData.parameterDescription: String
    get() {
        return getParameter(GeoDataExtra.PAR_DESCRIPTION)
            ?: ""
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_DESCRIPTION, value)
    }

fun GeoData.removeParameterDescription() {
    removeParameter(GeoDataExtra.PAR_DESCRIPTION)
}

// PAR_COMMENT

var GeoData.parameterComment: String
    get() {
        return getParameter(GeoDataExtra.PAR_COMMENT)
            ?: ""
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_COMMENT, value)
    }

// PAR_ADDRESS_STREET

var GeoData.parameterAddressStreet: String
    get() {
        return getParameter(GeoDataExtra.PAR_ADDRESS_STREET)
            ?: ""
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_ADDRESS_STREET, value)
    }

// PAR_ADDRESS_CITY

var GeoData.parameterAddressCity: String
    get() {
        return getParameter(GeoDataExtra.PAR_ADDRESS_CITY)
            ?: ""
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_ADDRESS_CITY, value)
    }

// PAR_ADDRESS_REGION

var GeoData.parameterAddressRegion: String
    get() {
        return getParameter(GeoDataExtra.PAR_ADDRESS_REGION)
            ?: ""
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_ADDRESS_REGION, value)
    }

// PAR_ADDRESS_POST_CODE

var GeoData.parameterAddressPostCode: String
    get() {
        return getParameter(GeoDataExtra.PAR_ADDRESS_POST_CODE)
            ?: ""
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_ADDRESS_POST_CODE, value)
    }

// PAR_ADDRESS_COUNTRY

var GeoData.parameterAddressCountry: String
    get() {
        return getParameter(GeoDataExtra.PAR_ADDRESS_COUNTRY)
            ?: ""
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_ADDRESS_COUNTRY, value)
    }

//*************************************************
// ROUTE PARAMETERS (100 - 199)
//*************************************************

// PAR_RTE_INDEX

/**
 * Get parameter that define index of point in track (index of trackpoint).
 *
 * @return index in track or '-1' if no index is defined
 */
var Point.parameterRteIndex: Int
    get() {
        return getParameter(GeoDataExtra.PAR_RTE_INDEX)
            ?.let { Utils.parseInt(it, -1) }
            ?: -1
    }
    set(value) {
        if (value >= 0) {
            addParameter(GeoDataExtra.PAR_RTE_INDEX, value.toString())
        } else {
            removeParameter(GeoDataExtra.PAR_RTE_INDEX)
        }
    }

// PAR_RTE_DISTANCE_F

/**
 * Check if point contains navigation distance value.
 */
val Point.hasParameterRteDistance: Boolean
    get() = hasParameter(GeoDataExtra.PAR_RTE_DISTANCE_F)

/**
 * Get navigation Distance (in metres) from current navPoint to next.
 */
var Point.parameterRteDistance: Float
    get() {
        return getParameter(GeoDataExtra.PAR_RTE_DISTANCE_F)
            ?.let { Utils.parseFloat(it) }
            ?: 0.0f
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_RTE_DISTANCE_F, value.toString())
    }

// PAR_RTE_TIME_I

/**
 * Check if point contains navigation time value.
 */
val Point.hasParameterRteTime: Boolean
    get() = hasParameter(GeoDataExtra.PAR_RTE_TIME_I)

/**
 * Get navigation time from the current waypoint to the next waypoint [in seconds].
 */
var Point.parameterRteTime: Int
    get() {
        return getParameter(GeoDataExtra.PAR_RTE_TIME_I)
            ?.let { Utils.parseInt(it) }
            ?: 0
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_RTE_TIME_I, value.toString())
    }

// PAR_RTE_SPEED_F

/**
 * Check if point contains navigation distance value.
 */
val Point.hasParameterRteSpeed: Boolean
    get() = hasParameter(GeoDataExtra.PAR_RTE_SPEED_F)

/**
 * Get the speed for the segment between current and the next waypoint [in m/s].
 */
var Point.parameterRteSpeed: Float
    get() {
        return getParameter(GeoDataExtra.PAR_RTE_SPEED_F)
            ?.let { Utils.parseFloat(it) }
            ?: 0.0f
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_RTE_SPEED_F, value.toString())
    }

// PAR_RTE_POINT_ACTION

/**
 * RTE action defined for current object.
 */
var Point.parameterRteAction: PointRteAction
    get() {
        return getParameter(GeoDataExtra.PAR_RTE_POINT_ACTION)
            ?.let { Utils.parseInt(it, -1) }
            ?.takeIf { it >= 0 }
            ?.let { PointRteAction.getActionById(it) }
            ?: PointRteAction.UNDEFINED
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_RTE_POINT_ACTION, value.id.toString())
    }

// PAR_RTE_POINT_PASS_PLACE_NOTIFY

/**
 * Parameter that define if Via-point should be notified during navigation.
 *
 * This parameter does not define if point is Via-point!
 */
var Point.parameterRtePassPlaceNotify: Boolean
    get() {
        return getParameter(GeoDataExtra.PAR_RTE_POINT_PASS_PLACE_NOTIFY)
            ?.takeIf { it.isNotEmpty() }
            ?.let { Utils.parseBoolean(it) }
            ?: true
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_RTE_POINT_PASS_PLACE_NOTIFY, if (value) "1" else "0")
    }

// PAR_RTE_COMPUTE_TYPE

/**
 * Type of track (car-fast, bike-short, ...).
 */
var Track.parameterRteComputeType: Int
    get() {
        return getParameter(GeoDataExtra.PAR_RTE_COMPUTE_TYPE)
            ?.takeIf { it.isNotEmpty() }
            ?.let { Utils.parseInt(it) }
            ?: GeoDataExtra.VALUE_RTE_TYPE_NO_TYPE
    }
    set(value) {
        addParameter(
            GeoDataExtra.PAR_RTE_COMPUTE_TYPE,
            value.toString()
        )
    }

//*************************************************
// LOPOINTS (310 - 330)
//*************************************************

// PAR_LOPOINTS_ID

/**
 * Check if point contains reference to online LoPoint.
 */
val Point.hasParameterLoPointsId: Boolean
    get() = parameterLoPointsId >= 1L

/**
 * Reference to the online LoPoint.
 */
var Point.parameterLoPointsId: Long
    get() {
        return getParameter(GeoDataExtra.PAR_LOPOINTS_ID)
            ?.let { Utils.parseLong(it) }
            ?: -1L
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_LOPOINTS_ID, value.toString())
    }

//*************************************************
// ATTACHMENTS
//*************************************************

/**
 * Add "email" to current object.
 *
 * @param email email to add
 * @param label (optional) label visible in the app
 */
@JvmOverloads
fun GeoData.addParameterEmail(email: String, label: String? = null): Boolean {
    return addParameter {
        addAttachment(GeoDataExtra.AttachType.EMAIL, label, email)
    }
}

/**
 * Add "phone" to current object.
 *
 * @param phone email to add
 * @param label (optional) label visible in the app
 */
@JvmOverloads
fun GeoData.addParameterPhone(phone: String, label: String? = null): Boolean {
    return addParameter {
        addAttachment(GeoDataExtra.AttachType.PHONE, label, phone)
    }
}

/**
 * Add "url" to current object.
 *
 * @param url email to add
 * @param label (optional) label visible in the app
 */
@JvmOverloads
fun GeoData.addParameterUrl(url: String, label: String? = null): Boolean {
    return addParameter {
        addAttachment(GeoDataExtra.AttachType.URL, label, url)
    }
}

/**
 * Add audio to current object.
 *
 * @param uri uri to add
 * @return `true` if audio was correctly added
 */
fun GeoData.addAttachmentAudio(uri: String, label: String? = null): Boolean {
    return addParameter {
        addAttachment(GeoDataExtra.AttachType.AUDIO, label, uri)
    }
}

/**
 * Add photo to current object.
 *
 * @param uri uri to add
 * @return `true` if photo was correctly added
 */
fun GeoData.addAttachmentPhoto(uri: String, label: String? = null): Boolean {
    return addParameter {
        addAttachment(GeoDataExtra.AttachType.PHOTO, label, uri)
    }
}

/**
 * Add video to current object.
 *
 * @param uri uri to add
 * @return `true` if video was correctly added
 */
fun GeoData.addAttachmentVideo(uri: String, label: String? = null): Boolean {
    return addParameter {
        addAttachment(GeoDataExtra.AttachType.VIDEO, label, uri)
    }
}

/**
 * Add other content to current object.
 *
 * @param uri uri to add
 * @return `true` if object was correctly added
 */
fun GeoData.addAttachmentOther(uri: String, label: String? = null): Boolean {
    return addParameter {
        addAttachment(GeoDataExtra.AttachType.OTHER, label, uri)
    }
}
