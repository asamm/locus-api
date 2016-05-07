package locus.api.objects.geocaching;

import java.io.IOException;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

public class GeocachingImage extends Storable {
	
	// PARAMETERS
	
	/*
	 * Name of image
	 */
	private String mName;
	/*
	 * Defined description for image
	 */
	private String mDescription;
	/*
	 * URL to thumbnail of image. Usable for a quick overview 
	 */
	private String mThumbUrl;
	/*
	 * URL to full image. Usually is better to still use URL to some optimized
	 * (mobile) image, then to full version
	 */
	private String mUrl;

	public GeocachingImage() {
		super();
	}
	
    /**************************************************/
    /*                   PARAMETERS                   */
	/**************************************************/
	
	// NAME
	
	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	// DESCRIPTION
	
	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

	// THUMB URL
	
	public String getThumbUrl() {
		return mThumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.mThumbUrl = thumbUrl;
	}

	// URL
	
	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		this.mUrl = url;
	}	
	
    /**************************************************/
    /*                 STORABLE PART                  */
	/**************************************************/
	
	@Override
	protected int getVersion() {
		return 0;
	}

	@Override
	protected void readObject(int version, DataReaderBigEndian dr)
			throws IOException {
		mName = dr.readString();
		mDescription = dr.readString();
		mThumbUrl = dr.readString();
		mUrl = dr.readString();
	}

	@Override
	protected void writeObject(DataWriterBigEndian dw) throws IOException {
		dw.writeString(mName);
		dw.writeString(mDescription);
		dw.writeString(mThumbUrl);
		dw.writeString(mUrl);
	}

	@Override
	public void reset() {
		mName = "";
		mDescription = "";
		mThumbUrl = "";
		mUrl = "";
	}
}
