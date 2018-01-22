package com.asamm.locus.api.sample.pages

import android.app.AlertDialog
import android.content.Intent
import android.widget.ImageView
import android.widget.Toast
import com.asamm.locus.api.sample.ActivityDashboard
import com.asamm.locus.api.sample.BuildConfig
import com.asamm.locus.api.sample.utils.BasicAdapterItem
import com.asamm.locus.api.sample.utils.SampleCalls
import locus.api.android.ActionMapTools
import locus.api.android.ActionTools
import locus.api.android.MapPreviewParams
import locus.api.android.features.geocaching.fieldNotes.FieldNotesHelper
import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils
import locus.api.android.utils.Utils
import locus.api.objects.extra.Location
import locus.api.utils.Logger
import java.util.*

/**
 * Created by menion on 29/08/2016.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
class PageUtilsFragment : ABasePageFragment() {

    override fun getItems(): List<BasicAdapterItem> {
        val items = ArrayList<BasicAdapterItem>()
        items.add(BasicAdapterItem(1,
                "Send GPX file to system",
                "Send existing GPX file to system. This should invoke selection of an app that will handle this request."))
        items.add(BasicAdapterItem(2,
                "Send GPX file directly to Locus",
                "You may also send intent (with a link to a file) directly to Locus app."))
        items.add(BasicAdapterItem(3,
                "Pick location from Locus",
                "If you need 'location' in your application, this call allows you to use Locus 'GetLocation' screen. Result is handled in MainActivity as 'LocusUtils.isIntentGetLocation()'"))
        items.add(BasicAdapterItem(4,
                "Pick file",
                "Allows to use Locus internal file picker and choose a file from the file system. You may also specify a filter on requested file. Request is sent as 'Activity.startActivityForResult()', so you have to handle the result in your own activity."))
        items.add(BasicAdapterItem(5,
                "Pick directory",
                "Same as previous sample, just for picking directories instead of files."))
        items.add(BasicAdapterItem(6,
                "Get ROOT directory",
                "Allows to get current active ROOT directory of installed Locus."))
        items.add(BasicAdapterItem(7,
                "Add WMS map",
                "Allows to add WMS map directly to the list of WMS services."))
        items.add(BasicAdapterItem(11,
                "Dashboard",
                "Very nice example that shows how your app may create its own dashboard filled with data received by Locus 'Periodic updates'"))
        items.add(BasicAdapterItem(17,
                "Get fresh UpdateContainer",
                "Simple method how to get fresh UpdateContainer with new data ( no need for PeriodicUpdates )"))
        items.add(BasicAdapterItem(12,
                "Show circles",
                "Small function that allows to draw circles on Locus map. This function is called as broadcast so check result in running Locus!"))
        items.add(BasicAdapterItem(13,
                "Is Periodic update enabled",
                "Because periodic updates are useful in many cases, not just for the dashboard, this function allows to check if 'Periodic updates' are enabled in Locus."))
        items.add(BasicAdapterItem(14,
                "Request available Geocaching field notes",
                "Simple method of getting number of existing field notes in Locus Map application"))
        items.add(BasicAdapterItem(15,
                "Check item purchase state",
                "This function allows to check state of purchase of a certain item (with known ID) in Locus Store"))
        items.add(BasicAdapterItem(16,
                "Display detail of Store item",
                "Display detail of a certain Locus Store item (with known ID)"))
        items.add(BasicAdapterItem(18,
                "Take a 'screenshot (deprecated)'",
                "Take a bitmap screenshot of certain place in app"))
        items.add(BasicAdapterItem(19,
                "Take a 'screenshot (new)'",
                "Take a bitmap screenshot of certain place in app"))

        // TEMPORARY TEST ITEMS

        if (BuildConfig.DEBUG) {
            items.add(BasicAdapterItem(100,
                    "Simple performance test on LocusInfo",
                    "Compare performance of old and new method to get LocusInfo object"))
        }
        return items
    }

    @Throws(Exception::class)
    override fun onItemClicked(itemId: Int, activeLocus: LocusUtils.LocusVersion) {
        // handle action
        when (itemId) {
            1 -> SampleCalls.callSendFileToSystem(activity)
            2 -> SampleCalls.callSendFileToLocus(activity, activeLocus)
            3 -> SampleCalls.pickLocation(activity)
            4 ->
                // filter data so only visible will be GPX and KML files
                ActionTools.actionPickFile(activity,
                        0, "Give me a FILE!!",
                        arrayOf(".gpx", ".kml"))
            5 -> ActionTools.actionPickDir(activity, 1)
            6 -> AlertDialog.Builder(activity)
                    .setTitle("Locus Root directory")
                    .setMessage("dir:" + SampleCalls.getRootDirectory(activity, activeLocus) +
                    "\n\n'null' means no required version installed or different problem")
                    .setPositiveButton("Close") { dialog, which -> }
                    .show()
            7 -> ActionTools.callAddNewWmsMap(activity,
                    "http://mapy.geology.cz/arcgis/services/Inspire/GM500K/MapServer/WMSServer")
            11 -> startActivity(Intent(activity, ActivityDashboard::class.java))
            12 -> SampleCalls.showCircles(activity)
            13 -> AlertDialog.Builder(activity).setTitle("Periodic update")
                    .setMessage("enabled:" + SampleCalls.isPeriodicUpdateEnabled(activity, activeLocus))
                    .setPositiveButton("Close") { dialog, which -> }
                    .show()
            14 -> {
                val count = FieldNotesHelper.getCount(activity, activeLocus)
                Toast.makeText(activity,
                        "Available field notes:" + count, Toast.LENGTH_LONG).show()
            }
            15 -> {
                // We test here if user has purchased "Add-on Field Notes Pro. Unique ID is defined on our Store
                // so it needs to be known for you before asking.
                val purchaseId = ActionTools.getItemPurchaseState(
                        activity, activeLocus, 5943264947470336L)
                when (purchaseId) {
                    LocusConst.PURCHASE_STATE_PURCHASED -> Toast.makeText(activity,
                            "Purchase item state: purchased", Toast.LENGTH_LONG).show()
                    LocusConst.PURCHASE_STATE_NOT_PURCHASED -> Toast.makeText(activity,
                            "Purchase item state: not purchased", Toast.LENGTH_LONG).show()
                    else -> // this usually means that user profile is not loaded. Best what to do is call
                        // "displayLocusStoreItemDetail" to display item detail which also loads users
                        // profile
                        Toast.makeText(activity,
                                "Purchase item state:" + purchaseId, Toast.LENGTH_LONG).show()
                }
            }
            16 ->
                // We display here Locus Store with certain item. In this case it is "Add-on Field Notes Pro.
                // Unique ID is defined on our Store so it needs to be known for you before asking.
                ActionTools.displayLocusStoreItemDetail(
                        activity, activeLocus, 5943264947470336L)
            17 -> {
                val uc = ActionTools.getDataUpdateContainer(activity, activeLocus)
                if (uc != null) {
                    AlertDialog.Builder(activity)
                            .setTitle("Fresh UpdateContainer")
                            .setMessage("UC: " + Utils.toString(uc))
                            .setPositiveButton("Close") { dialog, which -> }
                            .show()
                } else {
                    Toast.makeText(activity,
                            "Unable to obtain UpdateContainer from " + activeLocus, Toast.LENGTH_LONG).show()
                }
            }
            18 -> {
                val result = ActionTools.getMapPreview(activity, activeLocus,
                        Location(50.0, 14.0), 12, 512, 512, false)
                if (result == null || !result.isValid) {
                    AlertDialog.Builder(activity)
                            .setTitle("Unable to obtain map preview")
                            .setPositiveButton("Close") { dialog, which -> }
                            .show()
                } else {
                    val iv = ImageView(activity)
                    iv.setImageBitmap(result.image)
                    AlertDialog.Builder(activity)
                            .setTitle("Image loaded")
                            .setMessage("Not yet loaded tiles: " + result.numOfNotYetLoadedTiles)
                            .setView(iv)
                            .setPositiveButton("Close") { dialog, which -> }.show()
                }
            }
            19 -> {
                // prepare parameters
                val params = MapPreviewParams().apply {
                    locCenter = Location(50.0, 14.0)
                    zoom = 12
                    offsetX = 50
                    offsetY = 50
                    widthPx = 512
                    heightPx = 512
                    densityDpi = resources.displayMetrics.densityDpi
                }

                // get and display preview
                val result = ActionMapTools.getMapPreview(activity!!, activeLocus, params)
                if (result != null && result.isValid()) {
                    val iv = ImageView(activity)
                    iv.setImageBitmap(result.getAsImage())
                    AlertDialog.Builder(activity)
                            .setTitle("Image loaded")
                            .setMessage("Not yet loaded tiles: " + result.numOfNotYetLoadedTiles)
                            .setView(iv)
                            .setPositiveButton("Close") { dialog, which -> }
                            .show()
                } else {
                    AlertDialog.Builder(activity)
                            .setTitle("Unable to obtain map preview")
                            .setPositiveButton("Close") { dialog, which -> }
                            .show()
                }
            }
            100 -> {
                // test old method
                var timeStart = System.currentTimeMillis()
                for (i in 0..4999) {
                    val li = ActionTools.getLocusInfo(activity, activeLocus)
                }
                Logger.logD("PageUtilsFragment",
                        "performance OLD: " + (System.currentTimeMillis() - timeStart) / 1000.0)

                // test new method
                timeStart = System.currentTimeMillis()
                for (i in 0..4999) {
                    val li = ActionTools.getDataLocusInfo(activity, activeLocus)
                }
                Logger.logD("PageUtilsFragment",
                        "performance NEW: " + (System.currentTimeMillis() - timeStart) / 1000.0)
            }
        }
    }
}
