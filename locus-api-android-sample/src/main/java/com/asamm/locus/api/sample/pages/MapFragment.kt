/**
 * Created by menion on 25/01/2018.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package com.asamm.locus.api.sample.pages

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import locus.api.android.ActionMapTools
import locus.api.android.MapPreviewParams
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.LocusUtils
import locus.api.objects.extra.Location
import locus.api.utils.Logger

class MapFragment : DialogFragment() {

    // current version
    private lateinit var lv: LocusVersion
    // base map view drawer
    private lateinit var mapView: MapView
    // detector for gestures
    private var detector: GestureDetector? = null

    // base dimension
    private val imgDimen = 512
    // handler for map loading
    private var loader: Thread? = null
    // total applied map offset in X dimension
    private var mapOffsetX = 0
    // total applied map offset in Y dimension
    private var mapOffsetY = 0

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        lv = LocusUtils.getActiveVersion(ctx)
                ?: throw IllegalStateException("No active Locus-based application")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // prepare container for a map
        val container = object : FrameLayout(activity!!) {

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouchEvent(event: MotionEvent): Boolean {
                // request reload for up-event only
                val action = event.action and MotionEvent.ACTION_MASK
                if (action == MotionEvent.ACTION_UP) {
                    loadMap()
                }

                // handle event
                return if (detector?.onTouchEvent(event) == true) {
                    true
                } else {
                    super.onTouchEvent(event)
                }
            }
        }
        mapView = MapView(activity!!)
        mapView.layoutParams = FrameLayout.LayoutParams(2 * imgDimen, 3 * imgDimen)
                .apply { gravity = Gravity.CENTER }
        mapView.post {
            loadMap()
        }
        container.addView(mapView)

        // setup handler
        initializeHandler()

        // create and display dialog
        return AlertDialog.Builder(activity!!)
                .setTitle("Map preview")
                .setMessage("")
                .setView(container)
                .setPositiveButton("Close") { _, _ -> }
                .create()
    }

    private fun initializeHandler() {
        detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                loader = null
                mapOffsetX += distanceX.toInt()
                mapOffsetY += distanceY.toInt()
                mapView.offsetX += distanceX.toInt()
                mapView.offsetY += distanceY.toInt()
                mapView.invalidate()
                return true
            }

            override fun onDown(event: MotionEvent): Boolean {
                return true
            }

            override fun onFling(event1: MotionEvent, event2: MotionEvent,
                    velocityX: Float, velocityY: Float): Boolean {
                return true
            }
        })
    }

    override fun onStop() {
        super.onStop()

        // stop loader
        loader = null
    }

    /**
     * Perform load of map preview.
     */
    private fun loadMap() {
        loader = Thread(Runnable {
            Logger.logD("MapFragment", "loadMap()")

            // prepare parameters
            val params = MapPreviewParams().apply {
                locCenter = Location(52.35, 1.5)
                zoom = 14
                offsetX = mapOffsetX
                offsetY = mapOffsetY
                widthPx = mapView.width
                heightPx = mapView.height
                densityDpi = resources.displayMetrics.densityDpi

                // test rotation
//                rotate = true
//                rotation = 30
            }

            // get and display preview
            val result = ActionMapTools.getMapPreview(activity!!, lv, params)
            if (Thread.currentThread() == loader) {
                activity?.runOnUiThread {
                    if (result != null && result.isValid()) {
                        (dialog as AlertDialog).setMessage("Missing tiles: ${result.numOfNotYetLoadedTiles}")
                        mapView.img = result.getAsImage()
                        mapView.offsetX = 0
                        mapView.offsetY = 0
                        mapView.invalidate()

                        // call recursively if missing tiles
                        if (result.numOfNotYetLoadedTiles > 0) {
                            Thread.sleep(2000)
                            loadMap()
                        }
                    } else {
                        (dialog as AlertDialog).setMessage("Unable to obtain map preview")
                    }
                }
            } else {
                Logger.logD("MapFragment", "old request invalid")
            }
        })
        loader?.start()
    }

    inner class MapView(ctx: Context) : ImageView(ctx) {

        // map image to draw
        var img: Bitmap? = null
        // temporary offset for draw in X axis
        var offsetX: Int = 0
        // temporary offset for draw in Y axis
        var offsetY: Int = 0

        // paint object for a text
        private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            textSize = 18.0f
            textAlign = Paint.Align.CENTER
        }

        override fun onDraw(c: Canvas) {
            super.onDraw(c)

            // draw empty view
            if (img == null) {
                c.drawText("Loading map ...",
                        width / 2.0f, height / 2.0f, paintText)
                return
            }

            // draw map
            c.drawBitmap(img, -1.0f * offsetX, -1.0f * offsetY, null)
        }
    }
}