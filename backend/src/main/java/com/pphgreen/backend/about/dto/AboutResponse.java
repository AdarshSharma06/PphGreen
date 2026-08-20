package com.pphgreen.backend.about.dto;

import java.time.Instant;

public record AboutResponse(
        Long id,
        String title,
        String description,
        String ideals,
        String impactMetrics,
        String image,
        AboutUserSummary createdBy,
        Instant createdAt,
        Instant updatedAt) {
}