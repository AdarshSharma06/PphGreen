package com.pphgreen.backend.event.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.event.dto.EventRequest;
import com.pphgreen.backend.event.dto.EventResponse;
import com.pphgreen.backend.event.dto.EventUserSummary;
import com.pphgreen.backend.event.entity.Event;
import com.pphgreen.backend.event.repository.EventRepository;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserService userService;

    public EventService(EventRepository eventRepository, UserService userService) {
        this.eventRepository = eventRepository;
        this.userService = userService;
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAllWithCreatedBy()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EventResponse> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findAllWithCreatedBy()
                .stream()
                .filter(event -> !eventDateTime(event).isBefore(now))
                .map(this::toResponse)
                .toList();
    }

    public EventResponse getEvent(Long id) {
        return toResponse(findEvent(id));
    }

    public EventResponse createEvent(EventRequest request, String creatorEmail) {
        User creator = adminUser(creatorEmail);
        Event event = new Event();
        applyRequest(event, request);
        event.setCreatedBy(creator);
        return toResponse(eventRepository.save(event));
    }

    public EventResponse updateEvent(Long id, EventRequest request, String actorEmail) {
        adminUser(actorEmail);
        Event event = findEvent(id);
        applyRequest(event, request);
        return toResponse(eventRepository.save(event));
    }

    public void deleteEvent(Long id, String actorEmail) {
        adminUser(actorEmail);
        eventRepository.delete(findEvent(id));
    }

    private Event findEvent(Long id) {
        return eventRepository.findWithCreatedBy(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    private User adminUser(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can manage events");
        }
        return user;
    }

    private void applyRequest(Event event, EventRequest request) {
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setDate(request.date());
        event.setTime(request.time());
        event.setVenue(request.venue());
        event.setImage(request.image());
    }

    private LocalDateTime eventDateTime(Event event) {
        LocalTime time = event.getTime();
        return LocalDateTime.of(event.getDate(), time == null ? LocalTime.MIN : time);
    }

    private EventResponse toResponse(Event event) {
        User creator = event.getCreatedBy();
        return new EventResponse(event.getId(), event.getTitle(), event.getDescription(), event.getDate(),
                event.getTime(), event.getVenue(), event.getImage(),
                new EventUserSummary(creator.getId(), creator.getName()),
                event.getCreatedAt(), event.getUpdatedAt());
    }
}