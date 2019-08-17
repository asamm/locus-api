/**
 * Created by menion on 4. 7. 2014.
 * Class is part of Locus project
 */
package locus.api.android.utils

import android.database.Cursor

import locus.api.utils.Logger

object UtilsAnd {

    private const val TAG = "UtilsAnd"

    fun closeQuietly(cursor: Cursor?) {
        try {
            cursor?.close()
        } catch (e: Exception) {
            Logger.logE(TAG, "closeQuietly($cursor), e: $e")
            e.printStackTrace()
        }
    }
}
