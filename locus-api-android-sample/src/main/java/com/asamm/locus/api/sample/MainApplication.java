package com.asamm.locus.api.sample;

import android.app.Application;
import android.util.Log;

import locus.api.utils.Logger;

/**
 * Created by menion on 29/08/2016.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
public class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// register logger so logs are forwarded to Android system
		Logger.registerLogger(new Logger.ILogger() {

			@Override
			public void logI(String tag, String msg) {
				Log.i(tag, msg);
			}

			@Override
			public void logD(String tag, String msg) {
				Log.d(tag, msg);
			}

			@Override
			public void logW(String tag, String msg) {
				Log.w(tag, msg);
			}

			@Override
			public void logE(String tag, String msg, Exception e) {
				Log.e(tag, msg, e);
			}

			@Override
			public void logE(String tag, String msg) {
				Log.e(tag, msg);
			}
		});
	}
}
