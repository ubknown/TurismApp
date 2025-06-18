package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.model.Booking;
import com.licentarazu.turismapp.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Creează o rezervare nouă dacă unitatea este disponibilă
    @PostMapping
    public ResponseEntity<String> createBooking(@RequestBody Booking booking) {
        boolean success = bookingService.createBooking(booking);
        if (success) {
            return ResponseEntity.ok("Rezervarea a fost creată cu succes.");
        } else {
            return ResponseEntity.badRequest().body("Unitatea nu este disponibilă în perioada aleasă.");
        }
    }

    // Returnează toate rezervările existente
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    // Returnează toate rezervările pentru o unitate de cazare
    @GetMapping("/by-unit/{unitId}")
    public List<Booking> getBookingsByUnit(@PathVariable Long unitId) {
        return bookingService.getBookingsByUnit(unitId);
    }

    // Șterge o rezervare după ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        boolean deleted = bookingService.deleteBooking(id);
        if (deleted) {
            return ResponseEntity.ok("Rezervarea a fost anulată cu succes.");
        } else {
            return ResponseEntity.status(404).body("Rezervarea nu a fost găsită.");
        }
    }

    // ✅ Returnează profitul lunar pentru o unitate turistică, în ultimele N luni
    @GetMapping("/profit/by-unit/{unitId}")
    public ResponseEntity<Map<String, Double>> getMonthlyProfitByUnit(
            @PathVariable Long unitId,
            @RequestParam(defaultValue = "6") int months) {

        Map<String, Double> profitData = bookingService.calculateMonthlyProfitForUnit(unitId, months);
        return ResponseEntity.ok(profitData);
    }
}
