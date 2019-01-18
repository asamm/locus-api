/**
 * Created by menion on 29/08/2016.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package com.asamm.locus.api.sample

import android.app.Application
import android.util.Log

import locus.api.utils.Logger

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // register logger so logs are forwarded to Android system
        Logger.registerLogger(object : Logger.ILogger {

            override fun logI(tag: String, msg: String) {
                Log.i(tag, msg)
            }

            override fun logD(tag: String, msg: String) {
                Log.d(tag, msg)
            }

            override fun logW(tag: String, msg: String) {
                Log.w(tag, msg)
            }

            override fun logE(tag: String, msg: String, e: Exception) {
                Log.e(tag, msg, e)
            }

            override fun logE(tag: String, msg: String) {
                Log.e(tag, msg)
            }
        })
    }
}
