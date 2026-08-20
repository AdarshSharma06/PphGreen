package com.pphgreen.backend.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pphgreen.backend.admin.dto.AdminApprovalRequest;
import com.pphgreen.backend.common.exception.ConflictException;
import com.pphgreen.backend.user.dto.UserProfileResponse;
import com.pphgreen.backend.user.entity.AdminStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class AdminService {

    private final UserService userService;

    public AdminService(UserService userService) {
        this.userService = userService;
    }

    public List<UserProfileResponse> getPendingRequests() {
        return userService.findByAdminStatus(AdminStatus.PENDING)
                .stream()
                .map(userService::toProfileResponse)
                .toList();
    }

    public UserProfileResponse approve(Long id, AdminApprovalRequest request) {
        User user = pendingUser(id);
        user.setRole(Role.ADMIN);
        user.setAdminStatus(AdminStatus.APPROVED);
        return userService.toProfileResponse(userService.save(user));
    }

    public UserProfileResponse reject(Long id, AdminApprovalRequest request) {
        User user = pendingUser(id);
        user.setAdminStatus(AdminStatus.REJECTED);
        return userService.toProfileResponse(userService.save(user));
    }

    private User pendingUser(Long id) {
        User user = userService.findById(id);
        if (user.getAdminStatus() != AdminStatus.PENDING) {
            throw new ConflictException("Request is not pending");
        }
        return user;
    }
}