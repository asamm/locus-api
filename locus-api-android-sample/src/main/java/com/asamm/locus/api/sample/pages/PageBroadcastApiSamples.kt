/****************************************************************************
 *
 * Created by menion on 18/12/2018.
 * Copyright (c) 2018. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/

package com.asamm.locus.api.sample.pages

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.asamm.locus.api.sample.utils.BasicAdapter
import com.asamm.locus.api.sample.utils.BasicAdapterItem
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.LocusUtils
import locus.api.utils.Logger
import java.util.*

class PageBroadcastApiSamples : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity!!)
                .setView(createContent())
                .setPositiveButton("Close") { _, _ -> }
                .create()
    }

    private fun createContent(): ListView {
        // prepare adapter and ListView
        val lv = ListView(activity)
        val items = getItems()
        val adapter = BasicAdapter(activity!!, items)
        lv.adapter = adapter
        lv.setOnItemClickListener { _, _, position, _ ->
            // check valid Locus version
            val activeLocus = LocusUtils.getActiveVersion(activity!!)
            if (activeLocus == null) {
                Toast.makeText(activity,
                        "Locus is not installed", Toast.LENGTH_LONG).show()
                return@setOnItemClickListener
            }

            // handle event
            val item = items[position]
            try {
                onItemClicked(item.id, activeLocus)
            } catch (e: Exception) {
                Toast.makeText(activity,
                        "Problem with action:" + item.id, Toast.LENGTH_LONG).show()
                Logger.logE("PageBroadcastApiSamples",
                        "onItemClick(), item:" + item.id + " failed")
            }
        }

        // return layout
        return lv
    }

    private fun getItems(): MutableList<BasicAdapterItem> {
        val items = ArrayList<BasicAdapterItem>()
        items.add(BasicAdapterItem(1, "Toggle centering"))
        items.add(BasicAdapterItem(2, "Move map top-left"))
        items.add(BasicAdapterItem(3, "Move map bottom-right"))
        items.add(BasicAdapterItem(4, "Zoom map in"))
        items.add(BasicAdapterItem(5, "Zoom map out"))
        items.add(BasicAdapterItem(10, "Open map manager"))
        items.add(BasicAdapterItem(11, "Open quick action menu"))
        return items
    }

    private fun onItemClicked(itemId: Int, activeLocus: LocusVersion) {
        when (itemId) {
            1 -> activity?.sendBroadcast(Intent("com.asamm.locus.ACTION_TASK").apply {
                setPackage(activeLocus.packageName)
                putExtra("tasks", "{ map_center: { action: \"toggle\" } }")
            })
            2 -> activity?.sendBroadcast(Intent("com.asamm.locus.ACTION_TASK").apply {
                setPackage(activeLocus.packageName)
                putExtra("tasks", "{ map_move_x: { value: -25, unit: \"%\" }, map_move_y: { value: -25, unit: \"%\" } }")
            })
            3 -> activity?.sendBroadcast(Intent("com.asamm.locus.ACTION_TASK").apply {
                setPackage(activeLocus.packageName)
                putExtra("tasks", "{ map_move_x: { value: 25, unit: \"%\" }, map_move_y: { value: 25, unit: \"%\" } }")
            })
            4 -> activity?.sendBroadcast(Intent("com.asamm.locus.ACTION_TASK").apply {
                setPackage(activeLocus.packageName)
                putExtra("tasks", "{ map_zoom: { action: \"+\" } }")
            })
            5 -> activity?.sendBroadcast(Intent("com.asamm.locus.ACTION_TASK").apply {
                setPackage(activeLocus.packageName)
                putExtra("tasks", "{ map_zoom: { action: \"-\" } }")
            })
            10 -> activity?.sendBroadcast(Intent("com.asamm.locus.ACTION_TASK").apply {
                setPackage(activeLocus.packageName)
                putExtra("tasks", "{ function: { value: \"screen_maps\" } }")
            })
            11 -> activity?.sendBroadcast(Intent("com.asamm.locus.ACTION_TASK").apply {
                setPackage(activeLocus.packageName)
                putExtra("tasks", "{ function: { value: \"quick_action_menu\" } }")
            })
        }
    }
}