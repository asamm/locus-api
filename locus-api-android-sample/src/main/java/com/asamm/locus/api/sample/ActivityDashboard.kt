package com.asamm.locus.api.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.asamm.locus.api.sample.utils.SampleCalls
import locus.api.android.ActionBasics
import locus.api.android.features.periodicUpdates.UpdateContainer
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.LocusUtils
import locus.api.utils.Logger
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Sample activity that display method, how to periodically fetch base data from Locus Map
 * and display it, in this case, for example as simple dashboard.
 */
class ActivityDashboard : FragmentActivity() {

    // refresh handler
    private val handler: Handler = Handler(Looper.getMainLooper())
    // refresh interval (in ms)
    private val refreshInterval = TimeUnit.SECONDS.toMillis(1)
    // refresh task itself
    private val refresh: (() -> Unit) = {
        refreshContent()
    }

    // text containers
    private lateinit var tvInfo: TextView
    private lateinit var tv01: TextView
    private lateinit var tv02: TextView
    private lateinit var tv03: TextView
    private lateinit var tv04: TextView
    private lateinit var tv05: TextView
    private lateinit var tv06: TextView
    private lateinit var tv07: TextView
    private lateinit var tv08: TextView

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

        // start refresh
        handler.post(refresh)
    }

    public override fun onStop() {
        super.onStop()

        // stop refresh flow
        handler.removeCallbacks(refresh)
    }

    /**
     * Perform refresh and request another update after defined interval.
     */
    private fun refreshContent() {
        try {
            Thread {
                LocusUtils.getActiveVersion(this)?.let { lv ->
                    ActionBasics.getUpdateContainer(this, lv)?.let { uc ->
                        handleUpdate(lv, uc)
                    } ?: {
                        handleUpdate(lv, null)
                        Logger.logW(TAG, "refreshContent(), " +
                                "unable to obtain `UpdateContainer`")
                    }()
                } ?: {
                    handleUpdate(null, null)
                    Logger.logW(TAG, "refreshContent(), " +
                            "unable to obtain `ActiveVersion`")
                }()

            }.start()
        } finally {
            handler.postDelayed(refresh, refreshInterval)
        }
    }

    /**
     * Handle fresh data.
     *
     * @param lv current Locus version
     * @param uc received container
     */
    @SuppressLint("SetTextI18n")
    private fun handleUpdate(lv: LocusVersion?, uc: UpdateContainer?) {
        handler.post {
            // check if uc exists
            if (lv == null || uc == null) {
                // prepare text info
                val sb = StringBuilder()
                sb.append("UpdateContainer not valid\n\n")
                sb.append("- active version: ")
                        .append(lv?.versionName).append(" | ").append(lv?.versionCode)
                        .append("\n")
                sb.append("- Locus Map is running: ")
                        .append(if (lv?.let { SampleCalls.isRunning(this, it) } == true) "running" else "stopped")
                        .append("\n")
                sb.append("- periodic updates: ")
                        .append(if (lv?.let { SampleCalls.isPeriodicUpdateEnabled(this, it) } == true) "enabled" else "disabled")
                        .append("\n")

                // set text to field
                tvInfo.text = sb
            } else {
                // refresh content
                tvInfo.text = "Fresh uc received at ${SimpleDateFormat.getTimeInstance().format(Date())}\n" +
                        "App: ${lv.versionName}, battery:${uc.deviceBatteryValue}"
                tv01.text = uc.locMyLocation.latitude.toString()
                tv02.text = uc.locMyLocation.longitude.toString()
                tv03.text = uc.gpsSatsUsed.toString()
                tv04.text = uc.gpsSatsAll.toString()
                tv05.text = uc.isMapVisible.toString()
                tv06.text = uc.locMyLocation.accuracyHor.value.toString()
                tv07.text = uc.locMyLocation.bearing.value.toString()
                tv08.text = uc.locMyLocation.speed.value.toString()
            }
        }
    }

    companion object {

        // tag for logger
        private const val TAG = "ActivityDashboard"
    }
}
