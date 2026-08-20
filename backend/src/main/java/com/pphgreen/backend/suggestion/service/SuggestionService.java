package com.pphgreen.backend.suggestion.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.suggestion.dto.SuggestionRequest;
import com.pphgreen.backend.suggestion.dto.SuggestionResponse;
import com.pphgreen.backend.suggestion.dto.SuggestionUserSummary;
import com.pphgreen.backend.suggestion.entity.Suggestion;
import com.pphgreen.backend.suggestion.repository.SuggestionRepository;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final UserService userService;

    public SuggestionService(SuggestionRepository suggestionRepository, UserService userService) {
        this.suggestionRepository = suggestionRepository;
        this.userService = userService;
    }

    public SuggestionResponse createSuggestion(SuggestionRequest request, String email) {
        User submitter = userService.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        Suggestion suggestion = new Suggestion();
        suggestion.setContent(request.content());
        suggestion.setSubmitter(submitter);
        Suggestion saved = suggestionRepository.save(suggestion);
        return toResponse(findSuggestion(saved.getId()));
    }

    public List<SuggestionResponse> getAllSuggestions(String email) {
        adminUser(email);
        return suggestionRepository.findAllWithSubmitter()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SuggestionResponse updateSuggestion(Long id, SuggestionRequest request, String email) {
        adminUser(email);
        Suggestion suggestion = findSuggestion(id);
        suggestion.setContent(request.content());
        suggestionRepository.save(suggestion);
        return toResponse(findSuggestion(id));
    }

    private Suggestion findSuggestion(Long id) {
        return suggestionRepository.findWithSubmitter(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suggestion", id));
    }

    private User adminUser(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can manage suggestions");
        }
        return user;
    }

    private SuggestionResponse toResponse(Suggestion suggestion) {
        User submitter = suggestion.getSubmitter();
        return new SuggestionResponse(suggestion.getId(), suggestion.getContent(),
                new SuggestionUserSummary(submitter.getId(), submitter.getName()),
                suggestion.getCreatedAt(), suggestion.getUpdatedAt());
    }
}