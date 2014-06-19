package com.globant.next2you.Objects;

import java.util.ArrayList;

public class RetrievePendingTravelsResponse {

	ArrayList<Travel> travels;

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
