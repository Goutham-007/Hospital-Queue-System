package com.hospitalqueue.hospital_queue_backend.services;

import com.hospitalqueue.hospital_queue_backend.dto.request.AvailabilityRequest;
import com.hospitalqueue.hospital_queue_backend.dto.request.LeaveRequest;
import com.hospitalqueue.hospital_queue_backend.dto.response.DoctorResponse;
import com.hospitalqueue.hospital_queue_backend.dto.response.QueueResponse;
import com.hospitalqueue.hospital_queue_backend.entity.*;
import com.hospitalqueue.hospital_queue_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorLeaveRepository leaveRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SpecializationRepository specializationRepository;

    // ── Availability Management ─────────────────────────────────────────────
    @Transactional
    public void setAvailability(String email, AvailabilityRequest request) {
        Doctor doctor = getDoctorByEmail(email);
        DayOfWeek day = DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());

        // Delete existing availability for this day+shift if exists
        availabilityRepository.deleteByDoctorIdAndDayOfWeek(doctor.getId(), day);

        DoctorAvailability availability = DoctorAvailability.builder()
                .doctor(doctor)
                .dayOfWeek(day)
                .startTime(LocalTime.parse(request.getStartTime()))
                .endTime(LocalTime.parse(request.getEndTime()))
                .slotDurationMinutes(request.getSlotDurationMinutes())
                .maxPatientsPerDay(request.getMaxPatientsPerDay())
                .isActive(true)
                .build();

        availabilityRepository.save(availability);
    }

    public List<DoctorAvailability> getMyAvailability(String email) {
        Doctor doctor = getDoctorByEmail(email);
        return availabilityRepository.findByDoctorIdAndIsActiveTrue(doctor.getId());
    }

    // ── Leave Management ────────────────────────────────────────────────────
    @Transactional
    public void markLeave(String email, LeaveRequest request) {
        Doctor doctor = getDoctorByEmail(email);
        LocalDate leaveDate = LocalDate.parse(request.getLeaveDate());

        if (leaveRepository.existsByDoctorIdAndLeaveDate(doctor.getId(), leaveDate)) {
            throw new RuntimeException("Leave already marked for this date");
        }

        DoctorLeave leave = DoctorLeave.builder()
                .doctor(doctor)
                .leaveDate(leaveDate)
                .reason(request.getReason())
                .build();

        leaveRepository.save(leave);
    }

    @Transactional
    public void cancelLeave(String email, String dateStr) {
        Doctor doctor = getDoctorByEmail(email);
        LocalDate leaveDate = LocalDate.parse(dateStr);
        leaveRepository.findByDoctorIdAndLeaveDate(doctor.getId(), leaveDate)
                .ifPresent(leaveRepository::delete);
    }

    public List<DoctorLeave> getMyLeaves(String email) {
        Doctor doctor = getDoctorByEmail(email);
        return leaveRepository.findByDoctorId(doctor.getId());
    }

    // ── Live Queue ──────────────────────────────────────────────────────────
    public List<QueueResponse> getLiveQueue(String email) {
        Doctor doctor = getDoctorByEmail(email);
        LocalDate today = LocalDate.now();

        return bookingRepository.findLiveQueue(doctor.getId(), today)
                .stream()
                .map(b -> QueueResponse.builder()
                .bookingId(b.getId())
                .tokenNumber(b.getTokenNumber())
                .patientName(b.getPatient().getUser().getName())
                .slotTime(b.getSlotTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .status(b.getStatus().name())
                .build())
                .collect(Collectors.toList());
    }

    // ── Mark Token Done ─────────────────────────────────────────────────────
    @Transactional
    public void markTokenDone(String email, Long bookingId) {
        Doctor doctor = getDoctorByEmail(email);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("Not authorized");
        }

        booking.setStatus(com.hospitalqueue.hospital_queue_backend.enums.BookingStatus.COMPLETED);
        bookingRepository.save(booking);
    }

    // ── Dashboard Stats ─────────────────────────────────────────────────────
    public int getPatientsSeenToday(String email) {
        Doctor doctor = getDoctorByEmail(email);
        return bookingRepository.countCompletedToday(doctor.getId(), LocalDate.now());
    }

    public BigDecimal getEarningsToday(String email) {
        Doctor doctor = getDoctorByEmail(email);
        return paymentRepository.getTotalEarningsToday(doctor.getId(), LocalDate.now());
    }

    // ── Doctor Listing ──────────────────────────────────────────────────────
    public List<DoctorResponse> getDoctorsBySpecialization(Long specializationId) {
        List<Doctor> doctors = specializationId != null
                ? doctorRepository.findBySpecializationIdAndIsActiveTrue(specializationId)
                : doctorRepository.findAllActiveDoctors();

        return doctors.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<Specialization> getAllSpecializations() {
        return specializationRepository.findAll();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────
    public Doctor getDoctorByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return doctorRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
    }

    private DoctorResponse toResponse(Doctor d) {
        return DoctorResponse.builder()
                .doctorId(d.getId())
                .name(d.getUser().getName())
                .email(d.getUser().getEmail())
                .specialization(d.getSpecialization().getName())
                .qualification(d.getQualification())
                .experienceYears(d.getExperienceYears())
                .consultationFee(d.getConsultationFee())
                .bio(d.getBio())
                .build();
    }
}
