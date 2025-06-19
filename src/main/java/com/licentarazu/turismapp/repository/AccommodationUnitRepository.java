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

    @Query("SELECT a FROM AccommodationUnit a " +
            "LEFT JOIN Review r ON r.accommodationUnit = a " +
            "WHERE a.available = true " +
            "AND (:location IS NULL OR a.location = :location) " +
            "AND (:minPrice IS NULL OR a.pricePerNight >= :minPrice) " +
            "AND (:maxPrice IS NULL OR a.pricePerNight <= :maxPrice) " +
            "AND (:minCapacity IS NULL OR a.capacity >= :minCapacity) " +
            "AND (:maxCapacity IS NULL OR a.capacity <= :maxCapacity) " +
            "AND (:type IS NULL OR a.type = :type) " +
            "GROUP BY a " +
            "HAVING (:minRating IS NULL OR AVG(r.rating) >= :minRating)")
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
}
