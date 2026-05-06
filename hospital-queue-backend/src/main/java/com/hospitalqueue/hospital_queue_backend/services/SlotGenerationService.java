package com.hospitalqueue.hospital_queue_backend.services;

import com.hospitalqueue.hospital_queue_backend.dto.response.SlotResponse;
import com.hospitalqueue.hospital_queue_backend.entity.DoctorAvailability;
import com.hospitalqueue.hospital_queue_backend.repository.BookingRepository;
import com.hospitalqueue.hospital_queue_backend.repository.DoctorAvailabilityRepository;
import com.hospitalqueue.hospital_queue_backend.repository.DoctorLeaveRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotGenerationService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorLeaveRepository leaveRepository;
    private final BookingRepository bookingRepository;

    /**
     * Returns all slots for a doctor on a given date with availability status.
     * - Returns empty list if doctor is on leave that day - Returns empty list
     * if doctor has no availability set for that day - Marks slots as
     * unavailable if already booked - Respects maxPatientsPerDay limit
     */
    public List<SlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {
        List<SlotResponse> result = new ArrayList<>();

        // Check if doctor is on leave
        boolean onLeave = leaveRepository.existsByDoctorIdAndLeaveDate(doctorId, date);
        if (onLeave) {
            return result; // empty = not available
        }

        // Get availability for this day of week
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailability> availabilities
                = availabilityRepository.findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, dayOfWeek);

        if (availabilities.isEmpty()) {
            return result; // doctor doesn't work this day
        }

        // Get already booked slot times for this date
        List<LocalTime> bookedSlots = bookingRepository.findBookedSlotTimes(doctorId, date);

        // Count confirmed bookings today vs max patients
        int confirmedCount = bookingRepository.countConfirmedBookings(doctorId, date);

        // Generate slots for each shift (doctor may have morning + evening)
        for (DoctorAvailability availability : availabilities) {
            int maxPatients = availability.getMaxPatientsPerDay();
            List<LocalTime> shiftSlots = generateSlots(
                    availability.getStartTime(),
                    availability.getEndTime(),
                    availability.getSlotDurationMinutes()
            );

            for (LocalTime slot : shiftSlots) {
                boolean isBooked = bookedSlots.contains(slot);
                boolean maxReached = confirmedCount >= maxPatients;

                result.add(SlotResponse.builder()
                        .time(slot.format(DateTimeFormatter.ofPattern("HH:mm")))
                        .available(!isBooked && !maxReached)
                        .build());
            }
        }

        return result;
    }

    /**
     * Core slot generation algorithm: startTime=09:00, endTime=13:00,
     * duration=20 mins → generates: 09:00, 09:20, 09:40, 10:00, 10:20 ... 12:40
     * (stops when next slot would exceed or equal endTime)
     */
    public List<LocalTime> generateSlots(LocalTime startTime, LocalTime endTime, int durationMinutes) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = startTime;

        while (current.isBefore(endTime)) {
            slots.add(current);
            current = current.plusMinutes(durationMinutes);
        }

        return slots;
    }

    /**
     * Check if a specific slot is valid and available for booking
     */
    public boolean isSlotAvailable(Long doctorId, LocalDate date, LocalTime slotTime) {
        List<SlotResponse> slots = getAvailableSlots(doctorId, date);
        String timeStr = slotTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        return slots.stream()
                .anyMatch(s -> s.getTime().equals(timeStr) && s.isAvailable());
    }
}
