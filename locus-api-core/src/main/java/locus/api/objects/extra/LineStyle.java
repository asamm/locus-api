package locus.api.objects.extra;

import java.io.IOException;
import java.util.Hashtable;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

/**
 * Created by menion on 19/10/2017.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */

public class LineStyle extends Storable {

    // white color
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    // black color
    private static final int COLOR_BLACK = 0xFF000000;

    // parameters for storing extra meta information for line coloring
    public static String KEY_CP_ALTITUDE_MANUAL = "alt_man";
    public static String KEY_CP_ALTITUDE_MANUAL_MIN = "alt_man_min";
    public static String KEY_CP_ALTITUDE_MANUAL_MAX = "alt_man_max";
    public static String KEY_CP_SLOPE_MANUAL = "slo_man";
    public static String KEY_CP_SLOPE_MANUAL_MIN = "slo_man_min";
    public static String KEY_CP_SLOPE_MANUAL_MAX = "slo_man_max";

    /**
     * Type how line is presented to user.
     */
    public enum Symbol {
        DOTTED,
        DASHED_1, DASHED_2, DASHED_3,
        SPECIAL_1, SPECIAL_2, SPECIAL_3,
        ARROW_1, ARROW_2, ARROW_3,
        CROSS_1, CROSS_2
    }

    /**
     * Special color style for a lines.
     */
    public enum Coloring {
        // simple coloring
        SIMPLE,
        // coloring by speed value
        BY_SPEED,
        // coloring (relative) by speed change
        BY_SPEED_CHANGE,
        // coloring (relative) by altitude value
        BY_ALTITUDE,
        // coloring (relative) by slope
        BY_SLOPE,
        // coloring (relative) by accuracy
        BY_ACCURACY,
        // coloring (relative) by heart rate value
        BY_HRM,
        // coloring (relative) by cadence
        BY_CADENCE,
    }

    /**
     * Used units for line width.
     */
    public enum Units {
        PIXELS, METRES
    }

    // PARAMETERS

    // flag to draw background
    private boolean mDrawBase;
    // base background color
    private int mColorBase;
    // flag to draw symbol
    private boolean mDrawSymbol;
    // symbol coloring
    private int mColorSymbol;
    // selected symbol
    private Symbol mSymbol;
    // coloring style
    private Coloring mColoring;
    // coloring parameters
    private Hashtable<String, String> mColoringParams;
    // width of line [px | m]
    private float mWidth;
    // width units
    private Units mUnits;
    // flag to draw outline
    private boolean mDrawOutline;
    // color of outline
    private int mColorOutline;
    // flag if track should be closed and filled
    private boolean mDrawFill;
    // color of fill
    private int mColorFill;

    /**
     * Default empty constructor.
     */
    @SuppressWarnings("WeakerAccess")
    public LineStyle() {
        mDrawBase = true;
        mColorBase = COLOR_BLACK;
        mDrawSymbol = false;
        mColorSymbol = COLOR_WHITE;
        mSymbol = Symbol.DOTTED;
        mColoring = Coloring.SIMPLE;
        mColoringParams = new Hashtable<>();
        mWidth = 1.0f;
        mUnits = Units.PIXELS;
        mDrawOutline = false;
        mColorOutline = COLOR_WHITE;
        mDrawFill = false;
        mColorFill = COLOR_WHITE;
    }

    //*************************************************
    // GET & SET
    //*************************************************

    /**
     * Flag if draw base line.
     *
     * @return {@code true} to draw base line
     */
    public boolean isDrawBase() {
        return mDrawBase;
    }

    /**
     * Set to draw a base line.
     *
     * @param drawBase {@code true} to draw a base line
     * @return current object
     */
    public LineStyle setDrawBase(boolean drawBase) {
        mDrawBase = drawBase;
        return this;
    }

    /**
     * Get base style color.
     *
     * @return color
     */
    public int getColorBase() {
        return mColorBase;
    }

    /**
     * Set base style color.
     *
     * @param colorBase color
     * @return current object
     */
    public LineStyle setColorBase(int colorBase) {
        mColorBase = colorBase;
        return this;
    }

    /**
     * Flag if draw overlay symbols.
     *
     * @return {@code true} to draw symbols
     */
    public boolean isDrawSymbol() {
        return mDrawSymbol;
    }

    /**
     * @param drawSymbol
     * @return current object
     */
    public LineStyle setDrawSymbol(boolean drawSymbol) {
        mDrawSymbol = drawSymbol;
        return this;
    }

    /**
     * Get color for a symbol.
     *
     * @return color
     */
    public int getColorSymbol() {
        return mColorSymbol;
    }

    /**
     * Set color for a draw symbol.
     *
     * @param colorSymbol color
     * @return current object
     */
    public LineStyle setColorSymbol(int colorSymbol) {
        mColorSymbol = colorSymbol;
        return this;
    }

    /**
     * Get style of symbol to draw.
     *
     * @return defined symbol
     */
    public Symbol getSymbol() {
        return mSymbol;
    }

