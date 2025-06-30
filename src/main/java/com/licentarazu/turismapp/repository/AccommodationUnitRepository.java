package com.licentarazu.turismapp.repository;

import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccommodationUnitRepository extends JpaRepository<AccommodationUnit, Long> {

    @Query("SELECT DISTINCT a FROM AccommodationUnit a " +
            "LEFT JOIN FETCH a.photos " +
            "WHERE a.available = true AND a.status = 'active' " +
            "AND (:location IS NULL OR :location = '' OR LOWER(a.location) LIKE LOWER(CONCAT('%', :location, '%')) OR LOWER(a.county) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (:minPrice IS NULL OR a.pricePerNight >= :minPrice) " +
            "AND (:maxPrice IS NULL OR a.pricePerNight <= :maxPrice) " +
            "AND (:minCapacity IS NULL OR a.capacity >= :minCapacity) " +
            "AND (:maxCapacity IS NULL OR a.capacity <= :maxCapacity) " +
            "AND (:type IS NULL OR :type = '' OR a.type = :type) " +
            "AND (:minRating IS NULL OR " +
            "     (SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.accommodationUnit = a) >= :minRating)")
    List<AccommodationUnit> findByFiltersWithRating(
            @Param("location") String location,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minCapacity") Integer minCapacity,
            @Param("maxCapacity") Integer maxCapacity,
            @Param("type") String type,
            @Param("minRating") Double minRating
    );

    List<AccommodationUnit> findByLocationContainingIgnoreCase(String location);

    List<AccommodationUnit> findByOwner(User owner);
    
    // Find all active and available units for public browsing with photos
    @Query("SELECT DISTINCT a FROM AccommodationUnit a LEFT JOIN FETCH a.photos WHERE a.available = true AND a.status = 'active' ORDER BY a.createdAt DESC")
    List<AccommodationUnit> findAllActiveAndAvailable();
    
    // Find all units by owner including inactive ones with photos
    @Query("SELECT DISTINCT a FROM AccommodationUnit a LEFT JOIN FETCH a.photos WHERE a.owner = :owner ORDER BY a.createdAt DESC")
    List<AccommodationUnit> findAllByOwnerOrdered(@Param("owner") User owner);
    
    // Find all active and available units with photos
    @Query("SELECT DISTINCT a FROM AccommodationUnit a LEFT JOIN FETCH a.photos WHERE a.available = true AND a.status = 'active' ORDER BY a.createdAt DESC")
    List<AccommodationUnit> findAllActiveUnits();
    
    // Find units by owner with proper ordering and photos
    @Query("SELECT DISTINCT a FROM AccommodationUnit a LEFT JOIN FETCH a.photos WHERE a.owner = :owner ORDER BY a.createdAt DESC")
    List<AccommodationUnit> findByOwnerOrderByCreatedAtDesc(@Param("owner") User owner);
}
