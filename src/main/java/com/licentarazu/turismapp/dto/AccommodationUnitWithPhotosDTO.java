package com.licentarazu.turismapp.dto;

import java.time.LocalDate;
import java.util.List;

import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.User;

public class AccommodationUnitWithPhotosDTO {
    private Long id;
    private String name;
    private String description;
    private String location;
    private String county;
    private Double latitude;
    private Double longitude;
    private Double pricePerNight;
    private int capacity;
    private boolean available;
    private LocalDate createdAt;
    private String type;
    private List<String> amenities;
    private User owner;
    private List<String> photoUrls;
    private Double rating;
    private int reviewCount;
    private int totalBookings;

    // Constructors
    public AccommodationUnitWithPhotosDTO() {}

    public AccommodationUnitWithPhotosDTO(AccommodationUnit unit, List<String> photoUrls) {
        this.id = unit.getId();
        this.name = unit.getName();
        this.description = unit.getDescription();
        this.location = unit.getLocation();
        this.county = unit.getCounty();
        this.latitude = unit.getLatitude();
        this.longitude = unit.getLongitude();
        this.pricePerNight = unit.getPricePerNight();
        this.capacity = unit.getCapacity();
        this.available = unit.isAvailable();
        this.createdAt = unit.getCreatedAt();
        this.type = unit.getType();
        this.amenities = unit.getAmenities();
        this.owner = unit.getOwner();
        this.photoUrls = photoUrls;
        this.rating = unit.getRating();
        this.reviewCount = unit.getReviewCount();
        this.totalBookings = unit.getTotalBookings();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(Double pricePerNight) { this.pricePerNight = pricePerNight; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public int getTotalBookings() { return totalBookings; }
    public void setTotalBookings(int totalBookings) { this.totalBookings = totalBookings; }
}
