@echo off
echo ===============================================
echo DEBUG ADMIN DASHBOARD - OWNER APPLICATIONS
echo ===============================================
echo.

echo Step 1: Testing admin login to get session cookie...
echo ==================================================
echo.

curl -c cookies.txt -X POST http://localhost:8080/api/admin/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@turismapp.com\",\"password\":\"admin123\"}" ^
  -w "Login HTTP Status: %%{http_code}\n"

echo.
echo Step 2: Testing owner applications endpoint with admin session...
echo =============================================================
echo.

curl -b cookies.txt -X GET http://localhost:8080/api/owner-applications/pending ^
  -H "Content-Type: application/json" ^
  -w "Applications HTTP Status: %%{http_code}\n" ^
  -v

echo.
echo Step 3: Testing alternative endpoint paths...
echo ==========================================
echo.

echo Testing /api/admin/applications:
curl -b cookies.txt -X GET http://localhost:8080/api/admin/applications ^
  -H "Content-Type: application/json" ^
  -w "HTTP Status: %%{http_code}\n"

echo.
echo Step 4: Check database directly...
echo ================================
echo.
echo Please check if there are records in owner_applications table:
echo SQL: SELECT * FROM turismdb.owner_applications WHERE status = 'PENDING';
echo.

echo Step 5: Backend logs analysis...
echo ==============================
echo.
echo Please check the backend console window for any error messages
echo Look for ERROR or WARN messages when the endpoint is called
echo.

pause
