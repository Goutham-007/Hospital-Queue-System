package com.hospitalqueue.hospital_queue_backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospitalqueue.hospital_queue_backend.dto.request.BookingRequest;
import com.hospitalqueue.hospital_queue_backend.entity.Booking;
import com.hospitalqueue.hospital_queue_backend.services.BookingService;
import com.hospitalqueue.hospital_queue_backend.services.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class BookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    /**
     * Patient books a slot. Returns Razorpay order details needed by frontend
     * to open payment modal. Booking is PENDING until webhook confirms payment.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BookingRequest request) {

        // 1. Create PENDING booking (with SELECT FOR UPDATE concurrency lock)
        Booking pendingBooking = bookingService.createPendingBooking(
                userDetails.getUsername(), request);

        // 2. Create Razorpay order
        Map<String, Object> paymentData = paymentService.createOrder(
                userDetails.getUsername(), pendingBooking);

        return ResponseEntity.ok(paymentData);
    }
}
