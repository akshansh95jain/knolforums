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
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

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
	public String get(@RequestParam(required = false) String category, HttpServletRequest req, Model model) {
		Optional<String> maybeCategory = Optional.ofNullable(category);
		String categoryToAdd = maybeCategory.orElse("");

		boolean success = false;
		boolean tryToAdd = false;

		if (categoryToAdd.trim().length() > 1) {
			tryToAdd = true;
			success = addCategory(categoryToAdd);
		}

		model.addAttribute("path", "category.vm");
		model.addAttribute("title", "Add Category");
		model.addAttribute("success", success);
		model.addAttribute("tryToAdd", tryToAdd);
		model.addAttribute("categorySelected", "navbtn-hover");

		return "base";
	}

	private boolean addCategory(String category) {
		boolean success = false;
		try {
			Category newCategory = new Category(category);
			newCategory.create();
			success = true;
		} catch (Exception exception) {
			logger.info(exception.getMessage());
		}

		return success;
	}
}
