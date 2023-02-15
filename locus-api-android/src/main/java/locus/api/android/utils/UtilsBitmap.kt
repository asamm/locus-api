/**
 * Created by menion on 09/10/2015.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.asamm.loggerV2.logE
import com.asamm.loggerV2.logW
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.ByteArrayOutputStream

object UtilsBitmap {

    // tag for logger
    private const val TAG = "UtilsBitmap"

    fun readBitmap(dr: DataReaderBigEndian): Bitmap? {
        val size: Int = dr.readInt()
        return if (size > 0) {
            getBitmap(dr.readBytes(size))
        } else {
            null
        }
    }

    fun writeBitmap(
        dw: DataWriterBigEndian, bitmap: Bitmap?,
        format: Bitmap.CompressFormat
    ) {
        if (bitmap == null) {
            dw.writeInt(0)
        } else {
            val data = getBitmap(bitmap, format)
            if (data == null || data.isEmpty()) {
                logW(tag = TAG) { "writeBitmap(), unknown problem" }
                dw.writeInt(0)
            } else {
                dw.writeInt(data.size)
                dw.write(data)
            }
        }
    }

    fun getBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat): ByteArray? {
        var baos: ByteArrayOutputStream? = null
        return try {
            baos = ByteArrayOutputStream()
            // create compressed byte array. Value 80 is used for JPEG. PNG format ignore this value
            if (bitmap.compress(format, 80, baos)) {
                baos.toByteArray()
            } else {
                logW(tag = TAG) { "Problem with converting image to byte[]" }
                null
            }
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "getBitmap($bitmap)" }
            null
        } finally {
            locus.api.utils.Utils.closeStream(baos)
        }
    }

    fun getBitmap(data: ByteArray): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(data, 0, data.size)
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "getBitmap($data)" }
            null
        }
    }
}
