package com.licentarazu.turismapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.model.ReservationStatus;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    private AccommodationUnit unit;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfGuests;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    // --- Getters and setters ---

    public Long getId() {
        return id;
    }

    public AccommodationUnit getUnit() {
        return unit;
    }

    public void setUnit(AccommodationUnit unit) {
        this.unit = unit;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
