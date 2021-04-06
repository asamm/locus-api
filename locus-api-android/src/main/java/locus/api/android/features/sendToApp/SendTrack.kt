/****************************************************************************
 *
 * Created by menion on 06.04.2021.
 * Copyright (c) 2021. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/
package locus.api.android.features.sendToApp

import android.content.Context
import android.net.Uri
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.LocusConst
import locus.api.objects.geoData.Track
import java.io.File

class SendTrack(sendMode: SendMode, track: Track,
        setup: (SendTrack.() -> Unit)? = null)
    : SendTrackBase(sendMode, listOf(track)) {

    /**
     * Flag if we should start navigation when track is loaded on the map.
     * Parameter is usable only for `SendMode.Basic` or `SendMode.Silent`.
     */
    var startNavigation: Boolean = false

    init {
        setup?.invoke(this)
    }

    override fun send(ctx: Context, lv: LocusVersion?): Boolean {
        return sendImpl(ctx, lv) {
            putExtra(LocusConst.INTENT_EXTRA_START_NAVIGATION, startNavigation)
        }
    }

    override fun sendOverFile(ctx: Context, lv: LocusVersion?,
            cacheFile: File, cacheFileUri: Uri): Boolean {
        return sendOverFileImpl(ctx, lv, cacheFile, cacheFileUri) {
            putExtra(LocusConst.INTENT_EXTRA_START_NAVIGATION, startNavigation)
        }
    }
}