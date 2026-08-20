package com.pphgreen.backend.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.event.dto.EventRequest;
import com.pphgreen.backend.event.dto.EventResponse;
import com.pphgreen.backend.event.entity.Event;
import com.pphgreen.backend.event.repository.EventRepository;
import com.pphgreen.backend.user.entity.AccountStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserService userService;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, userService);
    }

    @Test
    void createEventByAdminUsesAuthenticatedUserAsCreator() {
        User admin = adminUser();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.createEvent(request(), "admin@example.com");

        Event saved = eventRepositoryCaptured();
        assertEquals("admin@example.com", saved.getCreatedBy().getEmail());
        assertEquals("Jane Doe", response.createdBy().name());

        assertEquals("Community BBQ", saved.getTitle());
        assertEquals("Summer gathering", saved.getDescription());
        assertEquals(LocalDate.of(2026, 9, 1), saved.getDate());
        assertEquals(LocalTime.of(18, 30), saved.getTime());
        assertEquals("Clubhouse", saved.getVenue());
        assertEquals("https://cdn.example.com/bbq.jpg", saved.getImage());
    }

    @Test
    void memberCanViewAllEvents() {
        when(eventRepository.findAllWithCreatedBy()).thenReturn(List.of(sampleEvent(), sampleEvent()));

        List<EventResponse> responses = eventService.getAllEvents();

        assertEquals(2, responses.size());
        assertEquals("Community BBQ", responses.get(0).title());
    }

    @Test
    void memberCanViewEventById() {
        Event event = sampleEvent();
        when(eventRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(event));

        EventResponse response = eventService.getEvent(1L);

        assertEquals("Community BBQ", response.title());
        assertEquals("Jane Doe", response.createdBy().name());
    }

    @Test
    void upcomingReturnsOnlyFutureEvents() {
        Event past = sampleEvent();
        past.setDate(LocalDate.now().minusDays(1));
        Event future = sampleEvent();
        future.setDate(LocalDate.now().plusDays(1));
        when(eventRepository.findAllWithCreatedBy()).thenReturn(List.of(past, future));

        List<EventResponse> responses = eventService.getUpcomingEvents();

        assertEquals(1, responses.size());
        assertEquals(LocalDate.now().plusDays(1), responses.get(0).date());
    }

    @Test
    void updateEventByAdminUpdatesFields() {
        User admin = adminUser();
        Event event = sampleEvent();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventRequest update = new EventRequest("Updated Title", "Updated description",
                LocalDate.of(2026, 10, 10), LocalTime.of(10, 0), "New Venue", null);

        EventResponse response = eventService.updateEvent(1L, update, "admin@example.com");

        assertEquals("Updated Title", event.getTitle());
        assertEquals("Updated description", event.getDescription());
        assertEquals(LocalDate.of(2026, 10, 10), event.getDate());
        assertEquals("New Venue", event.getVenue());
        assertEquals("Updated Title", response.title());
    }

    @Test
    void deleteEventByAdminDeletesEvent() {
        User admin = adminUser();
        Event event = sampleEvent();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findWithCreatedBy(1L)).thenReturn(Optional.of(event));

        eventService.deleteEvent(1L, "admin@example.com");

        verify(eventRepository).delete(event);
    }

    @Test
    void memberCannotCreateEvent() {
        User member = memberUser();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));

        assertThrows(ForbiddenException.class, () -> eventService.createEvent(request(), "member@example.com"));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void memberCannotUpdateEvent() {
        User member = memberUser();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));

        assertThrows(ForbiddenException.class, () -> eventService.updateEvent(1L, request(), "member@example.com"));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void memberCannotDeleteEvent() {
        User member = memberUser();
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));

        assertThrows(ForbiddenException.class, () -> eventService.deleteEvent(1L, "member@example.com"));
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void getMissingEventThrowsResourceNotFound() {
        when(eventRepository.findWithCreatedBy(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> eventService.getEvent(999L));

        assertEquals("Event not found with id: 999", ex.getMessage());
    }

    @Test
    void updateMissingEventThrowsResourceNotFound() {
        User admin = adminUser();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findWithCreatedBy(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> eventService.updateEvent(999L, request(), "admin@example.com"));
    }

    @Test
    void deleteMissingEventThrowsResourceNotFound() {
        User admin = adminUser();
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findWithCreatedBy(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> eventService.deleteEvent(999L, "admin@example.com"));
    }

    private Event eventRepositoryCaptured() {
        org.mockito.ArgumentCaptor<Event> captor = org.mockito.ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        return captor.getValue();
    }

    private EventRequest request() {
        return new EventRequest("Community BBQ", "Summer gathering",
                LocalDate.of(2026, 9, 1), LocalTime.of(18, 30), "Clubhouse", "https://cdn.example.com/bbq.jpg");
    }

    private Event sampleEvent() {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Community BBQ");
        event.setDescription("Summer gathering");
        event.setDate(LocalDate.of(2026, 9, 1));
        event.setTime(LocalTime.of(18, 30));
        event.setVenue("Clubhouse");
        event.setImage("https://cdn.example.com/bbq.jpg");
        event.setCreatedBy(adminUser());
        return event;
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