package locus.api.android.utils

import android.text.Html

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Class that serve formatting units to defined formats.
 */
@Suppress("NAME_SHADOWING")
object UtilsFormat {

    // ALTITUDE

    const val VALUE_UNITS_ALTITUDE_METRES = 0
    const val VALUE_UNITS_ALTITUDE_FEET = 1

    // ANGLE

    const val VALUE_UNITS_ANGLE_DEGREE = 0
    const val VALUE_UNITS_ANGLE_ANGULAR_MIL = 1
    const val VALUE_UNITS_ANGLE_RUSSIAN_MIL = 2
    const val VALUE_UNITS_ANGLE_US_ARTILLERY_MIL = 3

    // AREA

    const val VALUE_UNITS_AREA_M_SQ = 0
    const val VALUE_UNITS_AREA_HA = 1
    const val VALUE_UNITS_AREA_KM_SQ = 2
    const val VALUE_UNITS_AREA_FT_SQ = 3
    const val VALUE_UNITS_AREA_YA_SQ = 4
    const val VALUE_UNITS_AREA_ACRE = 5
    const val VALUE_UNITS_AREA_MI_SQ = 6
    const val VALUE_UNITS_AREA_NM_SQ = 7

    // DISTANCE

    const val VALUE_UNITS_DISTANCE_ME_M = 0
    const val VALUE_UNITS_DISTANCE_ME_MKM = 1
    const val VALUE_UNITS_DISTANCE_IM_F = 2
    const val VALUE_UNITS_DISTANCE_IM_FM = 3
    const val VALUE_UNITS_DISTANCE_IM_Y = 4
    const val VALUE_UNITS_DISTANCE_IM_YM = 5
    const val VALUE_UNITS_DISTANCE_NA_MNMI = 6

    // ENERGY

    const val VALUE_UNITS_ENERGY_KJ = 0
    const val VALUE_UNITS_ENERGY_KCAL = 1

    // SLOPE

    const val VALUE_UNITS_SLOPE_PERCENT = 0
    const val VALUE_UNITS_SLOPE_DEGREE = 1

    // SPEED

    const val VALUE_UNITS_SPEED_KMH = 0
    const val VALUE_UNITS_SPEED_MILH = 1
    const val VALUE_UNITS_SPEED_NMIH = 2
    const val VALUE_UNITS_SPEED_KNOT = 3

    // TEMPERATURE

    const val VALUE_UNITS_TEMPERATURE_CELSIUS = 0
    const val VALUE_UNITS_TEMPERATURE_FAHRENHEIT = 1

    // WEIGHT

    const val VALUE_UNITS_WEIGHT_KG = 0
    const val VALUE_UNITS_WEIGHT_LB = 1

    //*************************************************
    // ANGLE
    //*************************************************

    var angleInAngularMi = 2.0 * Math.PI * 1000.0 / 360.0
    var angleInRussianMil = 6000.0 / 360.0
    var angleInUsArttileryMil = 6400.0 / 360.0

    //*************************************************
    // DISTANCE
    //*************************************************

    // amount of metres in one mile
    const val UNIT_KILOMETER_TO_METER = 1000.0
    // amount of metres in one mile
    const val UNIT_MILE_TO_METER = 1609.344
    // amount of metres in one nautical mile
    const val UNIT_NMILE_TO_METER = 1852.0
    // amount of feet in one meter
    const val UNIT_METER_TO_FEET = 3.2808

    //*************************************************
    // ENERGY
    //*************************************************

    // convert energy values - calories > joule
    const val ENERGY_CAL_TO_J = 4.185f

    //*************************************************
    // WEIGHT
    //*************************************************

    // convert weight values - calories > joule
    const val WEIGHT_KG_TO_LB = 2.204622f

    private var formats: Array<Array<DecimalFormat>>? = null

    /**
     * Precision parameter used for formatting.
     */
    enum class UnitsPrecision {
        LOW, MEDIUM, HIGH
    }

    //*************************************************
    // ALTITUDE
    //*************************************************

    /**
     * Format altitude value.
     *
     * @param format   format of altitude
     * @param altitude altitude [in metres]
     * @param addUnits `true` to add units
     * @return formatted value
     */
    fun formatAltitude(format: Int, altitude: Double, addUnits: Boolean): String {
        return formatAltitude(format, altitude, 0, addUnits)
    }

