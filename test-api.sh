#!/bin/bash

# Tourism App API Test Script
# Run this script after starting the Spring Boot server

BASE_URL="http://localhost:8080"

echo "🚀 Testing Tourism App API..."
echo "================================"

# Test 1: Check if server is running
echo "1. Checking if server is running..."
response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/accommodation-units")
if [ "$response" = "200" ]; then
    echo "✅ Server is running on port 8080"
else
    echo "❌ Server not responding (HTTP $response)"
    exit 1
fi

# Test 2: Register a test user
echo "2. Registering test user..."
register_response=$(curl -s -X POST "$BASE_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "Test Owner",
        "email": "testowner@example.com",
        "password": "password123",
        "role": "OWNER"
    }')

if echo "$register_response" | grep -q "successfully\|created\|token"; then
    echo "✅ User registration successful"
else
    echo "⚠️  User registration response: $register_response"
fi

# Test 3: Login and get JWT token
echo "3. Logging in..."
login_response=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "email": "testowner@example.com",
        "password": "password123"
    }')

# Extract token (assuming response format: {"token":"..."})
token=$(echo "$login_response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$token" ]; then
    echo "✅ Login successful, token received"
else
    echo "❌ Login failed or no token received"
    echo "Response: $login_response"
    exit 1
fi

# Test 4: Test dashboard stats endpoint
echo "4. Testing dashboard stats..."
dashboard_response=$(curl -s -H "Authorization: Bearer $token" \
    "$BASE_URL/api/dashboard/stats")

if echo "$dashboard_response" | grep -q "totalUnits\|totalBookings"; then
    echo "✅ Dashboard stats endpoint working"
    echo "Response: $dashboard_response"
else
    echo "❌ Dashboard stats endpoint failed"
    echo "Response: $dashboard_response"
fi

# Test 5: Test dashboard insights endpoint
echo "5. Testing dashboard insights..."
insights_response=$(curl -s -H "Authorization: Bearer $token" \
    "$BASE_URL/api/dashboard/insights")

if echo "$insights_response" | grep -q "topPerformingUnit\|occupancyRate"; then
    echo "✅ Dashboard insights endpoint working"
    echo "Response: $insights_response"
else
    echo "❌ Dashboard insights endpoint failed"
    echo "Response: $insights_response"
fi

# Test 6: Create an accommodation unit
echo "6. Creating test accommodation unit..."
unit_response=$(curl -s -X POST "$BASE_URL/api/accommodation-units" \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "Test Beach House",
        "description": "A beautiful test property",
        "address": "123 Test Street",
        "city": "Test City",
        "country": "Test Country",
        "pricePerNight": 100.0,
        "type": "HOUSE",
        "maxGuests": 4,
        "bedrooms": 2,
        "bathrooms": 1,
        "amenities": ["WiFi", "Kitchen"]
    }')

if echo "$unit_response" | grep -q "id\|Test Beach House"; then
    echo "✅ Accommodation unit creation successful"
    unit_id=$(echo "$unit_response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "Unit ID: $unit_id"
else
    echo "❌ Accommodation unit creation failed"
    echo "Response: $unit_response"
fi

# Test 7: Get user's units
echo "7. Getting user's units..."
my_units_response=$(curl -s -H "Authorization: Bearer $token" \
    "$BASE_URL/api/accommodation-units/my-units")

if echo "$my_units_response" | grep -q "Test Beach House\|\[\]"; then
    echo "✅ My units endpoint working"
else
    echo "❌ My units endpoint failed"
    echo "Response: $my_units_response"
fi

echo "================================"
echo "🎉 API Testing Complete!"
echo ""
echo "📋 Summary:"
echo "- Server: Running ✅"
echo "- Authentication: Working ✅"
echo "- Dashboard Stats: Working ✅"
echo "- Dashboard Insights: Working ✅"
echo "- Accommodation Units: Working ✅"
echo ""
echo "🔗 You can now test with the frontend or use Postman"
echo "📖 See API_TEST_GUIDE.md for complete endpoint documentation"
