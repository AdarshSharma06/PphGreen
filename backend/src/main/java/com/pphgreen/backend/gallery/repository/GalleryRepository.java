package com.pphgreen.backend.gallery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pphgreen.backend.gallery.entity.Gallery;

public interface GalleryRepository extends JpaRepository<Gallery, Long> {

    @Query("select g from Gallery g join fetch g.uploadedBy order by g.createdAt desc")
    List<Gallery> findAllWithUploader();
}