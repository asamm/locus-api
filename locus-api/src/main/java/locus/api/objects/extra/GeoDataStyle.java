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

import locus.api.objects.Storable;
import locus.api.objects.extra.GeoDataStyle.LineStyle.ColorStyle;
import locus.api.objects.extra.GeoDataStyle.LineStyle.Units;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;

public class GeoDataStyle extends Storable {

    // tag for logger
	private static final String TAG = "GeoDataStyle";
	
	// style name - id in style tag
	private String mId;
	// style name - name tag inside style tag	
	private String mName;
	
	// BALLON STYLE (not used yet)
	BalloonStyle balloonStyle;
	// ICON STYLE
	IconStyle iconStyle;
	// LABEL STYLE
	LabelStyle labelStyle;
	// LINE STYLE
	LineStyle lineStyle;
	// LIST STYLE (not used yet)
	ListStyle listStyle;
	// POLY STYLE
	PolyStyle polyStyle;

	/**
	 * Create new instance of style container.
	 */
	public GeoDataStyle() {
		this("");
	}

	/**
	 * Create new instance of style container with defined name.
	 * @param name name of style container
	 */
	public GeoDataStyle(String name) {
		super();

		// set name
		if (name != null) {
			this.mName = name;
		}
	}

	/**
	 * Create new instance of style container based on ready to use reader.
	 * @param dr data reader with data
	 */
	public GeoDataStyle(DataReaderBigEndian dr) throws IOException {
		super(dr);
	}

	/**
	 * Create new instance of style container based on previously stored data.
	 * @param data data of style itself
	 */
	public GeoDataStyle(byte[] data) throws IOException {
		super(data);
	}

    /**************************************************/
    // SETTERS & GETTERS
    /**************************************************/

	// ID

	/**
	 * Get unique ID of current style.
	 * @return style ID
	 */
	public String getId() {
		return mId;
	}

	/**
	 * Set new ID to current style.
	 * @param id new ID
	 */
	public void setId(String id) {
		if (id == null) {
			id = "";
		}
		this.mId = id;
	}

	// NAME

	/**
	 * Get current defined name for a style.
	 * @return name of style
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Set new name for current style.
	 * @param name new name
	 */
	public void setName(String name) {
		if (name == null) {
			name = "";
		}
		this.mName = name;
	}

	// ICON STYLE

    /**
     * Get defined style for icons.
     * @return style for icons
     */
	public IconStyle getIconStyle() {
		return iconStyle;
	}

	/**
	 * Get style Url for an icon.
	 * @return style Url/href parameter
	 */
	public String getIconStyleIconUrl() {
		if (iconStyle == null) {
			return null;
		}
		return iconStyle.getIconHref();
	}

	public void setIconStyle(String iconUrl, float scale) {
    	setIconStyle(iconUrl, COLOR_DEFAULT, 0.0f, scale);
    }
	
    public void setIconStyle(String iconUrl, int color, float heading, float scale) {
    	iconStyle = new IconStyle();
    	iconStyle.setIconHref(iconUrl);
    	iconStyle.color = color;
    	iconStyle.heading = heading;
    	iconStyle.setScale(scale);
    	// set hot spot
    	setIconStyleHotSpot(HOTSPOT_BOTTOM_CENTER);
    }
	
	// definition of hotSpot of icon to bottom center
	public static final int HOTSPOT_BOTTOM_CENTER = 0;
	public static final int HOTSPOT_TOP_LEFT = 1;
	public static final int HOTSPOT_CENTER_CENTER = 2;

	public void setIconStyleHotSpot(int hotspot) {
		if (iconStyle == null) {
			Logger.logW(TAG, "setIconStyleHotSpot(" + hotspot + "), " +
					"initialize IconStyle before settings hotSpot!");
			return;
		}
		
		if (hotspot == HOTSPOT_TOP_LEFT) {
			iconStyle.hotSpot = new KmlVec2(
					0.0f, KmlVec2.Units.FRACTION, 1.0f, KmlVec2.Units.FRACTION);
		} else if (hotspot == HOTSPOT_CENTER_CENTER) {
			iconStyle.hotSpot = new KmlVec2(
					0.5f, KmlVec2.Units.FRACTION, 0.5f, KmlVec2.Units.FRACTION);
		} else {
			// hotspot == HOTSPOT_BOTTOM_CENTER
			iconStyle.hotSpot = generateDefaultHotSpot();
		}
	}
	
