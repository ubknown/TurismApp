import React, { useState, useEffect } from 'react';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  LineChart,
  Line
} from 'recharts';
import { TrendingUp, DollarSign, Calendar, Building2 } from 'lucide-react';
import GlassCard from './GlassCard';
import PrimaryButton from './PrimaryButton';
import { useToast } from '../context/ToastContext';
import api from '../services/axios';

const ProfitDashboard = () => {
  const { success, error: showError } = useToast();
  const [profitData, setProfitData] = useState(null);
  const [selectedPeriod, setSelectedPeriod] = useState(12);
  const [loading, setLoading] = useState(true);
  const [chartType, setChartType] = useState('bar'); // 'bar' or 'line'

  const periodOptions = [
    { value: 1, label: '1 Month' },
    { value: 3, label: '3 Months' },
    { value: 6, label: '6 Months' },
    { value: 9, label: '9 Months' },
    { value: 12, label: '1 Year' },
    { value: 24, label: '2 Years' }
  ];

  useEffect(() => {
    fetchProfitData();
  }, [selectedPeriod]);

  const fetchProfitData = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/api/units/my-units/profit/analytics?months=${selectedPeriod}`);
      
      // Transform data for chart
      const chartData = Object.entries(response.data).map(([month, profit]) => ({
        month: formatMonth(month),
        profit: profit,
        formattedProfit: `${profit.toFixed(2)} RON`
      }));
      
      setProfitData(chartData);
      
    } catch (error) {
      showError('Failed to Load Data', error.response?.data?.message || 'Could not fetch profit analytics');
    } finally {
      setLoading(false);
    }
  };

  const fetchProfitSummary = async () => {
    try {
      const response = await api.get('/api/units/my-units/profit/summary');
      return response.data;
    } catch (error) {
      showError('Failed to Load Summary', error.response?.data?.message || 'Could not fetch profit summary');
      return null;
    }
  };

  const formatMonth = (monthString) => {
    const [year, month] = monthString.split('-');
    const date = new Date(parseInt(year), parseInt(month) - 1);
    return date.toLocaleDateString('en-US', { month: 'short', year: '2-digit' });
  };

  const getTotalProfit = () => {
    if (!profitData) return 0;
    return profitData.reduce((sum, item) => sum + item.profit, 0);
  };

  const getAverageProfit = () => {
    if (!profitData || profitData.length === 0) return 0;
    return getTotalProfit() / profitData.length;
  };

  const getBestMonth = () => {
    if (!profitData || profitData.length === 0) return null;
    return profitData.reduce((best, current) => 
      current.profit > best.profit ? current : best
    );
  };

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-slate-800 border border-violet-500/30 rounded-lg p-3 shadow-lg">
          <p className="text-white font-medium">{label}</p>
          <p className="text-violet-400">
            Profit: <span className="text-white font-bold">{payload[0].value.toFixed(2)} RON</span>
          </p>
        </div>
      );
    }
    return null;
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-violet-400"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen p-6">
      <div className="max-w-7xl mx-auto space-y-8">
        {/* Header */}
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <h1 className="text-4xl font-bold text-white mb-2 flex items-center gap-3">
              <TrendingUp className="w-8 h-8 text-violet-400" />
              Profit Analytics
            </h1>
            <p className="text-violet-200">Track your property earnings and performance over time.</p>
          </div>
          
          <div className="flex flex-col sm:flex-row gap-3">
            {/* Period Selector */}
            <select
              value={selectedPeriod}
              onChange={(e) => setSelectedPeriod(parseInt(e.target.value))}
              className="px-4 py-2 bg-white/10 border border-white/20 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-violet-500/50"
            >
              {periodOptions.map(option => (
                <option key={option.value} value={option.value} className="bg-slate-800 text-white">
                  {option.label}
                </option>
              ))}
            </select>
            
            {/* Chart Type Toggle */}
            <div className="flex bg-white/10 rounded-xl p-1">
              <button
                onClick={() => setChartType('bar')}
                className={`px-3 py-1 rounded-lg text-sm font-medium transition-all ${
                  chartType === 'bar' 
                    ? 'bg-violet-500 text-white' 
                    : 'text-violet-200 hover:text-white'
                }`}
              >
                Bar Chart
              </button>
              <button
                onClick={() => setChartType('line')}
                className={`px-3 py-1 rounded-lg text-sm font-medium transition-all ${
                  chartType === 'line' 
                    ? 'bg-violet-500 text-white' 
                    : 'text-violet-200 hover:text-white'
                }`}
              >
                Line Chart
              </button>
            </div>
          </div>
        </div>

        {/* Summary Stats */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <GlassCard className="p-6 text-center">
            <div className="w-12 h-12 bg-green-500/20 rounded-xl flex items-center justify-center mx-auto mb-4">
              <DollarSign className="w-6 h-6 text-green-400" />
            </div>
            <h3 className="text-2xl font-bold text-white mb-1">
              {getTotalProfit().toFixed(2)} RON
            </h3>
            <p className="text-violet-200">Total Profit ({selectedPeriod}m)</p>
          </GlassCard>

          <GlassCard className="p-6 text-center">
            <div className="w-12 h-12 bg-blue-500/20 rounded-xl flex items-center justify-center mx-auto mb-4">
              <Calendar className="w-6 h-6 text-blue-400" />
            </div>
            <h3 className="text-2xl font-bold text-white mb-1">
              {getAverageProfit().toFixed(2)} RON
            </h3>
            <p className="text-violet-200">Average per Month</p>
          </GlassCard>

          <GlassCard className="p-6 text-center">
            <div className="w-12 h-12 bg-violet-500/20 rounded-xl flex items-center justify-center mx-auto mb-4">
              <TrendingUp className="w-6 h-6 text-violet-400" />
            </div>
            <h3 className="text-2xl font-bold text-white mb-1">
              {getBestMonth()?.formattedProfit || '0 RON'}
            </h3>
            <p className="text-violet-200">Best Month ({getBestMonth()?.month || 'N/A'})</p>
          </GlassCard>

          <GlassCard className="p-6 text-center">
            <div className="w-12 h-12 bg-orange-500/20 rounded-xl flex items-center justify-center mx-auto mb-4">
              <Building2 className="w-6 h-6 text-orange-400" />
            </div>
            <h3 className="text-2xl font-bold text-white mb-1">
              {profitData?.length || 0}
            </h3>
            <p className="text-violet-200">Active Months</p>
          </GlassCard>
        </div>

        {/* Profit Chart */}
        <GlassCard className="p-8">
          <div className="mb-6">
            <h2 className="text-2xl font-bold text-white mb-2">Profit Over Time</h2>
            <p className="text-violet-200">
              Earnings from your accommodation units over the last {selectedPeriod} months
            </p>
          </div>

          {profitData && profitData.length > 0 ? (
            <ResponsiveContainer width="100%" height={400}>
              {chartType === 'bar' ? (
                <BarChart data={profitData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                  <XAxis 
                    dataKey="month" 
                    stroke="#9CA3AF"
                    fontSize={12}
                  />
                  <YAxis 
                    stroke="#9CA3AF"
                    fontSize={12}
                    tickFormatter={(value) => `${value} RON`}
                  />
                  <Tooltip content={<CustomTooltip />} />
                  <Bar 
                    dataKey="profit" 
                    fill="url(#profitGradient)"
                    radius={[4, 4, 0, 0]}
                  />
                  <defs>
                    <linearGradient id="profitGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#8B5CF6" />
                      <stop offset="100%" stopColor="#A855F7" />
                    </linearGradient>
                  </defs>
                </BarChart>
              ) : (
                <LineChart data={profitData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                  <XAxis 
                    dataKey="month" 
                    stroke="#9CA3AF"
                    fontSize={12}
                  />
                  <YAxis 
                    stroke="#9CA3AF"
                    fontSize={12}
                    tickFormatter={(value) => `${value} RON`}
                  />
                  <Tooltip content={<CustomTooltip />} />
                  <Line 
                    type="monotone" 
                    dataKey="profit" 
                    stroke="#8B5CF6"
                    strokeWidth={3}
                    dot={{ fill: '#A855F7', strokeWidth: 2, r: 6 }}
                    activeDot={{ r: 8, stroke: '#8B5CF6', strokeWidth: 2 }}
                  />
                </LineChart>
              )}
            </ResponsiveContainer>
          ) : (
            <div className="text-center py-12">
              <div className="w-16 h-16 bg-white/10 rounded-full flex items-center justify-center mx-auto mb-4">
                <TrendingUp className="w-8 h-8 text-violet-400" />
              </div>
              <h3 className="text-xl font-semibold text-white mb-2">No Profit Data</h3>
              <p className="text-violet-200 mb-4">
                No earnings data available for the selected period.
              </p>
              <PrimaryButton onClick={() => window.location.href = '/my-units'}>
                View My Properties
              </PrimaryButton>
            </div>
          )}
        </GlassCard>

        {/* Additional Actions */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <PrimaryButton 
            onClick={() => window.location.href = '/my-units'}
            className="flex items-center gap-2"
          >
            <Building2 className="w-5 h-5" />
            Manage Properties
          </PrimaryButton>
          <PrimaryButton 
            onClick={fetchProfitData}
            variant="secondary"
            className="flex items-center gap-2"
          >
            <TrendingUp className="w-5 h-5" />
            Refresh Data
          </PrimaryButton>
        </div>
      </div>
    </div>
  );
};

export default ProfitDashboard;
