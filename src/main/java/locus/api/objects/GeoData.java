/*  
 * Copyright 2012, Asamm Software, s. r. o.
 * 
 * This file is part of LocusAPI.
 * 
 * LocusAPI is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * LocusAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *  
 * You should have received a copy of the Lesser GNU General Public
 * License along with LocusAPI. If not, see 
 * <http://www.gnu.org/licenses/lgpl.html/>.
 */

package locus.api.objects;

import java.io.IOException;

import locus.api.objects.extra.ExtraData;
import locus.api.objects.extra.ExtraStyle;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;

public abstract class GeoData extends Storable {

	private static final String TAG = "GeoData";
	
	// unique ID of this object
	public long id;
	
	// name of object, have to be unique
	protected String name;
	
	// time the data was created
	protected long timeCreated;
	
    // EXTRA CONTAINERS
    
	/* extra data */
	public ExtraData extraData;
	
	/* extra style for normal state */
	public ExtraStyle styleNormal;
	/* extra style for highlight state */
	public ExtraStyle styleHighlight;
	
	// PRIVATE PART
	
	/** normal state */
	public static final byte STATE_VISIBLE = 0;
	/** item is selected */
	public static final byte STATE_SELECTED = 1;
	/** item is disabled */
	public static final byte STATE_HIDDEN = 2;
	/** actual state */
	public byte state = STATE_VISIBLE;

	// MODE

	/**
	 * Define state of item (read-only/writable)
	 */
	public enum ReadWriteMode {

		/**
		 * Items is in read-only state
		 */
		READ_ONLY,
		/**
		 * Item is in read-write state
		 */
		READ_WRITE
	}

	// define read-write mode of item
	private ReadWriteMode mReadWriteMode;
	
	/**
	 * Additional temporary storage object. Object is not serialized!
	 * For Locus personal usage only
	 */
	public Object tag;
	
	// temporary variable for sorting
	public int dist;

	// CONSTRUCTORS

	public GeoData() {
		super();
	}
	
	public GeoData(DataReaderBigEndian dr) throws IOException {
		super(dr);
	}
	
	public GeoData(byte[] data) throws IOException {
		super(data);
	}
	
    /*******************************************/
    /*             STORABLE PART               */
    /*******************************************/
	
	protected void readExtraData(DataReaderBigEndian dr) throws IOException {
		if (dr.readBoolean()) {
			extraData = new ExtraData();
			extraData.read(dr);
		}
	}
	
	protected void writeExtraData(DataWriterBigEndian dw) throws IOException {
		if (extraData != null && extraData.getCount() > 0) {
			dw.writeBoolean(true);
			dw.writeStorable(extraData);
		} else {
			dw.writeBoolean(false);
		}
	}
	
	protected void readStyles(DataReaderBigEndian dr) throws IOException {
		if (dr.readBoolean()) {
			styleNormal = new ExtraStyle(dr);
		}
		if (dr.readBoolean()) {
			styleHighlight = new ExtraStyle(dr);
		}
	}
	
	protected void writeStyles(DataWriterBigEndian dw) throws IOException {
		if (styleNormal != null) {
			dw.writeBoolean(true);
			dw.writeStorable(styleNormal);
		} else {
			dw.writeBoolean(false);
		}
		
		if (styleHighlight != null) {
			dw.writeBoolean(true);
			dw.writeStorable(styleHighlight);
		} else {
			dw.writeBoolean(false);
		}
	}
	
	/**************************************/
	/*       MAIN GETTERS & SETTERS       */
	/**************************************/

	// ID

	/**
	 * Get ID of current item.
	 * @return ID of item
	 */
	public long getId() {
		return id;
	}

