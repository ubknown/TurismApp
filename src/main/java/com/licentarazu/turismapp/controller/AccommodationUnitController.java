package com.licentarazu.turismapp.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licentarazu.turismapp.dto.AccommodationUnitDTO;
import com.licentarazu.turismapp.dto.AccommodationUnitWithPhotosDTO;
import com.licentarazu.turismapp.dto.ProfitReportDTO;
import com.licentarazu.turismapp.model.AccommodationPhoto;
import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.AccommodationUnitRepository;
import com.licentarazu.turismapp.repository.UserRepository;
import com.licentarazu.turismapp.service.AccommodationPhotoService;
import com.licentarazu.turismapp.service.AccommodationUnitService;
import com.licentarazu.turismapp.service.PdfReportService;
import com.licentarazu.turismapp.util.AccommodationUnitMapper;

@RestController
@RequestMapping("/api/units")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174", "http://localhost:3000",
        "http://127.0.0.1:5173", "http://127.0.0.1:5174" }, allowCredentials = "true", methods = { RequestMethod.GET,
                RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS })
public class AccommodationUnitController {

    private final AccommodationUnitService unitService;
    private final UserRepository userRepository;
    private final AccommodationUnitRepository unitRepository;
    private final AccommodationPhotoService photoService;
    private final PdfReportService pdfReportService;

    @Autowired
    public AccommodationUnitController(AccommodationUnitService unitService,
            UserRepository userRepository,
            AccommodationUnitRepository unitRepository,
            AccommodationPhotoService photoService,
            PdfReportService pdfReportService) {
        this.unitService = unitService;
        this.userRepository = userRepository;
        this.unitRepository = unitRepository;
        this.photoService = photoService;
        this.pdfReportService = pdfReportService;
    }

    // ✅ Enhanced unit creation with authentication, ownership, and location
    // validation
    @PostMapping
    public ResponseEntity<?> addUnit(@RequestBody AccommodationUnit unit, Authentication authentication) {
        try {
            String email = authentication.getName(); // JWT conține emailul
            User owner = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Set the owner for the unit
            unit.setOwner(owner);

            AccommodationUnit savedUnit = unitService.addUnit(unit);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUnit);

        } catch (IllegalStateException | IllegalArgumentException e) {
            // Location uniqueness violation or validation errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // TODO: Add proper logging for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating unit: " + e.getMessage());
        }
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

