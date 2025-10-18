package com.varun.wayfinder.controller;

import com.varun.wayfinder.dto.PlaceDTO;
import com.varun.wayfinder.service.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private PlaceService placeService;

    @GetMapping("/api/featured-places")
    @ResponseBody
    public List<PlaceDTO> getFeaturedPlaces() {
        // Get top 3 rated places for the home page
        return placeService.getTopRatedPlaces()
                .stream()
                .limit(3)
                .toList();
    }
}