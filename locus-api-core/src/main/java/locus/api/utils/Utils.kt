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

package locus.api.utils

import java.io.Closeable
import java.nio.charset.Charset

object Utils {

    // tag for logger
    private const val TAG = "Utils"

    private val NEW_LINE = System.getProperty("line.separator")

    //*************************************************
    // PARSE SECTION
    //*************************************************

    fun parseBoolean(data: Any): Boolean {
        return parseBoolean(data.toString())
    }

    fun parseBoolean(data: String): Boolean {
        return try {
            data.toLowerCase().contains("true") || data.contains("1")
        } catch (e: Exception) {
            false
        }
    }

    fun parseInt(data: Any): Int {
        return parseInt(data.toString())
    }

    /**
     * Parse integer value from text.
     *
     * @param data     text to parse
     * @param defValue default value used in case, text is not an integer
     * @return parsed value
     */
    @JvmOverloads
    fun parseInt(data: String, defValue: Int = 0): Int {
        return try {
            Integer.parseInt(data.trim { it <= ' ' })
        } catch (e: Exception) {
            defValue
        }
    }

    fun parseInteger(data: Any): Int {
        return parseInteger(data.toString())
    }

    fun parseInteger(data: String): Int {
        return try {
            Integer.valueOf(data.trim { it <= ' ' })
        } catch (e: Exception) {
            0
        }
    }

    fun parseLong(data: Any): Long {
        return parseLong(data.toString())
    }

    fun parseLong(data: String): Long {
        return try {
            java.lang.Long.parseLong(data.trim { it <= ' ' })
        } catch (e: Exception) {
            0
        }
    }

    fun parseFloat(data: Any): Float {
        return parseFloat(data.toString())
    }

    fun parseFloat(data: String): Float {
        return try {
            java.lang.Float.parseFloat(data.trim { it <= ' ' })
        } catch (e: Exception) {
            0.0f
        }
    }

    fun parseDouble(data: Any): Double {
        return parseDouble(data.toString())
    }

    fun parseDouble(data: String): Double {
        return try {
            java.lang.Double.parseDouble(
                    data.trim { it <= ' ' }.replace(",", "."))
        } catch (e: Exception) {
            0.0
        }
    }

    fun doStringToBytes(text: String): ByteArray {
        return try {
            text.toByteArray(charset("UTF-8"))
        } catch (e: Exception) {
            Logger.logE(TAG, "doStringToBytes($text)", e)
            ByteArray(0)
        }
    }

    fun doBytesToString(data: ByteArray): String {
        return try {
            String(data, Charset.forName("UTF-8"))
        } catch (e: Exception) {
            Logger.logE(TAG, "doBytesToString($data)", e)
            ""
        }
    }

    //*************************************************
    // OTHER TOOLS
    //*************************************************

    fun closeStream(stream: Closeable?) {
        try {
            stream?.close()
        } catch (e: Exception) {
            System.err.println("closeStream($stream), e:$e")
            e.printStackTrace()
        }
    }

    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    fun isEmpty(str: CharSequence?): Boolean {
        return str == null || str.isEmpty()
    }

    @JvmOverloads
    fun toString(obj: Any?, prefix: String = ""): String {
        // add base
        val result = StringBuilder()
        result.append(prefix)
        if (obj == null) {
            result.append(" empty object!")
            return result.toString()
        }

        // handle existing object
        result.append(obj.javaClass.name).append(" [").append(NEW_LINE)

        // determine fields declared in this class only (no fields of superclass)
        val fields = obj.javaClass.declaredFields

        // print field names paired with their values
        for (field in fields) {
            result.append(prefix).append("    ")
            try {
                result.append(field.name)
                result.append(": ")
                // set accessible for private fields
                field.isAccessible = true
                // requires access to private field:
                result.append(field.get(obj))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            result.append(NEW_LINE)
        }
        result.append(prefix).append("]")
        return result.toString()
    }
}
