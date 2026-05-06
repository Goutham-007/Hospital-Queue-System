package com.hospitalqueue.hospital_queue_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
