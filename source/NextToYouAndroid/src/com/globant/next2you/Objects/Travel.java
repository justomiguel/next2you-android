package com.globant.next2you.Objects;

public class Travel {

	private long travelId;
	private long travelPersonId;
	private String personToBeApproved;
	private String startTime;
	private String endTime;

	public Travel() {
	}

	public Travel(long travelId, long travelPersonId,
			String personToBeApproved, String startTime, String endTime) {
		super();
		this.travelId = travelId;
		this.travelPersonId = travelPersonId;
		this.personToBeApproved = personToBeApproved;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public long getTravelId() {
		return travelId;
	}

	public void setTravelId(long travelId) {
		this.travelId = travelId;
	}

	public long getTravelPersonId() {
		return travelPersonId;
	}

	public void setTravelPersonId(long travelPersonId) {
		this.travelPersonId = travelPersonId;
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

}
