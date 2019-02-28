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
import java.util.Arrays;
import java.util.List;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;
import locus.api.utils.SparseArrayCompat;
import locus.api.utils.Utils;

@SuppressWarnings({"unused", "WeakerAccess"})
public class GeoDataExtra extends Storable {

    // tag for logger
    private static final String TAG = "GeoDataExtra";

    // DATA SOURCE DEFINED IN PARAMETER 'PAR_SOURCE'

    /**
     * Source unknown or undefined
     */
    public static final byte SOURCE_UNKNOWN = 48; // "0"
    /**
     * Special point for parking service
     */
    public static final byte SOURCE_PARKING_SERVICE = 49; // "1"
    /**
     * Additional waypoint for geocache
     */
    public static final byte SOURCE_GEOCACHING_WAYPOINT = 50; // "2"
    /**
     * Temporary point on map (not stored in database)
     */
    public static final byte SOURCE_MAP_TEMP = 51; // "3"
    /**
     * Waypoint on route, location with some more values
     */
    public static final byte SOURCE_ROUTE_WAYPOINT = 52; // "4"
    /**
     * Only location on route
     */
    public static final byte SOURCE_ROUTE_LOCATION = 53; // "5"
    /**
     * Point coming from OpenStreetBugs
     */
    public static final byte SOURCE_OPENSTREETBUGS = 55; // "7"
    /**
     * Temporary item do not display on map
     */
    public static final byte SOURCE_INVISIBLE = 56; // "8"
    /**
     * Items automatically loaded from OSM POI database
     */
    public static final byte SOURCE_POI_OSM_DB = 57; // "9"
    /**
     * Items loaded from Munzee service
     */
    public static final byte SOURCE_MUNZEE = 58; // ":"
    /**
     * Items loaded from Live-tracking service
     */
    public static final byte SOURCE_LIVE_TRACKING = 59;
    /**
     * Blocked area point for navigation and routing
     */
    public static final byte SOURCE_NAVI_BLOCKED_AREA = 60;
    /**
     * Point generated from map "selection point"
     */
    public static final byte SOURCE_MAP_SELECTION = 61;


    // ROUTE TYPES DEFINED IN PARAMETER 'PAR_RTE_COMPUTE_TYPE'

    public static final int VALUE_RTE_TYPE_GENERATED = -1;

    public static final int VALUE_RTE_TYPE_NO_TYPE = 100;
    public static final int VALUE_RTE_TYPE_CAR = 6;
    public static final int VALUE_RTE_TYPE_CAR_FAST = 0;
    public static final int VALUE_RTE_TYPE_CAR_SHORT = 1;
    public static final int VALUE_RTE_TYPE_MOTORCYCLE = 7;
    public static final int VALUE_RTE_TYPE_CYCLE = 2;
    public static final int VALUE_RTE_TYPE_CYCLE_FAST = 4;
    public static final int VALUE_RTE_TYPE_CYCLE_SHORT = 5;
    public static final int VALUE_RTE_TYPE_CYCLE_MTB = 8;
    public static final int VALUE_RTE_TYPE_CYCLE_RACING = 9;
    // basic routing profile, type "walk"
    public static final int VALUE_RTE_TYPE_FOOT_01 = 3;
    // routing profile usually used for "hiking"
    public static final int VALUE_RTE_TYPE_FOOT_02 = 10;
    // routing profile usually used for "climb" or "mountain hiking"
    public static final int VALUE_RTE_TYPE_FOOT_03 = 11;

    /**
     * All possible RTE_TYPES also sorted in correct order.
     */
    public static final int[] RTE_TYPES_SORTED = new int[]{
            VALUE_RTE_TYPE_NO_TYPE,
            VALUE_RTE_TYPE_CAR,
            VALUE_RTE_TYPE_CAR_FAST,
            VALUE_RTE_TYPE_CAR_SHORT,
            VALUE_RTE_TYPE_MOTORCYCLE,
            VALUE_RTE_TYPE_CYCLE,
            VALUE_RTE_TYPE_CYCLE_FAST,
            VALUE_RTE_TYPE_CYCLE_SHORT,
            VALUE_RTE_TYPE_CYCLE_MTB,
            VALUE_RTE_TYPE_CYCLE_RACING,
            VALUE_RTE_TYPE_FOOT_01,
            VALUE_RTE_TYPE_FOOT_02,
            VALUE_RTE_TYPE_FOOT_03,
    };

