package locus.api.objects.geoData

import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.extra.PointRteAction
import locus.api.utils.Utils

//*************************************************
// PARAMETER "DESCRIPTION"
//*************************************************

/**
 * Description parameter attached to this object.
 */
var GeoData.parameterDescription: String
    get() {
        return extraData?.getParameterNotNull(GeoDataExtra.PAR_DESCRIPTION)
            ?: ""
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_DESCRIPTION, value)
    }

fun GeoData.removeParameterDescription() {
    removeParameter(GeoDataExtra.PAR_DESCRIPTION)
}

//*************************************************
// PARAMETER "LOPOINTS_ID"
//*************************************************

/**
 * Check if point contains reference to online LoPoint.
 */
val Point.hasParameterLoPointsId: Boolean
    get() = hasParameter(GeoDataExtra.PAR_LOPOINTS_ID)

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
// PARAMETER "RTE TIME"
//*************************************************

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

//*************************************************
// PARAMETER "RTE_INDEX"
//*************************************************

/**
 * Get parameter that define index of point in track (index of trackpoint).
 *
 * @return index in track or '-1' if no index is defined
 */
val Point.parameterRteIndex: Int
    get() {
        return getParameter(GeoDataExtra.PAR_RTE_INDEX)
            ?.let { Utils.parseInt(it) }
            ?: -1
    }

//*************************************************
// PARAMETER "RTE_POINT_ACTION"
//*************************************************

/**
 * RTE action defined for current object.
 */
var Point.parameterRteAction: PointRteAction
    get() {
        return getParameter(GeoDataExtra.PAR_RTE_POINT_ACTION)
            ?.takeIf { it.isNotEmpty() }
            ?.let { PointRteAction.getActionById(Utils.parseInt(it)) }
            ?: PointRteAction.UNDEFINED
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_RTE_POINT_ACTION, value.id.toString())
    }

//*************************************************
// PARAMETER "RTE_POINT_PASS_PLACE_NOTIFY"
//*************************************************

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

//*************************************************
// PARAMETER "RTE SPEED"
//*************************************************

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

//*************************************************
// PARAMETER "RTE TIME"
//*************************************************

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

//*************************************************
// PARAMETER "SOURCE"
//*************************************************

/**
 * Current defined source parameter.
 *
 * @return source parameter or 'SOURCE_UNKNOWN' if not defined
 */
var GeoData.parameterSource: Byte
    get() {
        return extraData?.getParameterRaw(GeoDataExtra.PAR_SOURCE)
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

//*************************************************
// PARAMETER "STYLE"
//*************************************************

var Point.parameterStyleName: String
    get() {
        return extraData?.getParameter(GeoDataExtra.PAR_STYLE_NAME)
            ?: ""
    }
    set(value) {
        addParameter(GeoDataExtra.PAR_STYLE_NAME, value)
    }

fun Point.removeParameterStyleName() {
    removeParameter(GeoDataExtra.PAR_STYLE_NAME)
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
