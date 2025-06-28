import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  BarChart3, 
  Home, 
  Calendar, 
  TrendingUp, 
  Users, 
  DollarSign,
  Plus,
  Eye,
  Settings
} from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';
import ProfitChart from '../components/ProfitChart';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import api from '../services/axios';

const DashboardPage = () => {
  const navigate = useNavigate();
  // const { } = useAuth(); // Removed as not needed
  const { success, error: showError } = useToast();
  const [dashboardData, setDashboardData] = useState({
    totalUnits: 0,
    totalBookings: 0,
    monthlyRevenue: 0,
    occupancyRate: 0,
    averageRating: 0,
    guestSatisfaction: 0,
    revenueGrowth: 0,
    recentBookings: [],
    topUnits: []
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      
      // Fetch dashboard stats from our backend
      const statsResponse = await api.get('/api/dashboard/stats');
      const stats = statsResponse.data || {};
      
      // Fetch dashboard insights from our backend
      const insightsResponse = await api.get('/api/dashboard/insights');
      const insights = insightsResponse.data || {};
      
      // Fetch owner's units to get unit details
      const unitsResponse = await api.get('/api/accommodation-units/my-units');
      const ownerUnits = unitsResponse.data || [];
      
      // Fetch recent bookings for owner's properties
      const bookingsResponse = await api.get('/api/bookings/my-bookings');
      const allOwnerBookings = bookingsResponse.data || [];
      
      // Get recent bookings (last 5)
      const recentBookings = allOwnerBookings
        .sort((a, b) => new Date(b.checkInDate) - new Date(a.checkInDate))
        .slice(0, 5);
      
      setDashboardData({
        totalUnits: stats.totalUnits || 0,
        totalBookings: stats.totalBookings || 0,
        monthlyRevenue: stats.totalRevenue || 0,
        occupancyRate: insights.occupancyRate || 0,
        recentBookings: recentBookings,
        topUnits: ownerUnits.slice(0, 3), // Show top 3 units
        averageRating: stats.averageRating || 0,
        guestSatisfaction: insights.guestSatisfaction || 0,
        revenueGrowth: insights.revenueGrowth || 0
      });
      
      success('Dashboard Loaded', 'Your dashboard data has been updated');
    } catch (error) {
      console.error('Dashboard fetch error:', error);
      showError('Failed to Load Dashboard', error.response?.data?.message || 'Could not fetch dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const StatCard = ({ title, value, subtitle, icon: Icon, color = "violet" }) => (
    <GlassCard className="p-6 hover:bg-white/15 transition-all duration-300">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-violet-200 text-sm font-medium">{title}</p>
          <p className="text-3xl font-bold text-white mt-1">{value}</p>
          {subtitle && (
            <p className="text-violet-300 text-sm mt-1">{subtitle}</p>
          )}
        </div>
        <div className={`p-3 rounded-full bg-gradient-to-r from-${color}-500 to-${color}-600`}>
          <Icon className="w-6 h-6 text-white" />
        </div>
      </div>
    </GlassCard>
  );

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-violet-400"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-white mb-2">Dashboard</h1>
          <p className="text-violet-200">Welcome back! Here's an overview of your tourism business.</p>
        </div>

        {/* Quick Actions */}
        <div className="flex flex-wrap gap-4 mb-8">
          <PrimaryButton
            onClick={() => navigate('/my-units')}
            className="flex items-center gap-2"
          >
            <Plus className="w-4 h-4" />
            Add New Unit
          </PrimaryButton>
          <PrimaryButton
            onClick={() => navigate('/bookings')}
            variant="secondary"
            className="flex items-center gap-2"
          >
            <Eye className="w-4 h-4" />
            View All Bookings
          </PrimaryButton>
          <PrimaryButton
            onClick={() => navigate('/profit')}
            variant="secondary"
            className="flex items-center gap-2"
          >
            <BarChart3 className="w-4 h-4" />
            Analytics
          </PrimaryButton>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <StatCard
            title="Total Units"
            value={dashboardData.totalUnits}
            subtitle="Active properties"
            icon={Home}
            color="violet"
          />
          <StatCard
            title="Total Bookings"
            value={dashboardData.totalBookings}
            subtitle="All time"
            icon={Calendar}
            color="blue"
          />
          <StatCard
            title="Total Revenue"
            value={`$${dashboardData.monthlyRevenue.toLocaleString()}`}
            subtitle="All time earnings"
            icon={DollarSign}
            color="green"
          />
          <StatCard
            title="Occupancy Rate"
            value={`${parseFloat(dashboardData.occupancyRate).toFixed(1)}%`}
            subtitle="Current month"
            icon={TrendingUp}
            color="indigo"
          />
        </div>

        {/* Additional Insights Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <StatCard
            title="Average Rating"
            value={`${parseFloat(dashboardData.averageRating).toFixed(1)}/5`}
            subtitle="Guest reviews"
            icon={Users}
            color="yellow"
          />
          <StatCard
            title="Guest Satisfaction"
            value={`${parseFloat(dashboardData.guestSatisfaction).toFixed(1)}%`}
            subtitle="Satisfaction score"
            icon={Users}
            color="pink"
          />
          <StatCard
            title="Revenue Growth"
            value={`${parseFloat(dashboardData.revenueGrowth).toFixed(1)}%`}
            subtitle="Monthly growth"
            icon={TrendingUp}
            color="emerald"
          />
        </div>

        {/* Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Recent Bookings */}
          <GlassCard className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-white">Recent Bookings</h2>
              <PrimaryButton
                onClick={() => navigate('/bookings')}
                variant="secondary"
                size="sm"
              >
                View All
              </PrimaryButton>
            </div>
            <div className="space-y-4">
              {dashboardData.recentBookings.length > 0 ? (
                dashboardData.recentBookings.map((booking) => (
                  <div key={booking.id} className="flex items-center justify-between p-4 rounded-lg bg-white/5 hover:bg-white/10 transition-colors">
                    <div>
                      <p className="font-medium text-white">{booking.guestName}</p>
                      <p className="text-sm text-violet-200">{booking.unitName}</p>
                      <p className="text-xs text-violet-300">Check-in: {booking.checkIn}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-bold text-green-400">${booking.amount}</p>
                    </div>
                  </div>
                ))
              ) : (
                <div className="text-center py-8">
                  <Calendar className="w-12 h-12 text-violet-400 mx-auto mb-3" />
                  <p className="text-violet-200">No recent bookings</p>
                  <p className="text-sm text-violet-300">Bookings will appear here once guests start booking your units</p>
                </div>
              )}
            </div>
          </GlassCard>

          {/* Top Performing Units */}
          <GlassCard className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-white">Top Performing Units</h2>
              <PrimaryButton
                onClick={() => navigate('/my-units')}
                variant="secondary"
                size="sm"
              >
                Manage Units
              </PrimaryButton>
            </div>
            <div className="space-y-4">
              {dashboardData.topUnits.length > 0 ? (
                dashboardData.topUnits.map((unit, index) => (
                  <div key={unit.id} className="flex items-center justify-between p-4 rounded-lg bg-white/5 hover:bg-white/10 transition-colors">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-gradient-to-r from-violet-500 to-purple-600 flex items-center justify-center text-white font-bold text-sm">
                        {index + 1}
                      </div>
                      <div>
                        <p className="font-medium text-white">{unit.name}</p>
                        <p className="text-sm text-violet-200">{unit.bookings} bookings</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="font-bold text-green-400">${unit.revenue.toLocaleString()}</p>
                    </div>
                  </div>
                ))
              ) : (
                <div className="text-center py-8">
                  <Home className="w-12 h-12 text-violet-400 mx-auto mb-3" />
                  <p className="text-violet-200">No units data available</p>
                  <p className="text-sm text-violet-300">Add accommodation units to see performance metrics</p>
                </div>
              )}
            </div>
          </GlassCard>
        </div>

        {/* Profit Chart */}
        <div className="mt-8">
          <ProfitChart />
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
