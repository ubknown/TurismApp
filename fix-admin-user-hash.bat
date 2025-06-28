@echo off
echo ========================================
echo ADMIN PASSWORD HASH FIX
echo ========================================
echo.
echo This will fix the admin user password hash in the database
echo.

echo Connecting to MySQL and fixing admin user...
echo.

mysql -u root -p -e "USE turismdb; DELETE FROM users WHERE email = 'admin@tourism.com'; INSERT INTO users (first_name, last_name, email, password, enabled, role, created_at) VALUES ('Admin', 'User', 'admin@tourism.com', '$2a$10$GTUA08wkyZ6qkjN/vdK8buT7ZtSV9tDUrd0RTjTUjgB9o2XmJQWs.', TRUE, 'ADMIN', NOW()); SELECT 'Admin user fixed!' as status, id, email, enabled, role FROM users WHERE email = 'admin@tourism.com';"

echo.
echo ✅ Admin user should now be fixed!
echo.
echo Now test the login with:
echo   Email: admin@tourism.com
echo   Password: admin123
echo.
pause
