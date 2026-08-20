package com.pphgreen.backend.reaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pphgreen.backend.common.exception.ConflictException;
import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.event.entity.Event;
import com.pphgreen.backend.event.repository.EventRepository;
import com.pphgreen.backend.reaction.dto.ReactionRequest;
import com.pphgreen.backend.reaction.dto.ReactionResponse;
import com.pphgreen.backend.reaction.entity.Reaction;
import com.pphgreen.backend.reaction.entity.ReactionType;
import com.pphgreen.backend.reaction.repository.ReactionRepository;
import com.pphgreen.backend.user.entity.AccountStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {

    @Mock
    private ReactionRepository reactionRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserService userService;

    private ReactionService reactionService;

    @BeforeEach
    void setUp() {
        reactionService = new ReactionService(reactionRepository, eventRepository, userService);
    }

    @Test
    void memberCanAddReaction() {
        User member = memberUser();
        Reaction[] holder = new Reaction[1];
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(eventRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(sampleEvent()));
        when(reactionRepository.findByEventIdAndUserIdAndReactionType(1L, null, ReactionType.LIKE))
                .thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(invocation -> {
            Reaction r = invocation.getArgument(0);
            r.setId(10L);
            holder[0] = r;
            return r;
        });
        when(reactionRepository.findWithUserById(10L)).thenAnswer(invocation -> Optional.of(holder[0]));

        ReactionResponse response = reactionService.addReaction(1L, new ReactionRequest(ReactionType.LIKE), "member@example.com");

        assertEquals(1L, holder[0].getEvent().getId());
        assertEquals("member@example.com", holder[0].getUser().getEmail());
        assertEquals(ReactionType.LIKE, holder[0].getReactionType());
        assertEquals("John Smith", response.user().name());
    }

    @Test
    void adminCanAddReaction() {
        User admin = adminUser();
        Reaction[] holder = new Reaction[1];
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(sampleEvent()));
        when(reactionRepository.findByEventIdAndUserIdAndReactionType(1L, null, ReactionType.LOVE))
                .thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(invocation -> {
            Reaction r = invocation.getArgument(0);
            r.setId(10L);
            holder[0] = r;
            return r;
        });
        when(reactionRepository.findWithUserById(10L)).thenAnswer(invocation -> Optional.of(holder[0]));

        ReactionResponse response = reactionService.addReaction(1L, new ReactionRequest(ReactionType.LOVE), "admin@example.com");

        assertEquals("admin@example.com", holder[0].getUser().getEmail());
        assertEquals(ReactionType.LOVE, holder[0].getReactionType());
        assertEquals("Jane Doe", response.user().name());
    }

    @Test
    void userCanViewEventReactions() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(reactionRepository.findByEventIdWithUser(1L)).thenReturn(List.of(sampleReaction(), sampleReaction()));

        List<ReactionResponse> responses = reactionService.getReactionsForEvent(1L);

        assertEquals(2, responses.size());
        assertEquals(ReactionType.LIKE, responses.get(0).reactionType());
        assertEquals(1L, responses.get(0).eventId());
        assertEquals("John Smith", responses.get(0).user().name());
    }

    @Test
    void userCanRemoveOwnReaction() {
        Reaction own = sampleReaction();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(memberUser()));
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(reactionRepository.findByEventIdAndUserIdWithUser(1L, null)).thenReturn(List.of(own));

        reactionService.removeReaction(1L, "member@example.com");

        verify(reactionRepository).deleteAll(List.of(own));
    }

    @Test
    void userCannotRemoveAnotherUsersReaction() {
        Reaction other = sampleReaction();
        other.setUser(otherMemberUser());
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(memberUser()));
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(reactionRepository.findByEventIdAndUserIdWithUser(1L, null)).thenReturn(List.of(other));

        assertThrows(ForbiddenException.class, () -> reactionService.removeReaction(1L, "member@example.com"));
        verify(reactionRepository, never()).deleteAll(any());
    }

    @Test
    void duplicateSameReactionRejected() {
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(memberUser()));
        when(eventRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(sampleEvent()));
        when(reactionRepository.findByEventIdAndUserIdAndReactionType(1L, null, ReactionType.LIKE))
                .thenReturn(Optional.of(sampleReaction()));

        assertThrows(ConflictException.class,
                () -> reactionService.addReaction(1L, new ReactionRequest(ReactionType.LIKE), "member@example.com"));
        verify(reactionRepository, never()).save(any(Reaction.class));
    }

    @Test
    void differentReactionTypesAllowed() {
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(memberUser()));
        when(eventRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(sampleEvent()));
        when(reactionRepository.findByEventIdAndUserIdAndReactionType(1L, null, ReactionType.LIKE))
                .thenReturn(Optional.empty());
        when(reactionRepository.findByEventIdAndUserIdAndReactionType(1L, null, ReactionType.LOVE))
                .thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reactionRepository.findWithUserById(any())).thenAnswer(invocation -> Optional.of(sampleReaction()));

        reactionService.addReaction(1L, new ReactionRequest(ReactionType.LIKE), "member@example.com");
        reactionService.addReaction(1L, new ReactionRequest(ReactionType.LOVE), "member@example.com");

        verify(reactionRepository, times(2)).save(any(Reaction.class));
    }

    @Test
    void addReactionMissingEventThrowsResourceNotFound() {
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(memberUser()));
        when(eventRepository.findWithCreatedBy(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> reactionService.addReaction(999L, new ReactionRequest(ReactionType.LIKE), "member@example.com"));

        assertEquals("Event not found with id: 999", ex.getMessage());
    }

    @Test
    void viewReactionsMissingEventThrowsResourceNotFound() {
        when(eventRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> reactionService.getReactionsForEvent(999L));
    }

    @Test
    void removeReactionMissingEventThrowsResourceNotFound() {
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(memberUser()));
        when(eventRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> reactionService.removeReaction(999L, "member@example.com"));
    }

    @Test
    void removeReactionWithNoOwnReactionThrowsResourceNotFound() {
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(memberUser()));
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(reactionRepository.findByEventIdAndUserIdWithUser(1L, null)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> reactionService.removeReaction(1L, "member@example.com"));
    }

    private Reaction sampleReaction() {
        Reaction reaction = new Reaction();
        reaction.setId(1L);
        reaction.setEvent(sampleEvent());
        reaction.setUser(memberUser());
        reaction.setReactionType(ReactionType.LIKE);
        return reaction;
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