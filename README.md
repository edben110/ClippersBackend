# 🔧 Clipers Backend

> Modern REST API built with Spring Boot for the Clipers platform - A professional video-CV social network.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0+-green.svg)](https://www.mongodb.com/)
[![License](https://img.shields.io/badge/License-Private-red.svg)](LICENSE)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Design Patterns](#design-patterns)
- [Security](#security)
- [Deployment](#deployment)
- [Contributing](#contributing)

---

## 🎯 Overview

Clipers Backend is a production-ready REST API that powers a modern professional networking platform where candidates showcase their skills through video CVs (Clipers) and companies find the perfect talent using AI-powered matching algorithms.

### Key Capabilities

- 🎥 **Video CV Management** - Upload, process, and manage professional video profiles
- 🤖 **AI-Powered Matching** - Intelligent candidate-job matching with multiple strategies
- 🔐 **Secure Authentication** - JWT-based stateless authentication with refresh tokens
- 👥 **Social Features** - Posts, likes, comments, and professional networking
- 📊 **ATS Integration** - Applicant Tracking System with structured profiles
- 🧪 **Technical Tests** - Automated technical assessment system
- 📱 **Real-time Notifications** - Multi-channel notification system

---

## ✨ Features

### For Candidates
- ✅ Create and manage video CVs (Clipers)
- ✅ Build comprehensive ATS profiles
- ✅ Apply to jobs with AI matching scores
- ✅ Receive and complete technical tests
- ✅ Engage in professional social feed
- ✅ Track application status

### For Companies
- ✅ Post job opportunities
- ✅ View AI-ranked applicants
- ✅ Send custom technical tests
- ✅ Manage applications
- ✅ Access company dashboard
- ✅ Review candidate profiles

### For Administrators
- ✅ Full system access
- ✅ User management
- ✅ Data cleanup tools
- ✅ System monitoring

---

## 🚀 Technology Stack

### Core Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17 (LTS) | Programming language |
| **Spring Boot** | 3.5.6 | Application framework |
| **MongoDB** | 7.0+ | NoSQL database |
| **Maven** | 3.6+ | Build tool |

### Spring Ecosystem

- **Spring Data MongoDB** - Data access layer
- **Spring Security** - Authentication & authorization
- **Spring Validation** - Input validation
- **Spring Web** - REST API development

### Security & Authentication

- **JWT (jjwt)** 0.12.3 - Token-based authentication
- **BCrypt** - Password hashing
- **Spring Security** - Security framework

### Additional Libraries

- **Lombok** - Reduce boilerplate code
- **MapStruct** 1.5.5 - Object mapping
- **Spring Dotenv** 4.0.0 - Environment management
- **Jackson** - JSON processing
- **Commons FileUpload** 1.5 - File handling

---

## 🏗️ Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│         Controller Layer                │  ← REST endpoints
├─────────────────────────────────────────┤
│          Service Layer                  │  ← Business logic
├─────────────────────────────────────────┤
│        Repository Layer                 │  ← Data access
├─────────────────────────────────────────┤
│          Entity Layer                   │  ← Domain models
└─────────────────────────────────────────┘
```

### Package Structure

```
com.clipers.clipers/
├── config/              # Configuration classes
├── controller/          # REST Controllers (11 controllers)
├── dto/                 # Data Transfer Objects
├── entity/              # Domain Entities (15 entities)
├── repository/          # Data Access Layer (11 repositories)
├── security/            # Security Components
└── service/             # Business Logic (10 services)
```

### Design Patterns Implemented

The project implements **17 professional design patterns**:

#### Creational Patterns (3)
- ✅ **Singleton** - Spring IoC Container
- ✅ **Factory Method** - User creation by role
- ✅ **Builder** - ATS Profile construction

#### Structural Patterns (3)
- ✅ **Repository** - Data access abstraction
- ✅ **Adapter** - Entity-DTO conversion
- ✅ **Facade** - Simplified API interface

#### Behavioral Patterns (6)
- ✅ **Template Method** - Process flows
- ✅ **Strategy** - Matching algorithms
- ✅ **Observer** - Notification system
- ✅ **Mediator** - Post interactions
- ✅ **State** - Cliper status management
- ✅ **Chain of Responsibility** - Processing chain

#### Architectural Patterns (5)
- ✅ **Dependency Injection** - Spring DI
- ✅ **MVC** - Model-View-Controller
- ✅ **Layered Architecture** - Separation of concerns
- ✅ **Authentication Filter Chain** - Security
- ✅ **Role-Based Access Control** - Authorization

📖 **See [ARCHITECTURE.md](./ARCHITECTURE.md) for detailed documentation**

---

## 🚀 Quick Start

### Prerequisites

- ☕ Java 17 or higher
- 📦 Maven 3.6+
- 🍃 MongoDB 7.0+ (local or Atlas)

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd backend
```

2. **Configure environment variables**
```bash
cp .env.example .env
```

Edit `.env` with your configuration:
```bash
# MongoDB
MONGODB_URI=mongodb://localhost:27017/clipers_db

# AI Services (REQUIRED)
AI_MATCHING_SERVICE_URL=https://selector.clipers.pro
VIDEO_PROCESSING_SERVICE_URL=https://video.clipers.pro

# JWT Secret (REQUIRED)
JWT_SECRET=your-secret-key-min-256-bits
```

3. **Start MongoDB**
```bash
# Windows
start-mongodb.bat

# Linux/Mac
docker run -d -p 27017:27017 --name clipers-mongodb mongo:7.0
```

4. **Run the application**
```bash
# Development mode
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/clipers-0.0.1-SNAPSHOT.jar
```

5. **Verify installation**
- Health check: http://localhost:8080/actuator/health
- Test endpoint: http://localhost:8080/api/test/health

---

## ⚙️ Configuration

### Environment Variables

#### Required Variables

```bash
# Database
MONGODB_URI=mongodb://localhost:27017/clipers_db

# AI Services
AI_MATCHING_SERVICE_URL=https://selector.clipers.pro
VIDEO_PROCESSING_SERVICE_URL=https://video.clipers.pro

# Security
JWT_SECRET=your-secret-key-min-256-bits
```

#### Optional Variables (with defaults)

```bash
# Server
SPRING_PROFILE=dev                    # dev | prod
SERVER_PORT=8080

# JWT
JWT_EXPIRATION=86400000              # 24 hours in ms

# File Upload
MAX_FILE_SIZE=50MB
MAX_REQUEST_SIZE=50MB
FILE_UPLOAD_DIR=./uploads

# Frontend
FRONTEND_URL=http://localhost:3000
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001

# File URLs
FILE_UPLOAD_BASE_URL=http://localhost:8080
```

### Application Profiles

#### Development Profile (`dev`)
- Test endpoints publicly accessible
- Admin cleanup endpoints available
- Detailed logging
- Hot reload enabled

#### Production Profile (`prod`)
- Test endpoints require ADMIN role
- Enhanced security
- Optimized logging
- Performance tuning

Set profile with:
```bash
SPRING_PROFILE=prod
```

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "role": "CANDIDATE",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": "123",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "CANDIDATE"
  }
}
```

#### Get Current User
```http
GET /api/auth/me
Authorization: Bearer <access-token>
```

### Cliper Endpoints

#### Upload Cliper
```http
POST /api/clipers/upload
Authorization: Bearer <access-token>
Content-Type: multipart/form-data

video: <file>
title: "My Professional Introduction"
description: "Software Developer with 5 years experience"
```

#### Get Public Clipers
```http
GET /api/clipers/public?page=0&size=12
```

### Job Endpoints

#### Create Job (Company only)
```http
POST /api/jobs
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "title": "Senior Java Developer",
  "description": "We are looking for...",
  "requirements": ["5+ years Java", "Spring Boot"],
  "skills": ["Java", "Spring Boot", "MongoDB"],
  "location": "Remote",
  "type": "FULL_TIME",
  "salaryMin": 5000000,
  "salaryMax": 8000000
}
```

#### Get Public Jobs
```http
GET /api/jobs/public?page=0&size=10
```

#### Apply to Job (Candidate only)
```http
POST /api/jobs/{jobId}/apply
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "message": "I'm interested in this position..."
}
```

### Post Endpoints

#### Create Post
```http
POST /api/posts
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "content": "Excited to share my new project!",
  "type": "TEXT"
}
```

#### Get Feed
```http
GET /api/posts?page=0&size=10
```

### Role-Based Access

| Endpoint | Public | Candidate | Company | Admin |
|----------|--------|-----------|---------|-------|
| `/api/auth/login` | ✅ | ✅ | ✅ | ✅ |
| `/api/auth/register` | ✅ | ✅ | ✅ | ✅ |
| `/api/clipers/public` | ✅ | ✅ | ✅ | ✅ |
| `/api/clipers/upload` | ❌ | ✅ | ❌ | ✅ |
| `/api/jobs/public` | ✅ | ✅ | ✅ | ✅ |
| `/api/jobs/create` | ❌ | ❌ | ✅ | ✅ |
| `/api/jobs/{id}/apply` | ❌ | ✅ | ❌ | ✅ |
| `/api/admin/**` | ❌ | ❌ | ❌ | ✅ |

---

## 🔒 Security

### Authentication Flow

1. **User Login** → Credentials validation → JWT generation
2. **Authenticated Request** → JWT validation → SecurityContext setup
3. **Token Refresh** → Refresh token validation → New access token

### Security Features

- ✅ **JWT Tokens** - Stateless authentication
- ✅ **BCrypt Hashing** - Secure password storage
- ✅ **CORS Configuration** - Cross-origin resource sharing
- ✅ **Role-Based Access** - Fine-grained authorization
- ✅ **Input Validation** - Request payload validation
- ✅ **Method Security** - @PreAuthorize annotations

### Token Configuration

- **Access Token**: 24 hours expiration
- **Refresh Token**: 7 days expiration
- **Algorithm**: HS512
- **Claims**: userId, role, firstName, lastName

---

## 🐳 Deployment

### Docker Build

```bash
docker build -t clipers-backend .
```

### Docker Run

```bash
docker run -p 8080:8080 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/clipers_db \
  -e AI_MATCHING_SERVICE_URL=https://selector.clipers.pro \
  -e VIDEO_PROCESSING_SERVICE_URL=https://video.clipers.pro \
  -e JWT_SECRET=your-secret-key \
  clipers-backend
```

### Production Build

```bash
./mvnw clean package -DskipTests
java -jar target/clipers-0.0.1-SNAPSHOT.jar
```

### Environment Setup

1. **MongoDB**: Use MongoDB Atlas for production
2. **File Storage**: Configure persistent volume for `/app/uploads`
3. **Environment Variables**: Set all required variables
4. **Health Checks**: Monitor `/actuator/health`

---

## 🧪 Testing

### Run Tests

```bash
# All tests
./mvnw test

# With coverage
./mvnw test jacoco:report

# Skip tests
./mvnw clean package -DskipTests
```

### Test Structure

```
src/test/java/
└── com/clipers/clipers/
    ├── service/        # Service layer tests
    ├── controller/     # Controller tests
    ├── repository/     # Repository tests
    └── security/       # Security tests
```

---

## 📊 Performance

### Optimizations

- ✅ **Connection Pooling** - MongoDB (min: 10, max: 50)
- ✅ **HTTP/2 Support** - Enabled
- ✅ **Response Compression** - Gzip (min 1KB)
- ✅ **Thread Pool** - Tomcat (max: 200 threads)
- ✅ **Async Processing** - Background matching
- ✅ **Database Indexing** - Optimized queries

### Monitoring

- Health endpoint: `/actuator/health`
- Metrics: Available via Spring Actuator
- Logging: Configurable levels per package

---

## 🔧 Troubleshooting

### Common Issues

#### MongoDB Connection Error
```bash
# Check MongoDB is running
docker ps | grep mongo

# Verify connection string
echo $MONGODB_URI
```

#### Port Already in Use
```bash
# Change port in .env
SERVER_PORT=8081
```

#### AI Service Not Available
```bash
# Verify service URLs
curl https://selector.clipers.pro/health
curl https://video.clipers.pro/health
```

#### JWT Token Invalid
```bash
# Ensure JWT_SECRET is set and consistent
# Minimum 256 bits (32 characters)
```

---

## 📖 Additional Documentation

- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - Complete architecture documentation
- **[DESIGN_PATTERNS.md](./DESIGN_PATTERNS.md)** - Design patterns guide
- **[DEPLOYMENT.md](./DEPLOYMENT.md)** - Deployment instructions
- **[COOLIFY_SETUP.md](./COOLIFY_SETUP.md)** - Coolify deployment guide

---

## 🤝 Contributing

### Development Workflow

1. Create feature branch
2. Implement changes
3. Write tests
4. Run tests and linting
5. Submit pull request

### Code Standards

- ✅ Follow Java naming conventions
- ✅ Write meaningful comments
- ✅ Maintain test coverage > 80%
- ✅ Use design patterns appropriately
- ✅ Document public APIs

---

## 📝 License

Private - All rights reserved

---

## 👥 Team

**Clipers Development Team**

- Backend Architecture
- API Development
- Security Implementation
- Database Design

---

## 📞 Support

- 📧 Email: support@clipers.pro
- 📚 Documentation: [docs.clipers.pro](https://docs.clipers.pro)
- 🐛 Issues: GitHub Issues
- 💬 Discussions: GitHub Discussions

---

## 🎯 Project Status

- ✅ **Production Ready**
- ✅ **Actively Maintained**
- ✅ **Fully Documented**
- ✅ **Security Audited**

---

**Built with ❤️ using Spring Boot**
