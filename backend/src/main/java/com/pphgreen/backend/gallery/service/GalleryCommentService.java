package com.pphgreen.backend.gallery.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.gallery.dto.GalleryCommentRequest;
import com.pphgreen.backend.gallery.dto.GalleryCommentResponse;
import com.pphgreen.backend.gallery.dto.GalleryCommentUserSummary;
import com.pphgreen.backend.gallery.entity.Gallery;
import com.pphgreen.backend.gallery.entity.GalleryComment;
import com.pphgreen.backend.gallery.repository.GalleryCommentRepository;
import com.pphgreen.backend.gallery.repository.GalleryRepository;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class GalleryCommentService {

    private final GalleryCommentRepository commentRepository;
    private final GalleryRepository galleryRepository;
    private final UserService userService;

    public GalleryCommentService(GalleryCommentRepository commentRepository, GalleryRepository galleryRepository, UserService userService) {
        this.commentRepository = commentRepository;
        this.galleryRepository = galleryRepository;
        this.userService = userService;
    }

    public GalleryCommentResponse addComment(Long galleryId, GalleryCommentRequest request, String authorEmail) {
        User author = userService.findByEmail(authorEmail).orElseThrow(() -> new UnauthorizedException("User not found"));
        Gallery gallery = galleryRepository.findById(galleryId).orElseThrow(() -> new ResourceNotFoundException("Gallery", galleryId));
        GalleryComment comment = new GalleryComment();
        comment.setContent(request.content().trim());
        comment.setGallery(gallery);
        comment.setAuthor(author);
        GalleryComment saved = commentRepository.save(comment);
        return toResponse(commentRepository.findWithAuthorById(saved.getId()).orElse(saved));
    }

    public List<GalleryCommentResponse> getComments(Long galleryId) {
        if (!galleryRepository.existsById(galleryId)) {
            throw new ResourceNotFoundException("Gallery", galleryId);
        }
        return commentRepository.findByGalleryIdWithAuthor(galleryId).stream().map(this::toResponse).toList();
    }

    public void deleteComment(Long commentId, String actorEmail) {
        GalleryComment comment = commentRepository.findWithAuthorById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
        User actor = userService.findByEmail(actorEmail).orElseThrow(() -> new UnauthorizedException("User not found"));
        boolean isAuthor = comment.getAuthor().getId().equals(actor.getId());
        if (!isAuthor && actor.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

    private GalleryCommentResponse toResponse(GalleryComment c) {
        User author = c.getAuthor();
        String displayName = author.getName();
        if (displayName == null || displayName.isBlank()) displayName = author.getEmail();
        return new GalleryCommentResponse(c.getId(), c.getContent(), c.getGallery().getId(),
                new GalleryCommentUserSummary(author.getId(), displayName),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}
