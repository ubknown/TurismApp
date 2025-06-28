import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
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
import { useToast } from '../context/ToastContext';
import api from '../services/axios';

const MyUnitsPage = () => {
  const navigate = useNavigate();
  const { success, error: showError } = useToast();
  const [units, setUnits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deleteConfirm, setDeleteConfirm] = useState(null);
  const [showImageUpload, setShowImageUpload] = useState(null);

  useEffect(() => {
    fetchMyUnits();
  }, []);

  const fetchMyUnits = async () => {
    try {
      setLoading(true);
      const response = await api.get('/api/units/my-units');
      setUnits(response.data);
      success('Units Loaded', `Found ${response.data.length} accommodation units`);
    } catch (err) {
      showError('Failed to Load Units', err.response?.data?.message || 'Could not fetch your accommodation units');
      // Fallback to empty array on error
      setUnits([]);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteUnit = async (unitId) => {
    try {
      setLoading(true);
      await api.delete(`/api/units/${unitId}`);
      setUnits(prev => prev.filter(unit => unit.id !== unitId));
      setDeleteConfirm(null);
      success('Unit Deleted', 'Accommodation unit has been successfully deleted');
    } catch (error) {
      showError('Delete Failed', error.response?.data?.message || 'Could not delete the accommodation unit');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (unitId, currentStatus) => {
    try {
      const newStatus = currentStatus === 'active' ? 'inactive' : 'active';
      await api.patch(`/api/units/${unitId}/status`, { status: newStatus });
      
      setUnits(prev => prev.map(unit => 
        unit.id === unitId ? { ...unit, status: newStatus } : unit
      ));
      success('Status Updated', `Unit ${newStatus === 'active' ? 'activated' : 'deactivated'} successfully`);
    } catch (error) {
      showError('Status Update Failed', error.response?.data?.message || 'Could not update unit status');
    }
  };

  const handleImagesUpdated = (unitId, newImages) => {
    setUnits(prevUnits => 
      prevUnits.map(unit => 
        unit.id === unitId ? { ...unit, images: newImages } : unit
      )
    );
  };

  const UnitCard = ({ unit }) => (
    <GlassCard className="overflow-hidden hover:bg-white/15 transition-all duration-300">
      {/* Header with Image */}
      <div className="relative h-48 overflow-hidden">
        {unit.images && unit.images.length > 0 ? (
          <img
            src={unit.images[0].startsWith('http') ? unit.images[0] : `http://localhost:8080${unit.images[0]}`}
            alt={unit.name}
            className="w-full h-full object-cover"
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
            unit.status === 'active' 
              ? 'bg-green-500/20 text-green-300 border border-green-500/30' 
              : 'bg-red-500/20 text-red-300 border border-red-500/30'
          }`}>
            {unit.status === 'active' ? 'Active' : 'Inactive'}
          </span>
        </div>

        {/* Price */}
        <div className="absolute top-4 right-4 bg-white/20 backdrop-blur-sm rounded-full px-3 py-1">
          <span className="text-white font-bold">${unit.pricePerNight}/night</span>
        </div>

        {/* Image Count */}
        {unit.images && unit.images.length > 0 && (
          <div className="absolute bottom-4 right-4 bg-white/20 backdrop-blur-sm rounded-full px-2 py-1 flex items-center gap-1">
            <ImageIcon className="w-3 h-3 text-white" />
            <span className="text-white text-xs">{unit.images.length}</span>
          </div>
        )}

        {/* Unit Info */}
        <div className="absolute bottom-4 left-4">
          <h3 className="text-xl font-bold text-white">{unit.name}</h3>
          <p className="text-violet-200 flex items-center gap-1">
            <MapPin className="w-4 h-4" />
            {unit.location}
          </p>
        </div>
      </div>

      {/* Content */}
      <div className="p-6">
        {/* Description */}
        <p className="text-violet-200 text-sm mb-4 line-clamp-2">{unit.description}</p>
        
        {/* Stats Grid */}
        <div className="grid grid-cols-2 gap-4 mb-6">
          <div className="flex items-center gap-2 text-sm">
            <Users className="w-4 h-4 text-violet-400" />
            <span className="text-violet-200">{unit.capacity} guests</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <Star className="w-4 h-4 text-yellow-400 fill-current" />
            <span className="text-violet-200">{unit.rating} ({unit.reviewCount})</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <Calendar className="w-4 h-4 text-blue-400" />
            <span className="text-violet-200">{unit.totalBookings} bookings</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <DollarSign className="w-4 h-4 text-green-400" />
            <span className="text-violet-200">${unit.monthlyRevenue.toLocaleString()}</span>
          </div>
        </div>

        {/* Actions */}
        <div className="flex gap-2">
          <PrimaryButton
            onClick={() => navigate(`/units/${unit.id}`)}
            size="sm"
            className="flex-1 flex items-center justify-center gap-2"
          >
            <Eye className="w-4 h-4" />
            View
          </PrimaryButton>
          <PrimaryButton
            onClick={() => setShowImageUpload(unit.id)}
            variant="secondary"
            size="sm"
            className="flex items-center justify-center gap-2"
          >
            <Upload className="w-4 h-4" />
            Images
          </PrimaryButton>
          <PrimaryButton
            onClick={() => navigate(`/my-units/edit/${unit.id}`)}
            variant="secondary"
            size="sm"
            className="flex items-center justify-center gap-2"
          >
            <Edit3 className="w-4 h-4" />
            Edit
          </PrimaryButton>
          <PrimaryButton
            onClick={() => handleToggleStatus(unit.id, unit.status)}
            variant={unit.status === 'active' ? 'danger' : 'success'}
            size="sm"
            className="px-3"
          >
            {unit.status === 'active' ? 'Deactivate' : 'Activate'}
          </PrimaryButton>
          <PrimaryButton
            onClick={() => setDeleteConfirm(unit.id)}
            variant="danger"
            size="sm"
            className="px-3"
          >
            <Trash2 className="w-4 h-4" />
          </PrimaryButton>
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
              onClick={() => navigate('/my-units/add')}
              className="flex items-center gap-2"
            >
              <Plus className="w-5 h-5" />
              Add New Unit
            </PrimaryButton>
          </div>
        </div>

        {/* Summary Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <GlassCard className="p-6 text-center">
            <h3 className="text-2xl font-bold text-white">{units.length}</h3>
            <p className="text-violet-200">Total Units</p>
          </GlassCard>
          <GlassCard className="p-6 text-center">
            <h3 className="text-2xl font-bold text-white">
              {units.filter(u => u.status === 'active').length}
            </h3>
            <p className="text-violet-200">Active Units</p>
          </GlassCard>
          <GlassCard className="p-6 text-center">
            <h3 className="text-2xl font-bold text-white">
              ${units.reduce((sum, unit) => sum + unit.monthlyRevenue, 0).toLocaleString()}
            </h3>
            <p className="text-violet-200">Monthly Revenue</p>
          </GlassCard>
        </div>

        {/* Units Grid */}
        {units.length > 0 ? (
          <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-8">
            {units.map((unit) => (
              <UnitCard key={unit.id} unit={unit} />
            ))}
          </div>
        ) : (
          <div className="text-center py-16">
            <div className="w-24 h-24 mx-auto mb-6 rounded-full bg-gradient-to-r from-violet-500 to-purple-600 flex items-center justify-center">
              <Plus className="w-12 h-12 text-white" />
            </div>
            <h3 className="text-2xl font-bold text-white mb-2">No Units Yet</h3>
            <p className="text-violet-200 mb-6">Start building your tourism business by adding your first accommodation unit.</p>
            <PrimaryButton
              onClick={() => navigate('/my-units/add')}
              className="flex items-center gap-2 mx-auto"
            >
              <Plus className="w-5 h-5" />
              Add Your First Unit
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
                  currentImages={units.find(u => u.id === showImageUpload)?.images || []}
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
