package locus.api.android.utils;

import android.text.Html;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Class that serve formatting units to defined formats.
 */
public class UtilsFormat {

	// ALTITUDE

	public static final int VALUE_UNITS_ALTITUDE_METRES 						= 0;
	public static final int VALUE_UNITS_ALTITUDE_FEET 							= 1;
	
	// ANGLE

	public static final int VALUE_UNITS_ANGLE_DEGREE 							= 0;
	public static final int VALUE_UNITS_ANGLE_ANGULAR_MIL 						= 1;
	public static final int VALUE_UNITS_ANGLE_RUSSIAN_MIL 						= 2;
	public static final int VALUE_UNITS_ANGLE_US_ARTILLERY_MIL 					= 3;
	
	// AREA

	public static final int VALUE_UNITS_AREA_M_SQ 								= 0;
	public static final int VALUE_UNITS_AREA_HA 								= 1;
	public static final int VALUE_UNITS_AREA_KM_SQ 								= 2;
	public static final int VALUE_UNITS_AREA_FT_SQ 								= 3;
	public static final int VALUE_UNITS_AREA_YA_SQ 								= 4;
	public static final int VALUE_UNITS_AREA_ACRE 								= 5;
	public static final int VALUE_UNITS_AREA_MI_SQ 								= 6;
	public static final int VALUE_UNITS_AREA_NM_SQ 								= 7;

	// DISTANCE

	public static final int VALUE_UNITS_DISTANCE_ME_M 							= 0;
	public static final int VALUE_UNITS_DISTANCE_ME_MKM 						= 1;
	public static final int VALUE_UNITS_DISTANCE_IM_F 							= 2;
	public static final int VALUE_UNITS_DISTANCE_IM_FM 							= 3;
	public static final int VALUE_UNITS_DISTANCE_IM_Y 							= 4;
	public static final int VALUE_UNITS_DISTANCE_IM_YM 							= 5;
	public static final int VALUE_UNITS_DISTANCE_NA_MNMI 						= 6;

	// ENERGY

	public static final int VALUE_UNITS_ENERGY_KJ								= 0;
	public static final int VALUE_UNITS_ENERGY_KCAL								= 1;

	// SLOPE

	public static final int VALUE_UNITS_SLOPE_PERCENT							= 0;
	public static final int VALUE_UNITS_SLOPE_DEGREE							= 1;

	// SPEED

	public static final int VALUE_UNITS_SPEED_KMH 								= 0;
	public static final int VALUE_UNITS_SPEED_MILH 								= 1;
	public static final int VALUE_UNITS_SPEED_NMIH 								= 2;
	public static final int VALUE_UNITS_SPEED_KNOT 								= 3;
	
	// TEMPERATURE

	public static final int VALUE_UNITS_TEMPERATURE_CELSIUS 					= 0;
	public static final int VALUE_UNITS_TEMPERATURE_FAHRENHEIT 					= 1;

	// WEIGHT

	public static final int VALUE_UNITS_WEIGHT_KG 								= 0;
	public static final int VALUE_UNITS_WEIGHT_LB 								= 1;

	/**
     * Precision parameter used for formatting.
     */
    public enum UnitsPrecision {
        LOW, MEDIUM, HIGH
    }

	/**************************************************/
    // ALTITUDE
	/**************************************************/
    
	/**
     * Format altitude value.
     * @param format format of altitude
     * @param altitude altitude [in metres]
     * @param addUnits {@code true} to add units
     * @return formatted value
     */
    public static String formatAltitude(int format, double altitude, boolean addUnits) {
		return formatAltitude(format, altitude, 0, addUnits);
    }

	/**
	 * Format altitude value.
	 * @param format format of altitude
	 * @param altitude altitude [in metres]
	 * @param accuracy number of decimal places
	 * @param addUnits {@code true} to add units
	 * @return formatted value
	 */
	public static String formatAltitude(int format, double altitude, int accuracy, boolean addUnits) {
		double value = formatAltitudeValue(format, altitude);
		String res =  formatDouble(value, accuracy);
		if (addUnits) {
			return res + " " + formatAltitudeUnits(format);
		} else {
			return res;
		}
	}

	/**
	 * Format (convert) altitude value to require format.
	 * @param format format of altitude
	 * @param altitude altitude value [in metres]
	 * @return formatted value
	 */
    public static double formatAltitudeValue(int format, double altitude) {
    	if (format == VALUE_UNITS_ALTITUDE_FEET) {
            return altitude * UNIT_METER_TO_FEET;
        } else {
            return altitude;
        }
    }

