package com.pphgreen.backend.program.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pphgreen.backend.program.entity.Program;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    @Query("select p from Program p join fetch p.createdBy order by p.createdAt asc")
    List<Program> findAllWithCreatedBy();

    @Query("select p from Program p join fetch p.createdBy where p.id = :id")
    Optional<Program> findWithCreatedBy(@Param("id") Long id);
}