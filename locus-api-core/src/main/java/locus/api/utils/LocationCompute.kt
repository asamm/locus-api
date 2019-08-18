package locus.api.utils

import locus.api.objects.extra.Location

@Suppress("LocalVariableName")
class LocationCompute(private val loc: Location) {

    // cache the inputs and outputs of computeDistanceAndBearing
    // so calls to distanceTo() and bearingTo() can share work
    private var mLat1 = 0.0
    private var mLon1 = 0.0
    private var mLat2 = 0.0
    private var mLon2 = 0.0
    private val results = FloatArray(2)

    /**
     * Returns the approximate distance in meters between this location and the
     * given location. Distance is defined using the WGS84 ellipsoid.
     *
     * @param dest the destination location
     * @return the approximate distance in meters
     */
    fun distanceTo(dest: Location): Float {
        // See if we already have the result
        synchronized(results) {
            if (loc.latitude != mLat1 || loc.longitude != mLon1
                    || dest.latitude != mLat2 || dest.longitude != mLon2) {
                computeDistanceAndBearing(loc.latitude, loc.longitude, dest.latitude,
                        dest.longitude, results)
                mLat1 = loc.latitude
                mLon1 = loc.longitude
                mLat2 = dest.latitude
                mLon2 = dest.longitude
            }
            return results[0]
        }
    }

    /**
     * Returns the approximate initial bearing in degrees East of true North
     * when traveling along the shortest path between this location and the
     * given location. The shortest path is defined using the WGS84 ellipsoid.
     * Locations that are (nearly) antipodal may produce meaningless results.
     *
     * @param dest the destination location
     * @return the initial bearing in degrees
     */
    fun bearingTo(dest: Location): Float {
        synchronized(results) {
            // See if we already have the result
            if (loc.latitude != mLat1 || loc.longitude != mLon1
                    || dest.latitude != mLat2 || dest.longitude != mLon2) {
                computeDistanceAndBearing(loc.latitude, loc.longitude, dest.latitude,
                        dest.longitude, results)
                mLat1 = loc.latitude
                mLon1 = loc.longitude
                mLat2 = dest.latitude
                mLon2 = dest.longitude
            }
            return results[1]
        }
    }

