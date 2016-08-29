package com.asamm.locus.api.sample.pages;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.asamm.locus.api.sample.utils.BasicAdapter;
import com.asamm.locus.api.sample.utils.BasicAdapterItem;

import java.util.List;

import locus.api.android.utils.LocusUtils;
import locus.api.utils.Logger;

/**
 * Created by menion on 29/08/2016.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
public abstract class ABasePageFragment extends Fragment {

	// tag for logger
	private static final String TAG = "ABasePageFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// prepare adapter and ListView
		ListView lv = new ListView(getActivity());
		final List<BasicAdapterItem> items = getItems();
		BasicAdapter adapter = new BasicAdapter(getActivity(), items);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// check valid Locus version
				LocusUtils.LocusVersion activeLocus = LocusUtils.getActiveVersion(getActivity());
				if (activeLocus == null) {
					Toast.makeText(getActivity(),
							"Locus is not installed", Toast.LENGTH_LONG).show();
					return;
				}

				// handle event
				final BasicAdapterItem item = items.get(position);
				try {
					onItemClicked(item.id, activeLocus);
				} catch (Exception e) {
					Toast.makeText(getActivity(),
							"Problem with action:" + item.id, Toast.LENGTH_LONG).show();
					Logger.logE(TAG, "onItemClick(), " +
							"item:" + item.id + " failed");
				}
			}
		});

		// return layout
		return lv;
	}

	/**
	 * Get available features.
	 * @return list of features
	 */
	protected abstract List<BasicAdapterItem> getItems();

	/**
	 * Handle click event.
	 * @param itemId ID of item
	 * @param activeLocus active Locus Map application
	 * @throws Exception various exceptions
	 */
	protected abstract void onItemClicked(int itemId, LocusUtils.LocusVersion activeLocus)
			throws Exception;
}
