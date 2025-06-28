@echo off
cls
echo ================================================
echo        GMAIL EMAIL CONFIGURATION SETUP
echo ================================================
echo.

echo STEP 1: EMAIL CONFIGURATION REQUIREMENTS
echo ----------------------------------------
echo Before testing, you need to:
echo.
echo 1. Enable 2-Factor Authentication on your Gmail account
echo 2. Generate an App Password:
echo    - Go to: https://myaccount.google.com/security
echo    - Search for "App passwords"
echo    - Generate a password for "Mail"
echo    - Copy the 16-character password
echo.
echo 3. Update your application.properties:
echo    spring.mail.username=your-actual-email@gmail.com
echo    spring.mail.password=your-16-char-app-password
echo.
pause

echo.
echo STEP 2: CHECKING EMAIL CONFIGURATION
echo ------------------------------------
echo Testing current email configuration...
curl -X GET "http://localhost:8080/api/auth/test-email-config" -H "Content-Type: application/json"
echo.
echo.
pause

echo.
echo STEP 3: SENDING TEST EMAIL
echo --------------------------
set /p email="Enter your email to send test confirmation: "
echo Sending test email to: %email%
curl -X POST "http://localhost:8080/api/auth/test-send-email?email=%email%" -H "Content-Type: application/json"
echo.
echo.

echo STEP 4: CHECK APPLICATION LOGS
echo -------------------------------
echo Please check your Spring Boot application console for:
echo 1. "=== ATTEMPTING TO SEND CONFIRMATION EMAIL ==="
echo 2. Email configuration details
echo 3. Confirmation URL (if email fails)
echo 4. Any error messages
echo.
echo Look for lines containing:
echo - "CONFIRMATION EMAIL SENT SUCCESSFULLY" = Success
echo - "FAILED TO SEND CONFIRMATION EMAIL" = Failed
echo - "MANUAL CONFIRMATION URL" = Use this if email fails
echo.

echo STEP 5: COMMON ISSUES AND SOLUTIONS
echo ------------------------------------
echo Problem: "Email configuration not properly set"
echo Solution: Update application.properties with real credentials
echo.
echo Problem: "Authentication failed"
echo Solution: Check your Gmail App Password (not regular password)
echo.
echo Problem: "Connection timeout"
echo Solution: Check internet connection and firewall settings
echo.
echo Problem: Email sent but not received
echo Solution: Check spam/junk folder, wait a few minutes
echo.

pause
