package com.pphgreen.backend.suggestion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.suggestion.dto.SuggestionRequest;
import com.pphgreen.backend.suggestion.dto.SuggestionResponse;
import com.pphgreen.backend.suggestion.entity.Suggestion;
import com.pphgreen.backend.suggestion.repository.SuggestionRepository;
import com.pphgreen.backend.user.entity.AccountStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock
    private SuggestionRepository suggestionRepository;

    @Mock
    private UserService userService;

    private SuggestionService suggestionService;

    @BeforeEach
    void setUp() {
        suggestionService = new SuggestionService(suggestionRepository, userService);
    }

    @Test
    void memberCanCreateSuggestion() {
        User member = memberUser();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(suggestionRepository.save(any(Suggestion.class))).thenAnswer(invocation -> {
            Suggestion s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });
        when(suggestionRepository.findWithSubmitter(10L))
                .thenAnswer(invocation -> Optional.of(suggestionRepositoryCaptured()));

        SuggestionResponse response = suggestionService.createSuggestion(request(), "member@example.com");

        Suggestion saved = suggestionRepositoryCaptured();
        assertEquals("Add dark mode", saved.getContent());
        assertEquals("member@example.com", saved.getSubmitter().getEmail());
        assertEquals("John Smith", response.submittedBy().name());
    }

    @Test
    void adminCanCreateSuggestion() {
        User admin = adminUser();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(suggestionRepository.save(any(Suggestion.class))).thenAnswer(invocation -> {
            Suggestion s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });
        when(suggestionRepository.findWithSubmitter(10L))
                .thenAnswer(invocation -> Optional.of(suggestionRepositoryCaptured()));

        SuggestionResponse response = suggestionService.createSuggestion(request(), "admin@example.com");

        assertEquals("Add dark mode", response.content());
        assertEquals("Jane Doe", response.submittedBy().name());
    }

    @Test
    void createUsesAuthenticatedUserNotClientInput() {
        User member = memberUser();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(suggestionRepository.save(any(Suggestion.class))).thenAnswer(invocation -> {
            Suggestion s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });
        when(suggestionRepository.findWithSubmitter(10L))
                .thenAnswer(invocation -> Optional.of(suggestionRepositoryCaptured()));

        suggestionService.createSuggestion(request(), "member@example.com");

        Suggestion saved = suggestionRepositoryCaptured();
        assertEquals("member@example.com", saved.getSubmitter().getEmail());
    }

    @Test
    void adminCanRetrieveSuggestions() {
        User admin = adminUser();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(suggestionRepository.findAllWithSubmitter())
                .thenReturn(List.of(sampleSuggestion(1L, memberUser()), sampleSuggestion(2L, memberUser())));

        List<SuggestionResponse> responses = suggestionService.getAllSuggestions("admin@example.com");

        assertEquals(2, responses.size());
        assertEquals("Add dark mode", responses.get(0).content());
        assertEquals("John Smith", responses.get(0).submittedBy().name());
    }

    @Test
    void suggestionsReturnedInExpectedOrder() {
        User admin = adminUser();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        Suggestion newest = sampleSuggestion(2L, memberUser());
        Suggestion oldest = sampleSuggestion(1L, memberUser());
        when(suggestionRepository.findAllWithSubmitter()).thenReturn(List.of(newest, oldest));

        List<SuggestionResponse> responses = suggestionService.getAllSuggestions("admin@example.com");

        assertEquals(2L, responses.get(0).id());
        assertEquals(1L, responses.get(1).id());
    }

    @Test
    void memberCannotRetrieveSuggestions() {
        User member = memberUser();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));

        assertThrows(ForbiddenException.class, () -> suggestionService.getAllSuggestions("member@example.com"));
        verify(suggestionRepository, never()).findAllWithSubmitter();
    }

    @Test
    void adminCanUpdateSuggestion() {
        User admin = adminUser();
        Suggestion suggestion = sampleSuggestion(1L, memberUser());
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(suggestionRepository.findWithSubmitter(1L)).thenReturn(Optional.of(suggestion));
        when(suggestionRepository.save(any(Suggestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SuggestionResponse response = suggestionService.updateSuggestion(1L, new SuggestionRequest("Updated content"), "admin@example.com");

        assertEquals("Updated content", suggestion.getContent());
        assertEquals("Updated content", response.content());
        assertEquals("member@example.com", suggestion.getSubmitter().getEmail());
    }

    @Test
    void memberCannotUpdateSuggestion() {
        User member = memberUser();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));

        assertThrows(ForbiddenException.class,
                () -> suggestionService.updateSuggestion(1L, request(), "member@example.com"));
        verify(suggestionRepository, never()).save(any(Suggestion.class));
    }

    @Test
    void updateNonexistentSuggestionThrowsResourceNotFound() {
        User admin = adminUser();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(suggestionRepository.findWithSubmitter(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> suggestionService.updateSuggestion(999L, request(), "admin@example.com"));

        assertEquals("Suggestion not found with id: 999", ex.getMessage());
    }

    @Test
    void createWithUnknownUserThrowsUnauthorized() {
        when(userService.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> suggestionService.createSuggestion(request(), "ghost@example.com"));
        verify(suggestionRepository, never()).save(any(Suggestion.class));
    }

    @Test
    void requestDtoValidatesRequiredContent() throws Exception {
        java.lang.reflect.Field contentField = SuggestionRequest.class.getDeclaredField("content");
        boolean hasNotBlank = Arrays.stream(contentField.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().equals(NotBlank.class));
        boolean hasSize = Arrays.stream(contentField.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().equals(Size.class));
        assertTrue(hasNotBlank);
        assertTrue(hasSize);
    }

    private Suggestion suggestionRepositoryCaptured() {
        ArgumentCaptor<Suggestion> captor = ArgumentCaptor.forClass(Suggestion.class);
        verify(suggestionRepository).save(captor.capture());
        return captor.getValue();
    }

    private SuggestionRequest request() {
        return new SuggestionRequest("Add dark mode");
    }

    private Suggestion sampleSuggestion(Long id, User submitter) {
        Suggestion suggestion = new Suggestion();
        suggestion.setId(id);
        suggestion.setContent("Add dark mode");
        suggestion.setSubmitter(submitter);
        return suggestion;
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