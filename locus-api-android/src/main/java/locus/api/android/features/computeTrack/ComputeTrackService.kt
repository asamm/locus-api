/**
 * Created by menion on 10. 7. 2014.
 * Class is part of Locus project
 */
package locus.api.android.features.computeTrack

import android.app.Service
import android.content.Intent
import android.os.IBinder
import locus.api.android.objects.LocusVersion

import locus.api.android.objects.ParcelableContainer
import locus.api.android.utils.LocusUtils
import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.geoData.Track
import locus.api.utils.Logger

/**
 * Base class for compute route service.
 */
abstract class ComputeTrackService : Service() {

    private val binder = object : IComputeTrackService.Stub() {

        override fun getAttribution(): String {
            return this@ComputeTrackService.attribution
        }

        override fun getTrackTypes(): IntArray {
            return this@ComputeTrackService.trackTypes
        }

        override fun getIntentForSettings(): Intent {
            return this@ComputeTrackService.intentForSettings
        }

        override fun getNumOfTransitPoints(): Int {
            return this@ComputeTrackService.numOfTransitPoints
        }

        override fun computeTrack(trackParams: ParcelableContainer): ParcelableContainer? {
            try {
                // get track parameters from container
                val containerData = trackParams.data
                val params = ComputeTrackParameters()
                params.read(containerData!!)

                // get active running Locus
                val lv = LocusUtils.getActiveVersion(this@ComputeTrackService)
                if (lv == null) {
                    Logger.logW(TAG, "Problem with finding running Locus instance")
                    return null
                }

                // compute track itself
                val track = this@ComputeTrackService.computeTrack(lv, params)
                return if (track != null) {
                    ParcelableContainer(track.asBytes)
                } else {
                    null
                }
            } catch (e: Exception) {
                Logger.logE(TAG, "computeTrack($trackParams)", e)
                return null
            }
        }
    }

    /**
     * Get visible attribution for this route/track provider. Attribution may be HTML code
     * that will be correctly converted (just basic tags) into TextView in Locus.
     *
     * @return text visible as attribution
     */
    abstract val attribution: String

    /**
     * Get list of available routing methods. Definition of possibilities is in
     * [GeoDataExtra] class.
     *
     * @return array of supported routing methods
     */
    abstract val trackTypes: IntArray

    /**
     * If application offer some settings, here should return prepared
     * intent that should be called from Locus to display add-on settings.
     *
     * @return prepared Intent pointing to settings, or `null`,
     * if no settings are available.
     */
    abstract val intentForSettings: Intent

    /**
     * Number of additional transit points that may be passed to navigation
     *
     * @return max number of transit points
     */
    val numOfTransitPoints: Int
        get() = 0

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    /**
     * Main feature that perform routing itself.
     *
     * @param lv     Locus version that is requesting routing.
     * @param params parameters requested by Locus for a new track.
     * @return computed track with all defined parameters.
     */
    abstract fun computeTrack(lv: LocusVersion?, params: ComputeTrackParameters): Track?

    companion object {

        // tag for logger
        private val TAG = ComputeTrackService::class.java.simpleName
    }
}
