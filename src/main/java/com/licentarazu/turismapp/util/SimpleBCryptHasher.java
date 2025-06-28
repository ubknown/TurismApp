package com.licentarazu.turismapp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Minimal BCrypt hash generator for admin123
 */
public class SimpleBCryptHasher {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String hash = encoder.encode(password);
        
        System.out.println("=== BCRYPT HASH GENERATOR ===");
        System.out.println("Password: " + password);
        System.out.println("Generated Hash: " + hash);
        System.out.println("Verification: " + encoder.matches(password, hash));
        
        // Test the hash you're currently using
        String currentHash = "$2a$10$GTUA08wkyZ6qkjN/vdK8buT7ZtSV9tDUrd0RTjTUjgB9o2XmJQWs.";
        System.out.println("\n=== TESTING YOUR CURRENT HASH ===");
        System.out.println("Current Hash: " + currentHash);
        System.out.println("Matches 'admin123': " + encoder.matches(password, currentHash));
        
        System.out.println("\n=== SQL UPDATE COMMAND ===");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE email = 'admin@tourism.com';");
    }
}
