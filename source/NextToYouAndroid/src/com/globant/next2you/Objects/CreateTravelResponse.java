package com.globant.next2you.objects;

public class CreateTravelResponse {
	private int travelId;

	public CreateTravelResponse() {
	}

	public CreateTravelResponse(int travelId) {
		super();
		this.travelId = travelId;
	}

	public int getTravelId() {
		return travelId;
	}

	public void setTravelId(int travelId) {
		this.travelId = travelId;
	}

}