    /**
     * Format altitude value.
     *
     * @param format   format of altitude
     * @param altitude altitude [in metres]
     * @param accuracy number of decimal places
     * @param addUnits `true` to add units
     * @return formatted value
     */
    fun formatAltitude(format: Int, altitude: Double, accuracy: Int, addUnits: Boolean): String {
        val value = formatAltitudeValue(format, altitude)
        val res = formatDouble(value, accuracy)
        return if (addUnits) {
            res + " " + formatAltitudeUnits(format)
        } else {
            res
        }
    }

    /**
     * Format (convert) altitude value to require format.
     *
     * @param format   format of altitude
     * @param altitude altitude value [in metres]
     * @return formatted value
     */
    fun formatAltitudeValue(format: Int, altitude: Double): Double {
        return if (format == VALUE_UNITS_ALTITUDE_FEET) {
            altitude * UNIT_METER_TO_FEET
        } else {
            altitude
        }
    }

    /**
     * Get altitude units for certain format.
     *
     * @param format required format
     * @return unit value
     */
    fun formatAltitudeUnits(format: Int): String {
        return if (format == VALUE_UNITS_ALTITUDE_FEET) {
            "ft"
        } else {
            "m"
        }
    }

    /**
     * Format angle value.
     *
     * @param format      format of altitude
     * @param angle       angle [in degrees]
     * @param optimize    `true` to optimize/round angle value with defined accuracy
     * @param minAccuracy minimal number of decimal places
     * @return formatted value
     */
    fun formatAngle(format: Int, angle: Float,
            optimize: Boolean, minAccuracy: Int): String {
        // format texts
        val resValue = formatAngleValue(format,
                angle.toDouble(), optimize, minAccuracy)
        val units = formatAngleUnits(format)
        return formatDouble(resValue, minAccuracy) + units
    }

    /**
     * Format (convert) angle value to require format.
     *
     * @param format      format of angle
     * @param angle       angle value [in degrees]
     * @param optimize    `true` to optimize/round angle value with defined accuracy
     * @param minAccuracy minimal number of decimal places
     * @return formatted value
     */
    fun formatAngleValue(format: Int, angle: Double,
            optimize: Boolean, minAccuracy: Int): Double {
        var angle = angle
        // fix angle values (round them on correct values for display)
        if (optimize) {
            angle = optimizeAngleValue(angle, minAccuracy)
        }

        // finally format
        return when (format) {
            VALUE_UNITS_ANGLE_ANGULAR_MIL -> angle * angleInAngularMi
            VALUE_UNITS_ANGLE_RUSSIAN_MIL -> angle * angleInRussianMil
            VALUE_UNITS_ANGLE_US_ARTILLERY_MIL -> angle * angleInUsArttileryMil

            // VALUE_UNITS_ANGLE_DEGREE
            else -> angle
        }

    }

    /**
     * Get angle units for certain format.
     *
     * @param format required format
     * @return unit value
     */
    fun formatAngleUnits(format: Int): String {
        return if (format == VALUE_UNITS_ANGLE_DEGREE) {
            "°"
        } else {
            ""
        }
    }

    /**
     * Optimize/round angle value based on defined accuracy.
     *
     * @param angle       angle to optimize
     * @param minAccuracy minimal accuracy for output
     * @return optimized value
     */
    private fun optimizeAngleValue(angle: Double, minAccuracy: Int): Double {
        var angle = angle
        val divider = Math.pow(10.0, minAccuracy.toDouble()).toInt()
        angle = Math.round(angle * divider).toDouble()

        // round values
        if (minAccuracy == 0) {
            if (angle < -0.5) {
                angle += 360.0
            }
            if (angle >= 359.5) {
                angle -= 360.0
            }
        } else {
            if (angle < 0.0) {
                angle += 360.0 * divider
            }
            if (angle >= 360.0 * divider) {
                angle -= (360.0f * divider).toDouble()
            }
        }

        // return optimized value
        return angle / divider
    }

    //*************************************************
    // AREA
    //*************************************************

