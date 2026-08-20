package com.pphgreen.backend.reaction.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pphgreen.backend.reaction.entity.Reaction;
import com.pphgreen.backend.reaction.entity.ReactionType;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByEventIdAndUserIdAndReactionType(Long eventId, Long userId, ReactionType reactionType);

    @Query("select r from Reaction r join fetch r.user where r.event.id = :eventId order by r.createdAt asc")
    List<Reaction> findByEventIdWithUser(@Param("eventId") Long eventId);

    @Query("select r from Reaction r join fetch r.user where r.id = :id")
    Optional<Reaction> findWithUserById(@Param("id") Long id);

    @Query("select r from Reaction r join fetch r.user where r.event.id = :eventId and r.user.id = :userId")
    List<Reaction> findByEventIdAndUserIdWithUser(@Param("eventId") Long eventId, @Param("userId") Long userId);
}