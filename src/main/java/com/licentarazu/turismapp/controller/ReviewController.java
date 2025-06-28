package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.model.Review;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.service.ReviewService;
import com.licentarazu.turismapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:5174"}, allowCredentials = "true")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @Autowired
    public ReviewController(ReviewService reviewService, UserRepository userRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }

    // ✅ Create a new review (authenticated user)
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Map<String, Object> reviewData,
                                               Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Long unitId = Long.valueOf(reviewData.get("unitId").toString());
            int rating = Integer.parseInt(reviewData.get("rating").toString());
            String comment = reviewData.get("comment").toString();

            Review review = reviewService.createReview(user.getId(), unitId, rating, comment);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ Get all reviews for a unit
    @GetMapping("/unit/{unitId}")
    public List<Review> getReviewsByUnit(@PathVariable Long unitId) {
        return reviewService.getReviewsByUnit(unitId);
    }

    // ✅ Get average rating for a unit
    @GetMapping("/unit/{unitId}/average")
    public Double getAverageRatingForUnit(@PathVariable Long unitId) {
        return reviewService.getAverageRatingForUnit(unitId);
    }

    // ✅ Get reviews by authenticated user
    @GetMapping("/my-reviews")
    public ResponseEntity<List<Review>> getMyReviews(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        List<Review> myReviews = reviewService.getReviewsByUser(user.getId());
        return ResponseEntity.ok(myReviews);
    }
}