    // Caută după ID cu fotografii
    @GetMapping("/{id}")
    public ResponseEntity<AccommodationUnitWithPhotosDTO> getUnitById(@PathVariable Long id) {
        try {
            Optional<AccommodationUnit> unitOpt = unitService.getById(id);
            if (unitOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            AccommodationUnit unit = unitOpt.get();
            List<AccommodationPhoto> photos = photoService.getPhotosByUnitId(id);
            List<String> photoUrls = photos.stream()
                .map(AccommodationPhoto::getPhotoUrl)
                .toList();
            
            AccommodationUnitWithPhotosDTO response = new AccommodationUnitWithPhotosDTO(unit, photoUrls);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Enhanced unit deletion with owner verification
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUnit(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Optional<AccommodationUnit> unitOpt = unitService.getById(id);
            if (unitOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unitatea nu a fost găsită.");
            }

            AccommodationUnit unit = unitOpt.get();

            // Verify that the user owns this unit
            if (!unit.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Nu aveți permisiunea să ștergeți această unitate.");
            }

            unitService.deleteById(id);
            return ResponseEntity.ok("Unitatea a fost ștearsă cu succes!");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Eroare la ștergerea unității: " + e.getMessage());
        }
    }

    // ✅ Enhanced unit update with owner verification and location validation
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUnit(@PathVariable Long id, @RequestBody AccommodationUnit updatedUnit,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Optional<AccommodationUnit> unitOpt = unitService.getById(id);
            if (unitOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unitatea nu a fost găsită.");
            }

            AccommodationUnit existingUnit = unitOpt.get();

            // Verify that the user owns this unit
            if (!existingUnit.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Nu aveți permisiunea să modificați această unitate.");
            }

            AccommodationUnit updated = unitService.updateUnit(id, updatedUnit);
            return ResponseEntity.ok(updated);

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unitatea nu a fost găsită.");
        } catch (Exception e) {
            // TODO: Add proper logging for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Eroare la actualizarea unității: " + e.getMessage());
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

    // Returnează unitățile disponibile într-un interval
    @GetMapping("/available")
    public List<AccommodationUnit> getAvailableUnits(
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return unitService.findAvailableUnits(checkIn, checkOut);
    }

    // Returnează unitățile de cazare aflate într-o rază (km) față de un oraș dat
    @GetMapping("/proximity")
    public List<AccommodationUnit> getUnitsNearCity(
            @RequestParam("city") String city,
            @RequestParam("radius") double radiusKm) {
        return unitService.findUnitsNearCity(city, radiusKm);
    }

    // ✅ Returnează doar unitățile deținute de utilizatorul logat
    @GetMapping("/my-units")
    public ResponseEntity<List<AccommodationUnitDTO>> getMyUnits(Authentication authentication) {
        System.out.println("=== MY UNITS REQUEST ===");
        String email = authentication.getName(); // JWT conține emailul
        System.out.println("User email: " + email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        System.out.println("User found: " + user.getId());

        List<AccommodationUnit> myUnits = unitService.getUnitsByOwner(user);
        System.out.println("Found " + myUnits.size() + " units for user");
        
        // Convert entities to DTOs to prevent circular references
        List<AccommodationUnitDTO> unitDTOs = AccommodationUnitMapper.toDTOList(myUnits);
        System.out.println("Returning " + unitDTOs.size() + " units as DTOs");
        
        return ResponseEntity.ok(unitDTOs);
    }

    // Profit generat de unitățile deținute de utilizatorul logat
    @GetMapping("/my-units/profit")
    public List<AccommodationUnitService.ProfitResult> getMyUnitsProfit(
            Authentication authentication,
            @RequestParam(name = "lastMonths", defaultValue = "0") int lastMonths) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return unitService.calculateProfit(lastMonths, user);
    }

    // Profit lunar pentru unitățile deținute de utilizatorul logat
    @GetMapping("/my-units/profit/monthly")
    public List<AccommodationUnitService.MonthlyProfitResult> getMyUnitsMonthlyProfit(
            Authentication authentication,
            @RequestParam(name = "lastMonths", defaultValue = "0") int lastMonths) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return unitService.getMonthlyProfit(lastMonths, user);
    }

    // Predicție profit lunar pentru unitățile deținute de utilizatorul logat
    @GetMapping("/my-units/profit/predict")
    public List<AccommodationUnitService.PredictedProfitDTO> predictMyUnitsProfit(
            Authentication authentication,
            @RequestParam(name = "lastMonths", defaultValue = "6") int lastMonths,
            @RequestParam(name = "predictMonths", defaultValue = "3") int predictMonths) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return unitService.predictFutureProfits(lastMonths, predictMonths, user);
    }

    // Filtrare avansată: proximitate + disponibilitate
    @GetMapping("/advanced-filter")
    public List<AccommodationUnit> filterUnitsAdvanced(
            @RequestParam String location,
            @RequestParam Double radiusKm,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return unitService.filterUnitsAdvanced(location, radiusKm, checkIn, checkOut);
    }

    // ✅ Public endpoint for unit search and filtering (used by UnitsListPage.jsx)
    @GetMapping("/public")
    public ResponseEntity<List<AccommodationUnitDTO>> getPublicUnits(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String amenities,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        System.out.println("🌐 PUBLIC ENDPOINT ACCESSED - /api/units/public");
        System.out.println("=== PUBLIC UNITS REQUEST DEBUG ===");
        System.out.println("Search: '" + search + "'");
        System.out.println("County: '" + county + "'");
        System.out.println("Type: '" + type + "'");
        System.out.println("Price range: " + minPrice + " - " + maxPrice);
        System.out.println("Capacity: " + capacity);
        System.out.println("MinRating: " + minRating);
        System.out.println("Amenities: '" + amenities + "'");
        System.out.println("Date range: " + checkIn + " to " + checkOut);
        System.out.println("========================================");
        
        // Debug: Check total units in database first
        List<AccommodationUnit> allUnits = unitRepository.findAllActiveAndAvailable();
        System.out.println("📊 TOTAL ACTIVE UNITS IN DB: " + allUnits.size());
        
        List<AccommodationUnit> units;
        
        // If no filters are provided, get ALL active units
        if (isNoFiltersProvided(search, location, county, type, minPrice, maxPrice, capacity, minRating, checkIn, checkOut)) {
            System.out.println("No filters provided, getting all active and available units");
            units = unitRepository.findAllActiveAndAvailable();
            System.out.println("Found " + units.size() + " active units");
        } else {
            System.out.println("Filters provided, applying repository filtering...");
            
            // For search, apply it as location filter if no specific location/county provided
            String locationFilter = null;
            if (county != null && !county.isEmpty()) {
                locationFilter = county;
                System.out.println("Using county filter: " + county);
            } else if (location != null && !location.isEmpty()) {
                locationFilter = location;
                System.out.println("Using location filter: " + location);
            } else if (search != null && !search.isEmpty()) {
                locationFilter = search;
                System.out.println("Using search as location filter: " + search);
            }

            units = unitService.getFilteredUnits(
                    locationFilter,
                    minPrice,
                    maxPrice,
                    capacity,
                    null, // maxCapacity not needed for this endpoint
                    type,
                    minRating
            );
            System.out.println("Units after repository filtering: " + units.size());
            
            // If search is provided and we haven't used it as location filter, apply additional name/description filtering
            if (search != null && !search.isEmpty() && !search.equals(locationFilter)) {
                System.out.println("Applying additional search filtering for: " + search);
                final String searchTerm = search.toLowerCase();
                units = units.stream()
                        .filter(unit -> 
                            (unit.getName() != null && unit.getName().toLowerCase().contains(searchTerm)) ||
                            (unit.getDescription() != null && unit.getDescription().toLowerCase().contains(searchTerm)) ||
                            (unit.getLocation() != null && unit.getLocation().toLowerCase().contains(searchTerm)) ||
                            (unit.getCounty() != null && unit.getCounty().toLowerCase().contains(searchTerm))
                        )
                        .toList();
                System.out.println("Units after search filtering: " + units.size());
            }
        }

        // ✅ Apply date-based availability filter if check-in and check-out dates are provided
        if (checkIn != null && checkOut != null) {
            System.out.println("🗓️ Date filtering requested: " + checkIn + " to " + checkOut);
            
            // Validate dates
            if (checkIn.isAfter(checkOut)) {
                System.out.println("❌ Invalid date range: check-in after check-out");
                return ResponseEntity.ok(List.of()); // Return empty list for invalid date range
            }
            if (checkIn.isBefore(LocalDate.now())) {
                System.out.println("❌ Invalid date range: check-in in the past");
                return ResponseEntity.ok(List.of()); // Return empty list for past dates
            }

            // Filter units that are available in the given date range
            units = unitService.filterUnitsByAvailability(units, checkIn, checkOut);
            System.out.println("📊 Units after date filtering: " + units.size());
        }

        // Convert entities to DTOs to prevent circular references
        List<AccommodationUnitDTO> unitDTOs = AccommodationUnitMapper.toDTOList(units);
        System.out.println("Returning " + unitDTOs.size() + " units as DTOs");
        
        return ResponseEntity.ok(unitDTOs);
    }

    // ✅ Update unit status (for owners)
    @PatchMapping("/{id}/status")
    public ResponseEntity<AccommodationUnit> updateUnitStatus(@PathVariable Long id,
            @RequestBody StatusUpdateRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Optional<AccommodationUnit> unitOpt = unitService.getById(id);
            if (unitOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            AccommodationUnit unit = unitOpt.get();

            // Check if user owns this unit
            if (!unit.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            unit.setStatus(request.getStatus());
            AccommodationUnit updatedUnit = unitService.updateUnit(id, unit);
            return ResponseEntity.ok(updatedUnit);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Helper class for status update request
    public static class StatusUpdateRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    // ✅ Get profit analytics for the authenticated owner
    @GetMapping("/my-units/profit/analytics")
    public ResponseEntity<?> getOwnerProfitAnalytics(
            @RequestParam(defaultValue = "12") int months,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User owner = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Map<String, Double> analytics = unitService.getOwnerProfitAnalytics(owner, months);
            return ResponseEntity.ok(analytics);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching profit analytics: " + e.getMessage());
        }
    }

    // ✅ Get comprehensive profit summary for the authenticated owner
    @GetMapping("/my-units/profit/summary")
    public ResponseEntity<?> getOwnerProfitSummary(Authentication authentication) {
        try {
            String email = authentication.getName();
            User owner = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Map<String, Object> summary = unitService.getOwnerProfitSummary(owner);
            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching profit summary: " + e.getMessage());
        }
    }

    // ✅ Get specific unit details (only if owned by authenticated user)
    @GetMapping("/{id}/owner-details")
    public ResponseEntity<?> getUnitOwnerDetails(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Optional<AccommodationUnit> unitOpt = unitService.getById(id);
            if (unitOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unitatea nu a fost găsită.");
            }

            AccommodationUnit unit = unitOpt.get();

            // Verify that the user owns this unit
            if (!unit.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Nu aveți permisiunea să accesați această unitate.");
            }

            return ResponseEntity.ok(unit);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching unit details: " + e.getMessage());
        }
    }

    // ✅ Debug endpoint to check total units in database - RESTRICTED TO DEV
    @GetMapping("/debug/count")
    @CrossOrigin(origins = { "http://localhost:5174", "http://localhost:5173", "http://localhost:3000" })
    @org.springframework.context.annotation.Profile({"dev", "development"})
    public ResponseEntity<Map<String, Object>> getUnitsDebugInfo() {
        try {
            List<AccommodationUnit> allUnits = unitRepository.findAll();
            List<AccommodationUnit> availableUnits = unitRepository.findByFiltersWithRating(
                    null, null, null, null, null, null, null);

            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("totalUnits", allUnits.size());
            debugInfo.put("availableUnits", availableUnits.size());
            debugInfo.put("allUnitsDetails", allUnits.stream()
                    .map(unit -> Map.of(
                            "id", unit.getId(),
                            "name", unit.getName(),
                            "available", unit.isAvailable(),
                            "location", unit.getLocation() != null ? unit.getLocation() : "null"))
                    .toList());

            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            Map<String, Object> errorInfo = Map.of(
                    "error", e.getMessage(),
                    "cause", e.getCause() != null ? e.getCause().toString() : "unknown");
            return ResponseEntity.ok(errorInfo);
        }
    }

    // ✅ Simple health check endpoint (public) - RESTRICTED TO DEV
    @GetMapping("/debug/health")
    @CrossOrigin(origins = { "http://localhost:5174", "http://localhost:5173", "http://localhost:3000" })
    @org.springframework.context.annotation.Profile({"dev", "development"})
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "service", "AccommodationUnit API");
        return ResponseEntity.ok(health);
    }

