import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Home, Building, Calendar, TrendingUp, Search, Plus, Star, Users, Shield, Zap } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const HomePage = () => {
  const navigate = useNavigate();
  const { isAuthenticated, isGuest, isOwner, isAdmin } = useAuth();

  // Handlers for navigation with role preselection
  const handleExploreUnits = () => {
    if (!isAuthenticated) {
      navigate('/register?role=guest');
    } else {
      navigate('/units');
    }
  };

  const handleListProperty = () => {
    if (!isAuthenticated) {
      navigate('/register?role=owner');
    } else {
      navigate('/my-units');
    }
  };

  // Determine which buttons to show
  let heroButtons = null;
  if (isAuthenticated && isGuest()) {
    heroButtons = (
      <button
        onClick={handleExploreUnits}
        className="bg-white text-purple-900 px-8 py-4 text-lg rounded-xl font-medium hover:bg-gray-100 transition-all duration-300 transform hover:scale-105 flex items-center justify-center gap-2 mx-auto"
      >
        <Search className="w-5 h-5" />
        Explore Units
      </button>
    );
  } else if (isAuthenticated && isOwner()) {
    heroButtons = (
      <button
        onClick={handleListProperty}
        className="bg-white/10 text-white border border-white/30 px-8 py-4 text-lg rounded-xl font-medium hover:bg-white/20 transition-all duration-300 transform hover:scale-105 flex items-center justify-center gap-2 mx-auto"
      >
        <Plus className="w-5 h-5" />
        List Your Property
      </button>
    );
  } else {
    // Not logged in or admin: show both
    heroButtons = (
      <div className="flex flex-col sm:flex-row gap-4 justify-center">
        <button
          onClick={handleExploreUnits}
          className="bg-white text-purple-900 px-8 py-4 text-lg rounded-xl font-medium hover:bg-gray-100 transition-all duration-300 transform hover:scale-105 flex items-center justify-center gap-2"
        >
          <Search className="w-5 h-5" />
          Explore Units
        </button>
        <button
          onClick={handleListProperty}
          className="bg-white/10 text-white border border-white/30 px-8 py-4 text-lg rounded-xl font-medium hover:bg-white/20 transition-all duration-300 transform hover:scale-105 flex items-center justify-center gap-2"
        >
          <Plus className="w-5 h-5" />
          List Your Property
        </button>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <div className="flex items-center justify-center min-h-screen p-6">
        <div className="w-full max-w-6xl">
          <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-12 text-center">
            <h1 className="text-5xl md:text-6xl font-bold text-white mb-6">
              Discover Your Perfect
              <span className="block bg-gradient-to-r from-violet-400 via-purple-400 to-indigo-400 bg-clip-text text-transparent mt-2">
                Tourism Experience
              </span>
            </h1>
            
            <p className="text-xl text-violet-200 mb-8 max-w-3xl mx-auto">
              Find and book amazing accommodations, manage your properties, and track your success with our comprehensive tourism management platform.
            </p>

            {/* Role-based hero buttons */}
            {heroButtons}
          </div>
        </div>
      </div>

      {/* Features Section */}
      <div className="px-6 pb-20">
        <div className="max-w-6xl mx-auto">
          <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-8 mb-12">
            <h2 className="text-4xl font-bold text-center text-white mb-4">
              Everything You Need
            </h2>
            <p className="text-center text-violet-200 text-lg mb-8">
              Powerful tools for both travelers and property owners
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-6 text-center hover:scale-105 transition-transform duration-300">
              <div className="w-16 h-16 mx-auto mb-4 p-4 rounded-xl bg-violet-500/20">
                <Home className="w-full h-full text-violet-400" />
              </div>
              <h3 className="text-lg font-semibold text-white mb-2">Find Accommodations</h3>
              <p className="text-violet-200 text-sm">Browse through thousands of verified accommodations</p>
            </div>

            <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-6 text-center hover:scale-105 transition-transform duration-300">
              <div className="w-16 h-16 mx-auto mb-4 p-4 rounded-xl bg-indigo-500/20">
                <Building className="w-full h-full text-indigo-400" />
              </div>
              <h3 className="text-lg font-semibold text-white mb-2">Manage Properties</h3>
              <p className="text-violet-200 text-sm">Easily manage your accommodation units and bookings</p>
            </div>

            <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-6 text-center hover:scale-105 transition-transform duration-300">
              <div className="w-16 h-16 mx-auto mb-4 p-4 rounded-xl bg-blue-500/20">
                <Calendar className="w-full h-full text-blue-400" />
              </div>
              <h3 className="text-lg font-semibold text-white mb-2">Smart Booking</h3>
              <p className="text-violet-200 text-sm">Advanced booking system with availability tracking</p>
            </div>

            <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-6 text-center hover:scale-105 transition-transform duration-300">
              <div className="w-16 h-16 mx-auto mb-4 p-4 rounded-xl bg-purple-500/20">
                <TrendingUp className="w-full h-full text-purple-400" />
              </div>
              <h3 className="text-lg font-semibold text-white mb-2">Analytics & Insights</h3>
              <p className="text-violet-200 text-sm">Track performance and predict future profits</p>
            </div>
          </div>
        </div>
      </div>

      {/* Stats Section */}
      <div className="px-6 pb-20">
        <div className="max-w-6xl mx-auto">
          <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-8">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-center">
              <div>
                <div className="text-4xl font-bold text-white mb-2">10,000+</div>
                <div className="text-violet-200">Properties Listed</div>
              </div>
              <div>
                <div className="text-4xl font-bold text-white mb-2">50,000+</div>
                <div className="text-violet-200">Happy Travelers</div>
              </div>
              <div>
                <div className="text-4xl font-bold text-white mb-2">95%</div>
                <div className="text-violet-200">Satisfaction Rate</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* CTA Section */}
      <div className="px-6 pb-20">
        <div className="max-w-4xl mx-auto">
          <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-12 text-center">
            <h2 className="text-3xl font-bold text-white mb-4">
              Ready to Get Started?
            </h2>
            <p className="text-violet-200 text-lg mb-8">
              Join thousands of property owners and travelers who trust our platform.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button
                onClick={() => navigate('/register')}
                className="bg-white text-purple-900 px-8 py-4 text-lg rounded-xl font-medium hover:bg-gray-100 transition-all duration-300 transform hover:scale-105 flex items-center justify-center gap-2"
              >
                <Plus className="w-5 h-5" />
                Get Started Free
              </button>
              <button
                onClick={() => navigate('/login')}
                className="bg-white/10 text-white border border-white/30 px-8 py-4 text-lg rounded-xl font-medium hover:bg-white/20 transition-all duration-300 transform hover:scale-105"
              >
                Sign In
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
