package com.pphgreen.backend.message.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pphgreen.backend.message.entity.DirectMessage;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    @Query("select m from DirectMessage m join fetch m.sender where m.conversation.id = :conversationId order by m.createdAt asc, m.id asc")
    List<DirectMessage> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);

    @Query("select m from DirectMessage m where m.conversation.id = :conversationId order by m.createdAt desc, m.id desc")
    List<DirectMessage> findLatestByConversationId(@Param("conversationId") Long conversationId, Pageable pageable);

    @Query("select m from DirectMessage m where m.conversation.id = :conversationId and m.id < :cursor order by m.id desc")
    List<DirectMessage> findByConversationIdAndIdLessThanOrderByIdDesc(@Param("conversationId") Long conversationId, @Param("cursor") Long cursor, Pageable pageable);

    @Query("select m from DirectMessage m where m.conversation.id = :conversationId order by m.createdAt desc")
    java.util.Optional<DirectMessage> findTopByConversationIdOrderByCreatedAtDesc(@Param("conversationId") Long conversationId);

    long countByConversationIdAndSenderIdNotAndReadAtIsNull(Long conversationId, Long senderId);

    @Query("select m from DirectMessage m where m.conversation.id = :conversationId and m.sender.id != :userId and m.readAt is null")
    List<DirectMessage> findUnreadByConversationIdAndNotSender(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
