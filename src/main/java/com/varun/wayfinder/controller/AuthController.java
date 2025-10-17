package com.varun.wayfinder.controller;

import com.varun.wayfinder.model.User;
import com.varun.wayfinder.security.JwtUtil;
import com.varun.wayfinder.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    private boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            String username = jwtUtil.extractUsername(token);
            return username != null && !username.isEmpty() && jwtUtil.validateToken(token, username);
        } catch (Exception e) {
            return false;
        }
    }

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // Show login page
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        User user = userService.authenticate(username, password);
        if (user != null) {
            String token = jwtUtil.generateToken(user.getUsername());
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 day
            response.addCookie(cookie);
            return "redirect:/welcome";
        }
        redirectAttributes.addFlashAttribute("error", "Invalid username or password");
        return "redirect:/";
    }

    // Redirect to SignIn
    @GetMapping("/")
    public String showLoginPage(@CookieValue(value = "token", required = false) String token, HttpServletResponse response) {
        if (isValidToken(token)) {
            return "redirect:/welcome";
        }
        return "SignIn";
    }


    // Handle registration
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           HttpServletResponse response,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (userService.registerUser(username, password)) {
            // Generate token for the newly registered user
            String token = jwtUtil.generateToken(username);

            // Set the token in cookie
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 day
            response.addCookie(cookie);

            // Add success message
            redirectAttributes.addFlashAttribute("message", "Registration successful! Welcome, " + username + "!");

            // Redirect to welcome page
            return "redirect:/welcome";
        } else {
            model.addAttribute("error", "Username already exists!");
            return "SignUp";
        }
    }

    // Dashboard - protected page
    @GetMapping("/profile")
    public String showProfile(@CookieValue(value = "token", defaultValue = "") String token,
                              Model model) {
        if (token.isEmpty() || jwtUtil.isTokenExpired(token)) {
            return "redirect:/";
        }
        String username = jwtUtil.extractUsername(token);
        model.addAttribute("username", username);
        return "profile";
    }

    // Logout
    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Invalidate the JWT token by setting an expired cookie
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // This will delete the cookie
        response.addCookie(cookie);
        return "redirect:/login?logout";
    }

    @GetMapping("/welcome")
    public String welcome(@CookieValue(value = "token", required = false) String token,
                          Model model,
                          HttpServletRequest request,
                          HttpServletResponse response) {
        try {
            if (token == null || token.trim().isEmpty() || jwtUtil.isTokenExpired(token)) {
                return "redirect:/";
            }

            String username = jwtUtil.extractUsername(token);
            if (username == null || username.isEmpty()) {
                throw new RuntimeException("Invalid token: No username found");
            }

            model.addAttribute("username", username);
            request.setAttribute("username", username);
            return "welcome";

        } catch (Exception e) {
            System.err.println("Error in welcome endpoint: " + e.getMessage());
            // Clear invalid token
            Cookie cookie = new Cookie("token", "");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            return "redirect:/";
        }
    }
}
