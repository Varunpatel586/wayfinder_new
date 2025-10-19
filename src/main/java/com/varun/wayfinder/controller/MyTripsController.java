package com.varun.wayfinder.controller;

import com.varun.wayfinder.dto.TripDTO;
import com.varun.wayfinder.model.User;
import com.varun.wayfinder.service.TripService;
import com.varun.wayfinder.service.UserService;
import com.varun.wayfinder.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class MyTripsController {

    @Autowired
    private TripService tripService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/my-trips")
    public String myTrips(@CookieValue(value = "token", required = false) String token,
                          Model model) {
        // Check authentication
        if (token == null || token.trim().isEmpty()) {
            return "redirect:/login?redirect=/my-trips";
        }

        try {
            String username = jwtUtil.extractUsername(token);
            if (username == null || jwtUtil.isTokenExpired(token)) {
                return "redirect:/login?redirect=/my-trips";
            }
            model.addAttribute("username", username);
            model.addAttribute("isLoggedIn", true);
            return "mytrips";
        } catch (Exception e) {
            return "redirect:/login?redirect=/my-trips";
        }
    }

    @GetMapping("/api/my-trips")
    @ResponseBody
    public List<TripDTO> getMyTrips(HttpServletRequest request) {
        User user = parseUserFromRequest(request);
        return tripService.getUserTrips(user);
    }

    @GetMapping("/api/my-trips/upcoming")
    @ResponseBody
    public List<TripDTO> getUpcomingTrips(HttpServletRequest request) {
        User user = parseUserFromRequest(request);
        return tripService.getUpcomingTrips(user);
    }

    @GetMapping("/api/my-trips/completed")
    @ResponseBody
    public List<TripDTO> getCompletedTrips(HttpServletRequest request) {
        User user = parseUserFromRequest(request);
        return tripService.getCompletedTrips(user);
    }

    @DeleteMapping("/api/my-trips/{id}")
    @ResponseBody
    public void deleteTrip(@PathVariable Long id, HttpServletRequest request) {
        User user = parseUserFromRequest(request);
        tripService.deleteTrip(id, user);
    }

    private User parseUserFromRequest(HttpServletRequest request) {
        // First try header
        String header = request.getHeader("Authorization");
        String token = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } else {
            // Try cookie if header not present
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (token == null) {
            throw new RuntimeException("Missing authentication");
        }

        String username = jwtUtil.extractUsername(token);
        if (jwtUtil.isTokenExpired(token)) {
            throw new RuntimeException("Token expired");
        }

        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }
}