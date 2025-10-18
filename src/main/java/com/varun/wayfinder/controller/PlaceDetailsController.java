package com.varun.wayfinder.controller;

import com.varun.wayfinder.dto.PlaceDTO;
import com.varun.wayfinder.model.Trip;
import com.varun.wayfinder.model.User;
import com.varun.wayfinder.security.JwtUtil;
import com.varun.wayfinder.service.GeminiService;
import com.varun.wayfinder.service.PlaceService;
import com.varun.wayfinder.service.TripService;
import com.varun.wayfinder.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PlaceDetailsController {

    @Autowired
    private PlaceService placeService;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private TripService tripService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/place/{id}")
    public String placeDetails(@PathVariable Long id, Model model) {
        PlaceDTO place = placeService.getPlaceById(id);
        if (place == null) {
            return "redirect:/explore";
        }
        model.addAttribute("place", place);
        return "place-details";
    }

    @PostMapping("/api/generate-route")
    @ResponseBody
    public Map<String, String> generateRoute(@RequestBody Map<String, Object> request) {
        Long placeId = Long.valueOf(request.get("placeId").toString());
        int days = Integer.parseInt(request.get("days").toString());

        PlaceDTO place = placeService.getPlaceById(placeId);

        String itinerary = geminiService.generateTravelRoute(
                place.getName(),
                place.getCountry(),
                place.getDescription(),
                days
        );

        Map<String, String> response = new HashMap<>();
        response.put("itinerary", itinerary);
        return response;
    }

    @PostMapping("/api/add-to-trips")
    @ResponseBody
    public Map<String, Object> addToTrips(@RequestBody Map<String, Object> request,
                                          HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = parseUserFromRequest(httpRequest);

            Long placeId = Long.valueOf(request.get("placeId").toString());
            LocalDate startDate = LocalDate.parse(request.get("startDate").toString());
            LocalDate endDate = LocalDate.parse(request.get("endDate").toString());
            String notes = request.get("notes") != null ? request.get("notes").toString() : "";

            Trip trip = tripService.createTrip(user, placeId, startDate, endDate, notes);

            response.put("success", true);
            response.put("message", "Trip added successfully!");
            response.put("tripId", trip.getId());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    private User parseUserFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            // Try cookie fallback
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (jakarta.servlet.http.Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName())) {
                        header = "Bearer " + cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization");
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