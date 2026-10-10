package com.pphgreen.backend.community.dto;

import java.time.Instant;

public record CommunityPostCommentResponse(
        Long id,
        Long postId,
        String content,
        CommunityPostAuthorSummary author,
        Instant createdAt,
        Instant updatedAt
) {
}
