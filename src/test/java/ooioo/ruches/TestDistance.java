package ooioo.ruches;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDistance {

	private final Logger logger = LoggerFactory.getLogger(TestDistance.class);

	@Test
	@DisplayName("Comparaison algo. Haversine et Vincenty")
	void distTravVinc() {
		double r1Lat = 43.5384d;
		double r1Lon = 5.5267d;
		double r2Lat = 43.5384d;
		double r2Lon = 5.5265d;
		logger.info(
				"**********************************************************************************");
		double dVinc = DistancVincenty.vincentyDistance(r1Lat, r2Lat, r1Lon, r2Lon);
		double dHav = Utils.distance(Utils.rTerreLat(r1Lat) * 2d, r1Lat, r2Lat, r1Lon, r2Lon);
		logger.info("Vincenty " + dVinc + " Haversine " + dHav + " Diff " + (dVinc - dHav));
		assertEquals(dVinc, dHav, 0.1);
	}

	@Test
	@DisplayName("Demi longueur équateur")
	void distanceLatLon() {
		double r1Lat = 0.0d;
		double r1Lon = -90.0d;
		double r2Lat = 0.0d;
		double r2Lon = 90.0d;
		logger.info(
				"**********************************************************************************");
		double dHav = Utils.distance(DistancVincenty.EQUATORIAL_RADIUS * 2d, r1Lat, r2Lat, r1Lon, r2Lon);
		double pir = Math.PI * DistancVincenty.EQUATORIAL_RADIUS;
		logger.info("PI R " + pir + " Haversine " + dHav + " Diff " + (pir - dHav));
		assertEquals(pir, dHav, 0.01);
	}

	@Test
	@DisplayName("Point départ et arrivée identiques")
	void distance0() {
		double rLat = 43.5384d;
		double rLon = 5.5267d;
		double dHav = Utils.distance(DistancVincenty.EQUATORIAL_RADIUS * 2d, rLat, rLat, rLon, rLon);
		assertEquals(0.0d, dHav);
	}

}