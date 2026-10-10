package com.pphgreen.backend.message.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pphgreen.backend.common.exception.BadRequestException;
import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.community.dto.CommunityPostAuthorSummary;
import com.pphgreen.backend.message.dto.ConversationResponse;
import com.pphgreen.backend.message.dto.DirectMessageResponse;
import com.pphgreen.backend.message.entity.Conversation;
import com.pphgreen.backend.message.entity.DirectMessage;
import com.pphgreen.backend.message.repository.ConversationRepository;
import com.pphgreen.backend.message.repository.DirectMessageRepository;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final DirectMessageRepository messageRepository;
    private final UserService userService;

    public MessageService(ConversationRepository conversationRepository, DirectMessageRepository messageRepository, UserService userService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> listConversations(String currentEmail) {
        User current = userService.findByEmail(currentEmail).orElseThrow(() -> new UnauthorizedException("User not found"));
        List<Conversation> convs = conversationRepository.findByParticipantIdOrderByUpdatedAtDesc(current.getId());
        return convs.stream().map(c -> toConversationResponse(c, current.getId())).collect(Collectors.toList());
    }

    @Transactional
    public ConversationResponse findOrCreateConversation(Long otherUserId, String currentEmail) {
        if (otherUserId == null) throw new BadRequestException("Other user required");
        User current = userService.findByEmail(currentEmail).orElseThrow(() -> new UnauthorizedException("User not found"));
        if (current.getId().equals(otherUserId)) throw new BadRequestException("Cannot message yourself");
        User other = userService.findById(otherUserId);
        // canonical ordering by id to prevent duplicates
        Long a = Math.min(current.getId(), other.getId());
        Long b = Math.max(current.getId(), other.getId());
        // Need to load users for canonical order
        User p1 = a.equals(current.getId()) ? current : other;
        User p2 = b.equals(current.getId()) ? current : other;

        var existing = conversationRepository.findByParticipants(a, b);
        if (existing.isPresent()) {
            return toConversationResponse(existing.get(), current.getId());
        }
        Conversation conv = new Conversation();
        conv.setParticipantOne(p1);
        conv.setParticipantTwo(p2);
        try {
            Conversation saved = conversationRepository.save(conv);
            return toConversationResponse(saved, current.getId());
        } catch (DataIntegrityViolationException e) {
            // concurrent creation
            var retry = conversationRepository.findByParticipants(a, b).orElseThrow(() -> e);
            return toConversationResponse(retry, current.getId());
        }
    }

    @Transactional(readOnly = true)
    public List<DirectMessageResponse> getMessages(Long conversationId, Long cursor, Integer limit, String currentEmail) {
        User current = userService.findByEmail(currentEmail).orElseThrow(() -> new UnauthorizedException("User not found"));
        Conversation conv = conversationRepository.findWithParticipantsById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
        if (!conv.isParticipant(current.getId())) throw new ForbiddenException("Not a participant");
        int lim = (limit == null || limit <= 0 || limit > 50) ? 30 : limit;
        Pageable pageable = PageRequest.of(0, lim);
        List<DirectMessage> msgs;
        if (cursor == null) {
            msgs = new java.util.ArrayList<>(messageRepository.findLatestByConversationId(conversationId, pageable));
            // reverse to ASC for display (oldest first, newest last) — but for messages we want chronological ASC
            java.util.Collections.reverse(msgs);
        } else {
            msgs = new java.util.ArrayList<>(messageRepository.findByConversationIdAndIdLessThanOrderByIdDesc(conversationId, cursor, pageable));
            java.util.Collections.reverse(msgs);
        }
        return msgs.stream().map(this::toMessageResponse).collect(Collectors.toList());
    }

    @Transactional
    public DirectMessageResponse sendMessage(Long conversationId, String content, String currentEmail) {
        if (content == null || content.trim().isEmpty()) throw new BadRequestException("Message content required");
        if (content.length() > 2000) throw new BadRequestException("Message too long");
        User current = userService.findByEmail(currentEmail).orElseThrow(() -> new UnauthorizedException("User not found"));
        Conversation conv = conversationRepository.findWithParticipantsById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
        if (!conv.isParticipant(current.getId())) throw new ForbiddenException("Not a participant");
        DirectMessage msg = new DirectMessage();
        msg.setConversation(conv);
        msg.setSender(current);
        msg.setContent(content.trim());
        DirectMessage saved = messageRepository.save(msg);
        // touch conversation updatedAt
        conv.setParticipantOne(conv.getParticipantOne()); // trigger @PreUpdate via save
        conversationRepository.save(conv);
        return toMessageResponse(saved);
    }

    @Transactional
    public void markRead(Long conversationId, String currentEmail) {
        User current = userService.findByEmail(currentEmail).orElseThrow(() -> new UnauthorizedException("User not found"));
        Conversation conv = conversationRepository.findWithParticipantsById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
        if (!conv.isParticipant(current.getId())) throw new ForbiddenException("Not a participant");
        List<DirectMessage> unread = messageRepository.findUnreadByConversationIdAndNotSender(conversationId, current.getId());
        for (DirectMessage m : unread) {
            m.setReadAt(java.time.Instant.now());
            messageRepository.save(m);
        }
    }

    private ConversationResponse toConversationResponse(Conversation c, Long currentUserId) {
        User other = c.getOtherParticipant(currentUserId);
        String otherName = other.getName() != null && !other.getName().isBlank() ? other.getName() : other.getEmail();
        CommunityPostAuthorSummary otherSummary = new CommunityPostAuthorSummary(other.getId(), otherName, other.getProfilePicture());
        var lastOpt = messageRepository.findTopByConversationIdOrderByCreatedAtDesc(c.getId());
        String preview = lastOpt.map(DirectMessage::getContent).orElse("");
        if (preview.length() > 60) preview = preview.substring(0, 57) + "...";
        var lastAt = lastOpt.map(DirectMessage::getCreatedAt).orElse(c.getUpdatedAt());
        long unread = messageRepository.countByConversationIdAndSenderIdNotAndReadAtIsNull(c.getId(), currentUserId);
        // But need to count only where sender != current and readAt null
        // Use findUnread count
        long unread2 = messageRepository.findUnreadByConversationIdAndNotSender(c.getId(), currentUserId).size();
        return new ConversationResponse(c.getId(), otherSummary, preview, lastAt, unread2, c.getCreatedAt(), c.getUpdatedAt());
    }

    private DirectMessageResponse toMessageResponse(DirectMessage m) {
        return new DirectMessageResponse(m.getId(), m.getConversation().getId(), m.getSender().getId(), m.getSender().getName() != null && !m.getSender().getName().isBlank() ? m.getSender().getName() : m.getSender().getEmail(), m.getContent(), m.getCreatedAt(), m.getReadAt());
    }
}
