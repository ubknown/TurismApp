@echo off
echo ===============================================
echo DEBUG ADMIN DASHBOARD - JWT TOKEN METHOD
===============================================
echo.

echo Step 1: Login and extract JWT token...
echo ====================================

for /f "tokens=*" %%i in ('curl -s -X POST http://localhost:8080/api/admin/login -H "Content-Type: application/json" -d "{\"email\":\"admin@turismapp.com\",\"password\":\"admin123\"}"') do set LOGIN_RESPONSE=%%i

echo Login Response: %LOGIN_RESPONSE%
echo.

echo Step 2: Extract token from response...
echo ===================================
rem This is a simplified extraction - in real scenario you'd parse JSON properly
rem For now, let's manually copy the token from the response above

set /p JWT_TOKEN="Please copy the token from the response above and paste it here: "

echo.
echo Step 3: Test endpoint with JWT token...
echo ====================================

curl -X GET http://localhost:8080/api/owner-applications/pending ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %JWT_TOKEN%" ^
  -w "HTTP Status: %%{http_code}\n" ^
  -v

echo.
echo Step 4: Test alternative admin endpoint...
echo =======================================

curl -X GET http://localhost:8080/api/admin/applications ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %JWT_TOKEN%" ^
  -w "HTTP Status: %%{http_code}\n"

echo.
pause
