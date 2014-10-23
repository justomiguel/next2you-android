package com.globant.next2you.objects;

public class CreateTravelRequest {

	private double fromLatitude;
	private double fromLongitude;
	private double toLatitude;
	private double toLongitude;
	private boolean hasCar;
	private String timeSpan;

	public CreateTravelRequest() {
	}

	public CreateTravelRequest(double fromLatitude, double fromLongitude,
			double toLatitude, double toLongitude, boolean hasCar,
			String timeSpan) {
		super();
		this.fromLatitude = fromLatitude;
		this.fromLongitude = fromLongitude;
		this.toLatitude = toLatitude;
		this.toLongitude = toLongitude;
		this.hasCar = hasCar;
		this.timeSpan = timeSpan;
	}

	public double getFromLatitude() {
		return fromLatitude;
	}

	public void setFromLatitude(double fromLatitude) {
		this.fromLatitude = fromLatitude;
	}

	public double getFromLongitude() {
		return fromLongitude;
	}

	public void setFromLongitude(double fromLongitude) {
		this.fromLongitude = fromLongitude;
	}

	public double getToLatitude() {
		return toLatitude;
	}

	public void setToLatitude(double toLatitude) {
		this.toLatitude = toLatitude;
	}

	public double getToLongitude() {
		return toLongitude;
	}

	public void setToLongitude(double toLongitude) {
		this.toLongitude = toLongitude;
	}

	public boolean getHasCar() {
		return hasCar;
	}

	public void setHasCar(boolean hasCar) {
		this.hasCar = hasCar;
	}

	public String getTimeSpan() {
		return timeSpan;
	}

	public void setTimeSpan(String timeSpan) {
		this.timeSpan = timeSpan;
	}

}
