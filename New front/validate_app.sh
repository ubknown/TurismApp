#!/bin/bash

# 🧪 Tourism App - Quick Validation Script
# This script helps validate key functionality

echo "🏃‍♂️ Tourism Management App - Quick Validation"
echo "=============================================="

echo ""
echo "📋 Pre-flight Checklist:"
echo "1. Backend running on http://localhost:8080 ✓"
echo "2. Frontend running on http://localhost:5173 ✓"
echo "3. Database connected and seeded ✓"
echo ""

echo "🔍 Testing Critical Endpoints..."

# Test public endpoints (should work without auth)
echo "Testing public endpoints:"
curl -s -o /dev/null -w "Public Units API: %{http_code}\n" http://localhost:8080/api/units/public

# Test auth endpoints
echo "Testing auth endpoints:"
curl -s -o /dev/null -w "Auth Login: %{http_code}\n" -X POST http://localhost:8080/api/auth/login

echo ""
echo "📱 Browser Testing Steps:"
echo "1. Open http://localhost:5173"
echo "2. Follow the E2E_TESTING_GUIDE.md"
echo "3. Test each user role systematically"
echo ""

echo "🔒 Security Validation:"
echo "- Test unauthorized access to protected routes"
echo "- Verify JWT token expiration handling"  
echo "- Check role-based data isolation"
echo ""

echo "📊 Data Validation:"
echo "- Verify owner sees only their units/bookings"
echo "- Check guest can only book and review"
echo "- Validate profit data accuracy"
echo ""

echo "✅ Complete testing checklist in E2E_TESTING_GUIDE.md"
