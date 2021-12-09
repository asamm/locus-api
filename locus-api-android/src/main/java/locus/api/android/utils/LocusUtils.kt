package locus.api.android.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import locus.api.android.ActionBasics
import locus.api.android.objects.LocusVersion
import locus.api.android.objects.VersionCode
import locus.api.objects.extra.Location
import locus.api.objects.geoData.Point
import locus.api.utils.Logger
import java.util.*

/**
 * Close opened cursor quietly (it means, no report in case of problem).
 */
fun Cursor.closeQuietly() {
    try {
        close()
    } catch (e: Exception) {
        Logger.logE("Utils", "closeQuietly(), e: $e")
        e.printStackTrace()
    }
}

object LocusUtils {

    // tag for logger
    private const val TAG = "LocusUtils"

    /**
     * Get all available app packages supported by Locus API system.
     */
    private val packageNames: Array<String>
        get() = arrayOf(
                "menion.android.locus",
                "menion.android.locus.free.amazon",
                "menion.android.locus.free.samsung",
                "menion.android.locus.pro",
                "menion.android.locus.pro.amazon",
                "menion.android.locus.pro.asamm",
                "menion.android.locus.pro.computerBild")

    /**
     * Search for existing (and better also running) version of Locus. This function
     * search for Locus, but also grab data from all instances. So it's suggested
     * not to use this function too often. It is also possible to define minimum versionCode.
     *
     * @param ctx current context
     * @param minVersionCode minimal version code of Locus
     * @return active version
     */
    fun getActiveVersion(ctx: Context, minVersionCode: Int): LocusVersion? {
        return getActiveVersion(ctx, minVersionCode, minVersionCode, minVersionCode)
    }

    /**
     * Search for existing (and better also running) version of Locus. This function
     * search for Locus, but also grab data from all instances. So it's suggested
     * not to use this function too often. It is also possible to define minimum versionCode.
     *
     * @param ctx current context
     * @param vc  version code
     * @return active version
     */
    @JvmOverloads
    fun getActiveVersion(ctx: Context, vc: VersionCode = VersionCode.UPDATE_01): LocusVersion? {
        return getActiveVersion(ctx, vc.vcFree, vc.vcPro, vc.vcGis)
    }

    /**
     * Search for existing (and better also running) version of Locus. This function
     * search for Locus, but also grab data from all instances. So it's suggested
     * not to use this function too often. It is also possible to define minimum versionCode.
     *
     * @param ctx current context
     * @param minLocusMapFree minimal version code of Locus Map Free
     * @param minLocusMapPro minimal version code of Locus Map Pro
     * @param minLocusGis minimal version code of Locus GIS
     * @return active version
     */
    private fun getActiveVersion(ctx: Context,
            minLocusMapFree: Int, minLocusMapPro: Int, minLocusGis: Int): LocusVersion? {
        // get valid Locus version for any actions
        val versions = getAvailableVersions(ctx)
        if (versions.isEmpty()) {
            return null
        }

        // search for optimal version
        var backupVersion: LocusVersion? = null
        var backupLastActive = 0L
        for (element in versions) {
            // get and test version
            if (element.isVersionFree) {
                if (minLocusMapFree <= 0 || element.versionCode < minLocusMapFree) {
                    continue
                }
            } else if (element.isVersionPro) {
                if (minLocusMapPro <= 0 || element.versionCode < minLocusMapPro) {
                    continue
                }
            } else if (element.isVersionGis) {
                if (minLocusGis <= 0 || element.versionCode < minLocusGis) {
                    continue
                }
            } else {
                // unknown version
                continue
            }

            // check if Locus runs and if so, set it as active version
            val li = ActionBasics.getLocusInfo(ctx, element)
                    ?: continue

            // backup valid version
            if (backupVersion == null || li.lastActive >= backupLastActive) {
                backupVersion = element
                backupLastActive = li.lastActive
            }

            // check if is running
            if (li.isRunning) {
                return element
            }
        }

        // if version is not set, use backup
        return backupVersion ?: versions[0]
    }

