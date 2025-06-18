package com.licentarazu.turismapp.repository;

import com.licentarazu.turismapp.model.Reservation;
import com.licentarazu.turismapp.dto.MonthlyProfitDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByUnitId(Long unitId);

    // 🔴 Metoda care verifică dacă există rezervări suprapuse
    List<Reservation> findByUnitIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long unitId,
            LocalDate endDate,
            LocalDate startDate
    );

    // ✅ Metoda care calculează profitul lunar (dacă DATEDIFF merge)
    @Query("SELECT new com.licentarazu.turismapp.dto.MonthlyProfitDTO(" +
            "   FUNCTION('YEAR_MONTH', r.startDate), " +
            "   SUM(r.unit.pricePerNight * FUNCTION('DATEDIFF', r.endDate, r.startDate))" +
            ") " +
            "FROM Reservation r " +
            "WHERE r.unit.id = :unitId " +
            "GROUP BY FUNCTION('YEAR_MONTH', r.startDate) " +
            "ORDER BY FUNCTION('YEAR_MONTH', r.startDate)")
    List<MonthlyProfitDTO> findMonthlyProfitsByUnitId(@Param("unitId") Long unitId);
}
