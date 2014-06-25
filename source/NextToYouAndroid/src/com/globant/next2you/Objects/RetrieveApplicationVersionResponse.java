package com.globant.next2you.objects;

public class RetrieveApplicationVersionResponse {
	
	private int major;
	private int minor;
	private int build;
	private int release;
	private String version;
	private String createdDate;

	public RetrieveApplicationVersionResponse() {
	}

	public RetrieveApplicationVersionResponse(int major, int minor, int build,
			int release, String version, String createdDate) {
		super();
		this.major = major;
		this.minor = minor;
		this.build = build;
		this.release = release;
		this.version = version;
		this.createdDate = createdDate;
	}

	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public int getMinor() {
		return minor;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	public int getBuild() {
		return build;
	}

	public void setBuild(int build) {
		this.build = build;
	}

	public int getRelease() {
		return release;
	}

	public void setRelease(int release) {
		this.release = release;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

}
