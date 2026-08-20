package com.pphgreen.backend.developer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pphgreen.backend.developer.entity.Developer;

public interface DeveloperRepository extends JpaRepository<Developer, Long> {

    @Query("select d from Developer d join fetch d.createdBy order by d.createdAt asc")
    List<Developer> findAllWithCreatedBy();
}