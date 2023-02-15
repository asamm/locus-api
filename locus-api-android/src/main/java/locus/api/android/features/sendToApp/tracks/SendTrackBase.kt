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
import android.content.Intent
import android.net.Uri
import com.asamm.loggerV2.logE
import locus.api.android.features.sendToApp.SendMode
import locus.api.android.features.sendToApp.SendToAppBase
import locus.api.android.features.sendToApp.SendToAppHelper
import locus.api.android.objects.LocusVersion
import locus.api.android.objects.VersionCode
import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.Storable
import locus.api.objects.geoData.Track
import java.io.File

/**
 * Base class for sending track(s) to Locus apps.
 *
 * Do not use directly but instead use it's extensions. Class is public because of Kotlin
 * limitations in inheritance.
 */
abstract class SendTrackBase internal constructor(
    sendMode: SendMode,
    internal val tracks: List<Track>
) : SendToAppBase(sendMode) {

    // SEND DATA

    /**
     * Send defined parameters to the Locus apps. System is based on the 'Intent - Bundle' mechanism.
     * Method is useful only for data less then 1MB big. For bigger tracks, use alternative method
     * over FileUri system `sendOverFile`.
     */
    abstract fun send(
        ctx: Context,
        lv: LocusVersion? = LocusUtils.getActiveVersion(ctx, VersionCode.UPDATE_01)
    ): Boolean

    @Throws(RequiredVersionMissingException::class)
    internal fun sendImpl(
        ctx: Context, lv: LocusVersion?,
        intentExtra: (Intent.() -> Unit)? = null
    ): Boolean {
        // validate tracks
        if (!validateTracks()) {
            return false
        }

        // validate version
        if (lv == null || !lv.isVersionValid(VersionCode.UPDATE_01)) {
            throw RequiredVersionMissingException(VersionCode.UPDATE_01.vcFree)
        }

        // create and start intent
        val intent = Intent()
        if (tracks.size == 1) {
            intent.putExtra(
                LocusConst.INTENT_EXTRA_TRACKS_SINGLE,
                tracks.first().asBytes
            )
        } else {
            intent.putExtra(
                LocusConst.INTENT_EXTRA_TRACKS_MULTI,
                Storable.getAsBytes(tracks)
            )
        }
        intentExtra?.invoke(intent)

        // send request
        return sendFinal(ctx, intent, lv)
    }

    /**
     * Main function for sending pack of points over temporary stored file.
     *
     * @param ctx current ctx
     * @param lv required Locus version
     * @param cacheFile path where file will be temporary stored
     * @param cacheFileUri uri from `FileProvider`, which represents `file`
     * @return `true` if request was correctly send
     */
    @Throws(RequiredVersionMissingException::class)
    abstract fun sendOverFile(
        ctx: Context,
        lv: LocusVersion? = LocusUtils.getActiveVersion(ctx, VersionCode.UPDATE_17),
        cacheFile: File, cacheFileUri: Uri
    ): Boolean

    /**
     * Main function for sending pack of points over temporary stored file.
     *
     * @param ctx current ctx
     * @param lv required Locus version
     * @param cacheFile path where file will be temporary stored
     * @param cacheFileUri uri from `FileProvider`, which represents `file`
     * @return `true` if request was correctly send
     */
    @Throws(RequiredVersionMissingException::class)
    internal fun sendOverFileImpl(
        ctx: Context, lv: LocusVersion?,
        cacheFile: File, cacheFileUri: Uri,
        intentExtra: (Intent.() -> Unit)? = null
    ): Boolean {
        // validate tracks
        if (!validateTracks()) {
            return false
        }

        // validate version
        if (lv == null || !lv.isVersionValid(VersionCode.UPDATE_17)) {
            throw RequiredVersionMissingException(VersionCode.UPDATE_17.vcFree)
        }

        // write data to storage
        val writeResult = SendToAppHelper.sendDataWriteOnCard(cacheFile) {
            Storable.writeList(tracks, this)
        }

        // send intent with reference to stored data
        return if (writeResult) {
            // setup intent
            val intent = Intent()
            intent.putExtra(LocusConst.INTENT_EXTRA_TRACKS_FILE_URI, cacheFileUri)
            intentExtra?.invoke(intent)

            // grant permission to app package. Because we do not send `uri` over setData, we need
            // to use this method
            ctx.grantUriPermission(
                lv.packageName,
                cacheFileUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            // send request
            return sendFinal(ctx, intent, lv)
        } else {
            false
        }
    }

    // TOOLS

    /**
     * Validate all tracks that should be send to client.
     */
    internal fun validateTracks(): Boolean {
        // validate tracks
        if (tracks.isEmpty()) {
            logE(tag = TAG) { "validateTracks(), " + "no content to send" }
            return false
        }
        if (tracks.find { it.pointsCount == 0 } != null) {
            logE(tag = TAG) { "validateTracks(), " + "exists empty track" }
            return false
        }

        // tracks are valid
        return true
    }

    companion object {

        // tag for logger
        private const val TAG = "SendTrackBase"

        // RECEIVE DATA

        /**
         * Invert method to [SendTrackBase.sendOverFile]. This load serialized data from
         * a file stored in [Intent].
         *
         * @param ctx context
         * @param intent intent data
         * @return loaded tracks
         */
        fun readTracksFile(ctx: Context, intent: Intent): List<Track> {
            return when {
                intent.hasExtra(LocusConst.INTENT_EXTRA_TRACKS_FILE_URI) -> {
                    SendToAppHelper.readDataFromUri(
                        ctx,
                        intent.getParcelableExtra(LocusConst.INTENT_EXTRA_TRACKS_FILE_URI)!!
                    )
                }
                else -> {
                    listOf()
                }
            }
        }
    }
}
