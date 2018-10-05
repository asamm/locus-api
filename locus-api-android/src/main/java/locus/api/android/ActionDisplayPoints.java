package locus.api.android;

import android.content.Context;
import android.content.Intent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import locus.api.android.objects.PackWaypoints;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.Storable;
import locus.api.utils.Logger;
import locus.api.utils.Utils;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ActionDisplayPoints extends ActionDisplay {

    // tag for logger
    private static final String TAG = "ActionDisplayPoints";

    // ONE PACK_WAYPOINT OVER INTENT

    /**
     * Simple way how to send data over intent to Locus. Be aware that intent in Android have some size limits,
     * so for larger data, use below method {@link #sendPacksFile(Context, List, String, ExtraAction)}
     *
     * @param context     actual {@link Context}
     * @param data        {@link PackWaypoints} object that should be send to Locus
     * @param extraAction extra action that should happen after display in app
     * @return {@code true} if action was success
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    public static boolean sendPack(Context context, PackWaypoints data, ExtraAction extraAction)
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
     * @param data         {@link PackWaypoints} object that should be send to Locus
     * @param centerOnData allows to center on send data. This parameter is ignored
     *                     if <code>callImport</code> is set to <code>true</code>. Suggested is value
     *                     <code>false</code> because unexpected centering breaks usability.
     * @return <code>true</code> if action was success
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    public static boolean sendPackSilent(Context context, PackWaypoints data, boolean centerOnData)
            throws RequiredVersionMissingException {
        return sendPack(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                context, data,
                false, centerOnData);
    }

    /**
     * @param action       action to perform
     * @param context      actual {@link Context}
     * @param data         {@link PackWaypoints} object that should be send to Locus
     * @param callImport   whether import with this data should be called after Locus starts.
     *                     Otherwise data will be displayed as temporary objects on map.
     * @param centerOnData allow to center on displayed data. This parameter is ignored
     *                     if <code>callImport = true</code>. Suggested if <code>false</code> because
     *                     unexpected centering breaks usability.
     * @return {@code true} if data were correctly send
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    private static boolean sendPack(String action, Context context,
            PackWaypoints data, boolean callImport, boolean centerOnData)
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
     * Simple way how to send ArrayList<PackWaypoints> object over intent to Locus. Count that
     * intent in Android have some size limits so for larger data, use another method
     *
     * @param context actual {@link Context}
     * @param data    {@link ArrayList} of data that should be send to Locus
     * @return true if success
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    public static boolean sendPacks(Context context,
            List<PackWaypoints> data, ExtraAction extraAction)
            throws RequiredVersionMissingException {
        return sendPacks(LocusConst.ACTION_DISPLAY_DATA,
                context, data, extraAction == ExtraAction.IMPORT,
                extraAction == ExtraAction.CENTER);
    }

    public static boolean sendPacksSilent(Context context,
            List<PackWaypoints> data, boolean centerOnData)
            throws RequiredVersionMissingException {
        return sendPacks(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                context, data, false, centerOnData);
    }

    private static boolean sendPacks(String action, Context context,
            List<PackWaypoints> data, boolean callImport, boolean centerOnData)
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
     * Allow to send data to locus, by storing serialized version of data into file. This method
     * can have advantage over cursor in simplicity of implementation and also that filesize is
     * not limited as in Cursor method.<br></br />
     * On second case, <s>need permission for disk access</s> (not needed in latest android) and should
     * be slower due to IO operations.<br></br />
     * Be careful about size of data. This method can cause OutOfMemory error on Locus side if data
     * are too big, because all needs to be loaded at once before process.
     *
     * @param context     existing {@link Context}
     * @param data        data to send
     * @param filepath    path where data should be stored
     * @param extraAction extra action that should happen after Locus reads data
     * @return {@code true} if data were correctly send, otherwise {@code false}
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    public static boolean sendPacksFile(Context context,
            List<PackWaypoints> data, String filepath, ExtraAction extraAction)
            throws RequiredVersionMissingException {
        return sendPacksFile(LocusConst.ACTION_DISPLAY_DATA,
                context, data, filepath, extraAction == ExtraAction.IMPORT,
                extraAction == ExtraAction.CENTER);
    }

    public static boolean sendPacksFileSilent(Context context,
            List<PackWaypoints> data, String filepath, boolean centerOnData)
            throws RequiredVersionMissingException {
        return sendPacksFile(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                context, data, filepath, false, centerOnData);
    }

    /**
     * Main function for sending pack of points over temporary stored file.
     *
     * @param action       action we wants to perform
     * @param context      current context
     * @param data         data to send
     * @param filepath     path where file will be temporary stored
     * @param callImport   {@code true} to call import after load in Locus
     * @param centerOnData {@code true} to center on data
     * @return {@code true} if request was correctly send
     * @throws RequiredVersionMissingException exception in case of missing required app version
     */
    private static boolean sendPacksFile(String action, Context context,
            List<PackWaypoints> data, String filepath, boolean callImport, boolean centerOnData)
            throws RequiredVersionMissingException {
        if (sendDataWriteOnCard(data, filepath)) {
            Intent intent = new Intent();
            intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH, filepath);
            return sendData(action, context, intent, callImport, centerOnData);
        } else {
            return false;
        }
    }

    private static boolean sendDataWriteOnCard(List<PackWaypoints> data, String filepath) {
        if (data == null || data.size() == 0)
            return false;

        DataOutputStream dos = null;
        try {
            File file = new File(filepath);
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
            Logger.logE(TAG, "sendDataWriteOnCard(" + filepath + ", " + data + ")", e);
            return false;
        } finally {
            Utils.closeStream(dos);
        }
    }

    /**
     * Invert method to {@link #sendPacksFile(Context, List, String, ExtraAction)} . This load serialized data
     * from file object.
     *
     * @param filepath path to file
     * @return loaded pack of points
     */
    @SuppressWarnings("unchecked")
    public static List<PackWaypoints> readDataWriteOnCard(String filepath) {
        // check file
        File file = new File(filepath);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        DataInputStream dis = null;
        try {

            dis = new DataInputStream(new FileInputStream(file));
            return Storable.readList(PackWaypoints.class, dis);
        } catch (Exception e) {
            Logger.logE(TAG, "readDataWriteOnCard(" + filepath + ")", e);
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
    public void removePackFromLocus(Context ctx, String packName)
            throws RequiredVersionMissingException {
        // check data
        if (packName == null || packName.length() == 0) {
            return;
        }

        // create empty pack
        PackWaypoints pw = new PackWaypoints(packName);

        // create and send intent
        Intent intent = new Intent();
        intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_DATA, pw.getAsBytes());
        sendPackSilent(ctx, pw, false);
    }
}
