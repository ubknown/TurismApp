package com.licentarazu.turismapp.service;

import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    @Autowired
    public BookingService(BookingRepository bookingRepository, EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
    }

    // ✅ Enhanced booking creation with email notifications
    public boolean createBooking(Booking booking) {
        logger.info("Creating new booking for unit: {} by guest: {}",
                booking.getAccommodationUnit().getName(), booking.getGuestName());

        LocalDate checkIn = booking.getCheckInDate();
        LocalDate checkOut = booking.getCheckOutDate();

        List<Booking> overlappingBookings = bookingRepository
                .findByAccommodationUnitAndCheckOutDateAfterAndCheckInDateBefore(
                        booking.getAccommodationUnit(), checkIn, checkOut);

        if (overlappingBookings.isEmpty()) {
            // Save the booking first
            Booking savedBooking = bookingRepository.save(booking);
            logger.info("✅ Booking saved successfully with ID: {}", savedBooking.getId());

            // Send email notifications
            try {
                // Send confirmation email to guest
                if (savedBooking.getGuestEmail() != null && !savedBooking.getGuestEmail().isEmpty()) {
                    logger.info("Sending booking confirmation email to guest: {}", savedBooking.getGuestEmail());
                    emailService.sendBookingConfirmationToGuest(savedBooking);
                } else {
                    logger.warn("Cannot send guest confirmation - guest email is null or empty");
                }

                // Send notification email to property owner
                if (savedBooking.getAccommodationUnit().getOwner() != null) {
                    logger.info("Sending booking notification email to owner: {}",
                            savedBooking.getAccommodationUnit().getOwner().getEmail());
                    emailService.sendBookingNotificationToOwner(savedBooking);
                } else {
                    logger.warn("Cannot send owner notification - property owner is null");
                }

            } catch (Exception e) {
                logger.error("Error sending booking notification emails (booking still successful): {}",
                        e.getMessage());
                // Don't fail the booking if email sending fails
            }

            return true;
        } else {
            logger.warn("❌ Booking failed - overlapping bookings found for dates {} to {}", checkIn, checkOut);
            return false;
        }
    }

    /**
     * ✅ Enhanced booking creation with validation and automatic price calculation
     */
    public Booking createBookingWithEmailNotifications(Booking booking) {
        logger.info("Creating enhanced booking for unit: {} by guest: {}",
                booking.getAccommodationUnit().getName(), booking.getGuestName());

        // Validate booking data
        if (booking.getCheckInDate().isAfter(booking.getCheckOutDate())) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }

        if (booking.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        // Calculate total price if not set
        if (booking.getTotalPrice() == null) {
            long nights = java.time.temporal.ChronoUnit.DAYS.between(
                    booking.getCheckInDate(), booking.getCheckOutDate());
            double totalPrice = nights * booking.getAccommodationUnit().getPricePerNight();
            booking.setTotalPrice(totalPrice);
            logger.info("Calculated total price: {} RON for {} nights", totalPrice, nights);
        }

        // Check for overlapping bookings
        LocalDate checkIn = booking.getCheckInDate();
        LocalDate checkOut = booking.getCheckOutDate();

        List<Booking> overlappingBookings = bookingRepository
                .findByAccommodationUnitAndCheckOutDateAfterAndCheckInDateBefore(
                        booking.getAccommodationUnit(), checkIn, checkOut);

        if (!overlappingBookings.isEmpty()) {
            logger.warn("❌ Booking failed - overlapping bookings found for dates {} to {}", checkIn, checkOut);
            throw new IllegalStateException("The accommodation unit is not available for the selected dates");
        }

        // Save the booking
        Booking savedBooking = bookingRepository.save(booking);
        logger.info("✅ Booking saved successfully with ID: {}", savedBooking.getId());

        // Send email notifications asynchronously (don't fail booking if emails fail)
        try {
            // Send confirmation email to guest
            if (savedBooking.getGuestEmail() != null && !savedBooking.getGuestEmail().isEmpty()) {
                logger.info("Sending booking confirmation email to guest: {}", savedBooking.getGuestEmail());
                emailService.sendBookingConfirmationToGuest(savedBooking);
            } else {
                logger.warn("Cannot send guest confirmation - guest email is null or empty");
            }

            // Send notification email to property owner
            if (savedBooking.getAccommodationUnit().getOwner() != null) {
                logger.info("Sending booking notification email to owner: {}",
                        savedBooking.getAccommodationUnit().getOwner().getEmail());
                emailService.sendBookingNotificationToOwner(savedBooking);
            } else {
                logger.warn("Cannot send owner notification - property owner is null");
            }

        } catch (Exception e) {
            logger.error("Error sending booking notification emails (booking still successful): {}", e.getMessage());
            // Don't fail the booking if email sending fails
        }

        return savedBooking;
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

    // Get recent bookings with limit
    public List<Booking> getRecentBookings(int limit) {
        List<Booking> allBookings = bookingRepository.findAll();
        return allBookings.stream()
                .sorted((b1, b2) -> b2.getCheckInDate().compareTo(b1.getCheckInDate())) // Sort by check-in date
                                                                                        // descending
                .limit(limit)
                .toList();
    }

    // ===== DASHBOARD METHODS =====

    /**
     * Get total bookings count for units owned by a specific user
     */
    public int getBookingsCountByOwner(User owner) {
        List<Booking> allBookings = bookingRepository.findAll();
        return (int) allBookings.stream()
                .filter(booking -> booking.getAccommodationUnit().getOwner().getId().equals(owner.getId()))
                .count();
    }

    /**
     * Calculate total revenue for units owned by a specific user
     */
    public double getTotalRevenueByOwner(User owner) {
        List<Booking> allBookings = bookingRepository.findAll();
        return allBookings.stream()
                .filter(booking -> booking.getAccommodationUnit().getOwner().getId().equals(owner.getId()))
                .filter(booking -> booking.getTotalPrice() != null)
                .mapToDouble(Booking::getTotalPrice)
                .sum();
    }

    /**
     * Calculate occupancy rate for units owned by a specific user
     */
    public double getOccupancyRate(User owner) {
        List<Booking> ownerBookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getAccommodationUnit().getOwner().getId().equals(owner.getId()))
                .toList();

        if (ownerBookings.isEmpty()) {
            return 0.0;
        }

        // Simple calculation: total booked days / total available days in current month
        LocalDate now = LocalDate.now();
        int daysInMonth = now.lengthOfMonth();

        long totalBookedDays = ownerBookings.stream()
                .filter(booking -> {
                    LocalDate checkIn = booking.getCheckInDate();
                    return checkIn.getMonthValue() == now.getMonthValue() &&
                            checkIn.getYear() == now.getYear();
                })
                .mapToLong(booking -> {
                    LocalDate checkIn = booking.getCheckInDate();
                    LocalDate checkOut = booking.getCheckOutDate();
                    return java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
                })
                .sum();

        return Math.min(100.0, (totalBookedDays * 100.0) / daysInMonth);
    }

    /**
     * Calculate revenue growth percentage compared to previous period
     */
    public double getRevenueGrowth(User owner) {
        LocalDate now = LocalDate.now();
        LocalDate currentMonthStart = now.withDayOfMonth(1);
        LocalDate previousMonthStart = currentMonthStart.minusMonths(1);
        LocalDate previousMonthEnd = currentMonthStart.minusDays(1);

        List<Booking> allBookings = bookingRepository.findAll();

        double currentMonthRevenue = allBookings.stream()
                .filter(booking -> booking.getAccommodationUnit().getOwner().getId().equals(owner.getId()))
                .filter(booking -> booking.getCheckInDate().isAfter(currentMonthStart.minusDays(1)))
                .filter(booking -> booking.getTotalPrice() != null)
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        double previousMonthRevenue = allBookings.stream()
                .filter(booking -> booking.getAccommodationUnit().getOwner().getId().equals(owner.getId()))
                .filter(booking -> booking.getCheckInDate().isAfter(previousMonthStart.minusDays(1)) &&
                        booking.getCheckInDate().isBefore(previousMonthEnd.plusDays(1)))
                .filter(booking -> booking.getTotalPrice() != null)
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        if (previousMonthRevenue == 0) {
            return currentMonthRevenue > 0 ? 100.0 : 0.0;
        }

        return ((currentMonthRevenue - previousMonthRevenue) / previousMonthRevenue) * 100.0;
    }
}
