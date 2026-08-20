package com.pphgreen.backend.suggestion.dto;

import java.time.Instant;

public record SuggestionResponse(
        Long id,
        String content,
        SuggestionUserSummary submittedBy,
        Instant createdAt,
        Instant updatedAt) {
}