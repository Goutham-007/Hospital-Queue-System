package com.hospitalqueue.hospital_queue_backend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hospitalqueue.hospital_queue_backend.repository.BookingRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingCleanupScheduler {

    private final BookingRepository bookingRepository;

    /**
     * Runs every 5 minutes. Deletes PENDING bookings older than 15 minutes.
     * These are bookings where patient initiated but never completed payment.
     */
    @Scheduled(fixedRate = 300000) // every 5 minutes
    @Transactional
    public void cleanupStalePendingBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        bookingRepository.deleteStalePendingBookings(cutoff);
        log.info("Cleanup job ran: deleted PENDING bookings older than 15 minutes");
    }
}
