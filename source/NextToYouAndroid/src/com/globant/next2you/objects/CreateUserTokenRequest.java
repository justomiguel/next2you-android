package com.globant.next2you.objects;

public class CreateUserTokenRequest {

	private String username;
	private String password;

	public CreateUserTokenRequest() {
		
	}

	public CreateUserTokenRequest(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