	/**
	 * Get altitude units for certain format.
	 * @param format required format
	 * @return unit value
	 */
    public static String formatAltitudeUnits(int format) {
    	if (format == VALUE_UNITS_ALTITUDE_FEET) {
            return "ft";
        } else {
            return "m";
        }
    }
    
    /**************************************************/
    // ANGLE
    /**************************************************/
    
    public static double angleInAngularMi = 
    		(2 * Math.PI * 1000.0 / 360.0);
    public static double angleInRussianMil = 
    		(6000.0 / 360.0);
    public static double angleInUsArttileryMil = 
    		(6400.0 / 360.0);

	/**
	 * Format angle value.
	 * @param format format of altitude
	 * @param angle angle [in degrees]
	 * @param optimize {@code true} to optimize/round angle value with defined accuracy
	 * @param minAccuracy minimal number of decimal places
	 * @return formatted value
	 */
    public static String formatAngle(int format, float angle, 
    		boolean optimize, int minAccuracy) {
    	// format texts
    	double resValue = formatAngleValue(format, 
    			angle, optimize, minAccuracy);
    	String units = formatAngleUnits(format);
    	return formatDouble(resValue, minAccuracy) + units;
    }

	/**
	 * Format (convert) angle value to require format.
	 * @param format format of angle
	 * @param angle angle value [in degrees]
	 * @param optimize {@code true} to optimize/round angle value with defined accuracy
	 * @param minAccuracy minimal number of decimal places
	 * @return formatted value
	 */
    public static double formatAngleValue(int format, double angle,
    		boolean optimize, int minAccuracy) {
		// fix angle values (round them on correct values for display)
    	if (optimize) {
    		angle = optimizeAngleValue(angle, minAccuracy);
    	}
    	
    	// finally format
    	if (format == VALUE_UNITS_ANGLE_ANGULAR_MIL) {
			return angle * angleInAngularMi;
		} else if (format == VALUE_UNITS_ANGLE_RUSSIAN_MIL) {
			return angle * angleInRussianMil;
		} else if (format == VALUE_UNITS_ANGLE_US_ARTILLERY_MIL) {
			return angle * angleInUsArttileryMil;
		}
    	
    	// VALUE_UNITS_ANGLE_DEGREE
    	return angle;
    }

	/**
	 * Get angle units for certain format.
	 * @param format required format
	 * @return unit value
	 */
	public static String formatAngleUnits(int format) {
		if (format == VALUE_UNITS_ANGLE_DEGREE) {
			return "°";
		} else {
			return "";
		}
	}

	/**
	 * Optimize/round angle value based on defined accuracy.
	 * @param angle angle to optimize
	 * @param minAccuracy minimal accuracy for output
	 * @return optimized value
	 */
    private static double optimizeAngleValue(double angle, int minAccuracy) {
		int divider = (int) Math.pow(10, minAccuracy);
		angle = Math.round(angle * divider);

		// round values
		if (minAccuracy == 0) {
			if (angle < -0.5) {
				angle += 360.0;
			}
			if (angle >= 359.5) {
				angle -= 360.0f;
			}
		} else {
			if (angle < 0.0) {
				angle += (360.0 * divider);
			}
			if (angle >= (360.0 * divider)) {
				angle -= (360.0f * divider);
			}
		}

		// return optimized value
		return angle / divider;
    }

    /**************************************************/
    // AREA
    /**************************************************/

	/**
	 * Format area value.
	 * @param format format of area
	 * @param area area [in metres^2]
	 * @param addUnits {@code true} to add units
	 * @return formatted value
	 */
    public static CharSequence formatArea(int format, double area, boolean addUnits) {
    	StringBuilder sb = new StringBuilder();
    	if (!addUnits) {
    		sb.append(formatAreaValue(format, area));
    	} else {
    		sb.append(formatAreaValue(format, area)).append(" ").
    			append(formatAreaUnit(format, area));
    	}
    	
    	// return result
    	return Html.fromHtml(sb.toString());
    }

