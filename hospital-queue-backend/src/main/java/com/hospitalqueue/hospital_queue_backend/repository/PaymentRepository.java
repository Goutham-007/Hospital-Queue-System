package com.hospitalqueue.hospital_queue_backend.repository;

import com.hospitalqueue.hospital_queue_backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(String orderId);

    Optional<Payment> findByBookingId(Long bookingId);

    // Total earnings for doctor today
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.booking.doctor.id = :doctorId AND DATE(p.paidAt) = :date AND p.status = 'SUCCESS'")
    BigDecimal getTotalEarningsToday(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
}
