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
 * @author menion
 */
public class GeocachingAttribute extends Storable {

	// unique ID for attribute
	private int mId;

    /**
     * Basic constructor.
     */
	public GeocachingAttribute() {
		super();
	}

    /**
     * Create attribute container.
     * @param id ID of attribute
     * @param positive {@code true} to make attribute positive
     */
	public GeocachingAttribute(int id, boolean positive) {
		super();
		if (!positive) {
			this.mId = id;
		} else {
			this.mId = (id + 100);
		}
	}
	
	public GeocachingAttribute(String url) {
		super();
		if (url != null && url.length() > 0) {
			String imgName = url.substring(url.lastIndexOf("/" + 1), url.lastIndexOf("-"));
            mId = attrIds.get(imgName);
			if (url.contains("-yes.")) {
                mId += 100;
			}
		}
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
        mId = dr.readInt();
	}

	@Override
	protected void writeObject(DataWriterBigEndian dw) throws IOException {
		dw.writeInt(mId);
	}

	@Override
	public void reset() {
		mId = -1;
	}

	/**************************************************/
	// MAIN PART
    /**************************************************/

    // ID

    /**
     * Get current ID of attribute. This ID also contains positive/negative flag.
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
     * @return real ID
     */
	public int getIdReal() {
		return mId % 100;
	}

    // POSITIVE/NEGATIVE

    /**
     * Is current attribute value positive.
     * @return {@code true} if is positive
     */
	public boolean isPositive() {
		return mId > 100;
	}

    // STATIC CONTAINER

	private static Hashtable<String, Integer> attrIds = new Hashtable<String, Integer>();
	static {
		attrIds.put("dogs", 1);
		attrIds.put("fee", 2);
		attrIds.put("rappelling", 3);
		attrIds.put("boat", 4);
		attrIds.put("scuba", 5);
		attrIds.put("kids", 6);
		attrIds.put("onehour", 7);
		attrIds.put("scenic", 8);
		attrIds.put("hiking", 9);
		attrIds.put("climbing", 10);
		attrIds.put("wading", 11);
		attrIds.put("swimming", 12);
		attrIds.put("available", 13);
		attrIds.put("night", 14);
		attrIds.put("winter", 15);
		attrIds.put("camping", 16);
		attrIds.put("poisonoak", 17);
		attrIds.put("snakes", 18);
		attrIds.put("ticks", 19);
		attrIds.put("mine", 20);
		attrIds.put("cliff", 21);
		attrIds.put("hunting", 22);
		attrIds.put("danger", 23);
		attrIds.put("wheelchair", 24);
		attrIds.put("parking", 25);
		attrIds.put("public", 26);
		attrIds.put("water", 27);
		attrIds.put("restrooms", 28);
		attrIds.put("phone", 29);
		attrIds.put("picnic", 30);
		attrIds.put("camping", 31);
		attrIds.put("bicycles", 32);
		attrIds.put("motorcycles", 33);
		attrIds.put("quads", 34);
		attrIds.put("jeeps", 35);
		attrIds.put("snowmobiles", 36);
		attrIds.put("horses", 37);
		attrIds.put("campfires", 38);
		attrIds.put("thorn",39 );
		attrIds.put("stealth", 40); 
		attrIds.put("stroller", 41);
		attrIds.put("firstaid", 42);
		attrIds.put("cow", 43);
		attrIds.put("flashlight", 44);
		attrIds.put("landf", 45);
		attrIds.put("rv", 46);
		attrIds.put("field_puzzle", 47);
		attrIds.put("UV", 48);
		attrIds.put("snowshoes", 49);
		attrIds.put("skiis", 50);
		attrIds.put("s-tool", 51);
		attrIds.put("nightcache", 52);
		attrIds.put("parkngrab", 53);
		attrIds.put("AbandonedBuilding", 54);
		attrIds.put("hike_short", 55);
		attrIds.put("hike_med", 56);
		attrIds.put("hike_long", 57);
		attrIds.put("fuel", 58);
		attrIds.put("food", 59);
		attrIds.put("wirelessbeacon", 60);
		attrIds.put("partnership", 61);
		attrIds.put("seasonal", 62);
		attrIds.put("touristOK", 63);
		attrIds.put("treeclimbing", 64);
		attrIds.put("frontyard", 65);
		attrIds.put("teamwork", 66);
	}
}
