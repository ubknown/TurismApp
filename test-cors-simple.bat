@echo off
echo Testing CORS configuration...
echo.

echo 1. Testing simple GET request to auth endpoint:
curl -X GET "http://localhost:8080/api/auth/test-email-config" ^
  -H "Origin: http://localhost:5173" ^
  -H "Content-Type: application/json" ^
  -v

echo.
echo.

echo 2. Testing OPTIONS preflight request:
curl -X OPTIONS "http://localhost:8080/api/auth/login" ^
  -H "Origin: http://localhost:5173" ^
  -H "Access-Control-Request-Method: POST" ^
  -H "Access-Control-Request-Headers: Content-Type,Authorization" ^
  -v

echo.
echo.

echo 3. Testing POST request with credentials:
curl -X POST "http://localhost:8080/api/auth/login" ^
  -H "Origin: http://localhost:5173" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\",\"password\":\"password\"}" ^
  -v

echo.
echo CORS test complete.
pause
