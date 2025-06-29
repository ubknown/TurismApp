import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { 
  Plus, 
  Edit3, 
  Trash2, 
  Eye, 
  MapPin, 
  Users, 
  DollarSign,
  Calendar,
  Star,
  MoreHorizontal,
  Image as ImageIcon,
  Upload,
  TrendingUp
} from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';
import AlertBox from '../components/AlertBox';
import ImageUploadComponent from '../components/ImageUploadComponent';
import ProfitPdfExport from '../components/ProfitPdfExport';
import MonthlyProfitChart from '../components/MonthlyProfitChart';
import { useToast } from '../context/ToastContext';
import api from '../services/axios';

const MyUnitsPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { success, error: showError } = useToast();
  
  // Initialize units as empty array to ensure it's always an array
  const [units, setUnits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [deleteConfirm, setDeleteConfirm] = useState(null);
  const [showImageUpload, setShowImageUpload] = useState(null);

  useEffect(() => {
    fetchMyUnits();
    
    // Handle navigation state for newly created units
    if (location.state?.newUnit) {
      const newUnit = location.state.newUnit;
      console.log('New unit received from navigation:', newUnit);
      
      if (location.state.showSuccess) {
        success('Property Created!', location.state.message || `Property "${newUnit.name}" created successfully!`);
      }
      
      // Clear the navigation state
      navigate('/my-units', { replace: true });
    }
    
    // Check for localStorage flag to refresh
    if (typeof Storage !== 'undefined' && localStorage.getItem('shouldRefreshMyUnits') === 'true') {
      localStorage.removeItem('shouldRefreshMyUnits');
      const newUnitData = localStorage.getItem('newlyCreatedUnit');
      if (newUnitData) {
        try {
          const newUnit = JSON.parse(newUnitData);
          console.log('New unit from localStorage:', newUnit);
          localStorage.removeItem('newlyCreatedUnit');
        } catch (e) {
          console.error('Error parsing stored unit data:', e);
        }
      }
      // Force a refresh after a short delay to ensure backend is ready
      setTimeout(() => {
        fetchMyUnits();
      }, 1000);
    }
  }, [location.state]);

  // Helper function to ensure data is always an array
  const ensureArray = (data) => {
    console.log('Raw API response:', data);
    console.log('Type of response:', typeof data);
    
    if (Array.isArray(data)) {
      console.log('Response is already an array:', data.length, 'items');
      return data;
    }
    
    if (data && typeof data === 'object') {
      // Check if it's an object with a data property containing an array
      if (Array.isArray(data.data)) {
        console.log('Response has data property with array:', data.data.length, 'items');
        return data.data;
      }
      
      // Check if it's an object with a units property containing an array
      if (Array.isArray(data.units)) {
        console.log('Response has units property with array:', data.units.length, 'items');
        return data.units;
      }
      
      // Check if it's an object with content property containing an array
      if (Array.isArray(data.content)) {
        console.log('Response has content property with array:', data.content.length, 'items');
        return data.content;
      }
      
      // If object has no array property, convert to empty array
      console.warn('Response is object but no array property found, converting to empty array:', data);
      return [];
    }
    
    // If response is null, undefined, or primitive type, return empty array
    console.warn('Response is not array or object, converting to empty array:', data);
    return [];
  };

  const fetchMyUnits = async () => {
    try {
      console.log('=== FETCHING MY UNITS ===');
      setLoading(true);
      setError(null);
      
      const response = await api.get('/api/units/my-units');
      console.log('Full API response object:', response);
      console.log('API response data:', response.data);
      console.log('API response status:', response.status);
      
      // Ensure we always get an array
      const unitsArray = ensureArray(response.data);
      console.log('Final units array:', unitsArray);
      console.log('Final units array length:', unitsArray.length);
      
      setUnits(unitsArray);
      
      if (unitsArray.length > 0) {
        success('Units Loaded', `Found ${unitsArray.length} accommodation units`);
      } else {
        console.log('No units found in response');
      }
      
    } catch (err) {
      console.error('=== API ERROR ===');
      console.error('Error object:', err);
      console.error('Error response:', err.response);
      console.error('Error message:', err.message);
      console.error('Error status:', err.response?.status);
      console.error('Error data:', err.response?.data);
      
      const errorMessage = err.response?.data?.message || err.message || 'Could not fetch your accommodation units';
      setError(errorMessage);
      showError('Failed to Load Units', errorMessage);
      
      // Always ensure units is an empty array on error
      setUnits([]);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteUnit = async (unitId) => {
    try {
      console.log('=== DELETING UNIT ===');
      console.log('Unit ID to delete:', unitId);
      console.log('Current units before delete:', units);
      
      setLoading(true);
      await api.delete(`/api/units/${unitId}`);
      
      // Ensure units is an array before filtering
      const currentUnits = Array.isArray(units) ? units : [];
      const updatedUnits = currentUnits.filter(unit => unit.id !== unitId);
      
      console.log('Units after filtering:', updatedUnits);
      setUnits(updatedUnits);
      setDeleteConfirm(null);
      success('Unit Deleted', 'Accommodation unit has been successfully deleted');
    } catch (error) {
      console.error('Delete error:', error);
      showError('Delete Failed', error.response?.data?.message || 'Could not delete the accommodation unit');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (unitId, currentStatus) => {
    try {
      console.log('=== TOGGLING STATUS ===');
      console.log('Unit ID:', unitId, 'Current status:', currentStatus);
      
      const newStatus = currentStatus === 'active' ? 'inactive' : 'active';
      await api.patch(`/api/units/${unitId}/status`, { status: newStatus });
      
      // Ensure units is an array before mapping
      const currentUnits = Array.isArray(units) ? units : [];
      const updatedUnits = currentUnits.map(unit => 
        unit.id === unitId ? { ...unit, status: newStatus } : unit
      );
      
      console.log('Units after status update:', updatedUnits);
      setUnits(updatedUnits);
      success('Status Updated', `Unit ${newStatus === 'active' ? 'activated' : 'deactivated'} successfully`);
    } catch (error) {
      console.error('Status update error:', error);
      showError('Status Update Failed', error.response?.data?.message || 'Could not update unit status');
    }
  };

  const handleImagesUpdated = (unitId, newImages) => {
    console.log('=== UPDATING IMAGES ===');
    console.log('Unit ID:', unitId, 'New images:', newImages);
    
    // Ensure units is an array before mapping
    const currentUnits = Array.isArray(units) ? units : [];
    const updatedUnits = currentUnits.map(unit => 
      unit.id === unitId ? { ...unit, images: newImages } : unit
    );
    
    console.log('Units after image update:', updatedUnits);
    setUnits(updatedUnits);
  };

  const UnitCard = ({ unit }) => {
    // Defensive checks to ensure unit has required properties
    if (!unit || typeof unit !== 'object') {
      console.warn('Invalid unit object:', unit);
      return null;
    }

    // Ensure required properties exist with default values
    const safeUnit = {
      id: unit.id || 0,
      name: unit.name || 'Unnamed Property',
      description: unit.description || 'No description available',
      location: unit.location || 'Location not specified',
      pricePerNight: unit.pricePerNight || 0,
      capacity: unit.capacity || 0,
      status: unit.status || 'active',
      images: Array.isArray(unit.images) ? unit.images : [],
      photoUrls: Array.isArray(unit.photoUrls) ? unit.photoUrls : [], // Add DTO photoUrls
      rating: unit.rating || 0,
      reviewCount: unit.reviewCount || 0,
      totalBookings: unit.totalBookings || 0,
      monthlyRevenue: unit.monthlyRevenue || 0
    };

    return (
    <GlassCard className="overflow-hidden hover:bg-white/15 transition-all duration-300">
      {/* Header with Image */}
      <div className="relative h-48 overflow-hidden">
        {/* Display first uploaded image or use photoUrls from DTO */}
        {(safeUnit.photoUrls && safeUnit.photoUrls.length > 0) ? (
          <img
            src={safeUnit.photoUrls[0].startsWith('http') ? safeUnit.photoUrls[0] : `http://localhost:8080${safeUnit.photoUrls[0]}`}
            alt={safeUnit.name}
            className="w-full h-full object-cover"
            onError={(e) => {
              console.error('Photo failed to load:', safeUnit.photoUrls[0]);
              e.target.style.display = 'none';
            }}
          />
        ) : (safeUnit.images && safeUnit.images.length > 0) ? (
          <img
            src={safeUnit.images[0].startsWith('http') ? safeUnit.images[0] : `http://localhost:8080${safeUnit.images[0]}`}
            alt={safeUnit.name}
            className="w-full h-full object-cover"
            onError={(e) => {
              console.error('Image failed to load:', safeUnit.images[0]);
              e.target.style.display = 'none';
            }}
          />
        ) : (
          <div className="w-full h-full bg-gradient-to-r from-violet-500 to-purple-600 flex items-center justify-center">
            <ImageIcon className="w-12 h-12 text-white/50" />
          </div>
        )}
        
        <div className="absolute inset-0 bg-black/20"></div>
        
        {/* Status Badge */}
        <div className="absolute top-4 left-4">
          <span className={`px-3 py-1 rounded-full text-xs font-medium ${
            safeUnit.status === 'active' 
              ? 'bg-green-500/20 text-green-300 border border-green-500/30' 
              : 'bg-red-500/20 text-red-300 border border-red-500/30'
          }`}>
            {safeUnit.status === 'active' ? 'Active' : 'Inactive'}
          </span>
        </div>

        {/* Price */}
        <div className="absolute top-4 right-4 bg-white/20 backdrop-blur-sm rounded-full px-3 py-1">
          <span className="text-white font-bold">
            {safeUnit.pricePerNight ? `${safeUnit.pricePerNight.toLocaleString()} RON/night` : 'Price not set'}
          </span>
        </div>

        {/* Image Count */}
        {((safeUnit.photoUrls && safeUnit.photoUrls.length > 0) || (safeUnit.images && safeUnit.images.length > 0)) && (
          <div className="absolute bottom-4 right-4 bg-white/20 backdrop-blur-sm rounded-full px-2 py-1 flex items-center gap-1">
            <ImageIcon className="w-3 h-3 text-white" />
            <span className="text-white text-xs">
              {(safeUnit.photoUrls && safeUnit.photoUrls.length) || (safeUnit.images && safeUnit.images.length) || 0}
            </span>
          </div>
        )}

        {/* Unit Info */}
        <div className="absolute bottom-4 left-4">
          <h3 className="text-xl font-bold text-white">{safeUnit.name}</h3>
          <p className="text-violet-200 flex items-center gap-1">
            <MapPin className="w-4 h-4" />
            {safeUnit.location}
          </p>
        </div>
      </div>

      {/* Content */}
      <div className="p-6">
        {/* Description */}
        <p className="text-violet-200 text-sm mb-4 line-clamp-2">{safeUnit.description}</p>
        
        {/* Stats Grid */}
        <div className="grid grid-cols-2 gap-4 mb-6">
          <div className="flex items-center gap-2 text-sm">
            <Users className="w-4 h-4 text-violet-400" />
            <span className="text-violet-200">{safeUnit.capacity} guests</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <Star className="w-4 h-4 text-yellow-400 fill-current" />
            <span className="text-violet-200">{safeUnit.rating} ({safeUnit.reviewCount})</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <Calendar className="w-4 h-4 text-blue-400" />
            <span className="text-violet-200">{safeUnit.totalBookings} bookings</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <DollarSign className="w-4 h-4 text-green-400" />
            <span className="text-violet-200">{safeUnit.monthlyRevenue.toLocaleString()} RON</span>
          </div>
        </div>

        {/* Actions */}
        <div className="flex gap-2">
          <PrimaryButton
            onClick={() => navigate(`/units/${safeUnit.id}`)}
            size="sm"
            className="flex-1 flex items-center justify-center gap-2"
          >
            <Eye className="w-4 h-4" />
            View
          </PrimaryButton>
          <PrimaryButton
            onClick={() => setShowImageUpload(safeUnit.id)}
            variant="secondary"
            size="sm"
            className="flex items-center justify-center gap-2"
          >
            <Upload className="w-4 h-4" />
            Images
          </PrimaryButton>
          <PrimaryButton
            onClick={() => navigate(`/my-units/edit/${safeUnit.id}`)}
            variant="secondary"
            size="sm"
            className="flex items-center justify-center gap-2"
          >
            <Edit3 className="w-4 h-4" />
            Edit
          </PrimaryButton>
          <PrimaryButton
            onClick={() => setDeleteConfirm(safeUnit.id)}
            variant="danger"
            size="sm"
            className="px-3"
          >
            <Trash2 className="w-4 h-4" />
          </PrimaryButton>
        </div>
      </div>
      
      {/* Status Toggle Button - Centered Below Card */}
      <div className="px-6 pb-6 flex justify-center">
        <PrimaryButton
          onClick={() => handleToggleStatus(safeUnit.id, safeUnit.status)}
          variant={safeUnit.status === 'active' ? 'danger' : 'success'}
          size="sm"
          className="px-8 min-w-[140px]"
        >
          {safeUnit.status === 'active' ? 'Deactivate' : 'Activate'}
        </PrimaryButton>
      </div>
    </GlassCard>
    );
  };

  // Ensure units is always an array before any operations
  const safeUnits = Array.isArray(units) ? units : [];
  
  console.log('=== RENDER STATE ===');
  console.log('Units state:', units);
  console.log('Safe units:', safeUnits);
  console.log('Loading:', loading);
  console.log('Error:', error);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-violet-400 mx-auto mb-4"></div>
          <p className="text-violet-200">Loading your properties...</p>
        </div>
      </div>
    );
  }

  // Show error state if there's an error and no units
  if (error && safeUnits.length === 0) {
    return (
      <div className="min-h-screen p-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center py-16">
            <div className="w-24 h-24 mx-auto mb-6 rounded-full bg-red-500/20 flex items-center justify-center">
              <Trash2 className="w-12 h-12 text-red-400" />
            </div>
            <h3 className="text-2xl font-bold text-white mb-2">Error Loading Units</h3>
            <p className="text-violet-200 mb-6">{error}</p>
            <PrimaryButton
              onClick={() => fetchMyUnits()}
              className="flex items-center gap-2 mx-auto"
            >
              Try Again
            </PrimaryButton>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-8">
          <div>
            <h1 className="text-4xl font-bold text-white mb-2">My Units</h1>
            <p className="text-violet-200">Manage your accommodation units and track their performance.</p>
          </div>
          <div className="flex gap-3 mt-4 sm:mt-0">
            <PrimaryButton
              onClick={() => navigate('/profit-analytics')}
              variant="secondary"
              className="flex items-center gap-2"
            >
              <TrendingUp className="w-5 h-5" />
              View Analytics
            </PrimaryButton>
            <PrimaryButton
              onClick={() => navigate('/add-property')}
              className="flex items-center gap-2"
            >
              <Plus className="w-5 h-5" />
              Add New Property
            </PrimaryButton>
          </div>
        </div>

        {/* Summary Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <GlassCard className="p-6 text-center">
            <h3 className="text-2xl font-bold text-white">{safeUnits.length}</h3>
            <p className="text-violet-200">Total Units</p>
          </GlassCard>
          <GlassCard className="p-6 text-center">
            <h3 className="text-2xl font-bold text-white">
              {safeUnits.filter(u => u.status === 'active').length}
            </h3>
            <p className="text-violet-200">Active Units</p>
          </GlassCard>
          <GlassCard className="p-6 text-center">
            <h3 className="text-2xl font-bold text-white">
              {safeUnits.reduce((sum, unit) => sum + (unit.monthlyRevenue || 0), 0).toLocaleString()} RON
            </h3>
            <p className="text-violet-200">Monthly Revenue</p>
          </GlassCard>
        </div>

        {/* Profit Analytics Section */}
        {safeUnits.length > 0 && (
          <div className="mb-8 space-y-6">
            {/* PDF Export */}
            <GlassCard className="p-6">
              <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                  <h3 className="text-lg font-semibold text-white mb-2">Profit Reports</h3>
                  <p className="text-violet-200 text-sm">Export detailed profit analytics as PDF</p>
                </div>
                <ProfitPdfExport />
              </div>
            </GlassCard>

            {/* Monthly Profit Chart */}
            <MonthlyProfitChart />
          </div>
        )}

        {/* Units Grid */}
        {safeUnits.length > 0 ? (
          <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-8">
            {safeUnits.map((unit) => (
              <UnitCard key={unit.id} unit={unit} />
            ))}
          </div>
        ) : (
          <div className="text-center py-16">
            <div className="w-24 h-24 mx-auto mb-6 rounded-full bg-gradient-to-r from-violet-500 to-purple-600 flex items-center justify-center">
              <Plus className="w-12 h-12 text-white" />
            </div>
            <h3 className="text-2xl font-bold text-white mb-2">No Units Yet</h3>
            <p className="text-violet-200 mb-6">Start building your tourism business by adding your first property.</p>
            <PrimaryButton
              onClick={() => navigate('/add-property')}
              className="flex items-center gap-2 mx-auto"
            >
              <Plus className="w-5 h-5" />
              Add Your First Property
            </PrimaryButton>
          </div>
        )}

        {/* Delete Confirmation Modal */}
        {deleteConfirm && (
          <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
            <GlassCard className="max-w-md w-full p-6">
              <h3 className="text-xl font-bold text-white mb-4">Confirm Deletion</h3>
              <p className="text-violet-200 mb-6">
                Are you sure you want to delete this unit? This action cannot be undone.
              </p>
              <div className="flex gap-3">
                <PrimaryButton
                  onClick={() => setDeleteConfirm(null)}
                  variant="secondary"
                  className="flex-1"
                >
                  Cancel
                </PrimaryButton>
                <PrimaryButton
                  onClick={() => handleDeleteUnit(deleteConfirm)}
                  variant="danger"
                  className="flex-1"
                >
                  Delete
                </PrimaryButton>
              </div>
            </GlassCard>
          </div>
        )}

        {/* Image Upload Modal */}
        {showImageUpload && (
          <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
            <div className="w-full max-w-4xl max-h-[90vh] overflow-y-auto">
              <GlassCard className="p-6">
                <div className="flex items-center justify-between mb-6">
                  <h3 className="text-xl font-bold text-white">Manage Images</h3>
                  <button
                    onClick={() => setShowImageUpload(null)}
                    className="text-white/70 hover:text-white transition-colors duration-200"
                  >
                    ✕
                  </button>
                </div>
                
                <ImageUploadComponent
                  unitId={showImageUpload}
                  currentImages={safeUnits.find(u => u.id === showImageUpload)?.images || []}
                  onImagesUpdated={(newImages) => handleImagesUpdated(showImageUpload, newImages)}
                />
              </GlassCard>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default MyUnitsPage;
