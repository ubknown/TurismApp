@echo off
echo ========================================
echo TESTING YOUR SPECIFIC HASH
echo ========================================
echo.
echo Testing hash: $2a$10$JQnA/KNpUZFVzrPOdAzPmePGp3csnIZXZCyVuIkV9u3oe7yCHp0ya
echo Password: admin123
echo.

echo Using backend endpoint to test:
curl -s "http://localhost:8080/api/auth/test-password-hash?password=admin123"
echo.
echo.

echo ========================================
echo STEP-BY-STEP DEBUG
echo ========================================
echo.

echo 1. User details debug:
curl -s "http://localhost:8080/api/auth/debug-user-details?email=admin@tourism.com"
echo.
echo.

echo 2. Authentication components test:
curl -X POST "http://localhost:8080/api/auth/debug-authenticate" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@tourism.com\",\"password\":\"admin123\"}"
echo.
echo.

echo 3. Actual login attempt:
curl -X POST "http://localhost:8080/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@tourism.com\",\"password\":\"admin123\"}" ^
  -w "\nHTTP Status: %%{http_code}\n"
echo.
echo.

echo ========================================
echo BACKEND LOG ANALYSIS
echo ========================================
echo.
echo Check your backend console for these log messages:
echo.
echo ✅ SUCCESSFUL FLOW:
echo "🔐 Login attempt for email: admin@tourism.com"
echo "🔑 Raw password length: 8"
echo "✅ User found in DB - enabled: true, role: ADMIN"
echo "🔍 Password matches: true"
echo "🔐 Attempting Spring Security authentication..."
echo "✅ Authentication successful!"
echo.
echo ❌ FAILURE POINTS TO CHECK:
echo "❌ User not found in database"
echo "🔍 Password matches: false"
echo "❌ BAD CREDENTIALS: Password does not match"
echo "❌ ACCOUNT DISABLED: User account is disabled"
echo.

pause
