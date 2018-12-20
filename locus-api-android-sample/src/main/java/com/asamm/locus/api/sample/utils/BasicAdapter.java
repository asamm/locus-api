package com.asamm.locus.api.sample.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.asamm.locus.api.sample.R;

import java.util.List;

public class BasicAdapter extends ArrayAdapter<BasicAdapterItem> {

	// main inflater
	private LayoutInflater inflater;
	
	public BasicAdapter(Context ctx, List<BasicAdapterItem> items) {
		super(ctx, 0, items);
		this.inflater = (LayoutInflater) 
				ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	
	@Override
	public long getItemId(int pos) {
		return getItem(pos).id;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		return prepareView(position, convertView, parent);
	}
	
    private View prepareView(final int position, View convertView, ViewGroup parent) {
		// prepare item
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.basic_list_item, parent, false);
		}

		// get current item
		BasicAdapterItem item = getItem(position);

		// set new item
		TextView tvTitle = (TextView)
				convertView.findViewById(R.id.text_view_title);
		tvTitle.setText(item.name);
		
		TextView tvDesc = (TextView) 
				convertView.findViewById(R.id.text_view_desc);
		if (item.desc.length() > 0) {
			tvDesc.setVisibility(View.VISIBLE);
			tvDesc.setText(item.desc);
		} else {
			tvDesc.setVisibility(View.GONE);
		}
		
		// return filled view
		return convertView;
	}
}
