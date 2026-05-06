package com.hospitalqueue.hospital_queue_backend.repository;

import com.hospitalqueue.hospital_queue_backend.entity.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Used for concurrency control - SELECT FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.doctor.id = :doctorId AND b.bookingDate = :date AND b.slotTime = :slotTime AND b.status <> 'CANCELLED'")
    Optional<Booking> findSlotWithLock(@Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("slotTime") LocalTime slotTime);

    // Count confirmed bookings for a doctor on a date (for max patients check)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.doctor.id = :doctorId AND b.bookingDate = :date AND b.status = 'CONFIRMED'")
    int countConfirmedBookings(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // All booked slots for a doctor on a date (to filter out from available slots)
    @Query("SELECT b.slotTime FROM Booking b WHERE b.doctor.id = :doctorId AND b.bookingDate = :date AND b.status <> 'CANCELLED'")
    List<LocalTime> findBookedSlotTimes(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // Live queue for doctor - confirmed bookings ordered by token
    @Query("SELECT b FROM Booking b WHERE b.doctor.id = :doctorId AND b.bookingDate = :date AND b.status = 'CONFIRMED' ORDER BY b.tokenNumber ASC")
    List<Booking> findLiveQueue(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // Patient's bookings
    List<Booking> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    // Cleanup: delete PENDING bookings older than 15 minutes
    @Modifying
    @Query("DELETE FROM Booking b WHERE b.status = 'PENDING' AND b.createdAt < :cutoff")
    void deleteStalePendingBookings(@Param("cutoff") LocalDateTime cutoff);

    // Next token number for a doctor on a date
    @Query("SELECT COALESCE(MAX(b.tokenNumber), 0) + 1 FROM Booking b WHERE b.doctor.id = :doctorId AND b.bookingDate = :date AND b.status = 'CONFIRMED'")
    int getNextTokenNumber(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // Count completed bookings today (for doctor dashboard)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.doctor.id = :doctorId AND b.bookingDate = :date AND b.status = 'COMPLETED'")
    int countCompletedToday(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // Find booking by id and patient (security check)
    Optional<Booking> findByIdAndPatientId(Long id, Long patientId);

    // Queue position: how many confirmed tokens before mine
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.doctor.id = :doctorId AND b.bookingDate = :date AND b.status = 'CONFIRMED' AND b.tokenNumber < :tokenNumber")
    int countTokensAhead(@Param("doctorId") Long doctorId, @Param("date") LocalDate date, @Param("tokenNumber") int tokenNumber);
}
