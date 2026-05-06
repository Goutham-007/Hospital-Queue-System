package com.hospitalqueue.hospital_queue_backend.services;

import com.hospitalqueue.hospital_queue_backend.entity.Doctor;
import com.hospitalqueue.hospital_queue_backend.entity.Specialization;
import com.hospitalqueue.hospital_queue_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hospitalqueue.hospital_queue_backend.dto.request.RegisterRequest;
import com.hospitalqueue.hospital_queue_backend.dto.response.AuthResponse;
import com.hospitalqueue.hospital_queue_backend.entity.User;
import com.hospitalqueue.hospital_queue_backend.enums.Role;
import com.hospitalqueue.hospital_queue_backend.repository.DoctorRepository;
import com.hospitalqueue.hospital_queue_backend.repository.SpecializationRepository;
import com.hospitalqueue.hospital_queue_backend.repository.UserRepository;
import com.hospitalqueue.hospital_queue_backend.security.JwtUtil;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final BookingRepository bookingRepository;
    private final SpecializationRepository specializationRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public AuthResponse createDoctorAccount(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        Specialization specialization = specializationRepository
                .findById(request.getSpecializationId())
                .orElseThrow(() -> new RuntimeException("Specialization not found"));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.DOCTOR)
                .build();
        userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .specialization(specialization)
                .qualification(request.getQualification())
                .experienceYears(request.getExperienceYears())
                .consultationFee(request.getConsultationFee())
                .bio(request.getBio())
                .build();
        doctorRepository.save(doctor);

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, Role.DOCTOR.name());

        return AuthResponse.builder()
                .token(token).role("DOCTOR")
                .name(user.getName()).email(user.getEmail())
                .userId(user.getId()).build();
    }

    public Map<String, Object> getAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalDoctors", doctorRepository.count());
        analytics.put("totalPatients", patientRepository.count());
        analytics.put("totalBookings", bookingRepository.count());
        analytics.put("totalSpecializations", specializationRepository.count());
        return analytics;
    }

    @Transactional
    public Specialization addSpecialization(String name) {
        Specialization spec = Specialization.builder().name(name).build();
        return specializationRepository.save(spec);
    }

    @Transactional
    public void toggleDoctorStatus(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setIsActive(!doctor.getIsActive());
        doctorRepository.save(doctor);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public List<Specialization> getAllSpecializations() {
        return specializationRepository.findAll();
    }
}
