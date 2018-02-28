package com.erudika.scoold.controllers;

import com.erudika.scoold.utils.ScooldUtils;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/learn")
public class LearnController {

	private final ScooldUtils utils;
	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	@Inject
	public LearnController(ScooldUtils utils) {
		this.utils = utils;
	}

	@GetMapping
	public String get(HttpServletRequest req, Model model) {
		List<String> blogs = null;
		try {
			blogs = getBlogs();
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		model.addAttribute("blogs", blogs);
		model.addAttribute("path", "learn.vm");
		model.addAttribute("title", "Learn");
		model.addAttribute("learnSelected", "navbtn-hover");
		return "base";
	}

	public static List<String> getBlogs() throws IOException {
		URL url = new URL("https://public-api.wordpress.com/rest/v1.1/sites/blog.knoldus.com/posts");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuilder content = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();

		Blogs blogs = new Gson().fromJson(content.toString(), Blogs.class);
		List<Post> posts = blogs.getPosts();
		List<String> titles = new ArrayList<String>();
		for (int post = 0; post < posts.size(); post++) {
			titles.add(posts.get(post).getTitle());
		}

		return titles;

		//return content.toString();
	}
}
