package com.pphgreen.backend.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pphgreen.backend.admin.dto.AdminApprovalRequest;
import com.pphgreen.backend.admin.service.AdminService;
import com.pphgreen.backend.common.response.ApiResponse;
import com.pphgreen.backend.user.dto.UserProfileResponse;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getPendingRequests() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingRequests()));
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> approve(
            @PathVariable Long id,
            @RequestBody(required = false) AdminApprovalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Admin request approved", adminService.approve(id, request)));
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) AdminApprovalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Admin request rejected", adminService.reject(id, request)));
    }
}