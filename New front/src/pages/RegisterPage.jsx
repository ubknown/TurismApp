import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { UserPlus, Mail, Lock, User, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import axios from 'axios';

// Set base URL for axios
axios.defaults.baseURL = 'http://localhost:8080';

const RegisterPage = () => {
  const { isAuthenticated } = useAuth();
  const { success: showSuccess, error: showError } = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'GUEST' // Default to GUEST
  });
  
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  // Preselect role from URL
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const urlRole = params.get('role');
    if (urlRole && (urlRole.toUpperCase() === 'OWNER' || urlRole.toUpperCase() === 'GUEST')) {
      setFormData(prev => ({ ...prev, role: urlRole.toUpperCase() }));
    }
  }, [location.search]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear error when user starts typing
    if (error) setError('');
    if (success) setSuccess('');
  };

  const validateForm = () => {
    // Enhanced validation
    if (!formData.firstName || !formData.lastName || !formData.email || !formData.password || 
        !formData.confirmPassword) {
      const errorMessage = 'Please fill in all required fields';
      setError(errorMessage);
      showError('Validation Error', errorMessage);
      return false;
    }

    // Name validation
    if (formData.firstName.length < 2 || formData.lastName.length < 2) {
      const errorMessage = 'First and last names must be at least 2 characters long';
      setError(errorMessage);
      showError('Validation Error', errorMessage);
      return false;
    }

    // TODO: Add more comprehensive email validation for production
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      const errorMessage = 'Please enter a valid email address';
      setError(errorMessage);
      showError('Validation Error', errorMessage);
      return false;
    }

    // TODO: For production, implement stronger password requirements
    if (formData.password.length < 8) {
      const errorMessage = 'Password must be at least 8 characters long';
      setError(errorMessage);
      showError('Validation Error', errorMessage);
      return false;
    }

    // Basic password strength check
    const hasUpperCase = /[A-Z]/.test(formData.password);
    const hasLowerCase = /[a-z]/.test(formData.password);
    const hasNumbers = /\d/.test(formData.password);
    
    if (!hasUpperCase || !hasLowerCase || !hasNumbers) {
      const errorMessage = 'Password must contain at least one uppercase letter, one lowercase letter, and one number';
      setError(errorMessage);
      showError('Validation Error', errorMessage);
      return false;
    }

    if (formData.password !== formData.confirmPassword) {
      const errorMessage = 'Passwords do not match';
      setError(errorMessage);
      showError('Validation Error', errorMessage);
      return false;
    }

    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    if (!validateForm()) {
      setLoading(false);
      return;
    }

    try {
      // Prepare data for backend (excluding confirmPassword)
      const registrationData = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        password: formData.password,
        role: formData.role
      };

      const response = await axios.post('/api/auth/register', registrationData);
      
      console.log('Registration response:', response); // Debug log
      
      if (response.status === 201) {
        const roleText = formData.role === 'OWNER' ? 'Property Owner' : 'Guest';
        const successMessage = `Registration successful as ${roleText}! A confirmation email has been sent.`;
        
        // Show success message and log
        setSuccess(successMessage);
        showSuccess('Registration Successful', 'Redirecting to login page...');
        console.log('Registration successful, redirecting to login...'); // Debug log
        
        // Prepare navigation state
        const navigationState = {
          registrationSuccess: true,
          userRole: roleText,
          email: formData.email,
          timestamp: Date.now() // Add timestamp for debugging
        };
        
        console.log('Navigation state:', navigationState); // Debug log
        
        // Use a shorter timeout and add fallback
        const redirectTimer = setTimeout(() => {
          console.log('Executing redirect to login page...'); // Debug log
          navigate('/login', { 
            state: navigationState,
            replace: true 
          });
        }, 1000); // Reduced to 1 second
        
        // Fallback redirect after 3 seconds if something goes wrong
        const fallbackTimer = setTimeout(() => {
          console.log('Fallback redirect executing...'); // Debug log
          navigate('/login', { 
            state: navigationState,
            replace: true 
          });
        }, 3000);
        
        // Cleanup on unmount
        return () => {
          clearTimeout(redirectTimer);
          clearTimeout(fallbackTimer);
        };
      }
    } catch (error) {
      let errorMessage = 'Registration failed. Please try again.';
      
      if (error.response?.status === 409) {
        // Handle conflict (email or phone already exists)
        const errorData = error.response.data;
        errorMessage = errorData.message || 'Email or phone number already registered';
      } else if (error.response?.status === 400) {
        // Handle validation errors
        const errorData = error.response.data;
        errorMessage = errorData.message || 'Invalid registration data';
      }
      
      setError(errorMessage);
      showError('Registration Failed', errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6">
      <div className="w-full max-w-md">
        <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-8">
          {/* Header */}
          <div className="text-center mb-8">
            <div className="w-16 h-16 mx-auto mb-4 p-4 rounded-xl bg-violet-500/20">
              <UserPlus className="w-full h-full text-violet-400" />
            </div>
            <h1 className="text-3xl font-bold text-white mb-2">Create Account</h1>
            <p className="text-violet-200">Join our tourism platform</p>
          </div>

          {/* Error Message */}
          {error && (
            <div className="mb-6 p-4 bg-red-500/20 border border-red-500/30 rounded-xl">
              <p className="text-red-300 text-sm">{error}</p>
            </div>
          )}

          {/* Success Message */}
          {success && (
            <div className="mb-6 p-4 bg-green-500/20 border border-green-500/30 rounded-xl">
              <p className="text-green-300 text-sm">{success}</p>
            </div>
          )}

          {/* Registration Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Name Fields */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-white mb-2">
                  First Name *
                </label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    type="text"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleInputChange}
                    className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                    placeholder="First name"
                    required
                    disabled={loading}
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-white mb-2">
                  Last Name *
                </label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    type="text"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleInputChange}
                    className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                    placeholder="Last name"
                    required
                    disabled={loading}
                  />
                </div>
              </div>
            </div>

            {/* Email Field */}
            <div>
              <label className="block text-sm font-medium text-white mb-2">
                Email Address *
              </label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                  placeholder="Enter your email"
                  required
                  disabled={loading}
                />
              </div>
            </div>

            {/* Role Selection */}
            <div>
              <label className="block text-sm font-medium text-white mb-3">
                I want to register as *
              </label>
              <div className="space-y-3">
                <div className="flex items-center p-4 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl hover:bg-white/10 transition-all duration-300">
                  <input
                    type="radio"
                    id="guest"
                    name="role"
                    value="GUEST"
                    checked={formData.role === 'GUEST'}
                    onChange={handleInputChange}
                    className="w-4 h-4 text-violet-600 bg-transparent border-2 border-white/30 focus:ring-violet-500 focus:ring-2"
                    disabled={loading}
                  />
                  <label htmlFor="guest" className="ml-3 cursor-pointer">
                    <div className="text-white font-medium">Guest</div>
                    <div className="text-violet-200 text-sm">Browse accommodations, make bookings, and leave reviews</div>
                  </label>
                </div>
                <div className="flex items-center p-4 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl hover:bg-white/10 transition-all duration-300">
                  <input
                    type="radio"
                    id="owner"
                    name="role"
                    value="OWNER"
                    checked={formData.role === 'OWNER'}
                    onChange={handleInputChange}
                    className="w-4 h-4 text-violet-600 bg-transparent border-2 border-white/30 focus:ring-violet-500 focus:ring-2"
                    disabled={loading}
                  />
                  <label htmlFor="owner" className="ml-3 cursor-pointer">
                    <div className="text-white font-medium">Property Owner</div>
                    <div className="text-violet-200 text-sm">Manage properties, view analytics, and track profits</div>
                  </label>
                </div>
              </div>
            </div>

            {/* Password Fields */}
            <div>
              <label className="block text-sm font-medium text-white mb-2">
                Password *
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  className="w-full pl-12 pr-12 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                  placeholder="Enter password (min 6 characters)"
                  required
                  disabled={loading}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-white transition-colors"
                  disabled={loading}
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-white mb-2">
                Confirm Password *
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type={showConfirmPassword ? 'text' : 'password'}
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                  className="w-full pl-12 pr-12 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                  placeholder="Confirm your password"
                  required
                  disabled={loading}
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-white transition-colors"
                  disabled={loading}
                >
                  {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            {/* Register Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-violet-600 hover:bg-violet-700 disabled:bg-violet-800 disabled:cursor-not-allowed text-white font-medium py-3 px-6 rounded-xl transition-all duration-300 transform hover:scale-105 disabled:hover:scale-100 flex items-center justify-center gap-2"
            >
              {loading ? (
                <>
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                  Creating Account...
                </>
              ) : (
                <>
                  <UserPlus className="w-5 h-5" />
                  Create Account
                </>
              )}
            </button>
          </form>

          {/* Footer */}
          <div className="mt-8 text-center">
            <p className="text-gray-300">
              Already have an account?{' '}
              <Link
                to="/login"
                className="text-violet-400 hover:text-violet-300 font-medium transition-colors"
              >
                Sign in here
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
