/****************************************************************************
 *
 * Created by menion on 07.04.2021.
 * Copyright (c) 2021. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/
package locus.api.android.features.sendToApp.points

import android.content.Context
import android.content.Intent
import locus.api.android.features.sendToApp.SendToAppHelper
import locus.api.android.objects.PackPoints
import locus.api.android.utils.LocusConst

/**
 * Base class for sending point(s) to Locus apps.
 *
 * Do not use directly but instead use it's extensions. Class is public because of Kotlin
 * limitations in inheritance.
 */
class SendPointBase {

    companion object {

        // tag for logger
        private const val TAG = "SentPointBase"

        // RECEIVE DATA

        /**
         * Invert method to [.sendPacksFile] or [.sendPacksFile]. This load serialized data from
         * a file stored in [Intent].
         *
         * @param ctx    context
         * @param intent intent data
         * @return loaded pack of points
         */
        fun readPointsFile(ctx: Context, intent: Intent): List<PackPoints> {
            return when {
                intent.hasExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_URI) -> {
                    SendToAppHelper.readDataFromUri(ctx,
                            intent.getParcelableExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_URI)!!)
                }
                intent.hasExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH) -> {
                    // backward compatibility
                    SendToAppHelper.readDataFromPath(
                            intent.getStringExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH)!!)
                }
                else -> {
                    listOf()
                }
            }
        }
    }
}