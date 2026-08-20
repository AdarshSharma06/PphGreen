package com.pphgreen.backend.user.service;

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

import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.user.dto.UpdateProfileRequest;
import com.pphgreen.backend.user.dto.UserProfileResponse;
import com.pphgreen.backend.user.dto.UserPublicResponse;
import com.pphgreen.backend.user.entity.AccountStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void getCurrentUserProfileReturnsSafeDto() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser()));

        UserProfileResponse response = userService.getCurrentUserProfile("test@example.com");

        assertEquals("test@example.com", response.email());
        assertEquals("Jane Doe", response.name());
        assertEquals("Tower A", response.tower());
        assertEquals("101", response.apartmentNumber());
        assertEquals("https://cdn.example.com/pic.jpg", response.profilePicture());
    }

    @Test
    void getPublicProfileReturnsSafeDtoWithoutEmail() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(sampleUser()));

        UserPublicResponse response = userService.getPublicProfile(5L);

        assertEquals("Jane Doe", response.name());
        assertEquals("Tower A", response.tower());
        assertEquals("101", response.apartmentNumber());
        assertEquals("https://cdn.example.com/pic.jpg", response.profilePicture());
    }

    @Test
    void getPublicProfilesReturnsSafePeopleList() {
        User second = sampleUser();
        second.setName("John Smith");
        when(userRepository.findAll()).thenReturn(List.of(sampleUser(), second));

        List<UserPublicResponse> responses = userService.getPublicProfiles();

        assertEquals(2, responses.size());
        assertEquals("Jane Doe", responses.get(0).name());
        assertEquals("John Smith", responses.get(1).name());
        assertEquals("Tower A", responses.get(0).tower());
    }

    @Test
    void getPublicProfileThrowsWhenUserMissing() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getPublicProfile(999L));
    }

    @Test
    void updateProfileOnlyChangesAllowedFields() {
        User user = sampleUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = userService.updateProfile("test@example.com",
                new UpdateProfileRequest("New Name", "Tower B", "202", "https://cdn.example.com/new.jpg"));

        assertEquals("New Name", user.getName());
        assertEquals("Tower B", user.getTower());
        assertEquals("202", user.getApartmentNumber());
        assertEquals("https://cdn.example.com/new.jpg", user.getProfilePicture());
        assertEquals("New Name", response.name());

        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashed", user.getPasswordHash());
        assertEquals(Role.MEMBER, user.getRole());
        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
    }

    @Test
    void updateProfileIgnoresNullFields() {
        User user = sampleUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateProfile("test@example.com", new UpdateProfileRequest(null, null, null, null));

        assertEquals("Jane Doe", user.getName());
        assertEquals("Tower A", user.getTower());
        assertEquals("101", user.getApartmentNumber());
    }

    @Test
    void updateProfileThrowsWhenUserMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateProfile("missing@example.com", new UpdateProfileRequest("X", null, null, null)));
    }

    private User sampleUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.MEMBER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setName("Jane Doe");
        user.setTower("Tower A");
        user.setApartmentNumber("101");
        user.setProfilePicture("https://cdn.example.com/pic.jpg");
        return user;
    }
}