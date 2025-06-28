# 🧪 End-to-End Testing Guide - Tourism Management App

## 🚀 Setup Instructions

1. **Start the application:**
   ```bash
   npm run dev
   ```
   
2. **Ensure backend is running on:**
   ```
   http://localhost:8080
   ```

3. **Open application in browser:**
   ```
   http://localhost:5173
   ```

---

## 👤 **Test Flow 1: GUEST USER**

### 📝 **1.1 Guest Registration**

**Steps:**
1. Navigate to `/register`
2. Fill out registration form:
   - **Full Name:** Test Guest User
   - **Email:** testguest@example.com  
   - **Phone:** +1234567890
   - **Password:** testguest123
   - **Role:** Select "Guest" (should be default)
3. Click "Create Account"

**✅ Expected Results:**
- Success toast: "Registration successful as Guest!"
- Redirect to login page after 2 seconds
- Form validation works for empty/invalid fields

**🔍 What to Check:**
- Form validates all required fields
- Email format validation works
- Password strength requirements
- Role selection is properly set to GUEST
- Backend receives correct data format

---

### 🔐 **1.2 Guest Login**

**Steps:**
1. On login page, enter:
   - **Email:** testguest@example.com
   - **Password:** testguest123
2. Click "Sign In"

**✅ Expected Results:**
- Success toast: "Welcome back!"
- Redirect to home page
- JWT token stored in localStorage
- User data stored in localStorage
- Navigation bar shows guest-appropriate links

**🔍 What to Check:**
- Token is properly stored and attached to API requests
- AuthContext state updates correctly
- Navigation adapts to show guest options
- Protected routes become accessible

---

### 🏠 **1.3 Browse Units**

**Steps:**
1. Click "Explore Units" or navigate to `/units`
2. Browse available accommodation units
3. Check that only ACTIVE units are shown
4. Verify unit information displays correctly

**✅ Expected Results:**
- Units list loads successfully
- Only active/available units shown
- Unit cards show: images, name, location, price, capacity, rating
- "View Details" buttons work

**🔍 What to Check:**
- API call to `/api/units/public` works
- No authentication required for browsing
- Data renders correctly in glassmorphism cards
- Responsive design on mobile

---

### 🔍 **1.4 Filter and Search Units**

**Steps:**
1. Use search bar to search for location/name
2. Apply filters:
   - Location filter
   - Price range (min/max)
   - Guest capacity
   - Amenities (WiFi, Parking, etc.)
3. Verify results update dynamically

**✅ Expected Results:**
- Search results filter in real-time
- Multiple filters can be combined
- Clear filters option works
- No results message appears when appropriate

**🔍 What to Check:**
- Query parameters are properly built and sent
- Results match filter criteria
- Filter state persists during navigation
- Performance with multiple filters

---

### 📅 **1.5 Make a Booking**

**Steps:**
1. Click on a unit to view details (`/units/{id}`)
2. Click "Book Now"
3. Fill out booking form:
   - **Guest Name:** Test Guest
   - **Email:** testguest@example.com
   - **Phone:** +1234567890
   - **Check-in:** Tomorrow's date
   - **Check-out:** Day after tomorrow
   - **Guests:** 2
   - **Special Requests:** "Please provide late check-in"
4. Review total price calculation
5. Submit booking

**✅ Expected Results:**
- Unit details page loads correctly
- Booking form validates all fields
- Price calculates automatically based on dates
- Success message: "Booking Request Sent"
- Form closes after successful submission

**🔍 What to Check:**
- Unit details API call works
- Date picker prevents past dates
- Price calculation is accurate (days × unit price)
- Form validation prevents invalid dates (checkout before checkin)
- Booking data is properly formatted and sent

---

### 📋 **1.6 View Booking History**

**Steps:**
1. Navigate to `/bookings`
2. View list of your bookings
3. Check booking status and details
4. Verify only guest's own bookings are shown

**✅ Expected Results:**
- Booking list loads with all guest's bookings
- Each booking shows: unit name, dates, status, total price
- Different status indicators (PENDING, CONFIRMED, COMPLETED)
- "View Unit" links work correctly

