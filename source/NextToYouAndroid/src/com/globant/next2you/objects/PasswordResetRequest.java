package com.globant.next2you.objects;

public class PasswordResetRequest {

	private String email;

	public PasswordResetRequest() {
	}

	public PasswordResetRequest(String email) {
		super();
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
