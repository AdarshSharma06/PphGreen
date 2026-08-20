package com.pphgreen.backend.program.service;

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

import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.program.dto.ProgramRequest;
import com.pphgreen.backend.program.dto.ProgramResponse;
import com.pphgreen.backend.program.entity.Program;
import com.pphgreen.backend.program.repository.ProgramRepository;
import com.pphgreen.backend.user.entity.AccountStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class ProgramServiceTest {

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private UserService userService;

    private ProgramService programService;

    @BeforeEach
    void setUp() {
        programService = new ProgramService(programRepository, userService);
    }

    @Test
    void createProgramByAdminUsesAuthenticatedUserAsCreator() {
        User admin = adminUser();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(programRepository.save(any(Program.class))).thenAnswer(invocation -> {
            Program p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });
        when(programRepository.findWithCreatedBy(10L))
                .thenAnswer(invocation -> Optional.of(programRepositoryCaptured()));

        ProgramResponse response = programService.createProgram(request(), "admin@example.com");

        Program saved = programRepositoryCaptured();
        assertEquals("admin@example.com", saved.getCreatedBy().getEmail());
        assertEquals("Jane Doe", response.createdBy().name());

        assertEquals("Green Initiative", saved.getTitle());
        assertEquals("Program description", saved.getDescription());
        assertEquals("https://cdn.example.com/program.jpg", saved.getImage());
    }

    @Test
    void getAllProgramsReturnsSavedMetadata() {
        when(programRepository.findAllWithCreatedBy()).thenReturn(List.of(sampleProgram(), sampleProgram()));

        List<ProgramResponse> responses = programService.getAllPrograms();

        assertEquals(2, responses.size());
        assertEquals("Green Initiative", responses.get(0).title());
        assertEquals("Jane Doe", responses.get(0).createdBy().name());
    }

    @Test
    void getProgramByIdReturnsProgram() {
        when(programRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(sampleProgram()));

        ProgramResponse response = programService.getProgram(1L);

        assertEquals("Green Initiative", response.title());
        assertEquals("Jane Doe", response.createdBy().name());
    }

    @Test
    void updateProgramByAdminUpdatesFields() {
        User admin = adminUser();
        Program program = sampleProgram();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(programRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(program));
        when(programRepository.save(any(Program.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProgramRequest update = new ProgramRequest("Updated Program", "Updated description", null);

        ProgramResponse response = programService.updateProgram(1L, update, "admin@example.com");

        assertEquals("Updated Program", program.getTitle());
        assertEquals("Updated description", program.getDescription());
        assertEquals(null, program.getImage());
        assertEquals("Updated Program", response.title());
    }

    @Test
    void deleteProgramByAdminDeletesProgram() {
        User admin = adminUser();
        Program program = sampleProgram();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(programRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(program));

        programService.deleteProgram(1L, "admin@example.com");

        verify(programRepository).delete(program);
    }

    @Test
    void memberCannotCreateProgram() {
        User member = memberUser();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));

        assertThrows(ForbiddenException.class, () -> programService.createProgram(request(), "member@example.com"));
        verify(programRepository, never()).save(any(Program.class));
    }

    @Test
    void memberCannotUpdateProgram() {
        User member = memberUser();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));

        assertThrows(ForbiddenException.class, () -> programService.updateProgram(1L, request(), "member@example.com"));
        verify(programRepository, never()).save(any(Program.class));
    }

    @Test
    void memberCannotDeleteProgram() {
        User member = memberUser();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));

        assertThrows(ForbiddenException.class, () -> programService.deleteProgram(1L, "member@example.com"));
        verify(programRepository, never()).delete(any(Program.class));
    }

    @Test
    void getMissingProgramThrowsResourceNotFound() {
        when(programRepository.findWithCreatedBy(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> programService.getProgram(999L));

        assertEquals("Program not found with id: 999", ex.getMessage());
    }

    @Test
    void updateMissingProgramThrowsResourceNotFound() {
        User admin = adminUser();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(programRepository.findWithCreatedBy(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> programService.updateProgram(999L, request(), "admin@example.com"));
    }

    @Test
    void deleteMissingProgramThrowsResourceNotFound() {
        User admin = adminUser();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(programRepository.findWithCreatedBy(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> programService.deleteProgram(999L, "admin@example.com"));
    }

    private Program programRepositoryCaptured() {
        ArgumentCaptor<Program> captor = ArgumentCaptor.forClass(Program.class);
        verify(programRepository).save(captor.capture());
        return captor.getValue();
    }

    private ProgramRequest request() {
        return new ProgramRequest("Green Initiative", "Program description", "https://cdn.example.com/program.jpg");
    }

    private Program sampleProgram() {
        Program program = new Program();
        program.setId(1L);
        program.setTitle("Green Initiative");
        program.setDescription("Program description");
        program.setImage("https://cdn.example.com/program.jpg");
        program.setCreatedBy(adminUser());
        return program;
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