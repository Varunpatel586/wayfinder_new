package com.varun.wayfinder.controller;

import com.varun.wayfinder.dto.PlaceDTO;
import com.varun.wayfinder.model.Trip;
import com.varun.wayfinder.model.User;
import com.varun.wayfinder.security.JwtUtil;
import com.varun.wayfinder.service.GeminiService;
import com.varun.wayfinder.service.PlaceService;
import com.varun.wayfinder.service.TripService;
import com.varun.wayfinder.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Map;

@Controller
public class PlaceDetailsController {

    @Autowired private PlaceService placeService;
    @Autowired private GeminiService geminiService;
    @Autowired private TripService tripService;
    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;

    // Public page - anyone can view, but shows different UI based on login status
    @GetMapping("/place/{id}")
    public String placeDetails(@PathVariable Long id,
                               @CookieValue(value = "token", required = false) String token,
                               Model model) {
        PlaceDTO place = placeService.getPlaceById(id);
        if (place == null) {
            return "redirect:/explore";
        }

        // Check if user is logged in
        boolean isLoggedIn = false;
        String username = null;

        if (token != null && !token.trim().isEmpty()) {
            try {
                if (!jwtUtil.isTokenExpired(token)) {
                    username = jwtUtil.extractUsername(token);
                    if (username != null) {
                        isLoggedIn = true;
                    }
                }
            } catch (Exception e) {
                // Invalid token, but still show the page
            }
        }

        model.addAttribute("place", place);
        model.addAttribute("username", username);
        model.addAttribute("isLoggedIn", isLoggedIn);

        return "place-details";
    }

    // Public API - anyone can generate itinerary
    @PostMapping("/api/generate-route")
    @ResponseBody
    public Map<String, String> generateRoute(@RequestBody Map<String, Object> request) {
        Long placeId = Long.valueOf(request.get("placeId").toString());
        int days = Integer.parseInt(request.get("days").toString());

        PlaceDTO place = placeService.getPlaceById(placeId);
        if (place == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found");
        }

        String itinerary = geminiService.generateTravelRoute(
                place.getName(),
                place.getCountry(),
                place.getDescription(),
                days
        );

        return Map.of("itinerary", itinerary);
    }

    // Protected API - requires login
    @PostMapping("/api/add-to-trips")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToTrips(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        try {
            User user = parseUserFromRequest(httpRequest);

            Long placeId = Long.valueOf(request.get("placeId").toString());
            LocalDate startDate = LocalDate.parse(request.get("startDate").toString());
            LocalDate endDate = LocalDate.parse(request.get("endDate").toString());
            String notes = request.get("notes") != null ? request.get("notes").toString() : "";

            if (endDate.isBefore(startDate)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "End date cannot be before start date"
                ));
            }

            Trip trip = tripService.createTrip(user, placeId, startDate, endDate, notes);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Trip added successfully!",
                    "tripId", trip.getId()
            ));

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "success", false,
                    "message", e.getReason()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "An error occurred"
            ));
        }
    }

    private User parseUserFromRequest(HttpServletRequest request) {
        String token = null;

        // 1. Try Authorization header
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }

        // 2. Fallback to cookie
        if (token == null || token.isBlank()) {
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

        // 3. Validate
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }

        try {
            if (jwtUtil.isTokenExpired(token)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired");
            }

            String username = jwtUtil.extractUsername(token);
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
            }
            return user;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }
}