    // Endpoint pentru adăugarea unităților cu fotografii
    @PostMapping("/with-photos")
    public ResponseEntity<?> addUnitWithPhotos(
            @RequestParam("unit") String unitJson,
            @RequestParam("photos") MultipartFile[] photos,
            Authentication authentication) {
        
        System.out.println("=== ADD UNIT WITH PHOTOS REQUEST ===");
        System.out.println("Authentication: " + (authentication != null ? authentication.getName() : "null"));
        System.out.println("Unit JSON length: " + (unitJson != null ? unitJson.length() : "null"));
        System.out.println("Photos count: " + (photos != null ? photos.length : "null"));
        
        try {
            String email = authentication.getName();
            System.out.println("User email: " + email);
            
            User owner = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            System.out.println("User found: " + owner.getId());

            // Parse unit JSON
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("Parsing unit JSON...");
            AccommodationUnit unit = mapper.readValue(unitJson, AccommodationUnit.class);
            unit.setOwner(owner);
            System.out.println("Unit parsed successfully: " + unit.getName());

            // Validate photos
            if (photos == null || photos.length == 0) {
                System.out.println("ERROR: No photos provided");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("At least one photo is required");
            }
            if (photos.length > 10) {
                System.out.println("ERROR: Too many photos: " + photos.length);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Maximum 10 photos allowed");
            }
            System.out.println("Photos validation passed: " + photos.length + " photos");

            // Save photos and get URLs
            System.out.println("Saving photo files...");
            List<String> photoUrls = savePhotoFiles(photos);
            System.out.println("Photo files saved: " + photoUrls.size() + " URLs");

            System.out.println("Calling service to save unit...");
            AccommodationUnit savedUnit = unitService.addUnitWithPhotos(unit, photoUrls);
            System.out.println("Unit saved successfully with ID: " + savedUnit.getId());
            
            // Create a detailed response object
            Map<String, Object> response = new HashMap<>();
            response.put("unit", savedUnit);
            response.put("message", "Property created successfully");
            response.put("unitId", savedUnit.getId());
            response.put("ownerEmail", savedUnit.getOwner().getEmail());
            response.put("imageCount", photoUrls.size());
            response.put("createdAt", savedUnit.getCreatedAt());
            
            System.out.println("Returning response with unit ID: " + savedUnit.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("ERROR - IllegalStateException/IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR - Unexpected exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create unit: " + e.getMessage());
        }
    }

    // Endpoint pentru actualizarea unităților cu fotografii
    @PutMapping("/{id}/with-photos")
    public ResponseEntity<?> updateUnitWithPhotos(
            @PathVariable Long id,
            @RequestParam("unit") String unitJson,
            @RequestParam("photos") MultipartFile[] photos,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Optional<AccommodationUnit> unitOpt = unitService.getById(id);
            if (unitOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unit not found");
            }

            AccommodationUnit existingUnit = unitOpt.get();
            if (!existingUnit.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            // Parse unit JSON
            ObjectMapper mapper = new ObjectMapper();
            AccommodationUnit updatedUnit = mapper.readValue(unitJson, AccommodationUnit.class);
            updatedUnit.setId(id);
            updatedUnit.setOwner(user);

            // Calculate total images after adding new photos
            List<String> existingImages = updatedUnit.getImages() != null ? updatedUnit.getImages() : new ArrayList<>();
            int totalImages = existingImages.size() + photos.length;
            
            // Validate total image count
            if (totalImages == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("At least one image is required");
            }
            if (totalImages > 10) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Maximum 10 images allowed total");
            }

            // Save photos and get URLs
            List<String> photoUrls = savePhotoFiles(photos);

            AccommodationUnit savedUnit = unitService.updateUnitWithPhotos(updatedUnit, photoUrls);
            return ResponseEntity.ok(savedUnit);

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update unit: " + e.getMessage());
        }
    }

