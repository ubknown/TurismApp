package com.licentarazu.turismapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.licentarazu.turismapp.repository.UserRepository;
import com.licentarazu.turismapp.repository.AccommodationUnitRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:5173")
public class TestController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccommodationUnitRepository accommodationUnitRepository;

    @GetMapping("/database")
    public ResponseEntity<?> testDatabase() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Test database connection by counting records
            long userCount = userRepository.count();
            long accommodationCount = accommodationUnitRepository.count();
            
            response.put("status", "Database connection successful");
            response.put("userCount", userCount);
            response.put("accommodationCount", accommodationCount);
            response.put("message", "All repository connections working properly");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "Database connection failed");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("type", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Application is running");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
}
