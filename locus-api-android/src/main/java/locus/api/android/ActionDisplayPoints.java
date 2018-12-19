package locus.api.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import locus.api.android.objects.PackPoints;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.Storable;
import locus.api.utils.Logger;
import locus.api.utils.Utils;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "DeprecatedIsStillUsed"})
public class ActionDisplayPoints extends ActionDisplay {

    // tag for logger
    private static final String TAG = "ActionDisplayPoints";

    // ONE PACK_WAYPOINT OVER INTENT

    /**
     * Simple way how to send data over intent to Locus. Be aware that intent in Android have some size limits,
     * so for larger data, use below method {@link #sendPacksFile(Context, List, String, ExtraAction)}
     *
     * @param context     actual {@link Context}
     * @param data        {@link PackPoints} object that should be send to Locus
     * @param extraAction extra action that should happen after display in app
     * @return {@code true} if action was success
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    public static boolean sendPack(Context context, PackPoints data, ExtraAction extraAction)
            throws RequiredVersionMissingException {
        return sendPack(LocusConst.ACTION_DISPLAY_DATA,
                context, data,
                extraAction == ExtraAction.IMPORT,
                extraAction == ExtraAction.CENTER);
    }

    /**
     * Silent methods are useful in case, Locus is already running and you want to
     * display any data without interrupting a user. This method send data over
     * Broadcast intent
     *
     * @param context      actual {@link Context}
     * @param data         {@link PackPoints} object that should be send to Locus
     * @param centerOnData allows to center on send data. This parameter is ignored
     *                     if <code>callImport</code> is set to <code>true</code>. Suggested is value
     *                     <code>false</code> because unexpected centering breaks usability.
     * @return <code>true</code> if action was success
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    public static boolean sendPackSilent(Context context, PackPoints data, boolean centerOnData)
            throws RequiredVersionMissingException {
        return sendPack(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                context, data,
                false, centerOnData);
    }

    /**
     * @param action       action to perform
     * @param context      actual {@link Context}
     * @param data         {@link PackPoints} object that should be send to Locus
     * @param callImport   whether import with this data should be called after Locus starts.
     *                     Otherwise data will be displayed as temporary objects on map.
     * @param centerOnData allow to center on displayed data. This parameter is ignored
     *                     if <code>callImport = true</code>. Suggested if <code>false</code> because
     *                     unexpected centering breaks usability.
     * @return {@code true} if data were correctly send
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    private static boolean sendPack(String action, Context context,
            PackPoints data, boolean callImport, boolean centerOnData)
            throws RequiredVersionMissingException {
        // check data
        if (data == null) {
            return false;
        }

        // create and send intent
        Intent intent = new Intent();
        intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_DATA,
                data.getAsBytes());
        return sendData(action, context, intent, callImport, centerOnData);
    }

    // LIST OF PACK_WAYPOINTS OVER INTENT

    /**
     * Simple way how to send ArrayList<PackPoints> object over intent to Locus. Count that
     * intent in Android have some size limits so for larger data, use another method
     *
     * @param context actual {@link Context}
     * @param data    {@link ArrayList} of data that should be send to Locus
     * @return true if success
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    public static boolean sendPacks(Context context,
            List<PackPoints> data, ExtraAction extraAction)
            throws RequiredVersionMissingException {
        return sendPacks(LocusConst.ACTION_DISPLAY_DATA,
                context, data, extraAction == ExtraAction.IMPORT,
                extraAction == ExtraAction.CENTER);
    }

    public static boolean sendPacksSilent(Context context,
            List<PackPoints> data, boolean centerOnData)
            throws RequiredVersionMissingException {
        return sendPacks(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                context, data, false, centerOnData);
    }

    private static boolean sendPacks(String action, Context context,
            List<PackPoints> data, boolean callImport, boolean centerOnData)
            throws RequiredVersionMissingException {
        // check data
        if (data == null) {
            return false;
        }

        // create and send intent
        Intent intent = new Intent();
        intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_DATA_ARRAY,
                Storable.getAsBytes(data));
        return sendData(action, context, intent, callImport, centerOnData);
    }

    // MORE PACK_WAYPOINTS OVER FILE

    /**
     * <p>Allows to send data to Locus, by storing a serialized version of data into a file. This
     * method can have advantage over a cursor in simplicity of implementation and also that
     * the file size is not limited as in the cursor method.</p>
     *
     * <p>On the second case, <s>needs permission for disk access</s> (not needed in a latest
     * Android) and should be slower due to an IO operations.</p>
     *
     * <p>Be careful about the size of data. This method can cause OutOfMemory error on Locus side
     * if data are too big, because all needs to be loaded at once before process.</p>
     *
     * @param context     existing {@link Context}
     * @param data        data to send
     * @param filepath    path where data should be stored
     * @param extraAction extra action that should happen after Locus reads data
     * @return {@code true} if data were correctly send, otherwise {@code false}
     * @throws RequiredVersionMissingException exception in case of missing required app version
     * @deprecated Use {@link #sendPacksFile(Context, List, File, Uri, ExtraAction)} which doesn't
     * require permission for disk access
     */
    @Deprecated
    public static boolean sendPacksFile(
            @NonNull Context context,
            @NonNull List<PackPoints> data,
            @NonNull String filepath,
            @NonNull ExtraAction extraAction
    ) throws RequiredVersionMissingException {
        return sendPacksFile(LocusConst.ACTION_DISPLAY_DATA,
                context, data, new File(filepath), null, extraAction == ExtraAction.IMPORT,
                extraAction == ExtraAction.CENTER);
    }

