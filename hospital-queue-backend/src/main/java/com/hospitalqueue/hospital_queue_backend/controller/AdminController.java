package com.hospitalqueue.hospital_queue_backend.controller;

import com.hospitalqueue.hospital_queue_backend.entity.Doctor;
import com.hospitalqueue.hospital_queue_backend.entity.Specialization;
import com.hospitalqueue.hospital_queue_backend.services.AdminService;
import com.hospitalqueue.hospital_queue_backend.services.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.hospitalqueue.hospital_queue_backend.dto.request.RegisterRequest;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    @PostMapping("/doctors/create")
    public ResponseEntity<?> createDoctor(@RequestBody RegisterRequest request) {
        // Admin bypasses the self-registration restriction by directly calling AuthService
        // Force role to DOCTOR regardless of what was sent
        request.setRole("DOCTOR");
        return ResponseEntity.ok(adminService.createDoctorAccount(request));
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        return ResponseEntity.ok(adminService.getAnalytics());
    }

    @PostMapping("/specializations")
    public ResponseEntity<Specialization> addSpecialization(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.addSpecialization(body.get("name")));
    }

    @GetMapping("/specializations")
    public ResponseEntity<List<Specialization>> getAllSpecializations() {
        return ResponseEntity.ok(adminService.getAllSpecializations());
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(adminService.getAllDoctors());
    }

    @PutMapping("/doctors/{doctorId}/toggle")
    public ResponseEntity<Map<String, String>> toggleDoctorStatus(@PathVariable Long doctorId) {
        adminService.toggleDoctorStatus(doctorId);
        return ResponseEntity.ok(Map.of("message", "Doctor status updated"));
    }
}
