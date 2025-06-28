@echo off
cls
echo ================================================
echo      EMAIL CONFIRMATION FLOW TEST SCRIPT
echo ================================================
echo.

echo [STEP 1] Testing registration with new email...
curl -X POST "http://localhost:8080/api/auth/register" ^
     -H "Content-Type: application/json" ^
     -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"test@example.com\",\"password\":\"password123\",\"role\":\"GUEST\"}"
echo.
echo.

echo [STEP 2] Testing registration with same email (should resend confirmation)...
curl -X POST "http://localhost:8080/api/auth/register" ^
     -H "Content-Type: application/json" ^
     -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"test@example.com\",\"password\":\"password123\",\"role\":\"GUEST\"}"
echo.
echo.

echo [STEP 3] Testing manual resend confirmation...
curl -X POST "http://localhost:8080/api/auth/resend-confirmation?email=test@example.com" ^
     -H "Content-Type: application/json"
echo.
echo.

echo [STEP 4] Testing login with unconfirmed account (should fail)...
curl -X POST "http://localhost:8080/api/auth/login" ^
     -H "Content-Type: application/json" ^
     -d "{\"email\":\"test@example.com\",\"password\":\"password123\"}"
echo.
echo.

echo ================================================
echo                 IMPORTANT NOTES
echo ================================================
echo 1. Check your application logs for email sending attempts
echo 2. To set up real email, update application.properties:
echo    - spring.mail.username=your-email@gmail.com
echo    - spring.mail.password=your-app-password
echo 3. For Gmail, you need to generate an App Password
echo 4. The confirmation URL will be: http://localhost:8080/api/auth/confirm?token=...
echo.
pause
