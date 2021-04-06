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
import java.io.File

object SendToAppHelper {

    /**
     * Get cache directory used for sending content to Locus app.
     *
     * @param ctx current context
     * @param filename own name of the file or auto-generated
     */
    fun getCacheFile(ctx: Context,
            filename: String = "${System.currentTimeMillis()}.lb"): File {
        // get filepath
        val dir = File(ctx.cacheDir, "shared")
        dir.mkdirs()

        // return generated file
        return File(dir, filename)
    }
}