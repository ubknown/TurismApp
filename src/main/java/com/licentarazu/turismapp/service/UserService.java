package com.licentarazu.turismapp.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licentarazu.turismapp.dto.UserResponseDTO;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.model.OwnerApplication;
import com.licentarazu.turismapp.repository.AccommodationUnitRepository;
import com.licentarazu.turismapp.repository.BookingRepository;
import com.licentarazu.turismapp.repository.ConfirmationTokenRepository;
import com.licentarazu.turismapp.repository.OwnerApplicationRepository;
import com.licentarazu.turismapp.repository.PasswordResetTokenRepository;
import com.licentarazu.turismapp.repository.UserRepository;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccommodationUnitRepository accommodationUnitRepository;
    private final BookingRepository bookingRepository;
    private final OwnerApplicationRepository ownerApplicationRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      AccommodationUnitRepository accommodationUnitRepository,
                      BookingRepository bookingRepository,
                      OwnerApplicationRepository ownerApplicationRepository,
                      ConfirmationTokenRepository confirmationTokenRepository,
                      PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder; // TODO: Now using injected PasswordEncoder from SecurityConfig
        this.accommodationUnitRepository = accommodationUnitRepository;
        this.bookingRepository = bookingRepository;
        this.ownerApplicationRepository = ownerApplicationRepository;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    // Obține toți utilizatorii
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Înregistrează un utilizator nou (cu parolă hashuită)
    public User registerUser(User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email deja înregistrat");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        return userRepository.save(user);
    }

    // Caută un user după email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Validează datele de login (email + parolă)
    public boolean validateUserLogin(String email, String rawPassword) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    // Obține detalii utilizator ca UserResponseDTO după email
    public UserResponseDTO getUserDetailsByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return null;
        User user = userOpt.get();
        return new UserResponseDTO(
            user.getId(), 
            user.getFirstName(), 
            user.getLastName(),
            user.getEmail(), 
            user.getCreatedAt(),
            user.getRole().name(),
            user.getOwnerStatus()
        );
    }

    // Update user password with encryption
    public void updateUserPassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    /**
     * Permanently delete a user account and all related data
     * This includes: accommodation units, bookings, reviews, applications, tokens
     */
    @Transactional
    public void deleteUserAccount(String email) {
        logger.info("=== STARTING USER ACCOUNT DELETION ===");
        logger.info("Email: {}", email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.error("❌ USER NOT FOUND: {}", email);
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        logger.info("✅ User found: {} {} (ID: {})", user.getFirstName(), user.getLastName(), user.getId());
        
        try {
            // 1. Delete bookings made by this user (as a guest) on other properties
            logger.info("📅 Deleting bookings made by user as guest...");
            List<Booking> userBookings = bookingRepository.findByGuestEmail(user.getEmail());
            if (!userBookings.isEmpty()) {
                bookingRepository.deleteAll(userBookings);
                logger.info("✅ Deleted {} bookings made by user as guest", userBookings.size());
            } else {
                logger.info("ℹ️ No guest bookings found");
            }
            
            // 2. Delete all accommodation units owned by this user
            // This will cascade delete: bookings, accommodation photos, reviews for those units
            logger.info("🏠 Deleting accommodation units owned by user...");
            List<AccommodationUnit> userUnits = accommodationUnitRepository.findByOwner(user);
            logger.info("Found {} accommodation units to delete", userUnits.size());
            if (!userUnits.isEmpty()) {
                accommodationUnitRepository.deleteAll(userUnits);
                logger.info("✅ Deleted {} accommodation units", userUnits.size());
            }
            
            // 3. Unlink owner applications (preserve history for email-based tracking)
            logger.info("📝 Unlinking owner applications (preserving history)...");
            Optional<OwnerApplication> ownerApplication = ownerApplicationRepository.findByUser(user);
            if (ownerApplication.isPresent()) {
                OwnerApplication application = ownerApplication.get();
                // Unlink from user but keep the application record with email
                application.setUser(null);
                ownerApplicationRepository.save(application);
                logger.info("✅ Unlinked owner application (email: {}, status: {})", 
                           application.getEmail(), application.getStatus());
            } else {
                logger.info("ℹ️ No owner application found");
            }
            
            // 4. Delete confirmation tokens
            logger.info("🔗 Deleting confirmation tokens...");
            confirmationTokenRepository.deleteByUser(user);
            logger.info("✅ Deleted confirmation tokens");
            
            // 5. Delete password reset tokens  
            logger.info("🔑 Deleting password reset tokens...");
            passwordResetTokenRepository.deleteByUser(user);
            logger.info("✅ Deleted password reset tokens");
            
            // 6. Finally delete the user
            // This will cascade delete: reviews written by the user (due to CascadeType.ALL in User entity)
            logger.info("👤 Deleting user account...");
            userRepository.delete(user);
            logger.info("✅ USER ACCOUNT DELETED SUCCESSFULLY: {}", email);
            
        } catch (Exception e) {
            logger.error("❌ ERROR DURING USER DELETION: {}", e.getMessage());
            logger.error("Full error details: ", e);
            throw new RuntimeException("Failed to delete user account: " + e.getMessage(), e);
        }
    }
}
