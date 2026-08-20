package com.pphgreen.backend.developer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pphgreen.backend.developer.dto.DeveloperResponse;
import com.pphgreen.backend.developer.dto.DeveloperUserSummary;
import com.pphgreen.backend.developer.entity.Developer;
import com.pphgreen.backend.developer.repository.DeveloperRepository;
import com.pphgreen.backend.user.entity.User;

@Service
public class DeveloperService {

    private final DeveloperRepository developerRepository;

    public DeveloperService(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    public List<DeveloperResponse> getAllDevelopers() {
        return developerRepository.findAllWithCreatedBy()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DeveloperResponse toResponse(Developer developer) {
        User creator = developer.getCreatedBy();
        return new DeveloperResponse(developer.getId(), developer.getName(), developer.getRole(), developer.getBio(),
                developer.getImage(),
                new DeveloperUserSummary(creator.getId(), creator.getName()),
                developer.getCreatedAt(), developer.getUpdatedAt());
    }
}