package com.hospitalqueue.hospital_queue_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AvailabilityRequest {

    @NotBlank
    private String dayOfWeek; // MONDAY, TUESDAY, etc.
    @NotBlank
    private String startTime; // "09:00"
    @NotBlank
    private String endTime;   // "13:00"
    @Min(10)
    @Max(60)
    private int slotDurationMinutes;
    @Min(1)
    @Max(200)
    private int maxPatientsPerDay;
}
