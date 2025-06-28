import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Home, ArrowLeft, Search } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

const NotFoundPage = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="w-full max-w-md text-center">
        <GlassCard className="p-8">
          {/* 404 Illustration */}
          <div className="mb-8">
            <div className="text-8xl font-bold text-white/20 mb-4">404</div>
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gradient-to-r from-violet-500 to-purple-600 mb-4">
              <Search className="w-8 h-8 text-white" />
            </div>
          </div>

          {/* Content */}
          <h1 className="text-3xl font-bold text-white mb-4">
            Page Not Found
          </h1>
          
          <p className="text-violet-200 mb-8">
            The page you're looking for doesn't exist or has been moved to a different location.
          </p>

          {/* Actions */}
          <div className="flex flex-col sm:flex-row gap-3">
            <PrimaryButton
              onClick={() => navigate(-1)}
              variant="secondary"
              className="flex items-center justify-center gap-2"
            >
              <ArrowLeft className="w-4 h-4" />
              Go Back
            </PrimaryButton>
            
            <Link to="/">
              <PrimaryButton className="w-full flex items-center justify-center gap-2">
                <Home className="w-4 h-4" />
                Go Home
              </PrimaryButton>
            </Link>
          </div>
        </GlassCard>

        {/* Quick Links */}
        <div className="mt-8">
          <p className="text-violet-300 text-sm mb-4">You might be looking for:</p>
          <div className="flex flex-wrap justify-center gap-2">
            <Link 
              to="/units" 
              className="text-violet-400 hover:text-violet-300 text-sm transition-colors"
            >
              Browse Units
            </Link>
            <span className="text-violet-500">•</span>
            <Link 
              to="/login" 
              className="text-violet-400 hover:text-violet-300 text-sm transition-colors"
            >
              Login
            </Link>
            <span className="text-violet-500">•</span>
            <Link 
              to="/register" 
              className="text-violet-400 hover:text-violet-300 text-sm transition-colors"
            >
              Register
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default NotFoundPage;
