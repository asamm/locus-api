package com.asamm.locus.api.sample.pages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.asamm.locus.api.sample.ActivityDashboard;
import com.asamm.locus.api.sample.BuildConfig;
import com.asamm.locus.api.sample.utils.BasicAdapterItem;
import com.asamm.locus.api.sample.utils.SampleCalls;

import java.util.ArrayList;
import java.util.List;

import locus.api.android.ActionTools;
import locus.api.android.features.geocaching.fieldNotes.FieldNotesHelper;
import locus.api.android.features.periodicUpdates.UpdateContainer;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusInfo;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.Utils;
import locus.api.utils.Logger;

/**
 * Created by menion on 29/08/2016.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
public class PageUtilsFragment extends ABasePageFragment {

	@Override
	protected List<BasicAdapterItem> getItems() {
		List<BasicAdapterItem> items = new ArrayList<>();
		items.add(new BasicAdapterItem(1,
				"Send GPX file to system",
				"Send existing GPX file to system. This should invoke selection of an app that will handle this request."));
		items.add(new BasicAdapterItem(2,
				"Send GPX file directly to Locus",
				"You may also send intent (with a link to a file) directly to Locus app."));
		items.add(new BasicAdapterItem(3,
				"Pick location from Locus",
				"If you need 'location' in your application, this call allows you to use Locus 'GetLocation' screen. Result is handled in MainActivity as 'LocusUtils.isIntentGetLocation()'"));
		items.add(new BasicAdapterItem(4,
				"Pick file",
				"Allows to use Locus internal file picker and choose a file from the file system. You may also specify a filter on requested file. Request is sent as 'Activity.startActivityForResult()', so you have to handle the result in your own activity."));
		items.add(new BasicAdapterItem(5,
				"Pick directory",
				"Same as previous sample, just for picking directories instead of files."));
		items.add(new BasicAdapterItem(6,
				"Get ROOT directory",
				"Allows to get current active ROOT directory of installed Locus."));
		items.add(new BasicAdapterItem(7,
				"Add WMS map",
				"Allows to add WMS map directly to the list of WMS services."));
		items.add(new BasicAdapterItem(11,
				"Dashboard",
				"Very nice example that shows how your app may create its own dashboard filled with data received by Locus 'Periodic updates'"));
		items.add(new BasicAdapterItem(17,
				"Get fresh UpdateContainer",
				"Simple method how to get fresh UpdateContainer with new data ( no need for PeriodicUpdates )"));
		items.add(new BasicAdapterItem(12,
				"Show circles",
				"Small function that allows to draw circles on Locus map. This function is called as broadcast so check result in running Locus!"));
		items.add(new BasicAdapterItem(13,
				"Is Periodic update enabled",
				"Because periodic updates are useful in many cases, not just for the dashboard, this function allows to check if 'Periodic updates' are enabled in Locus."));
		items.add(new BasicAdapterItem(14,
				"Request available Geocaching field notes",
				"Simple method of getting number of existing field notes in Locus Map application"));
		items.add(new BasicAdapterItem(15,
				"Check item purchase state",
				"This function allows to check state of purchase of a certain item (with known ID) in Locus Store"));
		items.add(new BasicAdapterItem(16,
				"Display detail of Store item",
				"Display detail of a certain Locus Store item (with known ID)"));

		// TEMPORARY TEST ITEMS

		if (BuildConfig.DEBUG) {
			items.add(new BasicAdapterItem(100,
					"Simple performance test on LocusInfo",
					"Compare performance of old and new method to get LocusInfo object"));
		}
		return items;
	}

	@Override
	protected void onItemClicked(int itemId, LocusUtils.LocusVersion activeLocus)
			throws Exception{
		// handle action
		switch (itemId) {
			case 1:
				SampleCalls.callSendFileToSystem(getActivity());
				break;
			case 2:
				SampleCalls.callSendFileToLocus(getActivity(), activeLocus);
				break;
			case 3:
				SampleCalls.pickLocation(getActivity());
				break;
			case 4:
				// filter data so only visible will be GPX and KML files
				ActionTools.actionPickFile(getActivity(),
						0, "Give me a FILE!!",
						new String[] {".gpx", ".kml"});
				break;
			case 5:
				ActionTools.actionPickDir(getActivity(), 1);
				break;
			case 6:
				new AlertDialog.Builder(getActivity()).
						setTitle("Locus Root directory").
						setMessage("dir:" + SampleCalls.getRootDirectory(getActivity(), activeLocus) +
								"\n\n'null' means no required version installed or different problem").
						setPositiveButton("Close", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {}
						}).show();
				break;
			case 7:
				ActionTools.callAddNewWmsMap(getActivity(),
						"http://mapy.geology.cz/arcgis/services/Inspire/GM500K/MapServer/WMSServer");
				break;
			case 11:
				startActivity(new Intent(getActivity(), ActivityDashboard.class));
				break;
			case 12:
				SampleCalls.showCircles(getActivity());
				break;
			case 13:
				new AlertDialog.Builder(getActivity()).
						setTitle("Periodic update").
						setMessage("enabled:" + SampleCalls.isPeriodicUpdateEnabled(getActivity(), activeLocus)).
						setPositiveButton("Close", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {}
						}).show();
				break;
			case 14:
				int count = FieldNotesHelper.getCount(getActivity(), activeLocus);
				Toast.makeText(getActivity(),
						"Available field notes:" + count, Toast.LENGTH_LONG).show();
				break;
			case 15:
				// We test here if user has purchased "Add-on Field Notes Pro. Unique ID is defined on our Store
				// so it needs to be known for you before asking.
				int purchaseId = ActionTools.getItemPurchaseState(
						getActivity(), activeLocus, 5943264947470336L);
				if (purchaseId == LocusConst.PURCHASE_STATE_PURCHASED) {
					Toast.makeText(getActivity(),
							"Purchase item state: purchased", Toast.LENGTH_LONG).show();
				} else if (purchaseId == LocusConst.PURCHASE_STATE_NOT_PURCHASED) {
					Toast.makeText(getActivity(),
							"Purchase item state: not purchased", Toast.LENGTH_LONG).show();
				} else {
					// this usually means that user profile is not loaded. Best what to do is call
					// "displayLocusStoreItemDetail" to display item detail which also loads users
					// profile
					Toast.makeText(getActivity(),
							"Purchase item state:" + purchaseId, Toast.LENGTH_LONG).show();
				}
				break;
			case 16:
				// We display here Locus Store with certain item. In this case it is "Add-on Field Notes Pro.
				// Unique ID is defined on our Store so it needs to be known for you before asking.
				ActionTools.displayLocusStoreItemDetail(
						getActivity(), activeLocus, 5943264947470336L);
				break;
			case 17:
				UpdateContainer uc = ActionTools.getDataUpdateContainer(getActivity(), activeLocus);
				if (uc != null) {
					new AlertDialog.Builder(getActivity()).
							setTitle("Fresh UpdateContainer").
							setMessage("UC: " + Utils.toString(uc)).
							setPositiveButton("Close", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {}
							}).show();
				} else {
					Toast.makeText(getActivity(),
							"Unable to obtain UpdateContainer from " + activeLocus, Toast.LENGTH_LONG).show();
				}
				break;
			case 100:
				// test old method
				long timeStart = System.currentTimeMillis();
				for (int i = 0; i < 5000; i++) {
					LocusInfo li = ActionTools.getLocusInfo(getActivity(), activeLocus);
				}
				Logger.logD("PageUtilsFragment",
						"performance OLD: " + (System.currentTimeMillis() - timeStart) / 1000.0);

				// test new method
				timeStart = System.currentTimeMillis();
				for (int i = 0; i < 5000; i++) {
					LocusInfo li = ActionTools.getDataLocusInfo(getActivity(), activeLocus);
				}
				Logger.logD("PageUtilsFragment",
						"performance NEW: " + (System.currentTimeMillis() - timeStart) / 1000.0);

				// RESULT: SGS7, around 15ms per request, new version for 1ms faster
				break;
		}
	}
}
