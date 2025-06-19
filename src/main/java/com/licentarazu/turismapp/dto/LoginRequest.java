package com.licentarazu.turismapp.dto;

public class LoginRequest {
    private String email;
    private String password;

    // Getteri și setteri
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
