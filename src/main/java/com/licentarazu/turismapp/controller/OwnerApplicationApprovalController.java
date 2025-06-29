package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.model.OwnerApplication;
import com.licentarazu.turismapp.model.OwnerStatus;
import com.licentarazu.turismapp.model.Role;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.service.EmailService;
import com.licentarazu.turismapp.service.OwnerApplicationService;
import com.licentarazu.turismapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/owner-applications")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class OwnerApplicationApprovalController {

    private static final Logger logger = LoggerFactory.getLogger(OwnerApplicationApprovalController.class);
    private static final String ADMIN_PASSWORD = "Rzvtare112";
    
    // Store approval tokens with their metadata
    private static final Map<String, ApprovalToken> approvalTokens = new ConcurrentHashMap<>();

    @Autowired
    private OwnerApplicationService ownerApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Generate secure approval/rejection links and send admin notification
     */
    @PostMapping("/{applicationId}/generate-approval-links")
    public ResponseEntity<?> generateApprovalLinks(@PathVariable Long applicationId) {
        try {
            Optional<OwnerApplication> applicationOpt = ownerApplicationService.getApplicationById(applicationId);
            if (applicationOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Application not found"));
            }

            OwnerApplication application = applicationOpt.get();
            
            // Generate unique tokens
            String approvalToken = UUID.randomUUID().toString();
            String rejectionToken = UUID.randomUUID().toString();
            
            // Store tokens with metadata
            approvalTokens.put(approvalToken, new ApprovalToken(applicationId, "APPROVE", LocalDateTime.now()));
            approvalTokens.put(rejectionToken, new ApprovalToken(applicationId, "REJECT", LocalDateTime.now()));
            
            // Create approval/rejection URLs
            String baseUrl = "http://localhost:5173"; // Frontend URL
            String approvalUrl = baseUrl + "/owner-application-response?token=" + approvalToken + "&action=approve";
            String rejectionUrl = baseUrl + "/owner-application-response?token=" + rejectionToken + "&action=reject";
            
            // Get applicant info
            String applicantName = "Unknown";
            String applicantEmail = application.getEmail();
            
            if (application.getUser() != null) {
                applicantName = application.getUser().getFirstName() + " " + application.getUser().getLastName();
            }
            
            // Send admin notification email with action links
            try {
                emailService.sendOwnerApplicationApprovalLinks(
                    applicantName, 
                    applicantEmail, 
                    application.getMessage(), 
                    approvalUrl, 
                    rejectionUrl
                );
                
                logger.info("✅ APPROVAL LINKS SENT to admin for application {}", applicationId);
                
                return ResponseEntity.ok(Map.of(
                    "message", "Approval links sent to admin",
                    "approvalToken", approvalToken,
                    "rejectionToken", rejectionToken
                ));
                
            } catch (Exception e) {
                logger.error("❌ FAILED TO SEND APPROVAL LINKS: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send approval links"));
            }
            
        } catch (Exception e) {
            logger.error("❌ ERROR GENERATING APPROVAL LINKS: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate approval links"));
        }
    }

    /**
     * Validate token and show confirmation page data
     */
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token, @RequestParam String action) {
        try {
            ApprovalToken approvalToken = approvalTokens.get(token);
            
            if (approvalToken == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Invalid or expired token"));
            }
            
            if (approvalToken.isUsed()) {
                return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "This request has already been reviewed."
                ));
            }
            
            // Check if token action matches requested action
            if (!approvalToken.getAction().equalsIgnoreCase(action)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Token action mismatch"));
            }
            
            // Get application details
            Optional<OwnerApplication> applicationOpt = ownerApplicationService.getApplicationById(approvalToken.getApplicationId());
            if (applicationOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Application not found"));
            }
            
            OwnerApplication application = applicationOpt.get();
            String applicantName = "Unknown";
            
            if (application.getUser() != null) {
                applicantName = application.getUser().getFirstName() + " " + application.getUser().getLastName();
            }
            
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "action", action.toUpperCase(),
                "applicantName", applicantName,
                "applicantEmail", application.getEmail(),
                "applicationMessage", application.getMessage(),
                "submittedAt", application.getSubmittedAt().toString()
            ));
            
        } catch (Exception e) {
            logger.error("❌ ERROR VALIDATING TOKEN: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to validate token"));
        }
    }

    /**
     * Process approval/rejection with password confirmation and send applicant notification
     */
    @PostMapping("/confirm-action")
    public ResponseEntity<?> confirmAction(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String password = request.get("password");
            String reviewNotes = request.get("reviewNotes");
            
            // Validate password
            if (!ADMIN_PASSWORD.equals(password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid password"));
            }
            
            ApprovalToken approvalToken = approvalTokens.get(token);
            
            if (approvalToken == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Invalid or expired token"));
            }
            
            if (approvalToken.isUsed()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "This request has already been reviewed."
                ));
            }
            
            // Mark token as used
            approvalToken.setUsed(true);
            
            // Get application
            Optional<OwnerApplication> applicationOpt = ownerApplicationService.getApplicationById(approvalToken.getApplicationId());
            if (applicationOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Application not found"));
            }
            
            OwnerApplication application = applicationOpt.get();
            
            // Process approval or rejection
            boolean isApproval = "APPROVE".equals(approvalToken.getAction());
            OwnerStatus newStatus = isApproval ? OwnerStatus.APPROVED : OwnerStatus.REJECTED;
            
            // Update application
            application.setStatus(newStatus);
            application.setReviewedAt(LocalDateTime.now());
            application.setReviewNotes(reviewNotes);
            ownerApplicationService.saveApplication(application);
            
            // Update user if linked
            if (application.getUser() != null) {
                User user = application.getUser();
                user.setOwnerStatus(newStatus);
                
                if (isApproval) {
                    user.setRole(Role.OWNER);
                }
                
                userRepository.save(user);
            }
            
            // Send notification email to applicant
            String applicantName = application.getUser() != null ? 
                application.getUser().getFirstName() + " " + application.getUser().getLastName() : 
                "Applicant";
            
            try {
                if (isApproval) {
                    emailService.sendOwnerApplicationApprovalNotification(
                        application.getEmail(), 
                        applicantName, 
                        reviewNotes
                    );
                    logger.info("✅ APPROVAL NOTIFICATION EMAIL SENT to: {}", application.getEmail());
                } else {
                    emailService.sendOwnerApplicationRejectionNotification(
                        application.getEmail(), 
                        applicantName, 
                        reviewNotes
                    );
                    logger.info("✅ REJECTION NOTIFICATION EMAIL SENT to: {}", application.getEmail());
                }
                
            } catch (Exception e) {
                logger.error("❌ FAILED TO SEND NOTIFICATION EMAIL to {}: {}", 
                           application.getEmail(), e.getMessage());
                // Don't fail the approval process, but log the error
                logger.warn("⚠️ APPLICATION {} PROCESSED BUT APPLICANT NOT NOTIFIED", 
                           isApproval ? "APPROVED" : "REJECTED");
            }
            
            logger.info("✅ APPLICATION {} SUCCESSFULLY {} (ID: {})", 
                       application.getId(), 
                       isApproval ? "APPROVED" : "REJECTED",
                       application.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Application " + (isApproval ? "approved" : "rejected") + " successfully. The applicant has been notified via email.",
                "action", approvalToken.getAction(),
                "emailSent", true
            ));
            
        } catch (Exception e) {
            logger.error("❌ ERROR CONFIRMING ACTION: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to process request"));
        }
    }

    /**
     * Inner class to store approval token metadata
     */
    private static class ApprovalToken {
        private final Long applicationId;
        private final String action;
        private final LocalDateTime createdAt;
        private boolean used = false;

        public ApprovalToken(Long applicationId, String action, LocalDateTime createdAt) {
            this.applicationId = applicationId;
            this.action = action;
            this.createdAt = createdAt;
        }

        public Long getApplicationId() { return applicationId; }
        public String getAction() { return action; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public boolean isUsed() { return used; }
        public void setUsed(boolean used) { this.used = used; }
    }
}
