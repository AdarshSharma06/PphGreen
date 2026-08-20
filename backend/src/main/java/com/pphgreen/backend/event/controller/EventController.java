package com.pphgreen.backend.event.controller;

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
import com.pphgreen.backend.event.dto.EventRequest;
import com.pphgreen.backend.event.dto.EventResponse;
import com.pphgreen.backend.event.service.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEvents() {
        return ResponseEntity.ok(ApiResponse.success(eventService.getAllEvents()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getEvent(id)));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getUpcomingEvents() {
        return ResponseEntity.ok(ApiResponse.success(eventService.getUpcomingEvents()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Event created", eventService.createEvent(request, currentUserEmail())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Event updated", eventService.updateEvent(id, request, currentUserEmail())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id, currentUserEmail());
        return ResponseEntity.ok(ApiResponse.success("Event deleted", null));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}