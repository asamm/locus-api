package locus.api.android.features.geocaching.fieldNotes;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import locus.api.android.ActionTools;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.Utils;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;

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
public class FieldNotesHelper {

    public static final String PATH_FIELD_NOTES             = "fieldNotes";
    public static final String PATH_FIELD_NOTE_IMAGES       = "fieldNoteImages";

    public static class ColFieldNote {

        public static final String ID = "_id";
        public static final String CACHE_CODE = "cache_code";
        public static final String CACHE_NAME = "cache_name";
        public static final String TYPE = "type";
        public static final String TIME = "time";
        public static final String NOTE = "note";
        public static final String FAVORITE = "favorite";
        public static final String LOGGED = "logged";
    }

    public static class ColFieldNoteImage {

        public static final String ID = "_id";
        public static final String FIELD_NOTE_ID = "field_note_id";
        public static final String CAPTION = "caption";
        public static final String DESCRIPTION = "description";
        public static final String DATA = "data";
    }

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
    public static int getCount(Context ctx, LocusUtils.LocusVersion lv)
            throws RequiredVersionMissingException {
        // get parameters for query
        Uri cpUri = getUriFieldNoteTable(lv);

        // execute request
        Cursor c = null;
        try {
            c = ctx.getContentResolver().query(cpUri,
                    new String[] {ColFieldNote.ID},
                    null, null, null);
            if (c == null) {
                return 0;
            } else {
                return c.getCount();
            }
        } finally {
            Utils.closeQuietly(c);
        }
    }

    /**
     * Get List of all available field notes in Locus
     * @param ctx existing context
     * @param lv active Locus version
     * @return List of all field notes
     */
    public static List<FieldNote> getAll(Context ctx, LocusUtils.LocusVersion lv)
            throws RequiredVersionMissingException {
        return get(ctx, lv, "");
    }

    /**
     * Get single specific Field Note defined by it's ID
     * @param ctx existing context
     * @param lv active Locus version
     * @param id ID of note we want
     * @return {@link locus.api.android.features.geocaching.fieldNotes.FieldNote} or 'null'
     */
    public static FieldNote get(Context ctx, LocusUtils.LocusVersion lv, long id)
            throws RequiredVersionMissingException {
        // get parameters for query
        Uri cpUri = getUriFieldNoteTable(lv);
        cpUri = ContentUris.withAppendedId(cpUri, id);

        // execute request
        Cursor c = null;
        try {
            c = ctx.getContentResolver().query(cpUri,
                    null, null, null, null);
            if (c == null || c.getCount() != 1) {
                return null;
            }

            // get 'all' notes and return first
            FieldNote fn = create(c).get(0);

            // load also images
            getImages(ctx, lv, fn);

            // return field note
            return fn;
        } finally {
            Utils.closeQuietly(c);
        }
    }

    /**
     * Request on specific field notes for certain cache (define by it's cache code)
     * @param ctx existing context
     * @param lv active Locus version
     * @param cacheCode code of cache for which we want field notes
     * @return list of all field notes for certain cache
     */
    public static List<FieldNote> get(Context ctx, LocusUtils.LocusVersion lv, String cacheCode)
            throws RequiredVersionMissingException {
        // get parameters for query
        Uri cpUri = getUriFieldNoteTable(lv);

        // execute request
        Cursor c = null;
        try {
            // perform request based on 'cacheCode'
            if (cacheCode == null || cacheCode.length() == 0) {
                c = ctx.getContentResolver().query(cpUri,
                        null, null, null, null);
            } else {
                c = ctx.getContentResolver().query(cpUri,
                        null,
                        ColFieldNote.CACHE_CODE + "=?",
                        new String[]{cacheCode},
                        null);
            }

            // handle result
            if (c == null) {
                return new ArrayList<FieldNote>();
            } else {
                return create(c);
            }
        } finally {
            Utils.closeQuietly(c);
        }
    }

