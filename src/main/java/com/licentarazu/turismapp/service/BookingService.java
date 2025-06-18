package com.licentarazu.turismapp.service;

import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Creează o rezervare dacă nu există suprapuneri cu alte rezervări
    public boolean createBooking(Booking booking) {
        LocalDate checkIn = booking.getCheckInDate();
        LocalDate checkOut = booking.getCheckOutDate();

        List<Booking> overlappingBookings = bookingRepository
                .findByAccommodationUnitAndCheckOutDateAfterAndCheckInDateBefore(
                        booking.getAccommodationUnit(), checkIn, checkOut
                );

        if (overlappingBookings.isEmpty()) {
            bookingRepository.save(booking);
            return true;
        }

        return false;
    }

    // Returnează toate rezervările
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Returnează rezervările pentru o unitate de cazare
    public List<Booking> getBookingsByUnit(Long unitId) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getAccommodationUnit().getId().equals(unitId))
                .toList();
    }

    // Șterge o rezervare după ID
    public boolean deleteBooking(Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ✅ Calculează profitul lunar pentru o unitate în ultimele N luni
    public Map<String, Double> calculateMonthlyProfitForUnit(Long unitId, int monthsBack) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(monthsBack);

        List<Booking> bookings = bookingRepository
                .findByAccommodationUnit_IdAndCheckInDateBetween(unitId, startDate, endDate);

        Map<String, Double> monthlyProfit = new LinkedHashMap<>();

        // Inițializăm lunile cu 0
        for (int i = monthsBack - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear();
            monthlyProfit.put(label, 0.0);
        }

        for (Booking booking : bookings) {
            LocalDate checkIn = booking.getCheckInDate();
            LocalDate checkOut = booking.getCheckOutDate();
            long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
            double profit = nights * booking.getAccommodationUnit().getPricePerNight();

            YearMonth ym = YearMonth.from(checkIn);
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear();

            monthlyProfit.merge(label, profit, Double::sum);
        }

        return monthlyProfit;
    }
}
