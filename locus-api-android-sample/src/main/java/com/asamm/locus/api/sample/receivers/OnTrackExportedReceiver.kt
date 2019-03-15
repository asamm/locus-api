/****************************************************************************
 *
 * Created by menion on 15/03/2019.
 * Copyright (c) 2019. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/

package com.asamm.locus.api.sample.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.asamm.locus.api.sample.utils.Utils
import locus.api.android.utils.LocusConst
import locus.api.utils.Logger

class OnTrackExportedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Logger.logD(TAG, "onReceive($context, $intent)")
        try {
            // check if defined (optional) extra match
            val resultExtra = intent.getStringExtra("resultExtra")
            if (resultExtra == "test") {
                Logger.logW(TAG, "received resultExtra `" + resultExtra + "` " +
                        "does not match expected `xxx`. Export incorrect.")
                return
            }

            // check if result is valid
            intent.data?.let {
                val targetFile = createTempFile(System.currentTimeMillis().toString(), "gpx")
                Utils.copy(context, it, targetFile)
                Toast.makeText(context, "Process successful\n\nDir:" + targetFile.name +
                        ", exists:" + targetFile.exists(),
                        Toast.LENGTH_LONG).show()
            } ?: {
                val errorMessage = intent.getStringExtra(LocusConst.INTENT_EXTRA_ERROR)
                Toast.makeText(context, "Process failed\n\nError: $errorMessage",
                        Toast.LENGTH_LONG).show()
            }()

        } catch (e: Exception) {
            Logger.logE(TAG, "onReceive($context, $intent)", e)
        }
    }

    companion object {

        // tag for logger
        private const val TAG = "OnTrackExportedReceiver"
    }
}