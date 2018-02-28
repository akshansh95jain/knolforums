/*
 * Copyright 2013-2018 Erudika. https://erudika.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For issues and patches go to: https://github.com/erudika
 */
package com.erudika.scoold.controllers;

import com.erudika.para.client.ParaClient;
import com.erudika.para.core.Sysprop;
import com.erudika.para.core.User;
import com.erudika.para.utils.Config;
import com.erudika.para.utils.Utils;
import static com.erudika.scoold.ScooldServer.CONTEXT_PATH;
import static com.erudika.scoold.ScooldServer.CSRF_COOKIE;
import static com.erudika.scoold.ScooldServer.HOMEPAGE;
import com.erudika.scoold.utils.HttpUtils;
import com.erudika.scoold.utils.ScooldUtils;
import java.util.Collections;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import static com.erudika.scoold.ScooldServer.SIGNINLINK;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

@Controller
public class SigninController {

	private final ScooldUtils utils;
	private final ParaClient pc;

	@Inject
	public SigninController(ScooldUtils utils) {
		this.utils = utils;
		this.pc = utils.getParaClient();
	}

	@GetMapping("/signin")
	public String get(@RequestParam(name = "returnto", required = false, defaultValue = HOMEPAGE) String returnto,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		if (utils.isAuthenticated(req)) {
			return "redirect:" + HOMEPAGE;
		}
		if (!HOMEPAGE.equals(returnto) && !SIGNINLINK.equals(returnto)) {
			HttpUtils.setStateParam("returnto", Utils.urlEncode(returnto), req, res);
		} else {
			HttpUtils.removeStateParam("returnto", req, res);
		}
		model.addAttribute("path", "signin.vm");
		model.addAttribute("title", utils.getLang(req).get("signin.title"));
		model.addAttribute("signinSelected", "navbtn-hover");
		model.addAttribute("fbLoginEnabled", !Config.FB_APP_ID.isEmpty());
		model.addAttribute("gpLoginEnabled", !Config.getConfigParam("google_client_id", "").isEmpty());
		model.addAttribute("ghLoginEnabled", !Config.GITHUB_APP_ID.isEmpty());
		model.addAttribute("inLoginEnabled", !Config.LINKEDIN_APP_ID.isEmpty());
		model.addAttribute("twLoginEnabled", !Config.TWITTER_APP_ID.isEmpty());
		model.addAttribute("msLoginEnabled", !Config.MICROSOFT_APP_ID.isEmpty());
		model.addAttribute("ldapLoginEnabled", !Config.getConfigParam("security.ldap.server_url", "").isEmpty());
		model.addAttribute("passwordLoginEnabled", Config.getConfigBoolean("password_auth_enabled", false));
		return "base";
	}

	@GetMapping(path = "/signin", params = {"access_token", "provider"})
	public String signinGet(@RequestParam("access_token") String accessToken, @RequestParam("provider") String provider,
			HttpServletRequest req, HttpServletResponse res) {
		return getAuth(provider, accessToken, req, res);
	}

	@PostMapping(path = "/signin", params = {"access_token", "provider"})
	public String signinPost(@RequestParam("access_token") String accessToken, @RequestParam("provider") String provider,
			HttpServletRequest req, HttpServletResponse res) {
		return getAuth(provider, accessToken, req, res);
	}

	@GetMapping("/signin/success")
	public String signinSuccess(@RequestParam String jwt, HttpServletRequest req, HttpServletResponse res, Model model) {
		if (!StringUtils.isBlank(jwt) && !"?".equals(jwt)) {
			HttpUtils.setStateParam(Config.AUTH_COOKIE, jwt, req, res, true);
		} else {
			return "redirect:" + SIGNINLINK + "?code=3&error=true";
		}
		return "redirect:" + getBackToUrl(req);
	}

	@GetMapping(path = "/signin/register")
	public String register(@RequestParam(name = "verify", required = false, defaultValue = "false") Boolean verify,
			@RequestParam(name = "id", required = false) String id,
			@RequestParam(name = "token", required = false) String token,
			HttpServletRequest req, Model model) {
		if (utils.isAuthenticated(req)) {
			return "redirect:" + HOMEPAGE;
		}
		model.addAttribute("path", "signin.vm");
		model.addAttribute("title", utils.getLang(req).get("signup.title"));
		model.addAttribute("signinSelected", "navbtn-hover");
		model.addAttribute("register", true);
		model.addAttribute("verify", verify);
		if (id != null && token != null) {
			boolean verified = activateWithEmailToken((User) pc.read(id), token);
			if (verified) {
				model.addAttribute("verified", verified);
			} else {
				return "redirect:" + SIGNINLINK;
			}
		}
		return "base";
	}

