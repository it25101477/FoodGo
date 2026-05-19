package com.foodgo.controller;

import com.foodgo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web Controller - Handles Thymeleaf web views
 * Serves HTML pages and templates
 */
@Controller
public class WebController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/admin")
    public String admin(Model model) throws Exception {
        model.addAttribute("users", userService.getAllUsers());
        return "admin";
    }

    @GetMapping("/rider")
    public String rider() {
        return "riderpage";
    }
}
