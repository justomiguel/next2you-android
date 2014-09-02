package com.globant.next2you.objects;

import java.util.ArrayList;

public class TravelPersonResponse {
	private ArrayList<TravelPerson> travels;
	
	public TravelPersonResponse() {
		super();
	}

	public TravelPersonResponse(ArrayList<TravelPerson> travels) {
		super();
		this.travels = travels;
	}

	public ArrayList<TravelPerson> getTravels() {
		return travels;
	}

	public void setTravels(ArrayList<TravelPerson> travels) {
		this.travels = travels;
	}
}
