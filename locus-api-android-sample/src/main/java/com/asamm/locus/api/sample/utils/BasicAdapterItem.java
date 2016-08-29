package com.asamm.locus.api.sample.utils;

public class BasicAdapterItem {

	public final int id;
	public final CharSequence name;
	public final CharSequence desc;
	
	public BasicAdapterItem(int id, CharSequence name) {
		this.id = id;
		this.name = name;
		this.desc = "";
	}
	
	public BasicAdapterItem(int id, CharSequence name, CharSequence desc) {
		this.id = id;
		this.name = name;
		this.desc = desc;
	}
}
