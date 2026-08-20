package com.pphgreen.backend.event.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pphgreen.backend.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("select e from Event e join fetch e.createdBy order by e.date asc")
    List<Event> findAllWithCreatedBy();

    @Query("select e from Event e join fetch e.createdBy where e.id = :id")
    Optional<Event> findWithCreatedBy(@Param("id") Long id);
}