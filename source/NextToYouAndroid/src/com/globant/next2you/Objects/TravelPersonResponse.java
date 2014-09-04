package com.globant.next2you.objects;

import java.util.ArrayList;

public class TravelPersonResponse {
	private ArrayList<Travel> travels;
	
	public TravelPersonResponse() {
		super();
	}

	public TravelPersonResponse(ArrayList<Travel> travels) {
		super();
		this.travels = travels;
	}

	public ArrayList<Travel> getTravels() {
		return travels;
	}

	public void setTravels(ArrayList<Travel> travels) {
		this.travels = travels;
	}
}
