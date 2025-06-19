package com.licentarazu.turismapp.service;

import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProfitService {

    @Autowired
    private BookingRepository bookingRepository;

    public double predictProfit(int monthsAhead) {
        Map<YearMonth, Double> profitByMonth = bookingRepository.findAll().stream()
                .filter(b -> b.getCheckInDate() != null && b.getCheckOutDate() != null)
                .collect(Collectors.groupingBy(
                        b -> YearMonth.from(b.getCheckInDate()),
                        Collectors.summingDouble(b -> {
                            long nights = ChronoUnit.DAYS.between(b.getCheckInDate(), b.getCheckOutDate());
                            if (b.getAccommodationUnit() != null && b.getAccommodationUnit().getPricePerNight() != null) {
                                return nights * b.getAccommodationUnit().getPricePerNight();
                            }
                            return 0.0;
                        })
                ));

        if (profitByMonth.size() < 2) return 0.0;

        List<YearMonth> sortedMonths = profitByMonth.keySet().stream()
                .sorted()
                .toList();

        int n = sortedMonths.size();
        double[] x = new double[n];
        double[] y = new double[n];

        for (int i = 0; i < n; i++) {
            x[i] = i + 1;
            y[i] = profitByMonth.get(sortedMonths.get(i));
        }

        double xSum = 0, ySum = 0, xySum = 0, xSquaredSum = 0;
        for (int i = 0; i < n; i++) {
            xSum += x[i];
            ySum += y[i];
            xySum += x[i] * y[i];
            xSquaredSum += x[i] * x[i];
        }

        double slope = (n * xySum - xSum * ySum) / (n * xSquaredSum - xSum * xSum);
        double intercept = (ySum - slope * xSum) / n;

        double predictedX = n + monthsAhead;
        return slope * predictedX + intercept;
    }
}
