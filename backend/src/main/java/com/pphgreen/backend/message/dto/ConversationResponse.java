package com.pphgreen.backend.message.dto;

import java.time.Instant;

import com.pphgreen.backend.community.dto.CommunityPostAuthorSummary;

public record ConversationResponse(
        Long id,
        CommunityPostAuthorSummary otherParticipant,
        String lastMessagePreview,
        Instant lastMessageAt,
        long unreadCount,
        Instant createdAt,
        Instant updatedAt
) {
}
