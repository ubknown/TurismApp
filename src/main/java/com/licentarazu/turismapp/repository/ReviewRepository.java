package com.licentarazu.turismapp.repository;

import com.licentarazu.turismapp.model.Review;
import com.licentarazu.turismapp.model.AccommodationUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Toate recenziile pentru o anumită unitate
    List<Review> findByAccommodationUnit(AccommodationUnit unit);

    // Calculul mediei ratingului pentru o unitate
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.accommodationUnit = :unit")
    Double findAverageRatingByAccommodationUnit(AccommodationUnit unit);
}
