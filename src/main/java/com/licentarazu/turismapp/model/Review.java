package com.licentarazu.turismapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating; // de la 1 la 5

    @Column(length = 1000)
    private String comment;

    private LocalDate createdAt;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private AccommodationUnit accommodationUnit;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // --- Getteri și setteri ---

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public int getRating() { return rating; }

    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public LocalDate getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public AccommodationUnit getAccommodationUnit() { return accommodationUnit; }

    public void setAccommodationUnit(AccommodationUnit accommodationUnit) { this.accommodationUnit = accommodationUnit; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }
}
