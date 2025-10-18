package com.varun.wayfinder.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "places")
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    private double latitude;
    private double longitude;
    private Float averageRating;
    private Integer reviewCount;
    private String category;
    private String country;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "added_by_user_id", nullable = false)
    private User addedBy;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceImage> images;

    // Helper method to get primary image
    public String getPrimaryImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.stream()
                    .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                    .findFirst()
                    .map(PlaceImage::getImageUrl)
                    .orElse(images.get(0).getImageUrl());
        }
        return null;
    }
}