	/**
	 * Set new ID to current item.
	 * @param id value to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	// NAME
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null && name.length() > 0) {
			this.name = name;
		}
	}
	
	public long getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(long timeCreated) {
		this.timeCreated = timeCreated;
	}
	
	// EXTRA DATA

	public boolean hasExtraData() {
		return extraData != null;
	}
	
	public byte[] getExtraData() {
		try {
			DataWriterBigEndian dw = new DataWriterBigEndian();
			writeExtraData(dw);
			return dw.toByteArray();
		} catch (IOException e) {
			Logger.logE(TAG, "getExtraDataRaw()", e);
			return null;
		}
	}
	
	public void setExtraData(byte[] data) {
		try {
			readExtraData(new DataReaderBigEndian(data));
		} catch (Exception e) {
			Logger.logE(TAG, "setExtraData(" + data + ")", e);
			extraData = null;
		}
	}
	
	// EXTRA DATA - PARAMETERS
	
	// these are helper functions for more quick access
	// to parameter values without need to check state
	// of ExtraData object
	
	public boolean addParameter(int paramId, String param) {
        if (extraData == null) {
            extraData = new ExtraData();
        }
        return extraData.addParameter(paramId, param);
    }

    public boolean addParameter(int paramId, byte[] param) {
        if (extraData == null) {
            extraData = new ExtraData();
        }
        return extraData.addParameter(paramId, param);
    }

    public boolean addParameter(int paramId, int value) {
        if (extraData == null) {
            extraData = new ExtraData();
        }
        return extraData.addParameter(paramId, Integer.toString(value));
    }
	
	public boolean addParameter(int paramId, byte param) {
		if (extraData == null) {
			extraData = new ExtraData();
		}
		return extraData.addParameter(paramId, param);
	}

    /**
     * Return parameter stored in extraData container.
     * @param paramId ID of parameter
     * @return loaded value (length() bigger then 0) or 'null' in case, parameter do not exists
     */
	public String getParameter(int paramId) {
		if (extraData == null) {
			return null;
		}
		return extraData.getParameter(paramId);
	}
	
	public byte[] getParameterRaw(int paramId) {
		if (extraData == null) {
			return null;
		}
		return extraData.getParameterRaw(paramId);
	}

    /**
     * Check if current object has parameter defined by it's ID.
     * @param paramId ID of parameter
     * @return <code>true</code> if non-empty parameter exists
     */
	public boolean hasParameter(int paramId) {
	    // check existence of container
		if (extraData == null) {
			return false;
		}

        // check container itself
		return extraData.hasParameter(paramId);		
	}
	
	public String removeParameter(int paramId) {
		if (extraData == null) {
			return null;
		}
		return extraData.removeParameter(paramId);
	}

    /**************************************************/
	// SHORTCUTS FOR MOST USEFUL PARAMS
    /**************************************************/

    // PARAMETER 'SOURCE'

    /**
     * Get current defined source parameter.
     * @return source parameter or 'SOURCE_UNKNOWN' if not defined
     */
    public byte getParameterSource() {
        if (extraData == null) {
            return ExtraData.SOURCE_UNKNOWN;
        }
        byte[] res = extraData.getParameterRaw(ExtraData.PAR_SOURCE);
        if (res == null || res.length != 1) {
            return ExtraData.SOURCE_UNKNOWN;
        } else {
            return res[0];
        }
    }

    /**
     * Set parameter that define source of waypoint.
     * @param source source ID
     */
	public void setParameterSource(byte source) {
		addParameter(ExtraData.PAR_SOURCE, source);
	}

    /**
     * Check if waypoint source parameter is equal to expected value.
     * @param expectedSource source we are checking
     * @return <code>true</code> if waypoint source if same as expected
     */
	public boolean isParameterSource(byte expectedSource) {
		return getParameterSource() == expectedSource;
	}

    /**
     * Remove existing source parameter.
     */
	public void removeParameterSource() {
		removeParameter(ExtraData.PAR_SOURCE);
	}
	
	// PARAMETER 'STYLE'

	public String getParameterStyleName() {
		if (extraData == null) {
			return "";
		}
		return extraData.getParameter(ExtraData.PAR_STYLE_NAME);
	}

	public void setParameterStyleName(String style) {
		addParameter(ExtraData.PAR_STYLE_NAME, style);
	}
	
	public void removeParameterStyleName() {
		if (extraData == null) {
			return;
		}
		extraData.removeParameter(ExtraData.PAR_STYLE_NAME);
	}

    // PARAMETER 'DESCRIPTION'

    /**
     * Check if item has description parameter.
     * @return <code>true</code> if any description exists
     */
    public boolean hasParameterDescription() {
        return getParameterDescription().length() > 0;
    }

    /**
     * Get description parameter attached to this object.
     * @return description parameter of empty String if not defined
     */
    public String getParameterDescription() {
        if (extraData == null) {
            return "";
        }
        return extraData.getParameterNotNull(ExtraData.PAR_DESCRIPTION);
    }

