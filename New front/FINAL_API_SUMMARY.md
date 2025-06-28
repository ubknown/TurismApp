# API Endpoints Summary & Backend Response Structures

## Complete API Integration Overview

This document provides a comprehensive overview of all API endpoints used by the frontend application and their expected response structures for the Spring Boot backend implementation.

---

## 🔐 Authentication Endpoints

### 1. User Registration
- **Endpoint**: `POST /api/auth/register`
- **Authentication**: None (public)
- **Used in**: RegisterPage.jsx

**Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe", 
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "phone": "+1234567890",
  "address": "123 Main St, City, Country"
}
```

**Success Response (201)**:
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "address": "123 Main St, City, Country",
    "createdAt": "2025-06-27T10:30:00Z"
  }
}
```

**Error Response (400)**:
```json
{
  "error": "Validation failed",
  "message": "Email already exists",
  "details": ["Email is already registered"]
}
```

### 2. User Login
- **Endpoint**: `POST /api/auth/login`
- **Authentication**: None (public)
- **Used in**: LoginPage.jsx

**Request Body**:
```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

**Success Response (200)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "address": "123 Main St, City, Country"
  }
}
```

**Error Response (401)**:
```json
{
  "error": "Authentication failed",
  "message": "Invalid email or password"
}
```

---

## 🏠 Accommodation Units Endpoints

### 3. Get User's Units
- **Endpoint**: `GET /api/units/my-units`
- **Authentication**: JWT Required
- **Used in**: MyUnitsPage.jsx

**Request Headers**:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Success Response (200)**:
```json
[
  {
    "id": 1,
    "name": "Cozy Mountain Cabin",
    "location": "Brasov, Romania",
    "description": "A beautiful mountain cabin with stunning views and modern amenities.",
    "price": 120.00,
    "capacity": 6,
    "rating": 4.8,
    "reviewCount": 24,
    "totalBookings": 47,
    "monthlyRevenue": 5640.00,
    "status": "active",
    "images": ["https://example.com/image1.jpg"],
    "amenities": ["wifi", "parking", "kitchen", "fireplace"],
    "createdAt": "2023-06-15T10:00:00Z"
  }
]
```

### 4. Get Public Units (Search/Filter)
- **Endpoint**: `GET /api/units/public`
- **Authentication**: None (public)
- **Used in**: UnitsListPage.jsx

**Query Parameters**:
- `search` (optional): Search term
- `location` (optional): Filter by location
- `minPrice` (optional): Minimum price filter
- `maxPrice` (optional): Maximum price filter  
- `capacity` (optional): Minimum capacity
- `amenities` (optional): Comma-separated amenities

**Example Request**:
```
GET /api/units/public?search=mountain&location=Brasov&minPrice=100&maxPrice=300&capacity=4&amenities=wifi,parking
```

**Success Response (200)**:
```json
[
  {
    "id": 1,
    "name": "Cozy Mountain Cabin",
    "location": "Brasov, Romania", 
    "description": "A beautiful mountain cabin with stunning views and modern amenities.",
    "price": 120.00,
    "capacity": 6,
    "rating": 4.8,
    "reviewCount": 24,
    "images": ["https://example.com/image1.jpg"],
    "amenities": ["wifi", "parking", "kitchen", "fireplace"],
    "owner": "John Doe",
    "available": true
  }
]
```

### 5. Delete Unit
- **Endpoint**: `DELETE /api/units/{id}`
- **Authentication**: JWT Required
- **Used in**: MyUnitsPage.jsx

**Request Headers**:
```
Authorization: Bearer <jwt_token>
```

**Success Response (200)**:
```json
{
  "message": "Unit deleted successfully"
}
```

**Error Response (403)**:
```json
{
  "error": "Forbidden",
  "message": "You can only delete your own units"
}
```

### 6. Toggle Unit Status
- **Endpoint**: `PATCH /api/units/{id}/status`
- **Authentication**: JWT Required
- **Used in**: MyUnitsPage.jsx

