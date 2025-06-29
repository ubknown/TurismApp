package com.licentarazu.turismapp.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ProfitReportDTO {
    private String ownerName;
    private String ownerEmail;
    private LocalDate reportGeneratedDate;
    private Double totalProfitRON;
    private Integer totalProperties;
    private Integer totalConfirmedBookings;
    private Map<String, Double> monthlyProfitsRON; // Month -> Profit in RON
    private List<UnitProfitSummary> unitProfits;

    // Constructors
    public ProfitReportDTO() {
        this.reportGeneratedDate = LocalDate.now();
    }

    public ProfitReportDTO(String ownerName, String ownerEmail, Double totalProfitRON, 
                          Integer totalProperties, Integer totalConfirmedBookings,
                          Map<String, Double> monthlyProfitsRON, List<UnitProfitSummary> unitProfits) {
        this();
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.totalProfitRON = totalProfitRON;
        this.totalProperties = totalProperties;
        this.totalConfirmedBookings = totalConfirmedBookings;
        this.monthlyProfitsRON = monthlyProfitsRON;
        this.unitProfits = unitProfits;
    }

    // Getters and setters
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public LocalDate getReportGeneratedDate() {
        return reportGeneratedDate;
    }

    public void setReportGeneratedDate(LocalDate reportGeneratedDate) {
        this.reportGeneratedDate = reportGeneratedDate;
    }

    public Double getTotalProfitRON() {
        return totalProfitRON;
    }

    public void setTotalProfitRON(Double totalProfitRON) {
        this.totalProfitRON = totalProfitRON;
    }

    public Integer getTotalProperties() {
        return totalProperties;
    }

    public void setTotalProperties(Integer totalProperties) {
        this.totalProperties = totalProperties;
    }

    public Integer getTotalConfirmedBookings() {
        return totalConfirmedBookings;
    }

    public void setTotalConfirmedBookings(Integer totalConfirmedBookings) {
        this.totalConfirmedBookings = totalConfirmedBookings;
    }

    public Map<String, Double> getMonthlyProfitsRON() {
        return monthlyProfitsRON;
    }

    public void setMonthlyProfitsRON(Map<String, Double> monthlyProfitsRON) {
        this.monthlyProfitsRON = monthlyProfitsRON;
    }

    public List<UnitProfitSummary> getUnitProfits() {
        return unitProfits;
    }

    public void setUnitProfits(List<UnitProfitSummary> unitProfits) {
        this.unitProfits = unitProfits;
    }

    // Inner class for unit profit summary
    public static class UnitProfitSummary {
        private String unitName;
        private String location;
        private Double totalProfitRON;
        private Integer confirmedBookingsCount;

        public UnitProfitSummary() {}

        public UnitProfitSummary(String unitName, String location, Double totalProfitRON, Integer confirmedBookingsCount) {
            this.unitName = unitName;
            this.location = location;
            this.totalProfitRON = totalProfitRON;
            this.confirmedBookingsCount = confirmedBookingsCount;
        }

        public String getUnitName() {
            return unitName;
        }

        public void setUnitName(String unitName) {
            this.unitName = unitName;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public Double getTotalProfitRON() {
            return totalProfitRON;
        }

        public void setTotalProfitRON(Double totalProfitRON) {
            this.totalProfitRON = totalProfitRON;
        }

        public Integer getConfirmedBookingsCount() {
            return confirmedBookingsCount;
        }

        public void setConfirmedBookingsCount(Integer confirmedBookingsCount) {
            this.confirmedBookingsCount = confirmedBookingsCount;
        }
    }
}
