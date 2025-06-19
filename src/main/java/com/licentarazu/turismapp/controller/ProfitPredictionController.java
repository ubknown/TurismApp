package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.service.ProfitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profit")
public class ProfitPredictionController {

    @Autowired
    private ProfitService profitService;

    /**
     * Predicția profitului pentru un număr de luni în viitor.
     * Exemplu: /api/profit/predict?monthsAhead=3
     */
    @GetMapping("/predict")
    public ResponseEntity<Double> predictProfit(@RequestParam(defaultValue = "1") int monthsAhead) {
        double prediction = profitService.predictProfit(monthsAhead);
        return ResponseEntity.ok(prediction);
    }
}
