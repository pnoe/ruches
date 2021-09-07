package ooioo.ruches;

/**
 * Latitude et longitude
 */
public class LatLon {
	private Float lat;
	private Float lon;

	public LatLon(Float lat, Float lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public Float getLat() {
		return lat;
	}

	public void setLat(Float lat) {
		this.lat = lat;
	}

	public Float getLon() {
		return lon;
	}

	public void setLon(Float lon) {
		this.lon = lon;
	}

}
