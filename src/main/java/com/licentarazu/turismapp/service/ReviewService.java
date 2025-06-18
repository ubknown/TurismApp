package com.licentarazu.turismapp.service;

import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.Review;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.AccommodationUnitRepository;
import com.licentarazu.turismapp.repository.ReviewRepository;
import com.licentarazu.turismapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AccommodationUnitRepository accommodationUnitRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         AccommodationUnitRepository accommodationUnitRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.accommodationUnitRepository = accommodationUnitRepository;
        this.userRepository = userRepository;
    }

    // Creează o recenzie nouă
    public Review createReview(Long userId, Long unitId, int rating, String comment) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<AccommodationUnit> unitOpt = accommodationUnitRepository.findById(unitId);

        if (userOpt.isEmpty() || unitOpt.isEmpty()) {
            throw new RuntimeException("Utilizatorul sau unitatea nu au fost găsite.");
        }

        Review review = new Review();
        review.setUser(userOpt.get());
        review.setAccommodationUnit(unitOpt.get());
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDate.now());

        return reviewRepository.save(review);
    }

    // Toate recenziile pentru o unitate
    public List<Review> getReviewsByUnit(Long unitId) {
        Optional<AccommodationUnit> unit = accommodationUnitRepository.findById(unitId);
        return unit.map(reviewRepository::findByAccommodationUnit)
                .orElseThrow(() -> new RuntimeException("Unitatea nu a fost găsită."));
    }

    // Media ratingului pentru o unitate
    public Double getAverageRatingForUnit(Long unitId) {
        Optional<AccommodationUnit> unit = accommodationUnitRepository.findById(unitId);
        return unit.map(reviewRepository::findAverageRatingByAccommodationUnit)
                .orElse(0.0);
    }
}
