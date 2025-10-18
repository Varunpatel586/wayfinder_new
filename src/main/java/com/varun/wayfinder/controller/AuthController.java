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
            cookie.setHttpOnly(false);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            return "redirect:/";
        }
        redirectAttributes.addFlashAttribute("error", "Invalid username or password");
        return "redirect:/login";
    }

    // Redirect to SignIn
    @GetMapping("/")
    public String showLoginPage(@CookieValue(value = "token", required = false) String token, HttpServletResponse response) {
        if (isValidToken(token)) {
            return "redirect:/Home";
        }
        return "SignIn";
    }


    // Handle registration
    @GetMapping("/SignUp")
    public String showSignUpPage(@CookieValue(value = "token", required = false) String token) {
        if (isValidToken(token)) {
            // Already logged in → skip signup
            return "redirect:/Home";
        }
        // Just render the template
        return "SignUp";
    }

    // Handle form submission (POST)
    @PostMapping("/SignUp")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           HttpServletResponse response,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        if (userService.registerUser(username, password)) {
            // Generate token for the newly registered user
            String token = jwtUtil.generateToken(username);

            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);  // 1day
            response.addCookie(cookie);

            redirectAttributes.addFlashAttribute("message",
                    "Registration successful! Welcome, " + username + "!");
            return "redirect:/Home";
        } else {
            model.addAttribute("error", "Username already exists!");
            return "SignUp";
        }
    }

    // Logout
    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);            // delete the cookie
        response.addCookie(cookie);

        // After logout, back to login page or home screen
        return "redirect:/";
    }

    @GetMapping("/Home")
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
            return "Home";

        } catch (Exception e) {
            System.err.println("Error in Home endpoint: " + e.getMessage());
            // Clear invalid token
            Cookie cookie = new Cookie("token", "");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            return "redirect:/";
        }
    }
}