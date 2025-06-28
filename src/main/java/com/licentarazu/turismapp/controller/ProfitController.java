package com.licentarazu.turismapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.AccommodationUnitRepository;
import com.licentarazu.turismapp.repository.BookingRepository;
import com.licentarazu.turismapp.repository.UserRepository;
import com.licentarazu.turismapp.service.BookingService;

@RestController
@RequestMapping("/api/profit")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class ProfitController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccommodationUnitRepository unitRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingService bookingService;

    /**
     * Get dashboard statistics for authenticated user
     * Used by DashboardPage.jsx
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Get user's units
        List<AccommodationUnit> userUnits = unitRepository.findByOwner(user);
        
        // Get all bookings for user's units
        List<Booking> allBookings = bookingRepository.findAll();
        List<Booking> userBookings = allBookings.stream()
                .filter(booking -> userUnits.stream()
                    .anyMatch(unit -> unit.getId().equals(booking.getAccommodationUnit().getId())))
                .toList();

        // Calculate statistics
        double totalRevenue = userBookings.stream()
                .mapToDouble(booking -> {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(
                        booking.getCheckInDate(), 
                        booking.getCheckOutDate()
                    );
                    return days * booking.getAccommodationUnit().getPricePerNight();
                })
                .sum();

        int totalBookings = userBookings.size();
        int totalUnits = userUnits.size();
        
        double averageRating = userUnits.stream()
                .mapToDouble(unit -> unit.getRating() != null ? unit.getRating().doubleValue() : 0.0)
                .average()
                .orElse(0.0);

        // Recent bookings (last 5)
        List<Booking> recentBookings = userBookings.stream()
                .sorted((b1, b2) -> b2.getCheckInDate().compareTo(b1.getCheckInDate()))
                .limit(5)
                .toList();

        // Prepare response
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalBookings", totalBookings);
        stats.put("totalUnits", totalUnits);
        stats.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        stats.put("recentBookings", recentBookings);

        return ResponseEntity.ok(stats);
    }

    /**
     * Get monthly profit data for charts
     * Used by ProfitChart.jsx
     */
    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Double>> getMonthlyProfit(
            Authentication authentication,
            @RequestParam(defaultValue = "6") int months) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Get user's first unit (for now, until we implement multi-unit profit)
        List<AccommodationUnit> userUnits = unitRepository.findByOwner(user);
        if (userUnits.isEmpty()) {
            return ResponseEntity.ok(new HashMap<>());
        }

        // Use the booking service to calculate monthly profit for the first unit
        Map<String, Double> monthlyProfit = bookingService.calculateMonthlyProfitForUnit(
            userUnits.get(0).getId(), 
            months
        );

        return ResponseEntity.ok(monthlyProfit);
    }
}
