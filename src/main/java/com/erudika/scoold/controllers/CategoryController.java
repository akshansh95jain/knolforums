package com.erudika.scoold.controllers;

import com.erudika.para.client.ParaClient;
import com.erudika.scoold.models.Category;
import com.erudika.scoold.utils.ScooldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/category")
public class CategoryController {

	private final ScooldUtils utils;
	private final ParaClient pc;
	private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

	@Inject
	public CategoryController(ScooldUtils utils) {
		this.utils = utils;
		this.pc = utils.getParaClient();
	}

	@GetMapping
	public String get(HttpServletRequest req, Model model) {

		model.addAttribute("path", "category.vm");
		model.addAttribute("title", "Add Category");
		model.addAttribute("categorySelected", "navbtn-hover");

		return "base";
	}

	@GetMapping("/addcategory")
	public String addCategory(String category, HttpServletRequest req, Model model) {
		Category newCategory = new Category(category);
		logger.info("Persisting category of " + newCategory.getName() + " in data store.");
		newCategory.create();
		logger.info("Created");

		model.addAttribute("path", "category.vm");
		model.addAttribute("title", "Add Category");
		model.addAttribute("categorySelected", "navbtn-hover");

		return "base";
	}
}
