package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.dto.OwnerApplicationRequest;
import com.licentarazu.turismapp.dto.OwnerApplicationResponse;
import com.licentarazu.turismapp.model.OwnerApplication;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.UserRepository;
import com.licentarazu.turismapp.service.OwnerApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/owner-applications")
@CrossOrigin(origins = "http://localhost:5173")
public class OwnerApplicationController {

    @Autowired
    private OwnerApplicationService ownerApplicationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Submit an owner application
     * 
     * This endpoint allows authenticated GUEST users to submit an owner application.
     * The response includes both application data and updated user information to
     * enable immediate UI state updates without requiring a page reload.
     * 
     * Business Logic:
     * - Validates user eligibility (must be GUEST with ownerStatus NONE)
     * - Creates new OwnerApplication with PENDING status
     * - Updates user's ownerStatus from NONE to PENDING
     * - Sends admin notification email with approval links
     * - Returns comprehensive response for UI state management
     * 
     * @param request The application request containing optional message
     * @param authentication The authenticated user context
     * @return ResponseEntity with application data and updated user information
     */
    @PostMapping
    public ResponseEntity<?> submitApplication(@Valid @RequestBody OwnerApplicationRequest request, 
                                               Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Check if user can apply
            if (!ownerApplicationService.canUserApply(user)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Cannot apply",
                                "message", "You already have a pending, approved, or rejected application, or you are already an owner."
                        ));
            }

            OwnerApplication application = ownerApplicationService.submitApplication(user, request.getMessage());
            
            // Fetch updated user data to include in response for immediate UI state update
            User updatedUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found after application submission"));
            
            return ResponseEntity.ok(Map.of(
                    "message", "Your owner application has been submitted successfully!",
                    "applicationId", application.getId(),
                    "status", application.getStatus().name(),
                    // Include updated user data for immediate UI state synchronization
                    "updatedUser", Map.of(
                            "id", updatedUser.getId(),
                            "firstName", updatedUser.getFirstName(),
                            "lastName", updatedUser.getLastName(),
                            "email", updatedUser.getEmail(),
                            "role", updatedUser.getRole().name(),
                            "ownerStatus", updatedUser.getOwnerStatus().name()
                    )
            ));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Application failed",
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Internal server error",
                            "message", "Failed to submit application"
                    ));
        }
    }

    /**
     * Get user's application status
     */
    @GetMapping("/my-application")
    public ResponseEntity<?> getMyApplication(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Optional<OwnerApplication> applicationOpt = ownerApplicationService.getApplicationByUser(user);
            
            if (applicationOpt.isPresent()) {
                OwnerApplication application = applicationOpt.get();
                return ResponseEntity.ok(Map.of(
                        "hasApplication", true,
                        "status", application.getStatus().name(),
                        "submittedAt", application.getSubmittedAt(),
                        "reviewedAt", application.getReviewedAt(),
                        "message", application.getMessage() != null ? application.getMessage() : "",
                        "reviewNotes", application.getReviewNotes() != null ? application.getReviewNotes() : ""
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "hasApplication", false,
                        "canApply", ownerApplicationService.canUserApply(user)
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Internal server error",
                            "message", "Failed to retrieve application status"
                    ));
        }
    }

    /**
     * Check if user can apply (public endpoint for UI logic)
     */
    @GetMapping("/can-apply")
    public ResponseEntity<?> canApply(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            boolean canApply = ownerApplicationService.canUserApply(user);
            
            return ResponseEntity.ok(Map.of(
                    "canApply", canApply,
                    "ownerStatus", user.getOwnerStatus().name(),
                    "role", user.getRole().name()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Internal server error",
                            "message", "Failed to check application eligibility"
                    ));
        }
    }

    /**
     * Get all pending applications (admin only)
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingApplications(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Check if user is admin
            if (!user.getRole().name().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "message", "Admin access required"));
            }

            List<OwnerApplication> applications = ownerApplicationService.getPendingApplications();
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

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Internal server error",
                            "message", "Failed to retrieve pending applications"
                    ));
        }
    }

    /**
     * Approve an application (admin only)
     */
    @PostMapping("/{applicationId}/approve")
    public ResponseEntity<?> approveApplication(@PathVariable Long applicationId,
                                                @RequestBody(required = false) Map<String, String> requestBody,
                                                Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Check if user is admin
            if (!user.getRole().name().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "message", "Admin access required"));
            }

            String reviewNotes = requestBody != null ? requestBody.get("reviewNotes") : "";
            OwnerApplication application = ownerApplicationService.approveApplication(applicationId, reviewNotes);

            return ResponseEntity.ok(Map.of(
                    "message", "Application approved successfully",
                    "applicationId", application.getId(),
                    "userEmail", application.getUser().getEmail()
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Approval failed", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", "Failed to approve application"));
        }
    }

    /**
     * Reject an application (admin only)
     */
    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<?> rejectApplication(@PathVariable Long applicationId,
                                               @RequestBody(required = false) Map<String, String> requestBody,
                                               Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Check if user is admin
            if (!user.getRole().name().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "message", "Admin access required"));
            }

            String reviewNotes = requestBody != null ? requestBody.get("reviewNotes") : "";
            OwnerApplication application = ownerApplicationService.rejectApplication(applicationId, reviewNotes);

            return ResponseEntity.ok(Map.of(
                    "message", "Application rejected",
                    "applicationId", application.getId(),
                    "userEmail", application.getUser().getEmail()
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Rejection failed", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", "Failed to reject application"));
        }
    }
}
