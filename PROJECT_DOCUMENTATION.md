# 🏨 Tourism Management Application - Complete Documentation

## 📋 **Application Overview**

The **Tourism Management Application** is a comprehensive web-based platform designed for managing accommodation units (hotels, cabins, apartments) with separate interfaces for property owners and guests. The application enables property owners to manage their accommodation units, track bookings, analyze profits, and provides guests with the ability to search, filter, and book accommodations.

### **Core Purpose**
- **Property Management**: Owners can list, manage, and monitor their accommodation units
- **Booking System**: Guests can search, filter, and book accommodations  
- **Analytics Dashboard**: Comprehensive profit tracking and business insights
- **User Management**: Secure authentication with role-based access control

---

## 🛠️ **Technology Stack**

### **Frontend** 
- **Framework**: React 19.1.0 with Vite as build tool
- **Routing**: React Router DOM v7.6.2 for SPA navigation
- **Styling**: TailwindCSS 3.4.0 with custom glassmorphism design
- **HTTP Client**: Axios 1.10.0 for API communication
- **Icons**: Lucide React 0.523.0 for modern iconography
- **State Management**: React Context API for authentication and global state

### **Backend**
- **Framework**: Spring Boot 3.5.0 (Java 17)
- **Security**: Spring Security with JWT authentication
- **Database**: MySQL with Spring Data JPA/Hibernate
- **Architecture**: RESTful API with MVC pattern
- **Dependencies**:
  - `spring-boot-starter-web` - REST API development
  - `spring-boot-starter-data-jpa` - Database persistence
  - `spring-boot-starter-security` - Authentication & authorization
  - `jjwt 0.9.1` - JWT token handling
  - `mysql-connector-j` - MySQL database connection
  - `spring-boot-starter-validation` - Input validation

### **Database**
- **Primary Database**: MySQL 8.0+
- **ORM**: Hibernate/JPA with entity relationships
- **Schema Management**: Automatic table creation and updates

---

## 📁 **Project Structure**

### **Frontend Structure** (`/New front/src/`)
```
src/
├── components/          # Reusable UI components
│   ├── GlassCard.jsx           # Glassmorphism card component
│   ├── PrimaryButton.jsx       # Styled button component
│   ├── InputField.jsx          # Form input component
│   ├── LoadingSpinner.jsx      # Loading state component
│   ├── NavBar.jsx              # Navigation component
│   ├── PrivateRoute.jsx        # Route protection component
│   ├── ProfitChart.jsx         # Data visualization component
│   └── BookingForm.jsx         # Booking creation form
├── context/             # React Context providers
│   ├── AuthContext.jsx         # Authentication state management
│   └── ToastContext.jsx        # Global notification system
├── layouts/             # Page layout components
│   └── Layout.jsx              # Main application layout
├── pages/               # Main application pages
│   ├── HomePage.jsx            # Landing page
│   ├── LoginPage.jsx           # User authentication
│   ├── RegisterPage.jsx        # User registration
│   ├── DashboardPage.jsx       # Owner dashboard with analytics
│   ├── MyUnitsPage.jsx         # Owner's unit management
│   ├── UnitsListPage.jsx       # Public unit browsing
│   └── NotFoundPage.jsx        # 404 error page
├── router/              # Application routing
│   └── AppRouter.jsx           # Route configuration
├── services/            # External service integration
│   └── axios.js                # API client configuration
└── utils/               # Utility functions
    └── globalToast.js          # Toast notification helpers
```

### **Backend Structure** (`/src/main/java/com/licentarazu/turismapp/`)
```
src/main/java/com/licentarazu/turismapp/
├── controller/          # REST API endpoints
│   ├── AuthController.java         # Authentication endpoints
│   ├── AccommodationUnitController.java # Unit management
│   ├── BookingController.java       # Booking operations
│   ├── ProfitController.java        # Analytics endpoints
│   └── UserController.java          # User management
├── model/               # JPA entities
│   ├── User.java                    # User entity with roles
│   ├── AccommodationUnit.java       # Property entity
│   ├── Booking.java                 # Booking entity
│   ├── Role.java                    # User role enumeration
│   └── ReservationStatus.java       # Booking status enum
├── repository/          # Data access layer
│   ├── UserRepository.java
│   ├── AccommodationUnitRepository.java
│   └── BookingRepository.java
├── service/             # Business logic layer
│   ├── UserService.java             # User operations
│   ├── AccommodationUnitService.java # Property operations
│   └── BookingService.java          # Booking operations
├── dto/                 # Data transfer objects
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── UserResponseDTO.java
├── security/            # Security configuration
│   ├── SecurityConfig.java          # Spring Security setup
│   ├── JwtUtil.java                 # JWT token utilities
│   ├── JwtAuthenticationFilter.java # JWT request filter
│   └── UserDetailsServiceImpl.java  # User authentication
└── TurismappApplication.java # Main application class
```

---

## 🔧 **Key Functionalities**

### **1. User Registration & Authentication**

