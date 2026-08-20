package com.pphgreen.backend.auth.dto;

public record AuthResponse(String token, String email, String role) {
}