package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.model.Review;
import com.licentarazu.turismapp.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ✅ Creează o recenzie nouă
    @PostMapping
    public Review createReview(@RequestParam Long userId,
                               @RequestParam Long unitId,
                               @RequestParam int rating,
                               @RequestParam String comment) {
        return reviewService.createReview(userId, unitId, rating, comment);
    }

    // ✅ Obține toate recenziile pentru o unitate
    @GetMapping("/unit/{unitId}")
    public List<Review> getReviewsByUnit(@PathVariable Long unitId) {
        return reviewService.getReviewsByUnit(unitId);
    }

    // ✅ Obține media ratingurilor pentru o unitate
    @GetMapping("/unit/{unitId}/average")
    public Double getAverageRatingForUnit(@PathVariable Long unitId) {
        return reviewService.getAverageRatingForUnit(unitId);
    }
}
