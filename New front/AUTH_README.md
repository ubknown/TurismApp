# Authentication System Documentation

## Overview
This React + Vite frontend implements a complete authentication system that connects to a Spring Boot backend API. The system includes JWT token management, protected routes, and user-friendly notifications.

## Features

### 🔐 Authentication
- **Login**: POST to `/api/auth/login` with email/password
- **Registration**: POST to `/api/auth/register` with user details
- **JWT Storage**: Secure token storage in localStorage
- **Auto-logout**: Automatic logout on token expiration (401 errors)

### 🛡️ Route Protection
- **PrivateRoute**: Component that protects authenticated routes
- **Redirect**: Unauthenticated users redirected to login
- **Return Path**: Users redirected to intended page after login

### 🎨 User Experience
- **Toast Notifications**: Success, error, warning, and info messages
- **Glassmorphism UI**: Beautiful, modern interface with glass effects
- **Responsive Design**: Mobile-first approach with TailwindCSS
- **Loading States**: Visual feedback during API requests

## Architecture

### Components Structure
```
src/
├── components/
│   ├── NavBar.jsx          # Navigation with auth-aware links
│   ├── PrivateRoute.jsx    # Route protection wrapper
│   └── ErrorBoundary.jsx   # Error handling
├── context/
│   ├── AuthContext.jsx     # Authentication state management
│   └── ToastContext.jsx    # Global notification system
├── pages/
│   ├── LoginPage.jsx       # Login form with validation
│   ├── RegisterPage.jsx    # Registration with validation
│   ├── DashboardPage.jsx   # Protected dashboard
│   └── MyUnitsPage.jsx     # Protected user units
├── services/
│   └── axios.js           # API client with JWT interceptors
├── utils/
│   └── globalToast.js     # Global toast utility
└── router/
    └── AppRouter.jsx      # Main routing configuration
```

### Key Components

#### AuthContext
- Manages user authentication state
- Provides login, logout, and register functions
- Persists user data and JWT token
- Exposes `isAuthenticated`, `user`, and auth methods

#### PrivateRoute
- Wraps protected components
- Redirects to login if not authenticated
- Preserves intended destination for post-login redirect

#### Axios Service
- Adds JWT token to all API requests
- Handles 401 errors globally
- Automatically logs out users on token expiration
- Shows toast notification for auto-logout

## API Integration

### Backend Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- All other API calls automatically include JWT Bearer token

### Request/Response Format

#### Login Request
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

#### Login Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "user@example.com"
  }
}
```

#### Registration Request
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "user@example.com",
  "password": "password123",
  "phone": "+1234567890",
  "address": "123 Main St"
}
```

## Usage Examples

### Using AuthContext in Components
```jsx
import { useAuth } from '../context/AuthContext';

const MyComponent = () => {
  const { user, isAuthenticated, login, logout } = useAuth();

  if (!isAuthenticated) {
    return <div>Please log in</div>;
  }

  return <div>Welcome, {user.firstName}!</div>;
};
```

### Using Toast Notifications
```jsx
import { useToast } from '../context/ToastContext';

const MyComponent = () => {
  const { success, error, warning, info } = useToast();

  const handleAction = () => {
    try {
      // Some action
      success('Success!', 'Action completed successfully');
    } catch (err) {
      error('Error!', 'Something went wrong');
    }
  };
};
```

### Creating Protected Routes
```jsx
import PrivateRoute from '../components/PrivateRoute';

const router = createBrowserRouter([
  {
    path: '/dashboard',
    element: (
      <PrivateRoute>
        <DashboardPage />
      </PrivateRoute>
    )
  }
]);
```

## Security Features

1. **JWT Token Management**: Secure storage and automatic inclusion in requests
2. **Auto-logout**: Automatic logout on token expiration
3. **Route Protection**: Unauthorized access prevention
4. **Input Validation**: Client-side validation for forms
5. **Error Handling**: Graceful error handling with user feedback

## Development

### Environment Setup
1. Ensure backend is running on `http://localhost:8080`
2. Install dependencies: `npm install`
3. Start development server: `npm run dev`

### Testing Authentication Flow
1. Visit `/register` to create a new account
2. Visit `/login` to authenticate
3. Try accessing `/dashboard` or `/my-units` (should redirect if not authenticated)
4. Use browser dev tools to clear localStorage and test auto-logout

## Customization

### Changing API Base URL
Update `baseURL` in `src/services/axios.js`:
```javascript
const api = axios.create({
  baseURL: 'http://your-api-url:port',
  // ...
});
```

### Adding New Protected Routes
1. Wrap route component with `<PrivateRoute>`
2. Add route to `AppRouter.jsx`

### Customizing Toast Appearance
Modify styles in `src/context/ToastContext.jsx` or update Tailwind classes.

## Troubleshooting

### Common Issues
1. **CORS Errors**: Ensure backend allows frontend origin
2. **401 Errors**: Check JWT token format and expiration
3. **Redirect Loops**: Verify PrivateRoute logic and localStorage state
4. **Toast Not Showing**: Ensure ToastProvider wraps the app

### Debug Tips
- Check browser dev tools for network requests
- Inspect localStorage for token and user data
- Use React DevTools to inspect context state
- Check console for error messages

## Performance Considerations

1. **Token Refresh**: Consider implementing token refresh logic
2. **Persistent Storage**: Consider using secure storage instead of localStorage
3. **State Management**: For larger apps, consider Redux or Zustand
4. **Code Splitting**: Implement lazy loading for better performance

This authentication system provides a solid foundation for secure, user-friendly authentication in React applications.
