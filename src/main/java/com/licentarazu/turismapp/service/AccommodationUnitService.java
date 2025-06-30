package com.licentarazu.turismapp.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licentarazu.turismapp.model.AccommodationPhoto;
import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.model.BookingStatus;
import com.licentarazu.turismapp.model.Reservation;
import com.licentarazu.turismapp.model.ReservationStatus;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.AccommodationUnitRepository;
import com.licentarazu.turismapp.repository.BookingRepository;
import com.licentarazu.turismapp.repository.ReservationRepository;
import com.licentarazu.turismapp.util.GeoUtils;
import com.licentarazu.turismapp.dto.ProfitReportDTO;

@Service
public class AccommodationUnitService {

    private final AccommodationUnitRepository accommodationUnitRepository;
    private final BookingRepository bookingRepository;
    private final ReservationRepository reservationRepository;
    private final CityCoordinatesService cityCoordinatesService;
    private final AccommodationPhotoService photoService;

    @Autowired
    public AccommodationUnitService(AccommodationUnitRepository accommodationUnitRepository,
            BookingRepository bookingRepository,
            ReservationRepository reservationRepository,
            CityCoordinatesService cityCoordinatesService,
            AccommodationPhotoService photoService) {
        this.accommodationUnitRepository = accommodationUnitRepository;
        this.bookingRepository = bookingRepository;
        this.reservationRepository = reservationRepository;
        this.cityCoordinatesService = cityCoordinatesService;
        this.photoService = photoService;
    }

    // ✅ Enhanced unit creation with location uniqueness validation
    public AccommodationUnit addUnit(AccommodationUnit unit) {
        // Validate location uniqueness
        validateLocationUniqueness(unit.getLocation());

        unit.setCreatedAt(LocalDate.now());
        return accommodationUnitRepository.save(unit);
    }

    // ✅ Validate that no other property exists at the same location
    private void validateLocationUniqueness(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }

        String normalizedLocation = location.trim().toLowerCase();
        List<AccommodationUnit> existingUnits = accommodationUnitRepository.findAll();

