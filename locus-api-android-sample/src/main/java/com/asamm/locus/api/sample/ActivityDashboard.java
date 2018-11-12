package com.asamm.locus.api.sample;

import android.os.Bundle;
import android.widget.TextView;

import com.asamm.locus.api.sample.receivers.PeriodicUpdateReceiver;
import com.asamm.locus.api.sample.utils.SampleCalls;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.fragment.app.FragmentActivity;
import locus.api.android.features.periodicUpdates.PeriodicUpdatesHandler;
import locus.api.android.features.periodicUpdates.UpdateContainer;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.LocusUtils.LocusVersion;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.utils.Logger;

public class ActivityDashboard extends FragmentActivity {

	// tag for logger
	private static final String TAG = "ActivityDashboard";

	// text containers
	private TextView tvInfo;
	private TextView tv01;
	private TextView tv02;
	private TextView tv03;
	private TextView tv04;
	private TextView tv05;
	private TextView tv06;
	private TextView tv07;
	private TextView tv08;

	// handler for updates
	private PeriodicUpdatesHandler.OnUpdate updateHandler =
			new PeriodicUpdatesHandler.OnUpdate() {

				@Override
				public void onUpdate(LocusVersion locusVersion, UpdateContainer update) {
					handleUpdate(update);
				}

				@Override
				public void onIncorrectData() {}
			};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);

		// prepare references to views
		tvInfo = findViewById(R.id.textView_info);
		tv01 = findViewById(R.id.textView1);
		tv02 = findViewById(R.id.textView2);
		tv03 = findViewById(R.id.textView3);
		tv04 = findViewById(R.id.textView4);
		tv05 = findViewById(R.id.textView5);
		tv06 = findViewById(R.id.textView6);
		tv07 = findViewById(R.id.textView7);
		tv08 = findViewById(R.id.textView8);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// register update handler
		try {
			PeriodicUpdateReceiver.setOnUpdateListener(this, updateHandler);
		} catch (RequiredVersionMissingException e) {
			Logger.logE(TAG, "onStart()", e);
		}

		// set info text
		handleUpdate(null);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		// clear reference to prevent memory leaks
		try {
			PeriodicUpdateReceiver.setOnUpdateListener(this, null);
		} catch (RequiredVersionMissingException e) {
			Logger.logE(TAG, "onStop()", e);
		}
	}

	/**
	 * Handle fresh data.
	 * @param data received data
	 */
	private void handleUpdate(UpdateContainer data) {
		// check if data exists
		if (data == null) {
			LocusVersion activeVersion = LocusUtils.getActiveVersion(this);

			// prepare text info
			StringBuilder sb = new StringBuilder();
			sb.append("UpdateContainer not valid\n\n");
			sb.append("- active version: ").
					append(activeVersion.getVersionName()).
					append(" | ").
					append(activeVersion.getVersionCode()).
					append("\n");
			sb.append("- Locus Map is running: ").
					append(SampleCalls.isRunning(this, activeVersion) ? "running" : "stopped").
					append("\n");
			sb.append("- periodic updates: ").
					append(SampleCalls.isPeriodicUpdateEnabled(this, activeVersion) ? "enabled" : "disabled").
					append("\n");

			// set text to field
			tvInfo.setText(sb);
			return;
		}
		
		// refresh content
		tvInfo.setText("Fresh data received at " +
				SimpleDateFormat.getTimeInstance().format(new Date()));
		tv01.setText(String.valueOf(data.getLocMyLocation().getLatitude()));
		tv02.setText(String.valueOf(data.getLocMyLocation().getLongitude()));
		tv03.setText(String.valueOf(data.getGpsSatsUsed()));
		tv04.setText(String.valueOf(data.getGpsSatsAll()));
		tv05.setText(String.valueOf(data.isMapVisible()));
		tv06.setText(String.valueOf(data.getLocMyLocation().getAccuracy()));
		tv07.setText(String.valueOf(data.getLocMyLocation().getBearing()));
		tv08.setText(String.valueOf(data.getLocMyLocation().getSpeed()));
	}
}