	private static KmlVec2 generateDefaultHotSpot() {
		// HOTSPOT_BOTTOM_CENTER
		return new KmlVec2(0.5f, KmlVec2.Units.FRACTION,
				0.0f, KmlVec2.Units.FRACTION);
	}
	
	public void setIconStyleHotSpot(KmlVec2 vec2) {
		if (iconStyle == null || vec2 == null) {
			Logger.logW(TAG, "setIconStyleHotSpot(" + vec2 + "), " +
					"initialize IconStyle before settings hotSpot or hotSpot is null!");
			return;
		}
		
		iconStyle.hotSpot = vec2;
	}
	
	// LINE STYLE

    /**
     * Get current defined style for lines.
     * @return style for lines
     */
	public LineStyle getLineStyle() {
		return lineStyle;
	}

    /**
     * Remove defined style for lines.
     */
    public void removeLineStyle() {
    	lineStyle = null;
    }

    /**
     * Set parameters for style that draw a lines.
     * @param color color of lines
     * @param width width of lines in pixels
     */
    public void setLineStyle(int color, float width) {
    	setLineStyle(ColorStyle.SIMPLE, color, width, Units.PIXELS);
    }
    
    public void setLineStyle(LineStyle.ColorStyle style, int color,
    		float width, LineStyle.Units units) {
    	if (lineStyle == null) {
    		lineStyle = new LineStyle();
    	}
    	lineStyle.colorStyle = style;
    	lineStyle.color = color;
    	lineStyle.setWidth(width);
    	lineStyle.units = units;
    }
    
    public void setLineType(LineStyle.LineType type) {
    	if (lineStyle == null) {
    		lineStyle = new LineStyle();
    	}
    	lineStyle.lineType = type;
    }
    
	public void setLineOutline(boolean drawOutline, int colorOutline) {
    	if (lineStyle == null) {
    		lineStyle = new LineStyle();
    	}
    	lineStyle.drawOutline = drawOutline;
    	lineStyle.colorOutline = colorOutline;
	}

    // POLY STYLE

    /**
     * Get current defined style for polygons.
     * @return style for polygons
     */
    public PolyStyle getPolyStyle() {
        return polyStyle;
    }

    public void setPolyStyle(int color, boolean fill, boolean outline) {
    	polyStyle = new PolyStyle();
    	polyStyle.color = color;
    	polyStyle.fill = fill;
    	polyStyle.outline = outline;
    }
    
    public void removePolyStyle() {
    	polyStyle = null;
    }

    // LABEL STYLE

    /**
     * Get current defined style of label.
     * @return style of label
     */
    public LabelStyle getLabelStyle() {
        return labelStyle;
    }

    /**************************************************/
    // STYLES
    /**************************************************/

	// shortcut to simple black color
    public static final int BLACK       										= 0xFF000000;
    // shortcut to simple white color
	public static final int WHITE       										= 0xFFFFFFFF;
    // default basic color
	public static final int COLOR_DEFAULT                                       = WHITE;

	public static class BalloonStyle extends Storable {

		public enum DisplayMode {
			DEFAULT, HIDE
		}
		
		public int bgColor;
		public int textColor;
		public String text;
		public DisplayMode displayMode;

		@Override
		protected int getVersion() {
			return 0;
		}

		@Override
		public void reset() {
			bgColor = WHITE;
			textColor = BLACK;
			text = "";
			displayMode = DisplayMode.DEFAULT;
		}

		@Override
		protected void readObject(int version, DataReaderBigEndian dr)
				throws IOException {
			bgColor = dr.readInt();
			textColor = dr.readInt();
			text = dr.readString();
			int mode = dr.readInt();
			if (mode < DisplayMode.values().length) {
				displayMode = DisplayMode.values()[mode];
			}
		}
		
		@Override
		protected void writeObject(DataWriterBigEndian dw) throws IOException {
			dw.writeInt(bgColor);
			dw.writeInt(textColor);
			dw.writeString(text);
			dw.writeInt(displayMode.ordinal());
		}
	}
	
	public static class IconStyle extends Storable {
		
		public int color;
        // scale of icon, where 1.0f means base no-scale value
		private float mScale;
		public float heading;
		@Deprecated // do not use directly
		public String iconHref;
		public KmlVec2 hotSpot;
		
