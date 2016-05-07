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

package locus.api.objects.extra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;
import locus.api.utils.SparseArrayCompat;
import locus.api.utils.Utils;

public class ExtraData extends Storable {

	private static final String TAG = "ExtraData";

	// DATA SOURCE DEFINED IN PARAMETER 'PAR_SOURCE'
	
	/**
	 * Source unknown or undefined 
	 */
	public static final byte SOURCE_UNKNOWN 					                = 48; // "0"
	/**
	 * Special point for parking service 
	 */
	public static final byte SOURCE_PARKING_SERVICE 			                = 49; // "1"
	/**
	 * Additional waypoint for geocache 
	 */
	public static final byte SOURCE_GEOCACHING_WAYPOINT 		                = 50; // "2"
	/**
	 * Temporary point on map (not stored in database) 
	 */
	public static final byte SOURCE_MAP_TEMP 					                = 51; // "3"
	/**
	 * Waypoint on route, location with some more values 
	 */
	public static final byte SOURCE_ROUTE_WAYPOINT 				                = 52; // "4"
	/**
	 * Only location on route
	 */
	public static final byte SOURCE_ROUTE_LOCATION 				                = 53; // "5"
	/**
	 * Point coming from OpenStreetBugs 
	 */
	public static final byte SOURCE_OPENSTREETBUGS 				                = 55; // "7"
	/**
	 * Temporary item do not display on map
	 */
	public static final byte SOURCE_INVISIBLE 					                = 56; // "8"
	/**
	 * Items automatically loaded from OSM POI database
	 */
	public static final byte SOURCE_POI_OSM_DB 					                = 57; // "9"
    /**
     * Items loaded from Munzee service
     */
    public static final byte SOURCE_MUNZEE 					                    = 58; // ":"
	/**
	 * Items loaded from Live-tracking service
	 */
	public static final byte SOURCE_LIVE_TRACKING 					            = 59;


    // ROUTE TYPES DEFINED IN PARAMETER 'PAR_RTE_COMPUTE_TYPE'
	
	public static final int VALUE_RTE_TYPE_GENERATED = -1;

	public static final int VALUE_RTE_TYPE_CAR                                  = 6;
	public static final int VALUE_RTE_TYPE_CAR_FAST                             = 0;
	public static final int VALUE_RTE_TYPE_CAR_SHORT                            = 1;
    public static final int VALUE_RTE_TYPE_MOTORCYCLE                           = 7;
	public static final int VALUE_RTE_TYPE_CYCLE                                = 2;
	public static final int VALUE_RTE_TYPE_CYCLE_FAST                           = 4;
	public static final int VALUE_RTE_TYPE_CYCLE_SHORT                          = 5;
    public static final int VALUE_RTE_TYPE_CYCLE_MTB                            = 8;
    public static final int VALUE_RTE_TYPE_CYCLE_RACING                         = 9;
	public static final int VALUE_RTE_TYPE_FOOT                                 = 3;

    /**
     * All possible RTE_TYPES also sorted in correct order.
     */
    public static final int[] RTE_TYPES_SORTED = new int[] {
            VALUE_RTE_TYPE_CAR,
            VALUE_RTE_TYPE_CAR_FAST,
            VALUE_RTE_TYPE_CAR_SHORT,
            VALUE_RTE_TYPE_MOTORCYCLE,
            VALUE_RTE_TYPE_CYCLE,
            VALUE_RTE_TYPE_CYCLE_FAST,
            VALUE_RTE_TYPE_CYCLE_SHORT,
            VALUE_RTE_TYPE_CYCLE_MTB,
            VALUE_RTE_TYPE_CYCLE_RACING,
            VALUE_RTE_TYPE_FOOT,
    };

	// ROUTE ACTIONS DEFINED IN PARAMETER 'PAR_RTE_POINT_ACTION'

