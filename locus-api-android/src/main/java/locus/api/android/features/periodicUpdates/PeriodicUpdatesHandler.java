package locus.api.android.features.periodicUpdates;

import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.LocusUtils.LocusVersion;
import locus.api.objects.extra.Location;

import android.content.Context;
import android.content.Intent;

public class PeriodicUpdatesHandler {

    // tag for logger
    private static final String TAG = "PeriodicUpdatesHandler";

    // private temporary variables for checking changes
    protected Location mLastMapCenter;
    protected Location mLastGps;
    protected int mLastZoomLevel;

    // checker for new location
    protected double mLocMinDistance;

    // instance
    private static PeriodicUpdatesHandler mInstance;

    /**
     * Get instance of handler object.
     *
     * @return instance
     */
    public static PeriodicUpdatesHandler getInstance() {
        if (mInstance == null) {
            synchronized (TAG) {
                mInstance = new PeriodicUpdatesHandler();
            }
        }
        return mInstance;
    }

    /**
     * Private constructor.
     */
    private PeriodicUpdatesHandler() {
        this.mLastZoomLevel = -1;
        this.mLocMinDistance = 1.0;
    }

    /**
     * Set notification limit used for check if distance between previous and
     * new location is higher than this value. So new locations is market as NEW
     *
     * @param locMinDistance distance in metres
     */
    public void setLocNotificationLimit(double locMinDistance) {
        this.mLocMinDistance = locMinDistance;
    }

    /**
     * Interface that handle received responses from Locus
     */
    public interface OnUpdate {

        /**
         * If Intent contains correct data, this function is called for
         * future handling.
         *
         * @param locusVersion Version of application that send this intent.
         *                     This parameter is useful for future communication with Locus
         *                     application. It may be anyway 'null', in case, Locus version is
         *                     too old and do not support this feature.
         * @param update       Container with new fresh data from Locus.
         */
        void onUpdate(LocusVersion locusVersion, UpdateContainer update);

        /**
         * In case, there was a problem with received intent, this function is
         * called, to notify you about a problem.
         */
        void onIncorrectData();
    }

    /**
     * Major function that handle data received over intent. From this intent
     * are grabbed all information that Locus send and are redirected back
     * to your own handler.
     *
     * @param ctx     current context
     * @param intent  received intent
     * @param handler handler for result
     */
    public void onReceive(final Context ctx, Intent intent, OnUpdate handler) {
        // check handler
        if (handler == null) {
            throw new IllegalArgumentException("Incorrect arguments");
        }

        // check parameters and notify if any problem happen
        if (ctx == null || intent == null) {
            handler.onIncorrectData();
            return;
        }

        // prepare data container
        UpdateContainer update = PeriodicUpdatesFiller.intentToUpdate(intent, this);

        // send update back by handler (together with LocusVersion object)
        handler.onUpdate(LocusUtils.createLocusVersion(ctx, intent), update);
    }
}
