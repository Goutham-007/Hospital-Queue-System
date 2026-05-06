package com.hospitalqueue.hospital_queue_backend.repository;

import com.hospitalqueue.hospital_queue_backend.entity.DoctorLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorLeaveRepository extends JpaRepository<DoctorLeave, Long> {

    boolean existsByDoctorIdAndLeaveDate(Long doctorId, LocalDate leaveDate);

    List<DoctorLeave> findByDoctorId(Long doctorId);

    Optional<DoctorLeave> findByDoctorIdAndLeaveDate(Long doctorId, LocalDate leaveDate);
}
