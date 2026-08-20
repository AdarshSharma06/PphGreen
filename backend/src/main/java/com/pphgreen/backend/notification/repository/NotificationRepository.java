package com.pphgreen.backend.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pphgreen.backend.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("select n from Notification n join fetch n.recipient where n.recipient.id = :userId order by n.createdAt desc")
    List<Notification> findAllWithRecipientByRecipientId(@Param("userId") Long userId);

    @Query("select n from Notification n join fetch n.recipient where n.id = :id")
    Optional<Notification> findWithRecipientById(@Param("id") Long id);
}