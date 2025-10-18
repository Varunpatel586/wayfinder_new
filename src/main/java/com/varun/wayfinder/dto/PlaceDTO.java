package com.varun.wayfinder.dto;

import com.varun.wayfinder.model.Place;
import lombok.Data;

@Data
public class PlaceDTO {
    private Long id;
    private String name;
    private String shortDescription;
    private String description;
    private String imageUrl;
    private String category;
    private String country;
    private String address;
    private Float averageRating;
    private Integer reviewCount;
    private Double latitude;
    private Double longitude;

    public PlaceDTO(Place place) {
        this.id = place.getId();
        this.name = place.getName();
        this.shortDescription = place.getShortDescription();
        this.description = place.getDescription();
        this.imageUrl = place.getPrimaryImageUrl();
        this.category = place.getCategory();
        this.country = place.getCountry();
        this.address = place.getAddress();
        this.averageRating = place.getAverageRating();
        this.reviewCount = place.getReviewCount();
        this.latitude = place.getLatitude();
        this.longitude = place.getLongitude();
    }
}