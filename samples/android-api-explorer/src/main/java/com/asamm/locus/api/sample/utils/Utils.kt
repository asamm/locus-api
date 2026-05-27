/****************************************************************************
 *
 * Created by menion on 15/03/2019.
 * Copyright (c) 2019. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/

package com.asamm.locus.api.sample.utils

import android.content.Context
import android.net.Uri
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Class with small helping utils.
 */
object Utils {

    /**
     * Copy content from defined Uri into file object.
     *
     * @param ctx current context
     * @param source source received Uri
     * @param dst destination file
     */
    @Throws(IOException::class)
    fun copy(ctx: Context, source: Uri, dst: File) {
        DataInputStream(ctx.contentResolver.openInputStream(source)).use { inStream ->
            FileOutputStream(dst).use { out ->
                val buf = ByteArray(10240)
                while (true) {
                    val len = inStream.read(buf)
                    if (len > 0) {
                        out.write(buf, 0, len)
                    } else {
                        break
                    }
                }
            }
        }
    }

}