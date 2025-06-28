# Backend Alignment Plan

## Current Status Analysis

### ✅ What's Already Implemented
- Basic Spring Boot structure with good dependencies
- User authentication with JWT
- AccommodationUnit CRUD operations
- Booking system
- Security configuration
- Database models

### ❌ Critical Missing Alignments

#### 1. **Authentication Endpoints**
- ❌ `POST /api/auth/register` - Missing registration endpoint
- ⚠️ `POST /api/auth/login` - Response structure doesn't match frontend expectations

#### 2. **Units Endpoints** 
- ❌ `GET /api/units/public` - Missing public units with filtering
- ✅ `GET /api/units/my-units` - Implemented ✓
- ⚠️ Response structures need additional fields (rating, reviewCount, etc.)

#### 3. **Booking Endpoints**
- ⚠️ `POST /api/bookings` - Needs to match frontend request structure
- ❌ `GET /api/bookings/recent` - Missing recent bookings endpoint

#### 4. **Profit/Statistics Endpoints** 
- ❌ `GET /api/profit/stats` - Missing dashboard statistics endpoint
- ⚠️ Existing profit endpoints don't match frontend expectations

#### 5. **Data Structure Alignments**
- User model missing firstName, lastName, phone, address fields
- AccommodationUnit missing reviewCount, totalBookings, monthlyRevenue, images, amenities
- Error response format needs standardization

## Implementation Steps

### Step 1: Fix User Model & Registration
### Step 2: Add Public Units Endpoint  
### Step 3: Standardize Response Structures
### Step 4: Add Missing Booking Endpoints
### Step 5: Create Profit/Stats Endpoints
### Step 6: Test Full Integration

---

## Detailed Required Changes

### User Model Enhancement
```java
// Add missing fields:
private String firstName;
private String lastName; 
private String phone;
private String address;
private LocalDateTime createdAt;
```

### AccommodationUnit Model Enhancement
```java
// Add missing fields:
private Double rating;
private Integer reviewCount;
private Integer totalBookings;
private Double monthlyRevenue;
private String status;
private List<String> images;
private List<String> amenities;
```

### Response DTOs Needed
- UserResponseDTO
- UnitResponseDTO
- BookingResponseDTO
- ErrorResponseDTO
- StatsResponseDTO
