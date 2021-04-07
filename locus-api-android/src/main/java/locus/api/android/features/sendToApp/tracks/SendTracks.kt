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
package locus.api.android.features.sendToApp.tracks

import android.content.Context
import android.net.Uri
import locus.api.android.features.sendToApp.SendMode
import locus.api.android.objects.LocusVersion
import locus.api.objects.geoData.Track
import java.io.File

class SendTracks(sendMode: SendMode, tracks: List<Track>,
        setup: (SendTracks.() -> Unit)? = null)
    : SendTrackBase(sendMode, tracks) {

    init {
        setup?.invoke(this)
    }

    override fun send(ctx: Context, lv: LocusVersion?): Boolean {
        return sendImpl(ctx, lv)
    }

    override fun sendOverFile(ctx: Context, lv: LocusVersion?,
            cacheFile: File, cacheFileUri: Uri): Boolean {
        return sendOverFileImpl(ctx, lv, cacheFile, cacheFileUri)
    }
}