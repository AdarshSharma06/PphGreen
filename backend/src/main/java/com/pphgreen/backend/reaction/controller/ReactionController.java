package com.pphgreen.backend.reaction.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pphgreen.backend.common.response.ApiResponse;
import com.pphgreen.backend.reaction.dto.ReactionRequest;
import com.pphgreen.backend.reaction.dto.ReactionResponse;
import com.pphgreen.backend.reaction.service.ReactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ReactionController {

    private final ReactionService reactionService;

    public ReactionController(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @PostMapping("/events/{eventId}/reactions")
    public ResponseEntity<ApiResponse<ReactionResponse>> addReaction(
            @PathVariable Long eventId, @Valid @RequestBody ReactionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Reaction added",
                reactionService.addReaction(eventId, request, currentUserEmail())));
    }

    @GetMapping("/events/{eventId}/reactions")
    public ResponseEntity<ApiResponse<List<ReactionResponse>>> getReactions(@PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getReactionsForEvent(eventId)));
    }

    @DeleteMapping("/events/{eventId}/reactions")
    public ResponseEntity<ApiResponse<Void>> removeReaction(@PathVariable Long eventId) {
        reactionService.removeReaction(eventId, currentUserEmail());
        return ResponseEntity.ok(ApiResponse.success("Reaction removed", null));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}