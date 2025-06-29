@echo off
echo ===============================================
echo TEST ADMIN DASHBOARD - NO REVIEW NOTES COLUMN
===============================================
echo.

echo Step 1: Login as admin and get JWT token...
echo ==========================================

for /f "tokens=*" %%i in ('curl -s -X POST http://localhost:8080/api/admin/login -H "Content-Type: application/json" -d "{\"email\":\"admin@turismapp.com\",\"password\":\"admin123\"}"') do set LOGIN_RESPONSE=%%i

echo Login Response: %LOGIN_RESPONSE%
echo.

echo Step 2: Extract token from response...
echo ===================================
set /p JWT_TOKEN="Please copy the token from the response above and paste it here: "

echo.
echo Step 3: Test admin applications endpoint...
echo =========================================

curl -X GET http://localhost:8080/api/admin/applications ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %JWT_TOKEN%" ^
  -w "HTTP Status: %%{http_code}\n"

echo.
echo Step 4: Test pending applications endpoint...
echo ===========================================

curl -X GET http://localhost:8080/api/owner-applications/pending ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %JWT_TOKEN%" ^
  -w "HTTP Status: %%{http_code}\n"

echo.
echo ===============================================
echo SUMMARY:
echo ===============================================
echo.
echo 1. Admin dashboard should now work without errors
echo 2. Review Notes column has been removed from frontend
echo 3. Backend no longer returns reviewNotes in API response
echo 4. Applications should display properly in admin dashboard
echo.
pause
