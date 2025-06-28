package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.UserRepository;
import com.licentarazu.turismapp.service.AccommodationUnitService;
import com.licentarazu.turismapp.service.BookingService;
import com.licentarazu.turismapp.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccommodationUnitService unitService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Map<String, Object> stats = new HashMap<>();
            
            // Get user's units count
            int totalUnits = unitService.getUnitsByOwner(user).size();
            
            // Get total bookings count for user's units
            int totalBookings = bookingService.getBookingsCountByOwner(user);
            
            // Calculate total revenue
            double totalRevenue = bookingService.getTotalRevenueByOwner(user);
            
            // Get average rating for user's units
            double averageRating = reviewService.getAverageRatingByOwner(user);

            stats.put("totalUnits", totalUnits);
            stats.put("totalBookings", totalBookings);
            stats.put("totalRevenue", totalRevenue);
            stats.put("averageRating", averageRating);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            // Return default values if error occurs
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalUnits", 0);
            defaultStats.put("totalBookings", 0);
            defaultStats.put("totalRevenue", 0.0);
            defaultStats.put("averageRating", 0.0);
            
            return ResponseEntity.ok(defaultStats);
        }
    }

    @GetMapping("/insights")
    public ResponseEntity<Map<String, Object>> getDashboardInsights(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Map<String, Object> insights = new HashMap<>();
            
            // Add performance insights
            insights.put("topPerformingUnit", unitService.getTopPerformingUnit(user));
            insights.put("occupancyRate", bookingService.getOccupancyRate(user));
            insights.put("revenueGrowth", bookingService.getRevenueGrowth(user));
            insights.put("guestSatisfaction", reviewService.getGuestSatisfactionScore(user));

            return ResponseEntity.ok(insights);
        } catch (Exception e) {
            return ResponseEntity.ok(new HashMap<>());
        }
    }
}