	/** No maneuver occurs here */
	public static final int VALUE_RTE_ACTION_NO_MANEUVER 			            = 0;
	/** Continue straight */
	public static final int VALUE_RTE_ACTION_CONTINUE_STRAIGHT 		            = 1;
	/** No maneuver occurs here. Road name changes */
	public static final int VALUE_RTE_ACTION_NO_MANEUVER_NAME_CHANGE            = 2;
	/** Make a slight left */
	public static final int VALUE_RTE_ACTION_LEFT_SLIGHT 			            = 3;
	/** Turn left */
	public static final int VALUE_RTE_ACTION_LEFT 					            = 4;
	/** Make a sharp left */
	public static final int VALUE_RTE_ACTION_LEFT_SHARP 			            = 5;
	/** Make a slight right */
	public static final int VALUE_RTE_ACTION_RIGHT_SLIGHT			            = 6;
	/** Turn right */
	public static final int VALUE_RTE_ACTION_RIGHT 					            = 7;
	/** Make a sharp right */
	public static final int VALUE_RTE_ACTION_RIGHT_SHARP			            = 8;
	/** Stay left */
	public static final int VALUE_RTE_ACTION_STAY_LEFT 				            = 9;
	/** Stay right */
	public static final int VALUE_RTE_ACTION_STAY_RIGHT 			            = 10;
	/** Stay straight */
	public static final int VALUE_RTE_ACTION_STAY_STRAIGHT 			            = 11;
	/** Make a U-turn */
	public static final int VALUE_RTE_ACTION_U_TURN 				            = 12;
	/** Make a left U-turn */
	public static final int VALUE_RTE_ACTION_U_TURN_LEFT 			            = 13;
	/** Make a right U-turn */
	public static final int VALUE_RTE_ACTION_U_TURN_RIGHT 			            = 14;
	/** Exit left */
	public static final int VALUE_RTE_ACTION_EXIT_LEFT 				            = 15;
	/** Exit right */
	public static final int VALUE_RTE_ACTION_EXIT_RIGHT 			            = 16;
	/** Take the ramp on the left */
	public static final int VALUE_RTE_ACTION_RAMP_ON_LEFT 			            = 17;
	/** Take the ramp on the right */
	public static final int VALUE_RTE_ACTION_RAMP_ON_RIGHT 			            = 18;
	/** Take the ramp straight ahead */
	public static final int VALUE_RTE_ACTION_RAMP_STRAIGHT 			            = 19;
	/** Merge left */
	public static final int VALUE_RTE_ACTION_MERGE_LEFT 			            = 20;
	/** Merge right */
	public static final int VALUE_RTE_ACTION_MERGE_RIGHT 			            = 21;
	/** Merge */
	public static final int VALUE_RTE_ACTION_MERGE 					            = 22;
	/** Enter state/province */
	public static final int VALUE_RTE_ACTION_ENTER_STATE 			            = 23;
	/** Arrive at your destination */
	public static final int VALUE_RTE_ACTION_ARRIVE_DEST 			            = 24;
	/** Arrive at your destination on the left */
	public static final int VALUE_RTE_ACTION_ARRIVE_DEST_LEFT 		            = 25;
	/** Arrive at your destination on the right */
	public static final int VALUE_RTE_ACTION_ARRIVE_DEST_RIGHT 		            = 26;
	/** Enter the roundabout and take the 1st exit */
	public static final int VALUE_RTE_ACTION_ROUNDABOUT_EXIT_1 		            = 27;
	/** Enter the roundabout and take the 2nd exit */
	public static final int VALUE_RTE_ACTION_ROUNDABOUT_EXIT_2 		            = 28;
	/** Enter the roundabout and take the 3rd exit */
	public static final int VALUE_RTE_ACTION_ROUNDABOUT_EXIT_3 		            = 29;
	/** Enter the roundabout and take the 4th exit */
	public static final int VALUE_RTE_ACTION_ROUNDABOUT_EXIT_4 		            = 30;
	/** Enter the roundabout and take the 5th exit */
	public static final int VALUE_RTE_ACTION_ROUNDABOUT_EXIT_5 		            = 31;
	/** Enter the roundabout and take the 6th exit */
	public static final int VALUE_RTE_ACTION_ROUNDABOUT_EXIT_6 		            = 32;
	/** Enter the roundabout and take the 7th exit */
	public static final int VALUE_RTE_ACTION_ROUNDABOUT_EXIT_7 		            = 33;
	/** Enter the roundabout and take the 8th exit */
	public static final int VALUE_RTE_ACTION_ROUNDABOUT_EXIT_8 		            = 34;
	/** Pass POI */
	public static final int VALUE_RTE_ACTION_PASS_PLACE 			            = 50;
	
	// PRIVATE REFERENCES (0 - 29)
	
	public static final int PAR_SOURCE 								            = 0;

