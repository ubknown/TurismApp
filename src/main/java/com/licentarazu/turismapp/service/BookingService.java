package com.licentarazu.turismapp.service;

import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.model.BookingStatus;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.dto.BookingResponseDTO;
import com.licentarazu.turismapp.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

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

    // ✅ Get booking by ID
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }

    // ✅ Cancel booking with email notifications
    public Booking cancelBookingWithNotifications(Booking booking, User cancelledBy) {
        logger.info("Cancelling booking ID: {} by user: {}", booking.getId(), cancelledBy.getEmail());

        // Validate booking can be cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed booking");
        }

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        Booking cancelledBooking = bookingRepository.save(booking);
        
        logger.info("✅ Booking status updated to CANCELLED: {}", booking.getId());

        // Send email notifications asynchronously (don't fail cancellation if emails fail)
        try {
            // Determine who cancelled the booking
            boolean cancelledByGuest = cancelledBy.getEmail().equals(booking.getGuestEmail());
            boolean cancelledByOwner = booking.getAccommodationUnit().getOwner().getId().equals(cancelledBy.getId());

            // Send cancellation notification to guest (if cancelled by owner/admin)
            if (!cancelledByGuest && booking.getGuestEmail() != null && !booking.getGuestEmail().isEmpty()) {
                logger.info("Sending cancellation notification to guest: {}", booking.getGuestEmail());
                emailService.sendBookingCancellationToGuest(cancelledBooking, cancelledBy);
            }

            // Send cancellation notification to owner (if cancelled by guest/admin)
            if (!cancelledByOwner && booking.getAccommodationUnit().getOwner() != null) {
                logger.info("Sending cancellation notification to owner: {}", 
                           booking.getAccommodationUnit().getOwner().getEmail());
                emailService.sendBookingCancellationToOwner(cancelledBooking, cancelledBy);
            }

            // If cancelled by admin, notify both parties
            if (!cancelledByGuest && !cancelledByOwner) {
                logger.info("Admin cancellation - notifying both guest and owner");
                if (booking.getGuestEmail() != null && !booking.getGuestEmail().isEmpty()) {
                    emailService.sendBookingCancellationToGuest(cancelledBooking, cancelledBy);
                }
                if (booking.getAccommodationUnit().getOwner() != null) {
                    emailService.sendBookingCancellationToOwner(cancelledBooking, cancelledBy);
                }
            }

        } catch (Exception e) {
            logger.error("Error sending booking cancellation emails (cancellation still successful): {}", e.getMessage());
            // Don't fail the cancellation if email sending fails
        }

        return cancelledBooking;
    }

    // ✅ Convert Booking entity to BookingResponseDTO with unit details
    public BookingResponseDTO convertToResponseDTO(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        
        // Booking details
        dto.setId(booking.getId());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setGuestName(booking.getGuestName());
        dto.setGuestEmail(booking.getGuestEmail());
        dto.setGuestPhone(booking.getGuestPhone());
        dto.setNumberOfGuests(booking.getNumberOfGuests());
        dto.setSpecialRequests(booking.getSpecialRequests());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus());
        
        // Unit details (if available)
        AccommodationUnit unit = booking.getAccommodationUnit();
        if (unit != null) {
            dto.setUnitId(unit.getId());
            dto.setUnitName(unit.getName());
            dto.setUnitLocation(unit.getLocation());
            dto.setUnitCounty(unit.getCounty());
            dto.setUnitType(unit.getType() != null ? unit.getType().toString() : null);
            dto.setUnitPricePerNight(unit.getPricePerNight());
            
            // Get first image URL if available
            if (unit.getImages() != null && !unit.getImages().isEmpty()) {
                dto.setUnitImageUrl(unit.getImages().get(0));
            }
        }
        
        return dto;
    }

    // ✅ Get user bookings as DTOs with unit details
    public List<BookingResponseDTO> getUserBookingsAsDTO(String guestEmail) {
        List<Booking> allBookings = getAllBookings();
        return allBookings.stream()
                .filter(booking -> guestEmail.equals(booking.getGuestEmail()))
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}
