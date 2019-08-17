package com.asamm.locus.api.sample

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import locus.api.android.ActionBasics
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusUtils
import locus.api.objects.extra.GeoDataExtra
import locus.api.utils.Logger

class ActivityGeocacheTools : FragmentActivity() {

    @SuppressLint("SetTextI18n")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)
        tv.text = "ActivityGeocacheTools - only for receiving Tools actions from cache"
        setContentView(tv)

        // finally check intent that started this sample
        checkStartIntent()
    }

    private fun checkStartIntent() {
        val intent = intent
        Logger.logD(TAG, "received intent: $intent")
        if (intent == null) {
            return
        }

        // get Locus from intent
        val lv = LocusUtils.createLocusVersion(this, intent)
        if (lv == null) {
            Logger.logD(TAG, "checkStartIntent(), cannot obtain LocusVersion")
            return
        }

        if (IntentHelper.isIntentPointTools(intent)) {
            try {
                val pt = IntentHelper.getPointFromIntent(this, intent)
                if (pt == null) {
                    Toast.makeText(this@ActivityGeocacheTools, "Wrong INTENT - no point!", Toast.LENGTH_SHORT).show()
                } else {
                    AlertDialog.Builder(this)
                            .setTitle("Intent - On Point action")
                            .setMessage("Received intent with point:\n\n" + pt.name + "\n\n" +
                                    "loc:" + pt.location + "\n\n" +
                                    "gcData:" + if (pt.gcData == null) "sorry, but no..." else pt.gcData!!.cacheID)
                            .setNegativeButton("Close") { _, _ ->
                                // just do some action on required coordinates
                            }
                            .setPositiveButton("Send updated back") { _, _ ->
                                // because current test version is registered on geocache data,
                                // I'll send as result updated geocache
                                try {
                                    pt.addParameter(GeoDataExtra.PAR_DESCRIPTION, "UPDATED!")
                                    pt.location.latitude = pt.location.latitude + 0.001
                                    pt.location.longitude = pt.location.longitude + 0.001
                                    ActionBasics.updatePoint(this@ActivityGeocacheTools, lv, pt, false)
                                    finish()
                                } catch (e: Exception) {
                                    Logger.logE(TAG, "isIntentPointTools(), problem with sending new waypoint back", e)
                                }
                            }
                            .show()
                }
            } catch (e: Exception) {
                Logger.logE(TAG, "handle point tools", e)
            }

        }
    }

    companion object {

        private const val TAG = "ActivityGeocacheTools"
    }
}
