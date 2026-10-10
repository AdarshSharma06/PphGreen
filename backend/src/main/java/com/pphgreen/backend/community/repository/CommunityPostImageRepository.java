package com.pphgreen.backend.community.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pphgreen.backend.community.entity.CommunityPostImage;

public interface CommunityPostImageRepository extends JpaRepository<CommunityPostImage, Long> {

    List<CommunityPostImage> findByPostIdOrderByDisplayOrderAsc(Long postId);

    void deleteByPostId(Long postId);
}
