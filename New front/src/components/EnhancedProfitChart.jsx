import React, { useEffect, useState } from "react";
import { Line } from "react-chartjs-2";
import { useToast } from "../context/ToastContext";
import api from "../services/axios";
import {
  Chart as ChartJS,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Tooltip,
  Legend,
  Filler,
} from "chart.js";

ChartJS.register(LineElement, PointElement, LinearScale, CategoryScale, Tooltip, Legend, Filler);

const EnhancedProfitChart = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [timeRange, setTimeRange] = useState(12); // Default to 12 months
  const { showToast } = useToast();

  const timeRangeOptions = [
    { value: 3, label: "3 Months" },
    { value: 6, label: "6 Months" },
    { value: 12, label: "12 Months" },
    { value: 24, label: "24 Months" },
  ];

  const fetchProfitData = async (months) => {
    try {
      setLoading(true);
      const token = localStorage.getItem("token");
      if (!token) {
        showToast("Please login to view profit data", "error");
        return;
      }

      const [histRes, predRes] = await Promise.all([
        api.get(`/api/units/my-units/profit/monthly?lastMonths=${months}`),
        api.get(`/api/units/my-units/profit/predict?lastMonths=${months}&predictMonths=3`),
      ]);

      const actual = histRes.data.map((item) => ({
        month: item.month,
        value: item.totalProfit,
        type: "actual",
      }));

      const predicted = predRes.data.map((item) => ({
        month: item.month,
        value: item.predictedProfit,
        type: "predicted",
      }));

      setData([...actual, ...predicted]);
      showToast("Profit data updated successfully", "success");
    } catch (err) {
      console.error("Error fetching profit data:", err);
      showToast("Failed to load profit data", "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfitData(timeRange);
  }, [timeRange]);

  const handleTimeRangeChange = (newRange) => {
    setTimeRange(newRange);
  };

  if (loading) {
    return (
      <div className="bg-white/10 backdrop-blur-xl rounded-2xl p-6 border border-white/20">
        <div className="animate-pulse">
          <div className="h-6 bg-white/20 rounded mb-4"></div>
          <div className="h-64 bg-white/10 rounded"></div>
        </div>
      </div>
    );
  }

  if (data.length === 0) {
    return (
      <div className="bg-white/10 backdrop-blur-xl rounded-2xl p-6 border border-white/20">
        <h3 className="text-xl font-semibold text-white mb-4">Profit Analytics</h3>
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-white/60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          </div>
          <p className="text-white/80 text-lg">No profit data available</p>
          <p className="text-white/60 text-sm mt-2">Start adding bookings to see profit analytics</p>
        </div>
      </div>
    );
  }

  const months = data.map((d) => d.month);
  const actualData = data.filter((d) => d.type === "actual");
  const predictedData = data.filter((d) => d.type === "predicted");

  // Create chart data with proper alignment
  const allMonths = [...new Set(months)];
  const actualValues = allMonths.map(month => {
    const item = actualData.find(d => d.month === month);
    return item ? item.value : null;
  });
  const predictedValues = allMonths.map(month => {
    const item = predictedData.find(d => d.month === month);
    return item ? item.value : null;
  });

  const chartData = {
    labels: allMonths,
    datasets: [
      {
        label: "Actual Profit",
        data: actualValues,
        borderColor: "#60A5FA",
        backgroundColor: "rgba(96, 165, 250, 0.1)",
        borderWidth: 3,
        fill: true,
        tension: 0.4,
        pointRadius: 6,
        pointBackgroundColor: "#60A5FA",
        pointBorderColor: "#1E40AF",
        pointBorderWidth: 2,
        pointHoverRadius: 8,
        pointHoverBackgroundColor: "#3B82F6",
        pointHoverBorderColor: "#FFFFFF",
        pointHoverBorderWidth: 3,
      },
      {
        label: "Predicted Profit",
        data: predictedValues,
        borderColor: "#F59E0B",
        backgroundColor: "rgba(245, 158, 11, 0.1)",
        borderWidth: 3,
        fill: false,
        tension: 0.4,
        pointRadius: 6,
        pointBackgroundColor: "#F59E0B",
        pointBorderColor: "#D97706",
        pointBorderWidth: 2,
        pointHoverRadius: 8,
        pointHoverBackgroundColor: "#F59E0B",
        pointHoverBorderColor: "#FFFFFF",
        pointHoverBorderWidth: 3,
        borderDash: [8, 4],
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: "top",
        labels: {
          color: "#FFFFFF",
          font: {
            size: 14,
            weight: 500,
          },
          padding: 20,
          usePointStyle: true,
          pointStyle: "circle",
        },
      },
      tooltip: {
        backgroundColor: "rgba(0, 0, 0, 0.9)",
        titleColor: "#FFFFFF",
        bodyColor: "#FFFFFF",
        borderColor: "rgba(255, 255, 255, 0.2)",
        borderWidth: 1,
        cornerRadius: 12,
        padding: 12,
        callbacks: {
          label: function (context) {
            const label = context.dataset.label || "";
            const value = context.parsed.y || 0;
            return `${label}: ${value.toLocaleString("ro-RO", { 
              style: "currency", 
              currency: "RON" 
            })}`;
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        grid: {
          color: "rgba(255, 255, 255, 0.1)",
          borderColor: "rgba(255, 255, 255, 0.2)",
        },
        ticks: {
          color: "#FFFFFF",
          font: {
            size: 12,
          },
          callback: function (value) {
            return value.toLocaleString("ro-RO", { 
              style: "currency", 
              currency: "RON",
              minimumFractionDigits: 0,
              maximumFractionDigits: 0,
            });
          },
        },
        title: {
          display: true,
          text: "Profit (RON)",
          color: "#FFFFFF",
          font: {
            size: 14,
            weight: 600,
          },
        },
      },
      x: {
        grid: {
          color: "rgba(255, 255, 255, 0.1)",
          borderColor: "rgba(255, 255, 255, 0.2)",
        },
        ticks: {
          color: "#FFFFFF",
          font: {
            size: 12,
          },
        },
        title: {
          display: true,
          text: "Time Period",
          color: "#FFFFFF",
          font: {
            size: 14,
            weight: 600,
          },
        },
      },
    },
    interaction: {
      intersect: false,
      mode: 'index',
    },
    elements: {
      point: {
        hoverRadius: 8,
      },
    },
  };

  // Calculate insights
  const totalActualProfit = actualData.reduce((sum, item) => sum + item.value, 0);
  const totalPredictedProfit = predictedData.reduce((sum, item) => sum + item.value, 0);
  const avgMonthlyProfit = actualData.length > 0 ? totalActualProfit / actualData.length : 0;
  const growthTrend = actualData.length > 1 
    ? ((actualData[actualData.length - 1]?.value || 0) - (actualData[0]?.value || 0)) / (actualData[0]?.value || 1) * 100
    : 0;

  return (
    <div className="bg-white/10 backdrop-blur-xl rounded-2xl p-6 border border-white/20">
      {/* Header with Time Range Selector */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
        <div>
          <h3 className="text-2xl font-bold text-white mb-1">Profit Analytics</h3>
          <p className="text-white/70">Track your earnings and future predictions</p>
        </div>
        
        <div className="flex gap-2">
          {timeRangeOptions.map((option) => (
            <button
              key={option.value}
              onClick={() => handleTimeRangeChange(option.value)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-300 ${
                timeRange === option.value
                  ? "bg-blue-500 text-white shadow-lg shadow-blue-500/30"
                  : "bg-white/10 text-white/80 hover:bg-white/20 hover:text-white"
              }`}
            >
              {option.label}
            </button>
          ))}
        </div>
      </div>

      {/* Insights Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <div className="bg-white/5 rounded-lg p-4 border border-white/10">
          <div className="text-white/60 text-sm">Total Earned</div>
          <div className="text-white text-lg font-bold">
            {totalActualProfit.toLocaleString("ro-RO", { 
              style: "currency", 
              currency: "RON",
              minimumFractionDigits: 0,
            })}
          </div>
        </div>
        
        <div className="bg-white/5 rounded-lg p-4 border border-white/10">
          <div className="text-white/60 text-sm">Predicted Earnings</div>
          <div className="text-orange-400 text-lg font-bold">
            {totalPredictedProfit.toLocaleString("ro-RO", { 
              style: "currency", 
              currency: "RON",
              minimumFractionDigits: 0,
            })}
          </div>
        </div>
        
        <div className="bg-white/5 rounded-lg p-4 border border-white/10">
          <div className="text-white/60 text-sm">Monthly Average</div>
          <div className="text-white text-lg font-bold">
            {avgMonthlyProfit.toLocaleString("ro-RO", { 
              style: "currency", 
              currency: "RON",
              minimumFractionDigits: 0,
            })}
          </div>
        </div>
        
        <div className="bg-white/5 rounded-lg p-4 border border-white/10">
          <div className="text-white/60 text-sm">Growth Trend</div>
          <div className={`text-lg font-bold ${growthTrend >= 0 ? 'text-green-400' : 'text-red-400'}`}>
            {growthTrend >= 0 ? '+' : ''}{growthTrend.toFixed(1)}%
          </div>
        </div>
      </div>

      {/* Chart */}
      <div className="h-80 relative">
        <Line data={chartData} options={options} />
      </div>

      {/* AI Insights */}
      <div className="mt-6 p-4 bg-gradient-to-r from-purple-500/20 to-blue-500/20 rounded-lg border border-purple-300/20">
        <div className="flex items-center gap-2 mb-2">
          <div className="w-6 h-6 bg-gradient-to-r from-purple-500 to-blue-500 rounded-full flex items-center justify-center">
            <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
            </svg>
          </div>
          <h4 className="text-white font-semibold">AI Insights</h4>
        </div>
        <p className="text-white/80 text-sm">
          {growthTrend > 10 
            ? "Your profit is showing strong growth! Consider expanding your property portfolio."
            : growthTrend > 0 
            ? "Steady profit growth detected. Maintain your current strategy for consistent earnings."
            : "Profit trend needs attention. Consider optimizing pricing or improving property amenities."
          }
        </p>
      </div>
    </div>
  );
};

export default EnhancedProfitChart;
