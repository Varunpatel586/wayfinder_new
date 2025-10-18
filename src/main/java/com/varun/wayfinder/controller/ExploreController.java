package com.varun.wayfinder.controller;

import com.varun.wayfinder.dto.PlaceDTO;
import com.varun.wayfinder.service.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ExploreController {

    @Autowired
    private PlaceService placeService;

    @GetMapping("/explore")
    public String explore(Model model) {
        return "explore";
    }

    @GetMapping("/api/places")
    @ResponseBody
    public List<PlaceDTO> getPlaces(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

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
        return placeService.getTopRatedPlaces();
    }

    @GetMapping("/place/{id}")
    public String placeDetails(@PathVariable Long id, Model model) {
        model.addAttribute("placeId", id);
        return "place-details";
    }

    @GetMapping("/api/places/{id}")
    @ResponseBody
    public PlaceDTO getPlace(@PathVariable Long id) {
        return placeService.getPlaceById(id);
    }
}