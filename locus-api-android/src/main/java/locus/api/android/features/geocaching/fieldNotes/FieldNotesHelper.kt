package locus.api.android.features.geocaching.fieldNotes

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri

import java.util.ArrayList

import locus.api.android.ActionTools
import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils
import locus.api.android.utils.Utils
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.utils.Logger

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

    object ColFieldNoteItems {

        const val ID = "_id"
        const val FIELD_NOTE_ID = "field_note_id"
        const val ACTION = "action"
        const val CODE = "code"
        const val NAME = "name"
        const val ICON = "icon"
    }

    companion object {

        // tag for logger
        private const val TAG = "FieldNotesHelper"

        const val PATH_FIELD_NOTES = "fieldNotes"
        const val PATH_FIELD_NOTE_IMAGES = "fieldNoteImages"
        const val PATH_FIELD_NOTE_ITEMS = "fieldNoteItems"

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
            // get parameters for query
            val cpUri = getUriLogsTable(lv)

            // execute request
            var c: Cursor? = null
            try {
                c = ctx.contentResolver.query(cpUri,
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
        fun getAll(ctx: Context, lv: LocusUtils.LocusVersion): List<FieldNote> {
            return get(ctx, lv, "")
        }

        /**
         * Get single specific Field Note defined by it's ID
         * @param ctx existing context
         * @param lv active Locus version
         * @param id ID of note we want
         * @return [locus.api.android.features.geocaching.fieldNotes.FieldNote] or 'null'
         */
        @Throws(RequiredVersionMissingException::class)
        operator fun get(ctx: Context, lv: LocusUtils.LocusVersion, id: Long): FieldNote? {
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
                getItems(ctx, lv, fn)

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
        operator fun get(ctx: Context, lv: LocusUtils.LocusVersion, cacheCode: String?): List<FieldNote> {
            // get parameters for query
            val cpUri = getUriLogsTable(lv)

            // execute request
            var c: Cursor? = null
            try {
                // perform request based on 'cacheCode'
                c = if (cacheCode == null || cacheCode.isEmpty()) {
                    ctx.contentResolver.query(cpUri, null, null, null, null)
                } else {
                    ctx.contentResolver.query(cpUri, null,
                            ColFieldNote.CACHE_CODE + "=?",
                            arrayOf(cacheCode), null)
                }

                // handle result
                return if (c == null) {
                    ArrayList()
                } else {
                    createLogs(c)
                }
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
            // get parameters for query
            val cpUri = getUriLogsTable(lv)

            // execute request
            val res = ctx.contentResolver.delete(cpUri,
                    ColFieldNote.ID + "=?",
                    arrayOf(java.lang.Long.toString(fieldNoteId))
            )

            // delete images
            deleteImages(ctx, lv, fieldNoteId)
            deleteItems(ctx, lv, fieldNoteId)

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
            // get parameters for query
            val cpUri = getUriLogsTable(lv)

            // execute request (logs and also images)
            val count = ctx.contentResolver.delete(cpUri, null, null)
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
            // get parameters for query
            val cpUri = getUriLogsTable(lv)

            // createLogs data container
            val cv = createContentValues(gcFn)

            // execute request
            val newRow = ctx.contentResolver.insert(cpUri, cv) ?: return false

            // set new ID to field note
            gcFn.id = Utils.parseLong(newRow.lastPathSegment)

            // insert extra data
            storeAllImages(ctx, lv, gcFn)
            storeAllItems(ctx, lv, gcFn)

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
        fun update(ctx: Context, lv: LocusUtils.LocusVersion,
                gcFn: FieldNote): Boolean {
            // createLogs data container
            val cv = createContentValues(gcFn)

            // execute request
            return if (update(ctx, lv, gcFn, cv)) {
                // update extra data
                storeAllImages(ctx, lv, gcFn)
                storeAllItems(ctx, lv, gcFn)

                // all went well
                true
            } else {
                false
            }
        }

        @Throws(RequiredVersionMissingException::class)
        fun update(ctx: Context, lv: LocusUtils.LocusVersion,
                fn: FieldNote, cv: ContentValues): Boolean {
            // get parameters for query
            val cpUri = getUriLogsTable(lv)

            // execute request
            val newRow = ctx.contentResolver.update(cpUri, cv,
                    ColFieldNote.ID + "=?",
                    arrayOf(java.lang.Long.toString(fn.id)))
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

                // get 'all' notes and return first
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
                        arrayOf(java.lang.Long.toString(fn.id)), null)

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
                    arrayOf(java.lang.Long.toString(fieldNoteId)))
        }

        @Throws(RequiredVersionMissingException::class)
        private fun deleteImagesAll(ctx: Context, lv: LocusUtils.LocusVersion) {
            ctx.contentResolver.delete(
                    getUriImagesTable(lv), null, null)
        }

        // UPDATE

        @Throws(RequiredVersionMissingException::class)
        private fun updateImage(ctx: Context, lv: LocusUtils.LocusVersion, img: FieldNoteImage): Boolean {
            return ctx.contentResolver.update(
                    getUriImagesTable(lv),
                    createContentValues(img, false),
                    ColFieldNoteImage.ID + "=?",
                    arrayOf(java.lang.Long.toString(img.id))) == 1
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

        @Throws(RequiredVersionMissingException::class)
        private fun storeAllItems(ctx: Context, lv: LocusUtils.LocusVersion, fn: FieldNote) {
            // get existing items
            val existingItems = ArrayList<Long>()
            ctx.contentResolver.query(getUriItemsTable(lv),
                    arrayOf(ColFieldNoteItems.ID),
                    ColFieldNoteItems.FIELD_NOTE_ID + "=?",
                    arrayOf(java.lang.Long.toString(fn.id)), null)?.apply {
                while (moveToNext()) {
                    existingItems.add(getLong(0))
                }
                Utils.closeQuietly(this)
            }

            // update all items
            for (item in fn.items) {
                if (item.id >= 0) {
                    if (!existingItems.contains(item.id)) {
                        Logger.logD(TAG, "storeAllItems($ctx, $lv, $fn), " +
                                "item no longer exists in database")
                    } else {
                        existingItems.remove(item.id)
                        updateItem(ctx, lv, item)
                    }
                } else {
                    insertItem(ctx, lv, item)
                }
            }

            // remove remaining items
            for (remainingItem in existingItems) {
                deleteItem(ctx, lv, remainingItem)
            }
        }

        // GET

        @Throws(RequiredVersionMissingException::class)
        private fun getItems(ctx: Context, lv: LocusUtils.LocusVersion, fn: FieldNote) {
            fn.items.clear()
            ctx.contentResolver.query(getUriItemsTable(lv),
                    null,
                    ColFieldNoteItems.FIELD_NOTE_ID + "=?",
                    arrayOf(fn.id.toString()), null)?.apply {
                fn.items.addAll(createItems(this))
                Utils.closeQuietly(this)
            }
        }


        // INSERT

        @Throws(RequiredVersionMissingException::class)
        private fun insertItem(ctx: Context, lv: LocusUtils.LocusVersion, item: FieldNoteItem): Boolean {
            return ctx.contentResolver.insert(
                    getUriItemsTable(lv), createContentValues(item)) != null
        }

        // UPDATE

        @Throws(RequiredVersionMissingException::class)
        private fun updateItem(ctx: Context, lv: LocusUtils.LocusVersion, item: FieldNoteItem): Boolean {
            return ctx.contentResolver.update(
                    getUriItemsTable(lv), createContentValues(item),
                    ColFieldNoteItems.ID + "=?",
                    arrayOf(item.id.toString())) == 1
        }

        // DELETE

        @Throws(RequiredVersionMissingException::class)
        private fun deleteItem(ctx: Context, lv: LocusUtils.LocusVersion, itemId: Long) {
            ctx.contentResolver.delete(getUriItemsTable(lv),
                    "${ColFieldNoteItems.ID}=?",
                    arrayOf(itemId.toString()))
        }

        @Throws(RequiredVersionMissingException::class)
        private fun deleteItems(ctx: Context, lv: LocusUtils.LocusVersion, fieldNoteId: Long) {
            ctx.contentResolver.delete(getUriItemsTable(lv),
                    ColFieldNoteItems.FIELD_NOTE_ID + "=?",
                    arrayOf(fieldNoteId.toString()))
        }

        @Throws(RequiredVersionMissingException::class)
        private fun deleteItemsAll(ctx: Context, lv: LocusUtils.LocusVersion) {
            ctx.contentResolver.delete(getUriItemsTable(lv), null, null)
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
        private fun getUriItemsTable(lv: LocusUtils.LocusVersion): Uri {
            return ActionTools.getProviderUrlGeocaching(lv,
                    LocusUtils.VersionCode.UPDATE_05, PATH_FIELD_NOTE_ITEMS)
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
        private fun createContentValues(item: FieldNoteItem): ContentValues {
            return ContentValues().apply {
                put(ColFieldNoteItems.FIELD_NOTE_ID, item.fieldNoteId)
                put(ColFieldNoteItems.ACTION, item.action)
                put(ColFieldNoteItems.CODE, item.code)
                put(ColFieldNoteItems.NAME, item.name)
                put(ColFieldNoteItems.ICON, item.icon)
            }
        }

        // CREATING OBJECTS FROM CURSOR

        /**
         * Create list of logs from [cursor].
         */
        private fun createLogs(cursor: Cursor?): List<FieldNote> {
            // createLogs container and check data
            val res = ArrayList<FieldNote>()
            if (cursor == null) {
                throw IllegalArgumentException("Cursor cannot be 'null'")
            }

            // iterate over cursor
            var i = 0
            val m = cursor.count
            while (i < m) {
                cursor.moveToPosition(i)

                // createLogs empty object
                val fn = FieldNote()

                // set parameters (required)
                fn.id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(ColFieldNote.ID))
                fn.cacheCode = cursor.getString(
                        cursor.getColumnIndexOrThrow(ColFieldNote.CACHE_CODE))
                fn.cacheName = cursor.getString(
                        cursor.getColumnIndexOrThrow(ColFieldNote.CACHE_NAME))
                fn.type = cursor.getInt(
                        cursor.getColumnIndexOrThrow(ColFieldNote.TYPE))
                fn.time = cursor.getLong(
                        cursor.getColumnIndex(ColFieldNote.TIME))

                // set parameters (optional)
                val iNote = cursor.getColumnIndex(ColFieldNote.NOTE)
                if (iNote >= 0) {
                    fn.note = cursor.getString(iNote)
                }
                val iFavorite = cursor.getColumnIndex(ColFieldNote.FAVORITE)
                if (iFavorite >= 0) {
                    fn.isFavorite = cursor.getInt(iFavorite) == 1
                }
                val iLogged = cursor.getColumnIndex(ColFieldNote.LOGGED)
                if (iLogged >= 0) {
                    fn.isLogged = cursor.getInt(iLogged) == 1
                }

                // add field note to container
                res.add(fn)
                i++
            }

            // return result
            return res
        }

        /**
         * Create list of images from [cursor].
         */

        private fun createImages(cursor: Cursor?): List<FieldNoteImage> {
            // createLogs container and check data
            val res = ArrayList<FieldNoteImage>()
            if (cursor == null) {
                throw IllegalArgumentException("Cursor cannot be 'null'")
            }

            // iterate over cursor
            for (i in 0 until cursor.count) {
                cursor.moveToPosition(i)

                // createLogs empty object
                val img = FieldNoteImage()

                // set parameters (required)
                img.id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(ColFieldNoteImage.ID))

                // set parameters (optional)
                val iFnId = cursor.getColumnIndex(ColFieldNoteImage.FIELD_NOTE_ID)
                if (iFnId >= 0) {
                    img.fieldNoteId = cursor.getLong(iFnId)
                }
                val iCap = cursor.getColumnIndex(ColFieldNoteImage.CAPTION)
                if (iCap >= 0) {
                    img.caption = cursor.getString(iCap)
                }
                val iDesc = cursor.getColumnIndex(ColFieldNoteImage.DESCRIPTION)
                if (iDesc >= 0) {
                    img.description = cursor.getString(iDesc)
                }
                val iData = cursor.getColumnIndex(ColFieldNoteImage.DATA)
                if (iData >= 0) {
                    img.image = cursor.getBlob(iData)
                }

                // add field note to container
                res.add(img)
            }

            // return result
            return res
        }

        /**
         * Create list of items from [cursor].
         */
        private fun createItems(cursor: Cursor?): List<FieldNoteItem> {
            // createLogs container and check data
            val res = ArrayList<FieldNoteItem>()
            if (cursor == null) {
                throw IllegalArgumentException("Cursor cannot be 'null'")
            }

            // iterate over cursor
            for (i in 0 until cursor.count) {
                cursor.moveToPosition(i)

                // createLogs object
                res.add(FieldNoteItem().apply {
                    id = cursor.getLong(
                            cursor.getColumnIndexOrThrow(ColFieldNoteItems.ID))

                    // set parameters (optional)
                    cursor.getColumnIndex(ColFieldNoteItems.FIELD_NOTE_ID)
                            .takeIf { it >= 0 }
                            ?.let { fieldNoteId = cursor.getLong(it) }
                    cursor.getColumnIndex(ColFieldNoteItems.ACTION)
                            .takeIf { it >= 0 }
                            ?.let { action = cursor.getInt(it) }
                    cursor.getColumnIndex(ColFieldNoteItems.CODE)
                            .takeIf { it >= 0 }
                            ?.let { code = cursor.getString(it) }
                    cursor.getColumnIndex(ColFieldNoteItems.NAME)
                            .takeIf { it >= 0 }
                            ?.let { name = cursor.getString(it) }
                    cursor.getColumnIndex(ColFieldNoteItems.ICON)
                            .takeIf { it >= 0 }
                            ?.let { icon = cursor.getString(it) }
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
