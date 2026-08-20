package com.pphgreen.backend.event.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pphgreen.backend.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByOrderByDateAsc();
}