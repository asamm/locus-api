package locus.api.android

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

import java.io.File

import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils
import locus.api.android.utils.LocusUtils.LocusVersion
import locus.api.utils.Logger

@Suppress("unused")
object ActionFiles {

    // tag for logger
    private const val TAG = "ActionFiles"

    //*************************************************
    // SENDING FILE TO SYSTEM/LOCUS
    //*************************************************

    /**
     * Generic call to the system. All applications that are capable of handling of certain file should
     * be offered to user.
     *
     * @param ctx current context
     * @param file file we wants to share
     */
    fun importFileSystem(ctx: Context, fileUri: Uri, type: String): Boolean {
        // send Intent
        ctx.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, type)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        })
        return true
    }

    /**
     * Import GPX/KML files directly into Locus application.
     *
     * @param ctx current context
     * @param lv instance of target Locus app
     * @param fileUri file we wants to share
     * @param callImport `true` to start import, otherwise content is just displayed on the map
     * @return `false` if file don't exist or Locus is not installed
     */
    @JvmOverloads
    fun importFileLocus(ctx: Context, fileUri: Uri, type: String,
            lv: LocusVersion? = LocusUtils.getActiveVersion(ctx),
            callImport: Boolean = true): Boolean {
        // check requirements
        if (lv == null) {
            Logger.logE(TAG, "importFileLocus(" + ctx + ", " + lv + ", " + fileUri + ", " + callImport + "), " +
                    "invalid input parameters. Import cannot be performed!")
            return false
        }

        // send request
        ctx.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setClassName(lv.packageName, lv.mainActivityClassName)
            setDataAndType(fileUri, type)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(LocusConst.INTENT_EXTRA_CALL_IMPORT, callImport)
        })
        return true
    }

    /**
     * Get mime type for certain file. This method created simplified version of mime-type just
     * based on file extension.
     *
     * @param file we work with
     * @return generated mime-type
     */
    fun getMimeType(file: File): String {
        val name = file.name
        val index = name.lastIndexOf(".")
        return if (index == -1) {
            "*/*"
        } else "application/" + name.substring(index + 1)
    }

    //*************************************************
    // FILE PICKER
    //*************************************************

    /**
     * Allows to call activity for File pick. You can use Locus picker for this purpose, but
     * check if Locus version 231 and above are installed **isLocusAvailable(context, 231)**!
     *
     * Call use generic `org.openintents.action.PICK_FILE` call, so not just Locus Map may respond
     * on this intent.
     *
     * Since Android 4.4, write access to received content is disabled, so use this method for
     * read-only access.
     *
     * For read-write access, use since Android 5 available `ACTION_OPEN_DOCUMENT_TREE` feature.
     *
     * @param activity    starting activity that also receive result
     * @param requestCode request code
     * @param title title visible in header of picker
     * @param filter optional file filter, list of supported endings
     * @throws ActivityNotFoundException thrown in case of missing required Locus app
     */
    @JvmOverloads
    @Throws(ActivityNotFoundException::class)
    fun actionPickFile(activity: Activity, requestCode: Int,
            title: String? = null, filter: Array<String>? = null) {
        intentPick("org.openintents.action.PICK_FILE",
                activity, requestCode, title, filter)
    }

    /**
     * Allows to call activity for Directory pick. You can use Locus picker for this purpose, but
     * check if Locus version 231 and above are installed **isLocusAvailable(context, 231)**!
     *
     * Call use generic `org.openintents.action.PICK_DIRECTORY` call, so not just Locus Map may
     * respond on this intent.
     *
     * Since Android 4.4, write access to received content is disabled, so use this method for
     * read-only access.
     *
     * For read-write access, use since Android 5 available `ACTION_OPEN_DOCUMENT_TREE` feature.
     *
     * @param activity    starting activity that also receive result
     * @param requestCode request code
     * @param title title visible in header of picker
     * @throws ActivityNotFoundException thrown in case of missing required Locus app
     */
    @JvmOverloads
    @Throws(ActivityNotFoundException::class)
    fun actionPickDir(activity: Activity, requestCode: Int, title: String? = null) {
        intentPick("org.openintents.action.PICK_DIRECTORY",
                activity, requestCode, title, null)
    }

    /**
     * Execute pick of files/directories.
     *
     * @param action action to start
     * @param act starting activity that also receive result
     * @param requestCode request code
     * @param title title visible in header of picker
     * @param filter optional file filter, list of supported endings
     * @throws ActivityNotFoundException thrown in case of missing required Locus app
     */
    @Throws(ActivityNotFoundException::class)
    private fun intentPick(action: String, act: Activity, requestCode: Int,
            title: String?, filter: Array<String>?) {
        // create intent
        val intent = Intent(action)
        if (title != null && title.isNotEmpty()) {
            intent.putExtra("org.openintents.extra.TITLE", title)
        }
        if (filter != null && filter.isNotEmpty()) {
            intent.putExtra("org.openintents.extra.FILTER", filter)
        }

        // execute request
        act.startActivityForResult(intent, requestCode)
    }
}