		// temporary variables for Locus usage that are not serialized
		// and are for private Locus usage only
		public Object icon;
		public int iconW;
		public int iconH;
		public float scaleCurrent;

        /**
         * Default empty constructor.
         */
		public IconStyle() {
			super();
		}

        // SCALE

        /**
         * Get current defined scale.
         * @return scale value, base is 1.0f
         */
		public float getScale() {
			return mScale;
		}

        /**
         * Set new scale value.
         * @param scale scale to set
         */
		public void setScale(float scale) {
			if (scale != 0.0f) {
				this.mScale = scale;
				this.scaleCurrent = scale;
			}
		}

		/**
		 * Get link/id to image.
		 * @return href parameter
		 */
		private String getIconHref() {
			return iconHref;
		}

		/**
		 * Set link/id to image.
		 * @param iconHref href to image
		 */
		public void setIconHref(String iconHref) {
			if (iconHref == null) {
				iconHref = "";
			}
			this.iconHref = iconHref;
		}

		// STORABLE PART
		
		@Override
		protected int getVersion() {
			return 0;
		}

		@Override
		public void reset() {
			color = COLOR_DEFAULT;
            mScale = 1.0f;
			heading = 0.0f;
			iconHref = null;
			hotSpot = generateDefaultHotSpot();
			
			icon = null;
			iconW = -1;
			iconH = -1;
			scaleCurrent = 1.0f;
		}

		@Override
		protected void readObject(int version, DataReaderBigEndian dr)
				throws IOException {
			color = dr.readInt();
            mScale = dr.readFloat();
			heading = dr.readFloat();
			iconHref = dr.readString();
			hotSpot = KmlVec2.read(dr);
		}
		
		@Override
		protected void writeObject(DataWriterBigEndian dw) throws IOException {
			dw.writeInt(color);
			dw.writeFloat(mScale);
			dw.writeFloat(heading);
			dw.writeString(iconHref);
			hotSpot.write(dw);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("IconStyle [");
			sb.append("color:").append(color);
			sb.append(", scale:").append(mScale);
			sb.append(", heading:").append(heading);
			sb.append(", iconHref:").append(iconHref);
			sb.append(", hotSpot:").append(hotSpot);

			sb.append(", icon:").append(icon);
			sb.append(", iconW:").append(iconW);
			sb.append(", iconH:").append(iconH);
			sb.append(", scaleCurrent:").append(scaleCurrent);
			sb.append("]");
			return sb.toString();
		}
	}

	/**
	 * Container for label style.
	 */
	public static class LabelStyle extends Storable {

		// color of label
		private int mColor = COLOR_DEFAULT;
		// scale of label
		private float mScale = 1.0f;

		/**
		 * Get color defined for a label.
		 * @return color of label
		 */
		public int getColor() {
			return mColor;
		}

		/**
		 * Set new color to this label.
		 * @param color new color
		 */
		public void setColor(int color) {
			this.mColor = color;
		}

		/**
		 * Get scale defined for a label.
		 * @return scale of label, where 1.0 equals 100%
		 */
		public float getScale() {
			return mScale;
		}

		/**
		 * Set new scale to this label.
		 * @param scale new scale value
		 */
		public void setScale(float scale) {
			if (scale < 0.0f) {
				scale = 0.0f;
			}
			this.mScale = scale;
		}

		// STORABLE

		@Override
		protected int getVersion() {
			return 0;
		}

		@Override
		public void reset() {
			mColor = COLOR_DEFAULT;
			mScale = 1.0f;
		}

		@Override
		protected void readObject(int version, DataReaderBigEndian dr)
				throws IOException {
			mColor = dr.readInt();
			mScale = dr.readFloat();
		}
		
		@Override
		protected void writeObject(DataWriterBigEndian dw) throws IOException {
			dw.writeInt(mColor);
			dw.writeFloat(mScale);
		}
	}

	/**
	 * Style of lines.
	 */
	public static class LineStyle extends Storable {

		/**
		 * Special color style for a lines.
		 */
		public enum ColorStyle {
			// simple coloring
			SIMPLE,
			// coloring by speed value
			BY_SPEED,
			// coloring (relative) by altitude value
			BY_ALTITUDE,
			// coloring (relative) by accuracy
			BY_ACCURACY,
			// coloring (relative) by speed change
			BY_SPEED_CHANGE,
			// coloring (relative) by slope
			BY_SLOPE_REL,
			// coloring (relative) by heart rate value
			BY_HRM,
			// coloring (relative) by cadence
			BY_CADENCE,
			// coloring (absolute) by slope
			BY_SLOPE_ABS
		}

