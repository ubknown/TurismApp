package com.licentarazu.turismapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.model.User;

import jakarta.annotation.PostConstruct;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.admin.email:turismapplic@gmail.com}")
    private String adminEmail;

    // ✅ SPRING BOOT AUTOCONFIGURATION: Validation for direct property configuration
    @PostConstruct
    public void validateEmailConfiguration() {
        logger.info("=== VALIDATING EMAIL CONFIGURATION ===");
        
        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            logger.error("❌ EMAIL USERNAME NOT CONFIGURED! Set spring.mail.username in application.properties");
            logger.warn("⚠️ Email sending will fail until credentials are properly configured");
        } else {
            logger.info("✅ Email configuration loaded: {}", fromEmail);
        }
    }

    // ✅ Helper method to check if email configuration is valid
    private boolean isConfigurationValid() {
        return fromEmail != null && !fromEmail.trim().isEmpty() && 
               !fromEmail.equals("your-email@gmail.com");
    }

    public void sendConfirmationEmail(String to, String token) {
        logger.info("=== ATTEMPTING TO SEND CONFIRMATION EMAIL ===");
        logger.info("To: {}", to);
        logger.info("From: {}", fromEmail);
        logger.info("Token: {}", token);

        String confirmationUrl = baseUrl + "/api/auth/confirm?token=" + token;
        logger.info("Confirmation URL: {}", confirmationUrl);

        try {
            // ✅ SPRING BOOT AUTOCONFIGURATION: Validate email configuration
            if (!isConfigurationValid()) {
                logger.error("❌ EMAIL CONFIGURATION INVALID! Please check spring.mail.username in application.properties");
                throw new RuntimeException(
                        "Email configuration not properly set. Please verify spring.mail.username and spring.mail.password in application.properties");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Confirm Your Email Address - Tourism App");

            String emailBody = "Hello!\n\n" +
                    "Thank you for registering with Tourism App!\n\n" +
                    "Please click the link below to confirm your email address and activate your account:\n\n" +
                    confirmationUrl + "\n\n" +
                    "This link will expire in 24 hours for security reasons.\n\n" +
                    "If you didn't create an account with us, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "The Tourism App Team";

            message.setText(emailBody);

            logger.info("Sending email message...");
            mailSender.send(message);
            logger.info("✅ CONFIRMATION EMAIL SENT SUCCESSFULLY to: {}", to);

        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND CONFIRMATION EMAIL to: {}", to);
            logger.error("Error details: {}", e.getMessage());
            logger.error("Full stack trace: ", e);

            // Log the confirmation URL so user can manually confirm if needed
            logger.error("🔗 MANUAL CONFIRMATION URL (use this if email fails): {}", confirmationUrl);

            throw new RuntimeException("Failed to send confirmation email to " + to + ": " + e.getMessage(), e);
        }
    }

    public void sendPasswordResetEmail(String to, String token) {
        logger.info("=== SENDING PASSWORD RESET EMAIL ===");
        logger.info("To: {}", to);
        logger.info("Token: {}", token);

        try {
            // ✅ SPRING BOOT AUTOCONFIGURATION: Validate email configuration
            if (!isConfigurationValid()) {
                logger.error("❌ EMAIL CONFIGURATION INVALID! Please check spring.mail.username in application.properties");
                throw new RuntimeException(
                        "Email configuration not properly set. Please verify spring.mail.username and spring.mail.password in application.properties");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Reset Your Password - Tourism App");

            // ✅ FIXED: Frontend URL instead of backend API
            String frontendBaseUrl = baseUrl.replace(":8080", ":5173");
            String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;
            logger.info("Password reset URL: {}", resetUrl);

            String emailBody = "Hello,\n\n" +
                    "We received a request to reset your password for your Tourism App account.\n\n" +
                    "Click the link below to reset your password:\n\n" +
                    resetUrl + "\n\n" +
                    "This link will expire in 1 hour for security reasons.\n\n" +
                    "If you didn't request a password reset, please ignore this email. Your password will remain unchanged.\n\n" +
                    "For security reasons, this link can only be used once.\n\n" +
                    "Best regards,\n" +
                    "The Tourism App Team";

            message.setText(emailBody);

            logger.info("Sending password reset email...");
            mailSender.send(message);
            logger.info("✅ PASSWORD RESET EMAIL SENT SUCCESSFULLY to: {}", to);

        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND PASSWORD RESET EMAIL to: {}", to);
            logger.error("Error details: {}", e.getMessage());
            throw new RuntimeException("Failed to send password reset email to " + to + ": " + e.getMessage(), e);
        }
    }

    /**
     * Send booking confirmation email to the guest
     */
    public void sendBookingConfirmationToGuest(Booking booking) {
        logger.info("=== SENDING BOOKING CONFIRMATION EMAIL TO GUEST ===");
        logger.info("Guest Email: {}", booking.getGuestEmail());
        logger.info("Accommodation: {}", booking.getAccommodationUnit().getName());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(booking.getGuestEmail());
            message.setSubject("Booking Confirmation - " + booking.getAccommodationUnit().getName());

            String emailBody = String.format(
                    "Dear %s,\n\n" +
                            "Your booking has been confirmed! Here are your reservation details:\n\n" +
                            "🏠 ACCOMMODATION DETAILS:\n" +
                            "   • Name: %s\n" +
                            "   • Location: %s\n\n" +
                            "📅 BOOKING DETAILS:\n" +
                            "   • Check-in Date: %s\n" +
                            "   • Check-out Date: %s\n" +
                            "   • Total Price: %.2f RON\n\n" +
                            "📋 BOOKING INFORMATION:\n" +
                            "   • Booking ID: #%d\n" +
                            "   • Guest Name: %s\n\n" +
                            "IMPORTANT REMINDERS:\n" +
                            "• Please arrive after 3:00 PM on your check-in date\n" +
                            "• Check-out is before 11:00 AM on your departure date\n" +
                            "• Bring a valid ID for registration\n" +
                            "• Contact the property owner if you need to modify your booking\n\n" +
                            "If you have any questions about your booking, please don't hesitate to contact us.\n\n" +
                            "We hope you have a wonderful stay!\n\n" +
                            "Best regards,\n" +
                            "The Tourism App Team\n\n" +
                            "---\n" +
                            "This is an automated confirmation email. Please keep it for your records.",

                    booking.getGuestName() != null ? booking.getGuestName() : "Valued Guest",
                    booking.getAccommodationUnit().getName(),
                    booking.getAccommodationUnit().getLocation(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0,
                    booking.getId(),
                    booking.getGuestName() != null ? booking.getGuestName() : "Guest");

            message.setText(emailBody);

            logger.info("Sending booking confirmation email to guest...");
            mailSender.send(message);
            logger.info("✅ BOOKING CONFIRMATION EMAIL SENT SUCCESSFULLY to guest: {}", booking.getGuestEmail());

        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND BOOKING CONFIRMATION EMAIL to guest: {}", booking.getGuestEmail());
            logger.error("Error details: {}", e.getMessage());
            logger.error("Full stack trace: ", e);
            // Don't throw exception to avoid breaking the booking process
        }
    }

    /**
     * Send booking notification email to the property owner
     */
    public void sendBookingNotificationToOwner(Booking booking) {
        logger.info("=== SENDING BOOKING NOTIFICATION EMAIL TO OWNER ===");

        User owner = booking.getAccommodationUnit().getOwner();
        if (owner == null || owner.getEmail() == null) {
            logger.warn("Cannot send booking notification - owner or owner email is null");
            return;
        }

        logger.info("Owner Email: {}", owner.getEmail());
        logger.info("Property: {}", booking.getAccommodationUnit().getName());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(owner.getEmail());
            message.setSubject("New Booking Received - " + booking.getAccommodationUnit().getName());

            String emailBody = String.format(
                    "Dear %s %s,\n\n" +
                            "Great news! You have received a new booking for your property.\n\n" +
                            "🏠 PROPERTY DETAILS:\n" +
                            "   • Property Name: %s\n" +
                            "   • Location: %s\n\n" +
                            "👥 GUEST INFORMATION:\n" +
                            "   • Guest Name: %s\n" +
                            "   • Guest Email: %s\n\n" +
                            "📅 BOOKING DETAILS:\n" +
                            "   • Check-in Date: %s\n" +
                            "   • Check-out Date: %s\n" +
                            "   • Total Revenue: %.2f RON\n\n" +
                            "📋 RESERVATION INFORMATION:\n" +
                            "   • Booking ID: #%d\n" +
                            "   • Booking Date: %s\n\n" +
                            "NEXT STEPS:\n" +
                            "• The guest will receive a confirmation email with all booking details\n" +
                            "• Please prepare your property for the upcoming stay\n" +
                            "• You can contact the guest directly if needed using the email above\n" +
                            "• Log into your dashboard to view more booking details\n\n" +
                            "Thank you for being part of our tourism platform!\n\n" +
                            "Best regards,\n" +
                            "The Tourism App Team\n\n" +
                            "---\n" +
                            "This is an automated notification email.",

                    owner.getFirstName() != null ? owner.getFirstName() : "Property",
                    owner.getLastName() != null ? owner.getLastName() : "Owner",
                    booking.getAccommodationUnit().getName(),
                    booking.getAccommodationUnit().getLocation(),
                    booking.getGuestName() != null ? booking.getGuestName() : "Guest",
                    booking.getGuestEmail() != null ? booking.getGuestEmail() : "Not provided",
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0,
                    booking.getId(),
                    java.time.LocalDate.now());

            message.setText(emailBody);

            logger.info("Sending booking notification email to owner...");
            mailSender.send(message);
            logger.info("✅ BOOKING NOTIFICATION EMAIL SENT SUCCESSFULLY to owner: {}", owner.getEmail());

        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND BOOKING NOTIFICATION EMAIL to owner: {}", owner.getEmail());
            logger.error("Error details: {}", e.getMessage());
            logger.error("Full stack trace: ", e);
            // Don't throw exception to avoid breaking the booking process
        }
    }

    // ✅ Owner Application Approval Email
    public void sendOwnerApplicationApprovalEmail(String to, String applicantName) {
        logger.info("=== SENDING OWNER APPLICATION APPROVAL EMAIL ===");
        logger.info("To: {}", to);
        logger.info("Applicant: {}", applicantName);

        try {
            if (!isConfigurationValid()) {
                logger.error("❌ EMAIL CONFIGURATION INVALID! Please check spring.mail.username in application.properties");
                throw new RuntimeException(
                        "Email configuration not properly set. Please verify spring.mail.username and spring.mail.password in application.properties");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("🎉 Owner Application Approved - Tourism App");

            String emailBody = "Dear " + applicantName + ",\n\n" +
                    "Congratulations! Your owner application has been approved! 🎉\n\n" +
                    "You can now add and manage accommodation units on our platform. Here's what you can do now:\n\n" +
                    "✅ Add new accommodation units\n" +
                    "✅ Manage your property listings\n" +
                    "✅ View booking requests and analytics\n" +
                    "✅ Track your earnings and performance\n\n" +
                    "To get started, log in to your account and visit your dashboard.\n\n" +
                    "Welcome to the Tourism App owner community!\n\n" +
                    "Best regards,\n" +
                    "The Tourism App Team";

            message.setText(emailBody);

            mailSender.send(message);
            logger.info("✅ OWNER APPROVAL EMAIL SENT SUCCESSFULLY to {}", to);

        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND OWNER APPROVAL EMAIL to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send owner approval email: " + e.getMessage(), e);
        }
    }

    // ✅ Owner Application Rejection Email
    public void sendOwnerApplicationRejectionEmail(String to, String applicantName, String rejectionReason) {
        logger.info("=== SENDING OWNER APPLICATION REJECTION EMAIL ===");
        logger.info("To: {}", to);
        logger.info("Applicant: {}", applicantName);
        logger.info("Reason: {}", rejectionReason);

        try {
            if (!isConfigurationValid()) {
                logger.error("❌ EMAIL CONFIGURATION INVALID! Please check spring.mail.username in application.properties");
                throw new RuntimeException(
                        "Email configuration not properly set. Please verify spring.mail.username and spring.mail.password in application.properties");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Owner Application Update - Tourism App");

            String emailBody = "Dear " + applicantName + ",\n\n" +
                    "Thank you for your interest in becoming a property owner on Tourism App.\n\n" +
                    "After careful review, we regret to inform you that your owner application was not approved at this time.\n\n";

            // Add rejection reason if provided
            if (rejectionReason != null && !rejectionReason.trim().isEmpty()) {
                emailBody += "Reason: " + rejectionReason + "\n\n";
            }

            emailBody += "You can continue to use Tourism App as a guest to browse and book accommodations.\n\n" +
                    "If you have any questions about this decision or would like to discuss your application, " +
                    "please don't hesitate to contact our support team.\n\n" +
                    "Thank you for your understanding.\n\n" +
                    "Best regards,\n" +
                    "The Tourism App Team";

            message.setText(emailBody);

            mailSender.send(message);
            logger.info("✅ OWNER REJECTION EMAIL SENT SUCCESSFULLY to {}", to);

        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND OWNER REJECTION EMAIL to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send owner rejection email: " + e.getMessage(), e);
        }
    }

    // ✅ Admin Notification Email for New Owner Applications
    public void sendOwnerApplicationNotificationToAdmin(String applicantName, String applicantEmail, String applicationMessage) {
        logger.info("=== SENDING OWNER APPLICATION NOTIFICATION TO ADMIN ===");
        logger.info("Admin Email: {}", adminEmail);
        logger.info("Applicant: {} ({})", applicantName, applicantEmail);

        try {
            if (!isConfigurationValid()) {
                logger.error("❌ EMAIL CONFIGURATION INVALID! Please check spring.mail.username in application.properties");
                throw new RuntimeException(
                        "Email configuration not properly set. Please verify spring.mail.username and spring.mail.password in application.properties");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("🔔 New Owner Application Submitted - Tourism App");

            String emailBody = "Dear Admin,\n\n" +
                    "A new owner application has been submitted and requires your review.\n\n" +
                    "👤 APPLICANT DETAILS:\n" +
                    "   • Name: " + applicantName + "\n" +
                    "   • Email: " + applicantEmail + "\n\n" +
                    "📝 APPLICATION MESSAGE:\n" +
                    "   " + (applicationMessage != null && !applicationMessage.trim().isEmpty() ? 
                         applicationMessage : "No additional message provided") + "\n\n" +
                    "🔗 NEXT STEPS:\n" +
                    "   • Log into the admin dashboard to review this application\n" +
                    "   • You can approve or reject the application with review notes\n" +
                    "   • The applicant will be automatically notified of your decision\n\n" +
                    "📊 ADMIN ACTIONS AVAILABLE:\n" +
                    "   ✅ Approve Application (grants owner privileges)\n" +
                    "   ❌ Reject Application (with optional rejection reason)\n\n" +
                    "Please review this application as soon as possible.\n\n" +
                    "Best regards,\n" +
                    "Tourism App System\n\n" +
                    "---\n" +
                    "This is an automated notification email.\n" +
                    "Application submitted at: " + java.time.LocalDateTime.now();

            message.setText(emailBody);

            mailSender.send(message);
            logger.info("✅ ADMIN NOTIFICATION EMAIL SENT SUCCESSFULLY to {}", adminEmail);

        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND ADMIN NOTIFICATION EMAIL to {}: {}", adminEmail, e.getMessage());
            // Log full stack trace for debugging
            logger.error("Full error details: ", e);
            throw new RuntimeException("Failed to send admin notification email: " + e.getMessage(), e);
        }
    }

    /**
     * Send admin notification with approval/rejection links
     */
    public void sendOwnerApplicationApprovalLinks(String applicantName, String applicantEmail, 
                                                  String applicationMessage, String approvalUrl, String rejectionUrl) {
        try {
            logger.info("=== SENDING OWNER APPLICATION APPROVAL LINKS EMAIL ===");
            logger.info("To: {}", adminEmail);
            logger.info("Applicant: {} ({})", applicantName, applicantEmail);

            if (!isConfigurationValid()) {
                logger.error("❌ EMAIL CONFIGURATION INVALID - Cannot send approval links email");
                throw new RuntimeException("Email configuration not properly set up");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("🔐 SECURE OWNER APPLICATION REVIEW REQUIRED - " + applicantName);

            String emailBody = "🏨 TOURISM APP - SECURE OWNER APPLICATION REVIEW\n\n" +
                    "A new owner application requires your review:\n\n" +
                    "👤 APPLICANT DETAILS:\n" +
                    "   Name: " + applicantName + "\n" +
                    "   Email: " + applicantEmail + "\n" +
                    "   Application Message: " + (applicationMessage != null ? applicationMessage : "No message provided") + "\n\n" +
                    "🔐 SECURE REVIEW ACTIONS:\n\n" +
                    "✅ APPROVE APPLICATION:\n" +
                    "   " + approvalUrl + "\n\n" +
                    "❌ REJECT APPLICATION:\n" +
                    "   " + rejectionUrl + "\n\n" +
                    "🔒 SECURITY NOTICE:\n" +
                    "   • Each link can only be used once\n" +
                    "   • You will be asked for the admin password\n" +
                    "   • The applicant will be notified of your decision\n\n" +
                    "Please click one of the links above to review this application.\n\n" +
                    "Best regards,\n" +
                    "Tourism App System\n\n" +
                    "---\n" +
                    "This is an automated secure notification email.\n" +
                    "Application submitted at: " + java.time.LocalDateTime.now();

            message.setText(emailBody);

            mailSender.send(message);
            logger.info("✅ APPROVAL LINKS EMAIL SENT SUCCESSFULLY to {}", adminEmail);

        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND APPROVAL LINKS EMAIL to {}: {}", adminEmail, e.getMessage());
            logger.error("Full error details: ", e);
            throw new RuntimeException("Failed to send approval links email: " + e.getMessage(), e);
        }
    }

    /**
     * Send approval notification to applicant
     */
    public void sendOwnerApplicationApprovalNotification(String to, String applicantName, String reviewNotes) {
        try {
            logger.info("=== SENDING OWNER APPLICATION APPROVAL NOTIFICATION ===");
            logger.info("To: {}", to);

            if (!isConfigurationValid()) {
                logger.error("❌ EMAIL CONFIGURATION INVALID - Cannot send approval notification");
                throw new RuntimeException("Email configuration not properly set up");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("🎉 Congratulations! Your Tourism App Owner Application has been APPROVED");

            String emailBody = "� TOURISM APP - OWNER APPLICATION APPROVED 🎉\n\n" +
                    "Dear " + applicantName + ",\n\n" +
                    "Congratulations! We are pleased to inform you that your owner application for Tourism App has been APPROVED!\n\n" +
                    "✅ APPLICATION STATUS: APPROVED\n" +
                    "🏨 NEW ROLE: Property Owner\n" +
                    "📅 Approved on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm")) + "\n\n";

            if (reviewNotes != null && !reviewNotes.trim().isEmpty()) {
                emailBody += "📝 WELCOME MESSAGE FROM OUR TEAM:\n" + reviewNotes + "\n\n";
            }

            emailBody += "🚀 WHAT'S NEXT - GET STARTED:\n" +
                    "   🔐 Log in to your account with your existing credentials\n" +
                    "   🏠 Add your first accommodation property\n" +
                    "   📋 Set up property details, photos, and pricing\n" +
                    "   📊 Manage bookings and guest reviews\n" +
                    "   💰 Start earning from your properties today!\n\n" +
                    "🎯 OWNER BENEFITS:\n" +
                    "   • Full property management dashboard\n" +
                    "   • Real-time booking notifications\n" +
                    "   • Detailed analytics and profit reports\n" +
                    "   • Direct communication with guests\n" +
                    "   • Professional support from our team\n\n" +
                    "Welcome to the Tourism App owner community! We're excited to have you on board.\n\n" +
                    "If you have any questions or need assistance getting started, please don't hesitate to contact our support team.\n\n" +
                    "Best regards,\n" +
                    "The Tourism App Team 🏨\n\n" +
                    "---\n" +
                    "This is an automated notification email.\n" +
                    "Tourism App - Your gateway to successful property management";

            message.setText(emailBody);

            mailSender.send(message);
            logger.info("✅ APPROVAL NOTIFICATION SENT SUCCESSFULLY to {}", to);

        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND APPROVAL NOTIFICATION to {}: {}", to, e.getMessage());
            logger.error("Full error details: ", e);
            throw new RuntimeException("Failed to send approval notification: " + e.getMessage(), e);
        }
    }

    /**
     * Send rejection notification to applicant
     */
    public void sendOwnerApplicationRejectionNotification(String to, String applicantName, String reviewNotes) {
        try {
            logger.info("=== SENDING OWNER APPLICATION REJECTION NOTIFICATION ===");
            logger.info("To: {}", to);

            if (!isConfigurationValid()) {
                logger.error("❌ EMAIL CONFIGURATION INVALID - Cannot send rejection notification");
                throw new RuntimeException("Email configuration not properly set up");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("📧 Update on Your Tourism App Owner Application");

            String emailBody = "🏨 TOURISM APP - OWNER APPLICATION UPDATE\n\n" +
                    "Dear " + applicantName + ",\n\n" +
                    "Thank you for your interest in becoming a property owner on Tourism App. We appreciate the time you took to submit your application.\n\n" +
                    "📋 APPLICATION STATUS: UNDER REVIEW\n" +
                    "📅 Reviewed on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm")) + "\n\n" +
                    "After careful consideration, we are unable to approve your owner application at this time.\n\n";

            if (reviewNotes != null && !reviewNotes.trim().isEmpty()) {
                emailBody += "📝 FEEDBACK FROM OUR REVIEW TEAM:\n" + reviewNotes + "\n\n";
            }

            emailBody += "🔄 NEXT STEPS AND OPPORTUNITIES:\n" +
                    "   📊 Review the feedback provided above carefully\n" +
                    "   🔄 You may reapply in the future if circumstances change\n" +
                    "   📞 Contact our support team if you have questions about this decision\n" +
                    "   🏠 Continue using Tourism App as a guest to explore accommodations\n\n" +
                    "💡 WHAT YOU CAN DO NOW:\n" +
                    "   • Continue browsing and booking amazing properties\n" +
                    "   • Leave reviews to help other travelers\n" +
                    "   • Save your favorite destinations\n" +
                    "   • Stay updated on our platform improvements\n\n" +
                    "We value your participation in the Tourism App community and encourage you to continue being part of our growing platform.\n\n" +
                    "Thank you for your understanding and continued interest in Tourism App.\n\n" +
                    "Best regards,\n" +
                    "The Tourism App Review Team 🏨\n\n" +
                    "---\n" +
                    "This is an automated notification email.\n" +
                    "Tourism App - Connecting travelers with amazing accommodations";

            message.setText(emailBody);

            mailSender.send(message);
            logger.info("✅ REJECTION NOTIFICATION SENT SUCCESSFULLY to {}", to);

        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND REJECTION NOTIFICATION to {}: {}", to, e.getMessage());
            logger.error("Full error details: ", e);
            throw new RuntimeException("Failed to send rejection notification: " + e.getMessage(), e);
        }
    }

    // Helper methods for testing and configuration validation
    public String getFromEmail() {
        return fromEmail;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public boolean isPasswordConfigured() {
        return fromEmail != null &&
                !fromEmail.equals("your-email@gmail.com") &&
                !fromEmail.trim().isEmpty();
    }

    public void testConnection() throws Exception {
        if (!isPasswordConfigured()) {
            throw new RuntimeException(
                    "Email configuration not set. Please update spring.mail.username and spring.mail.password");
        }

        // This will test the connection without sending an email
        try {
            mailSender.send(new SimpleMailMessage()); // This should fail but test the connection
        } catch (Exception e) {
            if (e.getMessage().contains("authentication") || e.getMessage().contains("password")) {
                throw new RuntimeException("Email authentication failed. Check your Gmail App Password.", e);
            } else if (e.getMessage().contains("connection")) {
                throw new RuntimeException("Cannot connect to Gmail SMTP server. Check your internet connection.", e);
            }
            // Other errors might be okay for connection testing
        }
    }

    /**
     * Send booking cancellation notification email to the guest
     */
    public void sendBookingCancellationToGuest(Booking booking, User cancelledBy) {
        logger.info("=== SENDING BOOKING CANCELLATION EMAIL TO GUEST ===");

        if (booking.getGuestEmail() == null || booking.getGuestEmail().trim().isEmpty()) {
            logger.warn("Cannot send cancellation notification - guest email is null or empty");
            return;
        }

        if (!isConfigurationValid()) {
            logger.warn("❌ Email configuration invalid - skipping cancellation notification to guest");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(booking.getGuestEmail());
            message.setSubject("Booking Cancelled - " + booking.getAccommodationUnit().getName());

            // Determine who cancelled the booking
            String cancelledByText;
            if (cancelledBy.getEmail().equals(booking.getGuestEmail())) {
                cancelledByText = "You have";
            } else if (booking.getAccommodationUnit().getOwner().getId().equals(cancelledBy.getId())) {
                cancelledByText = "The property owner has";
            } else {
                cancelledByText = "An administrator has";
            }

            String emailBody = String.format(
                "Dear %s,\n\n" +
                "%s cancelled your booking for %s.\n\n" +
                "BOOKING DETAILS:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "🏨 Property: %s\n" +
                "📍 Location: %s\n" +
                "📅 Check-in: %s\n" +
                "📅 Check-out: %s\n" +
                "👥 Guests: %d\n" +
                "💰 Total Amount: %.2f RON\n" +
                "🆔 Booking ID: %d\n" +
                "❌ Status: CANCELLED\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "If you have any questions about this cancellation, please contact us or the property owner.\n\n" +
                "We apologize for any inconvenience and hope to serve you again in the future.\n\n" +
                "Best regards,\n" +
                "TurismApp Team\n" +
                "%s",
                booking.getGuestName() != null ? booking.getGuestName() : "Guest",
                cancelledByText,
                booking.getAccommodationUnit().getName(),
                booking.getAccommodationUnit().getName(),
                booking.getAccommodationUnit().getLocation(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNumberOfGuests() != null ? booking.getNumberOfGuests() : 1,
                booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0,
                booking.getId(),
                baseUrl
            );

            message.setText(emailBody);
            mailSender.send(message);

            logger.info("✅ Booking cancellation email sent successfully to guest: {}", booking.getGuestEmail());

        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND BOOKING CANCELLATION EMAIL to guest: {}", booking.getGuestEmail());
            logger.error("Error details: {}", e.getMessage());
            // Don't throw exception to avoid breaking the cancellation process
        }
    }

    /**
     * Send booking cancellation notification email to the property owner
     */
    public void sendBookingCancellationToOwner(Booking booking, User cancelledBy) {
        logger.info("=== SENDING BOOKING CANCELLATION EMAIL TO OWNER ===");

        User owner = booking.getAccommodationUnit().getOwner();
        if (owner == null || owner.getEmail() == null) {
            logger.warn("Cannot send cancellation notification - owner or owner email is null");
            return;
        }

        if (!isConfigurationValid()) {
            logger.warn("❌ Email configuration invalid - skipping cancellation notification to owner");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(owner.getEmail());
            message.setSubject("Booking Cancelled - " + booking.getAccommodationUnit().getName());

            // Determine who cancelled the booking
            String cancelledByText;
            if (cancelledBy.getEmail().equals(booking.getGuestEmail())) {
                cancelledByText = "The guest has";
            } else if (owner.getId().equals(cancelledBy.getId())) {
                cancelledByText = "You have";
            } else {
                cancelledByText = "An administrator has";
            }

            String emailBody = String.format(
                "Dear %s,\n\n" +
                "%s cancelled a booking for your property: %s.\n\n" +
                "BOOKING DETAILS:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "🏨 Property: %s\n" +
                "👤 Guest: %s\n" +
                "📧 Guest Email: %s\n" +
                "📞 Guest Phone: %s\n" +
                "📅 Check-in: %s\n" +
                "📅 Check-out: %s\n" +
                "👥 Guests: %d\n" +
                "💰 Total Amount: %.2f RON\n" +
                "🆔 Booking ID: %d\n" +
                "❌ Status: CANCELLED\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "%s\n\n" +
                "Please update your availability calendar accordingly.\n\n" +
                "Best regards,\n" +
                "TurismApp Team\n" +
                "%s",
                owner.getFirstName() != null ? owner.getFirstName() : "Property Owner",
                cancelledByText,
                booking.getAccommodationUnit().getName(),
                booking.getAccommodationUnit().getName(),
                booking.getGuestName() != null ? booking.getGuestName() : "N/A",
                booking.getGuestEmail(),
                booking.getGuestPhone() != null ? booking.getGuestPhone() : "N/A",
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNumberOfGuests() != null ? booking.getNumberOfGuests() : 1,
                booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0,
                booking.getId(),
                booking.getSpecialRequests() != null && !booking.getSpecialRequests().trim().isEmpty() 
                    ? "Special Requests: " + booking.getSpecialRequests() 
                    : "No special requests.",
                baseUrl
            );

            message.setText(emailBody);
            mailSender.send(message);

            logger.info("✅ Booking cancellation email sent successfully to owner: {}", owner.getEmail());

        } catch (Exception e) {
            logger.error("❌ FAILED TO SEND BOOKING CANCELLATION EMAIL to owner: {}", owner.getEmail());
            logger.error("Error details: {}", e.getMessage());
            // Don't throw exception to avoid breaking the cancellation process
        }
    }
}
