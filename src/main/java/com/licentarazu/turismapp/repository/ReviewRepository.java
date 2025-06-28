package com.licentarazu.turismapp.repository;

import com.licentarazu.turismapp.model.Review;
import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Toate recenziile pentru o anumită unitate
    List<Review> findByAccommodationUnit(AccommodationUnit unit);
    
    // Toate recenziile pentru un utilizator  
    List<Review> findByUser(User user);
    
    // Calculul mediei ratingului pentru o unitate
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.accommodationUnit = :unit")
    Double findAverageRatingByAccommodationUnit(AccommodationUnit unit);

    // Get average rating for all units owned by a user
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.accommodationUnit.owner = :owner")
    Double findAverageRatingByOwner(User owner);
    
    // Get reviews for all units owned by a user
    @Query("SELECT r FROM Review r WHERE r.accommodationUnit.owner = :owner")
    List<Review> findByAccommodationUnitOwner(User owner);
}
