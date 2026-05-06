package com.hospitalqueue.hospital_queue_backend.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospitalqueue.hospital_queue_backend.entity.Booking;
import com.hospitalqueue.hospital_queue_backend.entity.Patient;
import com.hospitalqueue.hospital_queue_backend.entity.Payment;
import com.hospitalqueue.hospital_queue_backend.enums.PaymentStatus;
import com.hospitalqueue.hospital_queue_backend.repository.PatientRepository;
import com.hospitalqueue.hospital_queue_backend.repository.PaymentRepository;
import com.hospitalqueue.hospital_queue_backend.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final NotificationService notificationService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    /**
     * Step 1: Create Razorpay order when patient initiates booking. Booking is
     * already PENDING at this point.
     */
    @Transactional
    public Map<String, Object> createOrder(String patientEmail, Booking booking) {
        Patient patient = patientRepository.findByUser(
                userRepository.findByEmail(patientEmail)
                        .orElseThrow(() -> new RuntimeException("User not found")))
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        BigDecimal amount = booking.getDoctor().getConsultationFee();

        try {
            // Amount must be in paise (1 INR = 100 paise)
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + booking.getId());

            Order order = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = order.get("id");

            // Save payment record with CREATED status
            Payment payment = Payment.builder()
                    .booking(booking)
                    .patient(patient)
                    .razorpayOrderId(razorpayOrderId)
                    .amount(amount)
                    .status(PaymentStatus.CREATED)
                    .build();
            paymentRepository.save(payment);

            // Return all info frontend needs to open Razorpay checkout
            Map<String, Object> response = new HashMap<>();
            response.put("razorpayOrderId", razorpayOrderId);
            response.put("amount", amount);
            response.put("currency", "INR");
            response.put("keyId", razorpayKeyId);
            response.put("bookingId", booking.getId());
            response.put("customerName", booking.getPatient().getUser().getName());
            response.put("customerEmail", patientEmail);
            response.put("customerPhone", booking.getPatient().getUser().getPhone());

            return response;

        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    /**
     * Step 2: Razorpay calls this webhook after payment is done (success or
     * failure). THIS is where token is generated — only on confirmed payment.
     */
    @Transactional
    public void handleWebhook(String payload, String signature) {
        // Verify webhook signature for security
        if (!verifyWebhookSignature(payload, signature)) {
            throw new RuntimeException("Invalid webhook signature");
        }

        JSONObject event = new JSONObject(payload);
        String eventType = event.getString("event");

        log.info("Razorpay webhook received: {}", eventType);

        if ("payment.captured".equals(eventType)) {
            JSONObject paymentData = event
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String razorpayOrderId = paymentData.getString("order_id");
            String razorpayPaymentId = paymentData.getString("id");

            Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for order: " + razorpayOrderId));

            // Mark payment SUCCESS
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setRazorpayPaymentId(razorpayPaymentId);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // NOW generate token and confirm booking
            Booking confirmedBooking = bookingService.confirmBooking(payment.getBooking().getId());

            // Send confirmation notification to patient
            String fcmToken = confirmedBooking.getPatient().getUser().getFcmToken();
            notificationService.sendBookingConfirmation(
                    fcmToken,
                    confirmedBooking.getTokenNumber(),
                    confirmedBooking.getDoctor().getUser().getName(),
                    confirmedBooking.getSlotTime().toString()
            );

            log.info("Booking confirmed. Token #{} issued for booking #{}",
                    confirmedBooking.getTokenNumber(), confirmedBooking.getId());

        } else if ("payment.failed".equals(eventType)) {
            JSONObject paymentData = event
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String razorpayOrderId = paymentData.getString("order_id");

            paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                log.info("Payment failed for order: {}", razorpayOrderId);
                // Booking stays PENDING — scheduler will clean it up after 15 mins
            });
        }
    }

    /**
     * Verify Razorpay webhook signature using HMAC-SHA256
     */
    private boolean verifyWebhookSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes());
            String computedSignature = HexFormat.of().formatHex(hash);
            return computedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

}