    /**
     * Format area value.
     *
     * @param format   format of area
     * @param area     area [in metres^2]
     * @param addUnits `true` to add units
     * @return formatted value
     */
    fun formatArea(format: Int, area: Double, addUnits: Boolean): CharSequence {
        val sb = StringBuilder()
        if (!addUnits) {
            sb.append(formatAreaValue(format, area))
        } else {
            sb.append(formatAreaValue(format, area)).append(" ").append(formatAreaUnit(format))
        }

        // return result
        return Html.fromHtml(sb.toString())
    }

    /**
     * Format (convert) area value to require format.
     *
     * @param format format of altitude
     * @param area   area value [in metres^2]
     * @return formatted value
     */
    fun formatAreaValue(format: Int, area: Double): String {
        var area = area
        area = when (format) {
            VALUE_UNITS_AREA_M_SQ -> return formatDouble(area, 0)
            VALUE_UNITS_AREA_HA -> area / 10000
            VALUE_UNITS_AREA_KM_SQ -> area / 1000000
            VALUE_UNITS_AREA_FT_SQ -> return formatDouble(area / 0.09290304, 0)
            VALUE_UNITS_AREA_YA_SQ -> return formatDouble(area / 0.83612736, 0)
            VALUE_UNITS_AREA_ACRE -> area / 4046.8564224
            VALUE_UNITS_AREA_MI_SQ -> area / 2589988.110336
            VALUE_UNITS_AREA_NM_SQ -> area / 3429904.0
            else -> return ""
        }

        // format area
        return when {
            area < 100 -> formatDouble(area, 2)
            area < 1000 -> formatDouble(area, 1)
            else -> formatDouble(area, 0)
        }
    }

    /**
     * Format units for area values.
     *
     * @param unitType type of units
     * @return formatted text
     */
    fun formatAreaUnit(unitType: Int): CharSequence {
        var text = ""
        when (unitType) {
            VALUE_UNITS_AREA_M_SQ -> text = "m&sup2;"
            VALUE_UNITS_AREA_HA -> text = "ha"
            VALUE_UNITS_AREA_KM_SQ -> text = "km&sup2;"
            VALUE_UNITS_AREA_FT_SQ -> text = "ft&sup2;"
            VALUE_UNITS_AREA_YA_SQ -> text = "yd&sup2;"
            VALUE_UNITS_AREA_ACRE -> text = "acre"
            VALUE_UNITS_AREA_MI_SQ -> text = "mi&sup2;"
            VALUE_UNITS_AREA_NM_SQ -> text = "nm&sup2;"
        }

        // return formatted text
        return Html.fromHtml(text)
    }

    fun formatDistance(unitType: Int, dist: Double, withoutUnits: Boolean): String? {
        return formatDistance(unitType, dist, UnitsPrecision.MEDIUM, !withoutUnits)
    }

