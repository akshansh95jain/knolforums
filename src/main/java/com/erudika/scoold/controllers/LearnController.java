package com.erudika.scoold.controllers;

import com.erudika.para.client.ParaClient;
import com.erudika.para.utils.Pager;
import com.erudika.para.utils.Utils;
import com.erudika.scoold.models.Blog;
import com.erudika.scoold.models.Blogs;
import com.erudika.scoold.models.Category;
import com.erudika.scoold.utils.ScooldUtils;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Controller
@RequestMapping("/learn")
public class LearnController {

	private final ScooldUtils utils;
	private final ParaClient pc;
	private static final Logger logger = LoggerFactory.getLogger(LearnController.class);

	@Inject
	public LearnController(ScooldUtils utils) {
		this.utils = utils;
		this.pc = utils.getParaClient();
	}

	@GetMapping
	public String get(@RequestParam(required = false) String searchTerm, HttpServletRequest req, Model model) {
		try {
			if (searchTerm != null) {
				String formattedSearchTerm = searchTerm.trim().replace(" ", "-");
				List<Blog> blogs = getBlogs(formattedSearchTerm);
				model.addAttribute("blogs", blogs);
			} else {
				String query = "type: category";
				String type = Utils.type(Category.class);
				Pager itemCount = utils.getPager("page", req);
				List<Category> categories = pc.findQuery(type, "*", itemCount);
				logger.info("Categories --> " + categories);

				model.addAttribute("categories", categories);
				model.addAttribute("itemcount", itemCount);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}

		model.addAttribute("path", "learn.vm");
		model.addAttribute("title", "Learn");
		model.addAttribute("learnSelected", "navbtn-hover");

		return "base";
	}

	private List<Blog> getBlogs(String searchTerm) throws IOException {
		String formattedUrl = "https://public-api.wordpress.com/rest/v1.1/sites/blog.knoldus.com/posts?tag='" + searchTerm + "'";
		URL url = new URL(formattedUrl);
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

		Blogs receivedBlogs = new Gson().fromJson(content.toString(), Blogs.class);

		return receivedBlogs.getBlogs();
	}
}
