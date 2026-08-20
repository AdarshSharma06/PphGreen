package com.pphgreen.backend.user.dto;

public record UserPublicResponse(
        Long id,
        String name,
        String tower,
        String apartmentNumber,
        String profilePicture) {
}