    /**
     * Format distance values by various parameters.
     *
     * @param unitType  type of unit
     * @param dist      distance value
     * @param precision required precision
     * @param addUnits  `true` to add units to result
     * @return formatted text as distance
     */
    fun formatDistance(unitType: Int, dist: Double,
            precision: UnitsPrecision, addUnits: Boolean): String? {
        var value: String? = null
        when (unitType) {
            VALUE_UNITS_DISTANCE_ME_M -> value = when (precision) {
                UtilsFormat.UnitsPrecision.LOW, UtilsFormat.UnitsPrecision.MEDIUM -> formatDouble(dist, 0)
                UtilsFormat.UnitsPrecision.HIGH -> formatDouble(dist, 1)
            }
            VALUE_UNITS_DISTANCE_ME_MKM -> when (precision) {
                UtilsFormat.UnitsPrecision.LOW, UtilsFormat.UnitsPrecision.MEDIUM -> value = if (dist >= UNIT_KILOMETER_TO_METER) {
                    val km = dist / UNIT_KILOMETER_TO_METER
                    if (km >= 100.0) {
                        formatDouble(km, 0)
                    } else {
                        formatDouble(km, 1)
                    }
                } else {
                    formatDistance(dist, 0)
                }
                UtilsFormat.UnitsPrecision.HIGH -> value = if (dist >= UNIT_KILOMETER_TO_METER) {
                    val km = dist / UNIT_KILOMETER_TO_METER
                    if (km >= 100.0) {
                        formatDouble(km, 1)
                    } else {
                        formatDouble(km, 2)
                    }
                } else {
                    formatDouble(dist, 1)
                }
            }
            VALUE_UNITS_DISTANCE_IM_F -> value = when (precision) {
                UtilsFormat.UnitsPrecision.LOW, UtilsFormat.UnitsPrecision.MEDIUM -> formatDistance(dist * UNIT_METER_TO_FEET, 0)
                UtilsFormat.UnitsPrecision.HIGH -> formatDistance(dist * UNIT_METER_TO_FEET, 1)
            }
            VALUE_UNITS_DISTANCE_IM_FM -> value = formatDistanceImperial(dist, dist * UNIT_METER_TO_FEET, precision)
            VALUE_UNITS_DISTANCE_IM_Y -> value = when (precision) {
                UtilsFormat.UnitsPrecision.LOW, UtilsFormat.UnitsPrecision.MEDIUM -> formatDistance(dist * 1.0936, 0)
                UtilsFormat.UnitsPrecision.HIGH -> formatDistance(dist * 1.0936, 1)
            }
            VALUE_UNITS_DISTANCE_IM_YM -> value = formatDistanceImperial(dist, dist * 1.0936, precision)
            VALUE_UNITS_DISTANCE_NA_MNMI -> when (precision) {
                UtilsFormat.UnitsPrecision.LOW, UtilsFormat.UnitsPrecision.MEDIUM -> value = if (dist > UNIT_NMILE_TO_METER) {
                    val nmi = dist / UNIT_NMILE_TO_METER
                    if (nmi >= 100) {
                        formatDouble(nmi, 0)
                    } else {
                        formatDouble(nmi, 1)
                    }
                } else {
                    formatDistance(dist, 0)
                }
                UtilsFormat.UnitsPrecision.HIGH -> value = if (dist > UNIT_NMILE_TO_METER) {
                    val nmi = dist / UNIT_NMILE_TO_METER
                    if (nmi >= 100) {
                        formatDouble(nmi, 1)
                    } else {
                        formatDouble(nmi, 2)
                    }
                } else {
                    formatDistance(dist, 1)
                }
            }
        }

        // return result
        return if (addUnits) {
            value + " " + formatDistanceUnits(unitType, dist)
        } else {
            value
        }
    }

    /**
     * Simple helper function for formatting double values as distance.
     *
     * @param dist          distance value
     * @param basePrecision base precision used for smallest values
     * @return formatted distance
     */
    private fun formatDistance(dist: Double, basePrecision: Int): String {
        return if (dist < 10) {
            formatDouble(dist, basePrecision)
        } else {
            formatDouble(dist, if (basePrecision > 0) basePrecision - 1 else 0)
        }
    }

    /**
     * Format distance value as imperial unit (mile, feet/yards).
     *
     * @param dist       distance value
     * @param distInUnit already converted distance to feet/yards units
     * @param precision  required precision
     * @return formatted distance
     */
    private fun formatDistanceImperial(dist: Double, distInUnit: Double,
            precision: UnitsPrecision): String {
        when (precision) {
            UtilsFormat.UnitsPrecision.LOW, UtilsFormat.UnitsPrecision.MEDIUM -> return if (distInUnit >= 1000.0) {
                val mi = dist / UNIT_MILE_TO_METER
                when {
                    mi >= 100 -> formatDouble(mi, 0)
                    mi >= 1 -> formatDouble(mi, 1)
                    else -> formatDouble(mi, 2)
                }
            } else {
                formatDistance(distInUnit, 0)
            }
            UtilsFormat.UnitsPrecision.HIGH -> return if (distInUnit >= 1000.0) {
                val mi = dist / UNIT_MILE_TO_METER
                when {
                    mi >= 100 -> formatDouble(mi, 1)
                    mi >= 1 -> formatDouble(mi, 2)
                    else -> formatDouble(mi, 3)
                }
            } else {
                formatDistance(distInUnit, 1)
            }
        }
    }

