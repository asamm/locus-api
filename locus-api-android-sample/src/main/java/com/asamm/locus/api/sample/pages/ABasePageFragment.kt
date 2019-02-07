/**
 * Created by menion on 29/08/2016.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package com.asamm.locus.api.sample.pages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.asamm.locus.api.sample.MainActivity
import com.asamm.locus.api.sample.utils.BasicAdapter
import com.asamm.locus.api.sample.utils.BasicAdapterItem
import locus.api.android.utils.LocusUtils
import locus.api.utils.Logger

abstract class ABasePageFragment : Fragment() {

    /**
     * Reference to main parent activity.
     */
    lateinit var act: MainActivity

    /**
     * Get available features.
     * @return list of features
     */
    protected abstract val items: List<BasicAdapterItem>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.act = context as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // prepare adapter and ListView
        val lv = ListView(act)
        val items = items
        val adapter = BasicAdapter(act, items)
        lv.adapter = adapter
        lv.setOnItemClickListener { _, _, position, _ ->
            // check valid Locus version
            val activeLocus = LocusUtils.getActiveVersion(act)
            if (activeLocus == null) {
                Toast.makeText(act,
                        "Locus is not installed", Toast.LENGTH_LONG).show()
                return@setOnItemClickListener
            }

            // handle event
            val item = items[position]
            try {
                onItemClicked(item.id, activeLocus)
            } catch (e: Exception) {
                Toast.makeText(act,
                        "Problem with action:" + item.id, Toast.LENGTH_LONG).show()
                Logger.logE(TAG, "onItemClick(), " +
                        "item:" + item.id + " failed")
            }
        }

        // return layout
        return lv
    }

    /**
     * Handle click event.
     * @param itemId ID of item
     * @param activeLocus active Locus Map application
     * @throws Exception various exceptions
     */
    @Throws(Exception::class)
    protected abstract fun onItemClicked(itemId: Int, activeLocus: LocusUtils.LocusVersion)

    companion object {

        // tag for logger
        private const val TAG = "ABasePageFragment"
    }
}
