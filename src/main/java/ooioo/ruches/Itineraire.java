package ooioo.ruches;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Itineraire {
	private Float distance;
	private Float duration;
	public Float getDistance() {
		return distance;
	}
	public void setDistance(Float distance) {
		this.distance = distance;
	}
	public Float getDuration() {
		return duration;
	}
	public void setDuration(Float duration) {
		this.duration = duration;
	}
	@Override
	public String toString() {
		return "Itineraire [distance=" + distance + ", duration=" + duration + "]";
	}
	
	
}