    fun formatDistanceValue(unitType: Int, dist: Double): Double {
        when (unitType) {
            VALUE_UNITS_DISTANCE_ME_M -> return dist
            VALUE_UNITS_DISTANCE_ME_MKM -> return if (dist >= UNIT_KILOMETER_TO_METER) {
                dist / UNIT_KILOMETER_TO_METER
            } else {
                dist
            }
            VALUE_UNITS_DISTANCE_IM_F -> return dist * UNIT_METER_TO_FEET
            VALUE_UNITS_DISTANCE_IM_FM -> {
                val feet = dist * UNIT_METER_TO_FEET
                return if (feet >= 1000.0) {
                    dist / UNIT_MILE_TO_METER
                } else {
                    feet
                }
            }
            VALUE_UNITS_DISTANCE_IM_Y -> return dist * 1.0936
            VALUE_UNITS_DISTANCE_IM_YM -> {
                val yards = dist * 1.0936
                return if (yards >= 1000.0) {
                    dist / UNIT_MILE_TO_METER
                } else {
                    yards
                }
            }
            VALUE_UNITS_DISTANCE_NA_MNMI -> return if (dist >= UNIT_NMILE_TO_METER) {
                dist / UNIT_NMILE_TO_METER
            } else {
                dist
            }
            else -> return dist
        }
    }

    fun formatDistanceUnits(unitType: Int, dist: Double): String {
        when (unitType) {
            VALUE_UNITS_DISTANCE_ME_M -> return "m"
            VALUE_UNITS_DISTANCE_ME_MKM -> return if (dist >= UNIT_KILOMETER_TO_METER) {
                "km"
            } else {
                "m"
            }
            VALUE_UNITS_DISTANCE_IM_F -> return " ft"
            VALUE_UNITS_DISTANCE_IM_FM -> {
                val feet = dist * UNIT_METER_TO_FEET
                return if (feet >= 1000.0) {
                    "mi"
                } else {
                    "ft"
                }
            }
            VALUE_UNITS_DISTANCE_IM_Y -> return "yd"
            VALUE_UNITS_DISTANCE_IM_YM -> {
                val yards = dist * 1.0936
                return if (yards >= 1000.0) {
                    "mi"
                } else {
                    "yd"
                }
            }
            VALUE_UNITS_DISTANCE_NA_MNMI -> return if (dist >= UNIT_NMILE_TO_METER) {
                "nmi"
            } else {
                "m"
            }
            else -> return ""
        }
    }

    /**
     * Format energy values and it's units.
     *
     * @param unitType type of unit
     * @param energy   energy value (in joule)
     * @param addUnits `true` to add units to format
     * @return formatted text
     */
    fun formatEnergy(unitType: Int, energy: Int, addUnits: Boolean): String {
        val res = formatEnergyValue(unitType, energy)
        return if (addUnits) {
            res + " " + formatEnergyUnit(unitType)
        } else {
            res
        }
    }

    /**
     * Format value of energy.
     *
     * @param format type of unit
     * @param energy energy value (in joule)
     * @return formatted text
     */
    fun formatEnergyValue(format: Int, energy: Int): String {
        return if (format == VALUE_UNITS_ENERGY_KJ) {
            formatDouble(energy * 1.0 / 1000.0, 0)
        } else { // VALUE_UNITS_ENERGY_KCAL
            formatDouble(energy * 1.0 / (ENERGY_CAL_TO_J * 1000.0), 0)
        }
    }

    /**
     * Get energy units for certain format.
     *
     * @param format required format
     * @return unit value
     */
    fun formatEnergyUnit(format: Int): String {
        return if (format == VALUE_UNITS_ENERGY_KJ) {
            "KJ"
        } else {
            "kcal"
        }
    }

    //*************************************************
    // SLOPE
    //*************************************************

    /**
     * Format slope values and it's units.
     *
     * @param unitType type of unit
     * @param slope    slope value (in joule)
     * @param addUnits `true` to add units to format
     * @return formatted text
     */
    fun formatSlope(unitType: Int, slope: Double, addUnits: Boolean): String {
        val res = formatSlopeValue(unitType, slope)
        return if (addUnits) {
            res + " " + formatSlopeUnit(unitType)
        } else {
            res
        }
    }

