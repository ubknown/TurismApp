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

    /**
     * Enhanced AI-powered profit prediction using multiple algorithms
     * Combines linear regression, seasonal analysis, and growth patterns
     */
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

        // Linear regression prediction
        double linearPrediction = calculateLinearRegression(x, y, n + monthsAhead);
        
        // Seasonal adjustment (basic seasonality pattern)
        double seasonalAdjustment = calculateSeasonalAdjustment(sortedMonths, profitByMonth, monthsAhead);
        
        // Growth trend analysis
        double growthFactor = calculateGrowthFactor(y, n);
        
        // Weighted combination of predictions
        double finalPrediction = (linearPrediction * 0.6) + (seasonalAdjustment * 0.3) + (growthFactor * 0.1);
        
        // Ensure non-negative result
        return Math.max(0, finalPrediction);
    }

    private double calculateLinearRegression(double[] x, double[] y, double predictedX) {
        int n = x.length;
        double xSum = 0, ySum = 0, xySum = 0, xSquaredSum = 0;
        
        for (int i = 0; i < n; i++) {
            xSum += x[i];
            ySum += y[i];
            xySum += x[i] * y[i];
            xSquaredSum += x[i] * x[i];
        }

        double slope = (n * xySum - xSum * ySum) / (n * xSquaredSum - xSum * xSum);
        double intercept = (ySum - slope * xSum) / n;

        return slope * predictedX + intercept;
    }

    private double calculateSeasonalAdjustment(List<YearMonth> months, Map<YearMonth, Double> profits, int monthsAhead) {
        if (months.size() < 12) {
            // Not enough data for seasonal analysis, use average
            return profits.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }
        
        // Calculate average profit for each month across years
        Map<Integer, Double> monthlyAverages = profits.entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getKey().getMonthValue(),
                        Collectors.averagingDouble(Map.Entry::getValue)
                ));
        
        // Predict based on seasonal pattern
        YearMonth futureMonth = months.get(months.size() - 1).plusMonths(monthsAhead);
        int targetMonth = futureMonth.getMonthValue();
        
        return monthlyAverages.getOrDefault(targetMonth, 
                profits.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
    }

    private double calculateGrowthFactor(double[] y, int n) {
        if (n < 3) return 0.0;
        
        // Calculate month-over-month growth rates
        double totalGrowthRate = 0.0;
        int validGrowthPoints = 0;
        
        for (int i = 1; i < n; i++) {
            if (y[i - 1] > 0) {
                double growthRate = (y[i] - y[i - 1]) / y[i - 1];
                totalGrowthRate += growthRate;
                validGrowthPoints++;
            }
        }
        
        if (validGrowthPoints == 0) return 0.0;
        
        double averageGrowthRate = totalGrowthRate / validGrowthPoints;
        double lastProfit = y[n - 1];
        
        // Apply average growth rate for prediction
        return lastProfit * (1 + averageGrowthRate);
    }

    /**
     * Calculate confidence score for predictions based on data quality
     */
    public double getPredictionConfidence(Map<YearMonth, Double> profitData) {
        if (profitData.size() < 3) return 0.2; // Low confidence with minimal data
        if (profitData.size() < 6) return 0.6; // Medium confidence
        if (profitData.size() < 12) return 0.8; // High confidence
        return 0.9; // Very high confidence with year+ of data
    }

    /**
     * Get trend analysis for dashboard insights
     */
    public String getTrendAnalysis(Map<YearMonth, Double> profitData) {
        if (profitData.size() < 2) return "Insufficient data for trend analysis";
        
        List<Double> values = profitData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();
        
        double firstHalf = values.subList(0, values.size() / 2).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);
        double secondHalf = values.subList(values.size() / 2, values.size()).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);
        
        double growthRate = firstHalf > 0 ? ((secondHalf - firstHalf) / firstHalf) * 100 : 0;
        
        if (growthRate > 15) return "Strong upward trend detected";
        if (growthRate > 5) return "Moderate growth trend";
        if (growthRate > -5) return "Stable profit pattern";
        if (growthRate > -15) return "Slight downward trend";
        return "Declining profit trend - optimization needed";
    }
}