**Request Headers**:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "status": "inactive"
}
```

**Success Response (200)**:
```json
{
  "message": "Unit status updated successfully",
  "unit": {
    "id": 1,
    "name": "Cozy Mountain Cabin",
    "status": "inactive"
  }
}
```

### 7. Get Dashboard Summary
- **Endpoint**: `GET /api/units/dashboard-summary`
- **Authentication**: JWT Required
- **Used in**: DashboardPage.jsx

**Request Headers**:
```
Authorization: Bearer <jwt_token>
```

**Success Response (200)**:
```json
{
  "totalUnits": 12,
  "totalBookings": 47,
  "occupancyRate": 78.5,
  "topUnits": [
    {
      "id": 1,
      "name": "Lake View Suite",
      "bookings": 15,
      "revenue": 6750.00
    },
    {
      "id": 2, 
      "name": "Cozy Mountain Cabin",
      "bookings": 12,
      "revenue": 3840.00
    }
  ]
}
```

---

## 📅 Booking Endpoints

### 8. Create Booking
- **Endpoint**: `POST /api/bookings`
- **Authentication**: JWT Required
- **Used in**: BookingForm.jsx

**Request Headers**:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "accommodationUnitId": 1,
  "guestName": "Jane Smith",
  "guestEmail": "jane.smith@example.com", 
  "guestPhone": "+1234567890",
  "checkInDate": "2025-07-01",
  "checkOutDate": "2025-07-05",
  "numberOfGuests": 2,
  "specialRequests": "Late check-in requested",
  "totalPrice": 480.00
}
```

**Success Response (201)**:
```json
{
  "id": 123,
  "accommodationUnitId": 1,
  "unitName": "Cozy Mountain Cabin",
  "guestName": "Jane Smith",
  "guestEmail": "jane.smith@example.com",
  "guestPhone": "+1234567890",
  "checkInDate": "2025-07-01",
  "checkOutDate": "2025-07-05", 
  "numberOfGuests": 2,
  "specialRequests": "Late check-in requested",
  "totalPrice": 480.00,
  "status": "confirmed",
  "bookingDate": "2025-06-27T14:30:00Z"
}
```

**Error Response (400)**:
```json
{
  "error": "Validation failed",
  "message": "Check-out date must be after check-in date"
}
```

**Error Response (409)**:
```json
{
  "error": "Conflict",
  "message": "Unit is not available for the selected dates"
}
```

### 9. Get Recent Bookings
- **Endpoint**: `GET /api/bookings/recent`
- **Authentication**: JWT Required
- **Used in**: DashboardPage.jsx

**Query Parameters**:
- `limit` (optional): Number of bookings to return (default: 5)

**Request Headers**:
```
Authorization: Bearer <jwt_token>
```

**Example Request**:
```
GET /api/bookings/recent?limit=5
```

**Success Response (200)**:
```json
[
  {
    "id": 123,
    "guestName": "Jane Smith",
    "unitName": "Cozy Mountain Cabin",
    "checkIn": "2025-07-01",
    "amount": 480.00,
    "status": "confirmed",
    "bookingDate": "2025-06-27T14:30:00Z"
  },
  {
    "id": 124,
    "guestName": "Bob Wilson", 
    "unitName": "Lake View Suite",
    "checkIn": "2025-07-03",
    "amount": 720.00,
    "status": "confirmed",
    "bookingDate": "2025-06-26T09:15:00Z"
  }
]
```

---

## 📊 Profit Analytics Endpoints

### 10. Get Profit Statistics
- **Endpoint**: `GET /api/profit/stats`
- **Authentication**: JWT Required
- **Used in**: ProfitChart.jsx

**Query Parameters**:
- `range` (optional): Time range - "6months", "12months", "2years" (default: "12months")

**Request Headers**:
```
Authorization: Bearer <jwt_token>
```

**Example Request**:
```
GET /api/profit/stats?range=12months
```

