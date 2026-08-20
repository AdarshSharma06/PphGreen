package com.pphgreen.backend.user.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.user.dto.UpdateProfileRequest;
import com.pphgreen.backend.user.dto.UserProfileResponse;
import com.pphgreen.backend.user.entity.AdminStatus;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(normalizeEmail(email));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public List<User> findByAdminStatus(AdminStatus adminStatus) {
        return userRepository.findByAdminStatus(adminStatus);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public UserProfileResponse getCurrentUserProfile(String email) {
        return toProfileResponse(getUserByEmail(email));
    }

    public UserProfileResponse getPublicProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toProfileResponse(user);
    }

    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getUserByEmail(email);

        if (request.name() != null) {
            user.setName(cleanValue(request.name()));
        }
        if (request.tower() != null) {
            user.setTower(cleanValue(request.tower()));
        }
        if (request.apartmentNumber() != null) {
            user.setApartmentNumber(cleanValue(request.apartmentNumber()));
        }
        if (request.profilePicture() != null) {
            user.setProfilePicture(cleanValue(request.profilePicture()));
        }

        return toProfileResponse(userRepository.save(user));
    }

    public UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getName(), user.getTower(),
                user.getApartmentNumber(), user.getProfilePicture());
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private String cleanValue(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}