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

package locus.api.android.utils.exceptions;

import locus.api.android.utils.LocusUtils.VersionCode;

public class RequiredVersionMissingException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String mistake;
	
	public RequiredVersionMissingException(int version) {
		this(version, version);
	}
	
	public RequiredVersionMissingException(int versionFree, int versionPro) {
		super("Required version: Free (" + versionFree + "), " +
				"or Pro (" +  versionPro + "), not installed!");
	}
	
	public RequiredVersionMissingException(VersionCode vc) {
		super("Required version: " +
                "Free (" + getVersionAsText(vc.vcFree) + "), or " +
				"Pro (" + getVersionAsText(vc.vcPro) + "), or " +
				"Gis (" + getVersionAsText(vc.vcGis) + "), not installed!");
	}

    private static String getVersionAsText(int code) {
        if (code == 0) {
            return "Not supported";
        } else {
            return Integer.toString(code);
        }
    }
	
	public RequiredVersionMissingException(String packageName, int version) {
		super(String.format("Required application: '%s', version: '%s', not installed",
				packageName, version));
	}
	
	public String getError() {
		return mistake;
	}
}
