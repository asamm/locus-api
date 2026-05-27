/**
 * Created by menion on 29/08/2016.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package com.asamm.locus.api.sample

import android.app.Application
import android.util.Log
import com.asamm.logger.Logger

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // setup logger system
        Logger.registerLogger(object : Logger.ILogger {

            override fun logD(ex: Throwable?, tag: String, msg: String, vararg args: Any) {
                Log.d(tag, msg.format(args), ex)
            }

            override fun logE(ex: Throwable?, tag: String, msg: String, vararg args: Any) {
                Log.e(tag, msg.format(args), ex)
            }

            override fun logI(tag: String, msg: String, vararg args: Any) {
                Log.i(tag, msg.format(args))
            }

            override fun logV(tag: String, msg: String, vararg args: Any) {
                Log.v(tag, msg.format(args))
            }

            override fun logW(ex: Throwable?, tag: String, msg: String, vararg args: Any) {
                Log.w(tag, msg.format(args), ex)
            }
        })
    }
}