    /**
     * Set new description value to this object.
     * @param desc new description value
     */
    public void setParameterDescription(String desc) {
        addParameter(ExtraData.PAR_DESCRIPTION, desc);
    }

    // PARAMETER 'EMAIL'

	public void addEmail(String email) {
		if (extraData == null) {
			extraData = new ExtraData();
		}
		extraData.addEmail(email);
	}

    // PARAMETER 'PHONE'

	public void addPhone(String phone) {
		addPhone("", phone);
	}
	
	public void addPhone(String label, String phone) {
		if (extraData == null) {
			extraData = new ExtraData();
		}
		extraData.addPhone(label, phone);
	}

    // PARAMETER 'URL'

	public void addUrl(String url) {
		if (extraData == null) {
			extraData = new ExtraData();
		}
		extraData.addUrl(url);
	}
	
	public void addUrl(String label, String url) {
		if (extraData == null) {
			extraData = new ExtraData();
		}
		extraData.addUrl(label, url);
	}

    // PARAMETER 'AUDIO' ATTACHMENT

    /**
     * Add audio to current object.
     * @param uri uri to add
     * @return <code>true</code> if audio was correctly added
     */
    public boolean addAttachmentAudio(String uri) {
        // check extra data
        boolean created = createExtraData();

        // add parameter and return result
        return afterItemAdded(extraData.addAudio(uri), created);
    }

    // PARAMETER 'PHOTO' ATTACHMENT

    /**
     * Add photo to current object.
     * @param uri uri to add
     * @return <code>true</code> if photo was correctly added
     */
	public boolean addAttachmentPhoto(String uri) {
        // check extra data
        boolean created = createExtraData();

        // add parameter and return result
        return afterItemAdded(extraData.addPhoto(uri), created);
	}

    // PARAMETER 'VIDEO' ATTACHMENT

    /**
     * Add video to current object.
     * @param uri uri to add
     * @return <code>true</code> if video was correctly added
     */
    public boolean addAttachmentVideo(String uri) {
        // check extra data
        boolean created = createExtraData();

        // add parameter and return result
        return afterItemAdded(extraData.addVideo(uri), created);
    }

    // PARAMETER 'OTHER' ATTACHMENT

    /**
     * Add video to current object.
     * @param uri uri to add
     * @return <code>true</code> if video was correctly added
     */
    public boolean addAttachmentOther(String uri) {
        // check extra data
        boolean created = createExtraData();

        // add parameter and return result
        return afterItemAdded(extraData.addOtherFile(uri), created);
    }
	
	// STYLES
	
	public byte[] getStyles() {
		try {
			DataWriterBigEndian dw = new DataWriterBigEndian();
			writeStyles(dw);
			return dw.toByteArray();
		} catch (IOException e) {
			Logger.logE(TAG, "getStylesRaw()", e);
			return null;
		}
	}

	public void setStyles(byte[] data) {
		try {
			readStyles(new DataReaderBigEndian(data));
		} catch (Exception e) {
			Logger.logE(TAG, "setExtraStyle(" + data + ")", e);
			extraData = null;
		}
	}

	// READ-WRITE MODE

	/**
	 * Get current defined read-write mode.
	 * @return current mode
	 */
	public ReadWriteMode getReadWriteMode() {
		if (mReadWriteMode == null) {
			return ReadWriteMode.READ_WRITE;
		}
		return mReadWriteMode;
	}

	/**
	 * Set mode for current item.
	 * @param mode new mode
	 */
	public void setReadWriteMode(ReadWriteMode mode) {
		this.mReadWriteMode = mode;
	}

    // EXTRA DATA TOOLS

    /**
     * Check if extra data exists and if not, create it.
     * @return <code>true</code> if container was created
     */
    private boolean createExtraData() {
        if (extraData == null) {
            extraData = new ExtraData();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Do some post-process work after item is added to container.
     * @param added <code>true</code> if item was added
     * @param created <code>true</code> if extra data were created before
     * @return <code>true</code> if all went well and item is stored
     */
    private boolean afterItemAdded(boolean added, boolean created) {
        // add parameter and return result
        if (added) {
            return true;
        } else {
            if (created) {
                extraData = null;
            }
            return false;
        }
    }
}
