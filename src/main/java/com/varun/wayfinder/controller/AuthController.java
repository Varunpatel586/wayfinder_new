package com.varun.wayfinder.controller;

import com.varun.wayfinder.model.User;
import com.varun.wayfinder.security.JwtUtil;
import com.varun.wayfinder.service.UserService;
import jakarta.servlet.http.Cookie;
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

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    private boolean isValidToken(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            String username = jwtUtil.extractUsername(token);
            return username != null && !username.isBlank() && !jwtUtil.isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // HOME PAGE
    @GetMapping("/")
    public String home(@CookieValue(value = "token", required = false) String token,
                       Model model) {
        String username = null;
        if (token != null && !token.trim().isEmpty()) {
            try {
                if (!jwtUtil.isTokenExpired(token)) {
                    username = jwtUtil.extractUsername(token);
                }
            } catch (Exception e) {
                // Invalid token, ignore
            }
        }
        model.addAttribute("username", username);
        return "Home";
    }

    @GetMapping("/Home")
    public String legacyHome() {
        return "redirect:/";
    }

    // LOGIN
    @GetMapping("/login")
    public String showLogin(@CookieValue(value = "token", required = false) String token,
                            @RequestParam(value = "next", required = false) String next,
                            Model model) {
        if (isValidToken(token)) {
            return "redirect:/";
        }
        model.addAttribute("next", next);
        return "SignIn";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam(value = "next", required = false) String next,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        User user = userService.authenticate(username, password);
        if (user != null) {
            String token = jwtUtil.generateToken(user.getUsername());

            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            if (next != null && !next.isBlank() && next.startsWith("/")) {
                return "redirect:" + next;
            }
            return "redirect:/";
        }
        redirectAttributes.addFlashAttribute("error", "Invalid username or password");
        String redirectUrl = "redirect:/login";
        if (next != null && !next.isBlank()) {
            redirectUrl += "?next=" + next;
        }
        return redirectUrl;
    }

    // SIGNUP
    @GetMapping("/SignUp")
    public String showSignUp(@CookieValue(value = "token", required = false) String token) {
        if (isValidToken(token)) {
            return "redirect:/";
        }
        return "SignUp";
    }

    @PostMapping("/SignUp")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           HttpServletResponse response,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (userService.registerUser(username, password)) {
            String token = jwtUtil.generateToken(username);

            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            redirectAttributes.addFlashAttribute("message",
                    "Registration successful! Welcome, " + username + "!");
            return "redirect:/";
        } else {
            model.addAttribute("error", "Username already exists!");
            return "SignUp";
        }
    }

    // LOGOUT
    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }
}