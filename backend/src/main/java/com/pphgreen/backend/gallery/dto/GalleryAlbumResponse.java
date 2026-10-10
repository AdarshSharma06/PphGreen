package com.pphgreen.backend.gallery.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record GalleryAlbumResponse(
        Long eventId,
        String title,
        String description,
        LocalDate date,
        LocalTime time,
        String venue,
        String image,
        int photoCount,
        int videoCount,
        int totalMediaCount,
        String thumbnailUrl,
        long totalLikes,
        GalleryUserSummary createdBy
) {
}
