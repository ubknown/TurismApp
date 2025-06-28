# Role-Based System Testing Guide

## Quick Start Testing

### 1. Test Guest Registration and Flow
```bash
# Navigate to registration
http://localhost:3000/register

# Fill form and select "Guest" role
# Complete registration
# Login with credentials
# Should redirect to: /units
```

### 2. Test Owner Registration and Flow  
```bash
# Navigate to registration
http://localhost:3000/register

# Fill form and select "Property Owner" role
# Complete registration
# Login with credentials
# Should redirect to: /dashboard
```

### 3. Test Role-Based Navigation
- **Guest user**: Should see "Browse Units", "My Bookings" in nav
- **Owner user**: Should see "Dashboard", "My Units", "Bookings" in nav
- Role indicator should show correct role in user menu

### 4. Test Route Protection
```bash
# As Guest user, try to access:
http://localhost:3000/dashboard    # Should redirect to /units
http://localhost:3000/my-units     # Should redirect to /units

# As Owner user, try to access:
http://localhost:3000/dashboard    # Should work
http://localhost:3000/my-units     # Should work
```

### 5. Test API Integration
Check browser Network tab for:
- Registration POST includes `role` field
- Login response includes user with `role` field
- JWT token is attached to subsequent requests

## Expected Behaviors

### Guest Account
✅ Can register selecting "Guest" role  
✅ Redirects to `/units` after login  
✅ Cannot access `/dashboard` or `/my-units`  
✅ Navigation shows guest-appropriate links  
✅ Bookings page shows "My Bookings"  

### Owner Account  
✅ Can register selecting "Property Owner" role  
✅ Redirects to `/dashboard` after login  
✅ Can access all owner routes  
✅ Navigation shows owner-appropriate links  
✅ Bookings page shows "Bookings Management"  

### Role Persistence
✅ Role persists after page refresh  
✅ Role persists after logout/login  
✅ Correct role displayed in navigation  

## Common Issues & Solutions

### Issue: Role not included in registration
**Solution**: Check `RegisterPage.jsx` line ~85, ensure `role: formData.role` is in registrationData

### Issue: User redirected to wrong page after login
**Solution**: Check `LoginPage.jsx` role-based redirect logic

### Issue: User can access restricted routes
**Solution**: Verify `RoleRoute.jsx` is used instead of `PrivateRoute.jsx` for role-specific routes

### Issue: Navigation doesn't change for different roles
**Solution**: Check `NavBar.jsx` uses `isOwner()`, `isGuest()` functions correctly

## Development Testing Commands

```bash
# Start frontend
cd "New front"
npm run dev

# Start backend
cd ..
./mvnw spring-boot:run

# Test registration API directly
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Test","lastName":"User","email":"test@example.com","password":"password123","role":"OWNER"}'

# Test login API directly  
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

## Browser Dev Tools Checks

### Local Storage
- Check `token` is stored after login
- Check `user` object includes `role` field

### Network Tab
- Registration request includes `role` 
- Login response includes user with `role`
- Subsequent API calls include `Authorization: Bearer <token>`

### Console
- No JavaScript errors during role checks
- AuthContext provides role helper functions

This testing guide will help you verify that the role-based system is working correctly!
