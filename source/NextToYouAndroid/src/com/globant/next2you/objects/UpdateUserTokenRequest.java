package com.globant.next2you.objects;

public class UpdateUserTokenRequest {

	private int id;

	public UpdateUserTokenRequest() {
	}

	public UpdateUserTokenRequest(int id) {
		super();
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
