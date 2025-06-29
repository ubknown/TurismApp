package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.dto.OwnerApplicationResponse;
import com.licentarazu.turismapp.model.OwnerApplication;
import com.licentarazu.turismapp.model.Role;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.UserRepository;
import com.licentarazu.turismapp.security.JwtUtil;
import com.licentarazu.turismapp.service.OwnerApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OwnerApplicationService ownerApplicationService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Admin login for dashboard access - requires fresh authentication
     */
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            if (email == null || password == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Missing credentials", "message", "Email and password are required"));
            }

            // Find user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Check if user is admin
            if (!user.getRole().equals(Role.ADMIN)) {
                logger.warn("Non-admin user attempted admin login: {}", email);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "message", "Admin access required"));
            }

            // Authenticate
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Generate fresh admin token
            String adminToken = jwtUtil.generateToken(user.getEmail());

            logger.info("Admin logged in successfully: {}", email);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Admin login successful",
                    "token", adminToken,
                    "user", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "role", user.getRole().name()
                    )
            ));

        } catch (Exception e) {
            logger.error("Admin login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed", "message", "Invalid credentials"));
        }
    }

    /**
     * Get all owner applications for admin dashboard
     */
    @GetMapping("/applications")
    public ResponseEntity<?> getAllApplications(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Verify admin role
            if (!user.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "message", "Admin access required"));
            }

            List<OwnerApplication> applications = ownerApplicationService.getAllApplications();
            List<OwnerApplicationResponse> responses = applications.stream()
                    .filter(app -> app.getUser() != null) // Filter out applications with null users
                    .map(app -> new OwnerApplicationResponse(
                            app.getId(),
                            app.getUser().getFirstName() + " " + app.getUser().getLastName(),
                            app.getUser().getEmail(),
                            app.getStatus(),
                            app.getMessage(),
                            app.getSubmittedAt(),
                            app.getReviewedAt()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "applications", responses,
                    "totalCount", responses.size()
            ));

        } catch (Exception e) {
            logger.error("Failed to retrieve applications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", "Failed to retrieve applications"));
        }
    }

    /**
     * Approve an owner application
     */
    @PostMapping("/applications/{applicationId}/approve")
    public ResponseEntity<?> approveApplication(@PathVariable Long applicationId,
                                                @RequestBody(required = false) Map<String, String> requestBody,
                                                Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Verify admin role
            if (!user.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "message", "Admin access required"));
            }

            String reviewNotes = requestBody != null ? requestBody.get("reviewNotes") : "";
            OwnerApplication application = ownerApplicationService.approveApplication(applicationId, reviewNotes);

            logger.info("Admin {} approved application ID {}", email, applicationId);

            return ResponseEntity.ok(Map.of(
                    "message", "Application approved successfully",
                    "applicationId", application.getId(),
                    "userEmail", application.getUser().getEmail(),
                    "status", application.getStatus().name()
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Approval failed", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to approve application: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", "Failed to approve application"));
        }
    }

    /**
     * Reject an owner application
     */
    @PostMapping("/applications/{applicationId}/reject")
    public ResponseEntity<?> rejectApplication(@PathVariable Long applicationId,
                                               @RequestBody(required = false) Map<String, String> requestBody,
                                               Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Verify admin role
            if (!user.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "message", "Admin access required"));
            }

            String reviewNotes = requestBody != null ? requestBody.get("reviewNotes") : "";
            OwnerApplication application = ownerApplicationService.rejectApplication(applicationId, reviewNotes);

            logger.info("Admin {} rejected application ID {}", email, applicationId);

            return ResponseEntity.ok(Map.of(
                    "message", "Application rejected",
                    "applicationId", application.getId(),
                    "userEmail", application.getUser().getEmail(),
                    "status", application.getStatus().name()
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Rejection failed", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to reject application: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", "Failed to reject application"));
        }
    }

    /**
     * Verify admin session is still valid
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyAdminSession(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Verify admin role
            if (!user.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "message", "Admin access required"));
            }

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "user", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "role", user.getRole().name()
                    )
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Session invalid"));
        }
    }
}
