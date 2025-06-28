package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.AccommodationUnitRepository;
import com.licentarazu.turismapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:5174"}, allowCredentials = "true")
public class FileUploadController {

    private final AccommodationUnitRepository unitRepository;
    private final UserRepository userRepository;
    
    // Upload directory
    private final String uploadDir = "uploads/units/";

    @Autowired
    public FileUploadController(AccommodationUnitRepository unitRepository, UserRepository userRepository) {
        this.unitRepository = unitRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/unit/{unitId}/images")
    public ResponseEntity<?> uploadUnitImages(@PathVariable Long unitId,
                                            @RequestParam("files") MultipartFile[] files,
                                            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Optional<AccommodationUnit> unitOpt = unitRepository.findById(unitId);
            if (unitOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            AccommodationUnit unit = unitOpt.get();
            
            // Check if user owns this unit
            if (!unit.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission to modify this unit"));
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            List<String> uploadedFiles = new ArrayList<>();
            
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // Generate unique filename
                    String originalFilename = file.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String newFilename = UUID.randomUUID().toString() + extension;
                    
                    // Save file
                    Path filePath = uploadPath.resolve(newFilename);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Add to list
                    uploadedFiles.add("/uploads/units/" + newFilename);
                }
            }

            // Update unit with new image URLs
            List<String> currentImages = unit.getImages();
            if (currentImages == null) {
                currentImages = new ArrayList<>();
            }
            currentImages.addAll(uploadedFiles);
            unit.setImages(currentImages);
            
            AccommodationUnit savedUnit = unitRepository.save(unit);

            return ResponseEntity.ok(Map.of(
                "message", "Images uploaded successfully",
                "uploadedFiles", uploadedFiles,
                "unit", savedUnit
            ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to upload files: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/unit/{unitId}/images")
    public ResponseEntity<?> deleteUnitImage(@PathVariable Long unitId,
                                           @RequestParam("imageUrl") String imageUrl,
                                           Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Optional<AccommodationUnit> unitOpt = unitRepository.findById(unitId);
            if (unitOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            AccommodationUnit unit = unitOpt.get();
            
            // Check if user owns this unit
            if (!unit.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission to modify this unit"));
            }

            // Remove image from unit
            List<String> currentImages = unit.getImages();
            if (currentImages != null) {
                currentImages.remove(imageUrl);
                unit.setImages(currentImages);
                unitRepository.save(unit);
            }

            // Try to delete the physical file
            try {
                String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadDir + filename);
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                // Log but don't fail the request if file deletion fails
                System.err.println("Failed to delete file: " + e.getMessage());
            }

            return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
