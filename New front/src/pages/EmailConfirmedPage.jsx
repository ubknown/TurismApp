import React, { useEffect, useState } from 'react';
import { CheckCircle, XCircle } from 'lucide-react';
import { useSearchParams } from 'react-router-dom';

const EmailConfirmedPage = () => {
  const [searchParams] = useSearchParams();
  const [isSuccess, setIsSuccess] = useState(true);
  const [errorType, setErrorType] = useState('');

  useEffect(() => {
    // Clean up any lingering navigation state
    window.history.replaceState({}, document.title);

    // Check URL parameters for success/error status
    const success = searchParams.get('success');
    const error = searchParams.get('error');
    
    if (success === 'true') {
      setIsSuccess(true);
    } else if (error) {
      setIsSuccess(false);
      setErrorType(error);
    }
  }, [searchParams]);

  const getErrorMessage = (errorType) => {
    switch (errorType) {
      case 'invalid-token':
        return 'Link-ul de confirmare nu este valid!';
      case 'expired-token':
        return 'Link-ul de confirmare a expirat!';
      case 'already-confirmed':
        return 'Contul a fost deja confirmat!';
      case 'server-error':
        return 'A apărut o eroare! Încearcă din nou!';
      default:
        return 'A apărut o eroare neașteptată!';
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6">
      <div className="w-full max-w-md">
        <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-12 text-center relative">
          {/* Large animated icon */}
          <div className="mb-8">
            <div className="relative mx-auto w-20 h-20 mb-6">
              {isSuccess ? (
                <>
                  <CheckCircle className="w-full h-full text-green-400 animate-pulse" />
                  <div className="absolute inset-0 bg-green-400/20 rounded-full blur-xl animate-pulse" />
                </>
              ) : (
                <>
                  <XCircle className="w-full h-full text-red-400 animate-pulse" />
                  <div className="absolute inset-0 bg-red-400/20 rounded-full blur-xl animate-pulse" />
                </>
              )}
            </div>
          </div>

          {/* Romanian message */}
          <h1 className="text-3xl md:text-4xl font-bold text-white mb-4 leading-tight">
            {isSuccess 
              ? 'Înregistrarea a fost făcută cu succes!' 
              : getErrorMessage(errorType)
            }
          </h1>

          {/* Subtle glow effect around the card */}
          <div className={`absolute inset-0 rounded-2xl blur-xl -z-10 ${
            isSuccess 
              ? 'bg-gradient-to-r from-violet-400/5 via-green-400/5 to-indigo-400/5' 
              : 'bg-gradient-to-r from-red-400/5 via-orange-400/5 to-red-400/5'
          }`} />
        </div>
      </div>
    </div>
  );
};

export default EmailConfirmedPage;
