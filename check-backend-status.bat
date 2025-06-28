@echo off
echo ========================================
echo BACKEND CONNECTION DIAGNOSTIC
echo ========================================
echo.

echo Step 1: Checking if port 8080 is in use...
netstat -an | findstr :8080
echo.

echo Step 2: Testing direct connection to localhost:8080...
curl -v http://localhost:8080 2>&1
echo.

echo Step 3: Checking for Java processes...
tasklist | findstr java
echo.

echo Step 4: Checking if backend responds to health check...
curl -v http://localhost:8080/api/auth/test-email-config 2>&1
echo.

echo ========================================
echo RESULTS INTERPRETATION:
echo ========================================
echo.
echo If you see:
echo - "No connections on port 8080" = Backend is NOT running
echo - "Connection refused" = Backend is NOT running  
echo - "Tomcat" or HTTP response = Backend IS running
echo - Java processes = Spring Boot might be running
echo.
pause
