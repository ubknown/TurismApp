@echo off
echo 🏥 Backend Health Check
echo ======================
echo.

set BACKEND_URL=http://localhost:8080

echo 🔍 Testing backend server health...
echo.

REM Test if server is responding
echo 1. Basic server health check...
curl -s -o nul -w "HTTP Status: %%{http_code}" "%BACKEND_URL%/api/accommodation-units"
echo.

REM Test specific endpoints
echo.
echo 2. Testing key endpoints...

echo   📋 GET /api/accommodation-units
curl -s -o nul -w "   Status: %%{http_code}" "%BACKEND_URL%/api/accommodation-units"
echo.

echo   🔐 POST /api/auth/register
curl -s -o nul -w "   Status: %%{http_code}" "%BACKEND_URL%/api/auth/register"
echo.

echo   📊 Dashboard endpoints (requires auth)
echo   ⚠️  These will return 401/403 without valid JWT token:
curl -s -o nul -w "   GET /api/dashboard/stats - Status: %%{http_code}" "%BACKEND_URL%/api/dashboard/stats"
echo.
curl -s -o nul -w "   GET /api/dashboard/insights - Status: %%{http_code}" "%BACKEND_URL%/api/dashboard/insights"
echo.

echo.
echo 📈 Expected Results:
echo ✅ 200 - Public endpoints (accommodation-units, auth)
echo ✅ 401/403 - Protected endpoints (dashboard) without auth
echo ❌ Connection refused - Server not running
echo ❌ 404 - Endpoint mapping issues  
echo ❌ 500 - Server errors (check logs)

echo.
echo 🔗 Quick Test URLs:
echo - Backend API: %BACKEND_URL%/api
echo - Test endpoint: %BACKEND_URL%/api/accommodation-units
echo - Swagger UI: %BACKEND_URL%/swagger-ui.html (if enabled)

echo.
echo 🎯 Next Steps if Healthy:
echo 1. Test with Postman collection: Tourism_App_API.postman_collection.json
echo 2. Start frontend: npm run dev (in New front folder)
echo 3. Test full integration at: http://localhost:5173

pause
