@echo off
echo � Testing Backend Health and CORS Configuration
echo ===============================================
echo.

echo ⏱️  Waiting for backend to be ready...
timeout /t 5 /nobreak >nul

echo.
echo 🏥 Testing Health Endpoint...
echo -----------------------------------------
curl -X GET "http://localhost:8080/actuator/health" -H "Content-Type: application/json" -w "\n⚡ HTTP Status: %%{http_code}\n" --connect-timeout 10 --max-time 30
echo.

echo � Testing Public Units Endpoint (No Auth Required)...
echo -----------------------------------------
curl -X GET "http://localhost:8080/api/units/public" -H "Content-Type: application/json" -H "Origin: http://localhost:5173" -w "\n⚡ HTTP Status: %%{http_code}\n" --connect-timeout 10 --max-time 30
echo.

echo 🔍 Testing with Search Parameter...
echo -----------------------------------------
curl -X GET "http://localhost:8080/api/units/public?search=hotel" -H "Content-Type: application/json" -H "Origin: http://localhost:5173" -w "\n⚡ HTTP Status: %%{http_code}\n" --connect-timeout 10 --max-time 30
echo.

echo 🌐 Testing CORS Preflight (OPTIONS)...
echo -----------------------------------------
curl -X OPTIONS "http://localhost:8080/api/units/public" -H "Origin: http://localhost:5173" -H "Access-Control-Request-Method: GET" -w "\n⚡ HTTP Status: %%{http_code}\n" --connect-timeout 10 --max-time 30
echo.

echo 📊 Result Analysis:
echo ==================
echo ✅ HTTP 200 = Success
echo ✅ Empty array [] = No data (normal for new database)
echo ❌ HTTP 403 = Security configuration issue
echo ❌ HTTP 0 or timeout = Backend not running
echo ❌ Connection refused = Port 8080 not accessible
echo.

pause
