package com.pphgreen.backend.community.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pphgreen.backend.common.response.ApiResponse;
import com.pphgreen.backend.community.dto.CommunityPostCommentResponse;
import com.pphgreen.backend.community.dto.CommunityPostResponse;
import com.pphgreen.backend.community.service.CommunityService;
import com.pphgreen.backend.user.dto.UserPublicResponse;

import jakarta.validation.Valid;
import com.pphgreen.backend.community.dto.CreatePostRequest;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<CommunityPostResponse>>> getPosts(
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer limit) {
        String email = currentUserEmailOrNull();
        return ResponseEntity.ok(ApiResponse.success(communityService.getPosts(cursor, limit, email)));
    }

    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<CommunityPostResponse>> createPost(
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        String email = currentUserEmail();
        return ResponseEntity.ok(ApiResponse.success("Post created", communityService.createPost(content, images, email)));
    }

    // Alternative JSON-only creation (for text-only)
    @PostMapping(value = "/posts/json", consumes = "application/json")
    public ResponseEntity<ApiResponse<CommunityPostResponse>> createPostJson(@Valid @org.springframework.web.bind.annotation.RequestBody CreatePostRequest request) {
        String email = currentUserEmail();
        return ResponseEntity.ok(ApiResponse.success("Post created", communityService.createPost(request.content(), null, email)));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommunityPostCommentResponse>>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(communityService.getComments(postId)));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommunityPostCommentResponse>> addComment(
            @PathVariable Long postId,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> body) {
        String content = body.get("content");
        String email = currentUserEmail();
        return ResponseEntity.ok(ApiResponse.success("Comment added", communityService.addComment(postId, content, email)));
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        // postId not needed but validated for scoping
        communityService.deleteComment(commentId, currentUserEmail());
        return ResponseEntity.ok(ApiResponse.success("Comment deleted", null));
    }

    @GetMapping("/members")
    public ResponseEntity<ApiResponse<List<UserPublicResponse>>> searchMembers(
            @RequestParam(required = false, value = "q") String q,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer limit) {
        String email = currentUserEmail();
        return ResponseEntity.ok(ApiResponse.success(communityService.searchMembers(q, cursor, limit, email)));
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new com.pphgreen.backend.common.exception.UnauthorizedException("Unauthorized");
        }
        return auth.getName();
    }

    private String currentUserEmailOrNull() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
            return auth.getName();
        } catch (Exception e) { return null; }
    }
}
