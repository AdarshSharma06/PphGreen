package com.pphgreen.backend.about.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pphgreen.backend.about.entity.About;

public interface AboutRepository extends JpaRepository<About, Long> {

    @Query("select a from About a join fetch a.createdBy order by a.createdAt asc")
    List<About> findAllWithCreatedBy();

    @Query("select a from About a join fetch a.createdBy where a.id = :id")
    Optional<About> findWithCreatedBy(@Param("id") Long id);
}