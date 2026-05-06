# Hospital Queue Management System — Architecture

## System Overview

A full-stack hospital queue management platform eliminating physical waiting.

## Tech Stack

| Layer            | Technology               |
| ---------------- | ------------------------ |
| Backend          | Spring Boot 3.2, Java 21 |
| Frontend         | React 18, Vite           |
| Database         | MySQL 8.0                |
| Auth             | JWT + Spring Security    |
| Real-time        | WebSocket (STOMP/SockJS) |
| Notifications    | Firebase Cloud Messaging |
| Containerization | Docker + Docker Compose  |
| CI/CD            | GitHub Actions           |
| IaC              | Terraform                |

## Architecture Diagram

Patient → React Frontend → Spring Boot REST API → MySQL
↓
WebSocket Server → Doctor Dashboard
↓
FCM → Patient Mobile Notification

## Network Configuration

- Frontend: Port 5173 (Vite dev) / Port 80 (Docker Nginx)
- Backend: Port 8080 (Spring Boot Tomcat)
- Database: Port 3306 (MySQL)
- WebSocket: /ws endpoint with SockJS fallback

## Security

- JWT Bearer token authentication
- Role-based access control (PATIENT, DOCTOR, ADMIN)
- BCrypt password hashing
- CORS configured for allowed origins only
