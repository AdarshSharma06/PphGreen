package com.pphgreen.backend.gallery.dto;

import java.time.Instant;

public record GalleryResponse(
        Long id,
        String fileName,
        String mediaType,
        String fileUrl,
        GalleryUserSummary uploadedBy,
        Instant createdAt,
        Long eventId,
        Boolean highlight,
        Integer highlightOrder,
        long likeCount,
        boolean likedByCurrentUser) {
}