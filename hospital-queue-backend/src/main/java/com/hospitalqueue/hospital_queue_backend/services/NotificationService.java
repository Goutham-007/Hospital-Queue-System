package com.hospitalqueue.hospital_queue_backend.services;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    /**
     * Sends FCM push notification to a patient device. Called when patient is 3
     * tokens away from being called.
     */
    public void sendQueueAlert(String fcmToken, int tokenNumber, String doctorName) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("FCM token is null/empty. Skipping notification.");
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase not initialized. Skipping notification.");
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle("Your turn is approaching!")
                            .setBody("Token #" + tokenNumber + " — Only 3 patients before you with Dr. " + doctorName + ". Please head to the clinic.")
                            .build())
                    .putData("tokenNumber", String.valueOf(tokenNumber))
                    .putData("doctorName", doctorName)
                    .putData("type", "QUEUE_ALERT")
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM notification sent successfully. Response: {}", response);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM notification: {}", e.getMessage());
        }
    }

    /**
     * Sends confirmation notification after booking is confirmed
     */
    public void sendBookingConfirmation(String fcmToken, int tokenNumber, String doctorName, String slotTime) {
        if (fcmToken == null || fcmToken.isBlank() || FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle("Booking Confirmed!")
                            .setBody("Your token #" + tokenNumber + " with Dr. " + doctorName + " at " + slotTime + " is confirmed.")
                            .build())
                    .putData("type", "BOOKING_CONFIRMED")
                    .putData("tokenNumber", String.valueOf(tokenNumber))
                    .build();

            FirebaseMessaging.getInstance().send(message);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send booking confirmation notification: {}", e.getMessage());
        }
    }
}
