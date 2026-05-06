package com.hospitalqueue.hospital_queue_backend.repository;

import com.hospitalqueue.hospital_queue_backend.entity.Doctor;
import com.hospitalqueue.hospital_queue_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUser(User user);

    Optional<Doctor> findByUserId(Long userId);

    List<Doctor> findBySpecializationIdAndIsActiveTrue(Long specializationId);

    @Query("SELECT d FROM Doctor d WHERE d.isActive = true")
    List<Doctor> findAllActiveDoctors();
}
