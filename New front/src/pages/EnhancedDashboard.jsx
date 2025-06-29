import React, { useEffect, useState } from "react";
import { useNavigate } from 'react-router-dom';
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { 
  TrendingUpIcon, 
  CalendarIcon, 
  HomeIcon, 
  UsersIcon,
  EyeIcon,
  StarIcon
} from "lucide-react";
import api from "../services/axios";
import EnhancedProfitChart from "../components/EnhancedProfitChart";

const EnhancedDashboard = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { showToast } = useToast();
  const [dashboardData, setDashboardData] = useState({
    stats: {
      totalUnits: 0,
      totalBookings: 0,
      totalRevenue: 0,
      averageRating: 0,
    },
    recentBookings: [],
    loading: true,
  });

  useEffect(() => {
    if (user) {
      fetchDashboardData();
    }
  }, [user]);

  const fetchDashboardData = async () => {
    try {
      const token = localStorage.getItem("token");
      if (!token) return;

      const [statsRes, bookingsRes] = await Promise.all([
        api.get("/api/dashboard/stats").catch(() => ({ data: { totalUnits: 0, totalBookings: 0, totalRevenue: 0, averageRating: 0 } })),
        api.get("/api/bookings/recent?limit=5").catch(() => ({ data: [] })),
      ]);

      setDashboardData({
        stats: statsRes.data,
        recentBookings: bookingsRes.data,
        loading: false,
      });
    } catch (error) {
      console.error("Error fetching dashboard data:", error);
      setDashboardData(prev => ({ ...prev, loading: false }));
      showToast("Failed to load dashboard data", "error");
    }
  };

  const StatCard = ({ icon: Icon, title, value, change, color = "blue" }) => (
    <div className="bg-white/10 backdrop-blur-xl rounded-2xl p-6 border border-white/20 hover:bg-white/15 transition-all duration-300">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-white/70 text-sm font-medium">{title}</p>
          <p className="text-2xl font-bold text-white mt-1">{value}</p>
          {change && (
            <div className={`flex items-center mt-2 text-sm ${
              change >= 0 ? 'text-green-400' : 'text-red-400'
            }`}>
              <TrendingUpIcon className={`w-4 h-4 mr-1 ${change < 0 ? 'rotate-180' : ''}`} />
              {Math.abs(change)}% vs last month
            </div>
          )}
        </div>
        <div className={`w-12 h-12 rounded-xl bg-gradient-to-r from-${color}-500 to-${color}-600 flex items-center justify-center shadow-lg`}>
          <Icon className="w-6 h-6 text-white" />
        </div>
      </div>
    </div>
  );

  const RecentBookingCard = ({ booking }) => (
    <div className="bg-white/5 rounded-lg p-4 border border-white/10 hover:bg-white/10 transition-all duration-300">
      <div className="flex justify-between items-start">
        <div className="flex-1">
          <h4 className="text-white font-semibold text-sm">{booking.guestName || booking.guestEmail}</h4>
          <p className="text-white/70 text-xs mt-1">{booking.accommodationUnit?.name || 'Property'}</p>
          <div className="flex items-center gap-4 mt-2 text-xs text-white/60">
            <span>📅 {new Date(booking.checkInDate).toLocaleDateString()}</span>
            <span>💰 {booking.totalPrice?.toLocaleString('ro-RO', { style: 'currency', currency: 'RON' }) || 'N/A'}</span>
          </div>
        </div>
        <div className={`px-2 py-1 rounded text-xs font-medium ${
          booking.status === 'confirmed' 
            ? 'bg-green-500/20 text-green-400' 
            : booking.status === 'pending'
            ? 'bg-yellow-500/20 text-yellow-400'
            : 'bg-gray-500/20 text-gray-400'
        }`}>
          {booking.status || 'Confirmed'}
        </div>
      </div>
    </div>
  );

  if (dashboardData.loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-purple-900 via-blue-900 to-indigo-900 p-6">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Loading Header */}
          <div className="animate-pulse">
            <div className="h-8 bg-white/20 rounded w-64 mb-2"></div>
            <div className="h-4 bg-white/10 rounded w-96"></div>
          </div>
          
          {/* Loading Stats */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {[...Array(4)].map((_, i) => (
              <div key={i} className="bg-white/10 backdrop-blur-xl rounded-2xl p-6 border border-white/20 animate-pulse">
                <div className="h-4 bg-white/20 rounded w-20 mb-4"></div>
                <div className="h-8 bg-white/20 rounded w-16"></div>
              </div>
            ))}
          </div>
          
          {/* Loading Chart */}
          <div className="bg-white/10 backdrop-blur-xl rounded-2xl p-6 border border-white/20 animate-pulse">
            <div className="h-80 bg-white/10 rounded"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-900 via-blue-900 to-indigo-900">
      {/* Background Elements */}
      <div className="fixed inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-purple-500 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob"></div>
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-blue-500 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-2000"></div>
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-indigo-500 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-4000"></div>
      </div>

      <div className="relative z-10 p-6">
        <div className="max-w-7xl mx-auto space-y-8">
          {/* Header */}
          <div className="text-center md:text-left">
            <h1 className="text-4xl md:text-5xl font-bold text-white mb-4">
              Welcome back, {user?.firstName || user?.name || 'Guest'}! 
            </h1>
            <p className="text-xl text-white/80">
              Here's what's happening with your properties today
            </p>
          </div>

          {/* Stats Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <StatCard
              icon={HomeIcon}
              title="Total Properties"
              value={dashboardData.stats.totalUnits}
              change={12}
              color="blue"
            />
            <StatCard
              icon={CalendarIcon}
              title="Total Bookings"
              value={dashboardData.stats.totalBookings}
              change={8}
              color="green"
            />
            <StatCard
              icon={TrendingUpIcon}
              title="Total Revenue"
              value={`${dashboardData.stats.totalRevenue.toLocaleString('ro-RO', { 
                style: 'currency', 
                currency: 'RON',
                minimumFractionDigits: 0,
              })}`}
              change={15}
              color="purple"
            />
            <StatCard
              icon={StarIcon}
              title="Average Rating"
              value={`${dashboardData.stats.averageRating.toFixed(1)} ⭐`}
              change={5}
              color="yellow"
            />
          </div>

          {/* Main Content Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Profit Chart - Takes 2 columns */}
            <div className="lg:col-span-2">
              <EnhancedProfitChart />
            </div>

            {/* Recent Bookings */}
            <div className="bg-white/10 backdrop-blur-xl rounded-2xl p-6 border border-white/20">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-xl font-bold text-white">Recent Bookings</h3>
                <EyeIcon className="w-5 h-5 text-white/60" />
              </div>
              
              <div className="space-y-4">
                {dashboardData.recentBookings.length > 0 ? (
                  dashboardData.recentBookings.map((booking, index) => (
                    <RecentBookingCard key={index} booking={booking} />
                  ))
                ) : (
                  <div className="text-center py-8">
                    <CalendarIcon className="w-12 h-12 text-white/40 mx-auto mb-4" />
                    <p className="text-white/60">No recent bookings</p>
                    <p className="text-white/40 text-sm">Bookings will appear here</p>
                  </div>
                )}
              </div>

              {dashboardData.recentBookings.length > 0 && (
                <button className="w-full mt-4 px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-lg transition-colors border border-white/20">
                  View All Bookings
                </button>
              )}
            </div>
          </div>

          {/* Quick Actions */}
          <div className="bg-white/10 backdrop-blur-xl rounded-2xl p-6 border border-white/20">
            <h3 className="text-xl font-bold text-white mb-6">Quick Actions</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <button 
                onClick={() => navigate('/add-property')}
                className="flex items-center gap-3 p-4 bg-white/5 hover:bg-white/15 rounded-lg transition-all duration-300 border border-white/10 group">
                <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-blue-600 rounded-lg flex items-center justify-center group-hover:scale-110 transition-transform">
                  <HomeIcon className="w-5 h-5 text-white" />
                </div>
                <div className="text-left">
                  <div className="text-white font-medium">Add Property</div>
                  <div className="text-white/60 text-sm">List a new accommodation</div>
                </div>
              </button>

              <button className="flex items-center gap-3 p-4 bg-white/5 hover:bg-white/15 rounded-lg transition-all duration-300 border border-white/10 group">
                <div className="w-10 h-10 bg-gradient-to-r from-green-500 to-green-600 rounded-lg flex items-center justify-center group-hover:scale-110 transition-transform">
                  <CalendarIcon className="w-5 h-5 text-white" />
                </div>
                <div className="text-left">
                  <div className="text-white font-medium">Manage Bookings</div>
                  <div className="text-white/60 text-sm">View and update reservations</div>
                </div>
              </button>

              <button className="flex items-center gap-3 p-4 bg-white/5 hover:bg-white/15 rounded-lg transition-all duration-300 border border-white/10 group">
                <div className="w-10 h-10 bg-gradient-to-r from-purple-500 to-purple-600 rounded-lg flex items-center justify-center group-hover:scale-110 transition-transform">
                  <TrendingUpIcon className="w-5 h-5 text-white" />
                </div>
                <div className="text-left">
                  <div className="text-white font-medium">View Analytics</div>
                  <div className="text-white/60 text-sm">Detailed performance reports</div>
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EnhancedDashboard;
