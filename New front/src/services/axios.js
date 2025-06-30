import axios from 'axios';
import { showAutoLogoutToast } from '../utils/globalToast';

// Create axios instance with base URL
const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,  // Enable credentials for CORS
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token to all requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('🔵 Axios Request:', {
        method: config.method?.toUpperCase(),
        url: config.url,
        hasToken: !!token,
        tokenPrefix: token.substring(0, 20) + '...'
      });
    } else {
      console.warn('⚠️ No token found for request:', config.method?.toUpperCase(), config.url);
    }
    return config;
  },
  (error) => {
    console.error('❌ Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor to handle 401 errors globally
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.error('❌ Axios Response Error:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      url: error.config?.url,
      method: error.config?.method?.toUpperCase(),
      data: error.response?.data
    });

    if (error.response?.status === 401) {
      // Check if this is a request to a public endpoint
      const publicEndpoints = [
        '/api/units',
        '/api/units/',
        '/api/auth/',
        '/api/reviews/unit/',
        '/uploads/'
      ];
      
      const isPublicEndpoint = publicEndpoints.some(endpoint => 
        error.config?.url?.includes(endpoint)
      );
      
      // Only redirect to login for protected endpoints
      if (!isPublicEndpoint) {
        console.warn('🔴 401 Unauthorized - redirecting to login');
        // Token expired or invalid - logout user
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        showAutoLogoutToast();
        window.location.href = '/login';
      }
    }

    if (error.response?.status === 403) {
      console.warn('🔴 403 Forbidden - access denied');
    }

    return Promise.reject(error);
  }
);

export default api;
