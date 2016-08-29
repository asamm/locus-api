package locus.api.android;

import locus.api.android.objects.PackWaypoints;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.LocusUtils.LocusVersion;
import locus.api.android.utils.LocusUtils.VersionCode;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.utils.Logger;
import android.content.Context;
import android.content.Intent;

public class ActionDisplay {
	
	private static final String TAG = "ActionDisplay";
	
	public enum ExtraAction {
		NONE, CENTER, IMPORT
	}
	
	/**************************************************/
	/*                 PRIVATE CALLS                  */
	/**************************************************/
	
	protected static boolean sendData(String action, Context context, Intent intent,
			boolean callImport, boolean center) throws RequiredVersionMissingException {
		return sendData(action, context, intent, 
				callImport, center, VersionCode.UPDATE_01);
	}
	
	protected static boolean sendData(String action, Context context, Intent intent,
			boolean callImport, boolean center, VersionCode vc) throws RequiredVersionMissingException {
		if (!LocusUtils.isLocusAvailable(context, vc.vcFree, vc.vcPro, 0)) {
			throw new RequiredVersionMissingException(vc.vcFree);
		}
		
		// check intent firstly
		if (!hasData(intent)) {
			Logger.logW(TAG, "Intent 'null' or not contain any data");
			return false;
		}
		
		// create intent with right calling method
		intent.setAction(action);
	
		// set centering tag
		intent.putExtra(LocusConst.INTENT_EXTRA_CENTER_ON_DATA, center);
		
		// set import tag
		if (action.equals(LocusConst.ACTION_DISPLAY_DATA_SILENTLY)) {
			// send broadcast
			context.sendBroadcast(intent);
		} else {
			// set import tag
			intent.putExtra(LocusConst.INTENT_EXTRA_CALL_IMPORT, callImport);
			// finally start activity
			context.startActivity(intent);			
		}
		
		return true;
	}
	
	public static boolean hasData(Intent intent) {
		if (intent == null) {
			return false;
		}
		
		return !(
				intent.getByteArrayExtra(LocusConst.INTENT_EXTRA_POINTS_DATA) == null && 
				intent.getByteArrayExtra(LocusConst.INTENT_EXTRA_POINTS_DATA_ARRAY) == null &&
				intent.getStringExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH) == null &&
				intent.getByteArrayExtra(LocusConst.INTENT_EXTRA_TRACKS_SINGLE) == null &&
				intent.getByteArrayExtra(LocusConst.INTENT_EXTRA_TRACKS_MULTI) == null &&
				intent.getByteArrayExtra(LocusConst.INTENT_EXTRA_CIRCLES_MULTI) == null
				);
	}
	
	/**
	 * Method used for removing special objects from Locus map. Currently this method
	 * is used only for removing circles. If you want to remove any visible points or
	 * tracks you already send by this API, send simply new same intent (as the one
	 * with your data), but with empty list of data. So for example send again
	 * {@link PackWaypoints} that has same name!, but contain no data
	 * @param ctx current context
	 * @param extraName name of items to remove
	 * @param itemsId ID of item
	 * @return <code>true</code> if item was correctly send
	 * @throws RequiredVersionMissingException
	 */
	protected static boolean removeSpecialDataSilently(Context ctx, LocusVersion lv,
			String extraName, long[] itemsId) throws RequiredVersionMissingException {
		// check Locus version
		if (!lv.isVersionValid(VersionCode.UPDATE_02)) {
			throw new RequiredVersionMissingException(VersionCode.UPDATE_02);
		}
		
		// check intent firstly
		if (itemsId == null || itemsId.length == 0) {
			Logger.logW(TAG, "Intent 'null' or not contain any data");
			return false;
		}
		
		// create intent with right calling method
		Intent intent = new Intent(LocusConst.ACTION_REMOVE_DATA_SILENTLY);
		intent.setPackage(lv.getPackageName());
		intent.putExtra(extraName, itemsId);
		
		// set import tag
		ctx.sendBroadcast(intent);
		return true;
	}
}
