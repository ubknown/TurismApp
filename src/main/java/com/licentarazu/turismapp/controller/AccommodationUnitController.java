package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.service.AccommodationUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/units")
@CrossOrigin(origins = "*")
public class AccommodationUnitController {

    private final AccommodationUnitService unitService;

    @Autowired
    public AccommodationUnitController(AccommodationUnitService unitService) {
        this.unitService = unitService;
    }

    // Adaugă o unitate nouă
    @PostMapping
    public AccommodationUnit addUnit(@RequestBody AccommodationUnit unit) {
        return unitService.addUnit(unit);
    }

    // Returnează toate unitățile
    @GetMapping
    public List<AccommodationUnit> getAllUnits() {
        return unitService.getAllUnits();
    }

    // Caută după locație
    @GetMapping("/search")
    public List<AccommodationUnit> searchByLocation(@RequestParam String location) {
        return unitService.searchByLocation(location);
    }

    // Caută după ID
    @GetMapping("/{id}")
    public Optional<AccommodationUnit> getUnitById(@PathVariable Long id) {
        return unitService.getById(id);
    }

    // Șterge o unitate după ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUnit(@PathVariable Long id) {
        Optional<AccommodationUnit> unit = unitService.getById(id);
        if (unit.isPresent()) {
            unitService.deleteById(id);
            return ResponseEntity.ok("Unitatea a fost ștearsă cu succes!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unitatea nu a fost găsită.");
        }
    }

    // Modifică o unitate existentă după ID
    @PutMapping("/{id}")
    public ResponseEntity<AccommodationUnit> updateUnit(@PathVariable Long id, @RequestBody AccommodationUnit updatedUnit) {
        try {
            AccommodationUnit updated = unitService.updateUnit(id, updatedUnit);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Filtrare după locație, preț, capacitate și tip
    @GetMapping("/filter")
    public List<AccommodationUnit> filterUnits(@RequestParam(required = false) String location,
                                               @RequestParam(required = false) Double minPrice,
                                               @RequestParam(required = false) Double maxPrice,
                                               @RequestParam(required = false) Integer minCapacity,
                                               @RequestParam(required = false) Integer maxCapacity,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(required = false) Double minRating) {
        return unitService.getFilteredUnits(location, minPrice, maxPrice, minCapacity, maxCapacity, type, minRating);
    }

    // Returnează unitățile de cazare disponibile într-un interval de check-in și check-out
    @GetMapping("/available")
    public List<AccommodationUnit> getAvailableUnits(
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return unitService.findAvailableUnits(checkIn, checkOut);
    }
// ... celelalte metode

    // Returnează unitățile de cazare aflate într-o rază (km) față de un oraș dat
    @GetMapping("/proximity")
    public List<AccommodationUnit> getUnitsNearCity(
            @RequestParam("city") String city,
            @RequestParam("radius") double radiusKm) {
        return unitService.findUnitsNearCity(city, radiusKm);
    }

}