	/**
	 * Format (convert) area value to require format.
	 * @param format format of altitude
	 * @param area area value [in metres^2]
	 * @return formatted value
	 */
    public static String formatAreaValue(int format, double area) {
        switch (format) {
            case VALUE_UNITS_AREA_M_SQ:
                return formatDouble(area, 0);
            case VALUE_UNITS_AREA_HA:
                area = area / 10000;
                break;
            case VALUE_UNITS_AREA_KM_SQ:
                area = area / 1000000;
                break;
            case VALUE_UNITS_AREA_FT_SQ:
                return formatDouble(area / 0.09290304, 0);
            case VALUE_UNITS_AREA_YA_SQ:
                return formatDouble(area / 0.83612736, 0);
            case VALUE_UNITS_AREA_ACRE:
                area = area / 4046.8564224;
                break;
            case VALUE_UNITS_AREA_MI_SQ:
                area = area / 2589988.110336;
                break;
            case VALUE_UNITS_AREA_NM_SQ:
                area = area / 3429904.0;
                break;
            default:
                return "";
        }

        // format area
        if (area < 100) {
            return formatDouble(area, 2);
        } else if (area < 1000) {
            return formatDouble(area, 1);
        } else {
            return formatDouble(area, 0);
        }
    }

	/**
	 * Format units for area values.
	 * @param unitType type of units
	 * @param area current area value
	 * @return formatted text
	 */
    public static CharSequence formatAreaUnit(int unitType, double area) {
		String text = "";
		switch (unitType) {
			case VALUE_UNITS_AREA_M_SQ:
				text = "m&sup2;";
				break;
			case VALUE_UNITS_AREA_HA:
				text = "ha";
				break;
			case VALUE_UNITS_AREA_KM_SQ:
				text = "km&sup2;";
				break;
			case VALUE_UNITS_AREA_FT_SQ:
				text = "ft&sup2;";
				break;
			case VALUE_UNITS_AREA_YA_SQ:
				text = "yd&sup2;";
				break;
			case VALUE_UNITS_AREA_ACRE:
				text = "acre";
				break;
			case VALUE_UNITS_AREA_MI_SQ:
				text = "mi&sup2;";
				break;
			case VALUE_UNITS_AREA_NM_SQ:
				text = "nm&sup2;";
				break;
		}

		// return formatted text
		return Html.fromHtml(text);
    }

    /**************************************************/
    // DISTANCE
    /**************************************************/

	// amount of metres in one mile
	public static final double UNIT_KILOMETER_TO_METER = 1000.0;
    // amount of metres in one mile
    public static final double UNIT_MILE_TO_METER = 1609.344;
    // amount of metres in one nautical mile
    public static final double UNIT_NMILE_TO_METER = 1852.0;
    // amount of feet in one meter
    public static final double UNIT_METER_TO_FEET = 3.2808;

    public static String formatDistance(int unitType, double dist, boolean withoutUnits) {
        return formatDistance(unitType, dist, UnitsPrecision.MEDIUM, !withoutUnits);
    }

