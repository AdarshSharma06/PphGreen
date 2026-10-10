package com.pphgreen.backend.community.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pphgreen.backend.community.entity.CommunityPostComment;

public interface CommunityPostCommentRepository extends JpaRepository<CommunityPostComment, Long> {

    @Query("select c from CommunityPostComment c join fetch c.author where c.post.id = :postId order by c.createdAt asc, c.id asc")
    List<CommunityPostComment> findByPostIdWithAuthor(@Param("postId") Long postId);

    @Query("select c from CommunityPostComment c join fetch c.author where c.id = :id")
    Optional<CommunityPostComment> findWithAuthorById(@Param("id") Long id);

    long countByPostId(Long postId);
}
