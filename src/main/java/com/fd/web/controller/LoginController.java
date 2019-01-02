package com.fd.web.controller;

import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fd.web.controller.base.BaseController;

@Controller
@RequestMapping("/login")
public class LoginController extends BaseController {

	@GetMapping
	public String login(HttpServletRequest req) {
		req.getSession().invalidate();
		return "login";
	}

	static ResourceBundle INIT_RB = ResourceBundle.getBundle("init");
	static int error = 0;

	@PostMapping
	public String userlogin(HttpServletRequest req) {
		try {
			if (error < 10) {
				String username = req.getParameter("username");
				String password = req.getParameter("password");
				if ("fudong".equals(username) && INIT_RB.getString("password").equals(password)) {
					req.getSession().setAttribute("auth_login_user", true);
					error = 0;
					return "redirect:/home";
				} else {
					error++;
					return "login";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "login";
	}
}