    /**
     * Format distance values by various parameters.
     * @param unitType type of unit
     * @param dist distance value
     * @param precision required precision
     * @param addUnits <code>true</code> to add units to result
     * @return formatted text as distance
     */
    public static String formatDistance(int unitType, double dist,
            UnitsPrecision precision, boolean addUnits) {
		String value = null;
		switch (unitType) {
			case VALUE_UNITS_DISTANCE_ME_M:
				switch (precision) {
					case LOW:
					case MEDIUM:
						value = formatDouble(dist, 0);
						break;
					case HIGH:
						value = formatDouble(dist, 1);
						break;
				}
				break;
			case VALUE_UNITS_DISTANCE_ME_MKM:
				switch (precision) {
					case LOW:
					case MEDIUM:
						if (dist >= UNIT_KILOMETER_TO_METER) {
							double km = dist / UNIT_KILOMETER_TO_METER;
							if (km >= 100.0) {
								value = formatDouble(km, 0);
							} else {
								value = formatDouble(km, 1);
							}
						} else {
							value = formatDistance(dist, 0);
						}
						break;
					case HIGH:
						if (dist >= UNIT_KILOMETER_TO_METER) {
							double km = dist / UNIT_KILOMETER_TO_METER;
							if (km >= 100.0) {
								value = formatDouble(km, 1);
							} else {
								value = formatDouble(km, 2);
							}
						} else {
							value = formatDouble(dist, 1);
						}
						break;
				}
				break;
			case VALUE_UNITS_DISTANCE_IM_F:
				switch (precision) {
					case LOW:
					case MEDIUM:
						value = formatDistance(dist * UNIT_METER_TO_FEET, 0);
						break;
					case HIGH:
						value = formatDistance(dist * UNIT_METER_TO_FEET, 1);
						break;
				}
				break;
			case VALUE_UNITS_DISTANCE_IM_FM:
				value = formatDistanceImperial(dist, dist * UNIT_METER_TO_FEET, precision);
				break;
			case VALUE_UNITS_DISTANCE_IM_Y:
				switch (precision) {
					case LOW:
					case MEDIUM:
						value = formatDistance(dist * 1.0936, 0);
						break;
					case HIGH:
						value = formatDistance(dist * 1.0936, 1);
						break;
				}
				break;
			case VALUE_UNITS_DISTANCE_IM_YM:
				value = formatDistanceImperial(dist, dist * 1.0936, precision);
				break;
			case VALUE_UNITS_DISTANCE_NA_MNMI:
				switch (precision) {
					case LOW:
					case MEDIUM:
						if (dist > UNIT_NMILE_TO_METER) {
							double nmi = dist / UNIT_NMILE_TO_METER;
							if (nmi >= 100) {
								value = formatDouble(nmi, 0);
							} else {
								value = formatDouble(nmi, 1);
							}
						} else {
							value = formatDistance(dist, 0);
						}
						break;
					case HIGH:
						if (dist > UNIT_NMILE_TO_METER) {
							double nmi = dist / UNIT_NMILE_TO_METER;
							if (nmi >= 100) {
								value = formatDouble(nmi, 1);
							} else {
								value = formatDouble(nmi, 2);
							}
						} else {
							value = formatDistance(dist, 1);
						}
						break;
				}
				break;
		}

		// return result
		if (addUnits) {
			return value + " " + formatDistanceUnits(unitType, dist);
		} else {
			return value;
		}
    }

    /**
     * Simple helper function for formatting double values as distance.
     * @param dist distance value
     * @param basePrecision base precision used for smallest values
     * @return formatted distance
     */
    private static String formatDistance(double dist, int basePrecision) {
        if (dist < 10) {
            return formatDouble(dist, basePrecision);
        } else {
            return formatDouble(dist, basePrecision > 0 ? basePrecision - 1 : 0);
        }
    }

    /**
     * Format distance value as imperial unit (mile, feet/yards).
     * @param dist distance value
     * @param distInUnit already converted distance to feet/yards units
     * @param precision required precision
     * @return formatted distance
     */
    private static String formatDistanceImperial(double dist, double distInUnit,
            UnitsPrecision precision) {
        switch (precision) {
            case LOW:
            case MEDIUM:
                if (distInUnit >= 1000.0) {
                    double mi = dist / UNIT_MILE_TO_METER;
                    if (mi >= 100) {
                        return formatDouble(mi, 0);
                    } else if (mi >= 1) {
                        return formatDouble(mi, 1);
                    } else {
                        return formatDouble(mi, 2);
                    }
                } else {
                    return formatDistance(distInUnit, 0);
                }
            case HIGH:
                if (distInUnit >= 1000.0) {
                    double mi = dist / UNIT_MILE_TO_METER;
                    if (mi >= 100) {
                        return formatDouble(mi, 1);
                    } else if (mi >= 1) {
                        return formatDouble(mi, 2);
                    } else {
                        return formatDouble(mi, 3);
                    }
                } else {
                    return formatDistance(distInUnit, 1);
                }
        }

        // return empty string
        return "";
    }
    
    public static double formatDistanceValue(int unitType, double dist) {
		switch (unitType) {
			case VALUE_UNITS_DISTANCE_ME_M:
				return dist;
			case VALUE_UNITS_DISTANCE_ME_MKM:
				if (dist >= UNIT_KILOMETER_TO_METER) {
					return dist / UNIT_KILOMETER_TO_METER;
				} else {
					return dist;
				}
			case VALUE_UNITS_DISTANCE_IM_F:
				return dist * UNIT_METER_TO_FEET;
			case VALUE_UNITS_DISTANCE_IM_FM:
				double feet = dist * UNIT_METER_TO_FEET;
				if (feet >= 1000.0) {
					return dist / UNIT_MILE_TO_METER;
				} else {
					return feet;
				}
			case VALUE_UNITS_DISTANCE_IM_Y:
				return dist * 1.0936;
			case VALUE_UNITS_DISTANCE_IM_YM:
				double yards = dist * 1.0936;
				if (yards >= 1000.0) {
					return dist / UNIT_MILE_TO_METER;
				} else {
					return yards;
				}
			case VALUE_UNITS_DISTANCE_NA_MNMI:
				if (dist >= UNIT_NMILE_TO_METER) {
					return dist / UNIT_NMILE_TO_METER;
				} else {
					return dist;
				}
			default:
				return dist;
		}
    }
    
