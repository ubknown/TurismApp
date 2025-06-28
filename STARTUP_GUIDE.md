# 🚀 TurismApp - Diploma Project Startup Guide

## 📋 Prerequisites

1. **Java 17 or higher** installed
2. **Node.js 18 or higher** installed
3. **MySQL 8.0** running on localhost:3306
4. **Maven** (included with mvnw)

## 🗄️ Database Setup

1. Start MySQL server
2. Create database:
   ```sql
   CREATE DATABASE turismdb;
   ```
3. The application will auto-create tables on first run

## ⚙️ Configuration

1. Copy environment template:
   ```bash
   copy .env.example .env
   ```
2. Edit `.env` with your database credentials
3. For diploma presentation, default values should work

## 🟦 Backend Startup

1. Open terminal in project root
2. Start the backend:
   ```bash
   mvnw.cmd spring-boot:run
   ```
3. Wait for "Started TurismappApplication"
4. Backend runs on: http://localhost:8080

### Backend Health Check:
- http://localhost:8080/api/units/debug/health
- Should return: `{"status": "healthy"}`

## 🟩 Frontend Startup

1. Open NEW terminal window
2. Navigate to frontend:
   ```bash
   cd "New front"
   ```
3. Install dependencies (first time only):
   ```bash
   npm install
   ```
4. Start development server:
   ```bash
   npm run dev
   ```
5. Frontend runs on: http://localhost:5173

## 🧪 Testing the Application

### 1. Database Seeding
- On first startup, realistic test data is automatically created
- 15 users (10 guests + 5 owners)
- 25+ accommodation units with Romanian names
- Sample reservations and reviews

### 2. Test User Accounts
The seeder creates accounts you can use for testing:
- **Owner**: `ana.popescu@gmail.com` / `password123`
- **Guest**: `ion.marin@yahoo.com` / `password123`

### 3. Core Features to Test

#### Authentication Flow:
1. Register new account → Check email confirmation message
2. Login with test accounts
3. Role-based navigation (Guest vs Owner)

#### Guest Features:
1. Browse accommodation units
2. Filter by location, price, capacity
3. Filter by availability dates
4. Create bookings
5. Leave reviews

#### Owner Features:
1. List new properties
2. Manage existing units
3. View profit analytics dashboard
4. Monitor reservations

## 🔍 Debug Endpoints

- **Health**: http://localhost:8080/api/units/debug/health
- **Database Count**: http://localhost:8080/api/units/debug/count
- **All Units**: http://localhost:8080/api/units/public

## 🚨 Common Issues & Solutions

### Backend Won't Start:
1. Check MySQL is running: `net start mysql80`
2. Check port 8080 not in use: `netstat -ano | findstr :8080`
3. Verify Java 17+: `java -version`

### Frontend Won't Start:
1. Clear npm cache: `npm cache clean --force`
2. Delete node_modules and reinstall: `rmdir /s node_modules && npm install`
3. Check port 5173 available: `netstat -ano | findstr :5173`

### Database Connection Issues:
1. Verify MySQL credentials in application.properties
2. Check database exists: `SHOW DATABASES;`
3. Ensure MySQL allows local connections

### Email Not Working:
- Email confirmation is configured but may require actual SMTP setup
- For diploma presentation, you can bypass email verification

## 📝 Key Features for Diploma Presentation

1. **Authentication System**: Registration, login, email confirmation
2. **Role-Based Access**: Different interfaces for guests and owners
3. **Property Management**: CRUD operations for accommodation units
4. **Booking System**: Date-based availability and reservations
5. **Analytics Dashboard**: Profit charts and business insights
6. **Search & Filtering**: Location, price, capacity, date filters
7. **Data Validation**: Input sanitization and error handling
8. **Responsive Design**: Works on desktop and mobile

## 🔧 Known Issues for Future Improvement

- TODO: Stronger password policies need implementation
- TODO: Email service needs production SMTP configuration
- TODO: Add proper logging and monitoring
- TODO: Implement rate limiting for API endpoints
- TODO: Add comprehensive unit tests
- TODO: Optimize database queries for better performance

## 📞 Support

If you encounter issues during diploma presentation:
1. Check both terminals for error messages
2. Verify all services are running
3. Use debug endpoints to check system status
4. Restart services if needed

Good luck with your diploma presentation! 🎓
