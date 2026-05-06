# 🏥 Hospital Queue Management System

A full-stack real-time hospital queue management system that eliminates physical waiting. Patients book appointments online, pay consultation fees, and receive notifications when their turn approaches.

## 🚀 Live Demo
- Frontend: http://localhost:8081
- Backend API: http://localhost:9090

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2, Java 21 |
| Frontend | React 18, Vite |
| Database | MySQL 8.0 |
| Authentication | JWT + Spring Security |
| Real-time | WebSocket (STOMP/SockJS) |
| Notifications | Firebase Cloud Messaging |
| Payment | Razorpay (Mock Payment) |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| IaC | Terraform |

## ✨ Features

### Patient
- Register and search doctors by specialization
- View only available time slots
- Book appointment and pay consultation fee
- Get token number after payment
- Track live queue position in real time
- Receive push notification when 3 patients away

### Doctor
- Set working hours, slot duration, max patients
- Mark leave dates and holidays
- View live patient queue
- Mark tokens as done
- View patients seen today and earnings

### Admin
- Create doctor accounts
- Manage specializations
- View system analytics

## 🔐 Security
- JWT Bearer token authentication
- Role-based access (PATIENT, DOCTOR, ADMIN)
- BCrypt password hashing
- CORS configured
- SELECT FOR UPDATE for concurrency control

## 🐳 Run with Docker

```bash
# Build backend jar first
cd hospital-queue-backend
mvn clean package -DskipTests

# Start all services
docker-compose up --build