    public static String formatDistanceUnits(int unitType, double dist) {
		switch (unitType) {
			case VALUE_UNITS_DISTANCE_ME_M:
				return "m";
			case VALUE_UNITS_DISTANCE_ME_MKM:
				if (dist >= UNIT_KILOMETER_TO_METER) {
					return "km";
				} else {
					return "m";
				}
			case VALUE_UNITS_DISTANCE_IM_F:
				return " ft";
			case VALUE_UNITS_DISTANCE_IM_FM:
				double feet = dist * UNIT_METER_TO_FEET;
				if (feet >= 1000.0) {
					return "mi";
				} else {
					return "ft";
				}
			case VALUE_UNITS_DISTANCE_IM_Y:
				return "yd";
			case VALUE_UNITS_DISTANCE_IM_YM:
				double yards = dist * 1.0936;
				if (yards >= 1000.0) {
					return "mi";
				} else {
					return "yd";
				}
			case VALUE_UNITS_DISTANCE_NA_MNMI:
				if (dist >= UNIT_NMILE_TO_METER) {
					return "nmi";
				} else {
					return "m";
				}
			default:
				return "";
		}
    }

	/**************************************************/
	// ENERGY
	/**************************************************/

	// convert energy values - calories > joule
	public static final float ENERGY_CAL_TO_J = 4.185f;

	/**
	 * Format energy values and it's units.
	 * @param unitType type of unit
	 * @param energy energy value (in joule)
	 * @param addUnits {@code true} to add units to format
	 * @return formatted text
	 */
	public static String formatEnergy(int unitType, int energy, boolean addUnits) {
		String res =  formatEnergyValue(unitType, energy);
		if (addUnits) {
			return res + " " + formatEnergyUnit(unitType);
		} else {
			return res;
		}
	}

	/**
	 * Format value of energy.
	 * @param format type of unit
	 * @param energy energy value (in joule)
	 * @return formatted text
	 */
	public static String formatEnergyValue(int format, int energy) {
		if (format == VALUE_UNITS_ENERGY_KJ) {
			return formatDouble(energy * 1.0 / 1000.0, 0);
		} else {
			return formatDouble(energy * 1.0 / (ENERGY_CAL_TO_J * 1000.0), 0);
		}
	}

	/**
	 * Get energy units for certain format.
	 * @param format required format
	 * @return unit value
	 */
	public static String formatEnergyUnit(int format) {
		if (format == VALUE_UNITS_ENERGY_KJ) {
			return "KJ";
		} else {
			return "kcal";
		}
	}

	/**************************************************/
	// SLOPE
	/**************************************************/

	/**
	 * Format slope values and it's units.
	 * @param unitType type of unit
	 * @param slope slope value (in joule)
	 * @param addUnits {@code true} to add units to format
	 * @return formatted text
	 */
	public static String formatSlope(int unitType, double slope, boolean addUnits) {
		String res =  formatSlopeValue(unitType, slope);
		if (addUnits) {
			return res + " " + formatSlopeUnit(unitType);
		} else {
			return res;
		}
	}

	/**
	 * Format value of slope.
	 * @param format type of unit
	 * @param slope slope value (in joule)
	 * @return formatted text
	 */
	public static String formatSlopeValue(int format, double slope) {
		if (format == VALUE_UNITS_SLOPE_PERCENT) {
			return formatDouble(slope * 100.0, 0);
		} else { // VALUE_UNITS_SLOPE_DEGREE
			double slopeAngle = Math.atan(slope) * (180.0 / Math.PI);
			return formatDouble(slopeAngle, 0);
		}
	}

	/**
	 * Get slope units for certain format.
	 * @param format required format
	 * @return unit value
	 */
	public static String formatSlopeUnit(int format) {
		if (format == VALUE_UNITS_SLOPE_PERCENT) {
			return "%";
		} else { // VALUE_UNITS_SLOPE_DEGREE
			return "°";
		}
	}

    /**************************************************/
    // SPEED
    /**************************************************/
    
