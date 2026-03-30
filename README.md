# OmniCharge – Microservices-Based Recharge Platform

OmniCharge is a **production-grade telecom recharge backend system** built using **Spring Boot Microservices Architecture**.

It demonstrates real-world backend engineering concepts like:

* Microservices communication
* API Gateway + JWT Security
* Event-driven architecture (RabbitMQ)
* Payment Gateway Integration (Stripe)
* Observability (Zipkin, Prometheus, Grafana)
* Dockerized deployment

---

# Architecture Overview

```
User → API Gateway → Recharge Service
                        ↓
                  Operator Service
                        ↓
                  Payment Service (Stripe)
                        ↓
                     RabbitMQ
                    ↙        ↘
        Recharge Service     Notification Service (Email)
```

---

# Microservices

| Service              | Description                           |
| -------------------- | ------------------------------------- |
| user-service         | Authentication (JWT), user management |
| operator-service     | Manage telecom operators and plans    |
| recharge-service     | Core business logic (recharge flow)   |
| payment-service      | Stripe payment integration            |
| notification-service | Email notifications via RabbitMQ      |
| api-gateway          | Central routing + JWT validation      |
| discovery-server     | Service registry (Eureka)             |
| config-server        | Centralized configuration             |

---

# Security

* JWT-based authentication
* API Gateway validation
* Role-based access:

    * **USER** → recharge
    * **ADMIN** → operator & plan management

---

# Payment Integration

* Integrated with **Stripe (Test Mode)**
* Payment flow:

    1. Create PaymentIntent
    2. Store transaction
    3. Verify payment (simulated)
    4. Update recharge status

---

# Event-Driven Architecture

* RabbitMQ used for async communication
* Payment success event triggers:

    * Recharge status update
    * Email notification

---

# Notification System

* Sends real email using SMTP (Gmail)
* Triggered via RabbitMQ events

---

# Observability

* **Spring Boot Actuator**
* **Prometheus** → metrics
* **Grafana** → dashboards
* **Zipkin** → distributed tracing

---

# Dockerized Setup

Run the entire system using:

```bash
docker-compose up --build
```

---

#  Testing

* JUnit 5
* Mockito
* Service layer unit tests
* Exception testing

---

#  API Documentation

Swagger UI available at:

```
http://localhost:<port>/swagger-ui/index.html
```

---

#  Tech Stack

* Java 17
* Spring Boot 3
* Spring Cloud
* MySQL
* RabbitMQ
* Stripe API
* Docker
* Prometheus + Grafana + Zipkin

---

#  Recharge Flow

1. User logs in → receives JWT
2. Calls recharge API
3. Recharge Service:

    * Validates plan (Operator Service)
    * Calls Payment Service
4. Payment Service:

    * Creates Stripe PaymentIntent
    * Publishes success event
5. RabbitMQ:

    * Recharge updated
    * Email sent

---

#  Key Features

* Microservices architecture
* Distributed system design
* Event-driven communication
* Secure API Gateway
* External service integration
* Production-ready logging & monitoring

---

#  How to Run Locally

### 1. Start Infrastructure

* MySQL
* RabbitMQ
* Zipkin
* Prometheus
* Grafana

### 2. Start Services (in order)

1. discovery-server
2. config-server
3. api-gateway
4. other services

---

#  Future Enhancements

* Frontend (React/Angular)
* Payment webhook (real Stripe flow)
* Kubernetes deployment
* Centralized logging (ELK stack)

---

# 👨 Author

Priyanshu Jha

---

#  If you like this project

Give it a ⭐ on GitHub!
