package com.erudika.scoold.models;

import com.erudika.para.client.ParaClient;
import com.erudika.para.core.Sysprop;
import com.erudika.scoold.utils.ScooldUtils;

public class Category extends Sysprop {

	public Category() {
		this(null);
	}

	public Category(String name) {
		setName(name);
	}

	private ParaClient client() {
		return ScooldUtils.getInstance().getParaClient();
	}

	public String create() {
		client().create(this);
		return getId();
	}

	public void delete() {
		client().delete(this);
	}
}
