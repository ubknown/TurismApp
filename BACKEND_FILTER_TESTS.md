# 🧪 Backend Filtering Tests - Postman/Curl Commands

## Base URL
```
http://localhost:8080/api/units/public
```

## 1. Test No Filters (Should Return All Units)
```bash
curl -X GET "http://localhost:8080/api/units/public"
```
**Expected:** Should return all 20 units

## 2. Test County Filter
```bash
curl -X GET "http://localhost:8080/api/units/public?county=Cluj"
```
**Postman:** `GET http://localhost:8080/api/units/public?county=Cluj`

## 3. Test Type Filter
```bash
curl -X GET "http://localhost:8080/api/units/public?type=HOTEL"
```
**Postman:** `GET http://localhost:8080/api/units/public?type=HOTEL`

**Valid types:** HOTEL, PENSIUNE, CABANA, VILA, APARTAMENT, CASA_DE_VACANTA, HOSTEL, MOTEL, CAMPING, BUNGALOW

## 4. Test Min Price Filter
```bash
curl -X GET "http://localhost:8080/api/units/public?minPrice=100"
```
**Postman:** `GET http://localhost:8080/api/units/public?minPrice=100`

## 5. Test Max Price Filter
```bash
curl -X GET "http://localhost:8080/api/units/public?maxPrice=300"
```
**Postman:** `GET http://localhost:8080/api/units/public?maxPrice=300`

## 6. Test Price Range
```bash
curl -X GET "http://localhost:8080/api/units/public?minPrice=100&maxPrice=300"
```
**Postman:** `GET http://localhost:8080/api/units/public?minPrice=100&maxPrice=300`

## 7. Test Capacity Filter
```bash
curl -X GET "http://localhost:8080/api/units/public?capacity=4"
```
**Postman:** `GET http://localhost:8080/api/units/public?capacity=4`

## 8. Test Rating Filter
```bash
curl -X GET "http://localhost:8080/api/units/public?minRating=4"
```
**Postman:** `GET http://localhost:8080/api/units/public?minRating=4`

## 9. Test Search Filter
```bash
curl -X GET "http://localhost:8080/api/units/public?search=mountain"
```
**Postman:** `GET http://localhost:8080/api/units/public?search=mountain`

## 10. Test Combined Filters
```bash
curl -X GET "http://localhost:8080/api/units/public?county=Cluj&type=HOTEL&minPrice=100&maxPrice=300&capacity=2&minRating=3"
```
**Postman:** `GET http://localhost:8080/api/units/public?county=Cluj&type=HOTEL&minPrice=100&maxPrice=300&capacity=2&minRating=3`

## 11. Test Date Availability
```bash
curl -X GET "http://localhost:8080/api/units/public?checkIn=2025-07-01&checkOut=2025-07-05"
```
**Postman:** `GET http://localhost:8080/api/units/public?checkIn=2025-07-01&checkOut=2025-07-05`

## Debug Logs to Check
When running these tests, check your console for:
```
🌐 PUBLIC ENDPOINT ACCESSED - /api/units/public
=== PUBLIC UNITS REQUEST DEBUG ===
Search: 'null'
County: 'Cluj'
Type: 'HOTEL'
Price range: 100.0 - 300.0
Capacity: 2
MinRating: 3.0
📊 TOTAL ACTIVE UNITS IN DB: 20
🔍 SERVICE: getFilteredUnits called with parameters:
  - location: 'Cluj'
  - minPrice: 100.0
  - maxPrice: 300.0
  - minCapacity: 2
  - maxCapacity: null
  - type: 'HOTEL'
  - minRating: 3.0
🎯 SERVICE: Repository returned X units
```

## Troubleshooting
If any test returns 0 results when it shouldn't:
1. Check the debug logs for parameter values
2. Verify your database has units with those criteria
3. Check if units have `available = true` and `status = 'active'`
4. Verify data types match (String types, Double prices, Integer capacity)
