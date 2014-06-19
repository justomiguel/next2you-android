package com.globant.next2you.Objects;

public class CreateUserTokenResponse {

	private boolean authenticate;
	private String token;

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

}