    // Endpoint pentru obținerea fotografiilor unei unități
    @GetMapping("/{id}/photos")
    public ResponseEntity<List<AccommodationPhoto>> getUnitPhotos(@PathVariable Long id) {
        try {
            List<AccommodationPhoto> photos = photoService.getPhotosByUnitId(id);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Export profit report as PDF
    @GetMapping("/my-units/profit/export-pdf")
    public ResponseEntity<byte[]> exportProfitReportPdf(
            @RequestParam(defaultValue = "12") int months,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User owner = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Generate profit report data
            ProfitReportDTO reportData = unitService.generateProfitReportData(owner, months);
            
            // Generate PDF
            byte[] pdfBytes = pdfReportService.generateProfitReportPdf(reportData);
            
            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("profit-report-" + owner.getFirstName() + "-" + LocalDate.now() + ".pdf")
                    .build());
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating PDF report: " + e.getMessage()).getBytes());
        }
    }

    // Helper method to check if no filters are provided
    private boolean isNoFiltersProvided(String search, String location, String county, String type, 
                                      Double minPrice, Double maxPrice, Integer capacity, Double minRating,
                                      LocalDate checkIn, LocalDate checkOut) {
        return (search == null || search.isEmpty()) &&
               (location == null || location.isEmpty()) &&
               (county == null || county.isEmpty()) &&
               (type == null || type.isEmpty()) &&
               minPrice == null &&
               maxPrice == null &&
               capacity == null &&
               minRating == null &&
               checkIn == null &&
               checkOut == null;
    }

