package com.licentarazu.turismapp.dto;

import java.time.YearMonth;

public class MonthlyProfitDTO {
    private YearMonth month;
    private double totalProfit;

    // Constructorul necesar pentru query-ul JPA
    public MonthlyProfitDTO(int year, int month, double totalProfit) {
        this.month = YearMonth.of(year, month);
        this.totalProfit = totalProfit;
    }

    // Opțional: păstrează-l și pe acesta, pentru alte utilizări
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