    /**
     * Search through whole Android system and search for existing versions of Locus
     *
     * @param ctx current context
     * @return list of available versions
     */
    fun getAvailableVersions(ctx: Context): List<LocusVersion> {
        // prepare container for existing versions
        val versions = ArrayList<LocusVersion>()

        // get information about version from supported list (if app is installed)
        val pm = ctx.packageManager
        for (pn in packageNames) {
            try {
                val packageInfo = pm.getPackageInfo(pn, 0)
                val appInfo = pm.getApplicationInfo(pn, 0)
                val lv = createLocusVersion(ctx, appInfo.packageName)
                if (lv != null) {
                    versions.add(lv)
                }
            } catch (ignored: PackageManager.NameNotFoundException) {
//                Logger.logD(TAG, "getAvailableVersions($ctx), " +
//                        "e: $ignored")
            }
        }

        // return result
        return versions
    }

    /**
     * Get LocusVersion for specific, known packageName. This method should not be used
     * in common work-flow. Better is receive list of versions and pick correct, or create
     * LocusVersion from received intent.
     *
     * @param ctx         current context
     * @param packageName Locus package name
     * @return generated Locus version
     */
    fun createLocusVersion(ctx: Context, packageName: String?): LocusVersion? {
        try {
            // check package name
            if (packageName == null
                    || packageName.isEmpty()
                    || !packageName.startsWith("menion.android.locus")) {
                return null
            }

            // get information about version
            val pm = ctx.packageManager
            val info = pm.getPackageInfo(packageName, 0) ?: return null

            // finally add item to list
            return LocusVersion(packageName, info.versionName, info.versionCode)
        } catch (e: Exception) {
            Logger.logE(TAG, "getLocusVersion($ctx, $packageName)", e)
            return null
        }
    }

    /**
     * Get LocusVersion based on received Intent object. Since Locus version 279,
     * all Intents contains valid `packageName`, so it's simple possible
     * to get valid LocusVersion object. If user has older version of Locus, this call
     * just return first available version.
     *
     * @param ctx    current context
     * @param intent received intent from Locus
     * @return generated Locus version
     */
    fun createLocusVersion(ctx: Context?, intent: Intent?): LocusVersion? {
        // check parameters
        if (ctx == null || intent == null) {
            return null
        }

        val packageName = intent.getStringExtra(LocusConst.INTENT_EXTRA_PACKAGE_NAME)
        return if (packageName != null && packageName.isNotEmpty()) {
            createLocusVersion(ctx, packageName)
        } else {
            createLocusVersion(ctx)
        }
    }

    /**
     * Old method that returns 'random' existing version of Locus installed on this
     * device. In most cases, users has only one version, so it's not a big problem.
     * But in cases, where exists more then one version, this method may cause troubles
     * so it's not recommended to use it.
     *
     * @param ctx current context
     * @return generated Locus version
     */
    @Deprecated("")
    fun createLocusVersion(ctx: Context?): LocusVersion? {
        // check parameters
        if (ctx == null) {
            return null
        }

        // older versions of Locus do not send package name in it's intents.
        // So we return closest valid Locus version (Pro/Free)
        Logger.logW(TAG, "getLocusVersion(" + ctx + "), " +
                "Warning: old version of Locus: Correct package name is not known!")
        val versions = getAvailableVersions(ctx)
        for (element in versions) {
            if (element.isVersionFree || element.isVersionPro) {
                return element
            }
        }
        return null
    }

    /**
     * Returns `true` if Locus in required version is installed.
     *
     * @param ctx actual [Context]
     * @param vc  required version code
     * @return `true` if Locus is available
     */
    @JvmOverloads
    fun isLocusAvailable(ctx: Context, vc: VersionCode = VersionCode.UPDATE_01): Boolean {
        return isLocusAvailable(ctx, vc.vcFree, vc.vcPro, vc.vcGis)
    }

    /**
     * Check if Locus in required version is installed on current system.
     *
     * @param ctx         Context
     * @param versionFree minimum required version of
     * Locus Free (or '0' if we don't want Locus Free)
     * @param versionPro  minimum required version of
     * Locus Pro (or '0' if we don't want Locus Pro)
     * @param versionGis  minimum required version of
     * Locus Gis (or '0' if we don't want Locus Gis)
     * @return `true` if required Locus is installed
     */
    fun isLocusAvailable(ctx: Context,
            versionFree: Int, versionPro: Int, versionGis: Int): Boolean {
        val versions = getAvailableVersions(ctx)
        for (element in versions) {
            val lv = element
            if (lv.isVersionFree && versionFree > 0 &&
                    lv.versionCode >= versionFree) {
                return true
            }
            if (lv.isVersionPro && versionPro > 0 &&
                    lv.versionCode >= versionPro) {
                return true
            }
            if (lv.isVersionGis && versionGis > 0 &&
                    lv.versionCode >= versionGis) {
                return true
            }
        }
        return false
    }

