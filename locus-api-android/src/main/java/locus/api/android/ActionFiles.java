package locus.api.android;

import java.io.File;

import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.LocusUtils.LocusVersion;
import locus.api.utils.Logger;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ActionFiles {

	private static final String TAG = ActionFiles.class.getSimpleName();
	
	/**
	 * Generic call to system for applications that can import your file.
	 * @param context
	 * @param file
	 * @return
	 */
	public static boolean importFileSystem(Context context, File file) {
		// check requirements
		if (file == null || !file.exists()) {
			return false;
		}
		
		// send Intent
    	Intent sendIntent = new Intent(Intent.ACTION_VIEW);
    	sendIntent.setDataAndType(Uri.fromFile(file), getMimeType(file));
    	context.startActivity(sendIntent);
    	return true;
	}

	/**
	 * Import GPX/KML files directly into Locus application. 
	 * Return false if file don't exist or Locus is not installed
	 * @param ctx
	 * @param file
	 */
	public static boolean importFileLocus(Context ctx, File file) {
		return importFileLocus(ctx, LocusUtils.getActiveVersion(ctx),
				file, true);
	}
	
	/**
	 * Import GPX/KML files directly into Locus application. 
	 * Return false if file don't exist or Locus is not installed
	 * @param ctx
	 * @param file
     * @param callImport
	 */
	public static boolean importFileLocus(Context ctx, LocusVersion lv, 
			File file, boolean callImport) {
		// check requirements
		if (!isReadyForImport(ctx, lv, file)) {
			Logger.logE(TAG, "importFileLocus(" + ctx + ", " + lv + ", " + file + ", " + callImport + "), " +
					"invalid input parameters. Import cannot be performed!");
			return false;
		}
		
		// create and sent Intent
    	Intent intent = new Intent(Intent.ACTION_VIEW);
    	intent.setClassName(lv.getPackageName(), "menion.android.locus.core.MainActivity");
    	intent.setDataAndType(Uri.fromFile(file), getMimeType(file));
    	intent.putExtra(LocusConst.INTENT_EXTRA_CALL_IMPORT, callImport);
    	ctx.startActivity(intent);
    	return true;
	}
	
	private static boolean isReadyForImport(Context context, LocusVersion lv, File file) {
		if (file == null || !file.exists() || lv == null) {
			return false;
		}
		return true;
	}
	
	private static String getMimeType(File file) {
		String name = file.getName();
		int index = name.lastIndexOf(".");
		if (index == -1) {
			return "*/*";
		}
		return "application/" + name.substring(index + 1);
	}
}
