import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/axios';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    // Check for existing auth token on app load
    const checkAuthStatus = () => {
      try {
        const token = localStorage.getItem('token');
        const userData = localStorage.getItem('user');
        
        if (token && userData) {
          setUser(JSON.parse(userData));
          setIsAuthenticated(true);
        }
      } catch (error) {
        console.error('Auth check failed:', error);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      } finally {
        setLoading(false);
      }
    };

    checkAuthStatus();
  }, []);

  const login = async (email, password) => {
    try {
      const response = await api.post('/api/auth/login', {
        email,
        password
      });

      const { token, user: userData } = response.data;
      
      // Store token and user data
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(userData));
      
      // Update state
      setUser(userData);
      setIsAuthenticated(true);
      
      return { success: true, data: response.data };
    } catch (error) {
      console.error('Login failed:', error);
      // TODO: Add proper error logging for production monitoring
      return { 
        success: false, 
        error: error.response?.data?.message || 'Login failed. Please check your credentials and try again.' 
      };
    }
  };

  const register = async (userData) => {
    try {
      const response = await api.post('/api/auth/register', userData);
      return { success: true, data: response.data };
    } catch (error) {
      console.error('Registration failed:', error);
      // TODO: Add proper error logging for production monitoring
      return { 
        success: false, 
        error: error.response?.data?.message || 'Registration failed. Please try again.' 
      };
    }
  };

  const logout = () => {
    try {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      setUser(null);
      setIsAuthenticated(false);
    } catch (error) {
      // TODO: Handle logout errors gracefully
      console.error('Logout error:', error);
    }
  };

  const value = {
    user,
    isAuthenticated,
    loading,
    login,
    register,
    logout,
    // Helper functions for role-based access
    isGuest: () => user?.role === 'GUEST',
    isOwner: () => user?.role === 'OWNER',
    isAdmin: () => user?.role === 'ADMIN',
    // Helper functions for owner status
    canApplyAsOwner: () => user?.role === 'GUEST' && user?.ownerStatus === 'NONE',
    hasOwnerApplication: () => user?.ownerStatus && user?.ownerStatus !== 'NONE',
    hasOwnerApplicationPending: () => user?.ownerStatus === 'PENDING',
    hasOwnerApplicationApproved: () => user?.ownerStatus === 'APPROVED',
    hasOwnerApplicationRejected: () => user?.ownerStatus === 'REJECTED',
    getOwnerStatus: () => user?.ownerStatus || 'NONE'
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;
