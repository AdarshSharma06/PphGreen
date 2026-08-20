package com.pphgreen.backend.developer.dto;

import java.time.Instant;

public record DeveloperResponse(
        Long id,
        String name,
        String role,
        String bio,
        String image,
        DeveloperUserSummary createdBy,
        Instant createdAt,
        Instant updatedAt) {
}