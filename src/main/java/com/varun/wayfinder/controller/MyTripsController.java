package com.varun.wayfinder.controller;

import com.varun.wayfinder.dto.TripDTO;
import com.varun.wayfinder.model.User;
import com.varun.wayfinder.service.TripService;
import com.varun.wayfinder.service.UserService;
import com.varun.wayfinder.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
    public String myTrips() {
        return "mytrips";
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

    // Utility: read token from header, validate, fetch user
    private User parseUserFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = header.substring(7);
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