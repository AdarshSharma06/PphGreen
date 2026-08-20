package com.pphgreen.backend.comment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pphgreen.backend.comment.dto.CommentRequest;
import com.pphgreen.backend.comment.dto.CommentResponse;
import com.pphgreen.backend.comment.entity.Comment;
import com.pphgreen.backend.comment.repository.CommentRepository;
import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.event.entity.Event;
import com.pphgreen.backend.event.repository.EventRepository;
import com.pphgreen.backend.user.entity.AccountStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserService userService;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, eventRepository, userService);
    }

    @Test
    void memberCanCreateComment() {
        User member = memberUser();
        Comment[] holder = new Comment[1];
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(eventRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(sampleEvent()));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(10L);
            holder[0] = c;
            return c;
        });
        when(commentRepository.findWithAuthorById(10L)).thenAnswer(invocation -> Optional.of(holder[0]));

        CommentResponse response = commentService.createComment(1L, new CommentRequest("Great event!"), "member@example.com");

        assertEquals("member@example.com", holder[0].getAuthor().getEmail());
        assertEquals(1L, holder[0].getEvent().getId());
        assertEquals("Great event!", holder[0].getContent());
        assertEquals("John Smith", response.author().name());
    }

    @Test
    void adminCanCreateComment() {
        User admin = adminUser();
        Comment[] holder = new Comment[1];
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(sampleEvent()));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(10L);
            holder[0] = c;
            return c;
        });
        when(commentRepository.findWithAuthorById(10L)).thenAnswer(invocation -> Optional.of(holder[0]));

        CommentResponse response = commentService.createComment(1L, new CommentRequest("Welcome!"), "admin@example.com");

        assertEquals("admin@example.com", holder[0].getAuthor().getEmail());
        assertEquals("Jane Doe", response.author().name());
    }

    @Test
    void commentsListedForEvent() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByEventIdWithAuthor(1L)).thenReturn(List.of(sampleComment(), sampleComment()));

        List<CommentResponse> responses = commentService.getCommentsForEvent(1L);

        assertEquals(2, responses.size());
        assertEquals("Nice event!", responses.get(0).content());
        assertEquals(1L, responses.get(0).eventId());
        assertEquals("John Smith", responses.get(0).author().name());
    }

    @Test
    void authorCanDeleteOwnComment() {
        Comment comment = sampleComment();
        when(commentRepository.findWithAuthorById(1L)).thenReturn(Optional.of(comment));
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(memberUser()));

        commentService.deleteComment(1L, "member@example.com");

        verify(commentRepository).delete(comment);
    }

    @Test
    void authorCannotDeleteAnotherUsersComment() {
        Comment comment = sampleComment();
        when(commentRepository.findWithAuthorById(1L)).thenReturn(Optional.of(comment));
        when(userService.findByEmail("other@example.com")).thenReturn(Optional.of(otherMemberUser()));

        assertThrows(ForbiddenException.class, () -> commentService.deleteComment(1L, "other@example.com"));
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void adminCanDeleteAnotherUsersComment() {
        Comment comment = sampleComment();
        when(commentRepository.findWithAuthorById(1L)).thenReturn(Optional.of(comment));
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser()));

        commentService.deleteComment(1L, "admin@example.com");

        verify(commentRepository).delete(comment);
    }

    @Test
    void createCommentMissingEventThrowsResourceNotFound() {
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(memberUser()));
        when(eventRepository.findWithCreatedBy(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> commentService.createComment(999L, new CommentRequest("Hi"), "member@example.com"));

        assertEquals("Event not found with id: 999", ex.getMessage());
    }

    @Test
    void listCommentsMissingEventThrowsResourceNotFound() {
        when(eventRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentsForEvent(999L));
    }

    @Test
    void deleteMissingCommentThrowsResourceNotFound() {
        when(commentRepository.findWithAuthorById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> commentService.deleteComment(999L, "member@example.com"));

        assertEquals("Comment not found with id: 999", ex.getMessage());
    }

    private Comment sampleComment() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("Nice event!");
        comment.setEvent(sampleEvent());
        comment.setAuthor(memberUser());
        return comment;
    }

    private Event sampleEvent() {
        Event event = new Event();
        event.setId(1L);
        return event;
    }

    private User memberUser() {
        User user = new User();
        user.setEmail("member@example.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.MEMBER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setName("John Smith");
        return user;
    }

    private User otherMemberUser() {
        User user = new User();
        user.setEmail("other@example.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.MEMBER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setName("Other Member");
        return user;
    }

    private User adminUser() {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.ADMIN);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setName("Jane Doe");
        return user;
    }
}