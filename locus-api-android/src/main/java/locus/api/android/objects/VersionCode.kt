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

/**
 * Locus versions used in this API
 *
 * @param vcFree version code for Locus Map Free
 * @param vcPro  version code for Locus Map Pro
 * @param vcGis  version code for Locus GIS
 */
enum class VersionCode(
        /**
         * Version code for a Free version.
         */
        val vcFree: Int,
        /**
         * Version code for a Pro version.
         */
        val vcPro: Int,
        /**
         * Version code for a GIS version.
         */
        val vcGis: Int) {

    /**
     *
     *  * Base Locus versions.
     *
     * Locus Free/Pro 2.7.0, Gis 1.0.0 (235, 235, 1)
     */
    UPDATE_01(235, 235, 0),
    /**
     *
     *  * Control of track recording
     *  * Ability to add/hide Circle map items
     *
     * Locus Free/Pro 2.8.4 (242, 242, 1)
     */
    UPDATE_02(242, 242, 0),
    /**
     *
     *  * Get waypoint by ID
     *
     * Locus Free/Pro 2.17.3 (269, 269, 1)
     */
    UPDATE_03(269, 269, 0),
    /**
     *
     *  * Added MapPreview
     *
     * Locus Free/Pro 2.20.2.4 (278, 278, 1)
     */
    UPDATE_04(278, 278, 0),
    /**
     *
     *  * Added Compute track service
     *  * Added Geocaching field notes support
     * <br></br>
     * Locus Free/Pro 3.2.0 (296) <br></br>
     * Locus GIS (no news)
     */
    UPDATE_05(296, 296, 0),
    /**
     *
     *  * Added request on purchase state of item in Locus Store
     * <br></br>
     * Locus Free/Pro 3.2.3(311), GIS 0.5.3 (5)<br></br>
     * Locus GIS (no news)
     */
    UPDATE_06(311, 311, 5),
    /**
     *
     *  * Added request to display Point detail screen
     * <br></br>
     * Locus Free/Pro 3.3.0(317)<br></br>
     */
    UPDATE_07(317, 317, 0),
    /**
     *
     *  * Added "Navigation" on address
     * <br></br>
     * Locus Free/Pro 3.5.3(343)<br></br>
     */
    UPDATE_08(343, 343, 0),
    /**
     *
     *  * Added "Track record profiles"
     * <br></br>
     * Locus Free/Pro 3.8.0(357)<br></br>
     */
    UPDATE_09(357, 357, 0),
    /**
     *
     *  * Added "Get track by ID"
     * <br></br>
     * Locus Free/Pro 3.9.0(370)<br></br>
     */
    UPDATE_10(370, 370, 0),
    /**
     *
     *  * Added parameters to Locus Info
     * <br></br>
     * Locus Free/Pro 3.9.2(380)<br></br>
     */
    UPDATE_11(380, 380, 0),
    /**
     *
     *  * Added parameters to display certain item in Locus Store
     * <br></br>
     * Locus Free/Pro 3.13.0(421)<br></br>
     */
    UPDATE_12(421, 421, 0),
    /**
     *
     *  * Added direct request on LocusInfo and UpdateContainer over ActionTools
     * <br></br>
     * Locus Free/Pro 3.25.6(652)<br></br>
     */
    UPDATE_13(652, 652, 0),
    /**
     *
     *  * New version of "Get map preview" system
     * <br></br>
     * Locus Free/Pro 3.29.0(684)<br></br>
     */
    UPDATE_14(684, 684, 0),
    /**
     *
     *  * chg: send pack file via FileProvider
     *  * add: ActionBasic.getPointsId for search in points by area
     *  * add: option to navigate/guide to point defined by it's ID
     * <br></br>
     * Locus Free/Pro 3.36.0(796)<br></br>
     */
    UPDATE_15(796, 796, 0),
    /**
     *
     *  * add: ActionBasic.getTrackInFormat, get track exported in defined format
     * <br></br>
     * Locus Free/Pro 3.37.0(815)<br></br>
     */
    UPDATE_16(815, 815, 0)
}