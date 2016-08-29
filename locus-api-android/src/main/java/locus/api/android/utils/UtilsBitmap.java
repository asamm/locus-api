package locus.api.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;

/**
 * Created by menion on 09/10/2015.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
public class UtilsBitmap {

    // tag for logger
    private static final String TAG = "UtilsBitmap";

    public static Bitmap readBitmap(DataReaderBigEndian dr) {
        int size = 0;
        if ((size = dr.readInt()) > 0) {
            byte[] data = dr.readBytes(size);
            return getBitmap(data);
        } else {
            return null;
        }
    }

    public static void writeBitmap(DataWriterBigEndian dw, Bitmap bitmap,
                                   Bitmap.CompressFormat format) throws IOException {
        if (bitmap == null) {
            dw.writeInt(0);
        } else {
            byte[] data = getBitmap(bitmap, format);
            if (data == null || data.length == 0) {
                Logger.logW(TAG, "writeBitmap(), unknown problem");
                dw.writeInt(0);
            } else {
                dw.writeInt(data.length);
                dw.write(data);
            }
        }
    }

    public static byte[] getBitmap(Bitmap bitmap, Bitmap.CompressFormat format) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            // create compressed byte array. Value 80 is used for JPEG. PNG format ignore this value
            if (bitmap.compress(format, 80, baos)) {
                return baos.toByteArray();
            } else {
                Logger.logW(TAG, "Problem with converting image to byte[]");
                return null;
            }
        } catch (Exception e) {
            Logger.logE(TAG, "getBitmap(" + bitmap + ")", e);
            return null;
        } finally {
            locus.api.utils.Utils.closeStream(baos);
        }
    }

    public static Bitmap getBitmap(byte[] data) {
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            Logger.logE(TAG, "getBitmap(" + data + ")", e);
            return null;
        }
    }
}
