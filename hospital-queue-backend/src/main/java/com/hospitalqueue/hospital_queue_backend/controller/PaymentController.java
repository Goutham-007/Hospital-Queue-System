package com.hospitalqueue.hospital_queue_backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hospitalqueue.hospital_queue_backend.entity.Payment;
import com.hospitalqueue.hospital_queue_backend.entity.Booking;
import com.hospitalqueue.hospital_queue_backend.enums.PaymentStatus;
import com.hospitalqueue.hospital_queue_backend.repository.PaymentRepository;
import com.hospitalqueue.hospital_queue_backend.services.BookingService;
import com.hospitalqueue.hospital_queue_backend.services.NotificationService;
import com.hospitalqueue.hospital_queue_backend.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final NotificationService notificationService;
    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        log.info("Razorpay webhook received");
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok(Map.of("status", "processed"));
    }

    @PostMapping("/mock-confirm")
    public ResponseEntity<Map<String, String>> mockConfirm(@RequestBody Map<String, Object> body) {
        String razorpayOrderId = (String) body.get("razorpayOrderId");

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId("mock_pay_" + System.currentTimeMillis());
        payment.setPaidAt(java.time.LocalDateTime.now());
        paymentRepository.save(payment);

        Booking confirmed = bookingService.confirmBooking(payment.getBooking().getId());

        String fcmToken = confirmed.getPatient().getUser().getFcmToken();
        notificationService.sendBookingConfirmation(
                fcmToken,
                confirmed.getTokenNumber(),
                confirmed.getDoctor().getUser().getName(),
                confirmed.getSlotTime().toString()
        );

        return ResponseEntity.ok(Map.of("message", "confirmed", "token", String.valueOf(confirmed.getTokenNumber())));
    }
}
