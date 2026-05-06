package com.hospitalqueue.hospital_queue_backend.services;

import com.hospitalqueue.hospital_queue_backend.dto.response.QueueResponse;
import com.hospitalqueue.hospital_queue_backend.entity.Booking;
import com.hospitalqueue.hospital_queue_backend.entity.Doctor;
import com.hospitalqueue.hospital_queue_backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService {

    private final BookingRepository bookingRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    /**
     * Called after doctor marks a token as COMPLETED. 1. Broadcasts updated
     * queue to all WebSocket subscribers 2. Checks if any patient is now 3
     * tokens away → sends FCM notification
     */
    @Transactional
    public void broadcastQueueUpdate(Doctor doctor) {
        Long doctorId = doctor.getId();
        LocalDate today = LocalDate.now();

        List<Booking> liveQueue = bookingRepository.findLiveQueue(doctorId, today);

        // Build response list
        List<QueueResponse> queueResponses = liveQueue.stream()
                .map(b -> QueueResponse.builder()
                .bookingId(b.getId())
                .tokenNumber(b.getTokenNumber())
                .patientName(b.getPatient().getUser().getName())
                .slotTime(b.getSlotTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .status(b.getStatus().name())
                .build())
                .collect(Collectors.toList());

        // Broadcast to WebSocket topic - all subscribed clients receive this instantly
        messagingTemplate.convertAndSend("/topic/queue/" + doctorId, queueResponses);
        log.info("Queue update broadcast sent to /topic/queue/{}", doctorId);

        // Check: who is now exactly 3 positions away from being called?
        // Position 0 = currently being seen, so "3 away" = position index 3 in the queue
        if (liveQueue.size() >= 3) {
            Booking threeAway = liveQueue.get(2); // index 2 = 3rd in line (0-based)
            String fcmToken = threeAway.getPatient().getUser().getFcmToken();
            String doctorName = doctor.getUser().getName();
            int tokenNumber = threeAway.getTokenNumber();

            notificationService.sendQueueAlert(fcmToken, tokenNumber, doctorName);
            log.info("FCM alert sent to patient with token #{}", tokenNumber);
        }
    }

    /**
     * Get queue for a specific doctor today - used by patients to track
     * position
     */
    public List<QueueResponse> getQueueForDoctor(Long doctorId) {
        return bookingRepository.findLiveQueue(doctorId, LocalDate.now())
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
}
