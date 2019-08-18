/**
 * Created by menion on 29/08/2016.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package com.asamm.locus.api.sample.pages

import android.app.AlertDialog
import android.content.Intent
import android.widget.Toast
import com.asamm.locus.api.sample.ActivityDashboard
import com.asamm.locus.api.sample.BuildConfig
import com.asamm.locus.api.sample.utils.BasicAdapterItem
import com.asamm.locus.api.sample.utils.SampleCalls
import locus.api.android.ActionBasics
import locus.api.android.ActionFiles
import locus.api.android.features.geocaching.fieldNotes.FieldNotesHelper
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.LocusConst
import java.util.*

class PageUtilsFragment : ABasePageFragment() {

    override val items: List<BasicAdapterItem>
        get() {
            val items = ArrayList<BasicAdapterItem>()
            items.add(BasicAdapterItem(-1,
                    "Get basic info about Locus app",
                    "Basic checks on installed Locus apps."))
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
            items.add(BasicAdapterItem(19,
                    "Take a 'screenshot'",
                    "Take a bitmap screenshot of certain place in app"))
            items.add(BasicAdapterItem(20,
                    "New 'Action tasks' API",
                    "Suggest to test in split screen mode with active Locus Map"))

            // TEMPORARY TEST ITEMS

            if (BuildConfig.DEBUG) {
                // nothing to test
            }
            return items
        }

    @Throws(Exception::class)
    override fun onItemClicked(itemId: Int, activeLocus: LocusVersion) {
        // handle action
        when (itemId) {
            -1 -> SampleCalls.callDisplayLocusMapInfo(act)
            1 -> SampleCalls.callSendFileToSystem(act)
            2 -> SampleCalls.callSendFileToLocus(act, activeLocus)
            3 -> SampleCalls.pickLocation(act)
            4 ->
                // filter data so only visible will be GPX and KML files
                ActionFiles.actionPickFile(act,
                        0, "Give me a FILE!!",
                        arrayOf(".gpx", ".kml"))
            5 -> ActionFiles.actionPickDir(act, 1)
            6 -> AlertDialog.Builder(act)
                    .setTitle("Locus Root directory")
                    .setMessage("dir:" + SampleCalls.getRootDirectory(act, activeLocus) +
                            "\n\n'null' means no required version installed or different problem")
                    .setPositiveButton("Close") { _, _ -> }
                    .show()
            7 -> ActionBasics.callAddNewWmsMap(act,
                    "http://mapy.geology.cz/arcgis/services/Inspire/GM500K/MapServer/WMSServer")
            11 -> startActivity(Intent(act, ActivityDashboard::class.java))
            12 -> SampleCalls.showCircles(act)
            13 -> AlertDialog.Builder(act).setTitle("Periodic update")
                    .setMessage("enabled:" + SampleCalls.isPeriodicUpdateEnabled(act, activeLocus))
                    .setPositiveButton("Close") { _, _ -> }
                    .show()
            14 -> {
                val count = FieldNotesHelper.getCount(act, activeLocus)
                Toast.makeText(act,
                        "Available field notes:$count", Toast.LENGTH_LONG).show()
            }
            15 -> {
                // We test here if user has purchased "Add-on Field Notes Pro. Unique ID is defined on our Store
                // so it needs to be known for you before asking.
                val purchaseId = ActionBasics.getItemPurchaseState(
                        act, activeLocus, 5943264947470336L)
                when (purchaseId) {
                    LocusConst.PURCHASE_STATE_PURCHASED -> Toast.makeText(act,
                            "Purchase item state: purchased", Toast.LENGTH_LONG).show()
                    LocusConst.PURCHASE_STATE_NOT_PURCHASED -> Toast.makeText(act,
                            "Purchase item state: not purchased", Toast.LENGTH_LONG).show()
                    else -> // this usually means that user profile is not loaded. Best what to do is call
                        // "displayLocusStoreItemDetail" to display item detail which also loads users
                        // profile
                        Toast.makeText(act,
                                "Purchase item state: $purchaseId", Toast.LENGTH_LONG).show()
                }
            }
            16 ->
                // We display here Locus Store with certain item. In this case it is "Add-on Field Notes Pro.
                // Unique ID is defined on our Store so it needs to be known for you before asking.
                ActionBasics.displayLocusStoreItemDetail(
                        act, activeLocus, 5943264947470336L)
            17 -> {
                val uc = ActionBasics.getUpdateContainer(act, activeLocus)
                if (uc != null) {
                    AlertDialog.Builder(act)
                            .setTitle("Fresh UpdateContainer")
                            .setMessage("UC: $uc")
                            .setPositiveButton("Close") { _, _ -> }
                            .show()
                } else {
                    Toast.makeText(act,
                            "Unable to obtain UpdateContainer from $activeLocus", Toast.LENGTH_LONG).show()
                }
            }
            19 -> {
                @Suppress("ReplaceSingleLineLet")
                act.supportFragmentManager
                        .beginTransaction()
                        .add(MapFragment(), "MAP_FRAGMENT")
                        .commit()
            }
            20 -> {
                @Suppress("ReplaceSingleLineLet")
                act.supportFragmentManager
                        .beginTransaction()
                        .add(PageBroadcastApiSamples(), "BROADCAST_API_FRAGMENT")
                        .commit()
            }
        }
    }
}
