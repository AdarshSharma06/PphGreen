package com.pphgreen.backend.community.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pphgreen.backend.community.entity.CommunityPost;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    @Query("select p from CommunityPost p join fetch p.author order by p.createdAt asc, p.id asc")
    List<CommunityPost> findAllOrderedAsc();

    @Query("select p from CommunityPost p join fetch p.author where p.id < :cursor order by p.id desc")
    List<CommunityPost> findByIdLessThanOrderByIdDesc(@Param("cursor") Long cursor, Pageable pageable);

    @Query("select p from CommunityPost p join fetch p.author order by p.id desc")
    List<CommunityPost> findLatestOrderByIdDesc(Pageable pageable);

    @Query("select p from CommunityPost p join fetch p.author where p.id = :id")
    java.util.Optional<CommunityPost> findWithAuthorById(@Param("id") Long id);
}
