A microservices-based e-commerce backend with product management, order processing, payment integration, and an intelligent AI chatbot.

## 🎯 Overview

VGearVN provides a complete e-commerce backend solution with:

- ✅ JWT-based authentication with email verification
- ✅ Product & category management (CRUD operations)
- ✅ Shopping cart & order processing
- ✅ VNPay payment gateway integration
- ✅ Real-time inventory tracking
- ✅ Email notifications via Brevo
- ✅ AI chatbot with RAG and semantic routing
- ✅ Personalized product recommendations

## 🏗️ Architecture

```
<img width="1222" height="661" alt="pbl7" src="https://github.com/user-attachments/assets/6f4b9948-fb7f-45a3-a5f5-5fa7b4fec2e4" />

```

**Authentication**: API Gateway validates JWT tokens via Identity Service `/auth/introspect` endpoint.

## 🛠️ Technology Stack

**Backend (Java)**: Spring Boot 3.4.2, Spring Cloud Gateway, Spring Data JPA, Spring Kafka, JWT, Java 21  
**AI Service (Python)**: Flask 3.0.3, Google Gemini API, ChromaDB, Sentence Transformers  
**Databases**: MySQL 8.0+, MongoDB 5.0+, Apache Kafka 3.0+  
**External**: Brevo (email), VNPay (payment), Google Gemini (AI)

## 📦 Prerequisites

- **Java**: JDK 21+
- **Maven**: 3.8+
- **Python**: 3.9+
- **MySQL**: 8.0+
- **MongoDB**: 5.0+
- **Kafka**: 3.0+
- **Docker** (optional)

## 🚀 Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/your-username/backend.git
cd backend
```

### 2. Setup Infrastructure (Docker)
```bash
# MySQL
docker run -d --name mysql-pbl7 -e MYSQL_ROOT_PASSWORD=your_password -p 3307:3306 mysql:8.0

# MongoDB
docker run -d --name mongodb-pbl7 -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=your_password -p 27017:27017 mongo:5.0

# Kafka
docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper:latest
docker run -d --name kafka -p 9094:9094 -e KAFKA_BROKER_ID=1 -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 confluentinc/cp-kafka:latest
```

### 3. Install Dependencies
```bash
# Java services (dependencies auto-download on build)
mvn clean install

# AI Service
cd AI-service
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
```

### 4. Configuration

Create `.env` file in project root:

```bash
# Database
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
MONGODB_URI=mongodb://root:password@localhost:27017/db?authSource=admin
MONGO_DB=your_database

# JWT
JWT_SIGNER_KEY=your_jwt_signer_key
JWT_VALID_DURATION=3600
JWT_REFRESHABLE_DURATION=36000

# External Services
BREVO_API_KEY=your_brevo_api_key
VNPAY_TMN_CODE=your_vnpay_tmn_code
VNPAY_SECRET_KEY=your_vnpay_secret_key
GENMINI_API_KEY=your_gemini_api_key

# AI Service
VECTOR_STORE=path/to/vector/store
DB_CHAT_HISTORY_COLLECTION=chat_history
COLLECTION_NAME=products
semanticCacheCollection=semantic_cache
```

**⚠️ Never commit `.env` files to repository!**

### 5. Run Services

**Start Java services** (in separate terminals):
```bash
# API Gateway (start first)
cd api-gateway && mvn spring-boot:run

# Other services
cd identity-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd category-service && mvn spring-boot:run
cd cart-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

**Start AI Service**:
```bash
cd AI-service
source venv/bin/activate
python flask_serve.py
```

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

### Example Requests
```bash
# Login
curl -X POST http://localhost:8888/api/v1/identity/auth/token \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'

# Get Products
curl -X GET http://localhost:8888/api/v1/product/find-all \
  -H "Authorization: Bearer <token>"

# AI Chat
curl -X POST http://localhost:8888/api/v1/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"query": "Show me laptops", "session_id": "session-123", "user_id": "user-456"}'
```

## 🔐 Security

- JWT-based authentication with token expiration (1 hour)
- Refresh token mechanism
- BCrypt password hashing
- API Gateway centralized authentication
- CORS configured for frontend
- Environment variables for sensitive data
