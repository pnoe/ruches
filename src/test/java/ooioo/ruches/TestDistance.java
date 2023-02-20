package ooioo.ruches;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDistance {

	private final Logger logger = LoggerFactory.getLogger(TestDistance.class);

	/**
	 * Comparaison calcul de distance Utils.distance (algo Haversine) avec algorithme Vincenty.
	 */
	@Test
	void distanceLatLon() {
		double r1Lat = 43.5384;
		double r1Lon = 5.5267;
		double r2Lat = 43.5384;
		double r2Lon = 5.5265;
		double dTerre = Utils.rTerreLat(r1Lat) * 2d;
		logger.info(
				"**********************************************************************************");
		logger.info("Vincenty " + TestDistancVincenty.vincentyDistance(r1Lat, r2Lat, r1Lon, r2Lon) + 
				" Traversine " + Utils.distance(dTerre, r1Lat, r2Lat, r1Lon, r2Lon));
		assertEquals(Utils.distance(dTerre, r1Lat, r2Lat, r1Lon, r2Lon), 
				TestDistancVincenty.vincentyDistance(r1Lat, r2Lat, r1Lon, r2Lon), 0.1);
	}

}