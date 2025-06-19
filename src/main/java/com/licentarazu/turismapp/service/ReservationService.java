package com.licentarazu.turismapp.service;

import com.licentarazu.turismapp.exception.ReservationConflictException;
import com.licentarazu.turismapp.model.AccommodationUnit;
import com.licentarazu.turismapp.model.Reservation;
import com.licentarazu.turismapp.model.ReservationStatus;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.AccommodationUnitRepository;
import com.licentarazu.turismapp.repository.ReservationRepository;
import com.licentarazu.turismapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private AccommodationUnitRepository unitRepository;

    @Autowired
    private UserRepository userRepository;

    // Creează o rezervare cu validare de suprapunere
    public Reservation createReservation(Reservation reservation) {
        // Încarcă unitatea și utilizatorul
        Long unitId = reservation.getUnit().getId();
        Long userId = reservation.getUser().getId();

        AccommodationUnit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unitate de cazare inexistentă"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizator inexistent"));

        // Verificare suprapunere rezervări
        List<Reservation> overlapping = reservationRepository
                .findByUnitIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        unitId, reservation.getEndDate(), reservation.getStartDate());

        if (!overlapping.isEmpty()) {
            throw new ReservationConflictException("Unitatea este deja rezervată în perioada aleasă.");
        }

        // Setează entitățile corecte
        reservation.setUnit(unit);
        reservation.setUser(user);
        reservation.setStatus(ReservationStatus.PENDING);

        return reservationRepository.save(reservation);
    }

    // Găsește toate rezervările unui utilizator
    public List<Reservation> getReservationsByUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    // Găsește toate rezervările unei unități
    public List<Reservation> getReservationsByUnit(Long unitId) {
        return reservationRepository.findByUnitId(unitId);
    }

    // Actualizează statusul unei rezervări
    public Optional<Reservation> updateStatus(Long reservationId, ReservationStatus status) {
        Optional<Reservation> reservation = reservationRepository.findById(reservationId);
        reservation.ifPresent(r -> {
            r.setStatus(status);
            reservationRepository.save(r);
        });
        return reservation;
    }

    // Șterge o rezervare
    public void deleteReservation(Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }
}
