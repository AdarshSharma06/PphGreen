package com.pphgreen.backend.event.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must be at most 255 characters")
        String title,

        @Size(max = 10000, message = "Description must be at most 10000 characters")
        String description,

        @NotNull(message = "Date is required")
        LocalDate date,

        LocalTime time,

        @Size(max = 255, message = "Venue must be at most 255 characters")
        String venue,

        @Size(max = 500, message = "Image reference must be at most 500 characters")
        String image) {
}