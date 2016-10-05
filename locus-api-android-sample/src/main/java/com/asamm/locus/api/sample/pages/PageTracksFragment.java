package com.asamm.locus.api.sample.pages;

import com.asamm.locus.api.sample.utils.BasicAdapterItem;
import com.asamm.locus.api.sample.utils.SampleCalls;

import java.util.ArrayList;
import java.util.List;

import locus.api.android.ActionTools;
import locus.api.android.utils.LocusUtils;

/**
 * Created by menion on 29/08/2016.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
public class PageTracksFragment extends ABasePageFragment {
	@Override
	protected List<BasicAdapterItem> getItems() {
		// create list of items
		List<BasicAdapterItem> items = new ArrayList<>();
		items.add(new BasicAdapterItem(1,
				"Display one track"));
		items.add(new BasicAdapterItem(2,
				"Display multiple tracks"));

		items.add(new BasicAdapterItem(8,
				"Start Navigation (to a point)",
				"Allows to start Navigation to a certain point. This function is a direct intent so it starts Locus if it's not running. You may also use 'actionStartGuiding' to start Guiding instead of Navigation."));
		items.add(new BasicAdapterItem(9,
				"Start Navigation (to an Address)",
				"Allows to start Navigation to a certain point defined by an address."));

		items.add(new BasicAdapterItem(20,
				"Start Track recording",
				"Allows to start track recording. This call is sent as Broadcast event so it requires to know LocusVersion that should start track recording."));
		items.add(new BasicAdapterItem(21,
				"Stop Track recording",
				"The same as previous sample, just used to stop the track recording."));
		return items;
	}

	@Override
	protected void onItemClicked(int itemId, LocusUtils.LocusVersion activeLocus)
			throws Exception {
		if (itemId == 1) {
			SampleCalls.callSendOneTrack(getActivity());
		} else if (itemId == 2) {
			SampleCalls.callSendMultipleTracks(getActivity());
		} else if (itemId == 8) {
			ActionTools.actionStartNavigation(getActivity(),
					SampleCalls.generateWaypoint(1));
		} else if (itemId == 9) {
			ActionTools.actionStartNavigation(getActivity(),
					"Řipská 20, Praha 2, ČR");
		} else if (itemId == 20) {
			// start track recording. Recording profile "Cycle" is optional parameter. If
			// this parameter is not used, last used profile is used for recording.
			ActionTools.actionTrackRecordStart(getActivity(), activeLocus, "Car");
		} else if (itemId == 21) {
			ActionTools.actionTrackRecordStop(getActivity(), activeLocus, true);
		}
	}
}
