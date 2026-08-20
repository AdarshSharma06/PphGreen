package com.pphgreen.backend.about.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.pphgreen.backend.about.dto.AboutRequest;
import com.pphgreen.backend.about.dto.AboutResponse;
import com.pphgreen.backend.about.service.AboutService;
import com.pphgreen.backend.common.response.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/about")
public class AboutController {

    private final AboutService aboutService;

    public AboutController(AboutService aboutService) {
        this.aboutService = aboutService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AboutResponse>>> getAbout() {
        return ResponseEntity.ok(ApiResponse.success(aboutService.getAllAbout()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AboutResponse>> getAbout(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(aboutService.getAbout(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AboutResponse>> createAbout(@Valid @RequestBody AboutRequest request) {
        return ResponseEntity.ok(ApiResponse.success("About content created", aboutService.createAbout(request, currentUserEmail())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AboutResponse>> updateAbout(
            @PathVariable Long id, @Valid @RequestBody AboutRequest request) {
        return ResponseEntity.ok(ApiResponse.success("About content updated", aboutService.updateAbout(id, request, currentUserEmail())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAbout(@PathVariable Long id) {
        aboutService.deleteAbout(id, currentUserEmail());
        return ResponseEntity.ok(ApiResponse.success("About content deleted", null));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}