    /**
     * Set symbol to draw.
     *
     * @param symbol symbol
     * @return current object
     */
    public LineStyle setSymbol(Symbol symbol) {
        mSymbol = symbol;
        return this;
    }

    // COLORING

    public Coloring getColoring() {
        return mColoring;
    }

    /**
     * @param coloring
     * @return current object
     */
    public LineStyle setColoring(Coloring coloring) {
        mColoring = coloring;
        return this;
    }

    // COLORING PARAMETERS

    /**
     * Get value for certain coloring parameter.
     *
     * @param key key of parameter
     * @return parameter value
     */
    public String getColoringParam(String key) {
        // check key
        if (key == null || key.length() == 0) {
            return null;
        }

        // return value
        return mColoringParams.get(key);
    }

    /**
     * Put valid coloring parameter into container.
     *
     * @param key   key of parameter
     * @param value parameter value
     * @return current object
     */
    public LineStyle setColoringParam(String key, String value) {
        // check params
        if (key == null || key.length() == 0) {
            return this;
        }

        // store value
        if (value == null) {
            mColoringParams.remove(key);
        } else {
            mColoringParams.put(key, value);
        }
        return this;
    }

    // WIDTH

    /**
     * Get width value independent on units.
     *
     * @return width value
     */
    public float getWidth() {
        return mWidth;
    }

    /**
     * Set width value for current style.
     *
     * @param width width value
     * @return current object
     */
    public LineStyle setWidth(float width) {
        mWidth = width;
        return this;
    }

    /**
     * Get units for defined line width.
     *
     * @return line units
     */
    public Units getUnits() {
        return mUnits;
    }

    /**
     * Set units for line width.
     *
     * @param units units
     * @return current object
     */
    public LineStyle setUnits(Units units) {
        mUnits = units;
        return this;
    }

    /**
     * Flag if draw outline.
     *
     * @return {@code true} to draw outline
     */
    public boolean isDrawOutline() {
        return mDrawOutline;
    }

    /**
     * @param drawOutline
     * @return current object
     */
    public LineStyle setDrawOutline(boolean drawOutline) {
        mDrawOutline = drawOutline;
        return this;
    }

    public int getColorOutline() {
        return mColorOutline;
    }

    /**
     * @param colorOutline
     * @return current object
     */
    public LineStyle setColorOutline(int colorOutline) {
        mColorOutline = colorOutline;
        return this;
    }

    /**
     * Flag if draw polygon fill color.
     *
     * @return {@code true} to draw fill
     */
    public boolean isDrawFill() {
        return mDrawFill;
    }

    /**
     * Set flag to fill an area with a color.
     *
     * @param drawFill {@code true} to draw a fill
     * @return current object
     */
    public LineStyle setDrawFill(boolean drawFill) {
        mDrawFill = drawFill;
        return this;
    }

    /**
     * Get color for a fill style.
     *
     * @return fill color
     */
    public int getColorFill() {
        return mColorFill;
    }

    /**
     * Set color for a polygon fill.
     *
     * @param colorFill fill color
     * @return current object
     */
    public LineStyle setColorFill(int colorFill) {
        mColorFill = colorFill;
        return this;
    }

    //*************************************************
    // TOOLS
    //*************************************************

    /**
     * Check if any valid "draw" parameter is defined.
     *
     * @return {@code true} if anything to draw is set
     */
    public boolean isDrawDefined() {
        return isDrawBase() || isDrawSymbol() || isDrawFill();
    }

    //*************************************************
    // STORABLE
    //*************************************************

    @Override
    protected int getVersion() {
        return 0;
    }

    @Override
    protected void readObject(int version, DataReaderBigEndian dr) throws IOException {
        mDrawBase = dr.readBoolean();
        mColorBase = dr.readInt();
        mDrawSymbol = dr.readBoolean();
        mColorSymbol = dr.readInt();
        mSymbol = Symbol.valueOf(dr.readString());
        mColoring = Coloring.valueOf(dr.readString());
        for (int i = 0, m = dr.readInt(); i < m; i++) {
            mColoringParams.put(dr.readString(), dr.readString());
        }
        mWidth = dr.readFloat();
        mUnits = Units.valueOf(dr.readString());
        mDrawOutline = dr.readBoolean();
        mColorOutline = dr.readInt();
        mDrawFill = dr.readBoolean();
        mColorFill = dr.readInt();
    }

    @Override
    protected void writeObject(DataWriterBigEndian dw) throws IOException {
        dw.writeBoolean(mDrawBase);
        dw.writeInt(mColorBase);
        dw.writeBoolean(mDrawSymbol);
        dw.writeInt(mColorSymbol);
        dw.writeString(mSymbol.name());
        dw.writeString(mColoring.name());
        dw.writeInt(mColoringParams.size());
        for (String key : mColoringParams.keySet()) {
            dw.writeString(key);
            dw.writeString(mColoringParams.get(key));
        }
        dw.writeFloat(mWidth);
        dw.writeString(mUnits.name());
        dw.writeBoolean(mDrawOutline);
        dw.writeInt(mColorOutline);
        dw.writeBoolean(mDrawFill);
        dw.writeInt(mColorFill);
    }
}
