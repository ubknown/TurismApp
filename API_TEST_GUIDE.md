# Tourism App API Testing Guide

## Starting the Server

Run the Spring Boot application using one of these commands:
```bash
# Using Maven wrapper (recommended)
./mvnw spring-boot:run

# Using Maven directly
mvn spring-boot:run

# Or run the JAR file
java -jar target/turismapp-0.0.1-SNAPSHOT.jar
```

The server should start on **port 8080**.

## Authentication Endpoints

### 1. Register a User
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "name": "Test Owner",
    "email": "owner@test.com",
    "password": "password123",
    "role": "OWNER"
}
```

### 2. Login
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "email": "owner@test.com",
    "password": "password123"
}
```

**Save the JWT token from the response for authenticated requests.**

## Dashboard Endpoints (Protected)

### 3. Get Dashboard Stats
```bash
GET http://localhost:8080/api/dashboard/stats
Authorization: Bearer YOUR_JWT_TOKEN
```

**Expected Response:**
```json
{
    "totalUnits": 0,
    "totalBookings": 0,
    "totalRevenue": 0.0,
    "averageRating": 0.0
}
```

### 4. Get Dashboard Insights
```bash
GET http://localhost:8080/api/dashboard/insights
Authorization: Bearer YOUR_JWT_TOKEN
```

**Expected Response:**
```json
{
    "topPerformingUnit": {"message": "No bookings found"},
    "occupancyRate": 0.0,
    "revenueGrowth": 0.0,
    "guestSatisfaction": 0.0
}
```

## Accommodation Unit Endpoints

### 5. Create an Accommodation Unit
```bash
POST http://localhost:8080/api/accommodation-units
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
    "name": "Beautiful Beach House",
    "description": "A stunning beach house with ocean views",
    "address": "123 Ocean Drive, Miami",
    "city": "Miami",
    "country": "USA",
    "pricePerNight": 150.0,
    "type": "HOUSE",
    "maxGuests": 6,
    "bedrooms": 3,
    "bathrooms": 2,
    "amenities": ["WiFi", "Pool", "Beach Access"]
}
```

### 6. Get User's Units
```bash
GET http://localhost:8080/api/accommodation-units/my-units
Authorization: Bearer YOUR_JWT_TOKEN
```

### 7. Get All Units (Public)
```bash
GET http://localhost:8080/api/accommodation-units
```

## Booking Endpoints

### 8. Create a Booking
```bash
POST http://localhost:8080/api/bookings
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
    "accommodationUnitId": 1,
    "checkInDate": "2025-07-01",
    "checkOutDate": "2025-07-05",
    "guestName": "John Doe",
    "guestEmail": "john@example.com",
    "totalPrice": 600.0
}
```

### 9. Get User's Bookings
```bash
GET http://localhost:8080/api/bookings/my-bookings
Authorization: Bearer YOUR_JWT_TOKEN
```

## Profit Statistics Endpoints

### 10. Calculate Profit
```bash
GET http://localhost:8080/api/profit/calculate?months=6
Authorization: Bearer YOUR_JWT_TOKEN
```

### 11. Get Monthly Profit
```bash
GET http://localhost:8080/api/profit/monthly?months=12
Authorization: Bearer YOUR_JWT_TOKEN
```

### 12. Predict Future Profits
```bash
GET http://localhost:8080/api/profit/predict?historyMonths=6&predictMonths=3
Authorization: Bearer YOUR_JWT_TOKEN
```

## Review Endpoints

### 13. Create a Review
```bash
POST http://localhost:8080/api/reviews
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
    "accommodationUnitId": 1,
    "rating": 5,
    "comment": "Amazing place! Highly recommended!"
}
```

### 14. Get Reviews for a Unit
```bash
GET http://localhost:8080/api/reviews/unit/1
```

## Testing Checklist

### ✅ Server Startup
- [ ] Server starts without errors
- [ ] Listens on port 8080
- [ ] Database connection established
- [ ] No compilation errors in logs

### ✅ Authentication Flow
- [ ] User registration works
- [ ] User login returns JWT token
- [ ] Protected endpoints reject requests without token
- [ ] Protected endpoints accept valid tokens

### ✅ Dashboard Functionality
- [ ] `/api/dashboard/stats` returns correct statistics
- [ ] `/api/dashboard/insights` returns performance metrics
- [ ] Dashboard data updates when new bookings/units are created

### ✅ Core Business Logic
- [ ] Can create accommodation units
- [ ] Can create bookings with totalPrice
- [ ] Profit calculations work correctly
- [ ] Review system functions properly

### ✅ Error Handling
- [ ] Invalid requests return appropriate error messages
- [ ] Database constraint violations handled gracefully
- [ ] Authentication errors return 401/403 status codes

## Postman Collection

Create a new Postman collection with the above requests. Set up:

1. **Environment Variables:**
   - `baseUrl`: `http://localhost:8080`
   - `jwtToken`: (set after login)

2. **Collection Authorization:**
   - Type: Bearer Token
   - Token: `{{jwtToken}}`

3. **Test Scripts:**
   Add to login request:
   ```javascript
   if (responseCode.code === 200) {
       var response = JSON.parse(responseBody);
       pm.environment.set("jwtToken", response.token);
   }
   ```

## Common Issues and Solutions

### Database Connection
- Ensure MySQL is running on localhost:3306
- Database `turismdb` exists
- Credentials in application.properties are correct

### Port Conflicts
- If port 8080 is in use, add to application.properties:
  ```properties
  server.port=8081
  ```

### CORS Issues
- Controllers have `@CrossOrigin(origins = "*")` annotation
- For production, specify exact frontend URL

## Verification Commands

After server starts, quickly verify with curl:

```bash
# Check server health
curl http://localhost:8080/api/accommodation-units

# Check authentication endpoint
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@test.com","password":"test123","role":"OWNER"}'
```
