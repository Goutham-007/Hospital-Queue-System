package com.hospitalqueue.hospital_queue_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull
    private Long doctorId;
    @NotBlank
    private String bookingDate; // "2025-08-15"
    @NotBlank
    private String slotTime;    // "09:20"
}
