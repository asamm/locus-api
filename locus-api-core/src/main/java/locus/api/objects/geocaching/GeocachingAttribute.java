/*
 * Copyright 2011, Asamm Software, s.r.o.
 *
 * This file is part of LocusAddonPublicLib.
 *
 * LocusAddonPublicLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LocusAddonPublicLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LocusAddonPublicLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package locus.api.objects.geocaching;

import java.io.IOException;
import java.util.Hashtable;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

/**
 * Class for holding geocaching attributes
 * <br><br>
 * Every instance holds just one attribute, defined by it's unique ID number. If
 * you want to set correct value, use constructor that allow set directly by number
 * or by attribute URL
 * <br><br>
 *
 * @author menion
 */
public class GeocachingAttribute extends Storable {

    // unique ID for attribute
    private int mId;

    /**
     * Basic constructor.
     */
    public GeocachingAttribute() {
        mId = -1;
    }

    /**
     * Create attribute container.
     *
     * @param id       ID of attribute
     * @param positive {@code true} to make attribute positive
     */
    public GeocachingAttribute(int id, boolean positive) {
        this();
        if (!positive) {
            this.mId = id;
        } else {
            this.mId = (id + 100);
        }
    }

    /**
     * Create attribute container based on certain uri value.
     *
     * @param url url value
     */
    public GeocachingAttribute(String url) {
        this();
        if (url != null && url.length() > 0) {
            String imgName = url.substring(url.lastIndexOf("/" + 1), url.lastIndexOf("-"));
            mId = mAttrIds.get(imgName);
            if (url.contains("-yes.")) {
                mId += 100;
            }
        }
    }

    //*************************************************/
    // GET & SET
    //*************************************************/

    // ID

    /**
     * Get current ID of attribute. This ID also contains positive/negative flag.
     *
     * @return ID of attribute
     */
    public int getId() {
        return mId;
    }

    /**
     * Force some ID number to this attribute. Use only if you know what you're doing.
     * This feature is mainly used directly in Locus to fill data
     *
     * @param id number, already increased by 100 if it's positive attribute
     */
    public void setId(int id) {
        this.mId = id;
    }

    // REAL ID

    /**
     * Get real ID of attribute that match ID's used by Groundspeak.
     *
     * @return real ID
     */
    public int getIdReal() {
        return mId % 100;
    }

    // POSITIVE/NEGATIVE

    /**
     * Is current attribute value positive.
     *
     * @return {@code true} if is positive
     */
    public boolean isPositive() {
        return mId > 100;
    }

    //*************************************************/
    // STORABLE PART
    //*************************************************/

    @Override
    protected int getVersion() {
        return 0;
    }

    @Override
    protected void readObject(int version, DataReaderBigEndian dr)
            throws IOException {
        mId = dr.readInt();
    }

    @Override
    protected void writeObject(DataWriterBigEndian dw) throws IOException {
        dw.writeInt(mId);
    }

    //*************************************************/
    // STATIC TOOLS
    //*************************************************/

    private static final Hashtable<String, Integer> mAttrIds = new Hashtable<>();

    static {
        mAttrIds.put("dogs", 1);
        mAttrIds.put("fee", 2);
        mAttrIds.put("rappelling", 3);
        mAttrIds.put("boat", 4);
        mAttrIds.put("scuba", 5);
        mAttrIds.put("kids", 6);
        mAttrIds.put("onehour", 7);
        mAttrIds.put("scenic", 8);
        mAttrIds.put("hiking", 9);
        mAttrIds.put("climbing", 10);
        mAttrIds.put("wading", 11);
        mAttrIds.put("swimming", 12);
        mAttrIds.put("available", 13);
        mAttrIds.put("night", 14);
        mAttrIds.put("winter", 15);
        //mAttrIds.put("camping", 16);
        mAttrIds.put("poisonoak", 17);
        mAttrIds.put("snakes", 18);
        mAttrIds.put("ticks", 19);
        mAttrIds.put("mine", 20);
        mAttrIds.put("cliff", 21);
        mAttrIds.put("hunting", 22);
        mAttrIds.put("danger", 23);
        mAttrIds.put("wheelchair", 24);
        mAttrIds.put("parking", 25);
        mAttrIds.put("public", 26);
        mAttrIds.put("water", 27);
        mAttrIds.put("restrooms", 28);
        mAttrIds.put("phone", 29);
        mAttrIds.put("picnic", 30);
        mAttrIds.put("camping", 31);
        mAttrIds.put("bicycles", 32);
        mAttrIds.put("motorcycles", 33);
        mAttrIds.put("quads", 34);
        mAttrIds.put("jeeps", 35);
        mAttrIds.put("snowmobiles", 36);
        mAttrIds.put("horses", 37);
        mAttrIds.put("campfires", 38);
        mAttrIds.put("thorn", 39);
        mAttrIds.put("stealth", 40);
        mAttrIds.put("stroller", 41);
        mAttrIds.put("firstaid", 42);
        mAttrIds.put("cow", 43);
        mAttrIds.put("flashlight", 44);
        mAttrIds.put("landf", 45);
        mAttrIds.put("rv", 46);
        mAttrIds.put("field_puzzle", 47);
        mAttrIds.put("UV", 48);
        mAttrIds.put("snowshoes", 49);
        mAttrIds.put("skiis", 50);
        mAttrIds.put("s-tool", 51);
        mAttrIds.put("nightcache", 52);
        mAttrIds.put("parkngrab", 53);
        mAttrIds.put("AbandonedBuilding", 54);
        mAttrIds.put("hike_short", 55);
        mAttrIds.put("hike_med", 56);
        mAttrIds.put("hike_long", 57);
        mAttrIds.put("fuel", 58);
        mAttrIds.put("food", 59);
        mAttrIds.put("wirelessbeacon", 60);
        mAttrIds.put("partnership", 61);
        mAttrIds.put("seasonal", 62);
        mAttrIds.put("touristOK", 63);
        mAttrIds.put("treeclimbing", 64);
        mAttrIds.put("frontyard", 65);
        mAttrIds.put("teamwork", 66);
        mAttrIds.put("geotour", 67);
    }
}
