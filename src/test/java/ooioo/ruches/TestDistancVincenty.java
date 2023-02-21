package ooioo.ruches;

public final class TestDistancVincenty {

	public static final double EQUATORIAL_RADIUS = 6378137.0;
	public static final double INVERSE_FLATTENING = 298.257223563;
	public static final double POLAR_RADIUS = 6356752.3142;

	/**
	 * Calcule la distance en mètres entre deux points lat, long en degrés.
	 * Méthode de Vincenty utilisant un ellipsoide.
	 * Cette méthode est plus longue que la méthode de Haversine et 
	 * n'a pas d'intérêt pour des points proches, distants d'une centaine de mètres.
	 * https://en.m.wikipedia.org/wiki/Earth_radius#Geocentric_radius
	 * http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
	 */
	public static double vincentyDistance(double lat1, double lat2, double long1, double long2) {
		double f = 1 / INVERSE_FLATTENING;
		double L = Math.toRadians(long2 - long1);
		double U1 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat1)));
		double U2 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat2)));
		double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);
		double lambda = L, lambdaP, iterLimit = 100;
		double cosSqAlpha = 0, sinSigma = 0, cosSigma = 0, cos2SigmaM = 0, sigma = 0, sinLambda = 0, sinAlpha = 0,
				cosLambda = 0;
		do {
			sinLambda = Math.sin(lambda);
			cosLambda = Math.cos(lambda);
			sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
					+ (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
			if (sinSigma == 0)
				return 0; // co-incident points
			cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
			sigma = Math.atan2(sinSigma, cosSigma);
			sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
			cosSqAlpha = 1 - sinAlpha * sinAlpha;
			if (cosSqAlpha != 0) {
				cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
			} else {
				cos2SigmaM = 0;
			}
			double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
			lambdaP = lambda;
			lambda = L + (1 - C) * f * sinAlpha
					* (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
		} while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);
		if (iterLimit == 0)
			return 0; // formula failed to converge
		double uSq = cosSqAlpha * (Math.pow(EQUATORIAL_RADIUS, 2) - Math.pow(POLAR_RADIUS, 2))
				/ Math.pow(POLAR_RADIUS, 2);
		double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
		double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
		double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)
				- B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
		double s = POLAR_RADIUS * A * (sigma - deltaSigma);
		return s;
	}
	
	private TestDistancVincenty() {
		throw new IllegalStateException("Constant class");
	}

}