	/**
	 * Private parameter for handling of styles. 
	 * Use getter/setter directly in GeoData object
	 */
	public static final int PAR_STYLE_NAME 							            = 5;
	/**
	 * Computed are value. Used mainly for tracks
	 */
	public static final int PAR_AREA_SIZE 							            = 12;
	/**
	 * Extra data for offline POI database
	 */
	public static final int PAR_DB_POI_EXTRA_DATA					            = 13;
    /**
     * ID of KML trip to which item belongs
     */
    public static final int PAR_KML_TRIP_ID                                     = 14;
    /**
     * Reference to original Google Places item.
     */
	public static final int PAR_GOOGLE_PLACES_REFERENCE 			            = 15;
    /**
     * Google Places rating.
     */
	public static final int PAR_GOOGLE_PLACES_RATING 				            = 16;
    /**
     * Google places details.
     */
	public static final int PAR_GOOGLE_PLACES_DETAILS 				            = 17;

	public static final int PAR_INTENT_EXTRA_CALLBACK 				            = 20;
	public static final int PAR_INTENT_EXTRA_ON_DISPLAY 			            = 21;

	// PUBLIC VALUES (30 - 49)
	
	// visible description
	public static final int PAR_DESCRIPTION 						            = 30;
	// storage for comments
	public static final int PAR_COMMENT 							            = 31;
	// relative path to working dir (for images for example)
	public static final int PAR_RELATIVE_WORKING_DIR 				            = 32;
	// Type (classification) of the waypoint.
	public static final int PAR_TYPE 								            = 33;
	// Special code used in Geocache waypoints
	public static final int PAR_GEOCACHE_CODE						            = 34;
	
	// LOCATION PARAMETERS (50 - 59)

    // street name
	public static final int PAR_ADDRESS_STREET                                  = 50;
	// city name
    public static final int PAR_ADDRESS_CITY                                    = 51;
	// name of region
    public static final int PAR_ADDRESS_REGION                                  = 52;
	// PSČ, post code number
    public static final int PAR_ADDRESS_POST_CODE                               = 53;
	// name of country
    public static final int PAR_ADDRESS_COUNTRY                                 = 54;
	
	// ROUTE PARAMETERS (100 - 199)
	
	// PARAMETERS FOR NAVIGATION POINTS (WAYPOINT)
	
	/**
	 * Index to point list 
	 * <br />
	 * Locus internal variable, <b>DO NOT SET</b>
	 */
	public static final int PAR_RTE_INDEX = 100;
	/**
	 * Distance (in metres) from current navPoint to next 
	 * <br />
	 * Locus internal variable, <b>DO NOT SET</b> (float)
	 */
	public static final int PAR_RTE_DISTANCE_F = 101;
	/**
	 * time (in sec) from current navPoint to next (integer)
	 */
	public static final int PAR_RTE_TIME_I = 102;
	/** 
	 * speed (in m/s) from current navPoint to next (float) 
	 */
	public static final int PAR_RTE_SPEED_F = 103;
	/** 
	 *  Number of seconds to transition between successive links along
	 *  the route. These take into account the geometry of the intersection,
	 *  number of links at the intersection, and types of roads at
	 *  the intersection. This attempts to estimate the time in seconds it
	 *  would take for stops, or places where a vehicle must slow to make a turn.
	 */
	public static final int PAR_RTE_TURN_COST = 104;
	/**
	 * String representation of next street label
	 */
	public static final int PAR_RTE_STREET = 109;
	/**
	 * used to determine which type of action should be taken in order to stay on route
	 */
	public static final int PAR_RTE_POINT_ACTION = 110;

	// PARAMETERS FOR NAVIGATION ROUTE (TRACK)
	
	/** type of route (car_fast, car_short, cyclo, foot) */
	public static final int PAR_RTE_COMPUTE_TYPE = 120;
	/**
	 * Roundabout is usually defined from two points. First on enter correctly
	 * defined by ACTION 27 - 34, second on exit simply defined by exit angle.
	 * In case of usage only exit point, it's need to set this flag
	 */
	public static final int PAR_RTE_SIMPLE_ROUNDABOUTS = 121;
	
	// OSM BUGS (300 - 309)
	public static final int PAR_OSM_NOTES_ID = 301;
	public static final int PAR_OSM_NOTES_CLOSED = 302;
	
	// PHONES (1000 - 1099)
	private static final int PAR_PHONE_MIN = 1000;
	private static final int PAR_PHONE_MAX = 1099;
	
	// EMAILS (1100 - 1199)
	private static final int PAR_EMAIL_MIN = 1100;
	private static final int PAR_EMAIL_MAX = 1199;
	
