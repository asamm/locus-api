package locus.api.objects.utils;

import locus.api.objects.extra.Location;

public class LocationCompute {

    private Location loc;

    // cache the inputs and outputs of computeDistanceAndBearing
    // so calls to distanceTo() and bearingTo() can share work
    private double mLat1 = 0.0;
    private double mLon1 = 0.0;
    private double mLat2 = 0.0;
    private double mLon2 = 0.0;
    private final float[] mResults = new float[2];

    // WGS84 parameters for compute
    private static final double parWgs84AxisA = 6378137.0;
    private static final double parWgs84AxisB = 6356752.3142;
    private static final double parWgs84Flat = (parWgs84AxisA - parWgs84AxisB) / parWgs84AxisA;

    public LocationCompute(Location loc) {
        this.loc = loc;
    }

    public static void computeDistanceAndBearing(double lat1, double lon1,
            double lat2, double lon2, float[] results) {
        computeDistanceAndBearing(lat1, lon1, lat2, lon2,
                parWgs84AxisA, parWgs84AxisB, parWgs84Flat, results);
    }

    public static void computeDistanceAndBearing(double lat1, double lon1, double lat2, double lon2,
            double a, double b, double f, float[] results) {
        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        // using the "Inverse Formula" (section 4)

        int MAXITERS = 20;
        // Convert lat/long to radians
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;

        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

        double L = lon2 - lon1;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;

        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha;
        double cos2SM;
        double cosSigma;
        double sinSigma;
        double cosLambda = 0.0;
        double sinLambda = 0.0;

        double lambda = L; // initial guess
        for (int iter = 0; iter < MAXITERS; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
            sigma = Math.atan2(sinSigma, cosSigma); // (16)
            double sinAlpha = (sinSigma == 0) ? 0.0 : cosU1cosU2 * sinLambda
                    / sinSigma; // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 : cosSigma - 2.0 * sinU1sinU2
                    / cosSqAlpha; // (18)
            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn

            double A1 = (uSquared / 16384.0) * (4096.0 + uSquared * (-768.0 + uSquared * (320.0 - 175.0 * uSquared)));
            A = 1.0 + A1;
            double B = (uSquared / 1024.0) * // (4)
                    (256.0 + uSquared * (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B * sinSigma * // (6)
                    (cos2SM + (B / 4.0) * (cosSigma * (-1.0 + 2.0 * cos2SMSq) - (B / 6.0)
                            * cos2SM * (-3.0 + 4.0 * sinSigma * sinSigma)
                            * (-3.0 + 4.0 * cos2SMSq)));
            lambda = L
                    + (1.0 - C)
                    * f
                    * sinAlpha
                    * (sigma + C
                    * sinSigma
                    * (cos2SM + C * cosSigma
                    * (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }

        float distance = (float) (b * A * (sigma - deltaSigma));
        results[0] = distance;
        if (results.length > 1) {
            float initialBearing = (float) Math.atan2(cosU2 * sinLambda, cosU1
                    * sinU2 - sinU1 * cosU2 * cosLambda);
            initialBearing *= 180.0 / Math.PI;
            results[1] = initialBearing;
            if (results.length > 2) {
                float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
                        -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
                finalBearing *= 180.0 / Math.PI;
                results[2] = finalBearing;
            }
        }
    }

    /**
     * Computes the approximate distance in meters between two locations, and
     * optionally the initial and final bearings of the shortest path between
     * them. Distance and bearing are defined using the WGS84 ellipsoid.
     * <p>
     * The computed distance is stored in results[0]. If results has length 2 or
     * greater, the initial bearing is stored in results[1]. If results has
     * length 3 or greater, the final bearing is stored in results[2].
     *
     * @param startLatitude  the starting latitude
     * @param startLongitude the starting longitude
     * @param endLatitude    the ending latitude
     * @param endLongitude   the ending longitude
     * @param results        an array of floats to hold the results
     * @throws IllegalArgumentException if results is null or has length less then 1
     */
    public static void distanceBetween(double startLatitude,
            double startLongitude, double endLatitude, double endLongitude,
            float[] results) {
        if (results == null || results.length < 1) {
            throw new IllegalArgumentException(
                    "results is null or has length < 1");
        }
        computeDistanceAndBearing(startLatitude, startLongitude, endLatitude,
                endLongitude, results);
    }

    /**
     * Returns the approximate distance in meters between this location and the
     * given location. Distance is defined using the WGS84 ellipsoid.
     *
     * @param dest the destination location
     * @return the approximate distance in meters
     */
    public float distanceTo(Location dest) {
        // See if we already have the result
        synchronized (mResults) {
            if (loc.latitude != mLat1 || loc.longitude != mLon1
                    || dest.latitude != mLat2 || dest.longitude != mLon2) {
                computeDistanceAndBearing(loc.latitude, loc.longitude, dest.latitude,
                        dest.longitude, mResults);
                mLat1 = loc.latitude;
                mLon1 = loc.longitude;
                mLat2 = dest.latitude;
                mLon2 = dest.longitude;
            }
            return mResults[0];
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
    public float bearingTo(Location dest) {
        synchronized (mResults) {
            // See if we already have the result
            if (loc.latitude != mLat1 || loc.longitude != mLon1
                    || dest.latitude != mLat2 || dest.longitude != mLon2) {
                computeDistanceAndBearing(loc.latitude, loc.longitude, dest.latitude,
                        dest.longitude, mResults);
                mLat1 = loc.latitude;
                mLon1 = loc.longitude;
                mLat2 = dest.latitude;
                mLon2 = dest.longitude;
            }
            return mResults[1];
        }
    }

    //*************************************************
    // FAST STATIC COMPUTE
    //*************************************************

    public static final double AVERAGE_RADIUS_OF_EARTH = 6371000.0;

    /**
     * Temporary variable for fast location computes. Use in case, you
     * need only a distance result
     */
    private static double[] mDistResult = new double[1];

    /**
     * Compute distance on Earth approximated as sphere.
     *
     * @param loc1 first location
     * @param loc2 second location
     * @return computed distance in metres
     */
    public static synchronized double computeDistanceFast(Location loc1, Location loc2) {
        computeDistanceAndBearingFast(
                loc1.getLatitude(), loc1.getLongitude(),
                loc2.getLatitude(), loc2.getLongitude(), mDistResult);
        return mDistResult[0];
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
    public static synchronized double computeDistanceFast(double lat1, double lon1,
            double lat2, double lon2) {
        computeDistanceAndBearingFast(
                lat1, lon1, lat2, lon2, mDistResult);
        return mDistResult[0];
    }

    /**
     * Compute distance and bearing on Earth approximated as sphere.
     * <br><br>Compute is based on Haversine formula http://en.wikipedia.org/wiki/Haversine_formula
     * <br><br>Precision is around 99.9%, speed around 5x faster then above method.
     *
     * @param lat1    latitude of first point
     * @param lon1    longitude of first point
     * @param lat2    latitude of second point
     * @param lon2    longitude of second point
     * @param results one (only distance) or two sized array for a results
     */
    public static void computeDistanceAndBearingFast(double lat1, double lon1,
            double lat2, double lon2, double[] results) {
        // convert lat/long to radians
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;

        // prepare variables
        double cosLat1 = Math.cos(lat1);
        double cosLat2 = Math.cos(lat2);
        double sinDLat2 = Math.sin((lat2 - lat1) / 2.0);
        double sinDLon2 = Math.sin((lon2 - lon1) / 2.0);

        // compute values
        double a = sinDLat2 * sinDLat2 + cosLat1 * cosLat2 * sinDLon2 * sinDLon2;
        double d = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        // convert to metres
        results[0] = d * LocationCompute.AVERAGE_RADIUS_OF_EARTH;

        // compute bearing
        if (results.length > 1) {
            double y = Math.sin(lon2 - lon1) * cosLat2;
            double x = cosLat1 * Math.sin(lat2) - Math.sin(lat1) * cosLat2 * Math.cos(lon2 - lon1);
            results[1] = Math.toDegrees(Math.atan2(y, x));
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