        for (AccommodationUnit existingUnit : existingUnits) {
            if (existingUnit.getLocation() != null &&
                    existingUnit.getLocation().trim().toLowerCase().equals(normalizedLocation)) {
                throw new IllegalStateException(
                        "A property already exists at this location. Please verify the address.");
            }
        }
    }

    // ✅ Validate location uniqueness for updates (excluding current unit)
    private void validateLocationUniquenessForUpdate(String location, Long excludeUnitId) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }

        String normalizedLocation = location.trim().toLowerCase();
        List<AccommodationUnit> existingUnits = accommodationUnitRepository.findAll();

        for (AccommodationUnit existingUnit : existingUnits) {
            if (!existingUnit.getId().equals(excludeUnitId) &&
                    existingUnit.getLocation() != null &&
                    existingUnit.getLocation().trim().toLowerCase().equals(normalizedLocation)) {
                throw new IllegalStateException(
                        "A property already exists at this location. Please verify the address.");
            }
        }
    }

    // Returnează toate unitățile active și disponibile
    public List<AccommodationUnit> getAllUnits() {
        return accommodationUnitRepository.findAllActiveUnits();
    }

    // Caută după locație (parțial, fără case-sensitive)
    public List<AccommodationUnit> searchByLocation(String location) {
        return accommodationUnitRepository.findByLocationContainingIgnoreCase(location);
    }

    // Caută după ID
    public Optional<AccommodationUnit> getById(Long id) {
        return accommodationUnitRepository.findById(id);
    }

    // Șterge unitate
    public void deleteById(Long id) {
        accommodationUnitRepository.deleteById(id);
    }

    // ✅ Enhanced unit update with location validation
    public AccommodationUnit updateUnit(Long id, AccommodationUnit updatedUnit) {
        Optional<AccommodationUnit> optionalUnit = accommodationUnitRepository.findById(id);

        if (optionalUnit.isEmpty()) {
            throw new RuntimeException("Unitatea cu ID-ul " + id + " nu a fost găsită.");
        }

        AccommodationUnit existingUnit = optionalUnit.get();

        // Validate location uniqueness if location is being changed
        if (updatedUnit.getLocation() != null &&
                !updatedUnit.getLocation().equals(existingUnit.getLocation())) {
            validateLocationUniquenessForUpdate(updatedUnit.getLocation(), id);
        }

        existingUnit.setName(updatedUnit.getName());
        existingUnit.setLocation(updatedUnit.getLocation());
        existingUnit.setPricePerNight(updatedUnit.getPricePerNight());
        existingUnit.setDescription(updatedUnit.getDescription());
        existingUnit.setAvailable(updatedUnit.isAvailable());

        return accommodationUnitRepository.save(existingUnit);
    }

    // Filtrare după locație, preț, capacitate și tip
    public List<AccommodationUnit> getFilteredUnits(String location, Double minPrice, Double maxPrice,
            Integer minCapacity, Integer maxCapacity, String type,
            Double minRating) {
        
        System.out.println("🔍 SERVICE: getFilteredUnits called with parameters:");
        System.out.println("  - location: '" + location + "'");
        System.out.println("  - minPrice: " + minPrice);
        System.out.println("  - maxPrice: " + maxPrice);
        System.out.println("  - minCapacity: " + minCapacity);
        System.out.println("  - maxCapacity: " + maxCapacity);
        System.out.println("  - type: '" + type + "'");
        System.out.println("  - minRating: " + minRating);
        
        List<AccommodationUnit> result = accommodationUnitRepository.findByFiltersWithRating(
                location, minPrice, maxPrice, minCapacity, maxCapacity, type, minRating);
        
        System.out.println("🎯 SERVICE: Repository returned " + result.size() + " units");
        if (result.size() > 0) {
            System.out.println("  - First unit: " + result.get(0).getName() + " (ID: " + result.get(0).getId() + ")");
        }
        
        return result;
    }

    // ✅ Filtrare după disponibilitate într-un interval check-in / check-out
    public List<AccommodationUnit> findAvailableUnits(LocalDate checkIn, LocalDate checkOut) {
        List<AccommodationUnit> allUnits = accommodationUnitRepository.findAll();
        List<AccommodationUnit> availableUnits = new ArrayList<>();

        for (AccommodationUnit unit : allUnits) {
            List<Booking> overlappingBookings = bookingRepository
                    .findByAccommodationUnitAndCheckOutDateAfterAndCheckInDateBefore(unit, checkIn, checkOut);

            if (overlappingBookings.isEmpty() && unit.isAvailable()) {
                availableUnits.add(unit);
            }
        }

        return availableUnits;
    }

    // Returnează unitățile de cazare aflate într-o rază (km) față de un oraș dat
    public List<AccommodationUnit> findUnitsNearCity(String city, double radiusKm) {
        double[] targetCoords = cityCoordinatesService.getCoordinates(city);
        if (targetCoords == null) {
            throw new IllegalArgumentException("Orașul introdus nu este recunoscut.");
        }

        double targetLat = targetCoords[0];
        double targetLon = targetCoords[1];

        List<AccommodationUnit> allUnits = accommodationUnitRepository.findAll();
        List<AccommodationUnit> nearbyUnits = new ArrayList<>();

        for (AccommodationUnit unit : allUnits) {
            if (unit.getLatitude() == null || unit.getLongitude() == null)
                continue;

            double distance = GeoUtils.distanceInKm(
                    targetLat, targetLon,
                    unit.getLatitude(), unit.getLongitude());

            if (distance <= radiusKm) {
                nearbyUnits.add(unit);
            }
        }

        return nearbyUnits;
    }

    // Filtrare avansată: proximitate + disponibilitate
    public List<AccommodationUnit> filterUnitsAdvanced(String location, Double radiusKm, LocalDate checkIn,
            LocalDate checkOut) {
        if (location == null || radiusKm == null || checkIn == null || checkOut == null) {
            throw new IllegalArgumentException(
                    "Toate filtrele avansate sunt obligatorii: location, radiusKm, checkIn, checkOut");
        }
        double[] targetCoords = cityCoordinatesService.getCoordinates(location);
        if (targetCoords == null) {
            throw new IllegalArgumentException("Orașul introdus nu este recunoscut.");
        }
        double targetLat = targetCoords[0];
        double targetLon = targetCoords[1];
        List<AccommodationUnit> allUnits = accommodationUnitRepository.findAll();
        List<AccommodationUnit> filtered = new ArrayList<>();
        for (AccommodationUnit unit : allUnits) {
            if (unit.getLatitude() == null || unit.getLongitude() == null)
                continue;
            double distance = GeoUtils.distanceInKm(targetLat, targetLon, unit.getLatitude(), unit.getLongitude());
            if (distance > radiusKm)
                continue;
            // Check availability
            List<Booking> overlapping = bookingRepository
                    .findByAccommodationUnitAndCheckOutDateAfterAndCheckInDateBefore(
                            unit, checkIn, checkOut);
            if (overlapping.isEmpty() && unit.isAvailable()) {
                filtered.add(unit);
            }
        }
        return filtered;
    }

    // ✅ Enhanced profit calculation - only confirmed/completed bookings, using totalPrice
    public List<ProfitResult> calculateProfit(int months, User owner) {
        List<AccommodationUnit> myUnits = accommodationUnitRepository.findByOwner(owner);
        LocalDate now = LocalDate.now();
        LocalDate fromDate = (months > 0) ? now.minusMonths(months) : LocalDate.MIN;
        List<ProfitResult> result = new ArrayList<>();
        
        for (AccommodationUnit unit : myUnits) {
            double profit = 0.0;
            List<Booking> bookings = bookingRepository.findByAccommodationUnit(unit);
            
            for (Booking booking : bookings) {
                // ✅ Only include confirmed or completed bookings
                if ((booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.COMPLETED) &&
                    booking.getTotalPrice() != null &&
                    ((months == 0) || (booking.getCheckInDate() != null && !booking.getCheckInDate().isBefore(fromDate)))) {
                    
                    // ✅ Use totalPrice directly for accuracy
                    profit += booking.getTotalPrice();
                }
            }
            result.add(new ProfitResult(unit.getId(), unit.getName(), profit));
        }
        return result;
    }

    // ✅ Enhanced monthly profit calculation - only confirmed/completed bookings, using totalPrice
    public List<MonthlyProfitResult> getMonthlyProfit(int months, User owner) {
        LocalDate now = LocalDate.now();
        LocalDate fromDate = (months > 0) ? now.minusMonths(months) : LocalDate.MIN;
        Map<YearMonth, Double> profitByMonth = new TreeMap<>();
        
        // ✅ Use efficient repository method to get only confirmed/completed bookings
        List<BookingStatus> validStatuses = List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED);
        List<Booking> bookings;
        
        if (months > 0) {
            bookings = bookingRepository.findByOwnerAndCheckInDateBetweenAndStatusIn(owner, fromDate, now, validStatuses);
        } else {
            bookings = bookingRepository.findByOwnerAndStatusIn(owner, validStatuses);
        }
        
        for (Booking booking : bookings) {
            if (booking.getTotalPrice() != null && booking.getCheckInDate() != null) {
                YearMonth ym = YearMonth.from(booking.getCheckInDate());
                // ✅ Use totalPrice directly for accuracy
                profitByMonth.put(ym, profitByMonth.getOrDefault(ym, 0.0) + booking.getTotalPrice());
            }
        }
        
        List<MonthlyProfitResult> result = new ArrayList<>();
        for (Map.Entry<YearMonth, Double> entry : profitByMonth.entrySet()) {
            result.add(new MonthlyProfitResult(entry.getKey().toString(), entry.getValue()));
        }
        return result;
    }

    public List<PredictedProfitDTO> predictFutureProfits(int historyMonths, int predictMonths, User owner) {
        List<MonthlyProfitResult> history = getMonthlyProfit(historyMonths, owner);
        int n = history.size();
        if (n < 2)
            return new ArrayList<>(); // Not enough data
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = history.get(i).getTotalProfit();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        List<PredictedProfitDTO> predictions = new ArrayList<>();
        java.time.YearMonth lastMonth = java.time.YearMonth.now();
        for (int i = 0; i < predictMonths; i++) {
            int monthIndex = n + i;
            double predicted = slope * monthIndex + intercept;
            predicted = Math.round(predicted * 100.0) / 100.0;
            java.time.YearMonth ym = lastMonth.plusMonths(i + 1);
            predictions.add(new PredictedProfitDTO(ym.toString(), predicted));
        }
        return predictions;
    }

    // ===== DASHBOARD METHODS =====

    /**
     * Get all accommodation units owned by a specific user (ordered by creation date)
     */
    public List<AccommodationUnit> getUnitsByOwner(User owner) {
        System.out.println("=== SERVICE: GET UNITS BY OWNER ===");
        System.out.println("Owner: " + owner.getEmail() + " (ID: " + owner.getId() + ")");
        
        List<AccommodationUnit> units = accommodationUnitRepository.findAllByOwnerOrdered(owner);
        System.out.println("Found " + units.size() + " units for owner");
        
        // Log each unit for debugging
        for (AccommodationUnit unit : units) {
            System.out.println("Unit: " + unit.getId() + " - " + unit.getName() + 
                             " (available: " + unit.isAvailable() + ", status: " + unit.getStatus() + 
                             ", created: " + unit.getCreatedAt() + ")");
        }
        
        return units;
    }

    /**
     * Get the top performing unit based on total revenue
     */
    public Map<String, Object> getTopPerformingUnit(User owner) {
        List<AccommodationUnit> ownerUnits = accommodationUnitRepository.findByOwner(owner);

        if (ownerUnits.isEmpty()) {
            return Map.of("message", "No units found");
        }

        AccommodationUnit topUnit = null;
        double maxRevenue = 0.0;

        for (AccommodationUnit unit : ownerUnits) {
            List<Booking> unitBookings = bookingRepository.findByAccommodationUnit(unit);
            double unitRevenue = 0.0;

            for (Booking booking : unitBookings) {
                if (booking.getTotalPrice() != null) {
                    unitRevenue += booking.getTotalPrice();
                }
            }

            if (unitRevenue > maxRevenue) {
                maxRevenue = unitRevenue;
                topUnit = unit;
            }
        }

        if (topUnit == null) {
            return Map.of("message", "No bookings found");
        }

        return Map.of(
                "unitId", topUnit.getId(),
                "unitName", topUnit.getName(),
                "totalRevenue", maxRevenue);
    }

    // ✅ Enhanced profit analytics - only confirmed/completed bookings, using totalPrice
    public Map<String, Double> getOwnerProfitAnalytics(User owner, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        Map<String, Double> monthlyProfits = new TreeMap<>();

        // Initialize all months with 0
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            String monthKey = ym.getYear() + "-" + String.format("%02d", ym.getMonthValue());
            monthlyProfits.put(monthKey, 0.0);
        }

        // ✅ Use efficient repository method to get only confirmed/completed bookings
        List<BookingStatus> validStatuses = List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED);
        List<Booking> bookings = bookingRepository.findByOwnerAndCheckInDateBetweenAndStatusIn(
                owner, startDate, endDate, validStatuses);

        for (Booking booking : bookings) {
            if (booking.getCheckInDate() != null && booking.getTotalPrice() != null) {
                YearMonth bookingMonth = YearMonth.from(booking.getCheckInDate());
                String monthKey = bookingMonth.getYear() + "-"
                        + String.format("%02d", bookingMonth.getMonthValue());

                // ✅ Use totalPrice directly for accuracy
                monthlyProfits.merge(monthKey, booking.getTotalPrice(), Double::sum);
            }
        }

        return monthlyProfits;
    }

    // ✅ Get total profit for owner across all time
    public Double getOwnerTotalProfit(User owner) {
        // ✅ Use efficient repository method to get only confirmed/completed bookings
        List<BookingStatus> validStatuses = List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED);
        List<Booking> allBookings = bookingRepository.findByOwnerAndStatusIn(owner, validStatuses);

        double totalProfit = 0.0;
        for (Booking booking : allBookings) {
            if (booking.getTotalPrice() != null) {
                totalProfit += booking.getTotalPrice();
            }
        }

        return totalProfit;
    }

    // ✅ Get profit summary with different time periods for dashboard
    public Map<String, Object> getOwnerProfitSummary(User owner) {
        Map<String, Object> summary = new TreeMap<>();

        // Different time periods
        int[] periods = { 1, 3, 6, 9, 12, 24 };

        for (int period : periods) {
            Map<String, Double> periodProfits = getOwnerProfitAnalytics(owner, period);
            Double totalForPeriod = periodProfits.values().stream().mapToDouble(Double::doubleValue).sum();

            summary.put(period + "_months", Map.of(
                    "total", totalForPeriod,
                    "monthly_breakdown", periodProfits));
        }

        // Add overall statistics
        summary.put("total_profit", getOwnerTotalProfit(owner));
        summary.put("total_properties", accommodationUnitRepository.findByOwner(owner).size());

        return summary;
    }

    // ✅ Filter units by date availability (checks both Booking and Reservation
    // entities)
    public List<AccommodationUnit> filterUnitsByAvailability(List<AccommodationUnit> units, LocalDate checkIn,
            LocalDate checkOut) {
        return units.stream()
                .filter(unit -> isUnitAvailable(unit, checkIn, checkOut))
                .toList();
    }

    // ✅ Check if a unit is available for the given date range
    private boolean isUnitAvailable(AccommodationUnit unit, LocalDate checkIn, LocalDate checkOut) {
        System.out.println("🔍 Checking availability for unit " + unit.getId() + " from " + checkIn + " to " + checkOut);
        
        // Check both Booking and Reservation entities for conflicts
        
        // 1. Check Bookings (if they exist)
        try {
            List<Booking> existingBookings = bookingRepository.findByAccommodationUnitAndStatusIn(
                unit, 
                List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING)
            );
            
            for (Booking booking : existingBookings) {
                if (datesOverlap(checkIn, checkOut, booking.getCheckInDate(), booking.getCheckOutDate())) {
                    System.out.println("❌ Unit " + unit.getId() + " unavailable - overlaps with booking " + booking.getId() + 
                                     " (" + booking.getCheckInDate() + " to " + booking.getCheckOutDate() + ")");
                    return false;
                }
            }
            System.out.println("✅ No conflicting bookings found for unit " + unit.getId());
        } catch (Exception e) {
            System.out.println("⚠️ Could not check bookings (might not exist): " + e.getMessage());
        }
        
        // 2. Check Reservations (main conflict source based on your SQL)
        try {
            // 🔧 FIXED: Get ALL reservations for this unit and filter manually for accurate results
            List<Reservation> allUnitReservations = reservationRepository.findByUnitId(unit.getId());
            List<Reservation> conflictingReservations = allUnitReservations.stream()
                .filter(res -> res.getStatus() == ReservationStatus.CONFIRMED)
                .filter(res -> datesOverlap(checkIn, checkOut, res.getStartDate(), res.getEndDate()))
                .toList();
            
            if (!conflictingReservations.isEmpty()) {
                System.out.println("❌ Unit " + unit.getId() + " unavailable - has " + conflictingReservations.size() + " confirmed reservations");
                for (Reservation res : conflictingReservations) {
                    System.out.println("   - Reservation " + res.getId() + ": " + res.getStartDate() + " to " + res.getEndDate() + " (status: " + res.getStatus() + ")");
                }
                return false;
            }
            System.out.println("✅ No conflicting reservations found for unit " + unit.getId());
        } catch (Exception e) {
            System.out.println("⚠️ Could not check reservations: " + e.getMessage());
            // If we can't check reservations but SQL shows they exist, this is the problem
            return false;
        }
        
        System.out.println("✅ Unit " + unit.getId() + " is available for the requested dates");
        return true;
    }
    
    // ✅ Check if two date ranges overlap
    private boolean datesOverlap(LocalDate checkIn1, LocalDate checkOut1, LocalDate checkIn2, LocalDate checkOut2) {
        // Two date ranges overlap if: 
        // (checkIn1 < checkOut2) AND (checkOut1 > checkIn2)
        return checkIn1.isBefore(checkOut2) && checkOut1.isAfter(checkIn2);
    }

    @Transactional
    public AccommodationUnit addUnitWithPhotos(AccommodationUnit unit, List<String> photoUrls) {
        System.out.println("=== SERVICE: ADD UNIT WITH PHOTOS ===");
        System.out.println("Unit owner: " + (unit.getOwner() != null ? unit.getOwner().getId() : "null"));
        System.out.println("Photo URLs count: " + (photoUrls != null ? photoUrls.size() : "null"));
        
        // Validate unit and owner
        if (unit == null) {
            throw new IllegalArgumentException("Accommodation unit cannot be null");
        }
        if (unit.getOwner() == null) {
            throw new IllegalArgumentException("Accommodation unit must have an owner");
        }
        
        // Validate photos
        if (photoUrls == null || photoUrls.isEmpty()) {
            throw new IllegalArgumentException("At least one photo is required");
        }
        if (photoUrls.size() > 10) {
            throw new IllegalArgumentException("Maximum 10 photos allowed");
        }
        
        // Validate location uniqueness
        validateLocationUniqueness(unit.getLocation());
        
        // Ensure proper defaults for new units
        unit.setAvailable(true); // ALWAYS set available to true for new units
        unit.setCreatedAt(LocalDate.now());
        
        System.out.println("Setting unit available: " + unit.isAvailable());
        System.out.println("Setting unit status: " + unit.getStatus());
        
        if (unit.getStatus() == null || unit.getStatus().isEmpty()) {
            unit.setStatus("active");
        }
        if (unit.getRating() == null) {
            unit.setRating(0.0);
        }
        if (unit.getReviewCount() == null) {
            unit.setReviewCount(0);
        }
        if (unit.getTotalBookings() == null) {
            unit.setTotalBookings(0);
        }
        if (unit.getMonthlyRevenue() == null) {
            unit.setMonthlyRevenue(0.0);
        }
        if (unit.getImages() == null) {
            unit.setImages(new ArrayList<>());
        }
        if (unit.getAmenities() == null) {
            unit.setAmenities(new ArrayList<>());
        }
        
        // Explicitly set available to true for new units
        unit.setAvailable(true);
        
        System.out.println("Unit before save - Status: " + unit.getStatus() + ", Available: " + unit.isAvailable());
        System.out.println("Unit before save - Owner: " + unit.getOwner().getId() + " (" + unit.getOwner().getEmail() + ")");
        
        // Store photo URLs in the images field for immediate availability
        unit.setImages(new ArrayList<>(photoUrls));
        
        System.out.println("Saving unit to database...");
        AccommodationUnit savedUnit = accommodationUnitRepository.save(unit);
        System.out.println("Unit saved with ID: " + savedUnit.getId());
        System.out.println("Saved unit - Status: " + savedUnit.getStatus() + ", Available: " + savedUnit.isAvailable());
        
        // Also save to AccommodationPhoto entity for photo service compatibility
        try {
            List<AccommodationPhoto> photos = new ArrayList<>();
            for (String photoUrl : photoUrls) {
                photos.add(new AccommodationPhoto(savedUnit.getId(), photoUrl));
            }
            photoService.savePhotos(photos);
            System.out.println("Photos saved to photo service successfully");
        } catch (Exception e) {
            // Log warning but don't fail the unit creation if photo service fails
            System.err.println("Warning: Failed to save to photo service: " + e.getMessage());
        }
        
        System.out.println("Unit creation completed successfully");
        return savedUnit;
    }

    public AccommodationUnit updateUnitWithPhotos(AccommodationUnit unit, List<String> newPhotoUrls) {
        // Get existing images from the unit data if provided
        List<String> existingImages = unit.getImages() != null ? unit.getImages() : new ArrayList<>();
        
        // Combine existing images with new photo URLs
        List<String> allImageUrls = new ArrayList<>(existingImages);
        allImageUrls.addAll(newPhotoUrls);
        
        // Validate total image count
        if (allImageUrls.isEmpty()) {
            throw new IllegalArgumentException("At least one image is required");
        }
        if (allImageUrls.size() > 10) {
            throw new IllegalArgumentException("Maximum 10 images allowed");
        }
        
        // Set the combined image list
        unit.setImages(allImageUrls);
        
        // Validate location uniqueness for update
        validateLocationUniquenessForUpdate(unit.getLocation(), unit.getId());
        
        AccommodationUnit savedUnit = accommodationUnitRepository.save(unit);
        
        // Also save to photo service if it exists (for backward compatibility)
        try {
            photoService.deletePhotosByUnitId(unit.getId());
            List<AccommodationPhoto> photos = new ArrayList<>();
            for (String photoUrl : allImageUrls) {
                photos.add(new AccommodationPhoto(savedUnit.getId(), photoUrl));
            }
            photoService.savePhotos(photos);
        } catch (Exception e) {
            // Log but don't fail if photo service is unavailable
            System.err.println("Warning: Failed to update photo service: " + e.getMessage());
        }
        
        return savedUnit;
    }

    // ✅ Generate profit report data for PDF export
    public ProfitReportDTO generateProfitReportData(User owner, int months) {
        // Get owner's units
        List<AccommodationUnit> ownerUnits = accommodationUnitRepository.findByOwner(owner);
        
        // Calculate total profit and confirmed bookings count
        double totalProfit = 0.0;
        int totalConfirmedBookings = 0;
        List<ProfitReportDTO.UnitProfitSummary> unitProfits = new ArrayList<>();
        
        LocalDate fromDate = (months > 0) ? LocalDate.now().minusMonths(months) : LocalDate.MIN;
        
        for (AccommodationUnit unit : ownerUnits) {
            List<Booking> bookings = bookingRepository.findByAccommodationUnit(unit);
            double unitProfit = 0.0;
            int unitConfirmedBookings = 0;
            
            for (Booking booking : bookings) {
                if ((booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.COMPLETED) &&
                    booking.getTotalPrice() != null &&
                    ((months == 0) || (booking.getCheckInDate() != null && !booking.getCheckInDate().isBefore(fromDate)))) {
                    
                    unitProfit += booking.getTotalPrice();
                    unitConfirmedBookings++;
                    totalConfirmedBookings++;
                }
            }
            
            totalProfit += unitProfit;
            unitProfits.add(new ProfitReportDTO.UnitProfitSummary(
                unit.getName(), 
                unit.getLocation(), 
                unitProfit, 
                unitConfirmedBookings
            ));
        }
        
        // Get monthly profits
        Map<String, Double> monthlyProfits = getOwnerProfitAnalytics(owner, months > 0 ? months : 12);
        
        // Create and return the report DTO
        return new ProfitReportDTO(
            owner.getFirstName() + " " + owner.getLastName(),
            owner.getEmail(),
            totalProfit,
            ownerUnits.size(),
            totalConfirmedBookings,
            monthlyProfits,
            unitProfits
        );
    }

    public static class ProfitResult {
        public Long unitId;
        public String unitName;
        public double profit;

        public ProfitResult(Long unitId, String unitName, double profit) {
            this.unitId = unitId;
            this.unitName = unitName;
            this.profit = profit;
        }

        // Getters (optional for JSON serialization)
        public Long getUnitId() {
            return unitId;
        }

        public String getUnitName() {
            return unitName;
        }

        public double getProfit() {
            return profit;
        }
    }

    public static class MonthlyProfitResult {
        public String month;
        public double totalProfit;

        public MonthlyProfitResult(String month, double totalProfit) {
            this.month = month;
            this.totalProfit = totalProfit;
        }

        public String getMonth() {
            return month;
        }

        public double getTotalProfit() {
            return totalProfit;
        }
    }

    public static class PredictedProfitDTO {
        public String month;
        public double predictedProfit;

        public PredictedProfitDTO(String month, double predictedProfit) {
            this.month = month;
            this.predictedProfit = predictedProfit;
        }

        public String getMonth() {
            return month;
        }

        public double getPredictedProfit() {
            return predictedProfit;
        }
    }

}
