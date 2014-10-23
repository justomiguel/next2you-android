package com.globant.next2you.objects;

public class Community {
	private int communityId;
	private String name;

	public Community() {
	}

	public Community(int id, String name) {
		super();
		this.setCommunityId(id);
		this.name = name;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCommunityId() {
		return communityId;
	}

	public void setCommunityId(int communityId) {
		this.communityId = communityId;
	}

}