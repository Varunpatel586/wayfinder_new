package com.varun.wayfinder.controller;

import com.varun.wayfinder.model.User;
import com.varun.wayfinder.repository.UserRepository;
import com.varun.wayfinder.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ProfileController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // --- view profile ---
    @GetMapping("/profile")
    public String showProfile(@CookieValue(value = "token", defaultValue = "") String token,
                              Model model) {
        if (token.isEmpty() || jwtUtil.isTokenExpired(token)) {
            return "redirect:/";
        }

        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "redirect:/"; // unexpected: token user not in DB
        }

        model.addAttribute("user", user);
        return "profile";
    }

    // --- update profile ---
    @PostMapping("/profile/update")
    public String updateProfile(@CookieValue(value = "token", defaultValue = "") String token,
                                @ModelAttribute("user") User updatedUser,
                                Model model) {
        if (token.isEmpty() || jwtUtil.isTokenExpired(token)) {
            return "redirect:/";
        }

        String username = jwtUtil.extractUsername(token);
        User existing = userRepository.findByUsername(username);
        if (existing != null) {
            existing.setFullName(updatedUser.getFullName());
            existing.setEmail(updatedUser.getEmail());
            existing.setContactNumber(updatedUser.getContactNumber());
            existing.setCountry(updatedUser.getCountry());
            existing.setZipCode(updatedUser.getZipCode());
            userRepository.save(existing);
        }

        return "redirect:/profile";
    }
}
