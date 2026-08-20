package com.pphgreen.backend.comment.dto;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String content,
        Long eventId,
        CommentUserSummary author,
        Instant createdAt,
        Instant updatedAt) {
}