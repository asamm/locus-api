package locus.api.android

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import locus.api.android.objects.LocusVersion
import locus.api.android.objects.PackPoints
import locus.api.android.objects.VersionCode
import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.Storable
import locus.api.objects.geoData.Circle
import locus.api.utils.Logger

object ActionDisplayVarious {

    // tag for logger
    private const val TAG = "ActionDisplayVarious"

    enum class ExtraAction {
        NONE, CENTER, IMPORT
    }

    // PRIVATE CALLS

    @Throws(RequiredVersionMissingException::class)
    internal fun sendData(action: String, context: Context, intent: Intent,
            callImport: Boolean, center: Boolean): Boolean {
        return sendData(action, context, intent,
                callImport, center, VersionCode.UPDATE_01)
    }

    @Throws(RequiredVersionMissingException::class)
    internal fun sendData(action: String, ctx: Context, intent: Intent,
            callImport: Boolean, center: Boolean, vc: VersionCode): Boolean {
        // get valid version
        val lv = LocusUtils.getActiveVersion(ctx, vc)
                ?: throw RequiredVersionMissingException(vc.vcFree)

        // send data with valid active version
        return sendData(action, ctx, intent, callImport, center, lv)
    }

    @Throws(RequiredVersionMissingException::class)
    internal fun sendData(action: String, ctx: Context, intent: Intent,
            callImport: Boolean, center: Boolean, lv: LocusVersion): Boolean {
        // check intent firstly
        if (!hasData(intent)) {
            Logger.logW(TAG, "Intent 'null' or not contain any data")
            return false
        }

        // create intent with right calling method
        intent.action = action

        // set centering tag
        intent.putExtra(LocusConst.INTENT_EXTRA_CENTER_ON_DATA, center)

        // set import tag
        if (action == LocusConst.ACTION_DISPLAY_DATA_SILENTLY) {
            // send broadcast
            LocusUtils.sendBroadcast(ctx, intent, lv)
        } else {
            // set import tag
            intent.putExtra(LocusConst.INTENT_EXTRA_CALL_IMPORT, callImport)
            // finally start activity
            ctx.startActivity(intent)
        }
        return true
    }

    /**
     * Method used for removing special objects from Locus map. Currently this method
     * is used only for removing circles. If you want to remove any visible points or
     * tracks you already send by this API, send simply new same intent (as the one
     * with your data), but with empty list of data. So for example send again
     * [PackPoints] that has same name!, but contain no data
     *
     * @param ctx       current context
     * @param extraName name of items to remove
     * @param itemsId   ID of item
     * @return `true` if item was correctly send
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    @Throws(RequiredVersionMissingException::class)
    internal fun removeSpecialDataSilently(ctx: Context, lv: LocusVersion,
            extraName: String, itemsId: LongArray?): Boolean {
        // check Locus version
        if (!lv.isVersionValid(VersionCode.UPDATE_02)) {
            throw RequiredVersionMissingException(VersionCode.UPDATE_02)
        }

        // check intent firstly
        if (itemsId == null || itemsId.isEmpty()) {
            Logger.logW(TAG, "Intent 'null' or not contain any data")
            return false
        }

        // create intent with right calling method
        val intent = Intent(LocusConst.ACTION_REMOVE_DATA_SILENTLY)
        intent.setPackage(lv.packageName)
        intent.putExtra(extraName, itemsId)

        // set import tag
        LocusUtils.sendBroadcast(ctx, intent, lv)
        return true
    }

    //*************************************************
    // CIRCLES
    //*************************************************

    /**
     * Simple way how to send circles over intent to Locus.
     *
     * @return true if success
     */
    @Throws(RequiredVersionMissingException::class)
    fun sendCirclesSilent(ctx: Context, circles: List<Circle>, centerOnData: Boolean): Boolean {
        return sendCirclesSilent(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                ctx, circles, false, centerOnData)
    }

    @Throws(RequiredVersionMissingException::class)
    private fun sendCirclesSilent(action: String, ctx: Context,
            circles: List<Circle>, callImport: Boolean, centerOnData: Boolean): Boolean {
        // check data
        if (circles.isEmpty()) {
            return false
        }

        // create and start intent
        val intent = Intent()
        intent.putExtra(LocusConst.INTENT_EXTRA_CIRCLES_MULTI,
                Storable.getAsBytes(circles))
        return sendData(action, ctx, intent, callImport, centerOnData,
                VersionCode.UPDATE_02)
    }

    /**
     * Allow to remove visible circles defined by it's ID value
     *
     * @param ctx     current context that send broadcast
     * @param itemsId list of circles IDs that should be removed from map
     */
    @Throws(RequiredVersionMissingException::class)
    fun removeCirclesSilent(ctx: Context, lv: LocusVersion, itemsId: LongArray) {
        removeSpecialDataSilently(ctx, lv, LocusConst.INTENT_EXTRA_CIRCLES_MULTI, itemsId)
    }

    //*************************************************
    // TOOLS
    //*************************************************

    /**
     * Test if intent is valid and contains any data to display.
     *
     * @param intent intent to test
     * @return `true` if intent is valid and contains data
     */
    fun hasData(intent: Intent): Boolean {
        // check intent object
        return !(intent.getByteArrayExtra(LocusConst.INTENT_EXTRA_POINTS_DATA) == null
                && intent.getByteArrayExtra(LocusConst.INTENT_EXTRA_POINTS_DATA_ARRAY) == null
                && intent.getStringExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH) == null
                && intent.getParcelableExtra<Parcelable>(LocusConst.INTENT_EXTRA_POINTS_FILE_URI) == null
                && intent.getByteArrayExtra(LocusConst.INTENT_EXTRA_TRACKS_SINGLE) == null
                && intent.getByteArrayExtra(LocusConst.INTENT_EXTRA_TRACKS_MULTI) == null
                && intent.getParcelableExtra<Parcelable>(LocusConst.INTENT_EXTRA_TRACKS_FILE_URI) == null
                && intent.getByteArrayExtra(LocusConst.INTENT_EXTRA_CIRCLES_MULTI) == null)
    }
}