    // PRIVATE REFERENCES (0 - 29)

    public static final int PAR_SOURCE = 0;

    /**
     * Private parameter for handling of styles.
     * Use getter/setter directly in GeoData object
     */
    public static final int PAR_STYLE_NAME = 5;
    /**
     * Computed are value. Used mainly for tracks
     */
    public static final int PAR_AREA_SIZE = 12;
    /**
     * Extra data for offline POI database
     */
    public static final int PAR_DB_POI_EXTRA_DATA = 13;
    /**
     * ID of KML trip to which item belongs
     */
    public static final int PAR_KML_TRIP_ID = 14;
    /**
     * Reference to original Google Places item.
     */
    public static final int PAR_GOOGLE_PLACES_REFERENCE = 15;
    /**
     * Google Places rating.
     */
    public static final int PAR_GOOGLE_PLACES_RATING = 16;
    /**
     * Google places details.
     */
    public static final int PAR_GOOGLE_PLACES_DETAILS = 17;

    public static final int PAR_INTENT_EXTRA_CALLBACK = 20;
    public static final int PAR_INTENT_EXTRA_ON_DISPLAY = 21;

    // PUBLIC VALUES (30 - 49)

    /**
     * [STRING] Item visible description.
     */
    public static final int PAR_DESCRIPTION = 30;
    /**
     * [STRING] Storage for comments (extra tiny description available in GPX files).
     */
    public static final int PAR_COMMENT = 31;
    /**
     * [STRING] Relative path to working dir (for images for example).
     */
    public static final int PAR_RELATIVE_WORKING_DIR = 32;
    /**
     * [INT] Type (classification) of the item (point).
     */
    public static final int PAR_TYPE = 33;
    /**
     * [STRING] Special code used in Geocache waypoints.
     */
    public static final int PAR_GEOCACHE_CODE = 34;
    /**
     * [BOOLEAN] Flag to include item in POI alert feature. If missing, "true" used as default.
     */
    public static final int PAR_POI_ALERT_INCLUDE = 35;

    // LOCATION PARAMETERS (50 - 59)

    // street name
    public static final int PAR_ADDRESS_STREET = 50;
    // city name
    public static final int PAR_ADDRESS_CITY = 51;
    // name of region
    public static final int PAR_ADDRESS_REGION = 52;
    // PSČ, post code number
    public static final int PAR_ADDRESS_POST_CODE = 53;
    // name of country
    public static final int PAR_ADDRESS_COUNTRY = 54;

    // ROUTE PARAMETERS (100 - 199)

    // PARAMETERS FOR NAVIGATION POINTS (WAYPOINT)

    /**
     * Index to point list
     * <br>
     * Locus internal variable, <b>DO NOT SET</b>
     */
    public static final int PAR_RTE_INDEX = 100;
    /**
     * Distance (in metres) from current navPoint to next
     * <br>
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
     * Number of seconds to transition between successive links along
     * the route. These take into account the geometry of the intersection,
     * number of links at the intersection, and types of roads at
     * the intersection. This attempts to estimate the time in seconds it
     * would take for stops, or places where a vehicle must slow to make a turn.
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

    /**
     * type of route (car_fast, car_short, cyclo, foot)
     */
    public static final int PAR_RTE_COMPUTE_TYPE = 120;
    /**
     * Roundabout is usually defined from two points. First on enter correctly
     * defined by ACTION 27 - 34, second on exit simply defined by exit angle.
     * In case of usage only exit point, it's need to set this flag
     */
    public static final int PAR_RTE_SIMPLE_ROUNDABOUTS = 121;
    /**
     * Configuration of (route) plan as defined in route planner.
     */
    public static final int PAR_RTE_PLAN_DEFINITION = 122;

    // OSM BUGS (300 - 309)
    public static final int PAR_OSM_NOTES_ID = 301;
    public static final int PAR_OSM_NOTES_CLOSED = 302;

