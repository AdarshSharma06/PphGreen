package com.pphgreen.backend.gallery.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pphgreen.backend.common.response.ApiResponse;
import com.pphgreen.backend.gallery.dto.GalleryAlbumResponse;
import com.pphgreen.backend.gallery.dto.GalleryCommentRequest;
import com.pphgreen.backend.gallery.dto.GalleryCommentResponse;
import com.pphgreen.backend.gallery.dto.GalleryResponse;
import com.pphgreen.backend.gallery.service.GalleryCommentService;
import com.pphgreen.backend.gallery.service.GalleryLikeService;
import com.pphgreen.backend.gallery.service.GalleryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    private final GalleryService galleryService;
    private final GalleryLikeService likeService;
    private final GalleryCommentService commentService;

    public GalleryController(GalleryService galleryService, GalleryLikeService likeService, GalleryCommentService commentService) {
        this.galleryService = galleryService;
        this.likeService = likeService;
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GalleryResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "eventId", required = false) Long eventId) {
        String email = currentUserEmail();
        if (email == null) throw new com.pphgreen.backend.common.exception.UnauthorizedException("Unauthorized");
        return ResponseEntity.ok(ApiResponse.success("Uploaded", galleryService.upload(file, email, eventId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GalleryResponse>>> getAll(
            @RequestParam(value = "eventId", required = false) Long eventId) {
        String email = currentUserEmailOrNull();
        if (eventId != null) {
            return ResponseEntity.ok(ApiResponse.success(galleryService.getByEvent(eventId, email)));
        }
        return ResponseEntity.ok(ApiResponse.success(galleryService.getAll(email)));
    }

    @GetMapping("/standalone")
    public ResponseEntity<ApiResponse<List<GalleryResponse>>> getStandalone() {
        return ResponseEntity.ok(ApiResponse.success(galleryService.getStandalone(currentUserEmailOrNull())));
    }

    @GetMapping("/albums")
    public ResponseEntity<ApiResponse<List<GalleryAlbumResponse>>> getAlbums() {
        return ResponseEntity.ok(ApiResponse.success(galleryService.getAlbums()));
    }

    @GetMapping("/events/{eventId}/media")
    public ResponseEntity<ApiResponse<List<GalleryResponse>>> getEventMedia(
            @PathVariable Long eventId,
            @RequestParam(value = "sort", required = false, defaultValue = "likes") String sort,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        String email = currentUserEmailOrNull();
        if ("likes".equalsIgnoreCase(sort)) {
            return ResponseEntity.ok(ApiResponse.success(galleryService.getEventMediaSorted(eventId, email, null)));
        }
        return ResponseEntity.ok(ApiResponse.success(galleryService.getByEvent(eventId, email)));
    }

    @GetMapping("/events/{eventId}/highlights")
    public ResponseEntity<ApiResponse<List<GalleryResponse>>> getHighlights(@PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.success(galleryService.getHighlights(eventId, currentUserEmailOrNull())));
    }

    @PutMapping("/{id}/highlight")
    public ResponseEntity<ApiResponse<GalleryResponse>> toggleHighlight(
            @PathVariable Long id,
            @RequestParam(value = "highlight", required = false) Boolean highlight,
            @RequestParam(value = "order", required = false) Integer order) {
        return ResponseEntity.ok(ApiResponse.success(galleryService.toggleHighlight(id, currentUserEmail(), highlight, order)));
    }

    // Photo likes
    @PostMapping("/{id}/likes")
    public ResponseEntity<ApiResponse<Long>> like(@PathVariable Long id) {
        long count = likeService.like(id, currentUserEmail());
        return ResponseEntity.ok(ApiResponse.success("Liked", count));
    }

    @DeleteMapping("/{id}/likes")
    public ResponseEntity<ApiResponse<Long>> unlike(@PathVariable Long id) {
        long count = likeService.unlike(id, currentUserEmail());
        return ResponseEntity.ok(ApiResponse.success("Unliked", count));
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<ApiResponse<Long>> likeCount(@PathVariable Long id) {
        long count = likeService.count(id);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // Photo comments
    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<GalleryCommentResponse>> addComment(
            @PathVariable Long id, @Valid @RequestBody GalleryCommentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Comment added", commentService.addComment(id, request, currentUserEmail())));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<java.util.List<GalleryCommentResponse>>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getComments(id)));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId, currentUserEmail());
        return ResponseEntity.ok(ApiResponse.success("Comment deleted", null));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }

    private String currentUserEmailOrNull() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) return null;
            return authentication.getName();
        } catch (Exception e) {
            return null;
        }
    }
}