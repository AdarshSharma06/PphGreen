package com.pphgreen.backend.message.entity;

import java.time.Instant;

import com.pphgreen.backend.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "conversations", uniqueConstraints = @UniqueConstraint(name = "uk_conversation_participants", columnNames = {"participant_one_id", "participant_two_id"}))
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_one_id", nullable = false)
    private User participantOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_two_id", nullable = false)
    private User participantTwo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() { Instant now = Instant.now(); this.createdAt = now; this.updatedAt = now; }
    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getParticipantOne() { return participantOne; }
    public void setParticipantOne(User participantOne) { this.participantOne = participantOne; }
    public User getParticipantTwo() { return participantTwo; }
    public void setParticipantTwo(User participantTwo) { this.participantTwo = participantTwo; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public User getOtherParticipant(Long userId) {
        if (participantOne.getId().equals(userId)) return participantTwo;
        return participantOne;
    }

    public boolean isParticipant(Long userId) {
        return participantOne.getId().equals(userId) || participantTwo.getId().equals(userId);
    }
}