    /**
     * Format value of slope.
     *
     * @param format type of unit
     * @param slope  slope value (in joule)
     * @return formatted text
     */
    fun formatSlopeValue(format: Int, slope: Double): String {
        return if (format == VALUE_UNITS_SLOPE_PERCENT) {
            formatDouble(slope * 100.0, 0)
        } else { // VALUE_UNITS_SLOPE_DEGREE
            val slopeAngle = Math.atan(slope) * (180.0 / Math.PI)
            formatDouble(slopeAngle, 0)
        }
    }

    /**
     * Get slope units for certain format.
     *
     * @param format required format
     * @return unit value
     */
    fun formatSlopeUnit(format: Int): String {
        return if (format == VALUE_UNITS_SLOPE_PERCENT) {
            "%"
        } else { // VALUE_UNITS_SLOPE_DEGREE
            "°"
        }
    }

    //*************************************************
    // SPEED
    //*************************************************

    /**
     * Format speed to correct format.
     *
     * @param speed Speed in m/s.
     * @return Formated speed in appropriate units.
     */
    fun formatSpeed(unitType: Int, speed: Double, withoutUnits: Boolean): String {
        var speed = speed
        // format speed value
        val result: String
        if (speed < 0.0) {
            result = "--"
        } else {
            speed = formatSpeedValue(unitType, speed)
            result = formatDouble(speed, if (speed > 100) 0 else 1)
        }

        // attach units if requested
        return if (withoutUnits) {
            result
        } else {
            result + " " + formatSpeedUnits(unitType)
        }
    }

    fun formatSpeedValue(format: Int, speed: Double): Double {
        var speed = speed
        speed *= if (format == VALUE_UNITS_SPEED_MILH) {
            2.237
        } else if (format == VALUE_UNITS_SPEED_NMIH || format == VALUE_UNITS_SPEED_KNOT) {
            3.6 / 1.852
        } else { // metric UNITS_LENGTH_METRIC
            3.6
        }
        return speed
    }

    /**
     * Get speed units for certain format.
     *
     * @param format required format
     * @return unit value
     */
    fun formatSpeedUnits(format: Int): String {
        return when (format) {
            VALUE_UNITS_SPEED_MILH -> "mi/h"
            VALUE_UNITS_SPEED_NMIH -> "nmi/h"
            VALUE_UNITS_SPEED_KNOT -> "kn"
            else -> // metric UNITS_LENGTH_METRIC
                "km/h"
        }
    }

    //*************************************************
    // TEMPERATURE
    //*************************************************

    /**
     * Format temperature value.
     *
     * @param unit     temperature unit
     * @param tempC    temperature itself [°C]
     * @param addUnits `true` to add units to result format
     * @return generated text
     */
    fun formatTemperature(unit: Int, tempC: Double, addUnits: Boolean): String {
        return formatTemperature(unit, tempC, UnitsPrecision.MEDIUM, addUnits)
    }

    /**
     * Format temperature value.
     *
     * @param unit      temperature unit
     * @param tempC     temperature itself [°C]
     * @param precision precision of format
     * @param addUnits  `true` to add units to result format
     * @return generated text
     */
    fun formatTemperature(unit: Int, tempC: Double, precision: UnitsPrecision, addUnits: Boolean): String {
        val res = formatTemperatureValue(unit, tempC)
        val decDigits = if (precision == UnitsPrecision.LOW) 0 else 1

        // return formatted result
        val tempFormatted = formatDouble(res, decDigits)
        return if (addUnits) {
            tempFormatted + " " + formatTemperatureUnit(unit)
        } else {
            tempFormatted
        }
    }

    /**
     * Format temperature value itself.
     *
     * @param unit  temperature unit
     * @param tempC temperature itself [°C]
     * @return formatted value
     */
    fun formatTemperatureValue(unit: Int, tempC: Double): Double {
        return if (unit == VALUE_UNITS_TEMPERATURE_FAHRENHEIT) {
            tempC * 9.0 / 5.0 + 32.0
        } else { // VALUE_UNITS_TEMPERATURE_CELSIUS
            tempC
        }
    }

    /**
     * Get temperature units for certain format.
     *
     * @param unit required format
     * @return unit value
     */
    fun formatTemperatureUnit(unit: Int): String {
        return if (unit == VALUE_UNITS_TEMPERATURE_FAHRENHEIT) {
            "F"
        } else { // VALUE_UNITS_TEMPERATURE_CELSIUS
            "°C"
        }
    }

