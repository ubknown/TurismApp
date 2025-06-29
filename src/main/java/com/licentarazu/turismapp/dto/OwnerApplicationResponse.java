package com.licentarazu.turismapp.dto;

import com.licentarazu.turismapp.model.OwnerStatus;
import java.time.LocalDateTime;

public class OwnerApplicationResponse {
    
    private Long id;
    private String userName;
    private String userEmail;
    private OwnerStatus status;
    private String message;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    public OwnerApplicationResponse() {}

    public OwnerApplicationResponse(Long id, String userName, String userEmail, OwnerStatus status, 
                                   String message, LocalDateTime submittedAt, LocalDateTime reviewedAt) {
        this.id = id;
        this.userName = userName;
        this.userEmail = userEmail;
        this.status = status;
        this.message = message;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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
}
