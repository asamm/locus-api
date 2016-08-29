package locus.api.objects.geocaching;

import java.io.IOException;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;

public class GeocachingImage extends Storable {

	// tag for logger
	private static final String TAG = "GeocachingImage";

	// PARAMETERS
	
	// name of image
	private String mName;
	// defined description for image
	private String mDescription;
	// URL to thumbnail of image. Usable for a quick overview
	private String mThumbUrl;
	// URL to full image. Usually is better to still use URL to some optimized
	// (mobile) image, then to full version
	private String mUrl;

	public GeocachingImage() {
		super();
	}
	
    /**************************************************/
    // GET & SET
	/**************************************************/
	
	// NAME

	/**
	 * Get name of current image.
	 * @return readable name of image
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Define name for this image.
	 * @param name name of image
	 */
	public void setName(String name) {
		if (name == null) {
			Logger.logD(TAG, "setName(), empty parameter");
			name = "";
		}
		this.mName = name;
	}

	// DESCRIPTION

	/**
	 * Get optional description for current image.
	 * @return description
	 */
	public String getDescription() {
		return mDescription;
	}

	/**
	 * Set description for current image.
	 * @param description optional description
	 */
	public void setDescription(String description) {
		if (description == null) {
			Logger.logD(TAG, "setDescription(), empty parameter");
			description = "";
		}
		this.mDescription = description;
	}

	// THUMB URL

	/**
	 * Get URL to thumbnail image.
	 * @return url to thumbnail
	 */
	public String getThumbUrl() {
		return mThumbUrl;
	}

	/**
	 * Define url to smaller version (thumbnail) of an image.
	 * @param thumbUrl url to thumbnail
	 */
	public void setThumbUrl(String thumbUrl) {
		if (thumbUrl == null) {
			Logger.logD(TAG, "setThumbUrl(), empty parameter");
			thumbUrl = "";
		}
		this.mThumbUrl = thumbUrl;
	}

	// URL

	/**
	 * Get URL to main image.
	 * @return url to main image
	 */
	public String getUrl() {
		return mUrl;
	}

	/**
	 * Define URL to main image.
	 * @param url url to main image
	 */
	public void setUrl(String url) {
		if (url == null) {
			Logger.logD(TAG, "setUrl(), empty parameter");
			url = "";
		}
		this.mUrl = url;
	}	
	
    /**************************************************/
    // STORABLE PART
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
