package com.varun.wayfinder.controller;

import com.varun.wayfinder.model.User;
import com.varun.wayfinder.repository.UserRepository;
import com.varun.wayfinder.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        // Check authentication - redirect to login if not authenticated
        if (token.isEmpty() || jwtUtil.isTokenExpired(token)) {
            return "redirect:/login?redirect=/profile";
        }

        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "redirect:/login?redirect=/profile";
        }

        model.addAttribute("user", user);
        model.addAttribute("username", username);
        model.addAttribute("isLoggedIn", true);
        return "profile";
    }

    // --- update profile ---
    @PostMapping("/profile/update")
    public String updateProfile(@CookieValue(value = "token", defaultValue = "") String token,
                                @ModelAttribute("user") User updatedUser,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        // Check authentication
        if (token.isEmpty() || jwtUtil.isTokenExpired(token)) {
            return "redirect:/login?redirect=/profile";
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

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found");
        }

        return "redirect:/profile";
    }
}
