/****************************************************************************
 *
 * Created by menion on 06.04.2021.
 * Copyright (c) 2021. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/
package locus.api.android.features.sendToApp

import android.content.Context
import android.net.Uri
import com.asamm.loggerV2.logE
import locus.api.objects.Storable
import locus.api.utils.Utils
import java.io.*

object SendToAppHelper {

    // tag for logger
    private const val TAG = "SendToAppHelper"

    //*************************************************
    // FILESYSTEM
    //*************************************************

    // SEND DATA

    /**
     * Get cache directory used for sending content to Locus app.
     *
     * @param ctx current context
     * @param filename own name of the file or auto-generated
     */
    fun getCacheFile(
        ctx: Context,
        filename: String = "${System.currentTimeMillis()}.lb"
    ): File {
        // get filepath
        val dir = File(ctx.cacheDir, "shared")
        dir.mkdirs()

        // return generated file
        return File(dir, filename)
    }

    internal fun sendDataWriteOnCard(file: File, writer: DataOutputStream.() -> Unit): Boolean {
        // prepare output
        var dos: DataOutputStream? = null
        try {
            file.parentFile!!.mkdirs()

            // delete previous file
            if (file.exists()) {
                file.delete()
            }

            // create stream
            dos = DataOutputStream(FileOutputStream(file, false))

            // write current version
            writer(dos)
            dos.flush()
            return true
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "sendDataWriteOnCard($file, $writer)" }
            return false
        } finally {
            Utils.closeStream(dos)
        }
    }

    // RECEIVE DATA

    /**
     * Read data stored in certain path. This method is deprecated and should not be used. Instead
     * use new method over FileUri system.
     */
    @Deprecated(message = "Use system over FileUri")
    internal inline fun <reified T : Storable> readDataFromPath(filepath: String): List<T> {
        // check file
        val file = File(filepath)
        if (!file.exists() || !file.isFile) {
            return ArrayList()
        }

        var dis: DataInputStream? = null
        try {
            dis = DataInputStream(FileInputStream(file))
            return Storable.readList(T::class.java, dis)
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "readDataFromPath($filepath)" }
        } finally {
            Utils.closeStream(dis)
        }
        return ArrayList()
    }

    /**
     * Read data from the supplied [fileUri] source.
     */
    internal inline fun <reified T : Storable> readDataFromUri(
        ctx: Context,
        fileUri: Uri
    ): List<T> {
        var dis: DataInputStream? = null
        try {
            dis = DataInputStream(ctx.contentResolver.openInputStream(fileUri))
            return Storable.readList(T::class.java, dis)
        } catch (e: Exception) {
            logE(tag = TAG, ex = e) { "readDataFromUri($fileUri)" }
        } finally {
            Utils.closeStream(dis)
        }
        return listOf()
    }
}
