package locus.api.android.utils;

import android.database.Cursor;

import locus.api.utils.Logger;

/**
 * Created by menion on 4. 7. 2014.
 * Class is part of Locus project
 */
public class Utils extends locus.api.utils.Utils {

    private static final String TAG = Utils.class.getSimpleName();

    // CLOSE HELPERS

    public static void closeQuietly(Cursor cursor) {
        try {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        } catch (Exception e) {
            Logger.logE(TAG, "closeQuietly(" + cursor + "), e:" + e);
            e.printStackTrace();
        }
    }
}
