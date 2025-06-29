import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { CheckCircle, XCircle, Mail, RefreshCw, ArrowLeft } from 'lucide-react';

const EmailConfirmationPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('loading');
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const success = searchParams.get('success');
    const error = searchParams.get('error');

    setTimeout(() => {
      setIsLoading(false);
      
      if (success === 'true') {
        setStatus('success');
        setMessage('Your email has been successfully confirmed! You can now log in to your account.');
      } else if (error) {
        setStatus('error');
        switch (error) {
          case 'invalid-token':
            setMessage('The confirmation link is invalid. Please request a new confirmation email.');
            break;
          case 'expired-token':
            setMessage('The confirmation link has expired. Please request a new confirmation email.');
            break;
          case 'already-confirmed':
            setMessage('Your email is already confirmed. You can log in to your account.');
            break;
          case 'server-error':
            setMessage('A server error occurred. Please try again or contact support.');
            break;
          default:
            setMessage('An unexpected error occurred during email confirmation.');
        }
      } else {
        setStatus('error');
        setMessage('No confirmation status provided.');
      }
    }, 1000);
  }, [searchParams]);

  const handleResendConfirmation = () => {
    // Redirect to a resend confirmation page or show modal
    navigate('/resend-confirmation');
  };

  const handleGoToLogin = () => {
    navigate('/login');
  };

  const handleGoHome = () => {
    navigate('/');
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6">
      <div className="w-full max-w-md">
        <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-8">
          
          {/* Loading State */}
          {isLoading && (
            <div className="text-center">
              <div className="w-16 h-16 mx-auto mb-4 p-4 rounded-xl bg-violet-500/20">
                <RefreshCw className="w-full h-full text-violet-400 animate-spin" />
              </div>
              <h1 className="text-2xl font-bold text-white mb-2">Confirming Email</h1>
              <p className="text-violet-200">Please wait while we verify your email...</p>
            </div>
          )}

          {/* Success State */}
          {!isLoading && status === 'success' && (
            <>
              <div className="text-center mb-8">
                <div className="w-16 h-16 mx-auto mb-4 p-4 rounded-xl bg-green-500/20">
                  <CheckCircle className="w-full h-full text-green-400" />
                </div>
                <h1 className="text-3xl font-bold text-white mb-2">Email Confirmed!</h1>
                <p className="text-green-200">{message}</p>
              </div>

              <div className="space-y-4">
                <button
                  onClick={handleGoToLogin}
                  className="w-full bg-gradient-to-r from-violet-600 to-indigo-600 hover:from-violet-700 hover:to-indigo-700 text-white font-semibold py-3 px-6 rounded-xl transition-all duration-300 transform hover:scale-105 shadow-lg"
                >
                  Go to Login
                </button>
                
                <button
                  onClick={handleGoHome}
                  className="w-full bg-white/10 hover:bg-white/20 text-white font-semibold py-3 px-6 rounded-xl transition-all duration-300 border border-white/20"
                >
                  <ArrowLeft className="w-4 h-4 inline mr-2" />
                  Back to Home
                </button>
              </div>
            </>
          )}

          {/* Error State */}
          {!isLoading && status === 'error' && (
            <>
              <div className="text-center mb-8">
                <div className="w-16 h-16 mx-auto mb-4 p-4 rounded-xl bg-red-500/20">
                  <XCircle className="w-full h-full text-red-400" />
                </div>
                <h1 className="text-3xl font-bold text-white mb-2">Confirmation Failed</h1>
                <p className="text-red-200">{message}</p>
              </div>

              <div className="space-y-4">
                {(searchParams.get('error') === 'invalid-token' || 
                  searchParams.get('error') === 'expired-token') && (
                  <button
                    onClick={handleResendConfirmation}
                    className="w-full bg-gradient-to-r from-violet-600 to-indigo-600 hover:from-violet-700 hover:to-indigo-700 text-white font-semibold py-3 px-6 rounded-xl transition-all duration-300 transform hover:scale-105 shadow-lg"
                  >
                    <Mail className="w-4 h-4 inline mr-2" />
                    Resend Confirmation Email
                  </button>
                )}
                
                <button
                  onClick={handleGoToLogin}
                  className="w-full bg-white/10 hover:bg-white/20 text-white font-semibold py-3 px-6 rounded-xl transition-all duration-300 border border-white/20"
                >
                  Go to Login
                </button>
                
                <button
                  onClick={handleGoHome}
                  className="w-full bg-white/5 hover:bg-white/10 text-white font-semibold py-3 px-6 rounded-xl transition-all duration-300"
                >
                  <ArrowLeft className="w-4 h-4 inline mr-2" />
                  Back to Home
                </button>
              </div>
            </>
          )}

          {/* Footer */}
          <div className="mt-8 text-center">
            <p className="text-violet-300 text-sm">
              Need help? Contact our{' '}
              <a href="mailto:support@turismapp.com" className="text-violet-200 hover:text-white transition-colors">
                support team
              </a>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EmailConfirmationPage;
