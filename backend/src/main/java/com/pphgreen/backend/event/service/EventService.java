package com.pphgreen.backend.event.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.pphgreen.backend.comment.repository.CommentRepository;
import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.event.dto.EventRequest;
import com.pphgreen.backend.event.dto.EventResponse;
import com.pphgreen.backend.event.dto.EventUserSummary;
import com.pphgreen.backend.event.entity.Event;
import com.pphgreen.backend.event.repository.EventRepository;
import com.pphgreen.backend.gallery.entity.Gallery;
import com.pphgreen.backend.gallery.repository.GalleryRepository;
import com.pphgreen.backend.reaction.repository.ReactionRepository;
import com.pphgreen.backend.storage.StorageService;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final GalleryRepository galleryRepository;
    private final StorageService storageService;

    public EventService(EventRepository eventRepository, UserService userService,
                        CommentRepository commentRepository, ReactionRepository reactionRepository,
                        GalleryRepository galleryRepository, StorageService storageService) {
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.commentRepository = commentRepository;
        this.reactionRepository = reactionRepository;
        this.galleryRepository = galleryRepository;
        this.storageService = storageService;
    }

    // Backward compatible constructor for existing tests that mock only 4 repos
    public EventService(EventRepository eventRepository, UserService userService,
                        CommentRepository commentRepository, ReactionRepository reactionRepository,
                        GalleryRepository galleryRepository) {
        this(eventRepository, userService, commentRepository, reactionRepository, galleryRepository, null);
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
        Event saved = eventRepository.save(event);
        return toResponse(findEvent(saved.getId()));
    }

    public EventResponse updateEvent(Long id, EventRequest request, String actorEmail) {
        adminUser(actorEmail);
        Event event = findEvent(id);
        applyRequest(event, request);
        eventRepository.save(event);
        return toResponse(findEvent(id));
    }

    public void deleteEvent(Long id, String actorEmail) {
        adminUser(actorEmail);
        Event event = findEvent(id);
        List<String> storagePaths = List.of();
        if (galleryRepository != null) {
            try {
                List<Gallery> media = galleryRepository.findByEventIdWithUploader(id);
                if (media != null) {
                    storagePaths = media.stream()
                            .map(Gallery::getStoragePath)
                            .filter(p -> p != null && !p.isBlank())
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("Failed to collect gallery storage paths for event {}: {}", id, e.getMessage());
            }
        }
        deleteEventData(event);
        // Best-effort storage cleanup after DB commit — cannot be rolled back
        // Only objects for this event_id are targeted; standalone media never included
        if (storageService != null && !storagePaths.isEmpty()) {
            for (String path : storagePaths) {
                try {
                    storageService.delete(path);
                } catch (RuntimeException e) {
                    log.error("Failed to delete gallery storage object for event {} path {}: {}. Manual cleanup may be required for bucket path '{}'",
                            id, sanitizePath(path), e.getMessage(), path);
                }
            }
        }
    }

    @Transactional
    void deleteEventData(Event event) {
        Long id = event.getId();
        commentRepository.deleteByEventId(id);
        reactionRepository.deleteByEventId(id);
        if (galleryRepository != null) {
            galleryRepository.deleteByEventId(id);
        }
        eventRepository.delete(event);
    }

    private String sanitizePath(String path) {
        if (path == null) return "(null)";
        // expose only prefix/length, not credentials
        return path.length() > 80 ? path.substring(0, 80) + "..." : path;
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