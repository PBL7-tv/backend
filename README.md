# 🛍️ VGearVN - E-Commerce Backend System

A microservices-based e-commerce backend with product management, order processing, payment integration, and an intelligent AI chatbot.

## 🎯 Overview

VGearVN provides a complete e-commerce backend solution with:

-  JWT-based authentication with email verification
-  Product & category management (CRUD operations)
-  Shopping cart & order processing
-  VNPay payment gateway integration
-  Real-time inventory tracking
-  Email notifications via Brevo
-  AI chatbot with RAG and semantic routing
-  Personalized product recommendations

## 🏗️ Architecture

```
<img width="122" height="61" alt="pbl7" src="https://github.com/user-attachments/assets/6f4b9948-fb7f-45a3-a5f5-5fa7b4fec2e4" />

```

**Authentication**: API Gateway validates JWT tokens via Identity Service `/auth/introspect` endpoint.

## 🛠️ Technology Stack

**Backend (Java)**: Spring Boot 3.4.2, Spring Cloud Gateway, Spring Data JPA, Spring Kafka, JWT, Java 21  
**AI (Python)**: Flask 3.0.3, Google Gemini API, ChromaDB, Sentence Transformers  
**Databases**: MySQL, MongoDB, Apache Kafka
**External**: Brevo (email), VNPay (payment), Google Gemini (AI)

## 📦 Prerequisites

- **Java**: JDK 21+
- **Maven**: 3.8+
- **Python**: 3.9+
- **MySQL**: 8.0+
- **MongoDB**: 5.0+
- **Kafka**: 3.0+
- **Docker** (optional)

## 📚 API Documentation

**Base URL**: `http://localhost:8888/api/v1`

**Authentication**: Include JWT token in header: `Authorization: Bearer <token>`

### Public Endpoints
```
POST   /identity/auth/token              # Login
POST   /identity/users/registration      # Register
POST   /identity/users/verify-email      # Verify email
GET    /product/find-all                 # Get all products
GET    /category/**                      # Category endpoints
GET    /product/search                   # Search products
POST   /ai/chat                          # AI chatbot
POST   /ai/recommendations               # Get recommendations
```

### Protected Endpoints
```
GET    /identity/users/profile           # Get/Update profile
PUT    /identity/users/profile
GET    /cart/**                          # Cart operations
POST   /cart/**                          # Add to cart
DELETE /cart/**                          # Remove from cart
POST   /order/**                         # Create order
GET    /order/**                         # Get orders
POST   /payment/**                       # Payment operations
GET    /inventory/**                     # Inventory operations
```

## 🔐 Security

- JWT-based authentication with token expiration (1 hour)
- Refresh token mechanism
- BCrypt password hashing
- API Gateway centralized authentication
- CORS configured for frontend
- Environment variables for sensitive data
