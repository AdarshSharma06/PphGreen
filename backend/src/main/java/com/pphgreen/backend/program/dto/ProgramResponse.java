package com.pphgreen.backend.program.dto;

import java.time.Instant;

public record ProgramResponse(
        Long id,
        String title,
        String description,
        String image,
        ProgramUserSummary createdBy,
        Instant createdAt,
        Instant updatedAt) {
}