package locus.api.android

import android.content.Context
import locus.api.android.features.sendToApp.SendMode
import locus.api.android.features.sendToApp.tracks.SendTrack
import locus.api.android.features.sendToApp.tracks.SendTracks
import locus.api.android.utils.LocusConst
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.geoData.Track

@Deprecated(message = "Use `SendTrack` or `SendTracks` objects.")
object ActionDisplayTracks {

    // tag for logger
    private const val TAG = "ActionDisplayTracks"

    // SEND ONE SINGLE TRACK

    @Throws(RequiredVersionMissingException::class)
    fun sendTrack(ctx: Context, track: Track,
            extraAction: ActionDisplayVarious.ExtraAction,
            startNavigation: Boolean = false): Boolean {
        return sendTrack(LocusConst.ACTION_DISPLAY_DATA,
                ctx, track, extraAction == ActionDisplayVarious.ExtraAction.IMPORT,
                extraAction == ActionDisplayVarious.ExtraAction.CENTER, startNavigation)
    }

    @Throws(RequiredVersionMissingException::class)
    fun sendTrackSilent(ctx: Context, track: Track, centerOnData: Boolean): Boolean {
        return sendTrack(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                ctx, track, false, centerOnData, false)
    }

    @Throws(RequiredVersionMissingException::class)
    private fun sendTrack(action: String, ctx: Context, track: Track,
            callImport: Boolean, centerOnData: Boolean, startNavigation: Boolean): Boolean {
        // prepare mode
        val sendMode = when {
            callImport -> {
                SendMode.Import()
            }
            action == LocusConst.ACTION_DISPLAY_DATA -> {
                SendMode.Basic(centerOnData)
            }
            action == LocusConst.ACTION_DISPLAY_DATA_SILENTLY -> {
                SendMode.Silent()
            }
            else -> {
                return false
            }
        }

        // send request
        return SendTrack(sendMode, track) {
            this.startNavigation = startNavigation
        }.send(ctx)
    }

    // SEND TRACK PACK (MORE THEN ONE)

    @Throws(RequiredVersionMissingException::class)
    fun sendTracks(ctx: Context, tracks: List<Track>,
            extraAction: ActionDisplayVarious.ExtraAction): Boolean {
        return sendTracks(LocusConst.ACTION_DISPLAY_DATA,
                ctx, tracks, extraAction == ActionDisplayVarious.ExtraAction.IMPORT,
                extraAction == ActionDisplayVarious.ExtraAction.CENTER)
    }

    @Throws(RequiredVersionMissingException::class)
    fun sendTracksSilent(ctx: Context, tracks: List<Track>, centerOnData: Boolean): Boolean {
        return sendTracks(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                ctx, tracks, false, centerOnData)
    }

    @Throws(RequiredVersionMissingException::class)
    private fun sendTracks(action: String, ctx: Context,
            tracks: List<Track>, callImport: Boolean, centerOnData: Boolean): Boolean {
        // prepare mode
        val sendMode = when {
            callImport -> {
                SendMode.Import()
            }
            action == LocusConst.ACTION_DISPLAY_DATA -> {
                SendMode.Basic(centerOnData)
            }
            action == LocusConst.ACTION_DISPLAY_DATA_SILENTLY -> {
                SendMode.Silent()
            }
            else -> {
                return false
            }
        }

        // send request
        return SendTracks(sendMode, tracks)
                .send(ctx)
    }
}
