package com.licentarazu.turismapp.service;

import com.licentarazu.turismapp.model.OwnerApplication;
import com.licentarazu.turismapp.model.OwnerStatus;
import com.licentarazu.turismapp.model.Role;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.OwnerApplicationRepository;
import com.licentarazu.turismapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OwnerApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(OwnerApplicationService.class);

    @Autowired
    private OwnerApplicationRepository ownerApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Submit an owner application for a user
     */
    @Transactional
    public OwnerApplication submitApplication(User user, String message) {
        // Check if there's already an application for this email (even from previous account)
        String userEmail = user.getEmail();
        Optional<OwnerApplication> existingByEmail = ownerApplicationRepository.findByEmail(userEmail);
        
        if (existingByEmail.isPresent()) {
            OwnerApplication existing = existingByEmail.get();
            // Re-link the application to the new user account
            existing.setUser(user);
            ownerApplicationRepository.save(existing);
            
            // Update user's owner status to match the existing application
            user.setOwnerStatus(existing.getStatus());
            userRepository.save(user);
            
            throw new IllegalStateException("This email already has an owner application with status: " + existing.getStatus() + 
                                          ". Previous application from " + existing.getSubmittedAt() + " has been restored to your account.");
        }

        // Check if user already has an application (this is now redundant but kept for safety)
        if (ownerApplicationRepository.existsByUser(user)) {
            throw new IllegalStateException("User already has a pending or processed application");
        }

        // Check if user is already an owner
        if (user.getRole() == Role.OWNER) {
            throw new IllegalStateException("User is already an owner");
        }

        // Create and save application
        OwnerApplication application = new OwnerApplication(user, message);
        OwnerApplication savedApplication = ownerApplicationRepository.save(application);

        // Update user's owner status to PENDING only if it's currently NONE
        if (user.getOwnerStatus() == OwnerStatus.NONE) {
            user.setOwnerStatus(OwnerStatus.PENDING);
            userRepository.save(user);
        }

        // Send admin notification email about the new application
        try {
            String applicantName = user.getFirstName() + " " + user.getLastName();
            logger.info("=== SENDING ADMIN NOTIFICATION FOR NEW OWNER APPLICATION ===");
            logger.info("Application ID: {}", savedApplication.getId());
            logger.info("Applicant: {} ({})", applicantName, user.getEmail());
            logger.info("Application Message: {}", message);
            
            // Send simple admin notification without approval links
            emailService.sendOwnerApplicationNotificationToAdmin(applicantName, user.getEmail(), message);
            logger.info("✅ ADMIN NOTIFICATION SENT SUCCESSFULLY");
        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND ADMIN NOTIFICATION EMAIL for application {}: {}", 
                        savedApplication.getId(), e.getMessage());
            logger.error("Full error details: ", e);
            // Don't fail the application submission, but make sure the error is visible
            logger.warn("⚠️ APPLICATION SUBMITTED BUT ADMIN NOT NOTIFIED - MANUAL REVIEW REQUIRED");
        }

        return savedApplication;
    }

    /**
     * Save an owner application (used for updates/re-linking)
     */
    public OwnerApplication saveApplication(OwnerApplication application) {
        return ownerApplicationRepository.save(application);
    }

    /**
     * Get application by user
     */
    public Optional<OwnerApplication> getApplicationByUser(User user) {
        return ownerApplicationRepository.findByUser(user);
    }
    
    /**
     * Get application by email (survives account deletion/recreation)
     */
    public Optional<OwnerApplication> getApplicationByEmail(String email) {
        return ownerApplicationRepository.findByEmail(email);
    }

    /**
     * Get application by ID
     */
    public Optional<OwnerApplication> getApplicationById(Long id) {
        return ownerApplicationRepository.findById(id);
    }

    /**
     * Check if user can apply to become an owner
     * This now checks both current user status AND email history
     */
    public boolean canUserApply(User user) {
        // User cannot apply if they're already an owner
        if (user.getRole() == Role.OWNER) {
            return false;
        }
        
        // Check if there's already an application for this email (from current or previous account)
        if (ownerApplicationRepository.existsByEmail(user.getEmail())) {
            return false;
        }
        
        // User cannot apply if they already have an application (pending, approved, or rejected)
        return user.getOwnerStatus() == OwnerStatus.NONE;
    }
    
    /**
     * Get the owner status for a user, checking both current user and email history
     */
    public OwnerStatus getUserOwnerStatus(User user) {
        // First check if there's an application by email (survives account deletion)
        Optional<OwnerApplication> applicationByEmail = ownerApplicationRepository.findByEmail(user.getEmail());
        if (applicationByEmail.isPresent()) {
            OwnerApplication application = applicationByEmail.get();
            // Re-link to current user if needed
            if (application.getUser() == null || !application.getUser().getId().equals(user.getId())) {
                application.setUser(user);
                ownerApplicationRepository.save(application);
                
                // Sync user's owner status
                if (user.getOwnerStatus() != application.getStatus()) {
                    user.setOwnerStatus(application.getStatus());
                    userRepository.save(user);
                }
            }
            return application.getStatus();
        }
        
        // Fall back to user's current status
        return user.getOwnerStatus();
    }
    
    /**
     * Get all pending applications (for admin review)
     */
    public List<OwnerApplication> getPendingApplications() {
        return ownerApplicationRepository.findByStatusOrderBySubmittedAtDesc(OwnerStatus.PENDING);
    }

    /**
     * Get all applications (for admin dashboard)
     */
    public List<OwnerApplication> getAllApplications() {
        return ownerApplicationRepository.findAllByOrderBySubmittedAtDesc();
    }

    /**
     * Approve an application (admin function)
     */
    @Transactional
    public OwnerApplication approveApplication(Long applicationId, String reviewNotes) {
        OwnerApplication application = ownerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (application.getStatus() != OwnerStatus.PENDING) {
            throw new IllegalStateException("Application is not pending");
        }

        // Update application
        application.setStatus(OwnerStatus.APPROVED);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewNotes(reviewNotes);
        OwnerApplication savedApplication = ownerApplicationRepository.save(application);

        // Update user role and status
        User user = application.getUser();
        user.setRole(Role.OWNER);
        user.setOwnerStatus(OwnerStatus.APPROVED);
        userRepository.save(user);

        // Send approval email to the applicant
        try {
            String applicantName = user.getFirstName() + " " + user.getLastName();
            logger.info("=== SENDING OWNER APPLICATION APPROVAL EMAIL ===");
            logger.info("Application ID: {}", applicationId);
            logger.info("User Email: {}", user.getEmail());
            logger.info("User Name: {}", applicantName);
            
            // Check email configuration before sending
            if (!emailService.isPasswordConfigured()) {
                logger.error("❌ EMAIL CONFIGURATION INVALID! Email notifications disabled.");
                logger.error("Please check spring.mail.username and spring.mail.password in application.properties");
            } else {
                emailService.sendOwnerApplicationApprovalEmail(user.getEmail(), applicantName);
                logger.info("✅ APPROVAL EMAIL SENT SUCCESSFULLY to {}", user.getEmail());
            }
        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND APPROVAL EMAIL to {}: {}", user.getEmail(), e.getMessage(), e);
            // Don't fail the approval process, but make sure the error is visible
        }

        return savedApplication;
    }

    /**
     * Reject an application (admin function)
     */
    @Transactional
    public OwnerApplication rejectApplication(Long applicationId, String reviewNotes) {
        OwnerApplication application = ownerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (application.getStatus() != OwnerStatus.PENDING) {
            throw new IllegalStateException("Application is not pending");
        }

        // Update application
        application.setStatus(OwnerStatus.REJECTED);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewNotes(reviewNotes);
        OwnerApplication savedApplication = ownerApplicationRepository.save(application);

        // Update user status
        User user = application.getUser();
        user.setOwnerStatus(OwnerStatus.REJECTED);
        userRepository.save(user);

        // Send rejection email to the applicant
        try {
            String applicantName = user.getFirstName() + " " + user.getLastName();
            logger.info("=== SENDING OWNER APPLICATION REJECTION EMAIL ===");
            logger.info("Application ID: {}", applicationId);
            logger.info("User Email: {}", user.getEmail());
            logger.info("User Name: {}", applicantName);
            logger.info("Rejection Reason: {}", reviewNotes);
            
            // Check email configuration before sending
            if (!emailService.isPasswordConfigured()) {
                logger.error("❌ EMAIL CONFIGURATION INVALID! Email notifications disabled.");
                logger.error("Please check spring.mail.username and spring.mail.password in application.properties");
            } else {
                emailService.sendOwnerApplicationRejectionEmail(user.getEmail(), applicantName, reviewNotes);
                logger.info("✅ REJECTION EMAIL SENT SUCCESSFULLY to {}", user.getEmail());
            }
        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND REJECTION EMAIL to {}: {}", user.getEmail(), e.getMessage(), e);
            // Don't fail the rejection process, but make sure the error is visible
        }

        return savedApplication;
    }
}
