package com.licentarazu.turismapp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.licentarazu.turismapp.dto.ForgotPasswordRequest;
import com.licentarazu.turismapp.dto.LoginRequest;
import com.licentarazu.turismapp.dto.RegisterRequest;
import com.licentarazu.turismapp.dto.ResetPasswordRequest;
import com.licentarazu.turismapp.dto.UserResponseDTO;
import com.licentarazu.turismapp.model.ConfirmationToken;
import com.licentarazu.turismapp.model.OwnerStatus;
import com.licentarazu.turismapp.model.PasswordResetToken;
import com.licentarazu.turismapp.model.Role;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.UserRepository;
import com.licentarazu.turismapp.security.JwtUtil;
import com.licentarazu.turismapp.service.ConfirmationTokenService;
import com.licentarazu.turismapp.service.EmailService;
import com.licentarazu.turismapp.service.OwnerApplicationService;
import com.licentarazu.turismapp.service.PasswordResetService;
import com.licentarazu.turismapp.service.UserService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ConfirmationTokenService confirmationTokenService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private OwnerApplicationService ownerApplicationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Check if user already exists by email
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                
                // If user exists but is not enabled, resend confirmation email
                if (!user.getEnabled()) {
                    // Delete old confirmation tokens for this user
                    confirmationTokenService.deleteTokensForUser(user);
                    
                    // Create new confirmation token and send email
                    ConfirmationToken confirmationToken = confirmationTokenService.createConfirmationToken(user);
                    
                    try {
                        emailService.sendConfirmationEmail(user.getEmail(), confirmationToken.getToken());
                        
                        return ResponseEntity.ok(Map.of(
                            "message", "Email already registered but not confirmed. A new confirmation email has been sent.",
                            "email", user.getEmail(),
                            "instruction", "Please check your email and click the confirmation link."
                        ));
                    } catch (Exception emailError) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of(
                                "error", "Email delivery failed",
                                "message", "Failed to send confirmation email. Please try again later.",
                                "details", emailError.getMessage()
                            ));
                    }
                } else {
                    // User exists and is already enabled
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                            "error", "Conflict",
                            "message", "Email already registered and confirmed",
                            "field", "email"
                        ));
                }
            }

            // Create new user (inactive by default)
            User user = new User();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setEnabled(false); // User must confirm email first
            
            // Handle role and owner status based on user selection
            try {
                Role selectedRole = Role.valueOf(request.getRole().toUpperCase());
                
                if (selectedRole == Role.OWNER) {
                    // If user wants to be an owner, create as GUEST with PENDING status
                    user.setRole(Role.GUEST);
                    user.setOwnerStatus(OwnerStatus.PENDING);
                } else {
                    // Regular guest registration
                    user.setRole(Role.GUEST);
                    user.setOwnerStatus(OwnerStatus.NONE);
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Validation failed",
                        "message", "Invalid role specified",
                        "details", new String[]{"Role must be either GUEST or OWNER"}
                    ));
            }

            // Register user (password will be hashed in service)
            User savedUser = userService.registerUser(user);

            // If user selected OWNER during registration, create an owner application
            if (request.getRole().toUpperCase().equals("OWNER")) {
                try {
                    ownerApplicationService.submitApplication(savedUser, "Automatic application created during registration");
                } catch (Exception e) {
                    // Log the error but don't fail registration
                    System.err.println("Failed to create owner application for user " + savedUser.getEmail() + ": " + e.getMessage());
                }
            }

            // Create confirmation token and send email
            ConfirmationToken confirmationToken = confirmationTokenService.createConfirmationToken(savedUser);
            
            try {
                emailService.sendConfirmationEmail(savedUser.getEmail(), confirmationToken.getToken());
                
                // Create response
                UserResponseDTO userResponse = new UserResponseDTO();
                userResponse.setId(savedUser.getId());
                userResponse.setFirstName(savedUser.getFirstName());
                userResponse.setLastName(savedUser.getLastName());
                userResponse.setEmail(savedUser.getEmail());
                userResponse.setCreatedAt(savedUser.getCreatedAt());
                userResponse.setRole(savedUser.getRole().name());
                userResponse.setOwnerStatus(savedUser.getOwnerStatus());

                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "message", "Registration successful! Please check your email to confirm your account.",
                        "user", userResponse,
                        "instruction", "Check your email inbox and click the confirmation link to activate your account."
                    ));
            } catch (Exception emailError) {
                // User was created but email failed - provide manual resend option
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body(Map.of(
                        "message", "Registration successful but confirmation email failed to send.",
                        "email", savedUser.getEmail(),
                        "instruction", "Use the /api/auth/resend-confirmation endpoint to resend the email.",
                        "error", "Email delivery failed: " + emailError.getMessage()
                    ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", "Registration failed",
                    "message", e.getMessage()
                ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            String token = jwtUtil.generateToken(userDetails.getUsername());
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            
            // ✅ CRITICAL FIX: Null pointer protection
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "error", "Authentication failed",
                        "message", "User not found"
                    ));
            }
            
            // Check if user account is enabled
            if (!user.getEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "error", "Account not confirmed",
                        "message", "Please confirm your email address before logging in"
                    ));
            }
            
            // Create user response DTO with null safety
            UserResponseDTO userResponse = new UserResponseDTO();
            userResponse.setId(user.getId());
            userResponse.setFirstName(user.getFirstName());
            userResponse.setLastName(user.getLastName());
            userResponse.setEmail(user.getEmail());
            userResponse.setRole(user.getRole().name());
            userResponse.setOwnerStatus(user.getOwnerStatus());
            
            return ResponseEntity.ok(Map.of(
                "token", token,
                "user", userResponse
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "error", "Authentication failed",
                    "message", "Invalid email or password"
                ));
        }
    }

    @GetMapping("/confirm")
    public RedirectView confirmEmail(@RequestParam("token") String token) {
        try {
            Optional<ConfirmationToken> confirmationToken = confirmationTokenService.getToken(token);
            
            if (confirmationToken.isEmpty()) {
                // Redirect to frontend with error parameter
                return new RedirectView("http://localhost:5173/email-confirmed?error=invalid-token");
            }
            
            ConfirmationToken confToken = confirmationToken.get();
            
            if (confToken.isExpired()) {
                // Redirect to frontend with error parameter
                return new RedirectView("http://localhost:5173/email-confirmed?error=expired-token");
            }
            
            if (confToken.isConfirmed()) {
                // Redirect to frontend with error parameter
                return new RedirectView("http://localhost:5173/email-confirmed?error=already-confirmed");
            }
            
            // Confirm the token and enable the user
            confirmationTokenService.confirmToken(confToken);
            User user = confToken.getUser();
            user.setEnabled(true);
            userRepository.save(user);
            
            // Redirect to frontend success page
            return new RedirectView("http://localhost:5173/email-confirmed?success=true");
            
        } catch (Exception e) {
            // Redirect to frontend with error parameter
            return new RedirectView("http://localhost:5173/email-confirmed?error=server-error");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            
            // Always return success message for security (don't reveal if email exists)
            String successMessage = "If an account with that email exists, we've sent a password reset link.";
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Create password reset token
                PasswordResetToken resetToken = passwordResetService.createPasswordResetToken(user);
                
                // Send password reset email
                emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());
            }
            
            return ResponseEntity.ok(Map.of(
                "message", successMessage
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Password reset failed",
                    "message", "An error occurred while processing your request"
                ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            // Validate passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Passwords don't match",
                        "message", "New password and confirm password must match"
                    ));
            }
            
            // Get and validate token
            Optional<PasswordResetToken> tokenOpt = passwordResetService.getPasswordResetToken(request.getToken());
            
            if (tokenOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Invalid token",
                        "message", "The password reset token is invalid"
                    ));
            }
            
            PasswordResetToken resetToken = tokenOpt.get();
            
            if (!resetToken.isValid()) {
                String message = resetToken.isExpired() ? 
                    "The password reset token has expired. Please request a new one." :
                    "This password reset token has already been used";
                    
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Token invalid",
                        "message", message
                    ));
            }
            
            // Update user password
            User user = resetToken.getUser();
            user.setPassword(request.getNewPassword()); // Will be hashed by UserService
            userService.updateUserPassword(user, request.getNewPassword());
            
            // Mark token as used
            passwordResetService.validateAndUseToken(request.getToken());
            
            return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully! You can now log in with your new password."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Password reset failed",
                    "message", "An error occurred while resetting your password"
                ));
        }
    }

    @GetMapping("/me")
    public UserResponseDTO me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.getUserDetailsByEmail(email);
    }

    @PostMapping("/resend-confirmation")
    public ResponseEntity<?> resendConfirmationEmail(@RequestParam String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "error", "User not found",
                        "message", "No user found with this email address"
                    ));
            }
            
            User user = userOpt.get();
            
            if (user.getEnabled()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Already confirmed",
                        "message", "This email address is already confirmed"
                    ));
            }
            
            // Delete old tokens and create new one
            confirmationTokenService.deleteTokensForUser(user);
            ConfirmationToken confirmationToken = confirmationTokenService.createConfirmationToken(user);
            
            try {
                emailService.sendConfirmationEmail(user.getEmail(), confirmationToken.getToken());
                
                return ResponseEntity.ok(Map.of(
                    "message", "Confirmation email has been resent successfully",
                    "email", user.getEmail(),
                    "instruction", "Please check your email and click the confirmation link"
                ));
            } catch (Exception emailError) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Email delivery failed",
                        "message", "Failed to send confirmation email: " + emailError.getMessage()
                    ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Server error",
                    "message", "An unexpected error occurred: " + e.getMessage()
                ));
        }
    }

    @GetMapping("/test-email-config")
    public ResponseEntity<?> testEmailConfiguration() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("smtp.host", "smtp.gmail.com");
            config.put("smtp.port", "587");
            config.put("from.email", emailService.getFromEmail());
            config.put("password.configured", emailService.isPasswordConfigured());
            config.put("mail.debug", "Check logs for detailed SMTP communication");
            
            return ResponseEntity.ok(Map.of(
                "message", "Email configuration check",
                "configuration", config,
                "status", emailService.isPasswordConfigured() ? "Ready" : "Needs configuration",
                "instructions", emailService.isPasswordConfigured() ? 
                    "Configuration looks good. Check logs for actual email sending." : 
                    "Please update spring.mail.username and spring.mail.password in application.properties"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Configuration check failed",
                    "message", e.getMessage()
                ));
        }
    }

    @PostMapping("/test-send-email")
    public ResponseEntity<?> testSendEmail(@RequestParam String email) {
        try {
            if (!emailService.isPasswordConfigured()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Email not configured",
                        "message", "Please configure spring.mail.username and spring.mail.password first"
                    ));
            }
            
            // Create a test user for demonstration
            User testUser = new User();
            testUser.setEmail(email);
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            
            ConfirmationToken testToken = new ConfirmationToken(testUser);
            testToken.setToken("TEST-TOKEN-" + System.currentTimeMillis());
            
            emailService.sendConfirmationEmail(email, testToken.getToken());
            
            return ResponseEntity.ok(Map.of(
                "message", "Test email sent successfully",
                "email", email,
                "testToken", testToken.getToken(),
                "testConfirmUrl", "http://localhost:8080/api/auth/confirm?token=" + testToken.getToken(),
                "instruction", "Check your email inbox and server logs"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Failed to send test email",
                    "message", e.getMessage(),
                    "instruction", "Check the server logs for detailed error information"
                ));
        }
    }
    
    /**
     * Test owner application approval email
     */
    @PostMapping("/test-owner-approval-email")
    public ResponseEntity<?> testOwnerApprovalEmail(@RequestParam String email) {
        try {
            if (!emailService.isPasswordConfigured()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Email not configured",
                        "message", "Please configure spring.mail.username and spring.mail.password first"
                    ));
            }
            
            emailService.sendOwnerApplicationApprovalEmail(email, "Test User");
            
            return ResponseEntity.ok(Map.of(
                "message", "Owner approval test email sent successfully",
                "email", email,
                "instruction", "Check your email inbox and server logs"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Failed to send owner approval test email",
                    "message", e.getMessage(),
                    "instruction", "Check the server logs for detailed error information"
                ));
        }
    }
    
    /**
     * Test owner application rejection email
     */
    @PostMapping("/test-owner-rejection-email")
    public ResponseEntity<?> testOwnerRejectionEmail(@RequestParam String email) {
        try {
            if (!emailService.isPasswordConfigured()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Email not configured",
                        "message", "Please configure spring.mail.username and spring.mail.password first"
                    ));
            }
            
            emailService.sendOwnerApplicationRejectionEmail(email, "Test User", "This is a test rejection for debugging purposes.");
            
            return ResponseEntity.ok(Map.of(
                "message", "Owner rejection test email sent successfully",
                "email", email,
                "instruction", "Check your email inbox and server logs"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Failed to send owner rejection test email",
                    "message", e.getMessage(),
                    "instruction", "Check the server logs for detailed error information"
                ));
        }
    }
    
    /**
     * Test admin notification email for new owner applications
     */
    @PostMapping("/test-admin-notification-email")
    public ResponseEntity<?> testAdminNotificationEmail() {
        try {
            if (!emailService.isPasswordConfigured()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Email not configured",
                        "message", "Please configure spring.mail.username and spring.mail.password first"
                    ));
            }
            
            emailService.sendOwnerApplicationNotificationToAdmin(
                "Test Applicant", 
                "test.applicant@example.com", 
                "This is a test owner application message for debugging purposes."
            );
            
            return ResponseEntity.ok(Map.of(
                "message", "Admin notification test email sent successfully",
                "adminEmail", emailService.getAdminEmail(),
                "applicantName", "Test Applicant",
                "applicantEmail", "test.applicant@example.com",
                "instruction", "Check the admin email inbox and server logs"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Failed to send admin notification test email",
                    "message", e.getMessage(),
                    "adminEmail", emailService.getAdminEmail(),
                    "instruction", "Check the server logs for detailed error information"
                ));
        }
    }
    
    /**
     * Delete user account permanently with all related data
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(Authentication authentication) {
        logger.info("=== DELETE ACCOUNT REQUEST ===");
        
        try {
            String email = authentication.getName();
            logger.info("User requesting deletion: {}", email);
            
            // Verify user exists and is authenticated
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                logger.error("❌ User not found for deletion: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "error", "User not found",
                        "message", "The user account was not found"
                    ));
            }
            
            User user = userOpt.get();
            logger.info("✅ User found for deletion: {} {} (ID: {})", 
                       user.getFirstName(), user.getLastName(), user.getId());
            
            // Delete the user account and all related data
            userService.deleteUserAccount(email);
            
            logger.info("✅ ACCOUNT DELETION COMPLETED SUCCESSFULLY for {}", email);
            
            return ResponseEntity.ok(Map.of(
                "message", "Account deleted successfully",
                "action", "logout_required"
            ));
            
        } catch (Exception e) {
            logger.error("❌ ACCOUNT DELETION FAILED: {}", e.getMessage());
            logger.error("Full error details: ", e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Deletion failed",
                    "message", "An error occurred while deleting your account: " + e.getMessage(),
                    "details", e.getClass().getSimpleName()
                ));
        }
    }
    
    /**
     * Test account deletion endpoint - for debugging only
     */
    @PostMapping("/test-delete-account")
    public ResponseEntity<?> testDeleteAccount(@RequestParam String email) {
        try {
            logger.info("=== TEST DELETE ACCOUNT REQUEST ===");
            logger.info("Test deletion for email: {}", email);
            
            // Verify user exists
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "error", "User not found",
                        "message", "The user account was not found: " + email
                    ));
            }
            
            // Test the deletion process
            userService.deleteUserAccount(email);
            
            return ResponseEntity.ok(Map.of(
                "message", "Test account deletion completed successfully",
                "email", email,
                "instruction", "Check the server logs for detailed deletion process"
            ));
            
        } catch (Exception e) {
            logger.error("❌ TEST ACCOUNT DELETION FAILED: {}", e.getMessage());
            logger.error("Full error details: ", e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Test deletion failed",
                    "message", "Error during test deletion: " + e.getMessage(),
                    "details", e.getClass().getSimpleName(),
                    "instruction", "Check the server logs for detailed error information"
                ));
        }
    }
}
