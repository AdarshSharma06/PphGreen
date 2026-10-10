package com.pphgreen.backend.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pphgreen.backend.user.entity.AdminStatus;
import com.pphgreen.backend.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByAdminStatus(AdminStatus adminStatus);

    @org.springframework.data.jpa.repository.Query("select u from User u where u.id != :excludeId order by u.name asc")
    java.util.List<User> findAllExcluding(@org.springframework.data.repository.query.Param("excludeId") Long excludeId, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("select u from User u where u.id != :excludeId and (lower(u.name) like lower(concat('%', :q, '%')) or lower(u.apartmentNumber) like lower(concat('%', :q, '%')) or lower(u.tower) like lower(concat('%', :q, '%'))) order by u.name asc")
    java.util.List<User> searchByNameOrFlat(@org.springframework.data.repository.query.Param("q") String q, @org.springframework.data.repository.query.Param("excludeId") Long excludeId, org.springframework.data.domain.Pageable pageable);
}