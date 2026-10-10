package com.pphgreen.backend.message.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.message.dto.DirectMessageResponse;
import com.pphgreen.backend.message.entity.Conversation;
import com.pphgreen.backend.message.entity.DirectMessage;
import com.pphgreen.backend.message.repository.ConversationRepository;
import com.pphgreen.backend.message.repository.DirectMessageRepository;
import com.pphgreen.backend.user.entity.AccountStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private DirectMessageRepository messageRepository;
    @Mock private UserService userService;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(conversationRepository, messageRepository, userService);
    }

    private User user(Long id, String name) {
        User u = new User();
        u.setEmail(name.toLowerCase().replace(" ", "") + "@example.com");
        u.setPasswordHash("hash");
        u.setRole(Role.MEMBER);
        u.setAccountStatus(AccountStatus.ACTIVE);
        u.setName(name);
        // set id via reflection
        try { var f = User.class.getDeclaredField("id"); f.setAccessible(true); f.set(u, id); } catch (Exception e) { throw new RuntimeException(e); }
        return u;
    }

    private Conversation conversation(Long id, User a, User b) {
        Conversation c = new Conversation();
        try { var f = Conversation.class.getDeclaredField("id"); f.setAccessible(true); f.set(c, id); } catch (Exception e) { throw new RuntimeException(e); }
        c.setParticipantOne(a);
        c.setParticipantTwo(b);
        return c;
    }

    private DirectMessage message(Long id, Conversation conv, User sender, String content) {
        DirectMessage m = new DirectMessage();
        try { var f = DirectMessage.class.getDeclaredField("id"); f.setAccessible(true); f.set(m, id); } catch (Exception e) { throw new RuntimeException(e); }
        m.setConversation(conv);
        m.setSender(sender);
        m.setContent(content);
        // set createdAt
        try { var f = DirectMessage.class.getDeclaredField("createdAt"); f.setAccessible(true); f.set(m, java.time.Instant.now().plusSeconds(id)); } catch (Exception e) {}
        return m;
    }

    @Test
    void fetchConversationWithMessagesReturnsChronologicalOrder() {
        User ankit = user(1L, "Ankit Kumar");
        User adarsh = user(2L, "Adarsh Sharma");
        Conversation conv = conversation(10L, ankit, adarsh);
        User current = ankit;
        when(userService.findByEmail("ankit@example.com")).thenReturn(Optional.of(current));
        when(conversationRepository.findWithParticipantsById(10L)).thenReturn(Optional.of(conv));
        // latest 3 messages in DB are id 3,2,1 DESC, but service reverses to ASC
        DirectMessage m1 = message(1L, conv, ankit, "Hi");
        DirectMessage m2 = message(2L, conv, adarsh, "Hello");
        DirectMessage m3 = message(3L, conv, ankit, "How are you?");
        when(messageRepository.findLatestByConversationId(eq(10L), any(Pageable.class))).thenReturn(List.of(m3, m2, m1));

        List<DirectMessageResponse> res = messageService.getMessages(10L, null, 30, "ankit@example.com");

        assertEquals(3, res.size());
        assertEquals("Hi", res.get(0).content());
        assertEquals("Hello", res.get(1).content());
        assertEquals("How are you?", res.get(2).content());
        assertEquals(1L, res.get(0).senderId());
        assertEquals(2L, res.get(1).senderId());
    }

    @Test
    void fetchEmptyConversationReturnsEmptyList() {
        User ankit = user(1L, "Ankit Kumar");
        User adarsh = user(2L, "Adarsh Sharma");
        Conversation conv = conversation(10L, ankit, adarsh);
        when(userService.findByEmail("ankit@example.com")).thenReturn(Optional.of(ankit));
        when(conversationRepository.findWithParticipantsById(10L)).thenReturn(Optional.of(conv));
        when(messageRepository.findLatestByConversationId(eq(10L), any(Pageable.class))).thenReturn(List.of());

        List<DirectMessageResponse> res = messageService.getMessages(10L, null, 30, "ankit@example.com");

        assertTrue(res.isEmpty());
    }

    @Test
    void fetchWithCursorReturnsOlderMessages() {
        User ankit = user(1L, "Ankit");
        User adarsh = user(2L, "Adarsh");
        Conversation conv = conversation(10L, ankit, adarsh);
        when(userService.findByEmail("ankit@example.com")).thenReturn(Optional.of(ankit));
        when(conversationRepository.findWithParticipantsById(10L)).thenReturn(Optional.of(conv));
        DirectMessage m1 = message(1L, conv, ankit, "Oldest");
        DirectMessage m2 = message(2L, conv, adarsh, "Middle");
        when(messageRepository.findByConversationIdAndIdLessThanOrderByIdDesc(eq(10L), eq(5L), any(Pageable.class))).thenReturn(List.of(m2, m1));

        List<DirectMessageResponse> res = messageService.getMessages(10L, 5L, 30, "ankit@example.com");

        assertEquals(2, res.size());
        assertEquals("Oldest", res.get(0).content());
    }

    @Test
    void markReadSucceedsWithNoUnread() {
        User ankit = user(1L, "Ankit");
        User adarsh = user(2L, "Adarsh");
        Conversation conv = conversation(10L, ankit, adarsh);
        when(userService.findByEmail("ankit@example.com")).thenReturn(Optional.of(ankit));
        when(conversationRepository.findWithParticipantsById(10L)).thenReturn(Optional.of(conv));
        when(messageRepository.findUnreadByConversationIdAndNotSender(10L, 1L)).thenReturn(List.of());

        assertDoesNotThrow(() -> messageService.markRead(10L, "ankit@example.com"));
        verify(messageRepository, never()).save(any());
    }

    @Test
    void markReadMarksOnlyOtherParticipantMessages() {
        User ankit = user(1L, "Ankit");
        User adarsh = user(2L, "Adarsh");
        Conversation conv = conversation(10L, ankit, adarsh);
        DirectMessage m = message(2L, conv, adarsh, "Hi");
        when(userService.findByEmail("ankit@example.com")).thenReturn(Optional.of(ankit));
        when(conversationRepository.findWithParticipantsById(10L)).thenReturn(Optional.of(conv));
        when(messageRepository.findUnreadByConversationIdAndNotSender(10L, 1L)).thenReturn(List.of(m));

        messageService.markRead(10L, "ankit@example.com");

        assertNotNull(m.getReadAt());
        verify(messageRepository).save(m);
    }

    @Test
    void nonParticipantCannotFetchMessages() {
        User ankit = user(1L, "Ankit");
        User adarsh = user(2L, "Adarsh");
        User outsider = user(3L, "Outsider");
        Conversation conv = conversation(10L, ankit, adarsh);
        when(userService.findByEmail("outsider@example.com")).thenReturn(Optional.of(outsider));
        when(conversationRepository.findWithParticipantsById(10L)).thenReturn(Optional.of(conv));

        assertThrows(ForbiddenException.class, () -> messageService.getMessages(10L, null, 30, "outsider@example.com"));
    }

    @Test
    void senderCannotBeForged() {
        // sender is derived from currentEmail, not from payload
        User ankit = user(1L, "Ankit");
        User adarsh = user(2L, "Adarsh");
        Conversation conv = conversation(10L, ankit, adarsh);
        when(userService.findByEmail("ankit@example.com")).thenReturn(Optional.of(ankit));
        when(conversationRepository.findWithParticipantsById(10L)).thenReturn(Optional.of(conv));
        when(messageRepository.save(any(DirectMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        DirectMessageResponse res = messageService.sendMessage(10L, "Hello", "ankit@example.com");

        assertEquals(1L, res.senderId());
        assertEquals("Ankit", res.senderName());
        // even if client tried to send senderId=2, service ignores and uses current
    }
}
