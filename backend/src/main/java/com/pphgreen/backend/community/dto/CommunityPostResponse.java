package com.pphgreen.backend.community.dto;

import java.time.Instant;
import java.util.List;

public record CommunityPostResponse(
        Long id,
        String content,
        CommunityPostAuthorSummary author,
        Instant createdAt,
        Instant updatedAt,
        List<CommunityPostImageResponse> images,
        long commentCount
) {
}