    // DELETE

    /**
     * Delete field note specified by it's ID
     * @param ctx existing context
     * @param lv active Locus version
     * @param fieldNoteId ID of field note to delete
     * @return <code>true</code> if deleted successfully, otherwise <code>false</code>
     */
    public static boolean delete(Context ctx, LocusUtils.LocusVersion lv, long fieldNoteId)
            throws RequiredVersionMissingException {
        // get parameters for query
        Uri cpUri = getUriFieldNoteTable(lv);

        // execute request
        int res = ctx.getContentResolver().delete(cpUri,
                ColFieldNote.ID + "=?",
                new String[] {Long.toString(fieldNoteId)}
        );

        // delete images
        deleteImages(ctx, lv, fieldNoteId);

        // return result
        return res == 1;
    }

    /**
     * Delete all field notes in Locus
     * @param ctx existing context
     * @param lv active Locus version
     * @return number of deleted field notes
     */
    public static int deleteAll(Context ctx, LocusUtils.LocusVersion lv)
            throws RequiredVersionMissingException {
        // get parameters for query
        Uri cpUri = getUriFieldNoteTable(lv);

        // execute request (logs and also images)
        int count = ctx.getContentResolver().delete(cpUri, null, null);
        deleteImagesAll(ctx, lv);
        return count;
    }

    // INSERT

    /**
     * Simple insert of one field note
     * @param ctx existing context
     * @param lv active Locus version
     * @param gcFn field note that should be inserted.
     * @return <code>true</code> if insert was successful, otherwise false
     */
    public static boolean insert(Context ctx, LocusUtils.LocusVersion lv,
                                 FieldNote gcFn) throws RequiredVersionMissingException {
        // get parameters for query
        Uri cpUri = getUriFieldNoteTable(lv);

        // create data container
        ContentValues cv = createContentValues(gcFn);

        // execute request
        Uri newRow = ctx.getContentResolver().insert(cpUri, cv);
        if (newRow != null) {
            // set new ID to field note
            gcFn.setId(Utils.parseLong(newRow.getLastPathSegment()));

            // insert also images
            storeAllImages(ctx, lv, gcFn);

            // return result
            return true;
        }
        return false;
    }

    // UPDATE

    /**
     * Update single field note define by it's ID
     * @param ctx existing context
     * @param lv active Locus version
     * @param gcFn field note, that should be updated
     * @return <code>true</code> if update was successful, otherwise false
     */
    public static boolean update(Context ctx, LocusUtils.LocusVersion lv,
            FieldNote gcFn) throws RequiredVersionMissingException {
        // create data container
        ContentValues cv = createContentValues(gcFn);

        // execute request
        if (update(ctx, lv, gcFn, cv)) {
            storeAllImages(ctx, lv, gcFn);
            return true;
        } else {
            return false;
        }
    }

    public static boolean update(Context ctx, LocusUtils.LocusVersion lv,
            FieldNote fn, ContentValues cv) throws RequiredVersionMissingException {
        // get parameters for query
        Uri cpUri = getUriFieldNoteTable(lv);

        // execute request
        int newRow = ctx.getContentResolver().update(cpUri, cv,
                ColFieldNote.ID + "=?",
                new String[] {Long.toString(fn.getId())});
        return newRow == 1;
    }

    /**************************************************/
    // IMAGE HANDLERS
    /**************************************************/

    private static void storeAllImages(Context ctx, LocusUtils.LocusVersion lv, FieldNote fn)
            throws RequiredVersionMissingException {
        Iterator<FieldNoteImage> images = fn.getImages();
        while (images.hasNext()) {
            FieldNoteImage img = images.next();
            img.setFieldNoteId(fn.getId());

            // update or insert image
            if (img.getId() >= 0) {
                updateImage(ctx, lv, img);
            } else {
                insertImage(ctx, lv, img);
            }
        }
    }

