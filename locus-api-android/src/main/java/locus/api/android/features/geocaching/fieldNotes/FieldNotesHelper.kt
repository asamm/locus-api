package locus.api.android.features.geocaching.fieldNotes

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import locus.api.android.ActionTools
import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils
import locus.api.android.utils.Utils
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.geocaching.GeocachingLog
import java.util.*

/**
 * Created by menion on 8. 7. 2014.
 * Class is part of Locus project
 *
 * Helper class for work with Geocaching Field notes content provider.
 *
 * For access to this provider are required read/write permissions.
 *
 * Read: com.asamm.locus.permission.READ_GEOCACHING_DATA
 * Write: com.asamm.locus.permission.WRITE_GEOCACHING_DATA
 */
class FieldNotesHelper private constructor() {

    object ColFieldNote {

        const val ID = "_id"
        const val CACHE_CODE = "cache_code"
        const val CACHE_NAME = "cache_name"
        const val TYPE = "type"
        const val TIME = "time"
        const val NOTE = "note"
        const val FAVORITE = "favorite"
        const val LOGGED = "logged"
    }

    object ColFieldNoteImage {

        const val ID = "_id"
        const val FIELD_NOTE_ID = "field_note_id"
        const val CAPTION = "caption"
        const val DESCRIPTION = "description"
        const val DATA = "data"
    }

    object ColTrackableLogs {

        const val ID = "_id"
        const val TB_CODE = "tb_code"
        const val NAME = "name"
        const val ICON = "icon"
        const val CACHE_CODE = "cache_code"
        const val ACTION = "action"
        const val TRACKING_CODE = "tracking_code"
        const val TIME = "time"
        const val NOTE = "note"
        const val LOGGED = "logged"
    }