**Success Response (200)**:
```json
{
  "monthlyData": [
    {
      "month": "2024-07",
      "revenue": 12450.00
    },
    {
      "month": "2024-08", 
      "revenue": 15680.00
    },
    {
      "month": "2024-09",
      "revenue": 18320.00
    },
    {
      "month": "2024-10",
      "revenue": 14275.00
    },
    {
      "month": "2024-11",
      "revenue": 16890.00
    },
    {
      "month": "2024-12",
      "revenue": 22150.00
    },
    {
      "month": "2025-01",
      "revenue": 19730.00
    },
    {
      "month": "2025-02",
      "revenue": 17950.00
    },
    {
      "month": "2025-03",
      "revenue": 21480.00
    },
    {
      "month": "2025-04",
      "revenue": 25670.00
    },
    {
      "month": "2025-05",
      "revenue": 28950.00
    },
    {
      "month": "2025-06",
      "revenue": 31200.00
    }
  ],
  "totalRevenue": 244745.00,
  "averageMonthly": 20395.42,
  "growth": 15.2,
  "bestMonth": "2025-06"
}
```

---

## 🚫 Error Response Standards

### Authentication Errors
**401 Unauthorized**:
```json
{
  "error": "Unauthorized", 
  "message": "Invalid or expired token"
}
```

**403 Forbidden**:
```json
{
  "error": "Forbidden",
  "message": "You don't have permission to access this resource"
}
```

### Validation Errors
**400 Bad Request**:
```json
{
  "error": "Validation failed",
  "message": "Invalid input data",
  "details": [
    "Email is required",
    "Price must be positive",
    "Check-out date must be after check-in date"
  ]
}
```

### Not Found Errors
**404 Not Found**:
```json
{
  "error": "Not found",
  "message": "Accommodation unit not found"
}
```

### Server Errors
**500 Internal Server Error**:
```json
{
  "error": "Internal server error",
  "message": "An unexpected error occurred"
}
```

---

## 🔒 Security Requirements

### JWT Token Format
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### CORS Configuration
The backend must allow the frontend origin:
```java
@CrossOrigin(origins = "http://localhost:5173")
```

### Rate Limiting (Recommended)
Consider implementing rate limiting for:
- Login attempts: 5 per minute
- Registration: 3 per hour
- Booking creation: 10 per hour

---

## 📱 Frontend Integration Status

| Endpoint | Component | Status | Toast Feedback | Error Handling | Empty States |
|----------|-----------|---------|----------------|----------------|--------------|
| `POST /api/auth/register` | RegisterPage | ✅ | ✅ | ✅ | N/A |
| `POST /api/auth/login` | LoginPage | ✅ | ✅ | ✅ | N/A |
| `GET /api/units/my-units` | MyUnitsPage | ✅ | ✅ | ✅ | ✅ |
| `GET /api/units/public` | UnitsListPage | ✅ | ✅ | ✅ | ✅ |
| `DELETE /api/units/{id}` | MyUnitsPage | ✅ | ✅ | ✅ | N/A |
| `PATCH /api/units/{id}/status` | MyUnitsPage | ✅ | ✅ | ✅ | N/A |
| `GET /api/units/dashboard-summary` | DashboardPage | ✅ | ✅ | ✅ | ✅ |
| `POST /api/bookings` | BookingForm | ✅ | ✅ | ✅ | N/A |
| `GET /api/bookings/recent` | DashboardPage | ✅ | ✅ | ✅ | ✅ |
| `GET /api/profit/stats` | ProfitChart | ✅ | ✅ | ✅ | ✅ |

---

## 🧪 Testing Recommendations

### Unit Tests for Backend
Test each endpoint with:
- Valid authentication tokens
- Invalid/expired tokens  
- Valid request data
- Invalid request data
- Edge cases (empty data, large datasets)

### Integration Tests
- End-to-end user flows
- Cross-browser compatibility
- Mobile responsiveness
- Performance under load

### API Documentation
Consider using Swagger/OpenAPI for interactive API documentation:
```java
@RestController
@RequestMapping("/api/units")
@Api(tags = "Accommodation Units")
public class AccommodationUnitController {
    // Implementation
}
```

This comprehensive API structure provides a solid foundation for your tourism management application with proper error handling, security, and scalability considerations.
