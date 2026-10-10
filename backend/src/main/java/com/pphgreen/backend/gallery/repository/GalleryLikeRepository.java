package com.pphgreen.backend.gallery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pphgreen.backend.gallery.entity.GalleryLike;

public interface GalleryLikeRepository extends JpaRepository<GalleryLike, Long> {

    Optional<GalleryLike> findByGalleryIdAndUserId(Long galleryId, Long userId);

    long countByGalleryId(Long galleryId);

    @Query("select count(l) from GalleryLike l where l.gallery.id = :galleryId")
    long countLikes(@Param("galleryId") Long galleryId);

    boolean existsByGalleryIdAndUserId(Long galleryId, Long userId);

    void deleteByGalleryId(Long galleryId);
}
