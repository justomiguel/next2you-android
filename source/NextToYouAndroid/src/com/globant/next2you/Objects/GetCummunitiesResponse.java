package com.globant.next2you.Objects;

import java.util.ArrayList;

public class GetCummunitiesResponse {

	private ArrayList<Community> communities;

	public GetCummunitiesResponse() {
	}

	public GetCummunitiesResponse(ArrayList<Community> communities) {
		super();
		this.communities = communities;
	}

	public ArrayList<Community> getCommunities() {
		return communities;
	}

	public void setCommunities(ArrayList<Community> communities) {
		this.communities = communities;
	}

}
