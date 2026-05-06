package com.hospitalqueue.hospital_queue_backend.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.hospitalqueue.hospital_queue_backend.enums.BookingStatus;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bookings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "booking_date", "slot_time"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "token_number")
    private Integer tokenNumber;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
