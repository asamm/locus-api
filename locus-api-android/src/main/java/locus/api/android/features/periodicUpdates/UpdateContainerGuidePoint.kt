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

import locus.api.objects.extra.Location

/**
 * Container with active guiding content to single point.
 */
class UpdateContainerGuidePoint internal constructor(
        /**
         * Type of active guidance.
         * Type is defined as constant in [UpdateContainer] class.
         */
        val type: Int,
        /**
         * 'pointId' of guide target.
         */
        val targetId: Long,
        /**
         * Name of target point.
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
         * Expected time to current point [ms].
         */
        val targetTime: Long)