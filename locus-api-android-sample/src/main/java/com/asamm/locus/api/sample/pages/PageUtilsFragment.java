package com.asamm.locus.api.sample.pages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.asamm.locus.api.sample.ActivityDashboard;
import com.asamm.locus.api.sample.utils.BasicAdapterItem;
import com.asamm.locus.api.sample.utils.SampleCalls;

import java.util.ArrayList;
import java.util.List;

import locus.api.android.ActionTools;
import locus.api.android.features.geocaching.fieldNotes.FieldNotesHelper;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils;

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
				"Send existing GPX file to system. This should invoke chooser for app, that will handle this request."));
		items.add(new BasicAdapterItem(2,
				"Send GPX file directly to Locus",
				"You may also send intent (with link to file) directly to Locus app."));
		items.add(new BasicAdapterItem(3,
				"Pick location from Locus",
				"If you need a 'location' in your application, this call allows you to use Locus 'GetLocation' screen. Result is handled in MainActivity as 'LocusUtils.isIntentGetLocation()'"));
		items.add(new BasicAdapterItem(4,
				"Pick file",
				"Allows to use Locus internal file picker and choose a file from file system. You may also specify a filter on requested file. Request is send as 'Activity.startActivityForResult()', so you have to handle result in own activity."));
		items.add(new BasicAdapterItem(5,
				"Pick directory",
				"Same as previous sample, just for picking a directories instead of files."));
		items.add(new BasicAdapterItem(6,
				"Get ROOT directory",
				"Allows to get current active ROOT directory of installed Locus."));
		items.add(new BasicAdapterItem(7,
				"Add WMS map",
				"Allows to add WMS map directly to list of WMS services."));
		items.add(new BasicAdapterItem(11,
				"Dashboard",
				"Very nice example that shows, how you app may create own dashboard filled with data received by Locus 'Periodic updates'"));
		items.add(new BasicAdapterItem(12,
				"Show circles",
				"Small function that allows to draw circles on Locus map. This function is called as broadcast so check result in running Locus!"));
		items.add(new BasicAdapterItem(13,
				"Is Periodic update enabled",
				"Because periodic updates is useful in many cases, not just for dashboard, this function allows to check if 'Periodic updates' are enabled in Locus."));
		items.add(new BasicAdapterItem(14,
				"Request available Geocaching field notes",
				"Sample method that get number of existing field notes in Locus Map application"));
		items.add(new BasicAdapterItem(15,
				"Check item purchase state",
				"This function allows to check state of purchase of certain item (with known ID) in Locus Store"));
		items.add(new BasicAdapterItem(16,
				"Display detail of Store item",
				"Display detail of certain Locus Store item (with known ID)"));
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
		}
	}
}
