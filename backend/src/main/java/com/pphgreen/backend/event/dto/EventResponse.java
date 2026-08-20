package com.pphgreen.backend.event.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        LocalDate date,
        LocalTime time,
        String venue,
        String image,
        EventUserSummary createdBy,
        Instant createdAt,
        Instant updatedAt) {
}