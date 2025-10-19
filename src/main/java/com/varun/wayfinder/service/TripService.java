package com.varun.wayfinder.service;

import com.varun.wayfinder.dto.TripDTO;
import com.varun.wayfinder.model.*;
import com.varun.wayfinder.repository.PlaceRepository;
import com.varun.wayfinder.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PlaceRepository placeRepository;

    public List<TripDTO> getUserTrips(User user) {
        List<Trip> trips = tripRepository.findByUserOrderByStatusAndDate(user);
        return trips.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TripDTO> getUpcomingTrips(User user) {
        List<Trip> trips = tripRepository.findByUserAndStatusOrderByStartDateAsc(
                user, TripStatus.UPCOMING);
        return trips.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TripDTO> getCompletedTrips(User user) {
        List<Trip> trips = tripRepository.findByUserAndStatusOrderByStartDateAsc(
                user, TripStatus.COMPLETED);
        return trips.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Trip createTrip(User user, Long placeId, LocalDate startDate, LocalDate endDate, String notes) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("Place not found"));

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setPlace(place);
        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        trip.setNotes(notes);

        return tripRepository.save(trip);
    }

    public void deleteTrip(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        tripRepository.delete(trip);
    }

    private TripDTO convertToDTO(Trip trip) {
        Place place = trip.getPlace();
        String imageUrl = getPlaceImage(place);

        return new TripDTO(
                trip.getId(),
                place.getName(),
                imageUrl,
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getStatus().toString(),
                place.getId(),
                trip.getNotes()
        );
    }

    private String getPlaceImage(Place place) {
        if (place.getImages() != null && !place.getImages().isEmpty()) {
            return place.getImages().stream()
                    .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                    .findFirst()
                    .map(PlaceImage::getImageUrl)
                    .orElse(place.getImages().get(0).getImageUrl());
        }
        return "https://via.placeholder.com/300x200?text=No+Image";
    }
}