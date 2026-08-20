package com.pphgreen.backend.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pphgreen.backend.auth.dto.AuthResponse;
import com.pphgreen.backend.auth.dto.LoginRequest;
import com.pphgreen.backend.auth.dto.RegisterRequest;
import com.pphgreen.backend.common.exception.EmailAlreadyExistsException;
import com.pphgreen.backend.security.JwtService;
import com.pphgreen.backend.user.entity.AccountStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userService, passwordEncoder, jwtService);
    }

    @Test
    void registerNormalizesEmailAndCreatesMemberActiveUser() {
        when(userService.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPassword123")).thenReturn("hashed");
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("jwt");

        AuthResponse response = authService.register(new RegisterRequest("  TEST@EXAMPLE.COM ", "StrongPassword123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("test@example.com", saved.getEmail());
        assertEquals("hashed", saved.getPasswordHash());
        assertEquals(Role.MEMBER, saved.getRole());
        assertEquals(AccountStatus.ACTIVE, saved.getAccountStatus());
        assertEquals("jwt", response.token());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userService.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register(new RegisterRequest("test@example.com", "StrongPassword123")));
    }

    @Test
    void loginSucceedsWithValidCredentials() {
        User user = userWith("test@example.com", "hashed", Role.MEMBER, AccountStatus.ACTIVE);
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("StrongPassword123", "hashed")).thenReturn(true);
        when(jwtService.generateToken("test@example.com", "MEMBER")).thenReturn("jwt");

        AuthResponse response = authService.login(new LoginRequest("TEST@EXAMPLE.COM ", "StrongPassword123"));

        assertEquals("jwt", response.token());
        assertEquals("test@example.com", response.email());
        assertEquals("MEMBER", response.role());
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = userWith("test@example.com", "hashed", Role.MEMBER, AccountStatus.ACTIVE);
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("test@example.com", "wrong")));
    }

    @Test
    void loginRejectsUnknownEmail() {
        when(userService.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("missing@example.com", "whatever")));
    }

    @Test
    void loginRejectsSuspendedAccount() {
        User user = userWith("test@example.com", "hashed", Role.MEMBER, AccountStatus.SUSPENDED);
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("test@example.com", "StrongPassword123")));
    }

    private User userWith(String email, String hash, Role role, AccountStatus status) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hash);
        user.setRole(role);
        user.setAccountStatus(status);
        return user;
    }
}