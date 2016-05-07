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

	private static final String TAG = Utils.class.getSimpleName();
	
	/**************************************************/
	/*                 PARSE SECTION                  */
	/**************************************************/
    
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
	 * @param data text to parse
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
    
	/**************************************************/
	/*                 OTHER TOOLS                    */
	/**************************************************/
    
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
    	result.append(obj.getClass().getName()).append(" {").append(NEW_LINE);

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
    			System.out.println(ex);
    		}
    		result.append(NEW_LINE);
    	}
    	result.append(prefix).append("}");
    	return result.toString();
    }
    
    // ARRAYS TOOLS (AVAILABLE SINCE ANDROID 2.3.X+)
    
    /**
     * Copies the specified array, truncating or padding with zeros (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>(byte)0</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with zeros
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static byte[] copyOf(byte[] original, int newLength) {
        byte[] copy = new byte[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }
    
    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>(byte)0</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with zeros to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static byte[] copyOfRange(byte[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

	public static float[] copyOfRange(float[] original, int start, int end) {
		if (start > end) {
			throw new IllegalArgumentException();
		}
		int originalLength = original.length;
		if (start < 0 || start > originalLength) {
			throw new ArrayIndexOutOfBoundsException();
		}
		int resultLength = end - start;
		int copyLength = Math.min(resultLength, originalLength - start);
		float[] result = new float[resultLength];
		System.arraycopy(original, start, result, 0, copyLength);
		return result;
	}
}
