package com.licentarazu.turismapp.repository;

import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Find all bookings for a specific accommodation unit
    List<Booking> findByAccommodationUnit(AccommodationUnit accommodationUnit);

    // Find bookings by owner (through accommodation unit)
    @Query("SELECT b FROM Booking b WHERE b.accommodationUnit.owner = :owner")
    List<Booking> findByOwner(@Param("owner") User owner);
    
    // Find bookings by guest email
    List<Booking> findByGuestEmail(String guestEmail);
}
