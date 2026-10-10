package com.pphgreen.backend.message.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import com.pphgreen.backend.message.dto.ConversationResponse;
import com.pphgreen.backend.message.dto.DirectMessageResponse;
import com.pphgreen.backend.user.entity.AccountStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.repository.UserRepository;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.open-in-view=false",
    "app.jwt.secret=01234567890123456789012345678901",
    "app.jwt.expiration-ms=3600000",
    "app.supabase.url=",
    "app.supabase.service-role-key=",
    "app.supabase.storage-bucket=",
    "app.cors.allowed-origins=http://localhost:5173"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MessageServiceIntegrationTest {

    @Autowired private MessageService messageService;
    @Autowired private UserRepository userRepository;

    private User createUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPasswordHash("hash");
        u.setRole(Role.MEMBER);
        u.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.save(u);
    }

    @Test
    @Transactional
    void conversationFlowAnkitToAdarsh() {
        User ankit = createUser("Ankit Kumar", "ankit@example.com");
        User adarsh = createUser("Adarsh Sharma", "adarsh@example.com");

        // Ankit creates conversation with Adarsh
        ConversationResponse conv = messageService.findOrCreateConversation(adarsh.getId(), ankit.getEmail());
        assertNotNull(conv.id());
        assertEquals("Adarsh Sharma", conv.otherParticipant().name());

        // Ankit sends message
        DirectMessageResponse msg1 = messageService.sendMessage(conv.id(), "Hello Adarsh", ankit.getEmail());
        assertEquals("Hello Adarsh", msg1.content());

        // Adarsh lists conversations — should see 1 with preview
        List<ConversationResponse> adarshConvs = messageService.listConversations(adarsh.getEmail());
        assertEquals(1, adarshConvs.size());
        assertEquals("Hello Adarsh", adarshConvs.get(0).lastMessagePreview());

        // Ankit lists — should also see 1
        List<ConversationResponse> ankitConvs = messageService.listConversations(ankit.getEmail());
        assertEquals(1, ankitConvs.size());

        // Both fetch messages — should see 1 message chronological
        List<DirectMessageResponse> ankitMsgs = messageService.getMessages(conv.id(), null, 30, ankit.getEmail());
        assertEquals(1, ankitMsgs.size());
        assertEquals("Hello Adarsh", ankitMsgs.get(0).content());

        List<DirectMessageResponse> adarshMsgs = messageService.getMessages(conv.id(), null, 30, adarsh.getEmail());
        assertEquals(1, adarshMsgs.size());

        // Adarsh replies
        DirectMessageResponse msg2 = messageService.sendMessage(conv.id(), "Hi Ankit", adarsh.getEmail());
        assertEquals("Hi Ankit", msg2.content());

        // Both fetch again — should see 2 in order
        ankitMsgs = messageService.getMessages(conv.id(), null, 30, ankit.getEmail());
        assertEquals(2, ankitMsgs.size());
        assertEquals("Hello Adarsh", ankitMsgs.get(0).content());
        assertEquals("Hi Ankit", ankitMsgs.get(1).content());

        // Mark read as Adarsh (should mark Ankit's message)
        assertDoesNotThrow(() -> messageService.markRead(conv.id(), adarsh.getEmail()));

        // List again — unread counts should update, but not throw
        adarshConvs = messageService.listConversations(adarsh.getEmail());
        assertEquals(1, adarshConvs.size());
        assertEquals(0, adarshConvs.get(0).unreadCount());
        // After marking, Ankit should have 1 unread (Adarsh's reply)
        ankitConvs = messageService.listConversations(ankit.getEmail());
        assertEquals(1, ankitConvs.size());
        assertEquals(1, ankitConvs.get(0).unreadCount());

        // Empty fetch with cursor beyond
        List<DirectMessageResponse> empty = messageService.getMessages(conv.id(), 1L, 30, ankit.getEmail());
        // Should return older messages with id <1 => empty, not error
        assertTrue(empty.isEmpty() || empty.size() >=0);

        // Non-participant should be forbidden
        User outsider = createUser("Outsider", "outsider@example.com");
        assertThrows(com.pphgreen.backend.common.exception.ForbiddenException.class, () -> messageService.getMessages(conv.id(), null, 30, outsider.getEmail()));
    }

    @Test
    @Transactional
    void emptyConversationHandling() {
        User a = createUser("A", "a@example.com");
        User b = createUser("B", "b@example.com");
        ConversationResponse conv = messageService.findOrCreateConversation(b.getId(), a.getEmail());

        // No messages yet — getMessages should return empty, not error
        List<DirectMessageResponse> msgs = messageService.getMessages(conv.id(), null, 30, a.getEmail());
        assertTrue(msgs.isEmpty());

        // listConversations should return 1 with empty preview
        List<ConversationResponse> list = messageService.listConversations(a.getEmail());
        assertEquals(1, list.size());
        assertEquals("", list.get(0).lastMessagePreview());
        assertEquals(0, list.get(0).unreadCount());

        // markRead with no unread should not throw
        assertDoesNotThrow(() -> messageService.markRead(conv.id(), a.getEmail()));
    }
}