    companion object {

        // paths to content provider
        const val PATH_FIELD_NOTES = "fieldNotes"
        const val PATH_FIELD_NOTE_IMAGES = "fieldNoteImages"
        const val PATH_TRACKABLE_LOGS = "trackableLogs"

        /**************************************************/
        // HELPERS FOR WORK WITH CONTENT PROVIDER
        /**************************************************/

        // GET

        /**
         * Compute count of existing field notes.
         * @param ctx existing context
         * @param lv active Locus version
         * @return number of existing field notes
         */
        @Throws(RequiredVersionMissingException::class)
        fun getCount(ctx: Context, lv: LocusUtils.LocusVersion): Int {
            // execute request
            var c: Cursor? = null
            try {
                c = ctx.contentResolver.query(getUriLogsTable(lv),
                        arrayOf(ColFieldNote.ID), null, null, null)
                return if (c == null) {
                    0
                } else {
                    c.count
                }
            } finally {
                Utils.closeQuietly(c)
            }
        }

        /**
         * Get List of all available field notes in Locus
         * @param ctx existing context
         * @param lv active Locus version
         * @return List of all field notes
         */
        @Throws(RequiredVersionMissingException::class)
        fun getAll(ctx: Context, lv: LocusUtils.LocusVersion): MutableList<FieldNote> {
            return get(ctx, lv, "")
        }

        /**
         * Get single specific Field Note defined by it's [id]. This also load images (only it's
         * ID parameters).
         */
        @Throws(RequiredVersionMissingException::class)
        fun get(ctx: Context, lv: LocusUtils.LocusVersion, id: Long): FieldNote? {
            // get parameters for query
            var cpUri = getUriLogsTable(lv)
            cpUri = ContentUris.withAppendedId(cpUri, id)

            // execute request
            var c: Cursor? = null
            try {
                c = ctx.contentResolver.query(cpUri, null, null, null, null)
                if (c == null || c.count != 1) {
                    return null
                }

                // get 'all' notes and return first
                val fn = createLogs(c)[0]

                // get extra data
                getImages(ctx, lv, fn)

                // return field note
                return fn
            } finally {
                Utils.closeQuietly(c)
            }
        }

        /**
         * Request on specific field notes for certain cache (define by it's cache code)
         * @param ctx existing context
         * @param lv active Locus version
         * @param cacheCode code of cache for which we want field notes
         * @return list of all field notes for certain cache
         */
        @Throws(RequiredVersionMissingException::class)
        fun get(ctx: Context, lv: LocusUtils.LocusVersion, cacheCode: String?): MutableList<FieldNote> {
            // execute request
            var c: Cursor? = null
            try {
                // perform request based on 'cacheCode'
                c = if (cacheCode == null || cacheCode.isEmpty()) {
                    ctx.contentResolver.query(getUriLogsTable(lv),
                            null, null, null, null)
                } else {
                    ctx.contentResolver.query(getUriLogsTable(lv),
                            null, ColFieldNote.CACHE_CODE + "=?",
                            arrayOf(cacheCode), null)
                }

                // handle result
                if (c == null) {
                    return arrayListOf()
                }

                // load logs & images
                val logs = createLogs(c)
                for (log in logs) {
                    getImages(ctx, lv, log)
                }
                return logs
            } finally {
                Utils.closeQuietly(c)
            }
        }

        /**
         * Get last logged "found" field note from database. Returned field not will also
         * contain all images and logged items.
         */
        fun getLastFoundLog(ctx: Context, lv: LocusUtils.LocusVersion): FieldNote? {
            // execute request
            var c: Cursor? = null
            try {
                // perform request based on 'cacheCode'. Type of logs needs to match to function
                // GcLoggingActivity.isLogTrackablesSupported() in Locus Core
                c = ctx.contentResolver.query(getUriLogsTable(lv),
                        null, ColFieldNote.TYPE + " IN (?,?,?)",
                        arrayOf(GeocachingLog.CACHE_LOG_TYPE_ATTENDED.toString(),
                                GeocachingLog.CACHE_LOG_TYPE_FOUND.toString(),
                                GeocachingLog.CACHE_LOG_TYPE_WRITE_NOTE.toString()),
                        ColFieldNote.ID + " DESC LIMIT 1")

                // handle result
                if (c.moveToNext()) {
                    val logs = createLogs(c)
                    if (logs.isNotEmpty()) {
                        return logs[0]
                    }
                }
                return null
            } finally {
                Utils.closeQuietly(c)
            }
        }

        // DELETE

        /**
         * Delete field note specified by it's ID
         * @param ctx existing context
         * @param lv active Locus version
         * @param fieldNoteId ID of field note to delete
         * @return `true` if deleted successfully, otherwise `false`
         */
        @Throws(RequiredVersionMissingException::class)
        fun delete(ctx: Context, lv: LocusUtils.LocusVersion, fieldNoteId: Long): Boolean {
            // execute request
            val res = ctx.contentResolver.delete(getUriLogsTable(lv),
                    ColFieldNote.ID + "=?",
                    arrayOf(java.lang.Long.toString(fieldNoteId))
            )

            // delete images
            deleteImages(ctx, lv, fieldNoteId)

            // return result
            return res == 1
        }

        /**
         * Delete all field notes in Locus
         * @param ctx existing context
         * @param lv active Locus version
         * @return number of deleted field notes
         */
        @Throws(RequiredVersionMissingException::class)
        fun deleteAll(ctx: Context, lv: LocusUtils.LocusVersion): Int {
            // execute request (logs and also images)
            val count = ctx.contentResolver.delete(getUriLogsTable(lv), null, null)
            deleteImagesAll(ctx, lv)
            return count
        }

        // INSERT

        /**
         * Simple insert of one field note
         * @param ctx existing context
         * @param lv active Locus version
         * @param gcFn field note that should be inserted.
         * @return `true` if insert was successful, otherwise false
         */
        @Throws(RequiredVersionMissingException::class)
        fun insert(ctx: Context, lv: LocusUtils.LocusVersion,
                   gcFn: FieldNote): Boolean {
            // createLogs data container
            val cv = createContentValues(gcFn)

            // execute request
            val newRow = ctx.contentResolver.insert(getUriLogsTable(lv), cv) ?: return false

            // set new ID to field note
            gcFn.id = Utils.parseLong(newRow.lastPathSegment)

            // insert extra data
            storeAllImages(ctx, lv, gcFn)

            // return result
            return true
        }

        // UPDATE

        /**
         * Update single field note define by it's ID
         * @param ctx existing context
         * @param lv active Locus version
         * @param gcFn field note, that should be updated
         * @return `true` if update was successful, otherwise false
         */
        @Throws(RequiredVersionMissingException::class)
        fun update(ctx: Context, lv: LocusUtils.LocusVersion, gcFn: FieldNote): Boolean {
            // createLogs data container
            val cv = createContentValues(gcFn)

            // execute request
            return if (update(ctx, lv, gcFn, cv)) {
                // update extra data
                storeAllImages(ctx, lv, gcFn)

                // all went well
                true
            } else {
                false
            }
        }

        @Throws(RequiredVersionMissingException::class)
        fun update(ctx: Context, lv: LocusUtils.LocusVersion,
                   fn: FieldNote, cv: ContentValues): Boolean {
            // execute request
            val newRow = ctx.contentResolver.update(getUriLogsTable(lv), cv,
                    ColFieldNote.ID + "=?",
                    arrayOf(fn.id.toString()))
            return newRow == 1
        }

        /**************************************************/
        // IMAGE HANDLERS
        /**************************************************/

        @Throws(RequiredVersionMissingException::class)
        private fun storeAllImages(ctx: Context, lv: LocusUtils.LocusVersion, fn: FieldNote) {
            for (image in fn.images) {
                image.fieldNoteId = fn.id

                // update or insert image
                if (image.id >= 0) {
                    updateImage(ctx, lv, image)
                } else {
                    insertImage(ctx, lv, image)
                }
            }
        }

        // GET

        @Throws(RequiredVersionMissingException::class)
        fun getImage(ctx: Context, lv: LocusUtils.LocusVersion, imgId: Long): FieldNoteImage? {
            // execute request
            var c: Cursor? = null
            try {
                c = ctx.contentResolver.query(
                        ContentUris.withAppendedId(getUriImagesTable(lv), imgId),
                        null, null, null, null)
                return if (c == null || c.count != 1) {
                    null
                } else createImages(c)[0]
            } finally {
                Utils.closeQuietly(c)
            }
        }

        /**
         * Get attached images from database and add it to FieldNote object. Keep in mind,
         * that this function only grab images ID's, not whole content. So for later use,
         * you need to request specific image by 'getImage' function with it's ID.
         * @param ctx existing context
         * @param lv active Locus version
         * @param fn Field Note for which we wants images
         * @throws RequiredVersionMissingException
         */
        @Throws(RequiredVersionMissingException::class)
        private fun getImages(ctx: Context, lv: LocusUtils.LocusVersion, fn: FieldNote) {
            var c: Cursor? = null
            try {
                // perform request based on 'cacheCode'
                c = ctx.contentResolver.query(getUriImagesTable(lv),
                        arrayOf(ColFieldNoteImage.ID),
                        ColFieldNoteImage.FIELD_NOTE_ID + "=?",
                        arrayOf(fn.id.toString()), null)

                // handle result
                if (c != null) {
                    val images = createImages(c)
                    for (image in images) {
                        fn.images.add(image)
                    }
                }
            } finally {
                Utils.closeQuietly(c)
            }
        }

        // DELETE

        @Throws(RequiredVersionMissingException::class)
        private fun deleteImages(ctx: Context, lv: LocusUtils.LocusVersion, fieldNoteId: Long) {
            ctx.contentResolver.delete(
                    getUriImagesTable(lv),
                    ColFieldNoteImage.FIELD_NOTE_ID + "=?",
                    arrayOf(fieldNoteId.toString()))
        }

        @Throws(RequiredVersionMissingException::class)
        private fun deleteImagesAll(ctx: Context, lv: LocusUtils.LocusVersion) {
            ctx.contentResolver.delete(
                    getUriImagesTable(lv), null, null)
        }

        // UPDATE

        @Throws(RequiredVersionMissingException::class)
        fun updateImage(ctx: Context, lv: LocusUtils.LocusVersion, img: FieldNoteImage): Boolean {
            return ctx.contentResolver.update(
                    getUriImagesTable(lv),
                    createContentValues(img, false),
                    ColFieldNoteImage.ID + "=?",
                    arrayOf(img.id.toString())) == 1
        }

        // INSERT

        @Throws(RequiredVersionMissingException::class)
        private fun insertImage(ctx: Context, lv: LocusUtils.LocusVersion, img: FieldNoteImage): Boolean {
            return ctx.contentResolver.insert(
                    getUriImagesTable(lv),
                    createContentValues(img, true)) != null
        }

        /**************************************************/
        // ITEMS HANDLERS
        /**************************************************/

//        @Throws(RequiredVersionMissingException::class)
//        private fun storeAllItems(ctx: Context, lv: LocusUtils.LocusVersion, items: List<TrackableLog>) {
//            // update all items
//            for (item in items) {
//                if (item.id >= 0) {
//                    updateItem(ctx, lv, item)
//                } else {
//                    insertItem(ctx, lv, item)
//                }
//            }
//        }

        // GET

        /**
         * Get all existing trackable logs for defined cache by it's [cacheCode].
         */
        @Throws(RequiredVersionMissingException::class)
        fun getTrackableLogs(ctx: Context, lv: LocusUtils.LocusVersion, cacheCode: String)
                : MutableList<TrackableLog> {
            val items = arrayListOf<TrackableLog>()
            ctx.contentResolver.query(getUriTrackablesLogsTable(lv),
                    null,
                    ColTrackableLogs.CACHE_CODE + "=?",
                    arrayOf(cacheCode), null)?.apply {
                items.addAll(createItems(this))
                Utils.closeQuietly(this)
            }

            // return container
            return items
        }

        /**
         * Get all existing trackable logs that was not yet correctly logged.
         */
        @Throws(RequiredVersionMissingException::class)
        fun getTrackablesLogsNotLogged(ctx: Context, lv: LocusUtils.LocusVersion)
                : MutableList<TrackableLog> {
            val items = arrayListOf<TrackableLog>()
            ctx.contentResolver.query(getUriTrackablesLogsTable(lv),
                    null,
                    ColTrackableLogs.LOGGED + "=?",
                    arrayOf("0"), null)?.apply {
                items.addAll(createItems(this))
                Utils.closeQuietly(this)
            }

            // return container
            return items
        }

        // INSERT

        @Throws(RequiredVersionMissingException::class)
        fun insertTrackableLog(ctx: Context, lv: LocusUtils.LocusVersion, item: TrackableLog): Boolean {
            return ctx.contentResolver.insert(
                    getUriTrackablesLogsTable(lv),
                    createContentValues(item)) != null
        }

        // UPDATE

        @Throws(RequiredVersionMissingException::class)
        fun updateTrackableLog(ctx: Context, lv: LocusUtils.LocusVersion, item: TrackableLog): Boolean {
            return ctx.contentResolver.update(
                    getUriTrackablesLogsTable(lv), createContentValues(item),
                    ColTrackableLogs.ID + "=?",
                    arrayOf(item.id.toString())) == 1
        }

        // DELETE

        @Throws(RequiredVersionMissingException::class)
        fun deleteTrackableLog(ctx: Context, lv: LocusUtils.LocusVersion, itemId: Long): Boolean {
            return ctx.contentResolver.delete(getUriTrackablesLogsTable(lv),
                    "${ColTrackableLogs.ID}=?",
                    arrayOf(itemId.toString())) == 1
        }

        /**********************************************/
        // HELP FUNCTIONS
        /**********************************************/

        /**
         * Create valid Uri to base logs provider.
         */
        @Throws(RequiredVersionMissingException::class)
        private fun getUriLogsTable(lv: LocusUtils.LocusVersion): Uri {
            return ActionTools.getProviderUrlGeocaching(lv,
                    LocusUtils.VersionCode.UPDATE_05, PATH_FIELD_NOTES)
        }

        /**
         * Create valid Uri to provider of images.
         */
        @Throws(RequiredVersionMissingException::class)
        private fun getUriImagesTable(lv: LocusUtils.LocusVersion): Uri {
            return ActionTools.getProviderUrlGeocaching(lv,
                    LocusUtils.VersionCode.UPDATE_05, PATH_FIELD_NOTE_IMAGES)
        }

        /**
         * Create valid Uri to provider of items.
         */
        @Throws(RequiredVersionMissingException::class)
        private fun getUriTrackablesLogsTable(lv: LocusUtils.LocusVersion): Uri {
            return ActionTools.getProviderUrlGeocaching(lv,
                    LocusUtils.VersionCode.UPDATE_05, PATH_TRACKABLE_LOGS)
        }

        // CREATE CONTENT VALUES

        /**
         * Create ContentValues object for [fn], that is used for updates and inserts of basic logs.
         */
        private fun createContentValues(fn: FieldNote?): ContentValues {
            // check parameters
            if (fn == null) {
                throw IllegalArgumentException("Field note cannot be 'null'")
            }

            // createLogs container
            val cv = ContentValues()
            cv.put(ColFieldNote.CACHE_CODE, fn.cacheCode)
            cv.put(ColFieldNote.CACHE_NAME, fn.cacheName)
            cv.put(ColFieldNote.TYPE, fn.type)
            cv.put(ColFieldNote.TIME, fn.time)
            cv.put(ColFieldNote.NOTE, fn.note)
            cv.put(ColFieldNote.FAVORITE, fn.isFavorite)
            cv.put(ColFieldNote.LOGGED, fn.isLogged)

            // return result
            return cv
        }

        /**
         * Create ContentValues object for [img], that is used for updates and inserts of images.
         * Optionally also define to store [alsoData].
         */
        private fun createContentValues(img: FieldNoteImage?, alsoData: Boolean): ContentValues {
            // check parameters
            if (img == null || alsoData && img.image == null) {
                throw IllegalArgumentException("Field note image cannot be 'null'")
            }

            // createLogs container
            val cv = ContentValues()
            cv.put(ColFieldNoteImage.FIELD_NOTE_ID, img.fieldNoteId)
            cv.put(ColFieldNoteImage.CAPTION, img.caption)
            cv.put(ColFieldNoteImage.DESCRIPTION, img.description)
            if (alsoData) {
                cv.put(ColFieldNoteImage.DATA, img.image)
            }

            // return result
            return cv
        }

        /**
         * Create ContentValues object for [item], that is used for updates and inserts of items.
         */
        private fun createContentValues(item: TrackableLog): ContentValues {
            return ContentValues().apply {
                put(ColTrackableLogs.TB_CODE, item.tbCode)
                put(ColTrackableLogs.NAME, item.name)
                put(ColTrackableLogs.ICON, item.icon)
                put(ColTrackableLogs.CACHE_CODE, item.cacheCode)
                put(ColTrackableLogs.ACTION, item.action)
                put(ColTrackableLogs.TRACKING_CODE, item.trackingCode)
                put(ColTrackableLogs.TIME, item.time)
                put(ColTrackableLogs.NOTE, item.note)
                put(ColTrackableLogs.LOGGED, item.isLogged)
            }
        }

        // CREATING OBJECTS FROM CURSOR

        /**
         * Create list of logs from [cursor].
         */
        private fun createLogs(cursor: Cursor?): MutableList<FieldNote> {
            // createLogs container and check data
            val res = ArrayList<FieldNote>()
            if (cursor == null) {
                throw IllegalArgumentException("Cursor cannot be 'null'")
            }

            // iterate over cursor
            for (i in 0 until cursor.count) {
                cursor.moveToPosition(i)

                // add field note to container
                res.add(FieldNote().apply {
                    // set parameters (required)
                    id = cursor.getLong(
                            cursor.getColumnIndexOrThrow(ColFieldNote.ID))
                    cacheCode = cursor.getString(
                            cursor.getColumnIndexOrThrow(ColFieldNote.CACHE_CODE))
                    cacheName = cursor.getString(
                            cursor.getColumnIndexOrThrow(ColFieldNote.CACHE_NAME))
                    type = cursor.getInt(
                            cursor.getColumnIndexOrThrow(ColFieldNote.TYPE))
                    time = cursor.getLong(
                            cursor.getColumnIndex(ColFieldNote.TIME))

                    // set parameters (optional)
                    cursor.getColumnIndex(ColFieldNote.NOTE)
                            .takeIf { it >= 0 }
                            ?.let { note = cursor.getString(it) }
                    cursor.getColumnIndex(ColFieldNote.FAVORITE)
                            .takeIf { it >= 0 }
                            ?.let { isFavorite = cursor.getInt(it) == 1 }
                    cursor.getColumnIndex(ColFieldNote.LOGGED)
                            .takeIf { it >= 0 }
                            ?.let { isLogged = cursor.getInt(it) == 1 }
                })
            }

            // return result
            return res
        }

        /**
         * Create list of images from [cursor].
         */
        private fun createImages(cursor: Cursor?): MutableList<FieldNoteImage> {
            // createLogs container and check data
            val res = ArrayList<FieldNoteImage>()
            if (cursor == null) {
                throw IllegalArgumentException("Cursor cannot be 'null'")
            }

            // iterate over cursor
            for (i in 0 until cursor.count) {
                cursor.moveToPosition(i)

                // add field note to container
                res.add(FieldNoteImage().apply {
                    // set parameters (required)
                    id = cursor.getLong(
                            cursor.getColumnIndexOrThrow(ColFieldNoteImage.ID))

                    // set parameters (optional)
                    cursor.getColumnIndex(ColFieldNoteImage.FIELD_NOTE_ID)
                            .takeIf { it >= 0 }
                            ?.let { fieldNoteId = cursor.getLong(it) }
                    cursor.getColumnIndex(ColFieldNoteImage.CAPTION)
                            .takeIf { it >= 0 }
                            ?.let { caption = cursor.getString(it) }
                    cursor.getColumnIndex(ColFieldNoteImage.DESCRIPTION)
                            .takeIf { it >= 0 }
                            ?.let { description = cursor.getString(it) }
                    cursor.getColumnIndex(ColFieldNoteImage.DATA)
                            .takeIf { it >= 0 }
                            ?.let { image = cursor.getBlob(it) }
                })
            }

            // return result
            return res
        }

        /**
         * Create list of items from [cursor].
         */
        private fun createItems(cursor: Cursor?): MutableList<TrackableLog> {
            // createLogs container and check data
            val res = ArrayList<TrackableLog>()
            if (cursor == null) {
                throw IllegalArgumentException("Cursor cannot be 'null'")
            }

            // iterate over cursor
            for (i in 0 until cursor.count) {
                cursor.moveToPosition(i)

                // createLogs object
                res.add(TrackableLog().apply {
                    id = cursor.getLong(
                            cursor.getColumnIndexOrThrow(ColTrackableLogs.ID))

                    // set parameters (optional)
                    cursor.getColumnIndex(ColTrackableLogs.TB_CODE)
                            .takeIf { it >= 0 }
                            ?.let { tbCode = cursor.getString(it) }
                    cursor.getColumnIndex(ColTrackableLogs.NAME)
                            .takeIf { it >= 0 }
                            ?.let { name = cursor.getString(it) }
                    cursor.getColumnIndex(ColTrackableLogs.ICON)
                            .takeIf { it >= 0 }
                            ?.let { icon = cursor.getString(it) }
                    cursor.getColumnIndex(ColTrackableLogs.CACHE_CODE)
                            .takeIf { it >= 0 }
                            ?.let { cacheCode = cursor.getString(it) }
                    cursor.getColumnIndex(ColTrackableLogs.ACTION)
                            .takeIf { it >= 0 }
                            ?.let { action = cursor.getInt(it) }
                    cursor.getColumnIndex(ColTrackableLogs.TRACKING_CODE)
                            .takeIf { it >= 0 }
                            ?.let { trackingCode = cursor.getString(it) }
                    cursor.getColumnIndex(ColTrackableLogs.TIME)
                            .takeIf { it >= 0 }
                            ?.let { time = cursor.getLong(it) }
                    cursor.getColumnIndex(ColTrackableLogs.NOTE)
                            .takeIf { it >= 0 }
                            ?.let { note = cursor.getString(it) }
                    cursor.getColumnIndex(ColTrackableLogs.LOGGED)
                            .takeIf { it >= 0 }
                            ?.let { isLogged = cursor.getInt(it) == 1 }
                })
            }

            // return result
            return res
        }

        /**********************************************/
        // SPECIAL ACTIONS
        /**********************************************/

        /**
         * Log list of defined field notes online on Geocaching server.
         *
         * @param ctx existing context
         * @param lv active Locus version
         * @param ids list of FieldNote id's that should be logged
         * @param createLog `true` if we wants to createLogs a log directly, or
         * `false` if note should be posted into FieldNotes section
         * @throws RequiredVersionMissingException error in case, LocusVersion is not valid
         */
        @Throws(RequiredVersionMissingException::class)
        fun logOnline(ctx: Context?, lv: LocusUtils.LocusVersion?,
                      ids: LongArray?, createLog: Boolean) {
            // check parameters
            if (ctx == null || lv == null || ids == null || ids.isEmpty()) {
                throw IllegalArgumentException("logOnline(" + ctx + ", " + lv + ", " + ids + "), " +
                        "invalid parameters")
            }

            // check version
            if (!lv.isVersionValid(LocusUtils.VersionCode.UPDATE_05)) {
                throw RequiredVersionMissingException(LocusUtils.VersionCode.UPDATE_05)
            }

            // execute request
            val intent = Intent(LocusConst.ACTION_LOG_FIELD_NOTES)
            intent.putExtra(LocusConst.INTENT_EXTRA_FIELD_NOTES_IDS, ids)
            intent.putExtra(LocusConst.INTENT_EXTRA_FIELD_NOTES_CREATE_LOG, createLog)
            ctx.startActivity(intent)
        }
    }
}