    // PHONES, EMAIL, URLS, PHOTOS, VIDEO, AUDIO, OTHER FILES (1000 - 1999)
    // below in "Attachments" section

    /**
     * table for additional parameters
     */
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

    public GeoDataExtra() {
        parameters = new SparseArrayCompat<>();
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

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

    //*************************************************
    // HANDLERS PART
    //*************************************************

    /**
     * Add a single parameter to container, defined by ID and it's text representation.
     *
     * @param key   key value
     * @param value value itself
     * @return {@code true} if parameter was correctly added
     */
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
            Logger.logW(TAG, "addParam(" + key + ", " + value + "), " +
                    "values 1000 - 1999 reserved!");
            return false;
        }

        // finally insert value
        parameters.put(key, Utils.doStringToBytes(value));
        return true;
    }

    /**
     * Add a single parameter to container, defined by ID and it's byte value.
     *
     * @param key   key value
     * @param value value itself
     * @return {@code true} if parameter was correctly added
     */
    public boolean addParameter(int key, byte value) {
        return addParameter(key, new byte[]{value});
    }

    /**
     * Add a single parameter to container, defined by ID and it's byte array representation.
     *
     * @param key   key value
     * @param value value itself
     * @return {@code true} if parameter was correctly added
     */
    public boolean addParameter(int key, byte[] value) {
        // remove previous parameter
        removeParameter(key);

        // trim new value and insert into table
        if (value == null || value.length == 0) {
            return false;
        }

        // check keys
        if (key > 1000 && key < 2000) {
            Logger.logW(TAG, "addParam(" + key + ", " + Arrays.toString(value) + "), " +
                    "values 1000 - 1999 reserved!");
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
     *
     * @param key key ID
     * @return raw parameters data
     */
    public byte[] getParameterRaw(int key) {
        return parameters.get(key);
    }

    /**
     * Get parameter from private container. Result is always not-null.
     *
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

    //*************************************************
    // ATTACHMENTS
    //*************************************************

    // PHONE

    @Deprecated // use `addAttachment`
    public boolean addPhone(String phone) {
        return addPhone("", phone);
    }
    @Deprecated // use `addAttachment`
    public boolean addPhone(String label, String phone) {
        return addAttachment(AttachType.PHONE, label, phone);
    }
    @Deprecated // use `getAttachments`
    public List<LabelTextContainer> getPhones() {
        return getAttachments(AttachType.PHONE);
    }
    @Deprecated // use `removeAttachment`
    public boolean removePhone(String phone) {
        return removeAttachment(AttachType.PHONE, phone);
    }
    @Deprecated // use `removeAllAttachments`
    public void removeAllPhones() {
        removeAllAttachments(AttachType.PHONE);
    }

    // EMAIL

    @Deprecated // use `addAttachment`
    public boolean addEmail(String email) {
        return addEmail("", email);
    }
    @Deprecated // use `addAttachment`
    public boolean addEmail(String label, String email) {
        return addAttachment(AttachType.EMAIL, label, email);
    }
    @Deprecated // use `getAttachments`
    public List<LabelTextContainer> getEmails() {
        return getAttachments(AttachType.EMAIL);
    }
    @Deprecated // use `removeAttachment`
    public boolean removeEmail(String email) {
        return removeAttachment(AttachType.EMAIL, email);
    }
    @Deprecated // use `removeAllAttachments`
    public void removeAllEmails() {
        removeAllAttachments(AttachType.EMAIL);
    }

    // URL

    @Deprecated // use `addAttachment`
    public boolean addUrl(String url) {
        return addUrl("", url);
    }
    @Deprecated // use `addAttachment`
    public boolean addUrl(String label, String url) {
        return addAttachment(AttachType.URL, label, url);
    }
    @Deprecated // use `getAttachments`
    public List<LabelTextContainer> getUrls() {
        return getAttachments(AttachType.URL);
    }
    @Deprecated // use `removeAttachment`
    public boolean removeUrl(String url) {
        return removeAttachment(AttachType.URL, url);
    }
    @Deprecated // use `removeAllAttachments`
    public void removeAllUrls() {
        removeAllAttachments(AttachType.URL);
    }

    // PHOTO

    @Deprecated // use `addAttachment`
    public boolean addPhoto(String photo) {
        return addPhoto("", photo);
    }
    @Deprecated // use `addAttachment`
    public boolean addPhoto(String label, String photo) {
        return addAttachment(AttachType.PHOTO, label, photo);
    }
    @Deprecated // use `getAttachments`
    public List<String> getPhotos() {
        return convertToTexts(getAttachments(AttachType.PHOTO));
    }
    @Deprecated // use `removeAttachment`
    public boolean removePhoto(String photo) {
        return removeAttachment(AttachType.PHOTO, photo);
    }

    // VIDEO

    @Deprecated // use `addAttachment`
    public boolean addVideo(String video) {
        return addVideo("", video);
    }
    @Deprecated // use `addAttachment`
    public boolean addVideo(String label, String video) {
        return addAttachment(AttachType.VIDEO, label, video);
    }
    @Deprecated // use `getAttachments`
    public List<String> getVideos() {
        return convertToTexts(getAttachments(AttachType.VIDEO));
    }
    @Deprecated // use `removeAttachment`
    public boolean removeVideo(String video) {
        return removeAttachment(AttachType.VIDEO, video);
    }

    // AUDIO

    @Deprecated // use `addAttachment`
    public boolean addAudio(String audio) {
        return addAudio("", audio);
    }
    @Deprecated // use `addAttachment`
    public boolean addAudio(String label, String audio) {
        return addAttachment(AttachType.AUDIO, label, audio);
    }
    @Deprecated // use `getAttachments`
    public List<String> getAudios() {
        return convertToTexts(getAttachments(AttachType.AUDIO));
    }
    @Deprecated // use `removeAttachment`
    public boolean removeAudio(String audio) {
        return removeAttachment(AttachType.AUDIO, audio);
    }

    // OTHER FILES

    @Deprecated // use `addAttachment`
    public boolean addOtherFile(String filpath) {
        return addOtherFile("", filpath);
    }
    @Deprecated // use `addAttachment`
    public boolean addOtherFile(String label, String filpath) {
        return addAttachment(AttachType.OTHER, label, filpath);
    }
    @Deprecated // use `getAttachments`
    public List<String> getOtherFiles() {
        return convertToTexts(getAttachments(AttachType.OTHER));
    }
    @Deprecated // use `removeAttachment`
    public boolean removeOtherFile(String filpath) {
        return removeAttachment(AttachType.OTHER, filpath);
    }

    /**
     * Type of attached object.
     */
    public enum AttachType {

        PHONE(1000, 1099),
        EMAIL(1100, 1199),
        URL(1200, 1299),
        PHOTO(1300, 1399),
        VIDEO(1400, 1499),
        AUDIO(1500, 1599),
        OTHER(1800, 1999);

        // minimal value in storage
        private int min;
        // maximal allowed value in storage
        private int max;

        /**
         * Create new type object.
         */
        AttachType(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }

    /**
     * Add attachment of certain type into container.
     *
     * @param type attachment type
     * @param label (optional) item label
     * @param value item value itself
     * @return `true` if correctly added
     */
    public boolean addAttachment(AttachType type, String label, String value) {
        return addToStorage(label, value, type.min, type.max);
    }

    /**
     * Get all attachments of certain type.
     *
     * @param type attachment type
     * @return list of all attachments
     */
    public List<LabelTextContainer> getAttachments(AttachType type) {
        return getFromStorage(type.min, type.max);
    }

    /**
     * Remove attachment defined by its value, from storage.
     *
     * @param type attachment type
     * @param value value of attachment
     * @return `true` if attachment was removed
     */
    public boolean removeAttachment(AttachType type, String value) {
        return removeFromStorage(value, type.min, type.max);
    }

    /**
     * Remove all attachments of certain type.
     *
     * @param type attachment type
     */
    public void removeAllAttachments(AttachType type) {
        removeAllFromStorage(type.min, type.max);
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

    //*************************************************
    // CALL BACK HELPERS
    //*************************************************

    /**
     * Generate string, that may be used as response information for Locus.
     *
     * @param name            name at start of string
     * @param packageName     package name
     * @param className       class name
     * @param returnDataName  variable name
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
