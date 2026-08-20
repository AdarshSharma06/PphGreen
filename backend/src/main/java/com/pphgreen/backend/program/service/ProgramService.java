package com.pphgreen.backend.program.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.program.dto.ProgramRequest;
import com.pphgreen.backend.program.dto.ProgramResponse;
import com.pphgreen.backend.program.dto.ProgramUserSummary;
import com.pphgreen.backend.program.entity.Program;
import com.pphgreen.backend.program.repository.ProgramRepository;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;
    private final UserService userService;

    public ProgramService(ProgramRepository programRepository, UserService userService) {
        this.programRepository = programRepository;
        this.userService = userService;
    }

    public List<ProgramResponse> getAllPrograms() {
        return programRepository.findAllWithCreatedBy()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProgramResponse getProgram(Long id) {
        return toResponse(findProgram(id));
    }

    public ProgramResponse createProgram(ProgramRequest request, String creatorEmail) {
        User creator = adminUser(creatorEmail);
        Program program = new Program();
        applyRequest(program, request);
        program.setCreatedBy(creator);
        Program saved = programRepository.save(program);
        return toResponse(findProgram(saved.getId()));
    }

    public ProgramResponse updateProgram(Long id, ProgramRequest request, String actorEmail) {
        adminUser(actorEmail);
        Program program = findProgram(id);
        applyRequest(program, request);
        programRepository.save(program);
        return toResponse(findProgram(id));
    }

    public void deleteProgram(Long id, String actorEmail) {
        adminUser(actorEmail);
        programRepository.delete(findProgram(id));
    }

    private Program findProgram(Long id) {
        return programRepository.findWithCreatedBy(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program", id));
    }

    private User adminUser(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can manage programs");
        }
        return user;
    }

    private void applyRequest(Program program, ProgramRequest request) {
        program.setTitle(request.title());
        program.setDescription(request.description());
        program.setImage(request.image());
    }

    private ProgramResponse toResponse(Program program) {
        User creator = program.getCreatedBy();
        return new ProgramResponse(program.getId(), program.getTitle(), program.getDescription(), program.getImage(),
                new ProgramUserSummary(creator.getId(), creator.getName()),
                program.getCreatedAt(), program.getUpdatedAt());
    }
}