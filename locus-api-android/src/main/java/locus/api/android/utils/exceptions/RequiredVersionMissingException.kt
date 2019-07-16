/*
 * Copyright 2011, Asamm Software, s.r.o.
 *
 * This file is part of LocusAddonPublicLib.
 *
 * LocusAddonPublicLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LocusAddonPublicLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LocusAddonPublicLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package locus.api.android.utils.exceptions

import locus.api.android.utils.LocusUtils.VersionCode

class RequiredVersionMissingException : Exception {

    val error: String? = null

    constructor(version: Int) : this(version, version)

    constructor(versionFree: Int, versionPro: Int) : super("Required version: Free (" + versionFree + "), " +
            "or Pro (" + versionPro + "), not installed!")

    constructor(vc: VersionCode) : super("Required version: " +
            "Free (" + getVersionAsText(vc.vcFree) + "), or " +
            "Pro (" + getVersionAsText(vc.vcPro) + "), or " +
            "Gis (" + getVersionAsText(vc.vcGis) + "), not installed!")

    constructor(packageName: String, version: Int)
            : super(String.format("Required application: '%s', version: '%s', not installed", packageName, version))

    companion object {

        private const val serialVersionUID = 1L

        private fun getVersionAsText(code: Int): String {
            return if (code == 0) {
                "Not supported"
            } else {
                Integer.toString(code)
            }
        }
    }
}