    /**
     * Check if LocusVersion if basic 'Locus Free' or 'Locus Pro' in required
     * minimal version.
     *
     * @param lv         version of Locus
     * @param minVersion required minimal version
     * @return `true` if LocusVersion is Free/Pro version of
     * Locus, otherwise returns `false`
     */
    fun isLocusFreePro(lv: LocusVersion?, minVersion: Int): Boolean {
        // check parameters
        if (lv == null) {
            return false
        }

        // check on versions
        return if (lv.isVersionFree && lv.versionCode >= minVersion) {
            true
        } else lv.isVersionPro && lv.versionCode >= minVersion
    }

    /**
     * Start intent that allows install Free version of Locus
     *
     * @param ctx current context
     */
    fun callInstallLocus(ctx: Context) {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(
                "http://market.android.com/details?id=menion.android.locus")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    /**
     * Start existing Locus installation in device.
     *
     * @param ctx current context
     */
    fun callStartLocusMap(ctx: Context) {
        ctx.startActivity(Intent("com.asamm.locus.map.START_APP").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    //*************************************************
    // RESPONSE HANDLING
    //*************************************************

    /**
     * Prepare intent that may be used for sending waypoint back to Locus application.
     *
     * @param pt           waypoint to send back
     * @param overridePoint `true` to overwrite original point in application
     * @return generated intent
     */
    fun prepareResultExtraOnDisplayIntent(pt: Point, overridePoint: Boolean): Intent {
        return Intent().apply {
            IntentHelper.addPointToIntent(this, pt)
            putExtra(LocusConst.INTENT_EXTRA_POINT_OVERWRITE, overridePoint)
        }
    }

    /**
     * Send explicit intent to Locus app defined by it's type and version.
     *
     * @param ctx    current context
     * @param intent intent to send
     * @param lv     version of receiver
     */
    fun sendBroadcast(ctx: Context, intent: Intent, lv: LocusVersion) {
        // define package
        intent.setPackage(lv.packageName)

        // send broadcast
        ctx.sendBroadcast(intent)
    }

    //*************************************************
    // VARIOUS TOOLS
    //*************************************************

    /**
     * Convert a Location object from Android to Locus format
     *
     * @param oldLoc location in Android object
     * @return new Locus object
     */
    fun convertToL(oldLoc: android.location.Location): Location {
        val loc = Location()
        loc.provider = oldLoc.provider
        loc.longitude = oldLoc.longitude
        loc.latitude = oldLoc.latitude
        loc.time = oldLoc.time
        if (oldLoc.hasAccuracy()) {
            loc.accuracyHor = oldLoc.accuracy
        }
        if (oldLoc.hasAltitude()) {
            loc.altitude = oldLoc.altitude
        }
        if (oldLoc.hasBearing()) {
            loc.bearing = oldLoc.bearing
        }
        if (oldLoc.hasSpeed()) {
            loc.speed = oldLoc.speed
        }
        return loc
    }

    /**
     * Convert a Location object from Locus to Android format
     *
     * @param oldLoc location in Locus object
     * @return converted location to Android object
     */
    fun convertToA(oldLoc: Location): android.location.Location {
        val loc = android.location.Location(oldLoc.provider)
        loc.longitude = oldLoc.longitude
        loc.latitude = oldLoc.latitude
        loc.time = oldLoc.time
        if (oldLoc.hasAccuracyHor) {
            loc.accuracy = oldLoc.accuracyHor
        }
        if (oldLoc.hasAltitude) {
            loc.altitude = oldLoc.altitude
        }
        if (oldLoc.hasBearing) {
            loc.bearing = oldLoc.bearing
        }
        if (oldLoc.hasSpeed) {
            loc.speed = oldLoc.speed
        }
        return loc
    }

    /**
     * General check if 'any' app defined by it's package name is available in system.
     *
     * @param ctx         current context
     * @param packageName package name of tested app
     * @param version     required application version
     * @return `true` if required application is available
     */
    fun isAppAvailable(ctx: Context, packageName: String, version: Int): Boolean {
        try {
            return ctx.packageManager.getPackageInfo(packageName, 0)
                    ?.versionCode ?: -1 >= version
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
    }
}