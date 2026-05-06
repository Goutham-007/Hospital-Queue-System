package com.hospitalqueue.hospital_queue_backend.repository;

import com.hospitalqueue.hospital_queue_backend.entity.Patient;
import com.hospitalqueue.hospital_queue_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUser(User user);

    Optional<Patient> findByUserId(Long userId);
}
