package locus.api.android.features.computeTrack;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import locus.api.android.objects.ParcelableContainer;
import locus.api.android.utils.LocusUtils;
import locus.api.objects.extra.Track;
import locus.api.utils.Logger;

/**
 * Created by menion on 10. 7. 2014.
 * Class is part of Locus project
 */
public abstract class ComputeTrackService extends Service {

    // tag for logger
    private static final String TAG = ComputeTrackService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IComputeTrackService.Stub mBinder = new IComputeTrackService.Stub() {

        @Override
        public String getAttribution() {
            return ComputeTrackService.this.getAttribution();
        }

        @Override
        public int[] getTrackTypes() throws RemoteException {
            return ComputeTrackService.this.getTrackTypes();
        }

        @Override
        public Intent getIntentForSettings() {
            return ComputeTrackService.this.getIntentForSettings();
        }

        @Override
        public int getNumOfTransitPoints() {
            return ComputeTrackService.this.getNumOfTransitPoints();
        }

        @Override
        public ParcelableContainer computeTrack(ParcelableContainer trackParams) throws RemoteException {
            try {
                // get track parameters from container
                byte[] containerData = trackParams.getData();
                ComputeTrackParameters params = new ComputeTrackParameters(containerData);

                // get active running Locus
                LocusUtils.LocusVersion lv = LocusUtils.getActiveVersion(ComputeTrackService.this);
                if (lv == null) {
                    Logger.logW(TAG, "Problem with finding running Locus instance");
                    return null;
                }

                // compute track itself
                Track track = ComputeTrackService.this.computeTrack(lv, params);
                if (track != null) {
                    return new ParcelableContainer(track.getAsBytes());
                } else {
                    return null;
                }
            } catch (Exception e) {
                Logger.logE(TAG, "computeTrack(" + trackParams + ")", e);
                return null;
            }

        }
    };

    /**
     * Get visible attribution for this route/track provider. Attribution may be HTML code
     * that will be correctly converted (just basic tags) into TextView in Locus.
     * @return text visible as attribution
     */
    public abstract String getAttribution();

    /**
     * Get list of available routing methods. Definition of possibilities is in
     * {@link locus.api.objects.extra.ExtraData} class.
     * @return array of supported routing methods
     */
    public abstract int[] getTrackTypes();

    /**
     * If application offer some settings, here should return prepared
     * intent that should be called from Locus to display add-on settings.
     * @return prepared Intent pointing to settings, or <code>null</code>,
     * if no settings are available.
     */
    public abstract Intent getIntentForSettings();

    /**
     * Number of additional transit points that may be passed to navigation
     * @return max number of transit points
     */
    public int getNumOfTransitPoints() {
        return 0;
    }

    /**
     * Main feature that perform routing itself.
     * @param lv Locus version that is requesting routing.
     * @param params parameters requested by Locus for a new track.
     * @return computed track with all defined parameters.
     */
    public abstract Track computeTrack(LocusUtils.LocusVersion lv, ComputeTrackParameters params);
}
