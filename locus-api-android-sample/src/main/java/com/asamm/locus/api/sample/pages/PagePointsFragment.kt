/**
 * Created by menion on 29/08/2016.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package com.asamm.locus.api.sample.pages

import com.asamm.locus.api.sample.utils.BasicAdapterItem
import com.asamm.locus.api.sample.utils.SampleCalls
import locus.api.android.objects.LocusVersion
import java.util.*

class PagePointsFragment : ABasePageFragment() {

    override val items: List<BasicAdapterItem>
        get() {
            val items = ArrayList<BasicAdapterItem>()
            items.add(BasicAdapterItem(1,
                    "Import one point",
                    "Send one simple point to Locus and execute 'import' request. If Locus does not run, this intent starts it."))
            items.add(BasicAdapterItem(2,
                    "Import more points at once",
                    "One of possibilities how to send data to Locus and execute 'import'. Number of points is limited by capacity of Intent. On 2.X devices - 1 MB, on 4.X+ devices - 2 MB."))
            items.add(BasicAdapterItem(3,
                    "Display one point with icon",
                    "Send one simple point to Locus together with icon defined as bitmap."))
            items.add(BasicAdapterItem(4,
                    "Display more points with icons",
                    "Send more points with various attached icons (all as bitmaps)."))
            items.add(BasicAdapterItem(5,
                    "Display Geocaching point",
                    "Geocache points behave a little bit different in Locus. This sample shows how to create it and how to display it on a map."))
            items.add(BasicAdapterItem(6,
                    "Display more Geocaches over intent",
                    "Improved version where we send more geocache points at once."))
            items.add(BasicAdapterItem(7,
                    "Display more Geocaches over local File",
                    "Second way how to send data (not just geocaches) to Locus is over a file. It is limited only by the device memory space for every app because Locus loads all data at once. Method is slower than \"intent\" only method but limits on number of points are not so strict."))
            items.add(BasicAdapterItem(8,
                    "Display point with `onDisplay callback`",
                    "Display simple point on the map. When user tap on your point, you will be notified about it. You may then supply additional information and send it back to Locus before 'Point screen' appears."))
            items.add(BasicAdapterItem(9,
                    "Display point with custom button",
                    "Display simple point on the map. After user display point detail, in bottom menu will be custom define action button."))
            items.add(BasicAdapterItem(10,
                    "Request ID of a point by its name",
                    "Allows to search in Locus internal point database for point by its name. Results in a list of points (its IDs) that match requested name."))
            items.add(BasicAdapterItem(11,
                    "Display 'Point screen' of a certain point",
                    "Allows to display main 'Point screen' of a certain point defined by its ID."))
            return items
        }

    @Throws(Exception::class)
    override fun onItemClicked(itemId: Int, activeLocus: LocusVersion) {
        when (itemId) {
            1 -> SampleCalls.callSendOnePoint(act)
            2 -> SampleCalls.callSendMorePoints(act)
            3 -> SampleCalls.callSendOnePointWithIcon(act)
            4 -> SampleCalls.callSendMorePointsWithIcons(act)
            5 -> SampleCalls.callSendOnePointGeocache(act)
            6 -> SampleCalls.callSendMorePointsGeocacheIntentMethod(act)
            7 -> SampleCalls.callSendMorePointsGeocacheFileMethod(act)
            8 -> SampleCalls.callSendOnePointWithCallbackOnDisplay(act)
            9 -> SampleCalls.callSendOnePointWithExtraCallback(act)
            10 -> SampleCalls.callRequestPointIdByName(act, activeLocus)
            11 -> SampleCalls.callRequestDisplayPointScreen(act, activeLocus, 3)
        }
    }
}
