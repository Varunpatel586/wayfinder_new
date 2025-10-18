package com.varun.wayfinder.service;

import com.varun.wayfinder.dto.PlaceDTO;
import com.varun.wayfinder.model.Place;
import com.varun.wayfinder.repository.PlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PlaceService {

    @Autowired
    private PlaceRepository placeRepository;

    public List<PlaceDTO> getAllPlaces() {
        return placeRepository.findAllWithImages().stream()
                .map(PlaceDTO::new)
                .collect(Collectors.toList());
    }

    public List<PlaceDTO> getPlacesByCategory(String category) {
        return placeRepository.findByCategoryContaining(category).stream()
                .map(PlaceDTO::new)
                .collect(Collectors.toList());
    }

    public List<PlaceDTO> searchPlaces(String search) {
        return placeRepository.searchPlaces(search).stream()
                .map(PlaceDTO::new)
                .collect(Collectors.toList());
    }

    public List<PlaceDTO> getTopRatedPlaces() {
        return placeRepository.findTopRatedPlaces().stream()
                .map(PlaceDTO::new)
                .collect(Collectors.toList());
    }

    public PlaceDTO getPlaceById(Long id) {
        return placeRepository.findById(id)
                .map(PlaceDTO::new)
                .orElse(null);
    }
}