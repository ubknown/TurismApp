@echo off
echo 🔧 Testing Frontend PostCSS Fix...
echo =======================================

cd "c:\Users\razvi\Desktop\SCD\TurismApp\New front"

echo 📦 Checking if dependencies are installed...
if not exist node_modules (
    echo Installing dependencies...
    call npm install
)

echo 🔍 Testing PostCSS configuration...
echo Running: npm run build

call npm run build

if %errorlevel%==0 (
    echo.
    echo ✅ SUCCESS: PostCSS configuration is working correctly!
    echo ✅ Project builds without errors
    echo.
    echo 🚀 You can now start the development server with:
    echo    npm run dev
    echo.
    echo 🌐 The frontend will be available at: http://localhost:5173
) else (
    echo.
    echo ❌ FAILED: There are still build errors
    echo Please check the output above for details
)

echo.
echo 📋 PostCSS Configuration Summary:
echo - Updated postcss.config.js to use ES module syntax ^(export default^)
echo - Removed duplicate postcss.config.cjs file
echo - TailwindCSS and Autoprefixer plugins are configured
echo - Compatible with "type": "module" in package.json

pause
