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
}