package com.pphgreen.backend.gallery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pphgreen.backend.gallery.entity.GalleryComment;

public interface GalleryCommentRepository extends JpaRepository<GalleryComment, Long> {

    @Query("select c from GalleryComment c join fetch c.author where c.gallery.id = :galleryId order by c.createdAt asc")
    List<GalleryComment> findByGalleryIdWithAuthor(@Param("galleryId") Long galleryId);

    @Query("select c from GalleryComment c join fetch c.author where c.id = :id")
    Optional<GalleryComment> findWithAuthorById(@Param("id") Long id);

    long countByGalleryId(Long galleryId);

    void deleteByGalleryId(Long galleryId);
}
