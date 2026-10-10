package com.pphgreen.backend.community.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pphgreen.backend.common.exception.BadRequestException;
import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.community.dto.CommunityPostAuthorSummary;
import com.pphgreen.backend.community.dto.CommunityPostCommentResponse;
import com.pphgreen.backend.community.dto.CommunityPostImageResponse;
import com.pphgreen.backend.community.dto.CommunityPostResponse;
import com.pphgreen.backend.community.entity.CommunityPost;
import com.pphgreen.backend.community.entity.CommunityPostComment;
import com.pphgreen.backend.community.entity.CommunityPostImage;
import com.pphgreen.backend.community.repository.CommunityPostCommentRepository;
import com.pphgreen.backend.community.repository.CommunityPostImageRepository;
import com.pphgreen.backend.community.repository.CommunityPostRepository;
import com.pphgreen.backend.storage.StorageService;
import com.pphgreen.backend.user.dto.UserPublicResponse;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class CommunityService {

    private static final String POST_IMAGE_FOLDER = "community";
    private static final int MAX_IMAGES_PER_POST = 6;
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    private final CommunityPostRepository postRepository;
    private final CommunityPostImageRepository imageRepository;
    private final CommunityPostCommentRepository commentRepository;
    private final UserService userService;
    private final StorageService storageService;
    private final com.pphgreen.backend.user.repository.UserRepository userRepository;

    public CommunityService(CommunityPostRepository postRepository, CommunityPostImageRepository imageRepository,
                            CommunityPostCommentRepository commentRepository, UserService userService,
                            StorageService storageService, com.pphgreen.backend.user.repository.UserRepository userRepository) {
        this.postRepository = postRepository;
        this.imageRepository = imageRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.storageService = storageService;
        this.userRepository = userRepository;
    }

    // Posts — ASC order (oldest→newest), but pagination fetches older via cursor (smallest id)
    public List<CommunityPostResponse> getPosts(Long cursor, Integer limit, String currentUserEmail) {
        int lim = (limit == null || limit <= 0 || limit > 50) ? 20 : limit;
        Pageable pageable = PageRequest.of(0, lim);
        List<CommunityPost> posts;
        if (cursor == null) {
            // latest N, then reverse to ASC for display (oldest at top, newest at bottom)
            List<CommunityPost> latestDesc = postRepository.findLatestOrderByIdDesc(pageable);
            posts = latestDesc.stream().sorted((a, b) -> {
                int cmp = a.getCreatedAt().compareTo(b.getCreatedAt());
                if (cmp != 0) return cmp;
                return a.getId().compareTo(b.getId());
            }).collect(Collectors.toList());
        } else {
            // older than cursor, DESC then reverse to ASC for prepend
            List<CommunityPost> olderDesc = postRepository.findByIdLessThanOrderByIdDesc(cursor, pageable);
            posts = olderDesc.stream().sorted((a, b) -> {
                int cmp = a.getCreatedAt().compareTo(b.getCreatedAt());
                if (cmp != 0) return cmp;
                return a.getId().compareTo(b.getId());
            }).collect(Collectors.toList());
        }
        return posts.stream().map(this::toPostResponse).collect(Collectors.toList());
    }

    @Transactional
    public CommunityPostResponse createPost(String content, List<MultipartFile> images, String authorEmail) {
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Post content is required");
        }
        if (content.length() > 5000) throw new BadRequestException("Content too long");
        if (images != null && images.size() > MAX_IMAGES_PER_POST) {
            throw new BadRequestException("Maximum " + MAX_IMAGES_PER_POST + " images per post");
        }
        User author = userService.findByEmail(authorEmail).orElseThrow(() -> new UnauthorizedException("User not found"));
        CommunityPost post = new CommunityPost();
        post.setAuthor(author);
        post.setContent(content.trim());
        CommunityPost saved = postRepository.save(post);

        if (images != null) {
            int order = 0;
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) continue;
                validateImage(file);
                String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
                String sanitized = original.replaceAll("[^a-zA-Z0-9._-]", "_");
                String path = POST_IMAGE_FOLDER + "/" + saved.getId() + "/" + UUID.randomUUID() + "-" + sanitized;
                String ct = file.getContentType() != null ? file.getContentType() : "image/jpeg";
                try {
                    String url = storageService.upload(file.getBytes(), ct, path);
                    CommunityPostImage img = new CommunityPostImage();
                    img.setPost(saved);
                    img.setStoragePath(path);
                    img.setFileUrl(url);
                    img.setDisplayOrder(order++);
                    imageRepository.save(img);
                } catch (IOException e) {
                    throw new BadRequestException("Failed to read image");
                }
            }
        }
        // Return freshly loaded with images
        return toPostResponse(postRepository.findWithAuthorById(saved.getId()).orElse(saved));
    }

    public List<CommunityPostCommentResponse> getComments(Long postId) {
        if (!postRepository.existsById(postId)) throw new ResourceNotFoundException("Post", postId);
        return commentRepository.findByPostIdWithAuthor(postId).stream().map(this::toCommentResponse).collect(Collectors.toList());
    }

    @Transactional
    public CommunityPostCommentResponse addComment(Long postId, String content, String authorEmail) {
        if (content == null || content.trim().isEmpty()) throw new BadRequestException("Comment is required");
        if (content.length() > 2000) throw new BadRequestException("Comment too long");
        User author = userService.findByEmail(authorEmail).orElseThrow(() -> new UnauthorizedException("User not found"));
        CommunityPost post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", postId));
        CommunityPostComment comment = new CommunityPostComment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(content.trim());
        CommunityPostComment saved = commentRepository.save(comment);
        return toCommentResponse(commentRepository.findWithAuthorById(saved.getId()).orElse(saved));
    }

    public void deleteComment(Long commentId, String actorEmail) {
        CommunityPostComment comment = commentRepository.findWithAuthorById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
        User actor = userService.findByEmail(actorEmail).orElseThrow(() -> new UnauthorizedException("User not found"));
        boolean isAuthor = comment.getAuthor().getId().equals(actor.getId());
        if (!isAuthor && !actor.getRole().name().equals("ADMIN")) {
            throw new ForbiddenException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

    public List<UserPublicResponse> searchMembers(String q, Long cursor, Integer limit, String currentUserEmail) {
        User current = userService.findByEmail(currentUserEmail).orElseThrow(() -> new UnauthorizedException("User not found"));
        int lim = (limit == null || limit <= 0 || limit > 50) ? 20 : limit;
        Pageable pageable = PageRequest.of(0, lim);
        List<User> users;
        if (q == null || q.trim().isEmpty()) {
            users = userRepository.findAllExcluding(current.getId(), pageable);
        } else {
            users = userRepository.searchByNameOrFlat(q.trim(), current.getId(), pageable);
        }
        // cursor is id-based for pagination: filter id > cursor? For member directory simple limit, cursor as last id not needed for now
        // But spec says cursor-based; we implement id > cursor for next page if cursor provided
        if (cursor != null) {
            users = users.stream().filter(u -> u.getId() > cursor).collect(Collectors.toList());
        }
        return users.stream().map(u -> new UserPublicResponse(u.getId(), u.getName(), u.getTower(), u.getApartmentNumber(), u.getProfilePicture())).collect(Collectors.toList());
    }

    private void validateImage(MultipartFile file) {
        if (file.getSize() > MAX_IMAGE_SIZE) throw new BadRequestException("Image too large (max 10MB)");
        String ct = file.getContentType();
        if (ct == null || !(ct.startsWith("image/"))) {
            throw new BadRequestException("Only image files are allowed");
        }
    }

    private CommunityPostResponse toPostResponse(CommunityPost post) {
        User author = post.getAuthor();
        String displayName = author.getName() != null && !author.getName().isBlank() ? author.getName() : author.getEmail();
        CommunityPostAuthorSummary authorSummary = new CommunityPostAuthorSummary(author.getId(), displayName, author.getProfilePicture());
        List<CommunityPostImageResponse> images = imageRepository.findByPostIdOrderByDisplayOrderAsc(post.getId()).stream()
                .map(img -> new CommunityPostImageResponse(img.getId(), img.getFileUrl(), img.getStoragePath(), img.getDisplayOrder()))
                .collect(Collectors.toList());
        long commentCount = commentRepository.countByPostId(post.getId());
        return new CommunityPostResponse(post.getId(), post.getContent(), authorSummary, post.getCreatedAt(), post.getUpdatedAt(), images, commentCount);
    }

    private CommunityPostCommentResponse toCommentResponse(CommunityPostComment c) {
        User author = c.getAuthor();
        String displayName = author.getName() != null && !author.getName().isBlank() ? author.getName() : author.getEmail();
        CommunityPostAuthorSummary authorSummary = new CommunityPostAuthorSummary(author.getId(), displayName, author.getProfilePicture());
        return new CommunityPostCommentResponse(c.getId(), c.getPost().getId(), c.getContent(), authorSummary, c.getCreatedAt(), c.getUpdatedAt());
    }
}