		/**
		 * Used units for line width.
		 */
		public enum Units {
			PIXELS, METRES
		}

		/**
		 * Type how line is presented to user.
		 */
		public enum LineType {
			NORMAL, DOTTED, DASHED_1, DASHED_2, DASHED_3,
			SPECIAL_1, SPECIAL_2, SPECIAL_3,
			ARROW_1, ARROW_2, ARROW_3,
			CROSS_1, CROSS_2
		}
		
		// KML styles
		public int color;
		// width of line [px | m]
		private float mWidth;
		public int gxOuterColor;
		public float gxOuterWidth;
		/**
		 * Not used. Instead of using this parameter, use {@link #mWidth} and define
		 * {@link #units} to Units.METRES
		 */
		@Deprecated
		public float gxPhysicalWidth;
		public boolean gxLabelVisibility;
		
		// Locus extension
		public ColorStyle colorStyle;
		public Units units;
		public LineType lineType;
		public boolean drawOutline;
		public int colorOutline;

		// temporary helper to convert old widths. Because of this, we increased version of object to V2,
		// so it's clear if value is in DPI or PX values.
		private int mObjectVersion;

		/**
		 * Create new line style object.
		 */
		public LineStyle() {
			super();
		}

		/**
		 * Get temporary version of line style object.
		 * @return object version defined by storable container
		 */
		public int getObjectVersion() {
			return mObjectVersion;
		}

		/**
		 * Set special object version type.
		 * @param objectVersion object version
		 */
		public void setObjectVersion(int objectVersion) {
			mObjectVersion = objectVersion;
		}

		// WIDTH

		/**
		 * Get width of line in units defined by {@link #units} parameter.
		 * @return line width
		 */
		public float getWidth() {
			return mWidth;
		}

		/**
		 * Set line width in units defined by {@link #units} parameter.
		 * @param width line width
		 */
		public void setWidth(float width) {
			this.mWidth = width;
		}

		// WIDTH UNITS

		/**
		 * Get line units.
		 * @return line units
		 */
		public Units getUnits() {
			return units;
		}

		/**
		 * Define line units.
		 * @param units line units
		 */
		public void setUnits(Units units) {
			this.units = units;
		}


		// STORABLE

		@Override
		protected int getVersion() {
			return 2;
		}

		@Override
		public void reset() {
			mObjectVersion = getVersion();
			color = COLOR_DEFAULT;
			mWidth = 1.0f;
			gxOuterColor = COLOR_DEFAULT;
			gxOuterWidth = 0.0f;
			gxPhysicalWidth = 0.0f;
			gxLabelVisibility = false;
			
			// Locus extension
			colorStyle = ColorStyle.SIMPLE;
			units = Units.PIXELS;
			lineType = LineType.NORMAL;
			drawOutline = false;
			colorOutline = WHITE;
		}

		@Override
		protected void readObject(int version, DataReaderBigEndian dr)
				throws IOException {
			mObjectVersion = version;
			color = dr.readInt();
			mWidth = dr.readFloat();
			gxOuterColor = dr.readInt();
			gxOuterWidth = dr.readFloat();
			gxPhysicalWidth = dr.readFloat();
			gxLabelVisibility = dr.readBoolean();
			
			int cs = dr.readInt();
			if (cs < ColorStyle.values().length) {
				colorStyle = ColorStyle.values()[cs];
			}
			int un = dr.readInt();
			if (un < Units.values().length) {
				units = Units.values()[un];
			}
			int lt = dr.readInt();
			if (lt < LineType.values().length) {
				lineType = LineType.values()[lt];
			}
			
			// V1

			if (version >= 1) {
				drawOutline = dr.readBoolean();
				colorOutline = dr.readInt();
			}
		}
		
		@Override
		protected void writeObject(DataWriterBigEndian dw) throws IOException {
			dw.writeInt(color);
			dw.writeFloat(mWidth);
			dw.writeInt(gxOuterColor);
			dw.writeFloat(gxOuterWidth);
			dw.writeFloat(gxPhysicalWidth);
			dw.writeBoolean(gxLabelVisibility);
			dw.writeInt(colorStyle.ordinal());
			dw.writeInt(units.ordinal());
			dw.writeInt(lineType.ordinal());

			// V1

			dw.writeBoolean(drawOutline);
			dw.writeInt(colorOutline);
		}
	}
	
