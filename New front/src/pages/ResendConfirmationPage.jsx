import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, ArrowLeft, RefreshCw } from 'lucide-react';
import { useToast } from '../context/ToastContext';
import axios from 'axios';

const ResendConfirmationPage = () => {
  const { success: showSuccess, error: showError } = useToast();
  const navigate = useNavigate();
  
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    // Validate email
    if (!email) {
      const errorMessage = 'Please enter your email address';
      setError(errorMessage);
      showError('Validation Error', errorMessage);
      setLoading(false);
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      const errorMessage = 'Please enter a valid email address';
      setError(errorMessage);
      showError('Validation Error', errorMessage);
      setLoading(false);
      return;
    }

    try {
      const response = await axios.post(`/api/auth/resend-confirmation?email=${encodeURIComponent(email)}`);
      
      if (response.status === 200) {
        const successMessage = 'Confirmation email sent! Please check your inbox.';
        setSuccess(successMessage);
        showSuccess('Email Sent', 'Please check your email for the confirmation link.');
        
        // Redirect to login after 3 seconds
        setTimeout(() => {
          navigate('/login');
        }, 3000);
      }
    } catch (error) {
      let errorMessage = 'Failed to send confirmation email. Please try again.';
      
      if (error.response?.status === 404) {
        errorMessage = 'No account found with this email address.';
      } else if (error.response?.status === 400) {
        errorMessage = error.response.data.message || 'Email already confirmed.';
      }
      
      setError(errorMessage);
      showError('Resend Failed', errorMessage);
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
              <Mail className="w-full h-full text-violet-400" />
            </div>
            <h1 className="text-3xl font-bold text-white mb-2">Resend Confirmation</h1>
            <p className="text-violet-200">Enter your email to receive a new confirmation link</p>
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

          {/* Resend Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-white mb-2">
                Email Address
              </label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                  placeholder="Enter your email address"
                  required
                  disabled={loading}
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className={`w-full font-semibold py-3 px-6 rounded-xl transition-all duration-300 transform shadow-lg ${
                loading
                  ? 'bg-gray-600 text-gray-300 cursor-not-allowed'
                  : 'bg-gradient-to-r from-violet-600 to-indigo-600 hover:from-violet-700 hover:to-indigo-700 text-white hover:scale-105'
              }`}
            >
              {loading ? (
                <div className="flex items-center justify-center">
                  <RefreshCw className="w-4 h-4 mr-2 animate-spin" />
                  Sending...
                </div>
              ) : (
                <div className="flex items-center justify-center">
                  <Mail className="w-4 h-4 mr-2" />
                  Resend Confirmation Email
                </div>
              )}
            </button>
          </form>

          {/* Navigation Links */}
          <div className="mt-8 space-y-4">
            <Link
              to="/login"
              className="w-full block text-center bg-white/10 hover:bg-white/20 text-white font-semibold py-3 px-6 rounded-xl transition-all duration-300 border border-white/20"
            >
              <ArrowLeft className="w-4 h-4 inline mr-2" />
              Back to Login
            </Link>
            
            <div className="text-center">
              <p className="text-violet-300 text-sm">
                Don't have an account?{' '}
                <Link to="/register" className="text-violet-200 hover:text-white transition-colors">
                  Sign up here
                </Link>
              </p>
            </div>
          </div>

          {/* Help Text */}
          <div className="mt-6 text-center">
            <p className="text-violet-300 text-xs">
              Check your spam folder if you don't see the email in your inbox.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ResendConfirmationPage;