    companion object {

        // WGS84 parameters for compute
        private const val parWgs84AxisA = 6378137.0
        private const val parWgs84AxisB = 6356752.3142
        private const val parWgs84Flat = (parWgs84AxisA - parWgs84AxisB) / parWgs84AxisA

        fun computeDistanceAndBearing(lat1: Double, lon1: Double,
                lat2: Double, lon2: Double, results: FloatArray) {
            computeDistanceAndBearing(lat1, lon1, lat2, lon2,
                    parWgs84AxisA, parWgs84AxisB, parWgs84Flat, results)
        }

        fun computeDistanceAndBearing(latP1: Double, lonP1: Double, latP2: Double, lonP2: Double,
                a: Double, b: Double, f: Double, results: FloatArray) {
            // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
            // using the "Inverse Formula" (section 4)

            val MAXITERS = 20
            // Convert lat/long to radians
            val lat1 = latP1 * Math.PI / 180.0
            val lat2 = latP2 * Math.PI / 180.0
            val lon1 = lonP1 * Math.PI / 180.0
            val lon2 = lonP2 * Math.PI / 180.0

            val aSqMinusBSqOverBSq = (a * a - b * b) / (b * b)

            val L = lon2 - lon1
            var A = 0.0
            val U1 = Math.atan((1.0 - f) * Math.tan(lat1))
            val U2 = Math.atan((1.0 - f) * Math.tan(lat2))

            val cosU1 = Math.cos(U1)
            val cosU2 = Math.cos(U2)
            val sinU1 = Math.sin(U1)
            val sinU2 = Math.sin(U2)
            val cosU1cosU2 = cosU1 * cosU2
            val sinU1sinU2 = sinU1 * sinU2

            var sigma = 0.0
            var deltaSigma = 0.0
            var cosSqAlpha: Double
            var cos2SM: Double
            var cosSigma: Double
            var sinSigma: Double
            var cosLambda = 0.0
            var sinLambda = 0.0

            var lambda = L // initial guess
            for (iter in 0 until MAXITERS) {
                val lambdaOrig = lambda
                cosLambda = Math.cos(lambda)
                sinLambda = Math.sin(lambda)
                val t1 = cosU2 * sinLambda
                val t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda
                val sinSqSigma = t1 * t1 + t2 * t2 // (14)
                sinSigma = Math.sqrt(sinSqSigma)
                cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda // (15)
                sigma = Math.atan2(sinSigma, cosSigma) // (16)
                val sinAlpha = if (sinSigma == 0.0)
                    0.0
                else
                    cosU1cosU2 * sinLambda / sinSigma // (17)
                cosSqAlpha = 1.0 - sinAlpha * sinAlpha
                cos2SM = if (cosSqAlpha == 0.0)
                    0.0
                else
                    cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha // (18)
                val uSquared = cosSqAlpha * aSqMinusBSqOverBSq // defn

                val A1 = uSquared / 16384.0 * (4096.0 + uSquared * (-768.0 + uSquared * (320.0 - 175.0 * uSquared)))
                A = 1.0 + A1
                val B = uSquared / 1024.0 * // (4)
                        (256.0 + uSquared * (-128.0 + uSquared * (74.0 - 47.0 * uSquared)))
                val C = f / 16.0 * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha)) // (10)
                val cos2SMSq = cos2SM * cos2SM
                deltaSigma = B * sinSigma * // (6)

                        (cos2SM + B / 4.0 * (cosSigma * (-1.0 + 2.0 * cos2SMSq) - (B / 6.0
                                * cos2SM * (-3.0 + 4.0 * sinSigma * sinSigma)
                                * (-3.0 + 4.0 * cos2SMSq))))
                lambda = L + ((1.0 - C)
                        * f
                        * sinAlpha
                        * (sigma + (C
                        * sinSigma
                        * (cos2SM + (C * cosSigma
                        * (-1.0 + 2.0 * cos2SM * cos2SM)))))) // (11)

                val delta = (lambda - lambdaOrig) / lambda
                if (Math.abs(delta) < 1.0e-12) {
                    break
                }
            }

