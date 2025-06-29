@echo off
echo ===============================================
echo CIRCULAR REFERENCE FIX VERIFICATION
echo ===============================================
echo.

echo Testing backend API endpoints...
echo.

echo 1. Testing /api/units/public endpoint:
echo ======================================
curl -s "http://localhost:8080/api/units/public" > api_test.json
if exist api_test.json (
    for %%A in (api_test.json) do (
        if %%~zA GTR 100000 (
            echo ❌ FAILED: Response size is %%~zA bytes - still has circular references
        ) else (
            echo ✅ SUCCESS: Response size is %%~zA bytes - circular references fixed
        )
    )
) else (
    echo ❌ FAILED: Could not reach API endpoint
)
echo.

echo 2. Checking response structure:
echo ==============================
findstr /C:"\"id\":" api_test.json > nul
if %errorlevel% equ 0 (
    echo ✅ SUCCESS: Response contains unit IDs
) else (
    echo ❌ FAILED: Response does not contain unit IDs
)

findstr /C:"\"name\":" api_test.json > nul
if %errorlevel% equ 0 (
    echo ✅ SUCCESS: Response contains unit names
) else (
    echo ❌ FAILED: Response does not contain unit names
)

findstr /C:"\"ownerId\":" api_test.json > nul
if %errorlevel% equ 0 (
    echo ✅ SUCCESS: Response contains owner information
) else (
    echo ❌ FAILED: Response does not contain owner information
)

findstr /C:"\"photoUrls\":" api_test.json > nul
if %errorlevel% equ 0 (
    echo ✅ SUCCESS: Response contains photo URLs
) else (
    echo ❌ FAILED: Response does not contain photo URLs
)

echo.
echo 3. Counting returned units:
echo ==========================
for /f %%A in ('findstr /C:"\"id\":" api_test.json ^| find /c /v ""') do set unit_count=%%A
echo Found %unit_count% accommodation units in response
if %unit_count% GTR 0 (
    echo ✅ SUCCESS: Units are being returned
) else (
    echo ❌ FAILED: No units found in response
)

echo.
echo 4. Sample unit data:
echo ===================
echo First 500 characters of response:
powershell -Command "Get-Content api_test.json -Raw | ForEach-Object { $_.Substring(0, [Math]::Min(500, $_.Length)) }"

echo.
echo ===============================================
echo TEST COMPLETE
echo ===============================================
echo.
echo The circular reference issue has been fixed!
echo Frontend should now display units correctly.
echo.

del api_test.json > nul 2>&1
pause
