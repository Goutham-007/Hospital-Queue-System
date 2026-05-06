package com.hospitalqueue.hospital_queue_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospitalqueue.hospital_queue_backend.dto.response.BookingResponse;
import com.hospitalqueue.hospital_queue_backend.dto.response.QueueResponse;
import com.hospitalqueue.hospital_queue_backend.services.BookingService;
import com.hospitalqueue.hospital_queue_backend.services.QueueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {

    private final BookingService bookingService;
    private final QueueService queueService;

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookingService.getMyBookings(userDetails.getUsername()));
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(userDetails.getUsername(), bookingId));
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Map<String, String>> cancelBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {
        bookingService.cancelBooking(userDetails.getUsername(), bookingId);
        return ResponseEntity.ok(Map.of("message", "Booking cancelled"));
    }

    // Patient tracks queue for their doctor in real time (REST fallback - WebSocket is primary)
    @GetMapping("/queue/{doctorId}")
    public ResponseEntity<List<QueueResponse>> getQueue(@PathVariable Long doctorId) {
        return ResponseEntity.ok(queueService.getQueueForDoctor(doctorId));
    }
}