            val distance = (b * A * (sigma - deltaSigma)).toFloat()
            results[0] = distance
            if (results.size > 1) {
                var initialBearing = Math.atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda).toFloat()
                initialBearing *= (180.0 / Math.PI).toFloat()
                results[1] = initialBearing
                if (results.size > 2) {
                    var finalBearing = Math.atan2(cosU1 * sinLambda,
                            -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda).toFloat()
                    finalBearing *= (180.0 / Math.PI).toFloat()
                    results[2] = finalBearing
                }
            }
        }

        /**
         * Computes the approximate distance in meters between two locations, and
         * optionally the initial and final bearings of the shortest path between
         * them. Distance and bearing are defined using the WGS84 ellipsoid.
         *
         *
         * The computed distance is stored in results[0]. If results has length 2 or
         * greater, the initial bearing is stored in results[1]. If results has
         * length 3 or greater, the final bearing is stored in results[2].
         *
         * @param startLatitude  the starting latitude
         * @param startLongitude the starting longitude
         * @param endLatitude    the ending latitude
         * @param endLongitude   the ending longitude
         * @param results        an array of floats to hold the results
         */
        fun distanceBetween(startLatitude: Double,
                startLongitude: Double, endLatitude: Double, endLongitude: Double,
                results: FloatArray?) {
            if (results == null || results.isEmpty()) {
                throw IllegalArgumentException(
                        "results is null or has length < 1")
            }
            computeDistanceAndBearing(startLatitude, startLongitude, endLatitude,
                    endLongitude, results)
        }

        //*************************************************
        // FAST STATIC COMPUTE
        //*************************************************

        const val AVERAGE_RADIUS_OF_EARTH = 6371000.0

        /**
         * Temporary variable for fast location computes. Use in case, you
         * need only a distance result
         */
        private val mDistResult = DoubleArray(1)

        /**
         * Compute distance on Earth approximated as sphere.
         *
         * @param loc1 first location
         * @param loc2 second location
         * @return computed distance in metres
         */
        @Synchronized
        fun computeDistanceFast(loc1: Location, loc2: Location): Double {
            computeDistanceAndBearingFast(
                    loc1.latitude, loc1.longitude,
                    loc2.latitude, loc2.longitude, mDistResult)
            return mDistResult[0]
        }

        /**
         * Compute distance on Earth approximated as sphere.
         *
         * @param lat1 latitude of first point
         * @param lon1 longitude of first point
         * @param lat2 latitude of second point
         * @param lon2 longitude of second point
         * @return computed distance in metres
         */
        @Synchronized
        fun computeDistanceFast(lat1: Double, lon1: Double,
                lat2: Double, lon2: Double): Double {
            computeDistanceAndBearingFast(
                    lat1, lon1, lat2, lon2, mDistResult)
            return mDistResult[0]
        }

        /**
         * Compute distance and bearing on Earth approximated as sphere.
         * <br></br><br></br>Compute is based on Haversine formula http://en.wikipedia.org/wiki/Haversine_formula
         * <br></br><br></br>Precision is around 99.9%, speed around 5x faster then above method.
         *
         * @param latP1    latitude of first point
         * @param lonP1    longitude of first point
         * @param latP2    latitude of second point
         * @param lonP2    longitude of second point
         * @param results one (only distance) or two sized array for a results
         */
        fun computeDistanceAndBearingFast(latP1: Double, lonP1: Double,
                latP2: Double, lonP2: Double, results: DoubleArray) {
            // convert lat/long to radians
            val lat1 = latP1 * Math.PI / 180.0
            val lat2 = latP2 * Math.PI / 180.0
            val lon1 = lonP1 * Math.PI / 180.0
            val lon2 = lonP2 * Math.PI / 180.0

            // prepare variables
            val cosLat1 = Math.cos(lat1)
            val cosLat2 = Math.cos(lat2)
            val sinDLat2 = Math.sin((lat2 - lat1) / 2.0)
            val sinDLon2 = Math.sin((lon2 - lon1) / 2.0)

            // compute values
            val a = sinDLat2 * sinDLat2 + cosLat1 * cosLat2 * sinDLon2 * sinDLon2
            val d = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a))

            // convert to metres
            results[0] = d * AVERAGE_RADIUS_OF_EARTH

            // compute bearing
            if (results.size > 1) {
                val y = Math.sin(lon2 - lon1) * cosLat2
                val x = cosLat1 * Math.sin(lat2) - Math.sin(lat1) * cosLat2 * Math.cos(lon2 - lon1)
                results[1] = Math.toDegrees(Math.atan2(y, x))
            }

            // LAW OF TRIANGLES - problem with ACOS

            //		// prepare variables
            //		double sinLat1 = Math.sin(lat1);
            //		double cosLat1 = Math.cos(lat1);
            //		double sinLat2 = Math.sin(lat2);
            //		double cosLat2 = Math.cos(lat2);
            //
            //		double sinDLon = Math.sin(lon2-lon1);
            //		double cosDLon = Math.cos(lon2-lon1);
            //
            //		// compute distance
            //		double d = Math.acos(sinLat1 * sinLat2 + cosLat1 * cosLat2 * cosDLon);
            //
            //		// convert to metres
            //		results[0] = d * AVERAGE_RADIUS_OF_EARTH;
            //
            //		// compute bearing
            //		double y = sinDLon * cosLat2;
            //		double x = cosLat1 * sinLat2 - sinLat1 * cosLat2 * cosDLon;
            //		results[1] = Math.toDegrees(Math.atan2(y, x));
        }
    }
}
