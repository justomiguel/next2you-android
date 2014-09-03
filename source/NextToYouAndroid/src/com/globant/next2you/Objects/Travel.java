package com.globant.next2you.objects;

public class Travel {

	private long travelId;
	private long personId;
	private String startTime;
	private String endTime;

	//Specific to api/travelPeople service
	private String personToBeApproved;
	private long personIdToBeApproved;
	
	//Specific to api/simpleTravels service
	private String fromLocation;
	private double fromLongitude;
	public String getFromLocation() {
		return fromLocation;
	}

	public void setFromLocation(String fromLocation) {
		this.fromLocation = fromLocation;
	}

	public double getFromLongitude() {
		return fromLongitude;
	}

	public void setFromLongitude(double fromLongitude) {
		this.fromLongitude = fromLongitude;
	}

	public double getFromLatitude() {
		return fromLatitude;
	}

	public void setFromLatitude(double fromLatitude) {
		this.fromLatitude = fromLatitude;
	}

	public String getToLocation() {
		return toLocation;
	}

	public void setToLocation(String toLocation) {
		this.toLocation = toLocation;
	}

	public double getToLongitude() {
		return toLongitude;
	}

	public void setToLongitude(double toLongitude) {
		this.toLongitude = toLongitude;
	}

	public double getToLatitude() {
		return toLatitude;
	}

	public void setToLatitude(double toLatitude) {
		this.toLatitude = toLatitude;
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

	private double fromLatitude;
	private String toLocation;
	private double toLongitude;
	private double toLatitude;

	//Latest api/simpleTravels service additions
	private boolean hasCar;
	private boolean owner;

	public Travel() {
	}
	
	//Constructor with all properties
	public Travel(long travelId, long personId, String startTime,
			String endTime, String personToBeApproved,
			long personIdToBeApproved, String fromLocation,
			double fromLongitude, double fromLatitude, String toLocation,
			double toLongitude, double toLatitude, boolean hasCar, boolean owner) {
		super();
		this.travelId = travelId;
		this.personId = personId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.personToBeApproved = personToBeApproved;
		this.personIdToBeApproved = personIdToBeApproved;
		this.fromLocation = fromLocation;
		this.fromLongitude = fromLongitude;
		this.fromLatitude = fromLatitude;
		this.toLocation = toLocation;
		this.toLongitude = toLongitude;
		this.toLatitude = toLatitude;
		this.hasCar = hasCar;
		this.owner = owner;
	}

	//Constructor with /api/simpleTravels related properties
	public Travel(long travelId, long personId, String startTime,
			String endTime, String fromLocation, double fromLongitude,
			double fromLatitude, String toLocation, double toLongitude,
			double toLatitude, boolean hasCar, boolean owner) {
		super();
		this.travelId = travelId;
		this.personId = personId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.personToBeApproved = "";
		this.personIdToBeApproved = personId;
		this.fromLocation = fromLocation;
		this.fromLongitude = fromLongitude;
		this.fromLatitude = fromLatitude;
		this.toLocation = toLocation;
		this.toLongitude = toLongitude;
		this.toLatitude = toLatitude;
		this.hasCar = hasCar;
		this.owner = owner;
	}	
	
	//Constructor with /api/travelPeople related properties
	public Travel(long travelId, long personId,
			String personToBeApproved, long personIdToBeApproved,
			String startTime, String endTime) {
		super();
		this.travelId = travelId;
		this.personId = personId;
		this.personToBeApproved = personToBeApproved;
		this.personIdToBeApproved = personIdToBeApproved;
		this.startTime = startTime;
		this.endTime = endTime;
		this.fromLocation = "";
		this.fromLongitude = 0;
		this.fromLatitude = 0;
		this.toLocation = "";
		this.toLongitude = 0;
		this.toLatitude = 0;
		this.hasCar = false;
		this.owner = false;
	}

	public long getTravelId() {
		return travelId;
	}

	public void setTravelId(long travelId) {
		this.travelId = travelId;
	}

	public long getPersonId() {
		return personId;
	}

	public void setPersonId(long personId) {
		this.personId = personId;
	}

	public String getPersonToBeApproved() {
		return personToBeApproved;
	}

	public void setPersonToBeApproved(String personToBeApproved) {
		this.personToBeApproved = personToBeApproved;
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

	public long getPersonIdToBeApproved() {
		return personIdToBeApproved;
	}

	public void setPersonIdToBeApproved(long personIdToBeApproved) {
		this.personIdToBeApproved = personIdToBeApproved;
	}

}
