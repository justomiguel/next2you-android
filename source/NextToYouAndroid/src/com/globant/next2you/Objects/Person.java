package com.globant.next2you.Objects;

public class Person {
	private int personId;
	private String name;
	private String email;
	private String nickname;
	private String comments;
	private String address;
	private String city;
	private String country;
	private double latitude;
	private double longitude;
	private int destinationId;
	private String destination;
	private String image;
	private String comment;
	private boolean isAddressVisible;
	private String icon;
	private String color;
	private boolean isAdmin;
	private String currentCommunity;
	private boolean hasCar;
	private boolean hasActiveTravel;

	public Person() {
	}

	public Person(int personId, String name, String email,
			String nickname, String comments, String address, String city,
			String country, double latitude, double longitude,
			int destinationId, String destination, String image,
			String comment, boolean isAddressVisible, String icon,
			String color, boolean isAdmin, String currentCommunity,
			boolean hasCar, boolean hasActiveTravel) {
		super();
		this.personId = personId;
		this.name = name;
		this.email = email;
		this.nickname = nickname;
		this.comments = comments;
		this.address = address;
		this.city = city;
		this.country = country;
		this.latitude = latitude;
		this.longitude = longitude;
		this.destinationId = destinationId;
		this.destination = destination;
		this.image = image;
		this.comment = comment;
		this.isAddressVisible = isAddressVisible;
		this.icon = icon;
		this.color = color;
		this.isAdmin = isAdmin;
		this.currentCommunity = currentCommunity;
		this.hasCar = hasCar;
		this.hasActiveTravel = hasActiveTravel;
	}

	public int getPersonId() {
		return personId;
	}

	public void setPersonId(int personId) {
		this.personId = personId;
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

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public int getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(int destinationId) {
		this.destinationId = destinationId;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean getIsAddressVisible() {
		return isAddressVisible;
	}

	public void setIsAddressVisible(boolean isAddressVisible) {
		this.isAddressVisible = isAddressVisible;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public String getCurrentCommunity() {
		return currentCommunity;
	}

	public void setCurrentCommunity(String currentCommunity) {
		this.currentCommunity = currentCommunity;
	}

	public boolean getHasCar() {
		return hasCar;
	}

	public void setHasCar(boolean hasCar) {
		this.hasCar = hasCar;
	}

	public boolean getHasActiveTravel() {
		return hasActiveTravel;
	}

	public void setHasActiveTravel(boolean hasActiveTravel) {
		this.hasActiveTravel = hasActiveTravel;
	}

}
