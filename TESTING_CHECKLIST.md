# 🧪 Frontend-Backend Integration Testing Checklist

## Prerequisites
- [ ] Backend running on http://localhost:8080
- [ ] Frontend running on http://localhost:5173
- [ ] Database is set up and accessible

## 🔐 Authentication Testing

### Registration Flow
- [ ] Navigate to http://localhost:5173/register
- [ ] Fill in all fields:
  - First Name: "John"
  - Last Name: "Doe"  
  - Email: "john.doe@test.com"
  - Password: "password123"
  - Phone: "+1234567890"
  - Address: "123 Test Street"
- [ ] Click "Register"
- [ ] ✅ **Expected**: Success toast + redirect to login page
- [ ] ❌ **If error**: Check console for API errors

### Login Flow
- [ ] Navigate to http://localhost:5173/login
- [ ] Enter credentials:
  - Email: "john.doe@test.com"
  - Password: "password123"
- [ ] Click "Login"
- [ ] ✅ **Expected**: Success toast + redirect to dashboard
- [ ] ❌ **If error**: Check network tab for 401/400 errors

## 📊 Dashboard Testing

### Dashboard Load
- [ ] Navigate to http://localhost:5173/dashboard
- [ ] Check if user is logged in (should see user info in navbar)
- [ ] ✅ **Expected**: Dashboard loads with statistics cards
- [ ] ✅ **Expected**: Recent bookings section (may be empty)
- [ ] ✅ **Expected**: Profit chart component
- [ ] ❌ **If error**: Check console for API call failures

### Dashboard API Calls
Open browser DevTools → Network tab:
- [ ] Verify API call to `/api/profit/stats` (200 status)
- [ ] Verify API call to `/api/bookings/recent` (200 status)
- [ ] Check if JWT token is sent in Authorization header

## 🏠 Units Management Testing

### My Units Page
- [ ] Navigate to http://localhost:5173/my-units
- [ ] ✅ **Expected**: "My Units" page loads
- [ ] ✅ **Expected**: Shows empty state OR user's units
- [ ] ✅ **Expected**: "Add New Unit" button visible
- [ ] Check Network tab for `/api/units/my-units` call

### Units List Page (Public)
- [ ] Navigate to http://localhost:5173/units
- [ ] ✅ **Expected**: Public units list loads
- [ ] ✅ **Expected**: Search box and filters work
- [ ] Test search functionality:
  - Type "mountain" in search
  - Apply location filter
  - Apply price range filter
- [ ] Check Network tab for `/api/units/public` calls

## 🔍 Error Handling Testing

### 401 Error Handling
- [ ] Clear localStorage: `localStorage.clear()`
- [ ] Try to access http://localhost:5173/dashboard
- [ ] ✅ **Expected**: Auto-redirect to login page
- [ ] ✅ **Expected**: "Session expired" toast message

### Network Error Handling
- [ ] Stop the backend server
- [ ] Try to login from frontend
- [ ] ✅ **Expected**: Network error toast
- [ ] ✅ **Expected**: No app crash

## 📱 UI/UX Testing

### Navigation
- [ ] Test all nav links work
- [ ] Test logout functionality
- [ ] Test responsive design (resize browser)

### Loading States
- [ ] Check loading spinners appear during API calls
- [ ] Check loading states don't persist forever

### Toast Messages
- [ ] Success toasts for successful actions
- [ ] Error toasts for failed actions
- [ ] Auto-logout toast for 401 errors

## 🐛 Debugging Guide

### Common Issues & Solutions

**❌ CORS Errors**
```
Access to XMLHttpRequest at 'http://localhost:8080/api/...' from origin 'http://localhost:5173' has been blocked by CORS policy
```
**Solution**: Verify `@CrossOrigin(origins = "http://localhost:5173")` in backend controllers

**❌ 401 Unauthorized**
```
Request failed with status code 401
```
**Solution**: Check JWT token in localStorage and verify backend JWT configuration

**❌ Connection Refused**
```
Network Error
```
**Solution**: Ensure backend is running on port 8080

**❌ Empty Responses**
```
API returns empty arrays or null
```
**Solution**: Add sample data to database or check database connection

## 📋 Sample Data Setup

If you need sample data for testing, run these SQL commands:

```sql
-- Insert sample accommodation unit
INSERT INTO accommodation_units (name, description, location, price_per_night, capacity, available, created_at, rating, review_count, total_bookings, monthly_revenue, status, owner_id) 
VALUES ('Cozy Mountain Cabin', 'Beautiful cabin with mountain views', 'Brasov, Romania', 120.00, 6, true, CURDATE(), 4.8, 24, 47, 5640.00, 'active', 1);

-- Insert sample booking
INSERT INTO bookings (accommodation_unit_id, check_in_date, check_out_date, guest_name, guest_email) 
VALUES (1, '2025-07-01', '2025-07-05', 'Jane Smith', 'jane@example.com');
```

## ✅ Success Criteria

Your integration is working correctly when:
- ✅ Registration and login flow complete without errors
- ✅ Dashboard shows data and charts
- ✅ Units pages load and display content  
- ✅ Error handling works properly
- ✅ Navigation and logout work
- ✅ All API calls return expected status codes
- ✅ JWT authentication works across all protected routes

## 🎯 Next Steps After Testing

1. **Fix any identified issues**
2. **Add sample data for better testing**
3. **Test edge cases and error scenarios**
4. **Optimize performance if needed**
5. **Deploy to staging environment**

Happy Testing! 🚀
