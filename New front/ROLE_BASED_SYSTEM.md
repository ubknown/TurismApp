# Role-Based Authentication System Implementation

## Overview

This tourism management application now features a complete role-based authentication system with three distinct user roles: **GUEST**, **OWNER**, and **ADMIN**. Each role has specific permissions and access to different parts of the application.

## User Roles

### 🏃 GUEST (Regular User)
- **Purpose**: Regular users who want to browse and book accommodations
- **Permissions**:
  - Browse all accommodation units
  - View unit details
  - Make bookings
  - View their own booking history
  - Leave reviews and ratings
- **Default redirect after login**: `/units` (Browse accommodations)
- **Access restrictions**: Cannot access dashboard, unit management, or owner analytics

### 🏠 OWNER (Property Owner)
- **Purpose**: Property owners who list and manage accommodations
- **Permissions**:
  - All GUEST permissions
  - Access to owner dashboard with analytics
  - Manage their own properties (add, edit, delete units)
  - View profit statistics and analytics
  - Manage bookings for their properties
  - View occupancy rates and performance metrics
- **Default redirect after login**: `/dashboard`
- **Access restrictions**: Only see data for their own properties

### 👑 ADMIN (System Administrator)
- **Purpose**: System administrators with full access
- **Permissions**:
  - All OWNER permissions
  - Manage all properties across the platform
  - View system-wide analytics
  - Manage all users and bookings
  - Access to admin-specific features
- **Default redirect after login**: `/dashboard`
- **Access restrictions**: None (full access)

## Registration Process

### Role Selection UI
The registration page now includes a clear role selector with two main options:

```jsx
// Guest Option
"Guest - Browse accommodations, make bookings, and leave reviews"

// Property Owner Option  
"Property Owner - Manage properties, view analytics, and track profits"
```

### Backend Integration
- Role is captured from the frontend form
- Validated and stored in the User entity
- Default role is `GUEST` if none specified
- Role is included in JWT tokens and API responses

## Authentication Flow

### 1. Registration
```
User fills form → Selects role → POST /api/auth/register → User created with role → Redirect to login
```

### 2. Login
```
User logs in → JWT includes role → Role-based redirect:
- GUEST → /units (Browse accommodations)
- OWNER → /dashboard (Owner dashboard)  
- ADMIN → /dashboard (Admin dashboard)
```

### 3. Route Protection
```
User navigates → RoleRoute checks role → Allow/Redirect based on permissions
```

## Technical Implementation

### Frontend Components

#### `RoleRoute.jsx`
```jsx
// Protects routes based on allowed roles
<RoleRoute allowedRoles={['OWNER', 'ADMIN']}>
  <DashboardPage />
</RoleRoute>
```

#### `AuthContext.jsx`
```jsx
// Helper functions for role checking
const { isGuest, isOwner, isAdmin } = useAuth();
```

#### Navigation (NavBar)
- Shows different menu items based on role
- Owner/Admin: Dashboard, My Units, Bookings
- Guest: Browse Units, My Bookings
- Role indicator in user menu

### Backend Implementation

#### User Entity
```java
@Entity
public class User {
    // ... other fields
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.GUEST;  // Default role
}
```

#### Role Enum
```java
public enum Role {
    GUEST,
    OWNER, 
    ADMIN
}
```

#### JWT Integration
- Role included in JWT payload
- Role returned in login/register responses
- Role used for API endpoint authorization

## Route Structure

### Public Routes
- `/` - Home page
- `/login` - Login page
- `/register` - Registration page
- `/units` - Browse accommodations (public)

### Protected Routes (All Authenticated Users)
- `/bookings` - View bookings (content varies by role)

### Owner/Admin Only Routes
- `/dashboard` - Analytics and management dashboard
- `/my-units` - Property management

### Role-Based Content
Even on shared routes, content is filtered by role:

#### `/bookings`
- **GUEST**: Shows only their own bookings
- **OWNER**: Shows bookings for their properties
- **ADMIN**: Shows all bookings in system

#### `/dashboard` 
- **OWNER**: Shows analytics for their properties only
- **ADMIN**: Shows system-wide analytics

## API Endpoints by Role

### Guest Endpoints
```
GET /api/units - Browse all units
GET /api/units/{id} - View unit details
POST /api/bookings - Create booking
GET /api/bookings/my-bookings - View own bookings
POST /api/reviews - Leave reviews
```

### Owner Endpoints
```
All Guest endpoints +
GET /api/units/owner - View own units
POST /api/units - Create new unit
PUT /api/units/{id} - Update own unit
DELETE /api/units/{id} - Delete own unit
GET /api/profit/stats - View profit analytics
GET /api/bookings/owner - View bookings for own units
```

### Admin Endpoints
```
All Owner endpoints +
GET /api/users - Manage all users
GET /api/bookings - View all bookings
GET /api/admin/stats - System-wide statistics
```

## Security Features

### Route Protection
- `PrivateRoute`: Requires authentication
- `RoleRoute`: Requires specific roles
- Automatic redirect to appropriate pages based on role

### API Security
- JWT token validation on all protected endpoints
- Role-based authorization on sensitive endpoints
- Automatic logout on token expiration (401 errors)

### Frontend Security
- Role-based conditional rendering
- Unauthorized access redirects
- Clear role indicators in UI

## Usage Examples

### Conditional Rendering
```jsx
const { isOwner, isAdmin } = useAuth();

return (
  <div>
    {/* Show to all authenticated users */}
    <BookingsList />
    
    {/* Show only to owners and admins */}
    {(isOwner() || isAdmin()) && (
      <AnalyticsDashboard />
    )}
  </div>
);
```

### Role-Based Navigation
```jsx
{isAuthenticated && (
  <>
    {/* Common links */}
    <Link to="/bookings">Bookings</Link>
    
    {/* Owner/Admin only */}
    {(isOwner() || isAdmin()) && (
      <>
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/my-units">My Units</Link>
      </>
    )}
  </>
)}
```

## Testing the System

### Test Scenarios
1. **Register as Guest**: Should redirect to `/units` after login
2. **Register as Owner**: Should redirect to `/dashboard` after login
3. **Guest accessing `/dashboard`**: Should redirect to `/units`
4. **Owner accessing guest bookings**: Should only see own property bookings
5. **Logout and role persistence**: Role should be maintained across sessions

### Manual Testing Checklist
- [ ] Register new Guest account
- [ ] Register new Owner account  
- [ ] Login with different roles
- [ ] Verify role-based redirects
- [ ] Test route protection
- [ ] Verify navigation menu changes
- [ ] Test role persistence after page refresh
- [ ] Verify API endpoints respect roles

## Future Enhancements

### Potential Improvements
1. **Role Hierarchy**: Implement role inheritance (ADMIN extends OWNER extends GUEST)
2. **Granular Permissions**: Add specific permissions beyond roles
3. **Multiple Roles**: Allow users to have multiple roles
4. **Role Switching**: Allow admins to impersonate other users
5. **Audit Logging**: Track role-based actions for security

### Additional Admin Features
- User management interface
- System-wide statistics
- Property approval workflow
- Advanced reporting and analytics

This role-based system provides a solid foundation for scaling the application with more sophisticated access control and user management features.
