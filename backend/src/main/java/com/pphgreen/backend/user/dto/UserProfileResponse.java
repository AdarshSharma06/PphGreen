package com.pphgreen.backend.user.dto;

public record UserProfileResponse(
        Long id,
        String email,
        String name,
        String tower,
        String apartmentNumber,
        String profilePicture) {
}