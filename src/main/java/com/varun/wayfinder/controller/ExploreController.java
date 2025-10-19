package com.varun.wayfinder.controller;

import com.varun.wayfinder.dto.PlaceDTO;
import com.varun.wayfinder.security.JwtUtil;
import com.varun.wayfinder.service.PlaceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ExploreController {

    @Autowired
    private PlaceService placeService;

    @Autowired
    private JwtUtil jwtUtil;

//    @GetMapping("/explore")
//    public String explore(@CookieValue(value = "token", required = false) String token,
//                          Model model,
//                          HttpServletRequest request,
//                          HttpServletResponse response) {
//
//        System.out.println("=== EXPLORE CONTROLLER CALLED ===");
//        System.out.println("Request URI: " + request.getRequestURI());
//        System.out.println("Token: " + token);
//        System.out.println("Response committed: " + response.isCommitted());
//
//        String username = null;
//        if (token != null && !token.trim().isEmpty()) {
//            try {
//                if (!jwtUtil.isTokenExpired(token)) {
//                    username = jwtUtil.extractUsername(token);
//                }
//            } catch (Exception e) {
//                // Invalid token, ignore
//            }
//        }
//
//        System.out.println("Username: " + username);
//        System.out.println("About to return 'explore' template");
//
//        model.addAttribute("username", username);
//        return "explore";
//    }

    @GetMapping("/explore")
    public String explore() {
        return "explore";
    }

    @GetMapping("/api/places")
    @ResponseBody
    public List<PlaceDTO> getPlaces(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        System.out.println("API: Getting places - category: " + category + ", search: " + search);

        if (search != null && !search.trim().isEmpty()) {
            return placeService.searchPlaces(search.trim());
        } else if (category != null && !category.equals("all")) {
            return placeService.getPlacesByCategory(category);
        }
        return placeService.getAllPlaces();
    }

    @GetMapping("/api/places/top-rated")
    @ResponseBody
    public List<PlaceDTO> getTopRatedPlaces() {
        System.out.println("API: Getting top-rated places");
        return placeService.getTopRatedPlaces();
    }
}