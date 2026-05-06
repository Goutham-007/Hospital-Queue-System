package com.hospitalqueue.hospital_queue_backend.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlotResponse {

    private String time;       // "09:20"
    private boolean available; // false if already booked
}
