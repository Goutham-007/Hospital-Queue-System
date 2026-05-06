package com.hospitalqueue.hospital_queue_backend.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String role;
    private String name;
    private String email;
    private Long userId;
}
