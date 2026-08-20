package com.pphgreen.backend.gallery.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pphgreen.backend.common.response.ApiResponse;
import com.pphgreen.backend.gallery.dto.GalleryResponse;
import com.pphgreen.backend.gallery.service.GalleryService;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    private final GalleryService galleryService;

    public GalleryController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GalleryResponse>> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Uploaded", galleryService.upload(file, currentUserEmail())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GalleryResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(galleryService.getAll()));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}