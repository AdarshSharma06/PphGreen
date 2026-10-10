package com.pphgreen.backend.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EventServiceConstructorTest {

    @Test
    void eventServiceHasSingleAutowiredConstructor() {
        Constructor<?>[] constructors = EventService.class.getDeclaredConstructors();
        // Should have exactly 1 constructor now (6-arg), not 2
        assertEquals(1, constructors.length, "EventService should have exactly 1 constructor to avoid Spring ambiguity");

        Constructor<?> ctor = constructors[0];
        assertTrue(ctor.isAnnotationPresent(Autowired.class), "Primary constructor must be @Autowired");

        // Verify all required dependencies are present
        Class<?>[] params = ctor.getParameterTypes();
        assertEquals(6, params.length, "Constructor should have 6 dependencies");
        assertTrue(Arrays.asList(params).contains(com.pphgreen.backend.event.repository.EventRepository.class));
        assertTrue(Arrays.asList(params).contains(com.pphgreen.backend.storage.StorageService.class));
    }

    @Test
    void galleryServiceHasAutowiredConstructor() {
        Constructor<?>[] constructors = com.pphgreen.backend.gallery.service.GalleryService.class.getDeclaredConstructors();
        long autowiredCount = Arrays.stream(constructors).filter(c -> c.isAnnotationPresent(Autowired.class)).count();
        assertEquals(1, autowiredCount, "GalleryService should have exactly 1 @Autowired constructor");
    }
}
