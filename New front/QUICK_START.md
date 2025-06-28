# 🚀 Quick Start Guide

## Complete Tourism Management App with File Upload & Reviews

### 🏃‍♂️ Instant Setup (5 minutes)

#### 1. Start Backend
```bash
cd "c:\Users\razvi\Desktop\SCD\TurismApp"
./mvnw spring-boot:run
```
*Backend will run on http://localhost:8080*

#### 2. Start Frontend
```bash
cd "c:\Users\razvi\Desktop\SCD\TurismApp\New front"
npm run dev
```
*Frontend will run on http://localhost:5173*

### 🎯 Quick Feature Demo

#### Test File Upload (2 minutes):
1. **Register as Owner**: http://localhost:5173/register
   - Choose "Owner" role
   - Fill out form and register
2. **Add Unit**: Go to "My Units" → "Add New Unit"
3. **Upload Images**: Click "Images" button → Drag & drop photos
4. **View Result**: Images appear in gallery and unit cards

#### Test Review System (2 minutes):
1. **Register as Guest**: http://localhost:5173/register
   - Choose "Guest" role
2. **Make Booking**: Browse units → Book accommodation
3. **Submit Review**: Go to "Bookings" → "Write Review" (after checkout date)
4. **View Reviews**: Check unit details page for reviews

### 📱 Key Features Ready to Test

#### ✅ **Image Management**
- **Upload**: Drag-and-drop multiple images
- **Gallery**: Navigate through unit photos
- **Delete**: Remove unwanted images
- **Display**: Images show on unit cards and details

#### ✅ **Review System**
- **Star Ratings**: 1-5 star interactive rating
- **Comments**: Text feedback from guests
- **Eligibility**: Only past guests can review
- **Display**: Public reviews with average ratings

#### ✅ **Complete User Flows**
- **Owner**: Add units → Upload images → View bookings → Monitor reviews
- **Guest**: Browse units → View images/reviews → Book → Leave reviews
- **Admin**: Full access to all features

### 🔧 Troubleshooting

#### Backend Issues:
```bash
# If port 8080 is busy
./mvnw spring-boot:run --server.port=8081
```

#### Frontend Issues:
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
npm run dev
```

#### File Upload Issues:
- Check `uploads/units/` directory is created
- Verify file size < 10MB
- Ensure user is logged in as unit owner

### 🎨 UI Highlights

- **Glassmorphism Design**: Modern transparent effects
- **Starry Background**: Immersive space theme
- **Responsive Layout**: Works on all devices
- **Smooth Animations**: Professional interactions

### 🔐 Authentication

#### Test Users:
**Owner:**
- Email: owner@test.com
- Password: test123
- Can: Add units, upload images, view bookings

**Guest:**
- Email: guest@test.com  
- Password: test123
- Can: Book units, write reviews

### 📊 Dashboard Features

**Owner Dashboard:**
- Total units owned
- Monthly revenue
- Recent bookings
- Unit performance stats

**Guest Dashboard:**
- Booking history
- Upcoming stays
- Past reviews
- Favorite units

### 🛠️ Tech Stack

**Backend:**
- Spring Boot 3.x
- MySQL Database
- JWT Authentication
- File Upload Support
- RESTful APIs

**Frontend:**
- React 18 + Vite
- TailwindCSS
- Glassmorphism UI
- React Router v6
- Axios HTTP Client

### 🚀 Ready for Production

**All implemented:**
✅ User registration & authentication
✅ Role-based access control
✅ Accommodation unit management
✅ Image upload & gallery
✅ Booking system
✅ Review & rating system
✅ Real-time dashboard
✅ Responsive design
✅ Error handling
✅ Security validations

### 📞 Need Help?

Check the detailed testing guide: `FINAL_TESTING_GUIDE.md`
Review implementation details: `IMPLEMENTATION_COMPLETE.md`

**Happy testing! 🎉**
