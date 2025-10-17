package com.varun.wayfinder.controller;

import com.varun.wayfinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // Show login page
    @GetMapping("/")
    public String showLoginPage(Model model) {
        // model attribute from redirect flash will automatically be available
        return "SignIn";
    }

    // Handle login
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        if (userService.authenticate(username, password)) {
            // Use redirect to stop form re-submission
            redirectAttributes.addFlashAttribute("username", username);
            return "redirect:/welcome";
        } else {
            model.addAttribute("error", "Invalid username or password!");
            return "login";
        }
    }

    // Show welcome page
    @GetMapping("/welcome")
    public String showWelcome(@ModelAttribute("username") String username, Model model) {
        model.addAttribute("username", username);
        return "welcome";
    }

    // Show registration page
    @GetMapping("/SignUp")
    public String showRegisterPage() {
        return "SignUp";
    }

    // Handle registration
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (userService.registerUser(username, password)) {
            // Redirect to login and pass a one-time success message
            redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
            return "redirect:/";
        } else {
            model.addAttribute("error", "Username already exists!");
            return "SignUp";
        }
    }
}