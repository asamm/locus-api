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

package locus.api.android.objects;

import android.graphics.Bitmap;

import locus.api.android.utils.UtilsBitmap;
import locus.api.objects.Storable;
import locus.api.objects.extra.GeoDataStyle;
import locus.api.objects.extra.Point;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PackPoints extends Storable {

    /**
     * Unique name
     * PackPoints send to Locus with same name (to display), will be overwrite in Locus
     */
    private String mName;

    // icon applied to whole PackPoints
    private GeoDataStyle style;
    // bitmap for this pack
    private Bitmap imgBitmap;

    // ArrayList of all points stored in this object
    private List<Point> points;

    /**
     * Empty constructor used for {@link Storable}
     * <br>
     * Do not use directly!
     */
    public PackPoints() {
        this.mName = "";
        this.style = null;
        this.points = new ArrayList<>();
    }

    public PackPoints(String uniqueName) {
        this();
        this.mName = uniqueName;
    }

    public String getName() {
        return mName;
    }

    public Bitmap getBitmap() {
        return imgBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.imgBitmap = bitmap;
    }

    public GeoDataStyle getExtraStyle() {
        return style;
    }

    public void setExtraStyle(GeoDataStyle extraStyle) {
        style = extraStyle;
    }

    public void addWaypoint(Point wpt) {
        this.points.add(wpt);
    }

    public List<Point> getWaypoints() {
        return points;
    }

    //*************************************************
    // STORABLE
    //*************************************************

    @Override
    protected int getVersion() {
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void readObject(int version, DataReaderBigEndian dr)
            throws IOException {
        // name
        mName = dr.readString();

        // style
        if (dr.readBoolean()) {
            style = new GeoDataStyle();
            style.read(dr);
        }

        // icon
        imgBitmap = UtilsBitmap.readBitmap(dr);

        // waypoints
        points = dr.readListStorable(Point.class);
    }

    @Override
    protected void writeObject(DataWriterBigEndian dw) throws IOException {
        // name
        dw.writeString(mName);

        // style
        if (style == null) {
            dw.writeBoolean(false);
        } else {
            dw.writeBoolean(true);
            dw.writeStorable(style);
        }

        // bitmap icon
        UtilsBitmap.writeBitmap(dw, imgBitmap, Bitmap.CompressFormat.PNG);

        // waypoints itself
        dw.writeListStorable(points);
    }
}
