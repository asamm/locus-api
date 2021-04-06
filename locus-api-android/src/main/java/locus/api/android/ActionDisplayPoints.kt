/****************************************************************************
 *
 * Created by menion on 20/02/2019.
 * Copyright (c) 2019. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/

package locus.api.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import locus.api.android.objects.LocusVersion
import locus.api.android.objects.PackPoints
import locus.api.android.objects.VersionCode
import locus.api.android.utils.LocusConst
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.Storable
import locus.api.utils.Logger
import locus.api.utils.Utils
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

object ActionDisplayPoints {

    // tag for logger
    private const val TAG = "ActionDisplayPoints"

    //*************************************************
    // ONE PACK_POINT OVER INTENT
    //*************************************************

    /**
     * Simple way how to send data over intent to Locus. Be aware that intent in Android have some size limits,
     * so for larger data, use below method [.sendPacksFile]
     *
     * @param ctx     actual [Context]
     * @param data        [PackPoints] object that should be send to Locus
     * @param extraAction extra action that should happen after display in app
     * @return `true` if action was success
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    @Throws(RequiredVersionMissingException::class)
    fun sendPack(ctx: Context, data: PackPoints, extraAction: ActionDisplayVarious.ExtraAction): Boolean {
        return sendPack(LocusConst.ACTION_DISPLAY_DATA,
                ctx, data,
                extraAction == ActionDisplayVarious.ExtraAction.IMPORT,
                extraAction == ActionDisplayVarious.ExtraAction.CENTER)
    }

    /**
     * Silent methods are useful in case, Locus is already running and you want to
     * display any data without interrupting a user. This method send data over
     * Broadcast intent
     *
     * @param ctx      actual [Context]
     * @param data         [PackPoints] object that should be send to Locus
     * @param centerOnData allows to center on send data. This parameter is ignored
     * if `callImport` is set to `true`. Suggested is value
     * `false` because unexpected centering breaks usability.
     * @return `true` if action was success
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    @Throws(RequiredVersionMissingException::class)
    fun sendPackSilent(ctx: Context, data: PackPoints, centerOnData: Boolean): Boolean {
        return sendPack(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                ctx, data, false, centerOnData)
    }

    /**
     * @param action       action to perform
     * @param ctx      actual [Context]
     * @param data         [PackPoints] object that should be send to Locus
     * @param callImport   whether import with this data should be called after Locus starts.
     * Otherwise data will be displayed as temporary objects on map.
     * @param centerOnData allow to center on displayed data. This parameter is ignored
     * if `callImport = true`. Suggested if `false` because
     * unexpected centering breaks usability.
     * @return `true` if data were correctly send
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    @Throws(RequiredVersionMissingException::class)
    private fun sendPack(action: String, ctx: Context,
            data: PackPoints, callImport: Boolean, centerOnData: Boolean): Boolean {
        return ActionDisplayVarious.sendData(action, ctx,
                Intent().apply {
                    putExtra(LocusConst.INTENT_EXTRA_POINTS_DATA, data.asBytes)
                },
                callImport, centerOnData)
    }

    //*************************************************
    // LIST OF PACK_POINTS OVER INTENT
    //*************************************************

    /**
     * Simple way how to send ArrayList<PackPoints> object over intent to Locus. Count that
     * intent in Android have some size limits so for larger data, use another method
     *
     * @param ctx actual [Context]
     * @param data    [ArrayList] of data that should be send to Locus
     * @return true if success
     * @throws RequiredVersionMissingException exception in case of missing required app version
    </PackPoints> */
    @Throws(RequiredVersionMissingException::class)
    fun sendPacks(ctx: Context, data: List<PackPoints>,
            extraAction: ActionDisplayVarious.ExtraAction): Boolean {
        return sendPacks(LocusConst.ACTION_DISPLAY_DATA,
                ctx, data, extraAction == ActionDisplayVarious.ExtraAction.IMPORT,
                extraAction == ActionDisplayVarious.ExtraAction.CENTER)
    }

    @Throws(RequiredVersionMissingException::class)
    fun sendPacksSilent(ctx: Context, data: List<PackPoints>, centerOnData: Boolean): Boolean {
        return sendPacks(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                ctx, data, false, centerOnData)
    }

    @Throws(RequiredVersionMissingException::class)
    private fun sendPacks(action: String, ctx: Context,
            data: List<PackPoints>, callImport: Boolean, centerOnData: Boolean): Boolean {
        return ActionDisplayVarious.sendData(action, ctx,
                Intent().apply {
                    putExtra(LocusConst.INTENT_EXTRA_POINTS_DATA_ARRAY, Storable.getAsBytes(data))
                },
                callImport, centerOnData)
    }

    //*************************************************
    // MORE PACK_POINTS OVER FILE
    //*************************************************

    /**
     *
     * Allow to send data to Locus, by storing a serialized version of data into a file. This
     * method can have advantage over a cursor in simplicity of implementation and also that
     * the file size is not limited as in the cursor method.
     *
     * Method support two systems.
     * 1. The old one (deprecated) that save data to file directly and share absolute path to it. In
     * this case, add-on have to have WRITE permission for the [file] and file have to on place
     * where Locus Map may access. In this case, leave [fileUri] blank.
     *
     * 2. New method over FileProvider system. Point [file] on your cache directory and set
     * generated Uri. More about this modern method in `SampleCalls.callSendMorePointsGeocacheFileMethod`
     * method where is working example.
     *
     * Be careful about the size of data. This method can cause OutOfMemory error on Locus side
     * if data are too big, because all needs to be loaded at once before process.
     *
     * @param ctx existing [Context]
     * @param data data to send
     * @param file path where data should be stored
     * @param fileUri uri from `FileProvider`, which represents filepath (optional)
     * @param extraAction extra action that should happen after Locus reads data
     * @return `true` if data were correctly send, otherwise `false`
     */
    @Throws(RequiredVersionMissingException::class)
    fun sendPacksFile(ctx: Context, lv: LocusVersion, data: List<PackPoints>,
            file: File, fileUri: Uri? = null,
            extraAction: ActionDisplayVarious.ExtraAction = ActionDisplayVarious.ExtraAction.CENTER): Boolean {
        return sendPacksFile(LocusConst.ACTION_DISPLAY_DATA,
                ctx, lv, data, file, fileUri,
                extraAction == ActionDisplayVarious.ExtraAction.IMPORT,
                extraAction == ActionDisplayVarious.ExtraAction.CENTER)
    }

    /**
     *
     * Allows to send data to Locus silently without user interaction, by storing a serialized
     * version of data into a file. This method can have advantage over a cursor in simplicity of
     * implementation and also that the file size is not limited as in the cursor method.
     *
     * Method support two systems.
     * 1. The old one (deprecated) that save data to file directly and share absolute path to it. In
     * this case, add-on have to have WRITE permission for the [file] and file have to on place
     * where Locus Map may access. In this case, leave [fileUri] blank.
     *
     * 2. New method over FileProvider system. Point [file] on your cache directory and set
     * generated Uri. More about this modern method in `SampleCalls.callSendMorePointsGeocacheFileMethod`
     * method where is working example.
     *
     * Be careful about the size of data. This method can cause OutOfMemory error on Locus side
     * if data are too big, because all needs to be loaded at once before process.
     *
     * @param ctx existing [Context]
     * @param data data to send
     * @param file path where data should be stored
     * @param fileUri uri from `FileProvider`, which represents filepath
     * @param centerOnData `true` to center on data
     * @return `true` if data were correctly send, otherwise `false`
     */
    @Throws(RequiredVersionMissingException::class)
    fun sendPacksFileSilent(ctx: Context, lv: LocusVersion, data: List<PackPoints>,
            file: File, fileUri: Uri? = null,
            centerOnData: Boolean = false): Boolean {
        return sendPacksFile(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                ctx, lv, data, file, fileUri,
                false, centerOnData)
    }

    /**
     * Main function for sending pack of points over temporary stored file.
     *
     * @param action       action we wants to perform
     * @param ctx      current ctx
     * @param data         data to send
     * @param file         path where file will be temporary stored
     * @param fileUri      uri from `FileProvider`, which represents `file`
     * @param callImport   `true` to call import after load in Locus
     * @param centerOnData `true` to center on data
     * @return `true` if request was correctly send
     */
    @Throws(RequiredVersionMissingException::class)
    private fun sendPacksFile(action: String, ctx: Context, lv: LocusVersion,
            data: List<PackPoints>, file: File, fileUri: Uri?,
            callImport: Boolean, centerOnData: Boolean): Boolean {
        // write data to storage
        val writeResult = ActionDisplayVarious.sendDataWriteOnCard(file) {
            Storable.writeList(data, this)
        }

        // send intent with reference to stored data
        return if (writeResult) {
            val intent = Intent()
            return if (fileUri != null) {
                // setup intent
                intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_URI, fileUri)

                // grant permission to app package. Because we do not send `uri` over setData, we need
                // to use this method
                ctx.grantUriPermission(lv.packageName, fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                // send request
                ActionDisplayVarious.sendData(action, ctx, intent, callImport, centerOnData, VersionCode.UPDATE_15)
            } else {
                // set path and send request
                intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH, file.absolutePath)
                ActionDisplayVarious.sendData(action, ctx, intent, callImport, centerOnData)
            }
        } else {
            false
        }
    }

    // HANDLE RECEIVED DATA

    /**
     * Invert method to [.sendPacksFile] or [.sendPacksFile]. This load serialized data from
     * a file stored in [Intent].
     *
     * @param ctx    context
     * @param intent intent data
     * @return loaded pack of points
     */
    fun readPacksFile(ctx: Context, intent: Intent): List<PackPoints> {
        return when {
            intent.hasExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_URI) -> {
                readDataFromUri(ctx, intent.getParcelableExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_URI)!!)
            }
            intent.hasExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH) -> {
                // backward compatibility
                readDataFromPath(intent.getStringExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH)!!)
            }
            else -> {
                listOf()
            }
        }
    }

    private fun readDataFromUri(ctx: Context, fileUri: Uri): List<PackPoints> {
        var dis: DataInputStream? = null
        try {
            dis = DataInputStream(ctx.contentResolver.openInputStream(fileUri))
            return Storable.readList(PackPoints::class.java, dis)
        } catch (e: Exception) {
            Logger.logE(TAG, "readDataFromUri($fileUri)", e)
        } finally {
            Utils.closeStream(dis)
        }
        return ArrayList()
    }

    private fun readDataFromPath(filepath: String): List<PackPoints> {
        // check file
        val file = File(filepath)
        if (!file.exists() || !file.isFile) {
            return ArrayList()
        }

        var dis: DataInputStream? = null
        try {
            dis = DataInputStream(FileInputStream(file))
            return Storable.readList(PackPoints::class.java, dis)
        } catch (e: Exception) {
            Logger.logE(TAG, "readDataFromPath($filepath)", e)
        } finally {
            Utils.closeStream(dis)
        }
        return ArrayList()
    }

    /**
     * Allows to remove already send Pack from the map. Keep in mind, that this method remove
     * only packs that are visible (temporary) on map.
     *
     * @param ctx      current context
     * @param packName name of pack
     */
    @Throws(RequiredVersionMissingException::class)
    fun removePackFromLocus(ctx: Context, packName: String?) {
        // check data
        if (packName == null || packName.isEmpty()) {
            return
        }

        // create empty pack
        val pw = PackPoints(packName)

        // create and send intent
        val intent = Intent()
        intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_DATA, pw.asBytes)
        sendPackSilent(ctx, pw, false)
    }
}