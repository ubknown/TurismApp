package com.licentarazu.turismapp.dto;

import java.time.YearMonth;

public class MonthlyProfitDTO {
    private YearMonth month;
    private double totalProfit;

    public MonthlyProfitDTO(YearMonth month, double totalProfit) {
        this.month = month;
        this.totalProfit = totalProfit;
    }

    public YearMonth getMonth() {
        return month;
    }

    public double getTotalProfit() {
        return totalProfit;
    }
}
