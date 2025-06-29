package com.licentarazu.turismapp.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class BookingRequestDTO {

    @NotNull(message = "Accommodation unit ID is required")
    private Long accommodationUnitId;

    @NotBlank(message = "Guest name is required")
    @Size(min = 2, max = 100, message = "Guest name must be between 2 and 100 characters")
    private String guestName;

    @NotBlank(message = "Guest email is required")
    @Email(message = "Please provide a valid email address")
    private String guestEmail;

    private String guestPhone; // Optional

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @FutureOrPresent(message = "Check-out date cannot be in the past")
    private LocalDate checkOutDate;

    @Min(value = 1, message = "Number of guests must be at least 1")
    @Max(value = 20, message = "Number of guests cannot exceed 20")
    private Integer numberOfGuests = 1;

    @Size(max = 500, message = "Special requests cannot exceed 500 characters")
    private String specialRequests;

    @DecimalMin(value = "0.0", inclusive = false, message = "Total price must be greater than 0")
    private Double totalPrice;

    // Default constructor
    public BookingRequestDTO() {}

    // Constructor with required fields
    public BookingRequestDTO(Long accommodationUnitId, String guestName, String guestEmail, 
                           LocalDate checkInDate, LocalDate checkOutDate) {
        this.accommodationUnitId = accommodationUnitId;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    // Getters and setters
    public Long getAccommodationUnitId() {
        return accommodationUnitId;
    }

    public void setAccommodationUnitId(Long accommodationUnitId) {
        this.accommodationUnitId = accommodationUnitId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "BookingRequestDTO{" +
                "accommodationUnitId=" + accommodationUnitId +
                ", guestName='" + guestName + '\'' +
                ", guestEmail='" + guestEmail + '\'' +
                ", guestPhone='" + guestPhone + '\'' +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", numberOfGuests=" + numberOfGuests +
                ", specialRequests='" + specialRequests + '\'' +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
