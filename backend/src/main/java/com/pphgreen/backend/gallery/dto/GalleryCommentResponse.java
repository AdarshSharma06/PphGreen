package com.pphgreen.backend.gallery.dto;

import java.time.Instant;

public record GalleryCommentResponse(
        Long id,
        String content,
        Long galleryId,
        GalleryCommentUserSummary author,
        Instant createdAt,
        Instant updatedAt) {
}
