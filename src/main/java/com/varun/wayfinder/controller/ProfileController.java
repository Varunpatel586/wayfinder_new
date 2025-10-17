package com.varun.wayfinder.controller;

import com.varun.wayfinder.model.User;
import com.varun.wayfinder.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showProfile(Model model, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");

        if (username == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(username);
        model.addAttribute("user", user);

        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User updatedUser, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");

        if (username == null) {
            return "redirect:/login";
        }

        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            existingUser.setFullName(updatedUser.getFullName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setContactNumber(updatedUser.getContactNumber());
            existingUser.setCountry(updatedUser.getCountry());
            existingUser.setZipCode(updatedUser.getZipCode());

            userRepository.save(existingUser);
        }

        return "redirect:/profile";
    }
}
