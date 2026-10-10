package com.pphgreen.backend.reaction.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.pphgreen.backend.common.exception.ConflictException;
import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.event.entity.Event;
import com.pphgreen.backend.event.repository.EventRepository;
import com.pphgreen.backend.reaction.dto.ReactionRequest;
import com.pphgreen.backend.reaction.dto.ReactionResponse;
import com.pphgreen.backend.reaction.dto.ReactionUserSummary;
import com.pphgreen.backend.reaction.entity.Reaction;
import com.pphgreen.backend.reaction.entity.ReactionType;
import com.pphgreen.backend.reaction.repository.ReactionRepository;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final EventRepository eventRepository;
    private final UserService userService;

    public ReactionService(ReactionRepository reactionRepository, EventRepository eventRepository,
                           UserService userService) {
        this.reactionRepository = reactionRepository;
        this.eventRepository = eventRepository;
        this.userService = userService;
    }

    public ReactionResponse addReaction(Long eventId, ReactionRequest request, String userEmail) {
        User user = user(userEmail);
        Event event = eventRepository.findWithCreatedBy(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        if (reactionRepository.findByEventIdAndUserIdAndReactionType(eventId, user.getId(), request.reactionType())
                .isPresent()) {
            throw new ConflictException("Reaction already exists");
        }

        Reaction reaction = new Reaction();
        reaction.setEvent(event);
        reaction.setUser(user);
        reaction.setReactionType(request.reactionType());

        try {
            Reaction saved = reactionRepository.save(reaction);
            return toResponse(findWithUserById(saved.getId()));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Reaction already exists");
        }
    }

    public List<ReactionResponse> getReactionsForEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event", eventId);
        }
        return reactionRepository.findByEventIdWithUser(eventId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void removeReaction(Long eventId, String userEmail) {
        removeReaction(eventId, userEmail, null);
    }

    public void removeReaction(Long eventId, String userEmail, ReactionType reactionType) {
        User user = user(userEmail);
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event", eventId);
        }

        if (reactionType != null) {
            var existing = reactionRepository.findByEventIdAndUserIdAndReactionType(eventId, user.getId(), reactionType);
            if (existing.isEmpty()) {
                throw new ResourceNotFoundException("Reaction not found for event: " + eventId);
            }
            Reaction reaction = existing.get();
            if (!reaction.getUser().getEmail().equalsIgnoreCase(user.getEmail())) {
                throw new ForbiddenException("You can only remove your own reactions");
            }
            reactionRepository.delete(reaction);
            return;
        }

        List<Reaction> reactions = reactionRepository.findByEventIdAndUserIdWithUser(eventId, user.getId());
        if (reactions.isEmpty()) {
            throw new ResourceNotFoundException("Reaction not found for event: " + eventId);
        }

        for (Reaction reaction : reactions) {
            if (!reaction.getUser().getEmail().equalsIgnoreCase(user.getEmail())) {
                throw new ForbiddenException("You can only remove your own reactions");
            }
        }

        reactionRepository.deleteAll(reactions);
    }

    private Reaction findWithUserById(Long id) {
        return reactionRepository.findWithUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reaction", id));
    }

    private User user(String email) {
        return userService.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private ReactionResponse toResponse(Reaction reaction) {
        User user = reaction.getUser();
        String displayName = user.getName();
        if (displayName == null || displayName.isBlank()) {
            displayName = user.getEmail();
        }
        return new ReactionResponse(reaction.getId(), reaction.getReactionType(), reaction.getEvent().getId(),
                new ReactionUserSummary(user.getId(), displayName), reaction.getCreatedAt());
    }
}