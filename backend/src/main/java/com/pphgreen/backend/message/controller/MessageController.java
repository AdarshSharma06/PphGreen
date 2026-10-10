package com.pphgreen.backend.message.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pphgreen.backend.common.response.ApiResponse;
import com.pphgreen.backend.message.dto.ConversationResponse;
import com.pphgreen.backend.message.dto.DirectMessageResponse;
import com.pphgreen.backend.message.service.MessageService;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> listConversations() {
        return ResponseEntity.ok(ApiResponse.success(messageService.listConversations(currentUserEmail())));
    }

    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<ConversationResponse>> findOrCreate(@RequestBody Map<String, Long> body) {
        Long otherUserId = body.get("otherUserId");
        if (otherUserId == null) otherUserId = body.get("participantId");
        return ResponseEntity.ok(ApiResponse.success("Conversation ready", messageService.findOrCreateConversation(otherUserId, currentUserEmail())));
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiResponse<List<DirectMessageResponse>>> getMessages(
            @PathVariable Long id,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessages(id, cursor, limit, currentUserEmail())));
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiResponse<DirectMessageResponse>> sendMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String content = body.get("content");
        return ResponseEntity.ok(ApiResponse.success("Message sent", messageService.sendMessage(id, content, currentUserEmail())));
    }

    @PostMapping("/conversations/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        messageService.markRead(id, currentUserEmail());
        return ResponseEntity.ok(ApiResponse.success("Marked read", null));
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new com.pphgreen.backend.common.exception.UnauthorizedException("Unauthorized");
        }
        return auth.getName();
    }
}
