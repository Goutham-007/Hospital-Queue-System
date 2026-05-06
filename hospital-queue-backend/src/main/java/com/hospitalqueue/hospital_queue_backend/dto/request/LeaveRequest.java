package com.hospitalqueue.hospital_queue_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LeaveRequest {

    @NotBlank
    private String leaveDate; // "2025-08-20"
    private String reason;
}
