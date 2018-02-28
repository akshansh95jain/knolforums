package com.erudika.scoold.models;

import java.util.List;

public class Blogs {

	private Integer found;
	private List<Post> posts;

	public Integer getFound() {
		return found;
	}

	public void setFound(Integer found) {
		this.found = found;
	}

	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}
}