**🔍 What to Check:**
- API call to `/api/bookings/my-bookings` 
- Role-based data filtering (only guest's bookings)
- Status displays are accurate
- Date formatting is correct

---

### ⭐ **1.7 Leave a Review**

**Steps:**
1. Find a COMPLETED booking in booking history
2. Click "Leave Review" button
3. Rate the accommodation (1-5 stars)
4. Write review comment
5. Submit review

**✅ Expected Results:**
- Review form only appears for completed bookings
- Star rating selector works
- Character limit on comment (if any)
- Success message after submission
- Review appears on unit details page

**🔍 What to Check:**
- Review eligibility check works (only completed bookings)
- Prevents duplicate reviews from same user
- Rating and comment are properly saved
- Review displays publicly on unit page

---

### 🚫 **1.8 Access Restriction Tests**

**Steps:**
1. Try to access owner-only pages:
   - `/dashboard` 
   - `/my-units`
2. Try to access admin routes (if any)
3. Verify proper redirects

**✅ Expected Results:**
- Redirect to `/units` page (appropriate for guests)
- Toast message about insufficient permissions
- URL changes but access is denied
- No sensitive data exposure

**🔍 What to Check:**
- RoleRoute component properly blocks access
- Redirects are role-appropriate
- No API calls to restricted endpoints succeed
- Error handling is graceful

---

## 🏢 **Test Flow 2: OWNER USER**

### 📝 **2.1 Owner Registration**

**Steps:**
1. Navigate to `/register`
2. Fill out registration form:
   - **Full Name:** Test Property Owner
   - **Email:** testowner@example.com
   - **Phone:** +1987654321
   - **Password:** testowner123
   - **Role:** Select "Property Owner"
3. Click "Create Account"

**✅ Expected Results:**
- Success toast: "Registration successful as Property Owner!"
- Redirect to login page
- Role properly set to OWNER

**🔍 What to Check:**
- Owner role is correctly assigned
- Registration data properly formatted
- Backend creates owner account

---

### 🔐 **2.2 Owner Login**

**Steps:**
1. Login with owner credentials
2. Verify owner-specific navigation appears

**✅ Expected Results:**
- Successful login
- Navigation shows: Dashboard, My Units, Bookings
- Owner role helper functions work correctly

**🔍 What to Check:**
- AuthContext recognizes owner role
- Navigation adapts for owner permissions
- Dashboard becomes accessible

---

### 🏠 **2.3 Add New Accommodation Unit**

**Steps:**
1. Navigate to `/my-units`
2. Click "Add New Unit"
3. Fill out unit creation form:
   - **Name:** Luxury Mountain Cabin
   - **Description:** Beautiful cabin with mountain views
   - **Location:** Aspen, Colorado
   - **Price per night:** $250
   - **Capacity:** 4 guests
   - **Amenities:** WiFi, Parking, Kitchen
   - **Contact Info:** +1987654321
4. Submit form

**✅ Expected Results:**
- Form validates all required fields
- Success message after creation
- New unit appears in "My Units" list
- Unit is created with ACTIVE status

**🔍 What to Check:**
- API call to create unit endpoint
- Owner ID is properly associated
- Form validation works correctly
- Data persistence in database

---

### 📸 **2.4 Upload Multiple Images**

**Steps:**
1. In "My Units", find the newly created unit
2. Click "Upload Images" or edit unit
3. Select multiple image files (3-5 images)
4. Upload images using drag-and-drop or file picker
5. Verify images are displayed in unit gallery

**✅ Expected Results:**
- Multiple file selection works
- Upload progress indicator shows
- Success message for each upload
- Images display in unit gallery
- Images are linked to correct unit

**🔍 What to Check:**
- FormData properly constructed
- File size/type validation (if implemented)
- Images stored with correct unit association
- Error handling for upload failures

---

### 🏢 **2.5 View and Manage Own Units**

**Steps:**
1. View complete list in "My Units"
2. Edit unit details
3. Change unit status (Active/Inactive)
4. Delete a test unit
5. Verify only owner's units are shown

**✅ Expected Results:**
- Only owner's units displayed
- Edit functionality works
- Status toggle works
- Delete confirmation dialog
- Real-time updates after changes

**🔍 What to Check:**
- API returns only owner's units (`/api/units/my-units`)
- CRUD operations work correctly
- Optimistic UI updates
- Proper error handling

---

### 📅 **2.6 View Unit Bookings**

**Steps:**
1. Navigate to `/bookings`
2. View bookings made for owner's units
3. Check booking details and guest information
4. Verify status management options

**✅ Expected Results:**
- Shows bookings for owner's units only
- Guest contact information visible
- Booking status and dates accurate
- Revenue information displayed

**🔍 What to Check:**
- API endpoint `/api/bookings/owner`
- Data filtered by unit ownership
- Sensitive guest data handled appropriately
- Financial calculations are correct

---

### 📊 **2.7 View Profit Analytics**

**Steps:**
1. Navigate to `/dashboard`
2. View profit chart with different time ranges:
   - 3 months
   - 6 months  
   - 12 months
   - 24 months
3. Check AI profit prediction
4. Verify dashboard statistics

**✅ Expected Results:**
- Chart loads with real data from owner's units
- Time range filtering works
- AI prediction displays future months
- Statistics show accurate totals

**🔍 What to Check:**
- API calls to profit endpoints with owner filtering
- Chart.js integration working
- Prediction algorithm results
- Data visualization accuracy

---

### 🚫 **2.8 Access Restriction Tests**

**Steps:**
1. Try to access guest-specific views
2. Attempt to view other owners' units
3. Try to access admin-only routes

**✅ Expected Results:**
- Appropriate redirects for unauthorized access
- No data leakage from other owners
- Security boundaries maintained

**🔍 What to Check:**
- Role-based access control working
- Data isolation between owners
- No unauthorized API access

---

## 👑 **Test Flow 3: ADMIN USER** (If Implemented)

### 🔐 **3.1 Admin Access**

**Steps:**
1. Login with admin credentials (if available)
2. Check admin-specific navigation
3. Verify global access permissions

**✅ Expected Results:**
- Admin dashboard accessible
- Global user/unit management available
- Override permissions work

**🔍 What to Check:**
- Admin role properly recognized
- Global data access works
- Admin-only endpoints accessible

---

## 🔒 **Security and Edge Case Testing**

### 🔧 **Cross-Role Security Tests**

**Steps:**
1. **Token Manipulation:**
   - Manually expire token in localStorage
   - Verify auto-logout works
   - Check API calls with invalid token

2. **Direct URL Access:**
   - Try accessing protected routes while logged out
   - Test role-based route restrictions
   - Verify proper redirects

3. **API Security:**
   - Check that guests can't access owner endpoints
   - Verify owners can't see other owners' data
   - Test with modified request headers

**✅ Expected Results:**
- 401 errors trigger auto-logout
- Protected routes redirect appropriately
- Cross-role data access is blocked
- Security boundaries are maintained

---

## 📱 **User Experience Testing**

### 🔄 **Error Handling**

**Steps:**
1. Test with network disconnected
2. Submit forms with invalid data
3. Try operations with expired sessions
4. Test with very large file uploads

**✅ Expected Results:**
- Graceful error messages
- Loading states during operations
- Form validation prevents submission
- Network errors handled appropriately

### 📱 **Responsive Design**

**Steps:**
1. Test on mobile device/small screen
2. Verify glassmorphism design works
3. Check navigation on mobile
4. Test form usability on touch devices

**✅ Expected Results:**
- All features work on mobile
- Design remains visually appealing
- Touch interactions work smoothly
- Text remains readable

---

## ✅ **Testing Completion Checklist**

### 🎯 **Core Functionality**
- [ ] Guest registration and login
- [ ] Owner registration and login  
- [ ] Unit browsing and filtering
- [ ] Booking creation and management
- [ ] Review system functionality
- [ ] Image upload system
- [ ] Profit analytics and dashboard
- [ ] Role-based access control

### 🔒 **Security**
- [ ] Authentication and authorization
- [ ] JWT token handling
- [ ] Cross-role access prevention
- [ ] Data isolation between users
- [ ] API endpoint protection

### 🎨 **User Experience**
- [ ] Navigation and routing
- [ ] Form validation and feedback
- [ ] Error handling and messages
- [ ] Loading states and indicators
- [ ] Responsive design
- [ ] Glassmorphism styling

### 🔧 **Technical**
- [ ] API integration and data flow
- [ ] Local storage management
- [ ] Real-time data updates
- [ ] File upload functionality
- [ ] Chart and analytics display

---

## 🚨 **Common Issues to Watch For**

1. **Authentication Issues:**
   - Token not persisting after page refresh
   - Auto-logout not working on 401 errors
   - Role detection failing

2. **Data Issues:**
   - Cross-user data leakage
   - Incorrect API endpoint calls
   - Form data not properly validated

3. **UI/UX Issues:**
   - Loading states missing
   - Error messages not clear
   - Mobile responsiveness problems
   - Navigation not updating based on role

4. **Security Issues:**
   - Unauthorized route access
   - API calls without proper authentication
   - Client-side validation bypass

---

**🎉 After completing all tests, your tourism management application should be fully validated and ready for production deployment!**
