package com.globant.next2you.objects;

import java.util.ArrayList;

public class CreateUserTokenResponse {

	private boolean authenticate;
	private String token;
	private ArrayList<Community> communities;
	
	public CreateUserTokenResponse() {
	}

	public CreateUserTokenResponse(boolean authenticate, String token) {
		super();
		this.authenticate = authenticate;
		this.token = token;
	}

	public boolean isAuthenticate() {
		return authenticate;
	}

	public void setAuthenticate(boolean authenticate) {
		this.authenticate = authenticate;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public ArrayList<Community> getCommunities() {
		return communities;
	}

	public void setCommunities(ArrayList<Community> communities) {
		this.communities = communities;
	}

}
