package com.asamm.locus.api.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.asamm.locus.api.sample.utils.SampleCalls;

import java.text.SimpleDateFormat;
import java.util.Date;

import locus.api.android.features.periodicUpdates.PeriodicUpdatesHandler;
import locus.api.android.features.periodicUpdates.UpdateContainer;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.LocusUtils.LocusVersion;

public class ActivityDashboard extends FragmentActivity {

	// receiver for events
	private BroadcastReceiver receiver;

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

		tvInfo = (TextView) findViewById(R.id.textView_info);
		tv01 = (TextView) findViewById(R.id.textView1);
		tv02 = (TextView) findViewById(R.id.textView2);
		tv03 = (TextView) findViewById(R.id.textView3);
		tv04 = (TextView) findViewById(R.id.textView4);
		tv05 = (TextView) findViewById(R.id.textView5);
		tv06 = (TextView) findViewById(R.id.textView6);
		tv07 = (TextView) findViewById(R.id.textView7);
		tv08 = (TextView) findViewById(R.id.textView8);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// prepare receiver
		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (isFinishing()) {
					return;
				}
				
				// handle received data
				PeriodicUpdatesHandler.getInstance().onReceive(
						ActivityDashboard.this,
						intent, updateHandler);
			}
		};

		// register receiver
		IntentFilter filter = new IntentFilter(
				LocusConst.ACTION_PERIODIC_UPDATE);
		registerReceiver(receiver, filter);

		// set info text
		handleUpdate(null);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		// set receiver
		if (receiver != null) {
			unregisterReceiver(receiver);
			receiver = null;
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
