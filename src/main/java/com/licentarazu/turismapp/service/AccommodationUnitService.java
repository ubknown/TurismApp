package com.licentarazu.turismapp.service;

import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.repository.AccommodationUnitRepository;
import com.licentarazu.turismapp.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.licentarazu.turismapp.util.GeoUtils;
import com.licentarazu.turismapp.service.CityCoordinatesService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccommodationUnitService {

    private final AccommodationUnitRepository accommodationUnitRepository;
    private final BookingRepository bookingRepository;
    private final CityCoordinatesService cityCoordinatesService;



    @Autowired
    public AccommodationUnitService(AccommodationUnitRepository accommodationUnitRepository,
                                    BookingRepository bookingRepository,
                                    CityCoordinatesService cityCoordinatesService) {
        this.accommodationUnitRepository = accommodationUnitRepository;
        this.bookingRepository = bookingRepository;
        this.cityCoordinatesService = cityCoordinatesService;
    }


    // Adaugă o unitate nouă
    public AccommodationUnit addUnit(AccommodationUnit unit) {
        unit.setCreatedAt(LocalDate.now());
        return accommodationUnitRepository.save(unit);
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

    // Modifică o unitate existentă
    public AccommodationUnit updateUnit(Long id, AccommodationUnit updatedUnit) {
        Optional<AccommodationUnit> optionalUnit = accommodationUnitRepository.findById(id);

        if (optionalUnit.isEmpty()) {
            throw new RuntimeException("Unitatea cu ID-ul " + id + " nu a fost găsită.");
        }

        AccommodationUnit existingUnit = optionalUnit.get();

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
            if (unit.getLatitude() == null || unit.getLongitude() == null) continue;

            double distance = GeoUtils.distanceInKm(
                    targetLat, targetLon,
                    unit.getLatitude(), unit.getLongitude());

            if (distance <= radiusKm) {
                nearbyUnits.add(unit);
            }
        }

        return nearbyUnits;
    }

}
