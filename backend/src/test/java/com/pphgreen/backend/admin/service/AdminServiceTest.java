package com.pphgreen.backend.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pphgreen.backend.admin.dto.AdminApprovalRequest;
import com.pphgreen.backend.common.exception.ConflictException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.user.dto.UserProfileResponse;
import com.pphgreen.backend.user.entity.AccountStatus;
import com.pphgreen.backend.user.entity.AdminStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.repository.UserRepository;
import com.pphgreen.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
        adminService = new AdminService(userService);
    }

    @Test
    void getPendingRequestsReturnsOnlyPendingUsers() {
        when(userRepository.findByAdminStatus(AdminStatus.PENDING)).thenReturn(List.of(pendingUser()));

        List<UserProfileResponse> responses = adminService.getPendingRequests();

        assertEquals(1, responses.size());
        assertEquals("Jane Doe", responses.get(0).name());
    }

    @Test
    void approveMakesUserAdmin() {
        User user = pendingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = adminService.approve(1L, new AdminApprovalRequest(null));

        assertEquals(Role.ADMIN, user.getRole());
        assertEquals(AdminStatus.APPROVED, user.getAdminStatus());
        assertEquals("Jane Doe", response.name());
    }

    @Test
    void rejectKeepsMemberRole() {
        User user = pendingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = adminService.reject(1L, new AdminApprovalRequest(null));

        assertEquals(AdminStatus.REJECTED, user.getAdminStatus());
        assertEquals(Role.MEMBER, user.getRole());
        assertEquals("Jane Doe", response.name());
    }

    @Test
    void approveRejectsAlreadyProcessedRequest() {
        User user = pendingUser();
        user.setAdminStatus(AdminStatus.REJECTED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class, () -> adminService.approve(1L, new AdminApprovalRequest(null)));
    }

    @Test
    void approveThrowsWhenUserMissing() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.approve(999L, new AdminApprovalRequest(null)));
    }

    private User pendingUser() {
        User user = new User();
        user.setEmail("adminreq@example.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.MEMBER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setAdminStatus(AdminStatus.PENDING);
        user.setName("Jane Doe");
        return user;
    }
}