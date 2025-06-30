# PowerShell script for testing Spring Security and Date Filtering
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "DEBUG: Spring Security and Date Filtering Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set variables for testing
$BACKEND_URL = "http://localhost:8080"
$CHECKIN_DATE = "2025-07-01"
$CHECKOUT_DATE = "2025-07-10"

Write-Host "Testing Spring Security configuration and date filtering..." -ForegroundColor Yellow
Write-Host "Backend URL: $BACKEND_URL" -ForegroundColor Green
Write-Host "Check-in: $CHECKIN_DATE" -ForegroundColor Green
Write-Host "Check-out: $CHECKOUT_DATE" -ForegroundColor Green
Write-Host ""

Write-Host "1. Testing backend health (should return 200)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BACKEND_URL/api/units/debug/health" -Method GET -UseBasicParsing
    Write-Host "Status: $($response.StatusCode) $($response.StatusDescription)" -ForegroundColor Green
    Write-Host "Content: $($response.Content)" -ForegroundColor White
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}
Write-Host ""

Write-Host "2. Testing public units endpoint WITHOUT authentication (should NOT return 403)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BACKEND_URL/api/units/public" -Method GET -UseBasicParsing
    Write-Host "Status: $($response.StatusCode) $($response.StatusDescription)" -ForegroundColor Green
    Write-Host "Content Length: $($response.Content.Length) characters" -ForegroundColor White
    if ($response.Content.Length -lt 200) {
        Write-Host "Response: $($response.Content)" -ForegroundColor White
    } else {
        Write-Host "Response: [Large response - $($response.Content.Length) chars]" -ForegroundColor White
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}
Write-Host ""

Write-Host "3. Testing public units endpoint WITH date filter (should NOT return 403)..." -ForegroundColor Yellow
try {
    $url = "$BACKEND_URL/api/units/public?checkIn=$CHECKIN_DATE&checkOut=$CHECKOUT_DATE"
    Write-Host "URL: $url" -ForegroundColor Gray
    $response = Invoke-WebRequest -Uri $url -Method GET -UseBasicParsing
    Write-Host "Status: $($response.StatusCode) $($response.StatusDescription)" -ForegroundColor Green
    Write-Host "Content Length: $($response.Content.Length) characters" -ForegroundColor White
    if ($response.Content.Length -lt 200) {
        Write-Host "Response: $($response.Content)" -ForegroundColor White
    } else {
        Write-Host "Response: [Large response - $($response.Content.Length) chars]" -ForegroundColor White
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}
Write-Host ""

Write-Host "4. Testing debug date filter endpoint..." -ForegroundColor Yellow
try {
    $url = "$BACKEND_URL/api/units/debug/date-filter?checkIn=$CHECKIN_DATE&checkOut=$CHECKOUT_DATE"
    Write-Host "URL: $url" -ForegroundColor Gray
    $response = Invoke-WebRequest -Uri $url -Method GET -UseBasicParsing
    Write-Host "Status: $($response.StatusCode) $($response.StatusDescription)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor White
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}
Write-Host ""

Write-Host "5. Testing CORS preflight (OPTIONS request)..." -ForegroundColor Yellow
try {
    $headers = @{
        "Origin" = "http://localhost:5173"
        "Access-Control-Request-Method" = "GET"
    }
    $response = Invoke-WebRequest -Uri "$BACKEND_URL/api/units/public" -Method OPTIONS -Headers $headers -UseBasicParsing
    Write-Host "Status: $($response.StatusCode) $($response.StatusDescription)" -ForegroundColor Green
    Write-Host "CORS Headers:" -ForegroundColor White
    foreach ($header in $response.Headers.GetEnumerator()) {
        if ($header.Key -like "*Access-Control*") {
            Write-Host "  $($header.Key): $($header.Value)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Security Status Check:" -ForegroundColor Yellow
Write-Host "- 200 OK = Success" -ForegroundColor Green
Write-Host "- 403 Forbidden = Spring Security blocking (PROBLEM)" -ForegroundColor Red
Write-Host "- 401 Unauthorized = Missing authentication" -ForegroundColor Yellow
Write-Host "- 404 Not Found = Endpoint doesn't exist" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Check the Spring Boot console for these messages:" -ForegroundColor Yellow
Write-Host "- 🔐 JWT Filter - Skipping public endpoint" -ForegroundColor Green
Write-Host "- 🗓️ Date filtering requested" -ForegroundColor Green
Write-Host "- 🔍 Checking availability for unit" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

Read-Host "Press Enter to exit"
