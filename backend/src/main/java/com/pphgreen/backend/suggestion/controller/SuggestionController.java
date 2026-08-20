package com.pphgreen.backend.suggestion.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pphgreen.backend.common.response.ApiResponse;
import com.pphgreen.backend.suggestion.dto.SuggestionRequest;
import com.pphgreen.backend.suggestion.dto.SuggestionResponse;
import com.pphgreen.backend.suggestion.service.SuggestionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SuggestionResponse>>> getSuggestions() {
        return ResponseEntity.ok(ApiResponse.success(suggestionService.getAllSuggestions(currentUserEmail())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SuggestionResponse>> createSuggestion(@Valid @RequestBody SuggestionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Suggestion submitted",
                suggestionService.createSuggestion(request, currentUserEmail())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SuggestionResponse>> updateSuggestion(
            @PathVariable Long id, @Valid @RequestBody SuggestionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Suggestion updated",
                suggestionService.updateSuggestion(id, request, currentUserEmail())));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}