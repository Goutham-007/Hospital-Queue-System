package com.hospitalqueue.hospital_queue_backend.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {

    private Long bookingId;
    private String doctorName;
    private String specialization;
    private String bookingDate;
    private String slotTime;
    private String status;
    private Integer tokenNumber;
    private String razorpayOrderId;
    private Double amount;
    private int queuePosition;
    private int estimatedWaitMinutes;
}
