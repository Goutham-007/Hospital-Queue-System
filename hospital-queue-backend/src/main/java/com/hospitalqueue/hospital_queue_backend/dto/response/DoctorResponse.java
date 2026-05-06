package com.hospitalqueue.hospital_queue_backend.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorResponse {

    private Long doctorId;
    private String name;
    private String email;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String bio;
}
