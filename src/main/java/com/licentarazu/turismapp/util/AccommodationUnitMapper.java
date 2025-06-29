package com.licentarazu.turismapp.util;

import com.licentarazu.turismapp.dto.AccommodationUnitDTO;
import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.AccommodationPhoto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AccommodationUnitMapper {

    public static AccommodationUnitDTO toDTO(AccommodationUnit unit) {
        if (unit == null) {
            return null;
        }

        AccommodationUnitDTO dto = new AccommodationUnitDTO();
        dto.setId(unit.getId());
        dto.setName(unit.getName());
        dto.setDescription(unit.getDescription());
        dto.setLocation(unit.getLocation());
        dto.setCounty(unit.getCounty());
        dto.setPhone(unit.getPhone());
        dto.setLatitude(unit.getLatitude());
        dto.setLongitude(unit.getLongitude());
        dto.setPricePerNight(unit.getPricePerNight());
        dto.setCapacity(unit.getCapacity());
        dto.setAvailable(unit.isAvailable());
        dto.setCreatedAt(unit.getCreatedAt());
        dto.setType(unit.getType());
        dto.setRating(unit.getRating());
        dto.setReviewCount(unit.getReviewCount());
        dto.setTotalBookings(unit.getTotalBookings());
        dto.setMonthlyRevenue(unit.getMonthlyRevenue());
        dto.setStatus(unit.getStatus());
        dto.setImages(unit.getImages());
        dto.setAmenities(unit.getAmenities());

        // Owner information (basic info only, no circular references)
        if (unit.getOwner() != null) {
            dto.setOwnerId(unit.getOwner().getId());
            dto.setOwnerFirstName(unit.getOwner().getFirstName());
            dto.setOwnerLastName(unit.getOwner().getLastName());
            dto.setOwnerEmail(unit.getOwner().getEmail());
        }

        // Photo URLs (simple list, no circular references)
        if (unit.getPhotos() != null) {
            System.out.println("=== MAPPING PHOTOS FOR UNIT: " + unit.getName() + " ===");
            System.out.println("Photos collection size: " + unit.getPhotos().size());
            
            List<String> photoUrls = unit.getPhotos().stream()
                    .map(photo -> {
                        String url = photo.getPhotoUrl();
                        System.out.println("Photo URL: " + url);
                        return url;
                    })
                    .collect(Collectors.toList());
            dto.setPhotoUrls(photoUrls);
            
            System.out.println("Final photoUrls list size: " + photoUrls.size());
        } else {
            System.out.println("=== NO PHOTOS FOR UNIT: " + unit.getName() + " ===");
            dto.setPhotoUrls(new ArrayList<>());
        }

        return dto;
    }

    public static List<AccommodationUnitDTO> toDTOList(List<AccommodationUnit> units) {
        if (units == null) {
            return null;
        }
        return units.stream()
                .map(AccommodationUnitMapper::toDTO)
                .collect(Collectors.toList());
    }
}
