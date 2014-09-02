package com.globant.next2you.objects;

public class TravelPerson {
	private int travelId;
	private int personId;
	private String startTime;
	private String endTime;
	private String fromLocation;
	private double fromLatitude;
	private double fromLongitude;
	private String toLocation;
	private double toLatitude;
	private double toLongitude;
	private boolean hasCar;
	private boolean owner;

	public TravelPerson() {
		super();
	}

	public TravelPerson(int travelId, int personId, String startTime,
			String endTime, String fromLocation, double fromLatitude,
			double fromLongitude, String toLocation, double toLatitude,
			double toLongitude, boolean hasCar, boolean owner) {
		super();
		this.travelId = travelId;
		this.personId = personId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.fromLocation = fromLocation;
		this.fromLatitude = fromLatitude;
		this.fromLongitude = fromLongitude;
		this.toLocation = toLocation;
		this.toLatitude = toLatitude;
		this.toLongitude = toLongitude;
		this.hasCar = hasCar;
		this.owner = owner;
	}

	public int getTravelId() {
		return travelId;
	}

	public void setTravelId(int travelId) {
		this.travelId = travelId;
	}

	public int getPersonId() {
		return personId;
	}

	public void setPersonId(int personId) {
		this.personId = personId;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getFromLocation() {
		return fromLocation;
	}

	public void setFromLocation(String fromLocation) {
		this.fromLocation = fromLocation;
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

	public String getToLocation() {
		return toLocation;
	}

	public void setToLocation(String toLocation) {
		this.toLocation = toLocation;
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

	public boolean isHasCar() {
		return hasCar;
	}

	public void setHasCar(boolean hasCar) {
		this.hasCar = hasCar;
	}

	public boolean isOwner() {
		return owner;
	}

	public void setOwner(boolean owner) {
		this.owner = owner;
	}
}
