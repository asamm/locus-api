package locus.api.android.features.mapProvider.data;

import android.graphics.Bitmap;

import java.io.IOException;

import locus.api.android.utils.Utils;
import locus.api.android.utils.UtilsBitmap;
import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

public class MapTileResponse extends Storable {

	public static final int CODE_UNKNOWN 			= 0;
	public static final int CODE_VALID				= 1;

	public static final int CODE_INVALID_REQUEST	= 2;
	public static final int CODE_NOT_EXISTS			= 3;
	public static final int CODE_INTERNAL_ERROR		= 4;
	
	/*
	 * Result code that indicate result of whole operation
	 */
	private int mResultCode;
	/*
	 * Image itself
	 */
	private Bitmap mImage;
	
	// BASIC CONSTRUCTORS
	
	public MapTileResponse() {
		super();
	}
	
	public MapTileResponse(byte[] data) throws IOException {
		super(data);
	}
	
	// GET & SET METHODS
	
	public int getResultCode() {
		return mResultCode;
	}

	public void setResultCode(int resultCode) {
		this.mResultCode = resultCode;
	}

	public Bitmap getImage() {
		return mImage;
	}

	public void setImage(Bitmap image) {
		this.mImage = image;
	}	
	
	/**************************************************/
	/*                 STORABLE PART                  */
	/**************************************************/
	
	@Override
	protected int getVersion() {
		return 0;
	}

	@Override
	public void reset() {
		mResultCode = CODE_UNKNOWN;
		mImage = null;
	}

	@Override
	protected void readObject(int version, DataReaderBigEndian dr)
			throws IOException {
		mResultCode = dr.readInt();
		
		// icon
		int size = 0;
		if ((size = dr.readInt()) > 0) {
			byte[] data = dr.readBytes(size);
			mImage = UtilsBitmap.getBitmap(data);
		} else {
			mImage = null;
		}
	}

	@Override
	protected void writeObject(DataWriterBigEndian dw) throws IOException {
		dw.writeInt(mResultCode);
		
		// icon
		if (mImage == null) {
			dw.writeInt(0);
		} else {
			byte[] data = UtilsBitmap.getBitmap(mImage, Bitmap.CompressFormat.PNG);
			if (data == null || data.length == 0) {
				dw.writeInt(0);
			} else {
				dw.writeInt(data.length);
				dw.write(data);
			}
		}
	}
}