    // GET

    public static FieldNoteImage getImage(Context ctx, LocusUtils.LocusVersion lv, long imgId)
            throws RequiredVersionMissingException {
        // get parameters for query
        Uri cpUri = getUriFieldNoteImagesTable(lv);
        cpUri = ContentUris.withAppendedId(cpUri, imgId);

        // execute request
        Cursor c = null;
        try {
            c = ctx.getContentResolver().query(cpUri,
                    null, null, null, null);
            if (c == null || c.getCount() != 1) {
                return null;
            }

            // get 'all' notes and return first
            return createFieldNoteImages(c).get(0);
        } finally {
            Utils.closeQuietly(c);
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
    private static void getImages(Context ctx, LocusUtils.LocusVersion lv, FieldNote fn)
            throws RequiredVersionMissingException {
        // get parameters for query
        Uri cpUri = getUriFieldNoteImagesTable(lv);

        // execute request
        Cursor c = null;
        try {
            // perform request based on 'cacheCode'
            c = ctx.getContentResolver().query(cpUri,
                    new String[] {ColFieldNoteImage.ID},
                    ColFieldNoteImage.FIELD_NOTE_ID + "=?",
                    new String[] {Long.toString(fn.getId())},
                    null);

            // handle result
            if (c != null) {
                List<FieldNoteImage> images = createFieldNoteImages(c);
                for (int i = 0, m = images.size(); i < m; i++) {
                    fn.addImage(images.get(i));
                }
            }
        } finally {
            Utils.closeQuietly(c);
        }
    }

    // DELETE

    private static void deleteImages(Context ctx, LocusUtils.LocusVersion lv, long fieldNoteId)
            throws RequiredVersionMissingException {
        // get parameters for query
        Uri cpUri = getUriFieldNoteImagesTable(lv);

        // execute request
        ctx.getContentResolver().delete(cpUri,
                ColFieldNoteImage.FIELD_NOTE_ID + "=?",
                new String[] {Long.toString(fieldNoteId)});
    }

    private static void deleteImagesAll(Context ctx, LocusUtils.LocusVersion lv)
            throws RequiredVersionMissingException {
        // get parameters for query
        Uri cpUri = getUriFieldNoteImagesTable(lv);

        // execute request
        ctx.getContentResolver().delete(cpUri, null, null);
    }

    // UPDATE

    private static boolean updateImage(Context ctx, LocusUtils.LocusVersion lv, FieldNoteImage img)
            throws RequiredVersionMissingException {
        // get parameters for query
        Uri cpUri = getUriFieldNoteImagesTable(lv);

        // create data container
        ContentValues cv = createContentValues(img, false);

        // execute request
        int newRow = ctx.getContentResolver().update(cpUri, cv,
                ColFieldNoteImage.ID + "=?",
                new String[] {Long.toString(img.getId())});
        return newRow == 1;
    }

    // INSERT

    private static boolean insertImage(Context ctx, LocusUtils.LocusVersion lv, FieldNoteImage img)
            throws RequiredVersionMissingException {
        // get parameters for query
        Uri cpUri = getUriFieldNoteImagesTable(lv);

        // create data container
        ContentValues cv = createContentValues(img, true);

        // execute request
        Uri newRow = ctx.getContentResolver().insert(cpUri, cv);
        return newRow != null;
    }


    // PRIVATE HELP FUNCTIONS

    /**
     * Create valid Uri to field note provider
     * @param lv active Locus version
     * @return Uri to Field note provider
     * @throws RequiredVersionMissingException
     */
    private static Uri getUriFieldNoteTable(LocusUtils.LocusVersion lv)
            throws RequiredVersionMissingException {
        Uri uri = ActionTools.getContentProviderGeocaching(lv,
                LocusUtils.VersionCode.UPDATE_05, PATH_FIELD_NOTES);
        if (uri == null) {
            throw new RequiredVersionMissingException(LocusUtils.VersionCode.UPDATE_05);
        }
        return uri;
    }

    /**
     * Create valid Uri to field note provider of images
     * @param lv active Locus version
     * @return Uri to Field note images provider
     * @throws RequiredVersionMissingException
     */
    private static Uri getUriFieldNoteImagesTable(LocusUtils.LocusVersion lv)
            throws RequiredVersionMissingException {
        Uri uri = ActionTools.getContentProviderGeocaching(lv,
                LocusUtils.VersionCode.UPDATE_05, PATH_FIELD_NOTE_IMAGES);
        if (uri == null) {
            throw new RequiredVersionMissingException(LocusUtils.VersionCode.UPDATE_05);
        }
        return uri;
    }

    /**************************************************/
    // WORKING WITH CURSORS
    /**************************************************/

    /**
     * Create List of FieldNotes from cursor.
     * @param cursor received as response from Locus
     * @return list of FieldNotes
     */
    private static List<FieldNote> create(Cursor cursor) {
        // create container and check data
        List<FieldNote> res = new ArrayList<>();
        if (cursor == null) {
            throw new IllegalArgumentException("Cursor cannot be 'null'");
        }

        // iterate over cursor
        for (int i = 0, m = cursor.getCount(); i < m; i++) {
            cursor.moveToPosition(i);

            // create empty object
            FieldNote fn = new FieldNote();

            // set parameters (required)
            fn.setId(cursor.getLong(
                    cursor.getColumnIndexOrThrow(ColFieldNote.ID)));
            fn.setCacheCode(cursor.getString(
                    cursor.getColumnIndexOrThrow(ColFieldNote.CACHE_CODE)));
            fn.setCacheName(cursor.getString(
                    cursor.getColumnIndexOrThrow(ColFieldNote.CACHE_NAME)));
            fn.setType(cursor.getInt(
                    cursor.getColumnIndexOrThrow(ColFieldNote.TYPE)));
            fn.setTime(cursor.getLong(
                    cursor.getColumnIndex(ColFieldNote.TIME)));

            // set parameters (optional)
            int iNote = cursor.getColumnIndex(ColFieldNote.NOTE);
            if (iNote >= 0) {
                fn.setNote(cursor.getString(iNote));
            }
            int iFavorite = cursor.getColumnIndex(ColFieldNote.FAVORITE);
            if (iFavorite >= 0) {
                fn.setFavorite(cursor.getInt(iFavorite) == 1);
            }
            int iLogged = cursor.getColumnIndex(ColFieldNote.LOGGED);
            if (iLogged >= 0) {
                fn.setLogged(cursor.getInt(iLogged) == 1);
            }

            // add field note to container
            res.add(fn);
        }

        // return result
        return res;
    }

    /**
     * Create ContentValues object, that is used for updates and inserts
     * over ContentProvider
     * @param fn FieldNote we want to update/insert
     * @return filled container
     */
    private static ContentValues createContentValues(FieldNote fn) {
        // check parameters
        if (fn == null) {
            throw new IllegalArgumentException("Field note cannot be 'null'");
        }

        // create container
        ContentValues cv = new ContentValues();
        cv.put(ColFieldNote.CACHE_CODE, fn.getCacheCode());
        cv.put(ColFieldNote.CACHE_NAME, fn.getCacheName());
        cv.put(ColFieldNote.TYPE, fn.getType());
        cv.put(ColFieldNote.TIME, fn.getTime());
        cv.put(ColFieldNote.NOTE, fn.getNote());
        cv.put(ColFieldNote.FAVORITE, fn.isFavorite());
        cv.put(ColFieldNote.LOGGED, fn.isLogged());

        // return result
        return cv;
    }

    // IMAGES

    /**
     * Create List of FieldNoteImages from cursor.
     * @param cursor received as response from Locus
     * @return list of FieldNoteImages
     */
    private static List<FieldNoteImage> createFieldNoteImages(Cursor cursor) {
        // create container and check data
        List<FieldNoteImage> res = new ArrayList<>();
        if (cursor == null) {
            throw new IllegalArgumentException("Cursor cannot be 'null'");
        }

        // iterate over cursor
        for (int i = 0, m = cursor.getCount(); i < m; i++) {
            cursor.moveToPosition(i);

            // create empty object
            FieldNoteImage img = new FieldNoteImage();

            // set parameters (required)
            img.setId(cursor.getLong(
                    cursor.getColumnIndexOrThrow(ColFieldNoteImage.ID)));

            // set parameters (optional)
            int iFnId = cursor.getColumnIndex(ColFieldNoteImage.FIELD_NOTE_ID);
            if (iFnId >= 0) {
                img.setFieldNoteId(cursor.getLong(iFnId));
            }
            int iCap = cursor.getColumnIndex(ColFieldNoteImage.CAPTION);
            if (iCap >= 0) {
                img.setCaption(cursor.getString(iCap));
            }
            int iDesc = cursor.getColumnIndex(ColFieldNoteImage.DESCRIPTION);
            if (iDesc >= 0) {
                img.setDescription(cursor.getString(iDesc));
            }
            int iData = cursor.getColumnIndex(ColFieldNoteImage.DATA);
            if (iData >= 0) {
                img.setImage(cursor.getBlob(iData));
            }

            // add field note to container
            res.add(img);
        }

        // return result
        return res;
    }

    /**
     * Create ContentValues object, that is used for updates and inserts
     * over ContentProvider
     * @param img FieldNoteImage we want to update/insert
     * @return filled container
     */
    private static ContentValues createContentValues(FieldNoteImage img, boolean alsoData) {
        // check parameters
        if (img == null || img.getImage() == null) {
            throw new IllegalArgumentException("Field note image cannot be 'null'");
        }

        // create container
        ContentValues cv = new ContentValues();
        cv.put(ColFieldNoteImage.FIELD_NOTE_ID, img.getFieldNoteId());
        cv.put(ColFieldNoteImage.CAPTION, img.getCaption());
        cv.put(ColFieldNoteImage.DESCRIPTION, img.getDescription());
        if (alsoData) {
            cv.put(ColFieldNoteImage.DATA, img.getImage());
        }

        // return result
        return cv;
    }

    /**************************************************/
    // SPECIAL ACTIONS
    /**************************************************/

	/**
     * Log list of defined field notes online on Geocaching server.
     *
	 * @param ctx existing context
	 * @param lv active Locus version
	 * @param ids list of FieldNote id's that should be logged
	 * @param createLog <code>true</code> if we wants to create a log directly, or
	 *                  <code>false</code> if note should be posted into FieldNotes section
	 * @throws RequiredVersionMissingException error in case, LocusVersion is not valid
	 */
    public static void logOnline(Context ctx, LocusUtils.LocusVersion lv,
            long[] ids, boolean createLog) throws RequiredVersionMissingException {
        // check parameters
        if (ctx == null || lv == null || ids == null || ids.length == 0) {
            throw new IllegalArgumentException("logOnline(" + ctx + ", " + lv + ", " + ids + "), " +
                    "invalid parameters");
        }

        // check version
        if (!lv.isVersionValid(LocusUtils.VersionCode.UPDATE_05)) {
            throw new RequiredVersionMissingException(LocusUtils.VersionCode.UPDATE_05);
        }

        // execute request
        Intent intent = new Intent(LocusConst.ACTION_LOG_FIELD_NOTES);
        intent.putExtra(LocusConst.INTENT_EXTRA_FIELD_NOTES_IDS, ids);
		intent.putExtra(LocusConst.INTENT_EXTRA_FIELD_NOTES_CREATE_LOG, createLog);
        ctx.startActivity(intent);
    }
}