	@PostMapping("/signin/register")
	public String signup(@RequestParam String name, @RequestParam String email, @RequestParam String passw,
			HttpServletRequest req, Model model) {
		boolean approvedDomain = utils.isEmailDomainApproved(email);
		if (!utils.isAuthenticated(req) && approvedDomain) {
			if (pc.read(email) == null) {
				pc.signIn("password", email + ":" + name + ":" + passw, false);
				verifyEmailIfNecessary("password", name, email, req);
			} else {
				model.addAttribute("path", "signin.vm");
				model.addAttribute("title", utils.getLang(req).get("signup.title"));
				model.addAttribute("signinSelected", "navbtn-hover");
				model.addAttribute("register", true);
				model.addAttribute("name", name);
				model.addAttribute("bademail", email);
				model.addAttribute("error", Collections.singletonMap("email", utils.getLang(req).get("msgcode.1")));
				return "base";
			}
		}
		return "redirect:" + SIGNINLINK + (approvedDomain ? "/register?verify=true" : "?code=3&error=true");
	}

	@PostMapping("/signout")
	public String post(HttpServletRequest req, HttpServletResponse res) {
		if (utils.isAuthenticated(req)) {
			utils.clearSession(req, res);
			return "redirect:" + SIGNINLINK + "?code=5&success=true";
		}
		return "redirect:" + HOMEPAGE;
	}

	@ResponseBody
	@GetMapping("/scripts/globals.js")
	public ResponseEntity<String> globals(HttpServletRequest req, HttpServletResponse res) {
		res.setContentType("text/javascript");
		StringBuilder sb = new StringBuilder();
		sb.append("APPID = \"").append(Config.getConfigParam("access_key", "app:scoold").substring(4)).append("\"; ");
		sb.append("ENDPOINT = \"").append(pc.getEndpoint()).append("\"; ");
		sb.append("CONTEXT_PATH = \"").append(CONTEXT_PATH).append("\"; ");
		sb.append("CSRF_COOKIE = \"").append(CSRF_COOKIE).append("\"; ");
		sb.append("FB_APP_ID = \"").append(Config.FB_APP_ID).append("\"; ");
		sb.append("GOOGLE_CLIENT_ID = \"").append(Config.getConfigParam("google_client_id", "")).append("\"; ");
		sb.append("GOOGLE_ANALYTICS_ID = \"").append(Config.getConfigParam("google_analytics_id", "")).append("\"; ");
		sb.append("GITHUB_APP_ID = \"").append(Config.GITHUB_APP_ID).append("\"; ");
		sb.append("LINKEDIN_APP_ID = \"").append(Config.LINKEDIN_APP_ID).append("\"; ");
		sb.append("TWITTER_APP_ID = \"").append(Config.TWITTER_APP_ID).append("\"; ");
		sb.append("MICROSOFT_APP_ID = \"").append(Config.MICROSOFT_APP_ID).append("\"; ");

		Locale currentLocale = utils.getCurrentLocale(utils.getLanguageCode(req), req);
		sb.append("RTL_ENABLED = ").append(utils.isLanguageRTL(currentLocale.getLanguage())).append("; ");
		String result = sb.toString();
		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
				.eTag(Utils.md5(result)).body(result);
	}

	private String getAuth(String provider, String accessToken, HttpServletRequest req, HttpServletResponse res) {
		if (!utils.isAuthenticated(req)) {
			User u = pc.signIn(provider, accessToken, false);
			if (u != null && utils.isEmailDomainApproved(u.getEmail())) {
				// the user password in this case is a Bearer token (JWT)
				HttpUtils.setStateParam(Config.AUTH_COOKIE, u.getPassword(), req, res, true);
			} else {
				String[] tokenParts = accessToken.split(":");
				String email = tokenParts.length > 0 ? tokenParts[0] : "";
				if (utils.isEmailDomainApproved(email)) {
					verifyEmailIfNecessary(provider, "Anonymous", email, req);
				}
				return "redirect:" + SIGNINLINK + "?code=3&error=true";
			}
		}
		return "redirect:" + getBackToUrl(req);
	}

	private String getBackToUrl(HttpServletRequest req) {
		String backtoFromCookie = Utils.urlDecode(HttpUtils.getStateParam("returnto", req));
		return (StringUtils.isBlank(backtoFromCookie) ? HOMEPAGE : backtoFromCookie);
	}

	private boolean activateWithEmailToken(User u, String token) {
		if (u != null && token != null) {
			Sysprop s = pc.read(u.getIdentifier());
			if (s != null && token.equals(s.getProperty(Config._EMAIL_TOKEN))) {
				s.addProperty(Config._EMAIL_TOKEN, "");
				pc.update(s);
				u.setActive(true);
				pc.update(u);
				return true;
			}
		}
		return false;
	}

	private void verifyEmailIfNecessary(String provider, String name, String email, HttpServletRequest req) {
		if ("password".equals(provider)) {
			Sysprop ident = pc.read(email);
			if (ident != null && !ident.hasProperty(Config._EMAIL_TOKEN)) {
				User u = new User(ident.getCreatorid());
				u.setActive(false);
				u.setName(name);
				u.setEmail(email);
				u.setIdentifier(email);
				utils.sendWelcomeEmail(u, true, req);
			}
		}
	}
}
