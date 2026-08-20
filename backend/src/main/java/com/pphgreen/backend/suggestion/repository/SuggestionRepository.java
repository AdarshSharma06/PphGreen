package com.pphgreen.backend.suggestion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pphgreen.backend.suggestion.entity.Suggestion;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    @Query("select s from Suggestion s join fetch s.submitter order by s.createdAt desc")
    List<Suggestion> findAllWithSubmitter();

    @Query("select s from Suggestion s join fetch s.submitter where s.id = :id")
    Optional<Suggestion> findWithSubmitter(@Param("id") Long id);
}