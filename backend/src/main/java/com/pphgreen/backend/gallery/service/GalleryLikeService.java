package com.pphgreen.backend.gallery.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.pphgreen.backend.common.exception.BadRequestException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.gallery.entity.Gallery;
import com.pphgreen.backend.gallery.entity.GalleryLike;
import com.pphgreen.backend.gallery.repository.GalleryLikeRepository;
import com.pphgreen.backend.gallery.repository.GalleryRepository;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class GalleryLikeService {

    private final GalleryLikeRepository likeRepository;
    private final GalleryRepository galleryRepository;
    private final UserService userService;

    public GalleryLikeService(GalleryLikeRepository likeRepository, GalleryRepository galleryRepository, UserService userService) {
        this.likeRepository = likeRepository;
        this.galleryRepository = galleryRepository;
        this.userService = userService;
    }

    public long like(Long galleryId, String userEmail) {
        User user = userService.findByEmail(userEmail).orElseThrow(() -> new ResourceNotFoundException("User", 0L));
        Gallery gallery = galleryRepository.findById(galleryId).orElseThrow(() -> new ResourceNotFoundException("Gallery", galleryId));
        if (likeRepository.existsByGalleryIdAndUserId(galleryId, user.getId())) {
            return likeRepository.countByGalleryId(galleryId);
        }
        GalleryLike like = new GalleryLike();
        like.setGallery(gallery);
        like.setUser(user);
        try {
            likeRepository.save(like);
        } catch (DataIntegrityViolationException e) {
            // concurrent duplicate
        }
        return likeRepository.countByGalleryId(galleryId);
    }

    public long unlike(Long galleryId, String userEmail) {
        User user = userService.findByEmail(userEmail).orElseThrow(() -> new ResourceNotFoundException("User", 0L));
        var existing = likeRepository.findByGalleryIdAndUserId(galleryId, user.getId());
        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("Like not found");
        }
        likeRepository.delete(existing.get());
        return likeRepository.countByGalleryId(galleryId);
    }

    public long count(Long galleryId) {
        if (!galleryRepository.existsById(galleryId)) {
            throw new ResourceNotFoundException("Gallery", galleryId);
        }
        return likeRepository.countByGalleryId(galleryId);
    }

    public boolean isLiked(Long galleryId, String userEmail) {
        var userOpt = userService.findByEmail(userEmail);
        if (userOpt.isEmpty()) return false;
        return likeRepository.existsByGalleryIdAndUserId(galleryId, userOpt.get().getId());
    }
}
