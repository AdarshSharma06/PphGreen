package com.pphgreen.backend.about.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pphgreen.backend.about.dto.AboutRequest;
import com.pphgreen.backend.about.dto.AboutResponse;
import com.pphgreen.backend.about.entity.About;
import com.pphgreen.backend.about.repository.AboutRepository;
import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.user.entity.AccountStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AboutServiceTest {

    @Mock
    private AboutRepository aboutRepository;

    @Mock
    private UserService userService;

    private AboutService aboutService;

    @BeforeEach
    void setUp() {
        aboutService = new AboutService(aboutRepository, userService);
    }

    @Test
    void createAboutByAdminUsesAuthenticatedUserAsCreator() {
        User admin = adminUser();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(aboutRepository.save(any(About.class))).thenAnswer(invocation -> {
            About a = invocation.getArgument(0);
            a.setId(10L);
            return a;
        });
        when(aboutRepository.findWithCreatedBy(10L))
                .thenAnswer(invocation -> Optional.of(aboutRepositoryCaptured()));

        AboutResponse response = aboutService.createAbout(request(), "admin@example.com");

        About saved = aboutRepositoryCaptured();
        assertEquals("admin@example.com", saved.getCreatedBy().getEmail());
        assertEquals("Jane Doe", response.createdBy().name());

        assertEquals("Who We Are", saved.getTitle());
        assertEquals("Organization information", saved.getDescription());
        assertEquals("Our ideals", saved.getIdeals());
        assertEquals("500 members, 120 events", saved.getImpactMetrics());
        assertEquals("https://cdn.example.com/about.jpg", saved.getImage());
    }

    @Test
    void getAllAboutReturnsSavedContent() {
        when(aboutRepository.findAllWithCreatedBy()).thenReturn(List.of(sampleAbout(), sampleAbout()));

        List<AboutResponse> responses = aboutService.getAllAbout();

        assertEquals(2, responses.size());
        assertEquals("Who We Are", responses.get(0).title());
        assertEquals("Jane Doe", responses.get(0).createdBy().name());
    }

    @Test
    void getAboutByIdReturnsContent() {
        when(aboutRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(sampleAbout()));

        AboutResponse response = aboutService.getAbout(1L);

        assertEquals("Who We Are", response.title());
        assertEquals("500 members, 120 events", response.impactMetrics());
        assertEquals("Jane Doe", response.createdBy().name());
    }

    @Test
    void updateAboutByAdminUpdatesFields() {
        User admin = adminUser();
        About about = sampleAbout();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(aboutRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(about));
        when(aboutRepository.save(any(About.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AboutRequest update = new AboutRequest("Updated Title", "Updated description", "Updated ideals", "10 partners", null);

        AboutResponse response = aboutService.updateAbout(1L, update, "admin@example.com");

        assertEquals("Updated Title", about.getTitle());
        assertEquals("Updated description", about.getDescription());
        assertEquals("Updated ideals", about.getIdeals());
        assertEquals("10 partners", about.getImpactMetrics());
        assertEquals(null, about.getImage());
        assertEquals("Updated Title", response.title());
    }

    @Test
    void deleteAboutByAdminDeletesContent() {
        User admin = adminUser();
        About about = sampleAbout();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(aboutRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(about));

        aboutService.deleteAbout(1L, "admin@example.com");

        verify(aboutRepository).delete(about);
    }

    @Test
    void memberCannotCreateAbout() {
        User member = memberUser();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));

        assertThrows(ForbiddenException.class, () -> aboutService.createAbout(request(), "member@example.com"));
        verify(aboutRepository, never()).save(any(About.class));
    }

    @Test
    void memberCannotUpdateAbout() {
        User member = memberUser();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));

        assertThrows(ForbiddenException.class, () -> aboutService.updateAbout(1L, request(), "member@example.com"));
        verify(aboutRepository, never()).save(any(About.class));
    }

    @Test
    void memberCannotDeleteAbout() {
        User member = memberUser();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));

        assertThrows(ForbiddenException.class, () -> aboutService.deleteAbout(1L, "member@example.com"));
        verify(aboutRepository, never()).delete(any(About.class));
    }

    @Test
    void getMissingAboutThrowsResourceNotFound() {
        when(aboutRepository.findWithCreatedBy(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> aboutService.getAbout(999L));

        assertEquals("About not found with id: 999", ex.getMessage());
    }

    @Test
    void updateMissingAboutThrowsResourceNotFound() {
        User admin = adminUser();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(aboutRepository.findWithCreatedBy(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> aboutService.updateAbout(999L, request(), "admin@example.com"));
    }

    @Test
    void deleteMissingAboutThrowsResourceNotFound() {
        User admin = adminUser();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(aboutRepository.findWithCreatedBy(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> aboutService.deleteAbout(999L, "admin@example.com"));
    }

    private About aboutRepositoryCaptured() {
        ArgumentCaptor<About> captor = ArgumentCaptor.forClass(About.class);
        verify(aboutRepository).save(captor.capture());
        return captor.getValue();
    }

    private AboutRequest request() {
        return new AboutRequest("Who We Are", "Organization information", "Our ideals", "500 members, 120 events",
                "https://cdn.example.com/about.jpg");
    }

    private About sampleAbout() {
        About about = new About();
        about.setId(1L);
        about.setTitle("Who We Are");
        about.setDescription("Organization information");
        about.setIdeals("Our ideals");
        about.setImpactMetrics("500 members, 120 events");
        about.setImage("https://cdn.example.com/about.jpg");
        about.setCreatedBy(adminUser());
        return about;
    }

    private User adminUser() {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.ADMIN);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setName("Jane Doe");
        return user;
    }

    private User memberUser() {
        User user = new User();
        user.setEmail("member@example.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.MEMBER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setName("John Smith");
        return user;
    }
}