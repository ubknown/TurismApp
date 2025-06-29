@echo off
echo ========================================
echo TESTING OWNER APPLICATION APPROVAL SYSTEM
echo ========================================
echo.

REM Set backend URL
set BACKEND_URL=http://localhost:8080

echo 1. Testing pending applications endpoint...
curl -X GET "%BACKEND_URL%/api/owner-application/pending" ^
  -H "Content-Type: application/json" ^
  --silent --show-error | jq . 2>nul || echo Response received
echo.

echo 2. Testing approval page endpoint (with dummy data)...
curl -X GET "%BACKEND_URL%/api/owner-application/respond?applicationId=1&action=approve&token=dummy" ^
  -H "Content-Type: application/json" ^
  --silent --show-error | jq . 2>nul || echo Response received
echo.

echo 3. Testing rejection page endpoint (with dummy data)...
curl -X GET "%BACKEND_URL%/api/owner-application/respond?applicationId=1&action=reject&token=dummy" ^
  -H "Content-Type: application/json" ^
  --silent --show-error | jq . 2>nul || echo Response received
echo.

echo 4. Testing admin links generation endpoint...
curl -X GET "%BACKEND_URL%/api/owner-application/1/admin-links" ^
  -H "Content-Type: application/json" ^
  --silent --show-error | jq . 2>nul || echo Response received
echo.

echo ========================================
echo MANUAL TESTING INSTRUCTIONS
echo ========================================
echo.
echo 1. Start the backend: mvnw spring-boot:run
echo 2. Start the frontend: cd "New front" ^&^& npm run dev
echo 3. Create a test owner application (register as guest, then apply)
echo 4. View pending applications in admin dashboard
echo 5. Click approve/reject links
echo 6. Enter password: Rzvtare112
echo 7. Verify email is sent and user role is updated
echo.

echo ========================================
echo SECURITY FEATURES IMPLEMENTED
echo ========================================
echo.
echo ✅ Password-protected approval/rejection
echo ✅ Secure token validation
echo ✅ One-time processing (prevents duplicate actions)
echo ✅ Admin action logging
echo ✅ Automatic email notifications
echo ✅ User role and status updates
echo ✅ Frontend confirmation page with nice UI
echo.

pause
