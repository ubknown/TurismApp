import React from 'react';
import { useNavigate } from 'react-router-dom';

// Test component to isolate issues
const TestHomePage = () => {
  const navigate = useNavigate();

  // Simple test without any custom components first
  const handleNavigate = (path) => {
    try {
      console.log('Navigating to:', path);
      navigate(path);
    } catch (error) {
      console.error('Navigation error:', error);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-900 via-purple-900 to-pink-900 flex items-center justify-center p-6">
      <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl p-8 shadow-xl max-w-4xl w-full text-center">
        <h1 className="text-5xl font-bold text-white mb-6">
          Tourism Management
        </h1>
        
        <p className="text-violet-200 text-xl mb-8">
          Welcome to your glassmorphism app!
        </p>

        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <button
            onClick={() => handleNavigate('/units')}
            className="bg-white text-purple-900 px-6 py-3 rounded-xl font-medium hover:bg-white/90 transition-colors"
          >
            Explore Units
          </button>
          <button
            onClick={() => handleNavigate('/register')}
            className="bg-white/10 text-white border border-white/30 px-6 py-3 rounded-xl font-medium hover:bg-white/20 transition-colors"
          >
            List Your Property
          </button>
        </div>
      </div>
    </div>
  );
};

export default TestHomePage;
