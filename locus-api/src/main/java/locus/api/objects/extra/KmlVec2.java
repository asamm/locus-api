package locus.api.objects.extra;

import java.io.IOException;

import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

public class KmlVec2 {

	public static enum Units {
		FRACTION, PIXELS, INSET_PIXELS
	}
	
	public double x = 0.5f;
	public Units xUnits = Units.FRACTION;
	public double y = 0.5f;
	public Units yUnits = Units.FRACTION;
	
	public KmlVec2() {}

	public KmlVec2(double x, Units xUnits, double y, Units yUnits) {
		this.x = x;
		this.xUnits = xUnits;
		this.y = y;
		this.yUnits = yUnits;
	}
	
	public double[] getCoords(double sourceWidth, double sourceHeight) {
        return getCoords(sourceWidth, sourceHeight, new double[2]);
	}

    public double[] getCoords(double sourceWidth, double sourceHeight, double[] result) {
        // check container for results
        if (result == null || result.length != 2) {
            result = new double[2];
        }

        // set X units
        if (xUnits == Units.FRACTION) {
            result[0] = sourceWidth * x;
        } else if (xUnits == Units.PIXELS) {
            result[0] = x;
        } else if (xUnits == Units.INSET_PIXELS) {
            result[0] = sourceWidth - x;
        }

        // set Y units
        if (yUnits == Units.FRACTION) {
            result[1] = sourceHeight * (1.0 - y);
        } else if (yUnits == Units.PIXELS) {
            result[1] = sourceHeight - y;
        } else if (yUnits == Units.INSET_PIXELS) {
            result[1] = y;
        }

        // return result
        return result;
    }

    public String getAsXmlText() {
        StringBuilder sb = new StringBuilder();

        // insert coordinates
        sb.append("\t\t\t<hotSpot x=\"").append(x).append("\" y=\"").append(y).append("\"");

        // insert X units
        sb.append(" xunits=\"");
        switch (xUnits) {
            case FRACTION:
                sb.append("fraction");
                break;
            case PIXELS:
                sb.append("pixels");
                break;
            case INSET_PIXELS:
                sb.append("insetPixels");
                break;
        }
        sb.append("\"");

        // insert Y units
        sb.append(" yunits=\"");
        switch (yUnits) {
            case FRACTION:
                sb.append("fraction");
                break;
            case PIXELS:
                sb.append("pixels");
                break;
            case INSET_PIXELS:
                sb.append("insetPixels");
                break;
        }
        sb.append("\"");

        // close tag and return
        sb.append(" />");
        return sb.toString();
    }

	public KmlVec2 getCopy() {
		KmlVec2 vec = new KmlVec2();
		vec.x = x;
		vec.xUnits = xUnits;
		vec.y = y;
		vec.yUnits = yUnits;
		return vec;
	}
	
	public void write(DataWriterBigEndian dw) throws IOException {
		dw.writeDouble(x);
		dw.writeInt(xUnits.ordinal());
		dw.writeDouble(y);
		dw.writeInt(yUnits.ordinal());
	}
	
	public static KmlVec2 read(DataReaderBigEndian dr) throws IOException {
		KmlVec2 vec = new KmlVec2();
		vec.x = dr.readDouble();
		vec.xUnits = Units.values()[dr.readInt()];
		vec.y = dr.readDouble();
		vec.yUnits = Units.values()[dr.readInt()];
		return vec;
	}
}
