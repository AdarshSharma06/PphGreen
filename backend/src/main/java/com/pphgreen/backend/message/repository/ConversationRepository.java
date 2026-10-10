package com.pphgreen.backend.message.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pphgreen.backend.message.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("select c from Conversation c join fetch c.participantOne join fetch c.participantTwo where c.participantOne.id = :userId or c.participantTwo.id = :userId order by c.updatedAt desc")
    List<Conversation> findByParticipantIdOrderByUpdatedAtDesc(@Param("userId") Long userId);

    @Query("select c from Conversation c where (c.participantOne.id = :a and c.participantTwo.id = :b) or (c.participantOne.id = :b and c.participantTwo.id = :a)")
    Optional<Conversation> findByParticipants(@Param("a") Long a, @Param("b") Long b);

    @Query("select c from Conversation c join fetch c.participantOne join fetch c.participantTwo where c.id = :id")
    Optional<Conversation> findWithParticipantsById(@Param("id") Long id);
}