	// URLS (1200 - 1299)
	private static final int PAR_URL_MIN = 1200;
	private static final int PAR_URL_MAX = 1299;
	
	// PHOTOS (1300 - 1399)
	private static final int PAR_PHOTO_MIN = 1300;
	private static final int PAR_PHOTO_MAX = 1399;
	
	// VIDEO (1400 - 1499)
	private static final int PAR_VIDEO_MIN = 1400;
	private static final int PAR_VIDEO_MAX = 1499;
	
	// AUDIO (1500 - 1599)
	private static final int PAR_AUDIO_MIN = 1500;
	private static final int PAR_AUDIO_MAX = 1599;
	
	// OTHER FILES (1800 - 1999)
	private static final int PAR_OTHER_FILES_MIN = 1800;
	private static final int PAR_OTHER_FILES_MAX = 1999;
	
	/** table for additional parameters */
	SparseArrayCompat<byte[]> parameters;

	public static class LabelTextContainer {
		
		public final String label;
		public final String text;
		
		public LabelTextContainer(String value) {
			if (value.contains("|")) {
				int index = value.indexOf("|");
				this.label = value.substring(0, index);
				this.text = value.substring(index + 1);
			} else {
				this.label = "";
				this.text = value;
			}
		}
		
		public LabelTextContainer(String label, String text) {
			if (label == null) {
				this.label = "";
			} else {
				this.label = label;
			}
			this.text = text;
		}
		
		public String getAsText() {
			if (label.length() > 0) {
				return label + "|" + text;
			} else {
				return text;
			}
		}
		
		public String getFormattedAsEmail() {
			String lab = label.length() == 0 ? text : label;
			return "<a href=\"mailto:" + text + "\">" + lab + "</a>";
		}
		
		public String getFormattedAsPhone() {
			String lab = label.length() == 0 ? text : label;
			return "<a href=\"tel:" + text + "\">" + lab + "</a>";
		}
		
		public String getFormattedAsUrl(boolean checkProtocol) {
			String lab = label.length() == 0 ? text : label;
			String url = text;
			if (checkProtocol && !url.contains("://")) {
				url = "http://" + url;
			}
			return "<a href=\"" + url + "\" target=\"_blank\">" + lab + "</a>";
		}
	}

	public ExtraData() {
		super();
	}
	
	public ExtraData(byte[] data) throws IOException {
		super(data);
	}
	
    /*******************************************/
    /*             STORABLE PART               */
    /*******************************************/
	
	@Override
	protected int getVersion() {
		return 0;
	}

	@Override
	protected void readObject(int version, DataReaderBigEndian dr)
			throws IOException {
		int size = dr.readInt();
		parameters.clear();
		for (int i = 0; i < size; i++) {
			int key = dr.readInt();
			parameters.put(key, dr.readBytes(dr.readInt()));
		}
	}

	@Override
	protected void writeObject(DataWriterBigEndian dw) throws IOException {
		dw.writeInt(parameters.size());
		for (int i = 0, m = parameters.size(); i < m; i++) {
			int key = parameters.keyAt(i);
			dw.writeInt(key);
			byte[] data = parameters.valueAt(i);
			dw.writeInt(data.length);
			if (data.length > 0) {
				dw.write(data);
			}
		}
	}

	@Override
	public void reset() {
		parameters = new SparseArrayCompat<>();
	}
	
    /**************************************************/
    // HANDLERS PART
    /**************************************************/
	
	public boolean addParameter(int key, String value) {
		// check on 'null' value
		if (value == null) {
			return false;
		}

		// remove previous parameter
		removeParameter(key);
		
		// trim new value and insert into table
		value = value.trim();
		if (value.length() == 0) {
			return false;
		}
		
		// check keys
		if (key > 1000 && key < 2000) {
			Logger.logW(TAG, "addParam(" + key + ", " + value + "), values 1000 - 1999 reserved!");
			return false;
		}
		
		// finally insert value
		parameters.put(key, Utils.doStringToBytes(value));
		return true;
	}
	
	public boolean addParameter(int key, byte value) {
		return addParameter(key, new byte[] {value});
	}
		
	public boolean addParameter(int key, byte[] value) {
		// remove previous parameter
		removeParameter(key);
				
		// trim new value and insert into table
		if (value == null || value.length == 0) {
			return false;
		}
		
		// check keys
		if (key > 1000 && key < 2000) {
			Logger.logW(TAG, "addParam(" + key + ", " + value + "), values 1000 - 1999 reserved!");
			return false;
		}
		
		// finally insert value
		parameters.put(key, value);
		return true;
	}

	
	public String getParameter(int key) {
		byte[] data = parameters.get(key);
		if (data != null) {
			return Utils.doBytesToString(data);
		} else {
			return null;
		}
	}

