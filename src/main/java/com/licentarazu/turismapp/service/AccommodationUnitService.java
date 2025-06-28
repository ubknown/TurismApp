package com.licentarazu.turismapp.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.licentarazu.turismapp.model.AccommodationPhoto;
import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.model.Reservation;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.AccommodationUnitRepository;
import com.licentarazu.turismapp.repository.BookingRepository;
import com.licentarazu.turismapp.repository.ReservationRepository;
import com.licentarazu.turismapp.util.GeoUtils;

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

    // Returnează toate unitățile
    public List<AccommodationUnit> getAllUnits() {
        return accommodationUnitRepository.findAll();
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
        return accommodationUnitRepository.findByFiltersWithRating(location, minPrice, maxPrice,
                minCapacity, maxCapacity, type, minRating);
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

    public List<ProfitResult> calculateProfit(int months, User owner) {
        List<AccommodationUnit> myUnits = accommodationUnitRepository.findByOwner(owner);
        LocalDate now = LocalDate.now();
        LocalDate fromDate = (months > 0) ? now.minusMonths(months) : LocalDate.MIN;
        List<ProfitResult> result = new ArrayList<>();
        for (AccommodationUnit unit : myUnits) {
            double profit = 0.0;
            List<Booking> bookings = bookingRepository.findByAccommodationUnit(unit);
            for (Booking booking : bookings) {
                if ((months == 0)
                        || (booking.getCheckInDate() != null && !booking.getCheckInDate().isBefore(fromDate))) {
                    long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
                    if (nights > 0 && unit.getPricePerNight() != null) {
                        profit += nights * unit.getPricePerNight();
                    }
                }
            }
            result.add(new ProfitResult(unit.getId(), unit.getName(), profit));
        }
        return result;
    }

    public List<MonthlyProfitResult> getMonthlyProfit(int months, User owner) {
        List<AccommodationUnit> myUnits = accommodationUnitRepository.findByOwner(owner);
        LocalDate now = LocalDate.now();
        LocalDate fromDate = (months > 0) ? now.minusMonths(months) : LocalDate.MIN;
        Map<YearMonth, Double> profitByMonth = new TreeMap<>();
        for (AccommodationUnit unit : myUnits) {
            List<Booking> bookings = bookingRepository.findByAccommodationUnit(unit);
            for (Booking booking : bookings) {
                if ((months == 0)
                        || (booking.getCheckInDate() != null && !booking.getCheckInDate().isBefore(fromDate))) {
                    long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
                    if (nights > 0 && unit.getPricePerNight() != null) {
                        YearMonth ym = YearMonth.from(booking.getCheckInDate());
                        double profit = nights * unit.getPricePerNight();
                        profitByMonth.put(ym, profitByMonth.getOrDefault(ym, 0.0) + profit);
                    }
                }
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
     * Get all accommodation units owned by a specific user
     */
    public List<AccommodationUnit> getUnitsByOwner(User owner) {
        return accommodationUnitRepository.findByOwner(owner);
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

    // ✅ Get profit analytics for a specific owner across different time periods
    public Map<String, Double> getOwnerProfitAnalytics(User owner, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        List<AccommodationUnit> ownerUnits = accommodationUnitRepository.findByOwner(owner);
        Map<String, Double> monthlyProfits = new TreeMap<>();

        // Initialize all months with 0
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            String monthKey = ym.getYear() + "-" + String.format("%02d", ym.getMonthValue());
            monthlyProfits.put(monthKey, 0.0);
        }

        // Calculate profits for each unit
        for (AccommodationUnit unit : ownerUnits) {
            List<Booking> bookings = bookingRepository.findByAccommodationUnit_IdAndCheckInDateBetween(
                    unit.getId(), startDate, endDate);

            for (Booking booking : bookings) {
                if (booking.getCheckInDate() != null && booking.getTotalPrice() != null) {
                    YearMonth bookingMonth = YearMonth.from(booking.getCheckInDate());
                    String monthKey = bookingMonth.getYear() + "-"
                            + String.format("%02d", bookingMonth.getMonthValue());

                    monthlyProfits.merge(monthKey, booking.getTotalPrice(), Double::sum);
                }
            }
        }

        return monthlyProfits;
    }

    // ✅ Get total profit for owner across all time
    public Double getOwnerTotalProfit(User owner) {
        List<AccommodationUnit> ownerUnits = accommodationUnitRepository.findByOwner(owner);
        double totalProfit = 0.0;

        for (AccommodationUnit unit : ownerUnits) {
            List<Booking> allBookings = bookingRepository.findByAccommodationUnit(unit);

            for (Booking booking : allBookings) {
                if (booking.getTotalPrice() != null) {
                    totalProfit += booking.getTotalPrice();
                }
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
        // Check if unit is marked as available
        if (!unit.isAvailable()) {
            return false;
        }

        // Check overlapping bookings (if using Booking entity)
        List<Booking> overlappingBookings = bookingRepository
                .findByAccommodationUnitAndCheckOutDateAfterAndCheckInDateBefore(unit, checkIn, checkOut);

        if (!overlappingBookings.isEmpty()) {
            return false;
        }

        // Check overlapping reservations (if using Reservation entity)
        List<Reservation> overlappingReservations = reservationRepository
                .findByUnitIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        unit.getId(), checkOut, checkIn);

        return overlappingReservations.isEmpty();
    }

    public AccommodationUnit addUnitWithPhotos(AccommodationUnit unit, List<String> photoUrls) {
        // Validate photos
        if (photoUrls == null || photoUrls.isEmpty()) {
            throw new IllegalArgumentException("At least one photo is required");
        }
        if (photoUrls.size() > 10) {
            throw new IllegalArgumentException("Maximum 10 photos allowed");
        }
        
        // Validate location uniqueness
        validateLocationUniqueness(unit.getLocation());
        
        unit.setCreatedAt(LocalDate.now());
        AccommodationUnit savedUnit = accommodationUnitRepository.save(unit);
        
        // Save photos
        List<AccommodationPhoto> photos = new ArrayList<>();
        for (String photoUrl : photoUrls) {
            photos.add(new AccommodationPhoto(savedUnit.getId(), photoUrl));
        }
        photoService.savePhotos(photos);
        
        return savedUnit;
    }

    public AccommodationUnit updateUnitWithPhotos(AccommodationUnit unit, List<String> photoUrls) {
        // Validate photos
        if (photoUrls == null || photoUrls.isEmpty()) {
            throw new IllegalArgumentException("At least one photo is required");
        }
        if (photoUrls.size() > 10) {
            throw new IllegalArgumentException("Maximum 10 photos allowed");
        }
        
        // Validate location uniqueness for update
        validateLocationUniquenessForUpdate(unit.getLocation(), unit.getId());
        
        AccommodationUnit savedUnit = accommodationUnitRepository.save(unit);
        
        // Delete existing photos and save new ones
        photoService.deletePhotosByUnitId(unit.getId());
        List<AccommodationPhoto> photos = new ArrayList<>();
        for (String photoUrl : photoUrls) {
            photos.add(new AccommodationPhoto(savedUnit.getId(), photoUrl));
        }
        photoService.savePhotos(photos);
        
        return savedUnit;
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