    // Helper method to save photo files and return their URLs
    private List<String> savePhotoFiles(MultipartFile[] photos) throws IOException {
        List<String> photoUrls = new ArrayList<>();
        String uploadDir = "uploads/units/";
        
        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile photo : photos) {
            String originalFilename = photo.getOriginalFilename();
            if (!photo.isEmpty() && originalFilename != null && !originalFilename.isEmpty()) {
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String filename = UUID.randomUUID().toString() + fileExtension;
                
                Path filePath = uploadPath.resolve(filename);
                Files.write(filePath, photo.getBytes());
                
                photoUrls.add("/uploads/units/" + filename);
            }
        }
        
        return photoUrls;
    }

    // ✅ Debug endpoint for testing date filtering - RESTRICTED TO DEV
    @GetMapping("/debug/date-filter")
    @CrossOrigin(origins = { "http://localhost:5174", "http://localhost:5173", "http://localhost:3000" })
    @org.springframework.context.annotation.Profile({"dev", "development"})
    public ResponseEntity<Map<String, Object>> debugDateFiltering(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        
        System.out.println("🔍 DEBUG: Date filtering test for " + checkIn + " to " + checkOut);
        
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("requestedCheckIn", checkIn.toString());
        debugInfo.put("requestedCheckOut", checkOut.toString());
        debugInfo.put("currentDate", LocalDate.now().toString());
        
        try {
            // Get all units first
            List<AccommodationUnit> allUnits = unitRepository.findAllActiveAndAvailable();
            debugInfo.put("totalActiveUnits", allUnits.size());
            
            // Apply date filtering
            List<AccommodationUnit> availableUnits = unitService.filterUnitsByAvailability(allUnits, checkIn, checkOut);
            debugInfo.put("availableUnitsAfterFilter", availableUnits.size());
            
            // Add details for each unit
            List<Map<String, Object>> unitDetails = new ArrayList<>();
            for (AccommodationUnit unit : allUnits.subList(0, Math.min(5, allUnits.size()))) { // Only first 5 for debugging
                Map<String, Object> unitInfo = new HashMap<>();
                unitInfo.put("id", unit.getId());
                unitInfo.put("name", unit.getName());
                unitInfo.put("available", availableUnits.contains(unit));
                unitDetails.add(unitInfo);
            }
            debugInfo.put("unitSample", unitDetails);
            
            debugInfo.put("success", true);
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            debugInfo.put("success", false);
            debugInfo.put("error", e.getMessage());
            debugInfo.put("stackTrace", e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(debugInfo);
        }
    }
}
