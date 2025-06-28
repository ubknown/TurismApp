# API Integration Testing Guide

## Overview
This guide covers testing the newly implemented protected data endpoints and booking functionality in the React frontend with your Spring Boot backend.

## New API Endpoints Integration

### 1. User Units Management 📋
**Endpoint**: `/api/units/my-units`
- **Method**: GET
- **Auth**: JWT Required
- **Page**: My Units (`/my-units`)

**Test Steps**:
1. Log in to the application
2. Navigate to "My Units" page
3. Verify the page makes a GET request to `/api/units/my-units`
4. Check that JWT token is included in Authorization header
5. Verify units are displayed in the glassmorphism cards
6. Test delete functionality (DELETE `/api/units/{id}`)
7. Test status toggle (PATCH `/api/units/{id}/status`)

### 2. Booking Creation 📅
**Endpoint**: `/api/bookings`
- **Method**: POST
- **Auth**: JWT Required
- **Component**: BookingForm modal

**Test Steps**:
1. Navigate to Units List page (`/units`)
2. Click "Book Now" on any unit card
3. Fill out the booking form:
   - Guest name and email (required)
   - Check-in and check-out dates (required)
   - Number of guests
   - Special requests (optional)
4. Verify price calculation works automatically
5. Submit form and check POST request to `/api/bookings`
6. Verify booking success toast appears
7. Test form validation (empty fields, invalid dates)

**Expected Request Body**:
```json
{
  "accommodationUnitId": 1,
  "guestName": "John Doe",
  "guestEmail": "john@example.com",
  "guestPhone": "+1234567890",
  "checkInDate": "2025-07-01",
  "checkOutDate": "2025-07-05",
  "numberOfGuests": 2,
  "specialRequests": "Late check-in requested",
  "totalPrice": 480
}
```

### 3. Profit Analytics 📊
**Endpoint**: `/api/profit/stats`
- **Method**: GET
- **Auth**: JWT Required
- **Component**: ProfitChart on Dashboard

**Test Steps**:
1. Log in and go to Dashboard (`/dashboard`)
2. Scroll down to see the Profit Analytics chart
3. Verify GET request to `/api/profit/stats?range=12months`
4. Test different time ranges (6months, 12months, 2years)
5. Check that chart displays monthly data correctly
6. Verify summary cards show correct statistics
7. Test refresh functionality

**Expected Response Format**:
```json
{
  "monthlyData": [
    {
      "month": "2025-01",
      "revenue": 15420
    },
    {
      "month": "2025-02", 
      "revenue": 18650
    }
  ],
  "totalRevenue": 125430,
  "averageMonthly": 12543,
  "growth": 15.2,
  "bestMonth": "2025-06"
}
```

### 4. Dashboard Summary 📈
**Endpoints**: 
- `/api/profit/stats` - Revenue data
- `/api/bookings/recent?limit=5` - Recent bookings
- `/api/units/dashboard-summary` - Units summary

**Test Steps**:
1. Navigate to Dashboard (`/dashboard`)
2. Verify all three API calls are made on page load
3. Check that statistics cards display correct data:
   - Total Units
   - Total Bookings  
   - Monthly Revenue
   - Occupancy Rate
4. Verify recent bookings list shows latest 5 bookings
5. Check top performing units list

### 5. Public Units Search 🔍
**Endpoint**: `/api/units/public`
- **Method**: GET
- **Auth**: None (public endpoint)
- **Page**: Units List (`/units`)

**Test Steps**:
1. Navigate to Units List (can be done without login)
2. Test search functionality with query parameters
3. Test filters: location, price range, capacity, amenities
4. Verify URL updates with search parameters
5. Check that "Book Now" button opens booking form
6. Verify booking requires authentication (redirect to login if not authenticated)

**Example Request**: 
```
GET /api/units/public?search=mountain&location=Brasov&minPrice=100&maxPrice=300&capacity=4&amenities=wifi,parking
```

## Backend API Requirements

Your Spring Boot backend should implement these endpoints:

### Authentication Endpoints (Already Implemented)
```java
POST /api/auth/login
POST /api/auth/register
```

### New Protected Endpoints Needed
```java
// Units Management
GET /api/units/my-units          // Get user's accommodation units
DELETE /api/units/{id}           // Delete a unit
PATCH /api/units/{id}/status     // Toggle unit active/inactive status
GET /api/units/dashboard-summary // Get dashboard summary stats

// Public Units (No auth required)
GET /api/units/public            // Search/filter public units

// Bookings
POST /api/bookings               // Create new booking
GET /api/bookings/recent         // Get recent bookings for dashboard

// Profit Analytics  
GET /api/profit/stats            // Get profit statistics and charts data
```

## Testing with Browser Dev Tools

### 1. Network Tab Monitoring
- Open browser Dev Tools (F12)
- Go to Network tab
- Monitor API requests as you interact with the app
- Check request headers include `Authorization: Bearer <token>`
- Verify response status codes (200, 401, etc.)

### 2. Authentication Headers
All protected endpoints should include:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

### 3. Error Handling Tests
- Test with backend offline → Should show error toasts
- Test with invalid JWT → Should auto-logout with toast
- Test with network errors → Should show error messages
- Test with validation errors → Should show validation toasts

## Common Backend Response Formats

### Success Response
```json
{
  "data": [...],
  "message": "Success"
}
```

### Error Response  
```json
{
  "error": "Validation failed",
  "message": "Invalid input data",
  "details": ["Email is required", "Price must be positive"]
}
```

### Validation Response
```json
{
  "error": "Bad Request",
  "message": "Check-out date must be after check-in date"
}
```

## Step-by-Step Integration Test

### Complete Flow Test
1. **Setup**: Ensure backend is running on `http://localhost:8080`
2. **Register**: Create new account via `/register`
3. **Login**: Authenticate via `/login` 
4. **Dashboard**: View dashboard with real data from APIs
5. **My Units**: View user's accommodation units
6. **Units List**: Browse public units (can logout first)
7. **Book Unit**: Create a booking through the modal form
8. **Profit Chart**: View analytics with different time ranges
9. **Logout**: Test session cleanup

### Backend Verification
- Check database for created bookings
- Verify user association with units  
- Confirm profit calculations are correct
- Test data pagination if implemented

## Troubleshooting

### Common Issues
1. **CORS Errors**: Ensure backend allows `http://localhost:5173` origin
2. **401 Unauthorized**: Check JWT token format and expiration
3. **Missing Data**: Verify backend has sample data for testing
4. **Network Errors**: Confirm backend endpoints match frontend calls

### Debug Tips
- Check browser console for JavaScript errors
- Use Network tab to inspect request/response details
- Verify localStorage contains valid JWT token
- Test backend endpoints directly with Postman/curl

## Performance Notes

### Optimizations Implemented
- Automatic toast notifications for user feedback
- Loading states during API calls
- Error boundaries for graceful failure handling
- Proper form validation before API calls
- Responsive design for mobile testing

### Monitoring
- Watch network requests timing
- Monitor memory usage during chart rendering
- Test on different devices/browsers
- Verify smooth transitions and animations

This integration provides a complete tourism management system with real-time data from your Spring Boot backend!
