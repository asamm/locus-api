package com.asamm.locus.api.sample.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import com.asamm.locus.api.sample.R

class BasicAdapter(ctx: Context, items: List<BasicAdapterItem>) : ArrayAdapter<BasicAdapterItem>(ctx, 0, items) {

    // main inflater
    private val inflater: LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItemId(pos: Int): Long {
        return getItem(pos)!!.id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return prepareView(position, convertView, parent)
    }

    private fun prepareView(position: Int, convertView: View?, parent: ViewGroup): View {
        // prepare item
        val finalView = convertView
                ?: inflater.inflate(R.layout.basic_list_item, parent, false)

        // get current item
        val item = getItem(position)

        // set new item
        val tvTitle = finalView!!.findViewById<View>(R.id.text_view_title) as TextView
        tvTitle.text = item!!.name

        val tvDesc = finalView.findViewById<View>(R.id.text_view_desc) as TextView
        if (item.desc.isNotEmpty()) {
            tvDesc.visibility = View.VISIBLE
            tvDesc.text = item.desc
        } else {
            tvDesc.visibility = View.GONE
        }

        // return filled view
        return finalView
    }
}
