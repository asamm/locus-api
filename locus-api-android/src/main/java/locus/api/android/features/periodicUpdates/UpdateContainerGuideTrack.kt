/****************************************************************************
 *
 * Created by menion on 18/03/2019.
 * Copyright (c) 2019. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/

package locus.api.android.features.periodicUpdates

import locus.api.objects.enums.PointRteAction
import locus.api.objects.extra.Location

/**
 * Container with active navigation/guidance along the track.
 */
class UpdateContainerGuideTrack internal constructor(
        /**
         * Type of active guidance.
         * Type is defined as constant in [UpdateContainer] class.
         */
        val type: Int,
        /**
         * 'trackId' of guide target.
         */
        val targetId: Long,
        /**
         * Name of the point, where current guiding arrow points.
         */
        val targetName: String,
        /**
         * Location of target point.
         */
        val targetLoc: Location,
        /**
         * Distance to target point [m].
         */
        val targetDistance: Double,
        /**
         * Azimuth from current location to target point [°].
         */
        val targetAzimuth: Float,
        /**
         * Get angle to current point. This angle is computed as current azimuth to point
         * minus current user heading [°].
         */
        val targetAngle: Float,
        /**
         * Expected time to current closest track point [ms].
         */
        val targetTime: Long,

        /**
         * Flag if current track guide is valid, this means that active guidance is not out of route and match
         * all necessary parameters.
         */
        val isValid: Boolean,
        /**
         * Distance from start of track to current place [m].
         */
        val distFromStart: Double,
        /**
         * Distance from current place to end of track [m].
         */
        val distToFinish: Double,
        /**
         * Expected time to finish [ms].
         */
        val timeToFinish: Long,
        /**
         * First next navigation point.
         */
        val navPointFirst: NavPoint?,
        /**
         * Second navigation point.
         */
        val navPointSecond: NavPoint?) {

    /**
     * Container of navigation point and metadata related to current active navigation/guidance.
     */
    class NavPoint internal constructor(
            /**
             * Name of current navigation point.
             */
            val name: String,
            /**
             * Location of current navigation point.
             */
            val location: Location,
            /**
             * Navigation action for current navigation point.
             */
            val action: PointRteAction,
            /**
             * Distance from current location to current navigation point [m].
             */
            val distance: Double,
            /**
             * Time from current location to current navigation point [ms].
             */
            val time: Long,
            /**
             * Extra information related to current navigation point. Usually text represent
             * street name or user-defined content.
             */
            val extraInfo: String)
}