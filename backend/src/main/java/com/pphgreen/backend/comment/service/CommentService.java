package com.pphgreen.backend.comment.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pphgreen.backend.comment.dto.CommentRequest;
import com.pphgreen.backend.comment.dto.CommentResponse;
import com.pphgreen.backend.comment.dto.CommentUserSummary;
import com.pphgreen.backend.comment.entity.Comment;
import com.pphgreen.backend.comment.repository.CommentRepository;
import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.event.entity.Event;
import com.pphgreen.backend.event.repository.EventRepository;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, EventRepository eventRepository,
                          UserService userService) {
        this.commentRepository = commentRepository;
        this.eventRepository = eventRepository;
        this.userService = userService;
    }

    public CommentResponse createComment(Long eventId, CommentRequest request, String authorEmail) {
        User author = user(authorEmail);
        Event event = eventRepository.findWithCreatedBy(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        Comment comment = new Comment();
        comment.setContent(request.content().trim());
        comment.setEvent(event);
        comment.setAuthor(author);

        Comment saved = commentRepository.save(comment);
        return toResponse(findWithAuthorById(saved.getId()));
    }

    public List<CommentResponse> getCommentsForEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event", eventId);
        }
        return commentRepository.findByEventIdWithAuthor(eventId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteComment(Long id, String actorEmail) {
        Comment comment = findWithAuthorById(id);
        User actor = user(actorEmail);

        boolean isAuthor = comment.getAuthor().getEmail().equalsIgnoreCase(actor.getEmail());
        if (!isAuthor && actor.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private Comment findWithAuthorById(Long id) {
        return commentRepository.findWithAuthorById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
    }

    private User user(String email) {
        return userService.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private CommentResponse toResponse(Comment comment) {
        User author = comment.getAuthor();
        return new CommentResponse(comment.getId(), comment.getContent(), comment.getEvent().getId(),
                new CommentUserSummary(author.getId(), author.getName()),
                comment.getCreatedAt(), comment.getUpdatedAt());
    }
}