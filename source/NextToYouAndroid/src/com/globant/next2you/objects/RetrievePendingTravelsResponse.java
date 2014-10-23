package com.globant.next2you.objects;

import java.util.ArrayList;

public class RetrievePendingTravelsResponse {
	private ArrayList<Travel> travels;

	public RetrievePendingTravelsResponse() {
		super();
    }
	
	public RetrievePendingTravelsResponse(ArrayList<Travel> travels) {
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
