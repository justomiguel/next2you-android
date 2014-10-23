package com.globant.next2you.objects;

public class Destination {

	private int destinationId;
	private String name;
	private String address;
	private double latitude;
	private double longitude;
	private String image;
	private String color;
	private int cityId;
	private String city;
	private String country;
	private String icon;
	private String personIcon;

	public Destination() {
	}

	public Destination(int destinationId, String name, String address,
			double latitude, double longitude, String image, String color,
			int cityId, String city, String country, String icon,
			String personIcon) {
		super();
		this.destinationId = destinationId;
		this.name = name;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.image = image;
		this.color = color;
		this.cityId = cityId;
		this.city = city;
		this.country = country;
		this.icon = icon;
		this.personIcon = personIcon;
	}

	public int getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(int destinationId) {
		this.destinationId = destinationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
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

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getPersonIcon() {
		return personIcon;
	}

	public void setPersonIcon(String personIcon) {
		this.personIcon = personIcon;
	}

}
