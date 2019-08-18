/**
 * Created by menion on 4. 7. 2014.
 * Class is part of Locus project
 */
package locus.api.android.utils

import android.database.Cursor

import locus.api.utils.Logger

/**
 * Close opened cursor quietly (it means, no report in case of problem).
 */
fun Cursor.closeQuietly() {
    try {
        close()
    } catch (e: Exception) {
        Logger.logE("Utils", "closeQuietly(), e: $e")
        e.printStackTrace()
    }
}