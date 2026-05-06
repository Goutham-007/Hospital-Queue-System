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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospitalqueue.hospital_queue_backend.dto.request.AvailabilityRequest;
import com.hospitalqueue.hospital_queue_backend.dto.request.LeaveRequest;
import com.hospitalqueue.hospital_queue_backend.dto.response.QueueResponse;
import com.hospitalqueue.hospital_queue_backend.entity.DoctorAvailability;
import com.hospitalqueue.hospital_queue_backend.entity.DoctorLeave;
import com.hospitalqueue.hospital_queue_backend.services.DoctorService;
import com.hospitalqueue.hospital_queue_backend.services.QueueService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {

    private final DoctorService doctorService;
    private final QueueService queueService;

    // ── Availability ────────────────────────────────────────────────────────
    @PostMapping("/availability")
    public ResponseEntity<Map<String, String>> setAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AvailabilityRequest request) {
        doctorService.setAvailability(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "Availability saved"));
    }

    @GetMapping("/availability")
    public ResponseEntity<List<DoctorAvailability>> getMyAvailability(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(doctorService.getMyAvailability(userDetails.getUsername()));
    }

    // ── Leave ───────────────────────────────────────────────────────────────
    @PostMapping("/leave")
    public ResponseEntity<Map<String, String>> markLeave(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody LeaveRequest request) {
        doctorService.markLeave(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "Leave marked"));
    }

    @DeleteMapping("/leave/{date}")
    public ResponseEntity<Map<String, String>> cancelLeave(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String date) {
        doctorService.cancelLeave(userDetails.getUsername(), date);
        return ResponseEntity.ok(Map.of("message", "Leave cancelled"));
    }

    @GetMapping("/leave")
    public ResponseEntity<List<DoctorLeave>> getMyLeaves(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(doctorService.getMyLeaves(userDetails.getUsername()));
    }

    // ── Queue ───────────────────────────────────────────────────────────────
    @GetMapping("/queue")
    public ResponseEntity<List<QueueResponse>> getLiveQueue(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(doctorService.getLiveQueue(userDetails.getUsername()));
    }

    @PostMapping("/queue/done/{bookingId}")
    public ResponseEntity<Map<String, String>> markTokenDone(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {

        // Mark as completed in DB
        doctorService.markTokenDone(userDetails.getUsername(), bookingId);

        // Broadcast updated queue to all patients via WebSocket + trigger FCM if 3 away
        com.hospitalqueue.hospital_queue_backend.entity.Doctor doctor = doctorService.getDoctorByEmail(userDetails.getUsername());
        queueService.broadcastQueueUpdate(doctor);

        return ResponseEntity.ok(Map.of("message", "Token marked as done"));
    }

    // ── Dashboard Stats ─────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        Map<String, Object> dashboard = Map.of(
                "patientsSeenToday", doctorService.getPatientsSeenToday(email),
                "earningsToday", doctorService.getEarningsToday(email),
                "liveQueue", doctorService.getLiveQueue(email)
        );

        return ResponseEntity.ok(dashboard);
    }
}
