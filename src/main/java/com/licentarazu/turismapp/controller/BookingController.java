package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.BookingStatus;
import com.licentarazu.turismapp.dto.BookingRequestDTO;
import com.licentarazu.turismapp.dto.BookingResponseDTO;
import com.licentarazu.turismapp.service.BookingService;
import com.licentarazu.turismapp.repository.UserRepository;
import com.licentarazu.turismapp.repository.AccommodationUnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:5174"}, 
             allowCredentials = "true")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final AccommodationUnitRepository unitRepository;

    @Autowired
    public BookingController(BookingService bookingService, UserRepository userRepository,
            AccommodationUnitRepository unitRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        this.unitRepository = unitRepository;
    }

    // ✅ Enhanced booking creation with proper DTO handling and debug logging
    @PostMapping
    public ResponseEntity<?> createBooking(@Validated @RequestBody BookingRequestDTO bookingRequest, 
                                         BindingResult bindingResult) {
        try {
            logger.info("🔵 Received booking request: {}", bookingRequest.toString());
            
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                StringBuilder errorMessage = new StringBuilder("Validation errors: ");
                bindingResult.getFieldErrors().forEach(error -> 
                    errorMessage.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ")
                );
                logger.error("❌ Validation errors: {}", errorMessage.toString());
                return ResponseEntity.badRequest().body(errorMessage.toString());
            }

            // ✅ Additional date validations
            if (bookingRequest.getCheckInDate().isBefore(java.time.LocalDate.now())) {
                logger.error("❌ Check-in date is in the past: {}", bookingRequest.getCheckInDate());
                return ResponseEntity.badRequest().body("Check-in date cannot be in the past");
            }

            if (bookingRequest.getCheckOutDate().isBefore(java.time.LocalDate.now())) {
                logger.error("❌ Check-out date is in the past: {}", bookingRequest.getCheckOutDate());
                return ResponseEntity.badRequest().body("Check-out date cannot be in the past");
            }

            if (bookingRequest.getCheckInDate().isAfter(bookingRequest.getCheckOutDate()) || 
                bookingRequest.getCheckInDate().equals(bookingRequest.getCheckOutDate())) {
                logger.error("❌ Invalid date range: checkIn={}, checkOut={}", 
                           bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());
                return ResponseEntity.badRequest().body("Check-in date must be before check-out date");
            }

            // Fetch the complete accommodation unit with owner information
            AccommodationUnit unit = unitRepository.findById(bookingRequest.getAccommodationUnitId())
                    .orElseThrow(() -> new RuntimeException("Accommodation unit not found with ID: " + bookingRequest.getAccommodationUnitId()));
            
            logger.info("✅ Found accommodation unit: {} (ID: {})", unit.getName(), unit.getId());

            // ✅ Server-side price calculation and validation
            long nights = java.time.temporal.ChronoUnit.DAYS.between(
                    bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());
            double serverCalculatedPrice = nights * unit.getPricePerNight();
            
            logger.info("💰 Price calculation: {} nights × {} RON = {} RON", 
                       nights, unit.getPricePerNight(), serverCalculatedPrice);
            
            // If client provided a price, validate it matches server calculation
            if (bookingRequest.getTotalPrice() != null) {
                double clientPrice = bookingRequest.getTotalPrice();
                double tolerance = 0.01; // Allow small floating point differences
                
                if (Math.abs(clientPrice - serverCalculatedPrice) > tolerance) {
                    logger.error("❌ Price mismatch: expected={}, received={}", 
                               serverCalculatedPrice, clientPrice);
                    return ResponseEntity.badRequest().body(
                        String.format("Price mismatch. Expected: %.2f RON, Received: %.2f RON", 
                                     serverCalculatedPrice, clientPrice));
                }
            }

            // ✅ Create Booking entity from DTO
            Booking booking = new Booking();
            booking.setAccommodationUnit(unit);
            booking.setGuestName(bookingRequest.getGuestName());
            booking.setGuestEmail(bookingRequest.getGuestEmail());
            booking.setGuestPhone(bookingRequest.getGuestPhone());
            booking.setCheckInDate(bookingRequest.getCheckInDate());
            booking.setCheckOutDate(bookingRequest.getCheckOutDate());
            booking.setNumberOfGuests(bookingRequest.getNumberOfGuests());
            booking.setSpecialRequests(bookingRequest.getSpecialRequests());
            booking.setTotalPrice(serverCalculatedPrice); // Always use server-calculated price
            booking.setStatus(BookingStatus.CONFIRMED); // Set initial status

            logger.info("🔄 Creating booking with status: {}", booking.getStatus());

            // Create booking with email notifications
            Booking savedBooking = bookingService.createBookingWithEmailNotifications(booking);
            
            logger.info("✅ Booking created successfully with ID: {}", savedBooking.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Booking created successfully! Confirmation emails have been sent.",
                    "bookingId", savedBooking.getId(),
                    "totalPrice", savedBooking.getTotalPrice()));

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("❌ Business logic error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Unexpected error creating booking: ", e);
            return ResponseEntity.status(500).body("An error occurred while creating the booking: " + e.getMessage());
        }
    }

    // Returnează toate rezervările existente
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    // Returnează toate rezervările pentru o unitate de cazare
    @GetMapping("/by-unit/{unitId}")
    public List<Booking> getBookingsByUnit(@PathVariable Long unitId) {
        return bookingService.getBookingsByUnit(unitId);
    }

    // Șterge o rezervare după ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        boolean deleted = bookingService.deleteBooking(id);
        if (deleted) {
            return ResponseEntity.ok("Rezervarea a fost anulată cu succes.");
        } else {
            return ResponseEntity.status(404).body("Rezervarea nu a fost găsită.");
        }
    }

    // ✅ Returnează profitul lunar pentru o unitate turistică, în ultimele N luni
    @GetMapping("/profit/by-unit/{unitId}")
    public ResponseEntity<Map<String, Double>> getMonthlyProfitByUnit(
            @PathVariable Long unitId,
            @RequestParam(defaultValue = "6") int months) {

        Map<String, Double> profitData = bookingService.calculateMonthlyProfitForUnit(unitId, months);
        return ResponseEntity.ok(profitData);
    }

    // ✅ Get recent bookings for dashboard (used by DashboardPage.jsx)
    @GetMapping("/recent")
    public ResponseEntity<List<Booking>> getRecentBookings(
            @RequestParam(defaultValue = "5") int limit) {
        List<Booking> recentBookings = bookingService.getRecentBookings(limit);
        return ResponseEntity.ok(recentBookings);
    }

    // ✅ Get bookings for authenticated guest user (by email) with unit details
    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponseDTO>> getUserBookings(Authentication authentication) {
        String email = authentication.getName();
        logger.info("🔍 Fetching bookings for user: {}", email);

        List<BookingResponseDTO> userBookings = bookingService.getUserBookingsAsDTO(email);
        logger.info("✅ Found {} bookings for user: {}", userBookings.size(), email);
        
        return ResponseEntity.ok(userBookings);
    }

    // ✅ Get bookings for owner's properties
    @GetMapping("/owner")
    public ResponseEntity<List<Booking>> getOwnerBookings(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Get owner's units
        List<AccommodationUnit> ownerUnits = unitRepository.findByOwner(user);

        // Get all bookings for owner's units
        List<Booking> ownerBookings = ownerUnits.stream()
                .flatMap(unit -> bookingService.getBookingsByUnit(unit.getId()).stream())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ownerBookings);
    }

    // ✅ Cancel booking endpoint - accessible by both guest and owner
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            logger.info("🔵 Cancel booking request: bookingId={}, user={}", bookingId, userEmail);

            // Get the booking
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                logger.error("❌ Booking not found: {}", bookingId);
                return ResponseEntity.notFound().build();
            }

            // Check if user is authorized to cancel this booking
            User currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            boolean isGuest = userEmail.equals(booking.getGuestEmail());
            boolean isOwner = booking.getAccommodationUnit().getOwner().getId().equals(currentUser.getId());
            boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

            if (!isGuest && !isOwner && !isAdmin) {
                logger.error("❌ Unauthorized cancel attempt: user={}, bookingId={}", userEmail, bookingId);
                return ResponseEntity.status(403).body("You are not authorized to cancel this booking");
            }

            // Check if booking can be cancelled
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                logger.warn("⚠️ Booking already cancelled: {}", bookingId);
                return ResponseEntity.badRequest().body("Booking is already cancelled");
            }

            if (booking.getStatus() == BookingStatus.COMPLETED) {
                logger.warn("⚠️ Cannot cancel completed booking: {}", bookingId);
                return ResponseEntity.badRequest().body("Cannot cancel a completed booking");
            }

            // Cancel the booking with email notifications
            Booking cancelledBooking = bookingService.cancelBookingWithNotifications(booking, currentUser);
            
            logger.info("✅ Booking cancelled successfully: {}", bookingId);

            return ResponseEntity.ok(Map.of(
                    "message", "Booking cancelled successfully. Notification emails have been sent.",
                    "bookingId", cancelledBooking.getId(),
                    "status", cancelledBooking.getStatus().toString(),
                    "cancelledBy", isGuest ? "guest" : (isOwner ? "owner" : "admin")
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("❌ Business logic error cancelling booking: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Unexpected error cancelling booking: ", e);
            return ResponseEntity.status(500).body("An error occurred while cancelling the booking: " + e.getMessage());
        }
    }

    // ✅ Debug endpoint to test authentication
    @GetMapping("/debug/auth")
    public ResponseEntity<?> debugAuth(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            logger.info("🔵 Debug auth - user: {}, role: {}", userEmail, currentUser.getRole());
            
            return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "email", userEmail,
                "role", currentUser.getRole().name(),
                "id", currentUser.getId(),
                "enabled", currentUser.getEnabled()
            ));
        } catch (Exception e) {
            logger.error("❌ Debug auth error: ", e);
            return ResponseEntity.status(500).body(Map.of(
                "authenticated", false,
                "error", e.getMessage()
            ));
        }
    }
}
