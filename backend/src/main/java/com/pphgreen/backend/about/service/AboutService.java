package com.pphgreen.backend.about.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pphgreen.backend.about.dto.AboutRequest;
import com.pphgreen.backend.about.dto.AboutResponse;
import com.pphgreen.backend.about.dto.AboutUserSummary;
import com.pphgreen.backend.about.entity.About;
import com.pphgreen.backend.about.repository.AboutRepository;
import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class AboutService {

    private final AboutRepository aboutRepository;
    private final UserService userService;

    public AboutService(AboutRepository aboutRepository, UserService userService) {
        this.aboutRepository = aboutRepository;
        this.userService = userService;
    }

    public List<AboutResponse> getAllAbout() {
        return aboutRepository.findAllWithCreatedBy()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AboutResponse getAbout(Long id) {
        return toResponse(findAbout(id));
    }

    public AboutResponse createAbout(AboutRequest request, String creatorEmail) {
        User creator = adminUser(creatorEmail);
        About about = new About();
        applyRequest(about, request);
        about.setCreatedBy(creator);
        About saved = aboutRepository.save(about);
        return toResponse(findAbout(saved.getId()));
    }

    public AboutResponse updateAbout(Long id, AboutRequest request, String actorEmail) {
        adminUser(actorEmail);
        About about = findAbout(id);
        applyRequest(about, request);
        aboutRepository.save(about);
        return toResponse(findAbout(id));
    }

    public void deleteAbout(Long id, String actorEmail) {
        adminUser(actorEmail);
        aboutRepository.delete(findAbout(id));
    }

    private About findAbout(Long id) {
        return aboutRepository.findWithCreatedBy(id)
                .orElseThrow(() -> new ResourceNotFoundException("About", id));
    }

    private User adminUser(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can manage about content");
        }
        return user;
    }

    private void applyRequest(About about, AboutRequest request) {
        about.setTitle(request.title());
        about.setDescription(request.description());
        about.setIdeals(request.ideals());
        about.setImpactMetrics(request.impactMetrics());
        about.setImage(request.image());
    }

    private AboutResponse toResponse(About about) {
        User creator = about.getCreatedBy();
        return new AboutResponse(about.getId(), about.getTitle(), about.getDescription(), about.getIdeals(),
                about.getImpactMetrics(), about.getImage(),
                new AboutUserSummary(creator.getId(), creator.getName()),
                about.getCreatedAt(), about.getUpdatedAt());
    }
}