	public static class ListStyle extends Storable {
		
		public enum ListItemType {
			CHECK, CHECK_OFF_ONLY, CHECK_HIDE_CHILDREN, RADIO_FOLDER
		}

		public static class ItemIcon {

			public enum State {
				OPEN, CLOSED, ERROR, FETCHING0, FETCHING1, FETCHING2
			}
			
			public State state = State.OPEN;
			public String href = "";
		}

		public ListItemType listItemType = ListItemType.CHECK;
		public int bgColor = WHITE;
		public ArrayList<ItemIcon> itemIcons = new ArrayList<>();

		@Override
		protected int getVersion() {
			return 0;
		}

		@Override
		public void reset() {
			listItemType = ListItemType.CHECK;
			bgColor = WHITE;
			itemIcons = new ArrayList<>();
		}

		@Override
		protected void readObject(int version, DataReaderBigEndian dis)
				throws IOException {
			int style = dis.readInt();
			if (style < ListStyle.ListItemType.values().length) {
				listItemType = ListStyle.ListItemType.values()[style];
			}
			bgColor = dis.readInt();
			int itemsCount = dis.readInt();
			for (int i = 0; i < itemsCount; i++) {
				ListStyle.ItemIcon itemIcon = new ListStyle.ItemIcon();
				int iconStyle = dis.readInt();
				if (iconStyle < ListStyle.ItemIcon.State.values().length) {
					itemIcon.state = ListStyle.ItemIcon.State.values()[iconStyle];	
				}
				itemIcon.href = dis.readString();
				itemIcons.add(itemIcon);
			}
		}
		
		@Override
		protected void writeObject(DataWriterBigEndian dw) throws IOException {
			dw.writeBoolean(true);
			dw.writeInt(listItemType.ordinal());
			dw.writeInt(bgColor);
			dw.writeInt(itemIcons.size());
			for (ListStyle.ItemIcon itemIcon : itemIcons) {
				dw.writeInt(itemIcon.state.ordinal());
				dw.writeString(itemIcon.href);
			}
		}
	}
	
	public static class PolyStyle extends Storable {
		
		public int color = COLOR_DEFAULT;
		public boolean fill = true;
		public boolean outline = true;
		
		@Override
		protected int getVersion() {
			return 0;
		}

		@Override
		public void reset() {
			color = COLOR_DEFAULT;
			fill = true;
			outline = true;
		}

		@Override
		protected void readObject(int version, DataReaderBigEndian dis)
				throws IOException {
			color = dis.readInt();
			fill = dis.readBoolean();
			outline = dis.readBoolean();
		}
		
		@Override
		protected void writeObject(DataWriterBigEndian dw) throws IOException {
			dw.writeInt(color);
			dw.writeBoolean(fill);
			dw.writeBoolean(outline);
		}
	}

    /**************************************************/
    // STORABLE PART
    /**************************************************/

    @Override
    protected int getVersion() {
        return 1;
    }

    @Override
    public void reset() {
        mId = "";
		mName = "";
        balloonStyle = null;
        iconStyle = null;
        labelStyle = null;
        lineStyle = null;
        listStyle = null;
        polyStyle = null;
    }

