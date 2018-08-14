package locus.api.android.features.periodicUpdates;

/**
 * List of variables used for PeriodicUpdates
 * <br><br>
 * Do not use these constants directly. All should be handled by 
 * {@link PeriodicUpdatesHandler PeriodicUpdate} and it's {@link UpdateContainer UpdateContainer} object
 * @author menion
 *
 */
class PeriodicUpdatesConst {

	static final String VAR_NO_ACTION 											= "-1";
	
	// LOCATION, GPS, BASIC VALUES
	
	static final String VAR_B_MY_LOCATION_ON 									= "1000";
	
	static final String VAR_LOC_MY_LOCATION 									= "1001";
	static final String VAR_I_GPS_SATS_USED 									= "1005";
	static final String VAR_I_GPS_SATS_ALL 										= "1006";
	static final String VAR_F_DECLINATION 										= "1007";
	static final String VAR_F_ORIENT_HEADING 									= "1008";
	static final String VAR_F_ORIENT_HEADING_OPPOSIT							= "1009";
    static final String VAR_F_ORIENT_COURSE 									= "1016";
	static final String VAR_F_ORIENT_PITCH 										= "1010";
	static final String VAR_F_ORIENT_ROLL 										= "1011";
	static final String VAR_F_ORIENT_GPS_ANGLE 									= "1013";
	static final String VAR_F_SPEED_VERTICAL 									= "1014";
	static final String VAR_F_SLOPE 											= "1015";

	// MAP STUFF
	
	static final String VAR_B_MAP_VISIBLE 										= "1300";
	static final String VAR_F_MAP_ROTATE 										= "1301";
	static final String VAR_LOC_MAP_CENTER 										= "1302";
	static final String VAR_LOC_MAP_BBOX_TOP_LEFT 								= "1303";
	static final String VAR_LOC_MAP_BBOX_BOTTOM_RIGHT 							= "1304";
	static final String VAR_I_MAP_ZOOM_LEVEL 									= "1305";
	static final String VAR_B_MAP_USER_TOUCHES 									= "1306";

	// TRACK RECORDING PART (last 1216)
	
	static final String VAR_B_REC_RECORDING 									= "1200";
	static final String VAR_B_REC_PAUSED 										= "1201";
    static final String VAR_S_REC_PROFILE_NAME 									= "1216";
	static final String VAR_L_REC_TRACK_STATS 									= "1217";
	static final String VAR_D_REC_DIST											= "1202";
	static final String VAR_D_REC_DIST_DOWNHILL 								= "1203";
	static final String VAR_D_REC_DIST_UPHILL 									= "1204";
	static final String VAR_F_REC_ALT_MIN 										= "1205";
	static final String VAR_F_REC_ALT_MAX 										= "1206";
	static final String VAR_F_REC_ALT_DOWNHILL 									= "1207";
	static final String VAR_F_REC_ALT_UPHILL 									= "1208";
	static final String VAR_F_REC_ALT_CUMULATIVE 								= "1209";
	static final String VAR_L_REC_TIME 											= "1210";
	static final String VAR_L_REC_TIME_MOVE 									= "1211";
	static final String VAR_F_REC_SPEED_AVG 									= "1212";
	static final String VAR_F_REC_SPEED_AVG_MOVE 								= "1213";
	static final String VAR_F_REC_SPEED_MAX 									= "1214";
	static final String VAR_I_REC_POINTS 										= "1215";

	// GUIDING PART (last 1421)
	
    static final String VAR_I_GUIDE_TYPE                                        = "1410";
	static final String VAR_S_GUIDE_WPT_NAME                                    = "1401";
	static final String VAR_LOC_GUIDE_WPT                                       = "1402";
	static final String VAR_D_GUIDE_WPT_DIST                                    = "1403";
	static final String VAR_F_GUIDE_WPT_AZIM                                    = "1405";
	static final String VAR_F_GUIDE_WPT_ANGLE                                   = "1406";
	static final String VAR_L_GUIDE_WPT_TIME                                    = "1407";

	static final String VAR_D_GUIDE_DIST_FROM_START                             = "1409";
	static final String VAR_D_GUIDE_DIST_TO_FINISH                              = "1404";
	static final String VAR_L_GUIDE_TIME_TO_FINISH                              = "1408";
	static final String VAR_L_GUIDE_VALID										= "1421";

    static final String VAR_S_GUIDE_NAV_POINT1_NAME                             = "1411";
    static final String VAR_LOC_GUIDE_NAV_POINT1_LOC                            = "1412";
    static final String VAR_D_GUIDE_NAV_POINT1_DIST                             = "1413";
    static final String VAR_L_GUIDE_NAV_POINT1_TIME                             = "1414";
    static final String VAR_L_GUIDE_NAV_POINT1_ACTION                           = "1419";
    static final String VAR_S_GUIDE_NAV_POINT2_NAME                             = "1415";
    static final String VAR_LOC_GUIDE_NAV_POINT2_LOC                            = "1416";
    static final String VAR_D_GUIDE_NAV_POINT2_DIST                             = "1417";
    static final String VAR_L_GUIDE_NAV_POINT2_TIME                             = "1418";
    static final String VAR_L_GUIDE_NAV_POINT2_ACTION                           = "1420";
	
	// VARIOUS
	
	static final String VAR_I_DEVICE_BATTERY_VALUE 								= "1500";
	static final String VAR_F_DEVICE_BATTERY_TEMPERATURE 						= "1501";
	static final String VAR_S_ACTIVE_DASHBOARD_ID								= "1502";
	static final String VAR_S_ACTIVE_LIVE_TRACK_ID								= "1503";
}
