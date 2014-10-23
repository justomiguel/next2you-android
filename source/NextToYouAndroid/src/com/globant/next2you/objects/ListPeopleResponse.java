package com.globant.next2you.objects;

import java.util.ArrayList;

public class ListPeopleResponse {
	
	ArrayList<Person> people;

	public ListPeopleResponse() {
	}

	public ListPeopleResponse(ArrayList<Person> people) {
		super();
		this.people = people;
	}

	public ArrayList<Person> getPeople() {
		return people;
	}

	public void setPeople(ArrayList<Person> people) {
		this.people = people;
	}

}
