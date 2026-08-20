package com.pphgreen.backend.developer.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pphgreen.backend.common.response.ApiResponse;
import com.pphgreen.backend.developer.dto.DeveloperResponse;
import com.pphgreen.backend.developer.service.DeveloperService;

@RestController
@RequestMapping("/api/developer")
public class DeveloperController {

    private final DeveloperService developerService;

    public DeveloperController(DeveloperService developerService) {
        this.developerService = developerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeveloperResponse>>> getDevelopers() {
        return ResponseEntity.ok(ApiResponse.success(developerService.getAllDevelopers()));
    }
}