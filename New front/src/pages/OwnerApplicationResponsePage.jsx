import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Eye, EyeOff, CheckCircle, XCircle, AlertCircle, Shield } from 'lucide-react';
import api from '../services/axios';

const OwnerApplicationResponsePage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  
  const [token, setToken] = useState('');
  const [action, setAction] = useState('');
  const [password, setPassword] = useState('');
  const [reviewNotes, setReviewNotes] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [applicationData, setApplicationData] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [alreadyReviewed, setAlreadyReviewed] = useState(false);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const tokenParam = params.get('token');
    const actionParam = params.get('action');
    
    if (!tokenParam || !actionParam) {
      setError('Invalid approval link. Missing required parameters.');
      setLoading(false);
      return;
    }
    
    setToken(tokenParam);
    setAction(actionParam);
    validateToken(tokenParam, actionParam);
  }, [location]);

  const validateToken = async (tokenParam, actionParam) => {
    try {
      setLoading(true);
      const response = await api.get('/api/owner-applications/validate-token', {
        params: { token: tokenParam, action: actionParam }
      });
      
      if (response.data.valid) {
        setApplicationData(response.data);
      } else {
        setAlreadyReviewed(true);
      }
    } catch (err) {
      console.error('Token validation error:', err);
      setError(err.response?.data?.error || 'Failed to validate approval link');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!password.trim()) {
      setError('Password is required');
      return;
    }
    
    try {
      setProcessing(true);
      setError('');
      
      const response = await api.post('/api/owner-applications/confirm-action', {
        token,
        password,
        reviewNotes
      });
      
      if (response.data.success) {
        setSuccess(`Application ${action.toLowerCase()}d successfully!`);
        setPassword('');
        setReviewNotes('');
        // Auto-redirect after success
        setTimeout(() => {
          navigate('/');
        }, 3000);
      } else {
        setAlreadyReviewed(true);
      }
    } catch (err) {
      console.error('Action confirmation error:', err);
      if (err.response?.status === 401) {
        setError('Invalid password. Please try again.');
      } else {
        setError(err.response?.data?.error || 'Failed to process request');
      }
    } finally {
      setProcessing(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg shadow-lg">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Validating approval link...</p>
        </div>
      </div>
    );
  }

  if (alreadyReviewed) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
        <div className="bg-white rounded-lg shadow-lg p-8 max-w-md w-full text-center">
          <AlertCircle className="w-16 h-16 text-yellow-500 mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-gray-800 mb-4">Already Reviewed</h1>
          <p className="text-gray-600 mb-6">
            This request has already been reviewed. Each approval link can only be used once for security purposes.
          </p>
          <button
            onClick={() => navigate('/')}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition duration-200"
          >
            Return to Home
          </button>
        </div>
      </div>
    );
  }

  if (error && !applicationData) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
        <div className="bg-white rounded-lg shadow-lg p-8 max-w-md w-full text-center">
          <XCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-gray-800 mb-4">Invalid Link</h1>
          <p className="text-gray-600 mb-6">{error}</p>
          <button
            onClick={() => navigate('/')}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition duration-200"
          >
            Return to Home
          </button>
        </div>
      </div>
    );
  }

  if (success) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
        <div className="bg-white rounded-lg shadow-lg p-8 max-w-md w-full text-center">
          <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-gray-800 mb-4">Success!</h1>
          <p className="text-gray-600 mb-6">{success}</p>
          <p className="text-sm text-gray-500 mb-4">
            The applicant has been notified of your decision. Redirecting to home page...
          </p>
          <button
            onClick={() => navigate('/')}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition duration-200"
          >
            Return to Home Now
          </button>
        </div>
      </div>
    );
  }

  const isApproval = action === 'approve';

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 py-8 px-4">
      <div className="max-w-2xl mx-auto">
        {/* Header */}
        <div className="bg-white rounded-lg shadow-lg p-6 mb-6">
          <div className="flex items-center mb-4">
            <Shield className="w-8 h-8 text-blue-600 mr-3" />
            <h1 className="text-2xl font-bold text-gray-800">
              Secure Owner Application Review
            </h1>
          </div>
          <div className={`flex items-center p-4 rounded-lg ${
            isApproval ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'
          }`}>
            {isApproval ? (
              <CheckCircle className="w-6 h-6 text-green-600 mr-3" />
            ) : (
              <XCircle className="w-6 h-6 text-red-600 mr-3" />
            )}
            <span className={`font-semibold ${
              isApproval ? 'text-green-800' : 'text-red-800'
            }`}>
              Action: {isApproval ? 'APPROVE' : 'REJECT'} Application
            </span>
          </div>
        </div>

        {/* Application Details */}
        <div className="bg-white rounded-lg shadow-lg p-6 mb-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Application Details</h2>
          <div className="space-y-3">
            <div>
              <span className="font-medium text-gray-600">Applicant Name:</span>
              <span className="ml-2 text-gray-800">{applicationData?.applicantName}</span>
            </div>
            <div>
              <span className="font-medium text-gray-600">Email:</span>
              <span className="ml-2 text-gray-800">{applicationData?.applicantEmail}</span>
            </div>
            <div>
              <span className="font-medium text-gray-600">Submitted:</span>
              <span className="ml-2 text-gray-800">
                {new Date(applicationData?.submittedAt).toLocaleString()}
              </span>
            </div>
            <div>
              <span className="font-medium text-gray-600">Message:</span>
              <div className="mt-1 p-3 bg-gray-50 rounded border">
                {applicationData?.applicationMessage || 'No message provided'}
              </div>
            </div>
          </div>
        </div>

        {/* Confirmation Form */}
        <div className="bg-white rounded-lg shadow-lg p-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">
            Confirm {isApproval ? 'Approval' : 'Rejection'}
          </h2>
          
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Password Field */}
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
                Admin Password *
              </label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 pr-10"
                  placeholder="Enter admin password"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700"
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            {/* Review Notes */}
            <div>
              <label htmlFor="reviewNotes" className="block text-sm font-medium text-gray-700 mb-2">
                {isApproval ? 'Approval Notes (Optional)' : 'Rejection Reason (Optional)'}
              </label>
              <textarea
                id="reviewNotes"
                value={reviewNotes}
                onChange={(e) => setReviewNotes(e.target.value)}
                rows={4}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder={isApproval ? 
                  'Welcome message or additional instructions...' : 
                  'Reason for rejection or feedback for the applicant...'
                }
              />
            </div>

            {/* Error Display */}
            {error && (
              <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-red-800">{error}</p>
              </div>
            )}

            {/* Security Notice */}
            <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
              <div className="flex items-start">
                <Shield className="w-5 h-5 text-blue-600 mr-2 mt-0.5" />
                <div className="text-sm text-blue-800">
                  <p className="font-medium">Security Notice:</p>
                  <p>This action can only be performed once. The applicant will be automatically notified of your decision.</p>
                </div>
              </div>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={processing}
              className={`w-full py-3 px-4 rounded-lg font-semibold text-white transition duration-200 ${
                processing
                  ? 'bg-gray-400 cursor-not-allowed'
                  : isApproval
                  ? 'bg-green-600 hover:bg-green-700'
                  : 'bg-red-600 hover:bg-red-700'
              }`}
            >
              {processing ? (
                <div className="flex items-center justify-center">
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                  Processing...
                </div>
              ) : (
                `Confirm ${isApproval ? 'Approval' : 'Rejection'}`
              )}
            </button>
          </form>
        </div>

        {/* Footer */}
        <div className="mt-6 text-center">
          <button
            onClick={() => navigate('/')}
            className="text-blue-600 hover:text-blue-800 underline"
          >
            Return to Home Page
          </button>
        </div>
      </div>
    </div>
  );
};

export default OwnerApplicationResponsePage;
