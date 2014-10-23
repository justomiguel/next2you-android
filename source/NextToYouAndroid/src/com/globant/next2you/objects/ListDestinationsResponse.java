package com.globant.next2you.objects;

import java.util.ArrayList;

public class ListDestinationsResponse {

	ArrayList<Destination> destinations;

	public ListDestinationsResponse() {
	}

	public ListDestinationsResponse(ArrayList<Destination> destinations) {
		super();
		this.destinations = destinations;
	}

	public ArrayList<Destination> getDestinations() {
		return destinations;
	}

	public void setDestinations(ArrayList<Destination> destinations) {
		this.destinations = destinations;
	}

}