	/**
	 * Return Raw data from storage. Do not modify these data directly. For
	 * fast access is provided original array, not any copy!
	 * @param key key ID
	 * @return raw parameters data
	 */
	public byte[] getParameterRaw(int key) {
		return parameters.get(key);
	}

    /**
     * Get parameter from private container. Result is always not-null.
     * @param key parameterID to obtain value for
     * @return parameter value
     */
	public String getParameterNotNull(int key) {
		String par = getParameter(key);
		if (par == null) {
			return "";
		} else {
			return par;
		}
	}
	
	public boolean hasParameter(int key) {
		return parameters.get(key) != null;
	}
	
	public String removeParameter(int key) {
		String value = getParameter(key);
		parameters.remove(key);
		return value;
	}
	
	public int getCount() {
		return parameters.size();
	}
	
	// PHONE
	
	public boolean addPhone(String phone) {
		return addToStorage("", phone, PAR_PHONE_MIN, PAR_PHONE_MAX);
	}
	
	public boolean addPhone(String label, String phone) {
		return addToStorage(label, phone, PAR_PHONE_MIN, PAR_PHONE_MAX);
	}
	
	public List<LabelTextContainer> getPhones() {
		return getFromStorage(PAR_PHONE_MIN, PAR_PHONE_MAX);
	}
	
	public boolean removePhone(String phone) {
		return removeFromStorage(phone, PAR_PHONE_MIN, PAR_PHONE_MAX);
	}
	
	public void removeAllPhones() {
		removeAllFromStorage(PAR_PHONE_MIN, PAR_PHONE_MAX);
	}
	
	// EMAIL
	
	public boolean addEmail(String email) {
		return addToStorage("", email, PAR_EMAIL_MIN, PAR_EMAIL_MAX);
	}
	
	public boolean addEmail(String label, String email) {
		return addToStorage(label, email, PAR_EMAIL_MIN, PAR_EMAIL_MAX);
	}
	
	public List<LabelTextContainer> getEmails() {
		return getFromStorage(PAR_EMAIL_MIN, PAR_EMAIL_MAX);
	}
	
	public boolean removeEmail(String email) {
		return removeFromStorage(email, PAR_EMAIL_MIN, PAR_EMAIL_MAX);
	}
	
	public void removeAllEmails() {
		removeAllFromStorage(PAR_EMAIL_MIN, PAR_EMAIL_MAX);
	}
	
	// URL
	
	public boolean addUrl(String url) {
		return addToStorage("", url, PAR_URL_MIN, PAR_URL_MAX);
	}
	
	public boolean addUrl(String label, String url) {
		return addToStorage(label, url, PAR_URL_MIN, PAR_URL_MAX);
	}
	
	public List<LabelTextContainer> getUrls() {
		return getFromStorage(PAR_URL_MIN, PAR_URL_MAX);
	}
	
	public boolean removeUrl(String url) {
		return removeFromStorage(url, PAR_URL_MIN, PAR_URL_MAX);
	}
	
	public void removeAllUrls() {
		removeAllFromStorage(PAR_URL_MIN, PAR_URL_MAX);
	}
	
	// PHOTO
	
	public boolean addPhoto(String photo) {
		return addToStorage("", photo, PAR_PHOTO_MIN, PAR_PHOTO_MAX);
	}
	
	public List<String> getPhotos() {
		return convertToTexts(getFromStorage(PAR_PHOTO_MIN, PAR_PHOTO_MAX));
	}
	
	public boolean removePhoto(String photo) {
		return removeFromStorage(photo, PAR_PHOTO_MIN, PAR_PHOTO_MAX);
	}
	
	// VIDEO
	
	public boolean addVideo(String video) {
		return addToStorage("", video, PAR_VIDEO_MIN, PAR_VIDEO_MAX);
	}
	
	public List<String> getVideos() {
		return convertToTexts(getFromStorage(PAR_VIDEO_MIN, PAR_VIDEO_MAX));
	}
	
	public boolean removeVideo(String video) {
		return removeFromStorage(video, PAR_VIDEO_MIN, PAR_VIDEO_MAX);
	}
	
	// AUDIO
	
	public boolean addAudio(String audio) {
		return addToStorage("", audio, PAR_AUDIO_MIN, PAR_AUDIO_MAX);
	}
	
