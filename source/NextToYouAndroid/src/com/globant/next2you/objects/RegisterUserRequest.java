package com.globant.next2you.objects;

public class RegisterUserRequest {

	private String name;
	private String email;
	private int communityId;

	public RegisterUserRequest() {
	}

	public RegisterUserRequest(String name, String email, int communityId) {
		super();
		this.name = name;
		this.email = email;
		this.communityId = communityId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getCommunityId() {
		return communityId;
	}

	public void setCommunityId(int communityId) {
		this.communityId = communityId;
	}

}
