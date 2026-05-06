package com.hospitalqueue.hospital_queue_backend.services;

import com.hospitalqueue.hospital_queue_backend.entity.*;
import com.hospitalqueue.hospital_queue_backend.repository.*;
import com.hospitalqueue.hospital_queue_backend.dto.request.BookingRequest;
import com.hospitalqueue.hospital_queue_backend.dto.response.BookingResponse;
import com.hospitalqueue.hospital_queue_backend.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final SlotGenerationService slotGenerationService;

    /**
     * Creates a PENDING booking with SELECT FOR UPDATE to prevent race
     * conditions. Even if 100 patients hit this simultaneously, only one will
     * succeed per slot. Token is NOT assigned here — only after Razorpay
     * webhook confirms payment.
     */
    @Transactional
    public Booking createPendingBooking(String patientEmail, BookingRequest request) {
        User user = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        LocalDate bookingDate = LocalDate.parse(request.getBookingDate());
        LocalTime slotTime = LocalTime.parse(request.getSlotTime());

        // CONCURRENCY CHECK — SELECT FOR UPDATE locks this row until transaction ends
        // If two patients try to book the same slot simultaneously, the second one waits
        // and then finds the slot already taken
        boolean slotTaken = bookingRepository
                .findSlotWithLock(doctor.getId(), bookingDate, slotTime)
                .isPresent();

        if (slotTaken) {
            throw new RuntimeException("This slot is no longer available. Please choose another slot.");
        }

        // Also validate slot is genuinely in doctor's schedule
        if (!slotGenerationService.isSlotAvailable(doctor.getId(), bookingDate, slotTime)) {
            throw new RuntimeException("Invalid slot for this doctor on this date.");
        }

        Booking booking = Booking.builder()
                .patient(patient)
                .doctor(doctor)
                .bookingDate(bookingDate)
                .slotTime(slotTime)
                .status(BookingStatus.PENDING)
                .tokenNumber(null) // assigned after payment
                .build();

        return bookingRepository.save(booking);
    }

    /**
     * Called by PaymentService after Razorpay webhook confirms SUCCESS. Assigns
     * token number and marks booking CONFIRMED.
     */
    @Transactional
    public Booking confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        int nextToken = bookingRepository.getNextTokenNumber(
                booking.getDoctor().getId(),
                booking.getBookingDate()
        );

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTokenNumber(nextToken);

        return bookingRepository.save(booking);
    }

    /**
     * Cancel a booking (patient-initiated)
     */
    @Transactional
    public void cancelBooking(String patientEmail, Long bookingId) {
        User user = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Booking booking = bookingRepository.findByIdAndPatientId(bookingId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Booking not found or not yours"));

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    /**
     * Patient dashboard — all their bookings with queue position and wait time
     */
    public List<BookingResponse> getMyBookings(String patientEmail) {
        User user = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        return bookingRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(String patientEmail, Long bookingId) {
        User user = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Booking booking = bookingRepository.findByIdAndPatientId(bookingId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        return toResponse(booking);
    }

    private BookingResponse toResponse(Booking b) {
        int queuePosition = 0;
        int waitMinutes = 0;

        if (b.getStatus() == BookingStatus.CONFIRMED && b.getTokenNumber() != null) {
            queuePosition = bookingRepository.countTokensAhead(
                    b.getDoctor().getId(), b.getBookingDate(), b.getTokenNumber()
            );

            // Estimate wait time based on slot duration (default 15 min if not found)
            var availabilities = slotGenerationService.generateSlots(
                    b.getSlotTime(), b.getSlotTime().plusHours(1), 15
            );
            waitMinutes = queuePosition * 15; // simplified estimate
        }

        return BookingResponse.builder()
                .bookingId(b.getId())
                .doctorName(b.getDoctor().getUser().getName())
                .specialization(b.getDoctor().getSpecialization().getName())
                .bookingDate(b.getBookingDate().toString())
                .slotTime(b.getSlotTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .status(b.getStatus().name())
                .tokenNumber(b.getTokenNumber())
                .queuePosition(queuePosition)
                .estimatedWaitMinutes(waitMinutes)
                .build();
    }
}
