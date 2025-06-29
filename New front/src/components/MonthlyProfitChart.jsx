import React, { useState, useEffect } from 'react';
import { TrendingUp, Calendar, BarChart3 } from 'lucide-react';
import GlassCard from './GlassCard';
import { useToast } from '../context/ToastContext';
import api from '../services/axios';

const MonthlyProfitChart = ({ className = "" }) => {
  const [profitData, setProfitData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [months, setMonths] = useState(12);
  const { error: showError } = useToast();

  useEffect(() => {
    fetchProfitData();
  }, [months]);

  const fetchProfitData = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/api/units/my-units/profit/analytics?months=${months}`);
      
      // Convert response data to array format for chart
      const data = Object.entries(response.data).map(([month, profit]) => ({
        month,
        profit: Number(profit) || 0,
        formattedProfit: new Intl.NumberFormat('ro-RO', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2
        }).format(Number(profit) || 0)
      }));
      
      setProfitData(data);
    } catch (error) {
      console.error('Error fetching profit data:', error);
      showError('Error Loading Chart', 'Failed to load profit analytics data.');
    } finally {
      setLoading(false);
    }
  };

  const maxProfit = Math.max(...profitData.map(d => d.profit), 1);
  const totalProfit = profitData.reduce((sum, d) => sum + d.profit, 0);

  if (loading) {
    return (
      <GlassCard className={`p-6 ${className}`}>
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-violet-400"></div>
        </div>
      </GlassCard>
    );
  }

  return (
    <GlassCard className={`p-6 ${className}`}>
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
        <div>
          <h3 className="text-lg font-semibold text-white flex items-center gap-2">
            <BarChart3 className="w-5 h-5" />
            Monthly Profit Analytics
          </h3>
          <p className="text-violet-200 text-sm">
            Total: {new Intl.NumberFormat('ro-RO', { minimumFractionDigits: 2 }).format(totalProfit)} RON
          </p>
        </div>
        
        {/* Time Period Selector */}
        <div className="flex items-center gap-2">
          <Calendar className="w-4 h-4 text-violet-300" />
          <select
            value={months}
            onChange={(e) => setMonths(Number(e.target.value))}
            className="px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/50"
          >
            <option value={6} className="bg-slate-800">Last 6 Months</option>
            <option value={12} className="bg-slate-800">Last 12 Months</option>
            <option value={18} className="bg-slate-800">Last 18 Months</option>
            <option value={24} className="bg-slate-800">Last 24 Months</option>
          </select>
        </div>
      </div>

      {/* Chart */}
      <div className="space-y-3">
        {profitData.length > 0 ? (
          profitData.map((data, index) => (
            <div key={index} className="flex items-center gap-3">
              {/* Month Label */}
              <div className="w-20 text-sm text-violet-200 font-medium">
                {data.month}
              </div>
              
              {/* Bar */}
              <div className="flex-1 relative">
                <div className="w-full h-8 bg-white/10 rounded-lg overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-violet-500 to-purple-600 rounded-lg transition-all duration-1000 ease-out"
                    style={{
                      width: `${(data.profit / maxProfit) * 100}%`,
                      minWidth: data.profit > 0 ? '2px' : '0'
                    }}
                  ></div>
                </div>
                
                {/* Value Label */}
                <div className="absolute right-2 top-1/2 transform -translate-y-1/2 text-xs text-white font-medium">
                  {data.formattedProfit} RON
                </div>
              </div>
              
              {/* Trend Indicator */}
              <div className="w-8 flex justify-center">
                {index > 0 && profitData[index].profit > profitData[index - 1].profit && (
                  <TrendingUp className="w-4 h-4 text-green-400" />
                )}
              </div>
            </div>
          ))
        ) : (
          <div className="text-center py-8">
            <BarChart3 className="w-12 h-12 text-violet-400 mx-auto mb-3 opacity-50" />
            <p className="text-violet-200">No profit data available for the selected period.</p>
          </div>
        )}
      </div>

      {/* Summary Stats */}
      {profitData.length > 0 && (
        <div className="mt-6 pt-4 border-t border-white/20">
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 text-center">
            <div>
              <p className="text-violet-200 text-xs">Average/Month</p>
              <p className="text-white font-semibold">
                {new Intl.NumberFormat('ro-RO', { minimumFractionDigits: 2 }).format(totalProfit / profitData.length)} RON
              </p>
            </div>
            <div>
              <p className="text-violet-200 text-xs">Best Month</p>
              <p className="text-white font-semibold">
                {Math.max(...profitData.map(d => d.profit)).toLocaleString('ro-RO', { minimumFractionDigits: 2 })} RON
              </p>
            </div>
            <div className="col-span-2 sm:col-span-1">
              <p className="text-violet-200 text-xs">Period Total</p>
              <p className="text-white font-semibold">
                {new Intl.NumberFormat('ro-RO', { minimumFractionDigits: 2 }).format(totalProfit)} RON
              </p>
            </div>
          </div>
        </div>
      )}
    </GlassCard>
  );
};

export default MonthlyProfitChart;
