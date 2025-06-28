@echo off
echo Testing Backend Endpoints...
echo.

echo 1. Checking if backend is running on port 8080...
netstat -an | findstr :8080
echo.

echo 2. Testing login endpoint...
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@test.com\",\"password\":\"test123\"}"
echo.
echo.

echo 3. Testing register endpoint...
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"testuser@example.com\",\"password\":\"test123\",\"role\":\"GUEST\"}"
echo.
echo.

echo 4. Testing forgot-password endpoint...
curl -X POST http://localhost:8080/api/auth/forgot-password ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@test.com\"}"
echo.
echo.

echo Testing complete!
pause
