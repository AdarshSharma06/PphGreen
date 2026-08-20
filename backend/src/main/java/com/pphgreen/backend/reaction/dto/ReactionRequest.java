package com.pphgreen.backend.reaction.dto;

import com.pphgreen.backend.reaction.entity.ReactionType;

import jakarta.validation.constraints.NotNull;

public record ReactionRequest(
        @NotNull(message = "Reaction type is required")
        ReactionType reactionType) {
}