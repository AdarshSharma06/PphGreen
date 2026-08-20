package com.pphgreen.backend.suggestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuggestionRequest(
        @NotBlank(message = "Suggestion content is required")
        @Size(max = 10000, message = "Suggestion content must be at most 10000 characters")
        String content) {
}