    /**
     * Format weight values and it's units.
     *
     * @param unitType type of unit
     * @param weight   weight value (in grams)
     * @param addUnits `true` to add units to format
     * @return formatted text
     */
    fun formatWeight(unitType: Int, weight: Double, addUnits: Boolean): String {
        val res = formatWeightValue(unitType, weight)
        return if (addUnits) {
            res + " " + formatWeightUnit(unitType)
        } else {
            res
        }
    }

    /**
     * Format value of weight.
     *
     * @param format type of unit
     * @param weight weight value (in grams)
     * @return formatted text
     */
    fun formatWeightValue(format: Int, weight: Double): String {
        return if (format == VALUE_UNITS_WEIGHT_KG) {
            formatDouble(weight * 1.0 / 1000.0, 1)
        } else {
            formatDouble(weight * WEIGHT_KG_TO_LB / 1000.0, 1)
        }
    }

    /**
     * Get weight units for certain format.
     *
     * @param format required format
     * @return unit value
     */
    fun formatWeightUnit(format: Int): String {
        return when (format) {
            VALUE_UNITS_WEIGHT_KG -> "kg"
            VALUE_UNITS_WEIGHT_LB -> "lb"
            else -> "???"
        }
    }

    @JvmOverloads
    fun formatDouble(value: Double, precision: Int, minlen: Int = 1): String {
        var precision = precision
        var minlen = minlen
        if (minlen < 0) {
            minlen = 0
        } else if (minlen > formats!!.size - 1) {
            minlen = formats!!.size - 1
        }
        if (precision < 0) {
            precision = 0
        } else if (precision > formats!![0].size - 1) {
            precision = formats!![0].size - 1
        }
        return formats!![minlen][precision].format(value)
    }

    init {
        formats = arrayOf(arrayOf(DecimalFormat("#"), DecimalFormat("#.0"), DecimalFormat("#.00"), DecimalFormat("#.000"), DecimalFormat("#.0000"), DecimalFormat("#.00000"), DecimalFormat("#.000000")), arrayOf(DecimalFormat("#0"), DecimalFormat("#0.0"), DecimalFormat("#0.00"), DecimalFormat("#0.000"), DecimalFormat("#0.0000"), DecimalFormat("#0.00000"), DecimalFormat("#0.000000")), arrayOf(DecimalFormat("#00"), DecimalFormat("#00.0"), DecimalFormat("#00.00"), DecimalFormat("#00.000"), DecimalFormat("#00.0000"), DecimalFormat("#00.00000"), DecimalFormat("#00.000000")), arrayOf(DecimalFormat("#000"), DecimalFormat("#000.0"), DecimalFormat("#000.00"), DecimalFormat("#000.000"), DecimalFormat("#000.0000"), DecimalFormat("#000.00000"), DecimalFormat("#000.000000")), arrayOf(DecimalFormat("#0000"), DecimalFormat("#0000.0"), DecimalFormat("#0000.00"), DecimalFormat("#0000.000"), DecimalFormat("#0000.0000"), DecimalFormat("#0000.00000"), DecimalFormat("#0000.000000")), arrayOf(DecimalFormat("#00000"), DecimalFormat("#00000.0"), DecimalFormat("#00000.00"), DecimalFormat("#00000.000"), DecimalFormat("#00000.0000"), DecimalFormat("#00000.00000"), DecimalFormat("#00000.000000")), arrayOf(DecimalFormat("#000000"), DecimalFormat("#000000.0"), DecimalFormat("#000000.00"), DecimalFormat("#000000.000"), DecimalFormat("#000000.0000"), DecimalFormat("#000000.00000"), DecimalFormat("#000000.000000")), arrayOf(DecimalFormat("#0000000"), DecimalFormat("#0000000.0"), DecimalFormat("#0000000.00"), DecimalFormat("#0000000.000"), DecimalFormat("#0000000.0000"), DecimalFormat("#0000000.00000"), DecimalFormat("#0000000.000000")))
        for (format in formats!!) {
            for (aFormat in format) {
                aFormat.decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH)
            }
        }
    }
}//*************************************************
// FORMAT DOUBLE PART
//*************************************************