    /**
     * <p>Allow to send data to Locus, by storing a serialized version of data into a file This
     * method can have advantage over a cursor in simplicity of implementation and also that
     * the file size is not limited as in the cursor method.</p>
     *
     * <p>On second case, should be slower due to IO operations.</p>
     *
     * <p>Be careful about the size of data. This method can cause OutOfMemory error on Locus side
     * if data are too big, because all needs to be loaded at once before process.</p>
     *
     * @param context     existing {@link Context}
     * @param data        data to send
     * @param file        path where data should be stored
     * @param fileUri     uri from {@code FileProvider}, which represents filepath
     * @param extraAction extra action that should happen after Locus reads data
     * @return {@code true} if data were correctly send, otherwise {@code false}
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    public static boolean sendPacksFile(
            @NonNull Context context,
            @NonNull List<PackPoints> data,
            @NonNull File file,
            @NonNull Uri fileUri,
            @NonNull ExtraAction extraAction
    ) throws RequiredVersionMissingException {
        return sendPacksFile(LocusConst.ACTION_DISPLAY_DATA,
                context, data, file, fileUri, extraAction == ExtraAction.IMPORT,
                extraAction == ExtraAction.CENTER);
    }

    /**
     * <p>Allows to send data to Locus silently without user interaction, by storing a serialized
     * version of data into a file. This method can have advantage over a cursor in simplicity of
     * implementation and also that the file size is not limited as in the cursor method.</p>
     *
     * <p>On the second case, <s>needs permission for disk access</s> (not needed in a latest
     * Android) and should be slower due to an IO operations.</p>
     *
     * <p>Be careful about the size of data. This method can cause OutOfMemory error on Locus side
     * if data are too big, because all needs to be loaded at once before process.</p>
     *
     * @param context      existing {@link Context}
     * @param data         data to send
     * @param filepath     path where data should be stored
     * @param centerOnData {@code true} to center on data
     * @return {@code true} if data were correctly send, otherwise {@code false}
     * @throws RequiredVersionMissingException exception in case of missing required app version
     * @deprecated Use {@link #sendPacksFileSilent(Context, List, File, Uri, boolean)} which
     * doesn't require permission for disk access
     */
    @Deprecated
    public static boolean sendPacksFileSilent(
            @NonNull Context context,
            @NonNull List<PackPoints> data,
            @NonNull String filepath,
            boolean centerOnData
    ) throws RequiredVersionMissingException {
        return sendPacksFile(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                context, data, new File(filepath), null, false, centerOnData);
    }

