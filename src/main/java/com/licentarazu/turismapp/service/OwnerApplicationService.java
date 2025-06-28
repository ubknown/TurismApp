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
        // Check if user already has an application
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
            
            emailService.sendOwnerApplicationNotificationToAdmin(applicantName, user.getEmail(), message);
            logger.info("✅ ADMIN NOTIFICATION EMAIL SENT SUCCESSFULLY");
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
     * Get application by user
     */
    public Optional<OwnerApplication> getApplicationByUser(User user) {
        return ownerApplicationRepository.findByUser(user);
    }

    /**
     * Check if user can apply to become an owner
     */
    public boolean canUserApply(User user) {
        // User cannot apply if they're already an owner
        if (user.getRole() == Role.OWNER) {
            return false;
        }
        
        // User cannot apply if they already have an application (pending, approved, or rejected)
        return user.getOwnerStatus() == OwnerStatus.NONE;
    }

    /**
     * Get all pending applications (for admin review)
     */
    public List<OwnerApplication> getPendingApplications() {
        return ownerApplicationRepository.findByStatusOrderBySubmittedAtDesc(OwnerStatus.PENDING);
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

    /**
     * Get all applications (for admin)
     */
    public List<OwnerApplication> getAllApplications() {
        return ownerApplicationRepository.findAll();
    }
}
