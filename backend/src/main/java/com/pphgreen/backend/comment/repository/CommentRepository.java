package com.pphgreen.backend.comment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pphgreen.backend.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c join fetch c.author where c.event.id = :eventId order by c.createdAt asc")
    List<Comment> findByEventIdWithAuthor(@Param("eventId") Long eventId);

    @Query("select c from Comment c join fetch c.author where c.id = :id")
    Optional<Comment> findWithAuthorById(@Param("id") Long id);
}