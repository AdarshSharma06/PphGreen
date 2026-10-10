package com.pphgreen.backend.message.dto;

import java.time.Instant;

public record DirectMessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderName,
        String content,
        Instant createdAt,
        Instant readAt
) {
}
