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

package locus.api.utils;

import java.io.Closeable;
import java.lang.reflect.Field;

public class Utils {

    // tag for logger
    private static final String TAG = "Utils";

    //*************************************************
    // PARSE SECTION
    //*************************************************

    public static boolean parseBoolean(Object data) {
        return parseBoolean(String.valueOf(data));
    }

    public static boolean parseBoolean(String data) {
        try {
            return data.toLowerCase().contains("true") || data.contains("1");
        } catch (Exception e) {
            return false;
        }
    }

    public static int parseInt(Object data) {
        return parseInt(String.valueOf(data));
    }

    public static int parseInt(String data) {
        return parseInt(data, 0);
    }

    /**
     * Parse integer value from text.
     *
     * @param data     text to parse
     * @param defValue default value used in case, text is not an integer
     * @return parsed value
     */
    public static int parseInt(String data, int defValue) {
        try {
            return Integer.parseInt(data.trim());
        } catch (Exception e) {
            return defValue;
        }
    }

    public static Integer parseInteger(Object data) {
        return parseInteger(String.valueOf(data));
    }

    public static Integer parseInteger(String data) {
        try {
            return Integer.valueOf(data.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public static long parseLong(Object data) {
        return parseLong(String.valueOf(data));
    }

    public static long parseLong(String data) {
        try {
            return Long.parseLong(data.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public static float parseFloat(Object data) {
        return parseFloat(String.valueOf(data));
    }

    public static float parseFloat(String data) {
        try {
            return Float.parseFloat(data.trim());
        } catch (Exception e) {
            return 0.0f;
        }
    }

    public static double parseDouble(Object data) {
        return parseDouble(String.valueOf(data));
    }

    public static double parseDouble(String data) {
        try {
            data = data.trim().replace(",", ".");
            return Double.parseDouble(data);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static byte[] doStringToBytes(String text) {
        try {
            return text.getBytes("UTF-8");
        } catch (Exception e) {
            Logger.logE(TAG, "doStringToBytes(" + text + ")", e);
            return new byte[0];
        }
    }

    public static String doBytesToString(byte[] data) {
        try {
            return new String(data, "UTF-8");
        } catch (Exception e) {
            Logger.logE(TAG, "doBytesToString(" + data + ")", e);
            return "";
        }
    }

    //*************************************************
    // OTHER TOOLS
    //*************************************************

    public static void closeStream(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        } catch (Exception e) {
            System.err.println("closeStream(" + stream + "), e:" + e);
            e.printStackTrace();
        }
    }

    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    private static final String NEW_LINE = System.getProperty("line.separator");

    public static String toString(Object obj) {
        return toString(obj, "");
    }

    public static String toString(Object obj, String prefix) {
        // add base
        StringBuilder result = new StringBuilder();
        result.append(prefix);
        if (obj == null) {
            result.append(" empty object!");
            return result.toString();
        }

        // handle existing object
        result.append(obj.getClass().getName()).append(" [").append(NEW_LINE);

        // determine fields declared in this class only (no fields of superclass)
        Field[] fields = obj.getClass().getDeclaredFields();

        // print field names paired with their values
        for (Field field : fields) {
            result.append(prefix).append("    ");
            try {
                result.append(field.getName());
                result.append(": ");
                // set accessible for private fields
                field.setAccessible(true);
                // requires access to private field:
                result.append(field.get(obj));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            result.append(NEW_LINE);
        }
        result.append(prefix).append("]");
        return result.toString();
    }
}
