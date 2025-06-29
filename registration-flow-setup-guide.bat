@echo off
echo ===============================================
echo REGISTRATION FLOW - SETUP AND TEST GUIDE
echo ===============================================
echo.

echo STEP 1: Start Backend and Frontend
echo ==================================
echo.
echo 1. Start Backend:
echo    cd "d:\razu\Licenta\SCD\TurismApp"
echo    start-backend.bat
echo.
echo 2. Start Frontend:
echo    cd "d:\razu\Licenta\SCD\TurismApp\New front"
echo    npm run dev
echo.

echo STEP 2: Add Missing Routes to Frontend
echo =======================================
echo.
echo Add these routes to your main routing file (App.jsx):
echo.
echo import EmailConfirmationPage from './pages/EmailConfirmationPage';
echo import ResendConfirmationPage from './pages/ResendConfirmationPage';
echo.
echo ^<Route path="/email-confirmed" element={^<EmailConfirmationPage /^>} /^>
echo ^<Route path="/resend-confirmation" element={^<ResendConfirmationPage /^>} /^>
echo.

echo STEP 3: Test Registration Flow
echo ===============================
echo.
echo Manual Testing:
echo 1. Open: http://localhost:5173/register
echo 2. Fill registration form:
echo    - First Name: Test
echo    - Last Name: User  
echo    - Email: test@example.com
echo    - Password: TestPass123
echo    - Role: GUEST or OWNER
echo 3. Submit form
echo 4. Verify success message appears
echo 5. Check redirect to login page
echo 6. Verify success banner shows on login page
echo.

echo Automated Testing:
echo Run: test-registration-flow.bat
echo.

echo STEP 4: Database Verification
echo =============================
echo.
echo Check created users:
echo    mysql -u root -p turismdb
echo    SELECT id, first_name, last_name, email, role, enabled, owner_status 
echo    FROM users WHERE email = 'test@example.com';
echo.
echo Check owner applications (if OWNER role selected):
echo    SELECT oa.id, u.email, oa.status, oa.submitted_at 
echo    FROM owner_applications oa 
echo    JOIN users u ON oa.user_id = u.id 
echo    WHERE u.email = 'test@example.com';
echo.

echo STEP 5: Email Confirmation Testing
echo ===================================
echo.
echo 1. Check email service configuration
echo 2. Verify confirmation email is sent
echo 3. Click confirmation link in email
echo 4. Verify redirect to: /email-confirmed?success=true
echo 5. Check user enabled in database
echo 6. Test login with confirmed account
echo.

echo ===============================================
echo REGISTRATION FLOW COMPONENTS STATUS
echo ===============================================
echo.
echo ✅ Frontend Registration Form (RegisterPage.jsx)
echo ✅ Backend Registration Endpoint (/api/auth/register)
echo ✅ Success Banner Component (SuccessBanner.jsx)
echo ✅ Email Confirmation Endpoint (/api/auth/confirm)
echo ✅ Resend Confirmation Endpoint (/api/auth/resend-confirmation)
echo ✅ Email Confirmation Page (EmailConfirmationPage.jsx) - CREATED
echo ✅ Resend Confirmation Page (ResendConfirmationPage.jsx) - CREATED
echo ✅ Owner Application Auto-Creation
echo ✅ Database User Management
echo ✅ Toast Notifications
echo ✅ Form Validation
echo ✅ Error Handling
echo.

echo ⚠️  MANUAL TASKS REQUIRED:
echo 1. Add new routes to frontend routing
echo 2. Configure email service (SMTP)
echo 3. Start backend and frontend servers
echo.

echo ===============================================
echo EXPECTED REGISTRATION FLOW:
echo ===============================================
echo.
echo 1. User fills registration form → POST /api/auth/register
echo 2. Backend creates user (disabled) → HTTP 201 response
echo 3. Confirmation email sent to user
echo 4. Frontend shows success message → Redirect to login
echo 5. Login page shows success banner with instructions
echo 6. User clicks email confirmation link → GET /api/auth/confirm
echo 7. Backend enables user account → Redirect to /email-confirmed
echo 8. User sees confirmation success page
echo 9. User can now log in with activated account
echo.

echo For OWNER registration: Owner application automatically created
echo.

pause
