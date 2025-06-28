import React, { useState, useEffect } from 'react';
import { BarChart3, TrendingUp, DollarSign, Calendar } from 'lucide-react';
import { useToast } from '../context/ToastContext';
import api from '../services/axios';

const ProfitChart = () => {
  const { error: showError } = useToast();
  const [profitData, setProfitData] = useState({
    monthly: [],
    summary: {
      totalRevenue: 0,
      averageMonthly: 0,
      growth: 0,
      bestMonth: null
    }
  });
  const [loading, setLoading] = useState(true);
  const [timeRange, setTimeRange] = useState('12months'); // 6months, 12months, 2years

  useEffect(() => {
    fetchProfitData();
  }, [timeRange]);

  const fetchProfitData = async () => {
    try {
      setLoading(true);
      
      // Map timeRange to months parameter
      const monthsParam = timeRange === '6months' ? 6 : timeRange === '12months' ? 12 : 24;
      
      // Use our backend's profit endpoints
      const response = await api.get(`/api/profit/monthly?months=${monthsParam}`);
      
      const monthlyData = response.data || [];
      
      // Transform data to match component expectations
      const transformedData = monthlyData.map(item => ({
        month: item.month || 'Unknown',
        year: new Date().getFullYear(), // Default to current year
        profit: item.totalProfit || 0,
        revenue: item.totalProfit || 0, // Profit is our revenue
        bookings: 0 // We don't have booking count in this endpoint
      }));
      
      // Calculate summary statistics
      const totalRevenue = transformedData.reduce((sum, item) => sum + item.profit, 0);
      const averageMonthly = transformedData.length > 0 ? totalRevenue / transformedData.length : 0;
      const bestMonth = transformedData.length > 0 
        ? transformedData.reduce((best, current) => current.profit > best.profit ? current : best)
        : null;
      
      // Calculate growth (compare last 2 months if available)
      let growth = 0;
      if (transformedData.length >= 2) {
        const lastMonth = transformedData[transformedData.length - 1].profit;
        const previousMonth = transformedData[transformedData.length - 2].profit;
        growth = previousMonth > 0 ? ((lastMonth - previousMonth) / previousMonth) * 100 : 0;
      }
      
      setProfitData({
        monthly: transformedData,
        summary: {
          totalRevenue,
          averageMonthly,
          growth,
          bestMonth
        }
      });
    } catch (error) {
      console.error('Profit data fetch error:', error);
      showError('Failed to Load Profit Data', error.response?.data?.message || 'Could not fetch profit statistics');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0
    }).format(amount);
  };

  const formatMonth = (month) => {
    return new Date(month + '-01').toLocaleDateString('en-US', { 
      month: 'short', 
      year: 'numeric' 
    });
  };

  const getBarHeight = (value, maxValue) => {
    if (maxValue === 0) return 0;
    return Math.max((value / maxValue) * 100, 2); // Minimum 2% height for visibility
  };

  const maxRevenue = Math.max(...profitData.monthly.map(item => item.revenue || 0));

  if (loading) {
    return (
      <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl p-6">
        <div className="animate-pulse">
          <div className="h-6 bg-white/20 rounded mb-4"></div>
          <div className="h-48 bg-white/10 rounded"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <BarChart3 className="w-6 h-6 text-violet-400" />
          <h3 className="text-xl font-bold text-white">Profit Analytics</h3>
        </div>
        
        <select
          value={timeRange}
          onChange={(e) => setTimeRange(e.target.value)}
          className="glass-input py-2 px-3 text-sm"
        >
          <option value="6months">Last 6 Months</option>
          <option value="12months">Last 12 Months</option>
          <option value="2years">Last 2 Years</option>
        </select>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <div className="bg-white/5 backdrop-blur-md border border-white/10 rounded-xl p-4">
          <div className="flex items-center gap-2 text-violet-300 text-sm mb-1">
            <DollarSign className="w-4 h-4" />
            Total Revenue
          </div>
          <div className="text-xl font-bold text-white">
            {formatCurrency(profitData.summary.totalRevenue)}
          </div>
        </div>

        <div className="bg-white/5 backdrop-blur-md border border-white/10 rounded-xl p-4">
          <div className="flex items-center gap-2 text-violet-300 text-sm mb-1">
            <Calendar className="w-4 h-4" />
            Monthly Avg
          </div>
          <div className="text-xl font-bold text-white">
            {formatCurrency(profitData.summary.averageMonthly)}
          </div>
        </div>

        <div className="bg-white/5 backdrop-blur-md border border-white/10 rounded-xl p-4">
          <div className="flex items-center gap-2 text-violet-300 text-sm mb-1">
            <TrendingUp className="w-4 h-4" />
            Growth
          </div>
          <div className={`text-xl font-bold ${profitData.summary.growth >= 0 ? 'text-green-400' : 'text-red-400'}`}>
            {profitData.summary.growth >= 0 ? '+' : ''}{profitData.summary.growth.toFixed(1)}%
          </div>
        </div>

        <div className="bg-white/5 backdrop-blur-md border border-white/10 rounded-xl p-4">
          <div className="flex items-center gap-2 text-violet-300 text-sm mb-1">
            <BarChart3 className="w-4 h-4" />
            Best Month
          </div>
          <div className="text-sm font-semibold text-white">
            {profitData.summary.bestMonth ? formatMonth(profitData.summary.bestMonth) : 'N/A'}
          </div>
        </div>
      </div>

      {/* Chart */}
      <div className="relative">
        <div className="h-64 flex items-end justify-between gap-2 px-2">
          {profitData.monthly.length > 0 ? (
            profitData.monthly.map((monthData, index) => (
              <div key={index} className="flex-1 flex flex-col items-center group">
                {/* Bar */}
                <div className="relative w-full mb-2">
                  <div
                    className="w-full bg-gradient-to-t from-violet-600 to-violet-400 rounded-t-lg transition-all duration-300 group-hover:from-violet-500 group-hover:to-violet-300"
                    style={{ height: `${getBarHeight(monthData.revenue, maxRevenue)}%` }}
                  />
                  
                  {/* Tooltip */}
                  <div className="absolute -top-8 left-1/2 transform -translate-x-1/2 bg-black/80 text-white text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity duration-200 whitespace-nowrap z-10">
                    {formatCurrency(monthData.revenue)}
                  </div>
                </div>
                
                {/* Month Label */}
                <div className="text-xs text-violet-300 transform -rotate-45 origin-left whitespace-nowrap">
                  {formatMonth(monthData.month)}
                </div>
              </div>
            ))
          ) : (
            <div className="w-full h-full flex items-center justify-center">
              <p className="text-violet-300 text-center">
                No profit data available for the selected time range
              </p>
            </div>
          )}
        </div>
        
        {/* Y-axis labels */}
        {maxRevenue > 0 && (
          <div className="absolute left-0 top-0 h-64 flex flex-col justify-between text-xs text-violet-300 -ml-12">
            <span>{formatCurrency(maxRevenue)}</span>
            <span>{formatCurrency(maxRevenue * 0.75)}</span>
            <span>{formatCurrency(maxRevenue * 0.5)}</span>
            <span>{formatCurrency(maxRevenue * 0.25)}</span>
            <span>$0</span>
          </div>
        )}
      </div>

      {/* Refresh Button */}
      <div className="mt-4 flex justify-end">
        <button
          onClick={fetchProfitData}
          disabled={loading}
          className="glass-button py-2 px-4 text-sm disabled:opacity-50"
        >
          {loading ? 'Refreshing...' : 'Refresh Data'}
        </button>
      </div>
    </div>
  );
};

export default ProfitChart;
