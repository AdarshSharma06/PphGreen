package com.pphgreen.backend.developer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pphgreen.backend.developer.dto.DeveloperResponse;
import com.pphgreen.backend.developer.entity.Developer;
import com.pphgreen.backend.developer.repository.DeveloperRepository;
import com.pphgreen.backend.user.entity.AccountStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;

@ExtendWith(MockitoExtension.class)
class DeveloperServiceTest {

    @Mock
    private DeveloperRepository developerRepository;

    private DeveloperService developerService;

    @BeforeEach
    void setUp() {
        developerService = new DeveloperService(developerRepository);
    }

    @Test
    void returnsAllDevelopersMappedToResponse() {
        when(developerRepository.findAllWithCreatedBy()).thenReturn(List.of(sampleDeveloper()));

        List<DeveloperResponse> responses = developerService.getAllDevelopers();

        assertEquals(1, responses.size());
        DeveloperResponse response = responses.get(0);
        assertEquals("Jane Doe", response.name());
        assertEquals("Backend Developer", response.role());
        assertEquals("Builds the API", response.bio());
        assertEquals("https://cdn.example.com/jane.jpg", response.image());
        verify(developerRepository).findAllWithCreatedBy();
    }

    @Test
    void createdBySummaryIsMappedSafely() {
        when(developerRepository.findAllWithCreatedBy()).thenReturn(List.of(sampleDeveloper()));

        List<DeveloperResponse> responses = developerService.getAllDevelopers();

        DeveloperResponse response = responses.get(0);
        assertEquals("Jane Doe", response.createdBy().name());
    }

    @Test
    void emptyResultReturnsEmptyList() {
        when(developerRepository.findAllWithCreatedBy()).thenReturn(List.of());

        List<DeveloperResponse> responses = developerService.getAllDevelopers();

        assertTrue(responses.isEmpty());
    }

    private Developer sampleDeveloper() {
        Developer developer = new Developer();
        developer.setId(1L);
        developer.setName("Jane Doe");
        developer.setRole("Backend Developer");
        developer.setBio("Builds the API");
        developer.setImage("https://cdn.example.com/jane.jpg");
        developer.setCreatedBy(creatorUser());
        return developer;
    }

    private User creatorUser() {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.ADMIN);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setName("Jane Doe");
        return user;
    }
}