package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.model.Reservation;
import com.licentarazu.turismapp.model.ReservationStatus;
import com.licentarazu.turismapp.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:5174"}, allowCredentials = "true")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    // POST - creează o rezervare nouă
    @PostMapping
    public Reservation createReservation(@RequestBody Reservation reservation) {
        return reservationService.createReservation(reservation);
    }

    // GET - rezervările unui utilizator
    @GetMapping("/user/{userId}")
    public List<Reservation> getReservationsByUser(@PathVariable Long userId) {
        return reservationService.getReservationsByUser(userId);
    }

    // GET - rezervările unei unități
    @GetMapping("/unit/{unitId}")
    public List<Reservation> getReservationsByUnit(@PathVariable Long unitId) {
        return reservationService.getReservationsByUnit(unitId);
    }

    // PUT - actualizează statusul unei rezervări
    @PutMapping("/{reservationId}/status")
    public Optional<Reservation> updateStatus(@PathVariable Long reservationId,
                                              @RequestParam ReservationStatus status) {
        return reservationService.updateStatus(reservationId, status);
    }

    // DELETE - șterge o rezervare
    @DeleteMapping("/{reservationId}")
    public void deleteReservation(@PathVariable Long reservationId) {
        reservationService.deleteReservation(reservationId);
    }
}
