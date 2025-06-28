# Frontend Testing Guide

## 🚀 Starting the React Frontend

### Prerequisites
- Node.js and npm installed
- Spring Boot backend running on port 8080

### 1. Install Dependencies
```bash
cd "c:\Users\razvi\Desktop\SCD\TurismApp\New front"
npm install
```

### 2. Start Development Server
```bash
npm run dev
```

The frontend should start on **http://localhost:5173** (default Vite port)

## 🧪 Testing the Frontend

### Manual Testing Checklist

#### ✅ **Authentication Flow**
1. **Register Page**: Create a new user account with role "OWNER"
2. **Login Page**: Login with the created credentials
3. **JWT Token**: Verify token is stored in localStorage
4. **Protected Routes**: Ensure dashboard requires authentication

#### ✅ **Dashboard Page**
1. **Stats Cards**: Should display:
   - Total Units: 0 (initially)
   - Total Bookings: 0 (initially)
   - Total Revenue: $0 (initially)
   - Occupancy Rate: 0% (initially)
   - Average Rating: 0/5 (initially)
   - Guest Satisfaction: 0% (initially)
   - Revenue Growth: 0% (initially)

2. **API Integration**: Verify network calls to:
   - `GET /api/dashboard/stats`
   - `GET /api/dashboard/insights`
   - `GET /api/accommodation-units/my-units`
   - `GET /api/bookings/my-bookings`

#### ✅ **Create Accommodation Unit**
1. Navigate to "Add New Unit" 
2. Fill out the form with sample data
3. Submit and verify unit is created
4. Check dashboard updates with new unit count

#### ✅ **Create Booking** 
1. Create a booking for your unit
2. Include totalPrice field
3. Verify booking appears in recent bookings
4. Check dashboard stats update

#### ✅ **Profit Analytics**
1. Navigate to Analytics/Profit page
2. Check profit charts load (may be empty initially)
3. Verify API calls to `/api/profit/monthly`

### Browser Developer Tools Verification

#### Network Tab
Check these API calls are successful (200 status):
```
GET /api/dashboard/stats
GET /api/dashboard/insights
GET /api/accommodation-units/my-units
GET /api/bookings/my-bookings
GET /api/profit/monthly?months=12
```

#### Console Tab
- No JavaScript errors
- No 404 or 500 API errors
- JWT token properly included in Authorization headers

#### Application Tab (Storage)
- `localStorage.token` contains JWT token after login
- Token persists across page refreshes

## 🎨 UI/UX Verification

### Design System
- ✅ Glassmorphism cards with backdrop blur
- ✅ Starry night background
- ✅ Purple/violet color scheme
- ✅ Smooth transitions and hover effects
- ✅ Responsive design (mobile-friendly)

### Components
- ✅ Navigation bar with logout
- ✅ Dashboard stat cards with icons
- ✅ Recent bookings list
- ✅ Profit charts (if data available)
- ✅ Quick action buttons

## 🔧 Troubleshooting

### Common Issues

#### Frontend won't start
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
npm run dev
```

#### API Connection Errors
- Verify backend is running on port 8080
- Check CORS is enabled in Spring Boot
- Verify axios baseURL in `src/services/axios.js`

#### Authentication Issues
- Clear localStorage: `localStorage.clear()`
- Check JWT token format in network requests
- Verify backend authentication endpoints work

#### Missing Data in Dashboard
- Create some test accommodation units first
- Add some bookings with totalPrice
- Verify backend endpoints return correct data structure

## 🧪 Test Data Creation Flow

### 1. Complete Flow Test
1. Start both backend (port 8080) and frontend (port 5173)
2. Register as OWNER: `owner@test.com` / `password123`
3. Login and access dashboard
4. Create accommodation unit:
   ```json
   {
     "name": "Test Beach House",
     "description": "Beautiful test property",
     "city": "Miami",
     "country": "USA",
     "pricePerNight": 150,
     "type": "HOUSE",
     "maxGuests": 4
   }
   ```
5. Create booking for the unit:
   ```json
   {
     "accommodationUnitId": 1,
     "checkInDate": "2025-07-01",
     "checkOutDate": "2025-07-05", 
     "guestName": "John Doe",
     "totalPrice": 600
   }
   ```
6. Verify dashboard stats update
7. Check profit analytics page

### 2. Expected Results After Test Data
- Dashboard shows: 1 Unit, 1 Booking, $600 Revenue
- Recent bookings shows the created booking
- Profit chart may show data if monthly calculation works

## 📱 Responsive Testing

Test on different screen sizes:
- **Mobile**: 375px width (iPhone)
- **Tablet**: 768px width (iPad)
- **Desktop**: 1200px+ width

Verify:
- Stats cards stack properly on mobile
- Navigation is mobile-friendly
- Charts/graphs scale correctly
- Text remains readable

## 🔗 Integration Testing

### Frontend ↔ Backend Integration
1. **Authentication**: JWT tokens work between systems
2. **Data Flow**: Created units appear in dashboard
3. **Real-time Updates**: Dashboard reflects new bookings
4. **Error Handling**: Backend errors display in frontend
5. **CORS**: No cross-origin issues

### Success Metrics
- ✅ Complete user flow works end-to-end
- ✅ Dashboard displays real backend data
- ✅ No console errors or API failures
- ✅ UI matches glassmorphism design system
- ✅ Responsive design works on all devices

## 🚀 Production Readiness

Before production deployment:
- [ ] Update API baseURL for production backend
- [ ] Optimize build: `npm run build`
- [ ] Test production build: `npm run preview`
- [ ] Verify environment variables
- [ ] Test with production data volumes
