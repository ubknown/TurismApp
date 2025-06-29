package com.licentarazu.turismapp.repository;

import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.model.BookingStatus;
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
    
    // Status-based queries for profit calculations
    
    // Find confirmed/completed bookings for an accommodation unit within date range
    List<Booking> findByAccommodationUnit_IdAndCheckInDateBetweenAndStatusIn(
            Long unitId,
            LocalDate start,
            LocalDate end,
            List<BookingStatus> statuses
    );
    
    // Find confirmed/completed bookings by owner
    @Query("SELECT b FROM Booking b WHERE b.accommodationUnit.owner = :owner AND b.status IN :statuses")
    List<Booking> findByOwnerAndStatusIn(@Param("owner") User owner, @Param("statuses") List<BookingStatus> statuses);
    
    // Find confirmed/completed bookings by owner within date range
    @Query("SELECT b FROM Booking b WHERE b.accommodationUnit.owner = :owner AND b.checkInDate BETWEEN :start AND :end AND b.status IN :statuses")
    List<Booking> findByOwnerAndCheckInDateBetweenAndStatusIn(
            @Param("owner") User owner,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("statuses") List<BookingStatus> statuses
    );
    
    // Find confirmed/completed bookings for accommodation unit
    List<Booking> findByAccommodationUnitAndStatusIn(AccommodationUnit accommodationUnit, List<BookingStatus> statuses);
}
