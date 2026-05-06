package com.hospitalqueue.hospital_queue_backend.repository;

import com.hospitalqueue.hospital_queue_backend.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctorIdAndDayOfWeekAndIsActiveTrue(Long doctorId, DayOfWeek dayOfWeek);

    List<DoctorAvailability> findByDoctorIdAndIsActiveTrue(Long doctorId);

    void deleteByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);
}
