package com.licentarazu.turismapp.repository;

import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.model.AccommodationUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Returnează rezervările care se suprapun cu intervalul dat
    List<Booking> findByAccommodationUnitAndCheckOutDateAfterAndCheckInDateBefore(
            AccommodationUnit accommodationUnit,
            LocalDate checkIn,
            LocalDate checkOut
    );

    // ✅ Returnează rezervările pentru o unitate în funcție de ID și intervalul dat
    List<Booking> findByAccommodationUnit_IdAndCheckInDateBetween(
            Long unitId,
            LocalDate start,
            LocalDate end
    );
}
