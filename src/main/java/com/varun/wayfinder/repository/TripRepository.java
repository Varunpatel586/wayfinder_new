package com.varun.wayfinder.repository;

import com.varun.wayfinder.model.Trip;
import com.varun.wayfinder.model.TripStatus;
import com.varun.wayfinder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByUserOrderByStartDateDesc(User user);

    List<Trip> findByUserAndStatusOrderByStartDateAsc(User user, TripStatus status);

    @Query("""
           SELECT DISTINCT t
           FROM Trip t
           JOIN FETCH t.place p
           LEFT JOIN FETCH p.images
           WHERE t.user = ?1
           ORDER BY CASE WHEN t.status = 'UPCOMING' THEN 0 ELSE 1 END,
                    t.startDate ASC
           """)
    List<Trip> findByUserOrderByStatusAndDate(User user);
}