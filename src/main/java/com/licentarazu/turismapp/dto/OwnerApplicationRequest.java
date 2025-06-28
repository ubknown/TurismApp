package com.licentarazu.turismapp.dto;

import jakarta.validation.constraints.Size;

public class OwnerApplicationRequest {
    
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;

    public OwnerApplicationRequest() {}

    public OwnerApplicationRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
