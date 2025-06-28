package com.licentarazu.turismapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.licentarazu.turismapp.model.AccommodationPhoto;

@Repository
public interface AccommodationPhotoRepository extends JpaRepository<AccommodationPhoto, Long> {
    
    List<AccommodationPhoto> findByAccommodationUnitId(Long accommodationUnitId);
    
    void deleteByAccommodationUnitId(Long accommodationUnitId);
    
    long countByAccommodationUnitId(Long accommodationUnitId);
}