    /**
     * Format speed to correct format.
     * @param speed Speed in m/s.
     * @return Formated speed in appropriate units.
     */
    public static String formatSpeed(int unitType, double speed, boolean withoutUnits) {
		// format speed value
		String result;
		if (speed < 0.0) {
			result = "--";
		} else {
			speed = formatSpeedValue(unitType, speed);
			result = formatDouble(speed, speed > 100 ? 0 : 1);
		}

		// attach units if requested
        if (withoutUnits) {
        	return result;
        } else {
        	return result + " " + formatSpeedUnits(unitType);
        }
    }
    
    public static double formatSpeedValue(int format, double speed) {
        if (format == VALUE_UNITS_SPEED_MILH) {
        	speed *= 2.237;
        } else if (format == VALUE_UNITS_SPEED_NMIH ||
        		format == VALUE_UNITS_SPEED_KNOT) {
        	speed *= (3.6 / 1.852);
        } else { // metric UNITS_LENGTH_METRIC
        	speed *= 3.6;
        }
        return speed;
    }

	/**
	 * Get speed units for certain format.
	 * @param format required format
	 * @return unit value
	 */
	public static String formatSpeedUnits(int format) {
        if (format == VALUE_UNITS_SPEED_MILH) {
        	return "mi/h";
        } else if (format == VALUE_UNITS_SPEED_NMIH) {
        	return "nmi/h";
        } else if (format == VALUE_UNITS_SPEED_KNOT) {
        	return "kn";
        } else { // metric UNITS_LENGTH_METRIC
        	return "km/h";
        }
    }
    
	/**************************************************/
    // TEMPERATURE
	/**************************************************/

	/**
	 * Format temperature value.
	 * @param unit temperature unit
	 * @param tempC temperature itself [°C]
	 * @param addUnits {@code true} to add units to result format
	 * @return generated text
	 */
    public static String formatTemperature(int unit, double tempC, boolean addUnits) {
		return formatTemperature(unit, tempC, UnitsPrecision.MEDIUM, addUnits);
	}

	/**
	 * Format temperature value.
	 * @param unit temperature unit
	 * @param tempC temperature itself [°C]
	 * @param precision precision of format
	 * @param addUnits {@code true} to add units to result format
	 * @return generated text
	 */
    public static String formatTemperature(int unit, double tempC, UnitsPrecision precision, boolean addUnits) {
    	double res = formatTemperatureValue(unit, tempC);
		int decDigits = precision == UnitsPrecision.LOW ? 0 : 1;

		// return formatted result
		String tempFormatted = UtilsFormat.formatDouble(res, decDigits);
    	if (addUnits) {
    		return tempFormatted + " " + formatTemperatureUnit(unit);
    	} else {
    		return tempFormatted;
    	}
    }

	/**
	 * Format temperature value itself.
	 * @param unit temperature unit
	 * @param tempC temperature itself [°C]
	 * @return formatted value
	 */
	public static double formatTemperatureValue(int unit, double tempC) {
		if (unit == VALUE_UNITS_TEMPERATURE_FAHRENHEIT) {
			return (tempC * 9.0) / 5.0 + 32.0;
		} else { // VALUE_UNITS_TEMPERATURE_CELSIUS
			return tempC;
		}
	}

	/**
	 * Get temperature units for certain format.
	 * @param unit required format
	 * @return unit value
	 */
    public static String formatTemperatureUnit(int unit) {
		if (unit == VALUE_UNITS_TEMPERATURE_FAHRENHEIT) {
			return "F";
		} else { // VALUE_UNITS_TEMPERATURE_CELSIUS
			return "°C";
		}
    }

	/**************************************************/
	// WEIGHT
	/**************************************************/

	// convert weight values - calories > joule
	public static final float WEIGHT_KG_TO_LB = 2.204622f;

	/**
	 * Format weight values and it's units.
	 * @param unitType type of unit
	 * @param weight weight value (in grams)
	 * @param addUnits {@code true} to add units to format
	 * @return formatted text
	 */
	public static String formatWeight(int unitType, double weight, boolean addUnits) {
		String res =  formatWeightValue(unitType, weight);
		if (addUnits) {
			return res + " " + formatWeightUnit(unitType);
		} else {
			return res;
		}
	}

