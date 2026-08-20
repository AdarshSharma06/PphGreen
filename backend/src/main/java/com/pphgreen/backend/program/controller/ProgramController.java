package com.pphgreen.backend.program.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pphgreen.backend.common.response.ApiResponse;
import com.pphgreen.backend.program.dto.ProgramRequest;
import com.pphgreen.backend.program.dto.ProgramResponse;
import com.pphgreen.backend.program.service.ProgramService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProgramResponse>>> getPrograms() {
        return ResponseEntity.ok(ApiResponse.success(programService.getAllPrograms()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProgramResponse>> getProgram(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(programService.getProgram(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProgramResponse>> createProgram(@Valid @RequestBody ProgramRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Program created", programService.createProgram(request, currentUserEmail())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProgramResponse>> updateProgram(
            @PathVariable Long id, @Valid @RequestBody ProgramRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Program updated", programService.updateProgram(id, request, currentUserEmail())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProgram(@PathVariable Long id) {
        programService.deleteProgram(id, currentUserEmail());
        return ResponseEntity.ok(ApiResponse.success("Program deleted", null));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}