package com.varun.wayfinder.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "place_images")
public class PlaceImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    private Boolean isPrimary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;
}
