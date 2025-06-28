package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.service.BookingService;
import com.licentarazu.turismapp.repository.UserRepository;
import com.licentarazu.turismapp.repository.AccommodationUnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:5174"}, 
             allowCredentials = "true")
public class BookingController {

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

    // ✅ Enhanced booking creation with email notifications
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        try {
            // ✅ CRITICAL FIX: Enhanced validation for security and data integrity
            
            // Validate required fields
            if (booking.getAccommodationUnit() == null || booking.getAccommodationUnit().getId() == null) {
                return ResponseEntity.badRequest().body("Accommodation unit is required");
            }

            if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null) {
                return ResponseEntity.badRequest().body("Check-in and check-out dates are required");
            }

            if (booking.getGuestEmail() == null || booking.getGuestEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("Guest email is required");
            }

            // ✅ CRITICAL FIX: Validate dates are not in the past
            if (booking.getCheckInDate().isBefore(java.time.LocalDate.now())) {
                return ResponseEntity.badRequest().body("Check-in date cannot be in the past");
            }

            if (booking.getCheckOutDate().isBefore(java.time.LocalDate.now())) {
                return ResponseEntity.badRequest().body("Check-out date cannot be in the past");
            }

            if (booking.getCheckInDate().isAfter(booking.getCheckOutDate()) || 
                booking.getCheckInDate().equals(booking.getCheckOutDate())) {
                return ResponseEntity.badRequest().body("Check-in date must be before check-out date");
            }

            // Fetch the complete accommodation unit with owner information
            AccommodationUnit unit = unitRepository.findById(booking.getAccommodationUnit().getId())
                    .orElseThrow(() -> new RuntimeException("Accommodation unit not found"));
            booking.setAccommodationUnit(unit);

            // ✅ CRITICAL FIX: Server-side price calculation and validation
            long nights = java.time.temporal.ChronoUnit.DAYS.between(
                    booking.getCheckInDate(), booking.getCheckOutDate());
            double serverCalculatedPrice = nights * unit.getPricePerNight();
            
            // If client provided a price, validate it matches server calculation
            if (booking.getTotalPrice() != null) {
                double clientPrice = booking.getTotalPrice();
                double tolerance = 0.01; // Allow small floating point differences
                
                if (Math.abs(clientPrice - serverCalculatedPrice) > tolerance) {
                    return ResponseEntity.badRequest().body(
                        String.format("Price mismatch. Expected: %.2f RON, Received: %.2f RON", 
                                     serverCalculatedPrice, clientPrice));
                }
            }
            
            // Always use server-calculated price for security
            booking.setTotalPrice(serverCalculatedPrice);

            // Create booking with email notifications
            Booking savedBooking = bookingService.createBookingWithEmailNotifications(booking);

            return ResponseEntity.ok(Map.of(
                    "message", "Booking created successfully! Confirmation emails have been sent.",
                    "bookingId", savedBooking.getId(),
                    "totalPrice", savedBooking.getTotalPrice()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
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

    // ✅ Get bookings for authenticated guest user (by email)
    @GetMapping("/my-bookings")
    public ResponseEntity<List<Booking>> getUserBookings(Authentication authentication) {
        String email = authentication.getName();

        // Get all bookings and filter by guest email
        List<Booking> allBookings = bookingService.getAllBookings();
        List<Booking> userBookings = allBookings.stream()
                .filter(booking -> email.equals(booking.getGuestEmail()))
                .collect(Collectors.toList());

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
}