	public List<String> getAudios() {
		return convertToTexts(getFromStorage(PAR_AUDIO_MIN, PAR_AUDIO_MAX));
	}
	
	public boolean removeAudio(String audio) {
		return removeFromStorage(audio, PAR_AUDIO_MIN, PAR_AUDIO_MAX);
	}

	// OTHER FILES
	
	public boolean addOtherFile(String filpath) {
		return addToStorage("", filpath, PAR_OTHER_FILES_MIN, PAR_OTHER_FILES_MAX);
	}
	
	public List<String> getOtherFiles() {
		return convertToTexts(getFromStorage(PAR_OTHER_FILES_MIN, PAR_OTHER_FILES_MAX));
	}
	
	public boolean removeOtherFile(String filpath) {
		return removeFromStorage(filpath, PAR_OTHER_FILES_MIN, PAR_OTHER_FILES_MAX);
	}
	
	// PRIVATE AND SPECIAL TOOLS
	
	public List<String> getAllAttachments() {
		List<String> result = new ArrayList<>();
		result.addAll(getPhotos());
		result.addAll(getAudios());
		result.addAll(getVideos());
		result.addAll(getOtherFiles());
		return result;
	}
	
	public int getAllAttachmentsCount() {
		return getAllAttachments().size();
	}

	private boolean addToStorage(String label, String text, int rangeFrom, int rangeTo) {
		// check text
		if (text == null || text.length() == 0) {
			return false;
		}
		
		// create item
		String item;
		if (label != null && label.length() > 0) {
			item = label + "|" + text;
		} else {
			item = text;
		}
		
		for (int key = rangeFrom; key <= rangeTo; key++) {
			String value = getParameter(key);
			if (value == null) {
				parameters.put(key, Utils.doStringToBytes(item));
				return true;
			} else if (value.equalsIgnoreCase(item)) {
				// item already exists
				return false;
			} else {
				// some other item already included, move to next index
			}
		}
		return false;
	}
	
	private List<LabelTextContainer> getFromStorage(int rangeFrom, int rangeTo) {
		List<LabelTextContainer> data = new ArrayList<>();
		for (int key = rangeFrom; key <= rangeTo; key++) {
			String value = getParameter(key);
			if (value == null || value.length() == 0) {
				continue;
			}
			
			// extract data
			data.add(new LabelTextContainer(value));
		}
		return data;
	}
	
	private List<String> convertToTexts(List<LabelTextContainer> data) {
		List<String> result = new ArrayList<>();
		for (int i = 0, m = data.size(); i < m; i++) {
			result.add(data.get(i).text);
		}
		return result;
	}
	
	private boolean removeFromStorage(String item, int rangeFrom, int rangeTo) {
		// check text
		if (item == null || item.length() == 0) {
			return false;
		}
		
		for (int key = rangeFrom; key <= rangeTo; key++) {
			String value = getParameter(key);
			if (value == null) {
				// no item
			} else if (value.endsWith(item)) {
				parameters.remove(key);
				return true;
			} else {
				// some other item already included, move to next index
			}
		}
		return false;
	}
	
	private void removeAllFromStorage(int rangeFrom, int rangeTo) {
		for (int i = rangeFrom; i <= rangeTo; i++) {
			parameters.remove(i);
		}
	}

    /**************************************************/
    // CALL BACK HELPERS
    /**************************************************/

    /**
     * Generate string, that may be used as response information for Locus.
     * @param name name at start of string
     * @param packageName package name
     * @param className class name
     * @param returnDataName variable name
     * @param returnDataValue variable value
     * @return generated text
     */
    public static String generateCallbackString(String name, String packageName,
            String className, String returnDataName, String returnDataValue) {
        // check parameters
        if (packageName == null || packageName.length() == 0 ||
                className == null || className.length() == 0) {
            Logger.logD(TAG, "generateCallbackString(" + name + ", " + packageName + ", " +
                    className + ", " + returnDataName + ", " + returnDataValue + "), " +
                    "invalid packageName or className parameter");
            return "";
        }

        // improve parameters
        if (name == null) {
            name = "";
        }
        if (returnDataName == null) {
            returnDataName = "";
        }
        if (returnDataValue == null) {
            returnDataValue = "";
        }

        // return generated result
        return name + ";" +
                packageName + ";" +
                className + ";" +
                returnDataName + ";" +
                returnDataValue + ";";
    }
}
