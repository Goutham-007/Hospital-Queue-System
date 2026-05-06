package com.hospitalqueue.hospital_queue_backend.controller;

import com.hospitalqueue.hospital_queue_backend.dto.response.AuthResponse;
import com.hospitalqueue.hospital_queue_backend.dto.request.LoginRequest;
import com.hospitalqueue.hospital_queue_backend.dto.request.RegisterRequest;
import com.hospitalqueue.hospital_queue_backend.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Patient app calls this on startup to keep FCM token fresh
    @PostMapping("/fcm-token")
    public ResponseEntity<Map<String, String>> updateFcmToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        authService.updateFcmToken(userDetails.getUsername(), body.get("fcmToken"));
        return ResponseEntity.ok(Map.of("message", "FCM token updated"));
    }
}
