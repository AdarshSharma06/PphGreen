package com.pphgreen.backend.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 255, message = "Name must be at most 255 characters")
        String name,

        @Size(max = 255, message = "Tower must be at most 255 characters")
        String tower,

        @Size(max = 50, message = "Apartment number must be at most 50 characters")
        String apartmentNumber,

        @Size(max = 500, message = "Profile picture reference must be at most 500 characters")
        String profilePicture) {
}