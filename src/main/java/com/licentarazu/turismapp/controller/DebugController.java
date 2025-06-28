package com.licentarazu.turismapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.licentarazu.turismapp.dto.LoginRequest;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debug controller to isolate password matching issues
 */
@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/test-password-match")
    public ResponseEntity<?> testPasswordMatch(@RequestBody LoginRequest request) {
        logger.info("🔍 ===== DEBUG PASSWORD MATCHING =====");
        logger.info("🔍 Email: '{}'", request.getEmail());
        logger.info("🔍 Password: '{}'", request.getPassword());
        logger.info("🔍 Password length: {}", request.getPassword().length());
        logger.info("🔍 Password bytes: {}", java.util.Arrays.toString(request.getPassword().getBytes()));

        Map<String, Object> response = new HashMap<>();
        response.put("email", request.getEmail());
        response.put("password", request.getPassword());
        response.put("passwordLength", request.getPassword().length());

        try {
            // Step 1: Find user in database
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            
            if (user == null) {
                logger.error("❌ User not found for email: '{}'", request.getEmail());
                response.put("userFound", false);
                return ResponseEntity.ok(response);
            }

            logger.info("✅ User found: ID={}, Email='{}', Enabled={}, Role={}", 
                       user.getId(), user.getEmail(), user.getEnabled(), user.getRole());
            logger.info("🔑 DB Password Hash: '{}'", user.getPassword());
            logger.info("🔑 DB Hash Length: {}", user.getPassword().length());

            response.put("userFound", true);
            response.put("userId", user.getId());
            response.put("userEmail", user.getEmail());
            response.put("userEnabled", user.getEnabled());
            response.put("userRole", user.getRole().toString());
            response.put("dbPasswordHash", user.getPassword());
            response.put("dbHashLength", user.getPassword().length());

            // Step 2: Test password matching
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            
            logger.info("🧪 About to call encoder.matches('{}', '{}')", request.getPassword(), user.getPassword());
            boolean passwordMatches = encoder.matches(request.getPassword(), user.getPassword());
            logger.info("🔍 encoder.matches() result: {}", passwordMatches);

            response.put("passwordMatches", passwordMatches);

            // Step 3: Test some variations
            String trimmedPassword = request.getPassword().trim();
            boolean trimmedMatches = encoder.matches(trimmedPassword, user.getPassword());
            logger.info("🔍 Trimmed password matches: {}", trimmedMatches);
            response.put("trimmedPasswordMatches", trimmedMatches);

            // Step 4: Test against known good hash
            String knownGoodHash = "$2a$10$JQnA/KNpUZFVzrPOdAzPmePGp3csnIZXZCyVuIkV9u3oe7yCHp0ya";
            boolean knownHashMatches = encoder.matches(request.getPassword(), knownGoodHash);
            logger.info("🔍 Known good hash matches: {}", knownHashMatches);
            response.put("knownGoodHashMatches", knownHashMatches);
            response.put("knownGoodHash", knownGoodHash);

            // Step 5: Generate new hash for comparison
            String newHash = encoder.encode(request.getPassword());
            logger.info("🔍 New generated hash: {}", newHash);
            boolean newHashMatches = encoder.matches(request.getPassword(), newHash);
            logger.info("🔍 New hash matches: {}", newHashMatches);
            response.put("newGeneratedHash", newHash);
            response.put("newHashMatches", newHashMatches);

            logger.info("🔍 ===== DEBUG COMPLETE =====");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error during password testing: {}", e.getMessage(), e);
            response.put("error", e.getMessage());
            response.put("exception", e.getClass().getSimpleName());
            return ResponseEntity.ok(response);
        }
    }
}
