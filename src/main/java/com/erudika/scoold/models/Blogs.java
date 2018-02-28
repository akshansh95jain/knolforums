package com.erudika.scoold.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Blogs {

	private Integer found;
	@SerializedName("posts")
	private List<Blog> blogs;

	public Integer getFound() {
		return found;
	}

	public void setFound(Integer found) {
		this.found = found;
	}

	public List<Blog> getBlogs() {
		return blogs;
	}

	public void setBlogs(List<Blog> posts) {
		this.blogs = posts;
	}
}
