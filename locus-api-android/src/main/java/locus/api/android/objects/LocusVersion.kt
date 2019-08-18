/****************************************************************************
 *
 * Created by menion on 18.08.2019.
 * Copyright (c) 2019. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/

package locus.api.android.objects

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException

/**
 * Main container that keeps information about existing versions in
 * whole current Android system.
 *
 * @param packageName name of application package
 * @param versionName name of version
 * @param versionCode unique ID code
 */
class LocusVersion(packageName: String, versionName: String, versionCode: Int) : Storable() {

    /**
     * Empty constructor for `Storable` instance.
     */
    constructor() : this("", "", 0)

    /**
     * Version package name defined in manifest of Locus app. It is main
     * parameter for calls to Locus.
     */
    var packageName: String = packageName
        private set
    /**
     * Version name defined in manifest of Locus app. This is just textual
     * information of versionCode.
     */
    var versionName: String = versionName
        private set
    /**
     * Version code defined in manifest of Locus app. Core information used
     * for checks, if Locus versions already has requested feature.
     */
    var versionCode: Int = versionCode
        private set

    /**
     * Test if current app is "Locus Map Free" flavor.
     */
    val isVersionFree: Boolean
        get() = !isVersionPro && !isVersionGis

    /**
     * Test if current app is "Locus Map Pro" flavor.
     */
    val isVersionPro: Boolean
        get() = packageName.contains(".pro")

    /**
     * Test if current app is "Locus GIS" flavor.
     */
    val isVersionGis: Boolean
        get() = packageName.contains(".gis")

    /**
     * Get ClassName for the main application activity (usually screen with map).
     * @return class name
     */
    val mainActivityClassName: String
        get() = if (isVersionFree || isVersionPro) {
            "com.asamm.locus.basic.MainActivityBasic"
        } else if (isVersionGis) {
            "com.asamm.locus.gis.core.MainActivityGis"
        } else {
            ""
        }

    /**
     * Check if current version is valid compare to required VersionCode
     *
     * @param code code of required version
     * @return `true` if version, compared to code, is valid
     */
    fun isVersionValid(code: VersionCode): Boolean {
        return when {
            isVersionFree -> code.vcFree != 0 && versionCode >= code.vcFree
            isVersionPro -> code.vcPro != 0 && versionCode >= code.vcPro
            isVersionGis -> code.vcGis != 0 && versionCode >= code.vcGis
            else -> false
        }
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 0
    }

    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        packageName = dr.readString()
        versionName = dr.readString()
        versionCode = dr.readInt()
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeString(packageName)
        dw.writeString(versionName)
        dw.writeInt(versionCode)
    }
}