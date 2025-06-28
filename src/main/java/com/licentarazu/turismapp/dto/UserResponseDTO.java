package com.licentarazu.turismapp.dto;

import com.licentarazu.turismapp.model.OwnerStatus;
import java.time.LocalDateTime;

public class UserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdAt;
    private String role;
    private OwnerStatus ownerStatus;

    // Constructors
    public UserResponseDTO() {}

    public UserResponseDTO(Long id, String firstName, String lastName, String email, 
                          LocalDateTime createdAt, String role, OwnerStatus ownerStatus) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.createdAt = createdAt;
        this.role = role;
        this.ownerStatus = ownerStatus;
    }

    // Legacy constructor for backward compatibility
    public UserResponseDTO(Long id, String firstName, String lastName, String email, 
                          LocalDateTime createdAt, String role) {
        this(id, firstName, lastName, email, createdAt, role, OwnerStatus.NONE);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public OwnerStatus getOwnerStatus() {
        return ownerStatus;
    }

    public void setOwnerStatus(OwnerStatus ownerStatus) {
        this.ownerStatus = ownerStatus;
    }
}
