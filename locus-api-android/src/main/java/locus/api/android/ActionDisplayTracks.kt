package locus.api.android

import android.content.Context
import android.content.Intent
import locus.api.android.utils.LocusConst
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.Storable
import locus.api.objects.geoData.Track
import locus.api.utils.Logger

@Suppress("unused")
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
        // check track
        if (track.points.size == 0) {
            Logger.logE(TAG, "sendTrack(" + action + ", " + ctx + ", " + track + ", " +
                    callImport + ", " + centerOnData + ", " + startNavigation + "), " +
                    "track is null or contain no points")
            return false
        }

        // create and start intent
        val intent = Intent()
        intent.putExtra(LocusConst.INTENT_EXTRA_TRACKS_SINGLE, track.asBytes)
        intent.putExtra(LocusConst.INTENT_EXTRA_START_NAVIGATION, startNavigation)
        return ActionDisplayVarious.sendData(action, ctx, intent, callImport, centerOnData)
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
        // check data
        if (tracks.isEmpty()) {
            return false
        }

        // create and start intent
        val intent = Intent()
        intent.putExtra(LocusConst.INTENT_EXTRA_TRACKS_MULTI,
                Storable.getAsBytes(tracks))
        return ActionDisplayVarious.sendData(action, ctx, intent, callImport, centerOnData)
    }
}
