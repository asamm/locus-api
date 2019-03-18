package com.asamm.locus.api.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView

import com.asamm.locus.api.sample.receivers.PeriodicUpdateReceiver
import com.asamm.locus.api.sample.utils.SampleCalls

import java.text.SimpleDateFormat
import java.util.Date

import androidx.fragment.app.FragmentActivity
import locus.api.android.features.periodicUpdates.PeriodicUpdatesHandler
import locus.api.android.features.periodicUpdates.UpdateContainer
import locus.api.android.utils.LocusUtils
import locus.api.android.utils.LocusUtils.LocusVersion
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.utils.Logger

class ActivityDashboard : FragmentActivity() {

    // text containers
    private var tvInfo: TextView? = null
    private var tv01: TextView? = null
    private var tv02: TextView? = null
    private var tv03: TextView? = null
    private var tv04: TextView? = null
    private var tv05: TextView? = null
    private var tv06: TextView? = null
    private var tv07: TextView? = null
    private var tv08: TextView? = null

    // handler for updates
    private val updateHandler = object : PeriodicUpdatesHandler.OnUpdate {

        override fun onUpdate(locusVersion: LocusVersion, update: UpdateContainer) {
            handleUpdate(update)
        }

        override fun onIncorrectData() {}
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // prepare references to views
        tvInfo = findViewById(R.id.textView_info)
        tv01 = findViewById(R.id.textView1)
        tv02 = findViewById(R.id.textView2)
        tv03 = findViewById(R.id.textView3)
        tv04 = findViewById(R.id.textView4)
        tv05 = findViewById(R.id.textView5)
        tv06 = findViewById(R.id.textView6)
        tv07 = findViewById(R.id.textView7)
        tv08 = findViewById(R.id.textView8)
    }

    public override fun onStart() {
        super.onStart()

        // register update handler
        try {
            PeriodicUpdateReceiver.setOnUpdateListener(this, updateHandler)
        } catch (e: RequiredVersionMissingException) {
            Logger.logE(TAG, "onStart()", e)
        }

        // set info text
        handleUpdate(null)
    }

    public override fun onStop() {
        super.onStop()

        // clear reference to prevent memory leaks
        try {
            PeriodicUpdateReceiver.setOnUpdateListener(this, null)
        } catch (e: RequiredVersionMissingException) {
            Logger.logE(TAG, "onStop()", e)
        }

    }

    /**
     * Handle fresh data.
     * @param data received data
     */
    @SuppressLint("SetTextI18n")
    private fun handleUpdate(data: UpdateContainer?) {
        // check if data exists
        val activeVersion = LocusUtils.getActiveVersion(this)!!
        if (data == null) {

            // prepare text info
            val sb = StringBuilder()
            sb.append("UpdateContainer not valid\n\n")
            sb.append("- active version: ").append(activeVersion.versionName).append(" | ").append(activeVersion.versionCode).append("\n")
            sb.append("- Locus Map is running: ").append(if (SampleCalls.isRunning(this, activeVersion)) "running" else "stopped").append("\n")
            sb.append("- periodic updates: ").append(if (SampleCalls.isPeriodicUpdateEnabled(this, activeVersion)) "enabled" else "disabled").append("\n")

            // set text to field
            tvInfo!!.text = sb
            return
        }

        // refresh content
        tvInfo!!.text = "Fresh data received at ${SimpleDateFormat.getTimeInstance().format(Date())}\n" +
                "App: ${activeVersion.versionName}, battery:${data.deviceBatteryValue}"
        tv01!!.text = data.locMyLocation.getLatitude().toString()
        tv02!!.text = data.locMyLocation.getLongitude().toString()
        tv03!!.text = data.gpsSatsUsed.toString()
        tv04!!.text = data.gpsSatsAll.toString()
        tv05!!.text = data.isMapVisible.toString()
        tv06!!.text = data.locMyLocation.accuracy.toString()
        tv07!!.text = data.locMyLocation.bearing.toString()
        tv08!!.text = data.locMyLocation.speed.toString()
    }

    companion object {

        // tag for logger
        private const val TAG = "ActivityDashboard"
    }
}
