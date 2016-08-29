// IComputeTrackService.aidl
package locus.api.android.features.computeTrack;

import locus.api.android.objects.ParcelableContainer;
import android.content.Intent;

interface IComputeTrackService {

    String getAttribution();

    int[] getTrackTypes();

    Intent getIntentForSettings();

    ParcelableContainer computeTrack(in ParcelableContainer trackParams);

    int getNumOfTransitPoints();
}
