package locus.api.android.features.sendToApp

import android.content.Context
import android.content.Intent
import locus.api.android.ActionDisplayVarious
import locus.api.android.objects.LocusVersion

/**
 * Core class for all "Send" tasks.
 *
 * @param sendMode mode we will be using for send operation
 */
open class SendToAppBase(internal val sendMode: SendMode) {

    /**
     * Final method that execute "Send" task itself.
     *
     * @param ctx current context
     * @param intent object we are sending
     * @param lv target Locus app
     */
    internal fun sendFinal(ctx: Context, intent: Intent, lv: LocusVersion): Boolean {
        return when (sendMode) {
            is SendMode.Basic -> {
                ActionDisplayVarious.sendData(sendMode.action, ctx, intent,
                        callImport = false,
                        center = sendMode.centerOnData,
                        lv = lv)
            }
            is SendMode.Silent -> {
                ActionDisplayVarious.sendData(sendMode.action, ctx, intent,
                        callImport = false,
                        center = false,
                        lv = lv)
            }
            is SendMode.Import -> {
                ActionDisplayVarious.sendData(sendMode.action, ctx, intent,
                        callImport = true,
                        center = false,
                        lv = lv)
            }
        }
    }
}
