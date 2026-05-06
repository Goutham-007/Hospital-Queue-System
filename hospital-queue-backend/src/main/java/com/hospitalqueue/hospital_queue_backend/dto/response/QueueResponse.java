package com.hospitalqueue.hospital_queue_backend.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueueResponse {

    private Long bookingId;
    private int tokenNumber;
    private String patientName;
    private String slotTime;
    private String status;
}
