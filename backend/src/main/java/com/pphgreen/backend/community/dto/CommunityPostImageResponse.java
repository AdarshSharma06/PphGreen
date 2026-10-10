package com.pphgreen.backend.community.dto;

public record CommunityPostImageResponse(Long id, String fileUrl, String storagePath, Integer displayOrder) {
}
