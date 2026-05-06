package com.hospitalqueue.hospital_queue_backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospitalqueue.hospital_queue_backend.dto.response.DoctorResponse;
import com.hospitalqueue.hospital_queue_backend.dto.response.SlotResponse;
import com.hospitalqueue.hospital_queue_backend.entity.Specialization;
import com.hospitalqueue.hospital_queue_backend.services.DoctorService;
import com.hospitalqueue.hospital_queue_backend.services.SlotGenerationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final DoctorService doctorService;
    private final SlotGenerationService slotGenerationService;

    @GetMapping("/specializations")
    public ResponseEntity<List<Specialization>> getSpecializations() {
        return ResponseEntity.ok(doctorService.getAllSpecializations());
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorResponse>> getDoctors(
            @RequestParam(required = false) Long specializationId) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(specializationId));
    }

    @GetMapping("/doctors/{doctorId}/slots")
    public ResponseEntity<List<SlotResponse>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotGenerationService.getAvailableSlots(doctorId, date));
    }
}
