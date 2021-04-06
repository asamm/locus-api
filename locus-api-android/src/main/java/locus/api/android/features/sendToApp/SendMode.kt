/****************************************************************************
 *
 * Created by menion on 06.04.2021.
 * Copyright (c) 2021. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/
package locus.api.android.features.sendToApp

import locus.api.android.utils.LocusConst

/**
 * Mode that define how data are send into the Locus application.
 */
sealed class SendMode(val action: String) {

    /**
     * Basic mode. Data are send over direct intent. App is started if not running and request
     * is handled once basic app initialization is done.
     *
     * @param centerOnData apply centering mechanism, so move & zoom map to display loaded content.
     */
    class Basic(val centerOnData: Boolean = false)
        : SendMode(LocusConst.ACTION_DISPLAY_DATA)

    /**
     * Silent mode. Data are send over broadcast. In case, app is not running, request is lost
     * and no data are delivered.
     */
    class Silent
        : SendMode(LocusConst.ACTION_DISPLAY_DATA_SILENTLY)

    /**
     * Import mode is based on 'BASIC' mode, but after app is started, import is called instead
     * of basic display on the map.
     */
    class Import
        : SendMode(LocusConst.ACTION_DISPLAY_DATA)
}