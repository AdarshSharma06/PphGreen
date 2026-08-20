package com.pphgreen.backend.about.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AboutRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must be at most 255 characters")
        String title,

        @Size(max = 10000, message = "Description must be at most 10000 characters")
        String description,

        @Size(max = 10000, message = "Ideals must be at most 10000 characters")
        String ideals,

        @Size(max = 10000, message = "Impact metrics must be at most 10000 characters")
        String impactMetrics,

        @Size(max = 500, message = "Image reference must be at most 500 characters")
        String image) {
}