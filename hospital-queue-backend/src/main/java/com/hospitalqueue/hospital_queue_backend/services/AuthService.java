package com.hospitalqueue.hospital_queue_backend.services;

import com.hospitalqueue.hospital_queue_backend.dto.request.LoginRequest;
import com.hospitalqueue.hospital_queue_backend.dto.request.RegisterRequest;
import com.hospitalqueue.hospital_queue_backend.dto.response.AuthResponse;
import com.hospitalqueue.hospital_queue_backend.entity.Doctor;
import com.hospitalqueue.hospital_queue_backend.entity.Patient;
import com.hospitalqueue.hospital_queue_backend.entity.Specialization;
import com.hospitalqueue.hospital_queue_backend.entity.User;
import com.hospitalqueue.hospital_queue_backend.enums.Role;
import com.hospitalqueue.hospital_queue_backend.repository.DoctorRepository;
import com.hospitalqueue.hospital_queue_backend.repository.PatientRepository;
import com.hospitalqueue.hospital_queue_backend.repository.SpecializationRepository;
import com.hospitalqueue.hospital_queue_backend.repository.UserRepository;
import com.hospitalqueue.hospital_queue_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SpecializationRepository specializationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // NOTE: AuthenticationManager removed — we do manual bcrypt check instead
    // This avoids Spring Security ProviderManager conflicts completely
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Role role = Role.valueOf(request.getRole().toUpperCase());

        if (role == Role.DOCTOR || role == Role.ADMIN) {
            throw new RuntimeException("Doctor and Admin accounts can only be created by the hospital admin.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .build();

        userRepository.save(user);

        if (role == Role.PATIENT) {
            Patient patient = Patient.builder()
                    .user(user)
                    .age(request.getAge())
                    .gender(request.getGender() != null
                            ? Patient.Gender.valueOf(request.getGender().toUpperCase()) : null)
                    .build();
            patientRepository.save(patient);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, role.name());

        return AuthResponse.builder()
                .token(token)
                .role(role.name())
                .name(user.getName())
                .email(user.getEmail())
                .userId(user.getId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Step 1: Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Step 2: Manually verify password with BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Step 3: Generate JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .name(user.getName())
                .email(user.getEmail())
                .userId(user.getId())
                .build();
    }

    @Transactional
    public void updateFcmToken(String email, String fcmToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }
}
