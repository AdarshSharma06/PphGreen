package com.pphgreen.backend.gallery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pphgreen.backend.gallery.entity.Gallery;

public interface GalleryRepository extends JpaRepository<Gallery, Long> {

    @Query("select g from Gallery g join fetch g.uploadedBy order by g.createdAt desc")
    List<Gallery> findAllWithUploader();

    @Query("select g from Gallery g join fetch g.uploadedBy where g.event.id = :eventId order by g.createdAt desc")
    List<Gallery> findByEventIdWithUploader(@org.springframework.data.repository.query.Param("eventId") Long eventId);

    @Query("select g from Gallery g join fetch g.uploadedBy where g.event is null order by g.createdAt desc")
    List<Gallery> findStandaloneWithUploader();

    @Query("select g from Gallery g join fetch g.uploadedBy where g.event.id = :eventId and g.highlight = true order by g.highlightOrder asc, g.createdAt asc")
    List<Gallery> findHighlightsByEventId(@org.springframework.data.repository.query.Param("eventId") Long eventId);

    @Query("select g from Gallery g join fetch g.uploadedBy left join fetch g.event where g.id = :id")
    java.util.Optional<Gallery> findWithUploaderAndEvent(@org.springframework.data.repository.query.Param("id") Long id);

    long countByEventId(Long eventId);

    long countByEventIsNull();

    void deleteByEventId(Long eventId);

    // For efficient album thumbnail picking, count by event
    @Query("select g.event.id, count(g) from Gallery g where g.event is not null group by g.event.id")
    List<Object[]> countByEventGrouped();
}