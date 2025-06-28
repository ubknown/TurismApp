package com.licentarazu.turismapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "accommodation_photos")
public class AccommodationPhoto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "accommodation_unit_id", nullable = false)
    private Long accommodationUnitId;
    
    @Column(name = "photo_url", nullable = false)
    private String photoUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_unit_id", insertable = false, updatable = false)
    private AccommodationUnit accommodationUnit;
    
    // Constructors
    public AccommodationPhoto() {}
    
    public AccommodationPhoto(Long accommodationUnitId, String photoUrl) {
        this.accommodationUnitId = accommodationUnitId;
        this.photoUrl = photoUrl;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAccommodationUnitId() {
        return accommodationUnitId;
    }
    
    public void setAccommodationUnitId(Long accommodationUnitId) {
        this.accommodationUnitId = accommodationUnitId;
    }
    
    public String getPhotoUrl() {
        return photoUrl;
    }
    
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    
    public AccommodationUnit getAccommodationUnit() {
        return accommodationUnit;
    }
    
    public void setAccommodationUnit(AccommodationUnit accommodationUnit) {
        this.accommodationUnit = accommodationUnit;
    }
}