#### **Registration Process**
- **Frontend**: `RegisterPage.jsx` with form validation
- **Backend**: `POST /api/auth/register`
- **Features**:
  - Client-side validation for all fields
  - Password strength requirements
  - Email uniqueness checking
  - Automatic role assignment (USER)
  - Password encryption with BCrypt

#### **Login System**
- **Frontend**: `LoginPage.jsx` with credential validation
- **Backend**: `POST /api/auth/login`
- **Features**:
  - JWT token generation and validation
  - Secure password verification
  - User session management
  - Automatic token refresh handling

### **2. Role-Based Access Control**

#### **User Roles**
- **USER**: Can browse and book accommodations
- **OWNER**: Can manage properties, view analytics, handle bookings
- **ADMIN**: Full system access (future enhancement)

#### **Route Protection**
- **Public Routes**: Home, Login, Register, Units List
- **Protected Routes**: Dashboard, My Units, Booking Management
- **Implementation**: `PrivateRoute.jsx` component with authentication checks

### **3. Accommodation Unit Management**

#### **Public Unit Browsing** (`UnitsListPage.jsx`)
- **Endpoint**: `GET /api/units/public`
- **Features**:
  - Search by name, location, description
  - Filter by price range, capacity, amenities
  - Real-time search with debouncing
  - Responsive card-based layout
  - Glassmorphism design elements

#### **Owner Unit Management** (`MyUnitsPage.jsx`)
- **Endpoint**: `GET /api/units/my-units`
- **Features**:
  - CRUD operations for owned properties
  - Status management (active/inactive)
  - Revenue tracking per unit
  - Booking statistics
  - Quick action buttons

### **4. Booking System**

#### **Booking Creation**
- **Component**: `BookingForm.jsx`
- **Endpoint**: `POST /api/bookings`
- **Features**:
  - Date range selection with validation
  - Guest information collection
  - Availability checking
  - Price calculation
  - Conflict prevention

#### **Booking Management**
- **Dashboard Integration**: Recent bookings display
- **Endpoint**: `GET /api/bookings/recent`
- **Features**:
  - Booking status tracking
  - Guest communication
  - Cancellation handling

### **5. Analytics & Profit Tracking**

#### **Dashboard Analytics** (`DashboardPage.jsx`)
- **Primary Endpoint**: `GET /api/profit/stats`
- **Key Metrics**:
  - Total revenue calculation
  - Booking count statistics
  - Occupancy rate analysis
  - Average rating display
  - Monthly growth trends

#### **Profit Visualization** (`ProfitChart.jsx`)
- **Endpoint**: `GET /api/profit/monthly`
- **Features**:
  - Monthly revenue charts
  - Trend analysis
  - Comparative data display
  - Interactive chart elements

---

## 🔗 **API Integration & Endpoints**

### **Authentication Endpoints**
```javascript
POST /api/auth/register    // User registration
POST /api/auth/login       // User authentication  
GET  /api/auth/me          // Current user details
```

### **Accommodation Unit Endpoints**
```javascript
GET    /api/units/public        // Public unit search/filter
GET    /api/units/my-units      // Owner's units
POST   /api/units              // Create new unit
PUT    /api/units/{id}          // Update unit
DELETE /api/units/{id}          // Delete unit
PATCH  /api/units/{id}/status   // Toggle unit status
```

### **Booking Endpoints**
```javascript
POST /api/bookings              // Create booking
GET  /api/bookings/recent       // Recent bookings
GET  /api/bookings/by-unit/{id} // Unit-specific bookings
```

### **Analytics Endpoints**
```javascript
GET /api/profit/stats     // Dashboard statistics
GET /api/profit/monthly   // Monthly profit data
```

---

## 🔐 **Authentication System Architecture**

### **JWT Token Flow**
1. **Login**: User credentials → Backend validation → JWT token generation
2. **Storage**: Token stored in localStorage with user data
3. **Request Injection**: Axios interceptor adds token to all API calls
4. **Validation**: Backend JWT filter validates token on protected routes
5. **Refresh**: Automatic logout on token expiration (401 errors)

### **AuthContext Implementation**
```javascript
// Frontend state management
const AuthContext = {
  user: UserObject,           // Current user data
  isAuthenticated: boolean,   // Authentication status
  loading: boolean,          // Loading state
  login: function,           // Login method
  register: function,        // Registration method
  logout: function           // Logout method
}
```

### **Backend Security Configuration**
- **JWT Secret**: Configurable secret key for token signing
- **Token Expiration**: Configurable token lifetime
- **CORS Policy**: Frontend origin whitelisting
- **Password Encryption**: BCrypt hashing with salt
- **Session Management**: Stateless JWT-based sessions

---

## 📡 **Axios Configuration & Token Management**

