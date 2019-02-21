package locus.api.android

import android.content.Context
import android.content.Intent

import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils.LocusVersion
import locus.api.android.utils.LocusUtils.VersionCode
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.Storable
import locus.api.objects.extra.Circle

object ActionDisplayVarious {

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
        return ActionDisplay.sendData(action, ctx, intent, callImport, centerOnData,
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
        ActionDisplay.removeSpecialDataSilently(ctx, lv, LocusConst.INTENT_EXTRA_CIRCLES_MULTI, itemsId)
    }
}
