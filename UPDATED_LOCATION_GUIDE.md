# 🏨 Updated Tourism App - Location-Based Architecture

## ✅ Changes Made

### 🔧 **Structural Changes:**
1. **Removed address fields from User registration** - Guests no longer need to provide location info
2. **Added location hierarchy**: `County` → `City` → `AccommodationUnit`
3. **Updated AccommodationUnit** to use proper location relationships
4. **Created Romanian counties/cities data loader** with 10 major counties and 50+ cities

### 📊 **New Database Schema:**
```sql
counties (id, name, code)
cities (id, name, type, county_id)
accommodation_units (..., county_id, city_id, address, ...)
```

### 🎯 **New Endpoints:**
- `GET /api/locations/counties` - Get all counties
- `GET /api/locations/counties/{countyId}/cities` - Get cities by county
- `GET /api/locations/cities/search?name=...` - Search cities

---

## 🧪 **Testing Guide**

### **1. Test Simplified User Registration**

**New Registration Request (No Address Required):**
```json
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "firstName": "Ana",
    "lastName": "Popescu",
    "email": "ana.popescu@test.com",
    "password": "password123",
    "role": "GUEST"
}
```

**Expected Response:**
```json
{
    "message": "Registration successful! Please check your email to confirm your account.",
    "user": {
        "id": 1,
        "firstName": "Ana",
        "lastName": "Popescu",
        "email": "ana.popescu@test.com",
        "role": "GUEST",
        "createdAt": "2025-06-27T16:49:26.589"
    }
}
```

### **2. Test Location Endpoints**

**Get All Counties:**
```http
GET http://localhost:8080/api/locations/counties
```

**Expected Response:**
```json
[
    {"id": 1, "name": "Alba", "code": "AB"},
    {"id": 2, "name": "Argeș", "code": "AG"},
    {"id": 3, "name": "Brașov", "code": "BV"},
    {"id": 4, "name": "București", "code": "B"},
    {"id": 5, "name": "Cluj", "code": "CJ"},
    ...
]
```

**Get Cities by County:**
```http
GET http://localhost:8080/api/locations/counties/3/cities
```

**Expected Response (Brașov cities):**
```json
[
    {"id": 1, "name": "Brașov", "type": "Municipiu", "county": {...}},
    {"id": 2, "name": "Codlea", "type": "Oraș", "county": {...}},
    {"id": 3, "name": "Râșnov", "type": "Oraș", "county": {...}},
    {"id": 4, "name": "Săcele", "type": "Oraș", "county": {...}},
    {"id": 5, "name": "Zărnești", "type": "Oraș", "county": {...}}
]
```

### **3. Test Owner Registration + Add Accommodation**

**Step 1 - Register Owner:**
```json
POST http://localhost:8080/api/auth/register

{
    "firstName": "Mihai",
    "lastName": "Ionescu",
    "email": "mihai.owner@test.com",
    "password": "password123",
    "role": "OWNER"
}
```

**Step 2 - Confirm Email (Get token from database):**
```http
GET http://localhost:8080/api/auth/confirm?token={TOKEN_FROM_DB}
```

**Step 3 - Login as Owner:**
```json
POST http://localhost:8080/api/auth/login

{
    "email": "mihai.owner@test.com",
    "password": "password123"
}
```

**Step 4 - Add Accommodation Unit:**
```json
POST http://localhost:8080/api/accommodation-units
Authorization: Bearer {JWT_TOKEN}

{
    "name": "Vila Carpați",
    "description": "Beautiful villa in the Carpathian Mountains",
    "countyId": 3,
    "cityId": 1,
    "address": "Strada Republicii 15A",
    "latitude": 45.6579,
    "longitude": 25.6012,
    "pricePerNight": 200.0,
    "capacity": 6,
    "type": "Vila",
    "images": ["vila1.jpg", "vila2.jpg"],
    "amenities": ["WiFi", "Parking", "Kitchen", "Garden"]
}
```

### **4. Test Filtering Accommodations by Location**

**Filter by County:**
```http
GET http://localhost:8080/api/accommodation-units?countyId=3
```

**Filter by City:**
```http
GET http://localhost:8080/api/accommodation-units?cityId=1
```

**Filter by County and Price Range:**
```http
GET http://localhost:8080/api/accommodation-units?countyId=3&minPrice=100&maxPrice=300
```

---

## 🎨 **Frontend Integration**

### **Registration Form (Simplified):**
```javascript
// Only these fields needed for registration
const registrationData = {
    firstName: "Ana",
    lastName: "Popescu", 
    email: "ana@test.com",
    password: "password123",
    role: "GUEST" // or "OWNER"
};
```

### **Add Accommodation Form (For Owners):**
```javascript
// 1. Load counties on page load
const counties = await fetch('/api/locations/counties').then(r => r.json());

// 2. Load cities when county selected
const cities = await fetch(`/api/locations/counties/${countyId}/cities`).then(r => r.json());

// 3. Submit form with location IDs
const accommodationData = {
    name: "Vila Carpați",
    description: "Beautiful villa...",
    countyId: 3,
    cityId: 1,
    address: "Strada Republicii 15A", // Free text
    pricePerNight: 200.0,
    capacity: 6,
    // ... other fields
};
```

### **Search/Filter Form:**
```javascript
// County dropdown for filtering
<select onChange={(e) => setSelectedCounty(e.target.value)}>
    <option value="">All Counties</option>
    {counties.map(county => (
        <option key={county.id} value={county.id}>
            {county.name}
        </option>
    ))}
</select>

// City dropdown (populated when county selected)
<select onChange={(e) => setSelectedCity(e.target.value)}>
    <option value="">All Cities</option>
    {cities.map(city => (
        <option key={city.id} value={city.id}>
            {city.name}
        </option>
    ))}
</select>
```

---

## 📊 **Database Verification Queries**

### **Check Location Data:**
```sql
-- Verify counties loaded
SELECT COUNT(*) as county_count FROM counties;

-- Verify cities loaded
SELECT COUNT(*) as city_count FROM cities;

-- Show counties with city counts
SELECT c.name, c.code, COUNT(ci.id) as city_count
FROM counties c 
LEFT JOIN cities ci ON c.id = ci.county_id 
GROUP BY c.id, c.name, c.code 
ORDER BY c.name;

-- Show all Brașov cities
SELECT ci.name, ci.type, co.name as county_name
FROM cities ci 
JOIN counties co ON ci.county_id = co.id 
WHERE co.code = 'BV' 
ORDER BY ci.name;
```

### **Check User Registration:**
```sql
-- Verify users don't have address fields
SELECT id, first_name, last_name, email, role, enabled, created_at 
FROM users 
ORDER BY created_at DESC;
```

### **Check Accommodation Units:**
```sql
-- Verify accommodation units have proper location relationships
SELECT au.name, au.address, ci.name as city, co.name as county
FROM accommodation_units au
JOIN cities ci ON au.city_id = ci.id
JOIN counties co ON au.county_id = co.id
ORDER BY au.created_at DESC;
```

---

## 🚀 **Next Steps**

1. **Run the updated SQL script** to create the new schema
2. **Restart the application** - location data will auto-load
3. **Test user registration** (no address required)
4. **Test location endpoints** for frontend integration
5. **Update frontend forms** to use the new location structure

This new architecture is much cleaner and more scalable! 🎉
