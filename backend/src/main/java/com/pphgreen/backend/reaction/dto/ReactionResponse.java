package com.pphgreen.backend.reaction.dto;

import java.time.Instant;

import com.pphgreen.backend.reaction.entity.ReactionType;

public record ReactionResponse(
        Long id,
        ReactionType reactionType,
        Long eventId,
        ReactionUserSummary user,
        Instant createdAt) {
}