### **API Client Setup** (`services/axios.js`)
```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' }
});

// Request interceptor - Token injection
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - Error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Auto-logout on token expiration
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      showAutoLogoutToast();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### **Token Lifecycle**
1. **Generation**: Backend creates JWT on successful login
2. **Storage**: Frontend stores token in localStorage
3. **Transmission**: Automatic injection in Authorization header
4. **Validation**: Backend validates token signature and expiration
5. **Renewal**: Manual re-login required (future: refresh tokens)

---

## 🎨 **Styling & Design System**

### **Glassmorphism Design**
- **Background**: Starry night sky with gradient overlays
- **Cards**: Semi-transparent with backdrop blur effects
- **Colors**: Purple/violet/indigo palette with transparency
- **Shadows**: Soft, multi-layered shadow effects

### **TailwindCSS Configuration**
```css
/* Core glassmorphism classes */
.glass-card {
  @apply bg-white/10 backdrop-blur-xl border border-white/20;
}

/* Responsive design */
- Mobile-first approach
- Breakpoint-based layouts
- Touch-friendly interface elements
- Optimized for various screen sizes
```

### **Component Design Patterns**
- **GlassCard**: Reusable container with glassmorphism styling
- **PrimaryButton**: Consistent button styling with hover effects
- **InputField**: Form inputs with focus states and validation
- **LoadingSpinner**: Animated loading indicators

---

## ✨ **Advanced Features**

### **1. Toast Notification System**
- **Global Context**: `ToastContext.jsx` for application-wide notifications
- **Types**: Success, error, warning, info messages
- **Auto-dismiss**: Configurable timeout periods
- **Positioning**: Consistent placement and styling

### **2. Loading States**
- **Component-level**: Individual loading spinners
- **Page-level**: Full-page loading overlays
- **API-driven**: Automatic loading state management
- **Skeleton screens**: Future enhancement for better UX

### **3. Error Handling**
- **API Errors**: Centralized error processing
- **Form Validation**: Real-time client-side validation
- **Network Errors**: Graceful degradation
- **User Feedback**: Clear error messages and recovery options

### **4. Responsive Design**
- **Mobile-first**: Optimized for mobile devices
- **Breakpoints**: Tablet and desktop adaptations
- **Touch Interface**: Touch-friendly interactions
- **Performance**: Optimized for various network conditions

### **5. Search & Filtering**
- **Real-time Search**: Debounced search input
- **Multi-criteria Filtering**: Price, location, capacity, amenities
- **URL State**: Filter state preserved in URL parameters
- **Empty States**: Graceful handling of no results

---

## 🔄 **Data Flow Architecture**

### **Frontend Data Flow**
1. **User Interaction** → Component State Update
2. **API Call** → Axios Request with JWT Token
3. **Backend Processing** → Database Query/Update
4. **Response Handling** → State Update & UI Refresh
5. **Error Handling** → Toast Notification & Error Display

### **Backend Data Flow**
1. **HTTP Request** → Spring Boot Controller
2. **Authentication** → JWT Filter Validation
3. **Business Logic** → Service Layer Processing
4. **Data Access** → Repository Layer (JPA/Hibernate)
5. **Response** → JSON Serialization & Return

---

## 🚀 **Performance & Optimization**

### **Frontend Optimizations**
- **Code Splitting**: Route-based lazy loading
- **Memoization**: React.memo for expensive components
- **Debouncing**: Search input optimization
- **Image Optimization**: Responsive image loading

### **Backend Optimizations**
- **Connection Pooling**: Database connection management
- **Query Optimization**: Efficient JPA queries
- **Caching**: Future enhancement for frequently accessed data
- **Pagination**: Large dataset handling

---

## 🔒 **Security Considerations**

### **Frontend Security**
- **XSS Protection**: Input sanitization
- **CSRF Protection**: Token-based requests
- **Sensitive Data**: No sensitive data in localStorage (except tokens)
- **HTTPS**: Production deployment requirement

### **Backend Security**
- **JWT Security**: Signed tokens with expiration
- **Password Security**: BCrypt hashing
- **Input Validation**: Comprehensive request validation
- **SQL Injection**: JPA/Hibernate protection
- **CORS Policy**: Restricted origin access

---

## 📊 **Database Schema**

### **Core Entities**
```sql
users
├── id (PRIMARY KEY)
├── first_name, last_name
├── email (UNIQUE)
├── password (ENCRYPTED)
├── phone, address
├── role (ENUM: USER, OWNER, ADMIN)
└── created_at

accommodation_units
├── id (PRIMARY KEY)
├── name, description, location
├── price_per_night, capacity
├── rating, review_count
├── total_bookings, monthly_revenue
├── status (ENUM: active, inactive)
├── images (ElementCollection)
├── amenities (ElementCollection)
└── owner_id (FOREIGN KEY → users.id)

bookings
├── id (PRIMARY KEY)
├── accommodation_unit_id (FOREIGN KEY)
├── guest_name, guest_email
├── check_in_date, check_out_date
├── number_of_guests
├── total_price
├── status (ENUM: confirmed, cancelled, completed)
└── created_at
```

This comprehensive tourism management application provides a complete solution for accommodation management with modern web technologies, secure authentication, and intuitive user experience. The architecture supports scalability and future enhancements while maintaining clean code organization and best practices.
