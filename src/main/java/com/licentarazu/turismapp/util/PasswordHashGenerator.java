package com.licentarazu.turismapp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes for testing
 * Run this to generate correct password hashes
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== PASSWORD HASH GENERATOR ===");
        System.out.println();
        
        // Generate hash for admin123
        String adminPassword = "admin123";
        String adminHash = encoder.encode(adminPassword);
        System.out.println("Password: " + adminPassword);
        System.out.println("BCrypt Hash: " + adminHash);
        System.out.println("Verification: " + encoder.matches(adminPassword, adminHash));
        System.out.println();
        
        // Test the existing hash from database
        String existingHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";
        System.out.println("Testing existing hash from database:");
        System.out.println("Hash: " + existingHash);
        System.out.println("Matches 'admin123': " + encoder.matches("admin123", existingHash));
        System.out.println("Matches 'password': " + encoder.matches("password", existingHash));
        System.out.println("Matches 'hello': " + encoder.matches("hello", existingHash));
        System.out.println();
        
        // Generate hashes for other test passwords
        String[] passwords = {"owner123", "guest123", "password", "123456"};
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("Password: " + password);
            System.out.println("Hash: " + hash);
            System.out.println("Verification: " + encoder.matches(password, hash));
            System.out.println();
        }
    }
}
