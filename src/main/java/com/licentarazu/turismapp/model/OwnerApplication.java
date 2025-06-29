package com.licentarazu.turismapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "owner_applications")
public class OwnerApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = true) // Allow null when user is deleted
    private User user;
    
    @Column(nullable = false, unique = true) // Track by email to survive account deletion/recreation
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OwnerStatus status = OwnerStatus.PENDING;

    @Column(length = 1000)
    private String message; // Optional message from the user

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime reviewedAt;

    @Column(length = 1000)
    private String reviewNotes; // Admin notes for approval/rejection

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        // Ensure email is set from user if not already set
        if (this.email == null && this.user != null) {
            this.email = this.user.getEmail();
        }
    }

    // Constructors
    public OwnerApplication() {}

    public OwnerApplication(User user, String message) {
        this.user = user;
        this.email = user.getEmail(); // Always store email for persistence
        this.message = message;
        this.status = OwnerStatus.PENDING;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public OwnerStatus getStatus() {
        return status;
    }

    public void setStatus(OwnerStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
}