    /**
     * <p>Allows to send data to Locus silently without user interaction, by storing a serialized
     * version of data into a file. This method can have advantage over a cursor in simplicity of
     * implementation and also that the file size is not limited as in the cursor method.</p>
     *
     * <p>On the second case, <s>needs permission for disk access</s> (not needed in a latest
     * Android) and should be slower due to an IO operations.</p>
     *
     * <p>Be careful about the size of data. This method can cause OutOfMemory error on Locus side
     * if data are too big, because all needs to be loaded at once before process.</p>
     *
     * @param context      existing {@link Context}
     * @param data         data to send
     * @param file         path where data should be stored
     * @param fileUri      uri from {@code FileProvider}, which represents filepath
     * @param centerOnData {@code true} to center on data
     * @return {@code true} if data were correctly send, otherwise {@code false}
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    public static boolean sendPacksFileSilent(
            @NonNull Context context,
            @NonNull List<PackPoints> data,
            @NonNull File file,
            @NonNull Uri fileUri,
            boolean centerOnData
    ) throws RequiredVersionMissingException {
        return sendPacksFile(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                context, data, file, fileUri, false, centerOnData);
    }

    /**
     * Main function for sending pack of points over temporary stored file.
     *
     * @param action       action we wants to perform
     * @param context      current context
     * @param data         data to send
     * @param file         path where file will be temporary stored
     * @param fileUri      uri from {@code FileProvider}, which represents {@code file}
     * @param callImport   {@code true} to call import after load in Locus
     * @param centerOnData {@code true} to center on data
     * @return {@code true} if request was correctly send
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    private static boolean sendPacksFile(
            @NonNull String action,
            @NonNull Context context,
            @NonNull List<PackPoints> data,
            @NonNull File file,
            @Nullable Uri fileUri,
            boolean callImport,
            boolean centerOnData
    ) throws RequiredVersionMissingException {
        if (sendDataWriteOnCard(data, file)) {
            Intent intent = new Intent();
            if (fileUri != null) {
                intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_URI, fileUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                return sendData(action, context, intent, callImport, centerOnData, LocusUtils.VersionCode.UPDATE_15);
            } else {
                intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH, file.getAbsolutePath());
                return sendData(action, context, intent, callImport, centerOnData);
            }
        } else {
            return false;
        }
    }

    private static boolean sendDataWriteOnCard(@NonNull List<PackPoints> data, @NonNull File file) {
        if (data.size() == 0)
            return false;

        DataOutputStream dos = null;
        try {
            file.getParentFile().mkdirs();

            // delete previous file
            if (file.exists()) {
                file.delete();
            }

            // create stream
            dos = new DataOutputStream(new FileOutputStream(file, false));

            // write current version
            Storable.writeList(data, dos);
            dos.flush();
            return true;
        } catch (Exception e) {
            Logger.logE(TAG, "sendDataWriteOnCard(" + file.getAbsolutePath() + ", " + data + ")", e);
            return false;
        } finally {
            Utils.closeStream(dos);
        }
    }

    private static List<PackPoints> readDataFromPath(@NonNull String filepath) {
        // check file
        File file = new File(filepath);
        if (!file.exists() || !file.isFile()) {
            return new ArrayList<>();
        }

        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
            return Storable.readList(PackPoints.class, dis);
        } catch (Exception e) {
            Logger.logE(TAG, "readDataFromPath(" + filepath + ")", e);
        } finally {
            Utils.closeStream(dis);
        }
        return new ArrayList<>();
    }

    /**
     * Invert method to {@link #sendPacksFile(Context, List, File, Uri, ExtraAction)} or
     * {@link #sendPacksFile(Context, List, String, ExtraAction)}. This load serialized data from
     * a file stored in {@link Intent}.
     *
     * @param ctx    context
     * @param intent intent data
     * @return loaded pack of points
     */
    public static List<PackPoints> readPacksFile(@NonNull Context ctx, @NonNull Intent intent) {
        if (intent.hasExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_URI)) {
            return readDataFromUri(ctx, intent.getParcelableExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_URI));
        } else {
            // backward compatibility
            return readDataFromPath(intent.getStringExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH));
        }
    }

    private static List<PackPoints> readDataFromUri(@NonNull Context ctx, @NonNull Uri fileUri) {
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(ctx.getContentResolver().openInputStream(fileUri));
            return Storable.readList(PackPoints.class, dis);
        } catch (Exception e) {
            Logger.logE(TAG, "readDataFromUri(" + fileUri + ")", e);
        } finally {
            Utils.closeStream(dis);
        }
        return new ArrayList<>();
    }

    /**
     * Allows to remove already send Pack from the map. Keep in mind, that this method remove
     * only packs that are visible (temporary) on map.
     *
     * @param ctx      current context
     * @param packName name of pack
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    public static void removePackFromLocus(Context ctx, String packName)
            throws RequiredVersionMissingException {
        // check data
        if (packName == null || packName.length() == 0) {
            return;
        }

        // create empty pack
        PackPoints pw = new PackPoints(packName);

        // create and send intent
        Intent intent = new Intent();
        intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_DATA, pw.getAsBytes());
        sendPackSilent(ctx, pw, false);
    }
}
