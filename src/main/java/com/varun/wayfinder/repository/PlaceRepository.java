package com.varun.wayfinder.repository;

import com.varun.wayfinder.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    // Find by category (supports multiple categories like "beach mountain")
    @Query("SELECT DISTINCT p FROM Place p LEFT JOIN FETCH p.images " +
            "WHERE LOWER(p.category) LIKE LOWER(CONCAT('%', :category, '%'))")
    List<Place> findByCategoryContaining(@Param("category") String category);

    // Search places by name, description, address, or country
    @Query("SELECT DISTINCT p FROM Place p LEFT JOIN FETCH p.images " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.address) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.country) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Place> searchPlaces(@Param("search") String search);

    // Get all places with images (avoid N+1 query problem)
    @Query("SELECT DISTINCT p FROM Place p LEFT JOIN FETCH p.images")
    List<Place> findAllWithImages();

    // Get top rated places
    @Query("SELECT p FROM Place p WHERE p.averageRating IS NOT NULL " +
            "ORDER BY p.averageRating DESC, p.reviewCount DESC")
    List<Place> findTopRatedPlaces();
}