	/**
	 * Format value of weight.
	 * @param format type of unit
	 * @param weight weight value (in grams)
	 * @return formatted text
	 */
	public static String formatWeightValue(int format, double weight) {
		if (format == VALUE_UNITS_WEIGHT_KG) {
			return formatDouble(weight * 1.0 / 1000.0, 1);
		} else {
			return formatDouble(weight * WEIGHT_KG_TO_LB / 1000.0, 1);
		}
	}

	/**
	 * Get weight units for certain format.
	 * @param format required format
	 * @return unit value
	 */
	public static String formatWeightUnit(int format) {
		if (format == VALUE_UNITS_WEIGHT_KG) {
			return "kg";
		} else if (format == VALUE_UNITS_WEIGHT_LB) {
			return "lb";
		} else {
			return "???";
		}
	}
    
    /**************************************************/
    // FORMAT DOUBLE PART
    /**************************************************/

	public static String formatDouble(double value, int precision) {
		return formatDouble(value, precision, 1);
    }
	
	public static String formatDouble(double value, int precision, int minlen) {
		if (minlen < 0) {
			minlen = 0;
		} else if (minlen > formats.length - 1) {
			minlen = formats.length - 1;
		}
		if (precision < 0) {
			precision = 0;
		} else if (precision > formats[0].length - 1) {
			precision = formats[0].length - 1;
		}
		return formats[minlen][precision].format(value);
    }
	
    private static DecimalFormat[][] formats;
    static {
    	formats = new DecimalFormat[][] {
    			{
    				new DecimalFormat("#"),
    	        	new DecimalFormat("#.0"),
    	        	new DecimalFormat("#.00"),
    	        	new DecimalFormat("#.000"),
    	        	new DecimalFormat("#.0000"),
    	        	new DecimalFormat("#.00000"),
    				new DecimalFormat("#.000000")
    			}, {
    				new DecimalFormat("#0"),
    	        	new DecimalFormat("#0.0"),
    	        	new DecimalFormat("#0.00"),
    	        	new DecimalFormat("#0.000"),
    	        	new DecimalFormat("#0.0000"),
    	        	new DecimalFormat("#0.00000"),
    	        	new DecimalFormat("#0.000000")
    			}, {
    				new DecimalFormat("#00"),
    	        	new DecimalFormat("#00.0"),
    	        	new DecimalFormat("#00.00"),
    	        	new DecimalFormat("#00.000"),
    	        	new DecimalFormat("#00.0000"),
    	        	new DecimalFormat("#00.00000"),
    	        	new DecimalFormat("#00.000000")
    			}, {
    				new DecimalFormat("#000"),
    				new DecimalFormat("#000.0"),
    				new DecimalFormat("#000.00"),
    				new DecimalFormat("#000.000"),
    				new DecimalFormat("#000.0000"),
    				new DecimalFormat("#000.00000"),
    				new DecimalFormat("#000.000000")
    			}, {
    				new DecimalFormat("#0000"),
    				new DecimalFormat("#0000.0"),
    				new DecimalFormat("#0000.00"),
    				new DecimalFormat("#0000.000"),
    				new DecimalFormat("#0000.0000"),
    				new DecimalFormat("#0000.00000"),
    				new DecimalFormat("#0000.000000")
    			}, {
    				new DecimalFormat("#00000"),
    				new DecimalFormat("#00000.0"),
    				new DecimalFormat("#00000.00"),
    				new DecimalFormat("#00000.000"),
    				new DecimalFormat("#00000.0000"),
    				new DecimalFormat("#00000.00000"),
    				new DecimalFormat("#00000.000000")
    			}, {
    				new DecimalFormat("#000000"),
    				new DecimalFormat("#000000.0"),
    				new DecimalFormat("#000000.00"),
    				new DecimalFormat("#000000.000"),
    				new DecimalFormat("#000000.0000"),
    				new DecimalFormat("#000000.00000"),
    				new DecimalFormat("#000000.000000")
    			}, {
    				new DecimalFormat("#0000000"),
    				new DecimalFormat("#0000000.0"),
    				new DecimalFormat("#0000000.00"),
    				new DecimalFormat("#0000000.000"),
    				new DecimalFormat("#0000000.0000"),
    				new DecimalFormat("#0000000.00000"),
    				new DecimalFormat("#0000000.000000")
    			}
        	};
		for (DecimalFormat[] format : formats) {
			for (DecimalFormat aFormat : format) {
				aFormat.setDecimalFormatSymbols(
						new DecimalFormatSymbols(Locale.ENGLISH));
			}
		}
    }
}
