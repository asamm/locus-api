/**
 * Created by menion on 15/10/2016.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.objects.extra

/**
 * Possible actions for Route navigation point.
 *
 * @param id id of item
 */
enum class PointRteAction constructor(
        /**
         * Unique ID of action
         */
        val id: Int,
        /**
         * Text ID representation
         */
        val textId: String) {

    /**
     * Special action that serve as fallback if no valid action is defined/found.
     */
    UNDEFINED(Integer.MIN_VALUE, "undefined"),
    /**
     * No maneuver occurs here.
     */
    NO_MANEUVER(0, "no_maneuver"),
    /**
     * Continue straight.
     */
    CONTINUE_STRAIGHT(1, "straight"),
    /**
     * No maneuver occurs here. Road name changes.
     */
    NO_MANEUVER_NAME_CHANGE(2, "name_change"),
    /**
     * Make a slight left.
     */
    LEFT_SLIGHT(3, "left_slight"),
    /**
     * Turn left.
     */
    LEFT(4, "left"),
    /**
     * Make a sharp left.
     */
    LEFT_SHARP(5, "left_sharp"),
    /**
     * Make a slight right.
     */
    RIGHT_SLIGHT(6, "right_slight"),
    /**
     * Turn right.
     */
    RIGHT(7, "right"),
    /**
     * Make a sharp right.
     */
    RIGHT_SHARP(8, "right_sharp"),
    /**
     * Stay left.
     */
    STAY_LEFT(9, "stay_left"),
    /**
     * Stay right.
     */
    STAY_RIGHT(10, "stay_right"),
    /**
     * Stay straight.
     */
    STAY_STRAIGHT(11, "stay_straight"),
    /**
     * Make a U-turn.
     */
    U_TURN(12, "u-turn"),
    /**
     * Make a left U-turn.
     */
    U_TURN_LEFT(13, "u-turn_left"),
    /**
     * Make a right U-turn.
     */
    U_TURN_RIGHT(14, "u-turn_right"),
    /**
     * Exit left.
     */
    EXIT_LEFT(15, "exit_left"),
    /**
     * Exit right.
     */
    EXIT_RIGHT(16, "exit_right"),
    /**
     * Take the ramp on the left.
     */
    RAMP_ON_LEFT(17, "ramp_left"),
    /**
     * Take the ramp on the right.
     */
    RAMP_ON_RIGHT(18, "ramp_right"),
    /**
     * Take the ramp straight ahead.
     */
    RAMP_STRAIGHT(19, "ramp_straight"),
    /**
     * Merge left.
     */
    MERGE_LEFT(20, "merge_left"),
    /**
     * Merge right.
     */
    MERGE_RIGHT(21, "merge_right"),
    /**
     * Merge.
     */
    MERGE(22, "merge"),
    /**
     * Enter state/province.
     */
    ENTER_STATE(23, "enter_state"),
    /**
     * Arrive at your destination.
     */
    ARRIVE_DEST(24, "dest"),
    /**
     * Arrive at your destination on the left.
     */
    ARRIVE_DEST_LEFT(25, "dest_left"),
    /**
     * Arrive at your destination on the right.
     */
    ARRIVE_DEST_RIGHT(26, "dest_right"),
    /**
     * Enter the roundabout and take the 1st exit.
     */
    ROUNDABOUT_EXIT_1(27, "roundabout_e1"),
    /**
     * Enter the roundabout and take the 2nd exit.
     */
    ROUNDABOUT_EXIT_2(28, "roundabout_e2"),
    /**
     * Enter the roundabout and take the 3rd exit.
     */
    ROUNDABOUT_EXIT_3(29, "roundabout_e3"),
    /**
     * Enter the roundabout and take the 4th exit.
     */
    ROUNDABOUT_EXIT_4(30, "roundabout_e4"),
    /**
     * Enter the roundabout and take the 5th exit.
     */
    ROUNDABOUT_EXIT_5(31, "roundabout_e5"),
    /**
     * Enter the roundabout and take the 6th exit.
     */
    ROUNDABOUT_EXIT_6(32, "roundabout_e6"),
    /**
     * Enter the roundabout and take the 7th exit.
     */
    ROUNDABOUT_EXIT_7(33, "roundabout_e7"),
    /**
     * Enter the roundabout and take the 8th exit.
     */
    ROUNDABOUT_EXIT_8(34, "roundabout_e8"),
    /**
     * Pass POI.
     */
    PASS_PLACE(50, "pass_place");

    // HELP TOOLS
    companion object {

        // array of enums for optimized/faster access
        private val VALUES = values()

        /**
         * Get action defined by it's ID.
         *
         * @param id ID of required action
         * @return found action or 'null' if not found
         */
        fun getActionById(id: Int): PointRteAction {
            for (action in VALUES) {
                if (action.id == id) {
                    return action
                }
            }

            // return action not found
            return UNDEFINED
        }

        /**
         * Get turn instruction from text representation.
         *
         * @param text text to analyze
         * @return turn instruction
         */
        fun getActionByText(text: String?): PointRteAction {
            // check text
            if (text == null || text.isEmpty()) {
                return UNDEFINED
            }

            // test actions
            for (action in VALUES) {
                if (text.equals(action.textId, ignoreCase = true)) {
                    return action
                }
            }

            // test on some special cases
            return when (text.lowercase().trim { it <= ' ' }) {
                "turn-left" -> LEFT
                "turn-right" -> RIGHT
                else -> UNDEFINED
            }
        }

        /**
         * Get aciton for roundabouts, defined by number of exit.
         *
         * @param exitNo exit no. (supported values are 1 - 8)
         * @return action for roundabout
         */
        fun getActionRoundabout(exitNo: Int): PointRteAction {
            // check edge options
            if (exitNo < 1) {
                return ROUNDABOUT_EXIT_1
            } else if (exitNo > 8) {
                return ROUNDABOUT_EXIT_8
            }

            // return correct action
            return getActionById(ROUNDABOUT_EXIT_1.id - 1 + exitNo)
        }

        // ACTION FOR NAVIGATION ANGLES

        // maximum value for straight angle
        private const val ANGLE_NO_MAX = 30
        // maximum value for slight angle turn
        private const val ANGLE_SLIGHT_MAX = 45
        // maximum value for normal angle turn
        private const val ANGLE_REGULAR_MAX = 120
        // maximum value for hard angle turn
        private const val ANGLE_HARD_MAX = 170

        /**
         * Get angle for certain angle.
         *
         * @param angle computed angle
         * @return action
         */
        fun getActionByAngle(angle: Float): PointRteAction {
            return when {
                angle < ANGLE_NO_MAX -> CONTINUE_STRAIGHT
                angle < ANGLE_SLIGHT_MAX -> RIGHT_SLIGHT
                angle < ANGLE_REGULAR_MAX -> RIGHT
                angle < ANGLE_HARD_MAX -> RIGHT_SHARP
                angle < 180 -> U_TURN_RIGHT
                angle < 360 - ANGLE_HARD_MAX -> U_TURN_LEFT
                angle < 360 - ANGLE_REGULAR_MAX -> LEFT_SHARP
                angle < 360 - ANGLE_SLIGHT_MAX -> LEFT
                angle < 360 - ANGLE_NO_MAX -> LEFT_SLIGHT
                else -> CONTINUE_STRAIGHT
            }
        }
    }
}
