import React, { useState, useEffect } from 'react';
import { Database, RefreshCw } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';
import api from '../services/axios';

const DatabaseDebugPage = () => {
  const [debugInfo, setDebugInfo] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchDebugInfo = async () => {
    try {
      setLoading(true);
      setError(null);
      console.log('🔍 Fetching debug info...');
      
      const response = await api.get('/api/units/debug/count');
      console.log('📦 Debug response:', response.data);
      setDebugInfo(response.data);
    } catch (err) {
      console.error('❌ Debug fetch error:', err);
      setError(err.response?.data?.message || err.message || 'Failed to fetch debug info');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDebugInfo();
  }, []);

  return (
    <div className="min-h-screen p-6">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-white mb-2 flex items-center gap-3">
            <Database className="w-10 h-10 text-violet-400" />
            Database Debug
          </h1>
          <p className="text-violet-200">Debug information for accommodation units database</p>
        </div>

        {/* Refresh Button */}
        <div className="mb-6">
          <PrimaryButton 
            onClick={fetchDebugInfo} 
            disabled={loading}
            className="flex items-center gap-2"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            {loading ? 'Loading...' : 'Refresh Data'}
          </PrimaryButton>
        </div>

        {/* Error Display */}
        {error && (
          <GlassCard className="p-6 mb-6 border-red-500/50 bg-red-500/10">
            <h3 className="text-red-400 font-semibold mb-2">Error</h3>
            <p className="text-red-300">{error}</p>
          </GlassCard>
        )}

        {/* Debug Info Display */}
        {debugInfo && (
          <div className="space-y-6">
            {/* Summary Stats */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <GlassCard className="p-6 text-center">
                <h3 className="text-2xl font-bold text-white mb-2">
                  {debugInfo.totalUnits || 0}
                </h3>
                <p className="text-violet-200">Total Units in Database</p>
              </GlassCard>
              
              <GlassCard className="p-6 text-center">
                <h3 className="text-2xl font-bold text-white mb-2">
                  {debugInfo.availableUnits || 0}
                </h3>
                <p className="text-violet-200">Available Units (visible to public)</p>
              </GlassCard>
            </div>

            {/* Error Info */}
            {debugInfo.error && (
              <GlassCard className="p-6 border-red-500/50 bg-red-500/10">
                <h3 className="text-red-400 font-semibold mb-4">Database Error</h3>
                <div className="space-y-2">
                  <p className="text-red-300"><strong>Error:</strong> {debugInfo.error}</p>
                  {debugInfo.cause && (
                    <p className="text-red-300"><strong>Cause:</strong> {debugInfo.cause}</p>
                  )}
                </div>
              </GlassCard>
            )}

            {/* Unit Details */}
            {debugInfo.allUnitsDetails && debugInfo.allUnitsDetails.length > 0 && (
              <GlassCard className="p-6">
                <h3 className="text-xl font-semibold text-white mb-4">All Units Details</h3>
                <div className="space-y-3">
                  {debugInfo.allUnitsDetails.map((unit, index) => (
                    <div 
                      key={unit.id || index} 
                      className={`p-4 rounded-lg border ${
                        unit.available 
                          ? 'border-green-500/30 bg-green-500/10' 
                          : 'border-red-500/30 bg-red-500/10'
                      }`}
                    >
                      <div className="flex justify-between items-start">
                        <div>
                          <h4 className="text-white font-medium">{unit.name}</h4>
                          <p className="text-gray-300 text-sm">{unit.location}</p>
                        </div>
                        <div className="text-right">
                          <span className={`px-2 py-1 rounded text-xs ${
                            unit.available 
                              ? 'bg-green-500/20 text-green-300' 
                              : 'bg-red-500/20 text-red-300'
                          }`}>
                            {unit.available ? 'Available' : 'Not Available'}
                          </span>
                          <p className="text-gray-400 text-xs mt-1">ID: {unit.id}</p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </GlassCard>
            )}

            {/* Empty State */}
            {debugInfo.totalUnits === 0 && (
              <GlassCard className="p-8 text-center">
                <Database className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                <h3 className="text-xl font-semibold text-white mb-2">Database is Empty</h3>
                <p className="text-violet-200 mb-4">
                  No accommodation units found in the database.
                </p>
                <p className="text-gray-300 text-sm">
                  The DataSeeder should populate test data when the backend starts. 
                  Check the backend console for seeding logs.
                </p>
              </GlassCard>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default DatabaseDebugPage;
