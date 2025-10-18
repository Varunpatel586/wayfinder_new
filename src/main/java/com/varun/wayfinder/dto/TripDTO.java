package com.varun.wayfinder.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TripDTO {
    private Long id;
    private String tripName;
    private String placeName;
    private String imageUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String dateRange;
    private Long placeId;

    public TripDTO(Long id, String placeName, String imageUrl,
                   LocalDate startDate, LocalDate endDate, String status, Long placeId) {
        this.id = id;
        this.tripName = placeName;
        this.placeName = placeName;
        this.imageUrl = imageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.placeId = placeId;
        this.dateRange = formatDateRange(startDate, endDate);
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        String startMonth = start.getMonth().toString().substring(0, 3);
        String endMonth = end.getMonth().toString().substring(0, 3);

        if (start.getMonth() == end.getMonth()) {
            return startMonth + " " + start.getDayOfMonth() + "-" + end.getDayOfMonth();
        }
        return startMonth + " " + start.getDayOfMonth() + "-" +
                endMonth + " " + end.getDayOfMonth();
    }
}