    @Override
    protected void readObject(int version, DataReaderBigEndian dr)
            throws IOException {
        // read core
		mId = dr.readString();
		mName = dr.readString();

        // read old version 0
        if (version == 0) {
            // this method breaks compatibility if any app with older API will try to load new data
            readVersion0(dr);
            return;
        }

        // balloon style
        try {
            if (dr.readBoolean()) {
                balloonStyle = (BalloonStyle) Storable.read(BalloonStyle.class, dr);
            }
            if (dr.readBoolean()) {
                iconStyle = (IconStyle) Storable.read(IconStyle.class, dr);
            }
            if (dr.readBoolean()) {
                labelStyle = (LabelStyle) Storable.read(LabelStyle.class, dr);
            }
            if (dr.readBoolean()) {
                lineStyle = (LineStyle) Storable.read(LineStyle.class, dr);
            }
            if (dr.readBoolean()) {
                listStyle = (ListStyle) Storable.read(ListStyle.class, dr);
            }
            if (dr.readBoolean()) {
                polyStyle = (PolyStyle) Storable.read(PolyStyle.class, dr);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
			e.printStackTrace();
		}
    }

    private void readVersion0(DataReaderBigEndian dr) throws IOException {
        // balloon style
        if (dr.readBoolean()) {
            balloonStyle = new BalloonStyle();
            balloonStyle.bgColor = dr.readInt();
            balloonStyle.textColor = dr.readInt();
            balloonStyle.text = dr.readString();
            int displayMode = dr.readInt();
            if (displayMode < BalloonStyle.DisplayMode.values().length) {
                balloonStyle.displayMode = BalloonStyle.
                        DisplayMode.values()[displayMode];
            }
        }

        // icon style
        if (dr.readBoolean()) {
            iconStyle = new IconStyle();
            iconStyle.color = dr.readInt();
            iconStyle.setScale(dr.readFloat());
            iconStyle.heading = dr.readFloat();
            iconStyle.iconHref = dr.readString();
            iconStyle.hotSpot = KmlVec2.read(dr);
        }

        // label style
        if (dr.readBoolean()) {
            labelStyle = new LabelStyle();
            labelStyle.setColor(dr.readInt());
            labelStyle.setScale(dr.readFloat());
        }

        // line style
        if (dr.readBoolean()) {
            lineStyle = new LineStyle();
            lineStyle.color = dr.readInt();
            lineStyle.mWidth = dr.readFloat();
            lineStyle.gxOuterColor = dr.readInt();
            lineStyle.gxOuterWidth = dr.readFloat();
            lineStyle.gxPhysicalWidth = dr.readFloat();
            lineStyle.gxLabelVisibility = dr.readBoolean();

            int colorStyle = dr.readInt();
            if (colorStyle < LineStyle.ColorStyle.values().length) {
                lineStyle.colorStyle = LineStyle.ColorStyle.values()[colorStyle];
            }
            int units = dr.readInt();
            if (units < LineStyle.Units.values().length) {
                lineStyle.units = LineStyle.Units.values()[units];
            }
        }

        // list style
        if (dr.readBoolean()) {
            listStyle = new ListStyle();
            int listItemStyle = dr.readInt();
            if (listItemStyle < ListStyle.ListItemType.values().length) {
                listStyle.listItemType = ListStyle.ListItemType.values()[listItemStyle];
            }
            listStyle.bgColor = dr.readInt();
            int itemsCount = dr.readInt();
            for (int i = 0; i < itemsCount; i++) {
                ListStyle.ItemIcon itemIcon = new ListStyle.ItemIcon();
                int iconStyle = dr.readInt();
                if (iconStyle < ListStyle.ItemIcon.State.values().length) {
                    itemIcon.state = ListStyle.ItemIcon.State.values()[iconStyle];
                }
                itemIcon.href = dr.readString();
                listStyle.itemIcons.add(itemIcon);
            }
        }

        // poly style
        if (dr.readBoolean()) {
            polyStyle = new PolyStyle();
            polyStyle.color = dr.readInt();
            polyStyle.fill = dr.readBoolean();
            polyStyle.outline = dr.readBoolean();
        }
    }

    @Override
    protected void writeObject(DataWriterBigEndian dw) throws IOException {
        // write core
        dw.writeString(mId);
        dw.writeString(mName);

        // balloon style
        if (balloonStyle == null) {
            dw.writeBoolean(false);
        } else {
            dw.writeBoolean(true);
            balloonStyle.write(dw);
        }

        // icon style
        if (iconStyle == null) {
            dw.writeBoolean(false);
        } else {
            dw.writeBoolean(true);
            iconStyle.write(dw);
        }

        // label style
        if (labelStyle == null) {
            dw.writeBoolean(false);
        } else {
            dw.writeBoolean(true);
            labelStyle.write(dw);
        }

        // line style
        if (lineStyle == null) {
            dw.writeBoolean(false);
        } else {
            dw.writeBoolean(true);
            lineStyle.write(dw);
        }

        // list style
        if (listStyle == null) {
            dw.writeBoolean(false);
        } else {
            dw.writeBoolean(true);
            listStyle.write(dw);
        }

        // poly style
        if (polyStyle == null) {
            dw.writeBoolean(false);
        } else {
            dw.writeBoolean(true);
            polyStyle.write(dw);
        }
    }
}

