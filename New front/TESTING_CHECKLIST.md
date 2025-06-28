# Authentication Testing Checklist

## Pre-Testing Setup
- [ ] Backend Spring Boot application is running on `http://localhost:8080`
- [ ] Frontend development server is running (`npm run dev`)
- [ ] Backend endpoints `/api/auth/login` and `/api/auth/register` are accessible

## Authentication Flow Tests

### 1. Registration Flow ✅
- [ ] Navigate to `/register`
- [ ] Fill out the registration form with valid data
- [ ] Verify client-side validation (required fields, email format, password match)
- [ ] Submit form and verify success toast notification
- [ ] Verify redirect to login page after successful registration

### 2. Login Flow ✅
- [ ] Navigate to `/login`
- [ ] Enter invalid credentials and verify error toast
- [ ] Enter valid credentials and verify success toast
- [ ] Verify redirect to dashboard after successful login
- [ ] Check that user data is stored in localStorage

### 3. Protected Routes ✅
- [ ] Try accessing `/dashboard` without authentication (should redirect to login)
- [ ] Try accessing `/my-units` without authentication (should redirect to login)
- [ ] Login and verify access to protected routes
- [ ] Verify that the intended page is shown after login redirect

### 4. Navigation & UI ✅
- [ ] Verify NavBar shows different links when authenticated vs unauthenticated
- [ ] Test logout functionality from NavBar
- [ ] Verify logout toast notification
- [ ] Verify redirect to home page after logout

### 5. Token Management ✅
- [ ] Verify JWT token is added to API requests (check browser dev tools)
- [ ] Clear localStorage manually and verify automatic redirect to login
- [ ] Test automatic logout on 401 errors (if backend returns 401)

### 6. Form Validation ✅
- [ ] Test login form validation (empty fields, invalid email)
- [ ] Test registration form validation (password mismatch, short password)
- [ ] Verify validation error toasts are shown

### 7. Error Handling ✅
- [ ] Test with backend offline (should show error toasts)
- [ ] Test with invalid API responses
- [ ] Verify graceful error handling without app crashes

### 8. Responsive Design ✅
- [ ] Test on mobile devices (responsive design)
- [ ] Test mobile navigation menu
- [ ] Verify glassmorphism effects work on different screen sizes

## Browser Testing

### Chrome/Edge
- [ ] All authentication flows work
- [ ] Toast notifications appear correctly
- [ ] LocalStorage persistence works

### Firefox
- [ ] All authentication flows work
- [ ] Toast notifications appear correctly
- [ ] LocalStorage persistence works

### Safari (if available)
- [ ] All authentication flows work
- [ ] Toast notifications appear correctly
- [ ] LocalStorage persistence works

## Performance & UX Testing

### User Experience
- [ ] Loading states are shown during API calls
- [ ] Smooth transitions and animations
- [ ] Clear error messages
- [ ] Intuitive navigation flow

### Performance
- [ ] Fast initial load
- [ ] Quick route transitions
- [ ] Efficient re-renders
- [ ] No memory leaks

## Security Testing

### Client-Side Security
- [ ] JWT tokens are properly stored
- [ ] Sensitive data is not exposed in console
- [ ] Automatic cleanup on logout
- [ ] Proper route protection

### Network Security
- [ ] HTTPS in production (when deployed)
- [ ] Proper CORS configuration
- [ ] JWT tokens in Authorization headers
- [ ] No sensitive data in URLs

## Production Readiness

### Code Quality
- [ ] No console errors
- [ ] Clean code structure
- [ ] Proper error boundaries
- [ ] Consistent styling

### Configuration
- [ ] Environment variables for API URLs
- [ ] Proper build configuration
- [ ] Optimized bundle size
- [ ] Service worker (if needed)

---

## Quick Test Commands

```bash
# Start backend (Spring Boot)
./mvnw spring-boot:run

# Start frontend (React + Vite)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Common Test Scenarios

### Happy Path
1. Register → Login → Access Dashboard → Logout
2. Login → Access My Units → Logout
3. Direct URL access → Redirect to Login → Login → Access Intended Page

### Error Scenarios
1. Invalid credentials → Error toast → Retry with valid credentials
2. Network error → Error toast → Retry when network is restored
3. Session expiry → Auto logout → Login again

### Edge Cases
1. Refresh page while authenticated → Stay authenticated
2. Multiple tabs → Consistent auth state
3. Browser back/forward → Proper navigation
4. Bookmark protected page → Redirect to login → Access after login

---

## Notes
- Take screenshots of any issues found
- Note browser/device combinations where issues occur
- Document any performance bottlenecks
- Record any UX friction points for future improvement
