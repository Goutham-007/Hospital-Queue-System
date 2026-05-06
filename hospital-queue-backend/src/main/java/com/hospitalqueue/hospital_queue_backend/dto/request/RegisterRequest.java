package com.hospitalqueue.hospital_queue_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String name;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    @Size(min = 6)
    private String password;
    @NotBlank
    private String phone;
    @NotBlank
    private String role; // PATIENT, DOCTOR, ADMIN

    // Patient-only fields (optional)
    private Integer age;
    private String gender;

    // Doctor-only fields (optional)
    private Long specializationId;
    private String qualification;
    private Integer experienceYears;
    private java.math.BigDecimal consultationFee;
    private String bio;
}
