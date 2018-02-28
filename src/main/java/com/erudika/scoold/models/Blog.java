package com.erudika.scoold.models;

import com.google.gson.annotations.SerializedName;

public class Blog {

	private String title;
	@SerializedName("URL")
	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
