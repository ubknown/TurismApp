import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Home, 
  MapPin, 
  DollarSign, 
  Users, 
  FileText, 
  Save,
  ArrowLeft,
  Phone,
  Image as ImageIcon,
  Building
} from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';
import CountyDropdown from '../components/CountyDropdown';
import TypeDropdown from '../components/TypeDropdown';
import LocationImageGallery from '../components/LocationImageGallery';
import { useToast } from '../context/ToastContext';
import api from '../services/axios';

const AddPropertyPage = () => {
  const navigate = useNavigate();
  const { success, error: showError } = useToast();
  
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    county: '',
    address: '',
    phone: '',
    capacity: '',
    pricePerNight: '',
    amenities: [],
    type: 'HOTEL' // Default type
  });
  
  const [locationImages, setLocationImages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  const availableAmenities = [
    { id: 'wifi', label: 'WiFi', icon: '📶' },
    { id: 'parking', label: 'Parking', icon: '🚗' },
    { id: 'kitchen', label: 'Kitchen', icon: '🍳' },
    { id: 'tv', label: 'TV', icon: '📺' },
    { id: 'air_conditioning', label: 'Air Conditioning', icon: '❄️' },
    { id: 'bathroom', label: 'Private Bathroom', icon: '🚿' },
    { id: 'balcony', label: 'Balcony', icon: '🌅' },
    { id: 'heating', label: 'Heating', icon: '🔥' },
    { id: 'pool', label: 'Pool', icon: '🏊' },
    { id: 'spa', label: 'Spa', icon: '🧖' },
    { id: 'gym', label: 'Gym', icon: '💪' },
    { id: 'restaurant', label: 'Restaurant', icon: '🍽️' }
  ];

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear specific error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handleAmenityToggle = (amenityId) => {
    setFormData(prev => ({
      ...prev,
      amenities: prev.amenities.includes(amenityId)
        ? prev.amenities.filter(id => id !== amenityId)
        : [...prev.amenities, amenityId]
    }));
  };

  const handleLocationImagesChange = (images) => {
    setLocationImages(images);
    // Clear image validation errors when images are added
    if (images.length > 0 && errors.images) {
      setErrors(prev => ({ ...prev, images: '' }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    // Required field validations
    if (!formData.name.trim()) {
      newErrors.name = 'Property name is required';
    }

    if (!formData.type) {
      newErrors.type = 'Property type is required';
    }

    if (!formData.county.trim()) {
      newErrors.county = 'County is required';
    }

    if (!formData.address.trim()) {
      newErrors.address = 'Address is required';
    }

    if (!formData.phone.trim()) {
      newErrors.phone = 'Phone number is required';
    } else if (!/^[\+]?[0-9\-\s\(\)]+$/.test(formData.phone)) {
      newErrors.phone = 'Please enter a valid phone number';
    }

    if (!formData.description.trim()) {
      newErrors.description = 'Description is required';
    } else if (formData.description.trim().length < 20) {
      newErrors.description = 'Description must be at least 20 characters long';
    }

    if (!formData.capacity || parseInt(formData.capacity) < 1) {
      newErrors.capacity = 'Capacity must be at least 1 guest';
    }

    if (!formData.pricePerNight || parseFloat(formData.pricePerNight) < 0) {
      newErrors.pricePerNight = 'Price per night must be a positive number';
    }

    // Validate images
    if (locationImages.length === 0) {
      newErrors.images = 'At least one image is required';
    } else if (locationImages.length > 10) {
      newErrors.images = 'You can upload a maximum of 10 images';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      showError('Validation Error', 'Please correct the errors below');
      return;
    }

    // Validate images
    if (locationImages.length === 0) {
      setErrors(prev => ({ ...prev, images: 'At least one image is required' }));
      showError('Validation Error', 'Please add at least one image');
      return;
    }

    setLoading(true);

    try {
      const submitData = {
        ...formData,
        location: `${formData.address}, ${formData.county}`, // Combine address and county for backward compatibility
        capacity: parseInt(formData.capacity),
        pricePerNight: parseFloat(formData.pricePerNight)
      };

      // Remove the separate address field as we've combined it with county into location
      delete submitData.address;
      // Keep county field for the new county property

      // Create FormData for multipart upload
      const formDataToSend = new FormData();
      formDataToSend.append('unit', JSON.stringify(submitData));
      
      // Add images from LocationImageGallery
      locationImages.forEach((image, index) => {
        if (image.file) {
          // New file to upload
          formDataToSend.append('photos', image.file);
        }
      });

      const response = await api.post('/api/units/with-photos', formDataToSend, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      console.log('=== PROPERTY CREATION SUCCESS ===');
      console.log('Response status:', response.status);
      console.log('Response data:', response.data);
      
      if (response.status === 201) {
        const result = response.data;
        const unitData = result.unit || result; // Handle both response formats
        
        success(
          'Property Created Successfully!', 
          `"${unitData.name}" has been added to your properties. ID: ${unitData.id}`
        );
        
        // Store the new unit data for immediate use
        if (typeof Storage !== 'undefined') {
          localStorage.setItem('newlyCreatedUnit', JSON.stringify(unitData));
          localStorage.setItem('shouldRefreshMyUnits', 'true');
          localStorage.setItem('shouldRefreshUnits', 'true'); // Also refresh browse units
        }
        
        // Navigate to my units page
        navigate('/my-units', { 
          state: { 
            newUnit: unitData,
            showSuccess: true,
            message: `Property "${unitData.name}" created successfully!`
          }
        });
      }
    } catch (error) {
      let errorMessage = 'Failed to create property. Please try again.';
      
      if (error.response?.status === 400) {
        const responseMessage = error.response.data;
        
        // Check if this is a location uniqueness error
        if (typeof responseMessage === 'string' && 
            (responseMessage.includes('location') || 
             responseMessage.includes('address') || 
             responseMessage.includes('already exists'))) {
          errorMessage = responseMessage;
          // Highlight the address and county fields
          setErrors(prev => ({
            ...prev,
            address: 'This address may already be in use',
            county: 'Please verify the location details'
          }));
        } else {
          errorMessage = responseMessage || 'Invalid property data provided';
        }
      } else if (error.response?.status === 403) {
        errorMessage = 'You do not have permission to create properties';
      } else if (error.response?.status === 401) {
        errorMessage = 'Please log in to create properties';
        navigate('/login');
        return;
      }
      
      showError('Creation Failed', errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen p-6">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="flex items-center gap-4 mb-8">
          <button
            onClick={() => navigate('/dashboard')}
            className="p-2 rounded-xl bg-white/10 hover:bg-white/20 transition-colors"
          >
            <ArrowLeft className="w-6 h-6 text-white" />
          </button>
          <div>
            <h1 className="text-4xl font-bold text-white mb-2">Add New Property</h1>
            <p className="text-violet-200">Create a new accommodation listing for your guests.</p>
          </div>
        </div>

        <GlassCard className="p-8">
          <form onSubmit={handleSubmit} className="space-y-8">
            {/* Basic Information */}
            <div className="space-y-6">
              <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-2">
                <Building className="w-6 h-6" />
                Basic Information
              </h2>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    Property Name *
                  </label>
                  <div className="relative">
                    <Home className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                      placeholder="e.g., Luxury Mountain Resort"
                      required
                      disabled={loading}
                    />
                  </div>
                  {errors.name && <p className="mt-1 text-red-400 text-sm">{errors.name}</p>}
                </div>

                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    Property Type *
                  </label>
                  <TypeDropdown
                    value={formData.type}
                    onChange={handleInputChange}
                    name="type"
                    required
                    disabled={loading}
                    includeAllOption={false}
                  />
                  {errors.type && <p className="mt-1 text-red-400 text-sm">{errors.type}</p>}
                </div>
              </div>
            </div>

            {/* Location Information */}
            <div className="space-y-6">
              <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-2">
                <MapPin className="w-6 h-6" />
                Location Details
              </h2>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    County *
                  </label>
                  <CountyDropdown
                    value={formData.county}
                    onChange={handleInputChange}
                    name="county"
                    required
                    disabled={loading}
                    includeAllOption={false}
                  />
                  {errors.county && <p className="mt-1 text-red-400 text-sm">{errors.county}</p>}
                </div>

                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    Phone Number *
                  </label>
                  <div className="relative">
                    <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input
                      type="tel"
                      name="phone"
                      value={formData.phone}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                      placeholder="e.g., +40 123 456 789"
                      required
                      disabled={loading}
                    />
                  </div>
                  {errors.phone && <p className="mt-1 text-red-400 text-sm">{errors.phone}</p>}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-white mb-2">
                  Full Address *
                </label>
                <div className="relative">
                  <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    type="text"
                    name="address"
                    value={formData.address}
                    onChange={handleInputChange}
                    className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                    placeholder="e.g., Strada Principala 123, Brasov"
                    required
                    disabled={loading}
                  />
                </div>
                {errors.address && <p className="mt-1 text-red-400 text-sm">{errors.address}</p>}
              </div>
            </div>

            {/* Property Details */}
            <div className="space-y-6">
              <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-2">
                <DollarSign className="w-6 h-6" />
                Property Details
              </h2>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    Guest Capacity *
                  </label>
                  <div className="relative">
                    <Users className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input
                      type="number"
                      name="capacity"
                      value={formData.capacity}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                      placeholder="e.g., 4"
                      min="1"
                      max="50"
                      required
                      disabled={loading}
                    />
                  </div>
                  {errors.capacity && <p className="mt-1 text-red-400 text-sm">{errors.capacity}</p>}
                </div>

                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    Price per Night (RON) *
                  </label>
                  <div className="relative">
                    <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input
                      type="number"
                      name="pricePerNight"
                      value={formData.pricePerNight}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                      placeholder="e.g., 150"
                      min="0"
                      step="0.01"
                      required
                      disabled={loading}
                    />
                  </div>
                  {errors.pricePerNight && <p className="mt-1 text-red-400 text-sm">{errors.pricePerNight}</p>}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-white mb-2">
                  Description *
                </label>
                <div className="relative">
                  <FileText className="absolute left-3 top-4 w-5 h-5 text-gray-400" />
                  <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleInputChange}
                    rows="4"
                    className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300 resize-none"
                    placeholder="Describe your property, amenities, nearby attractions, and what makes it special... (minimum 20 characters)"
                    required
                    disabled={loading}
                  />
                </div>
                {errors.description && <p className="mt-1 text-red-400 text-sm">{errors.description}</p>}
                <p className="mt-1 text-violet-300 text-xs">
                  {formData.description.length}/20 characters minimum
                </p>
              </div>
            </div>

            {/* Amenities */}
            <div className="space-y-6">
              <h2 className="text-2xl font-bold text-white mb-4">Amenities & Features</h2>
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                {availableAmenities.map(amenity => (
                  <div
                    key={amenity.id}
                    onClick={() => !loading && handleAmenityToggle(amenity.id)}
                    className={`p-4 rounded-xl border-2 cursor-pointer transition-all duration-300 ${
                      formData.amenities.includes(amenity.id)
                        ? 'border-violet-500 bg-violet-500/20 text-white'
                        : 'border-white/20 bg-white/5 text-gray-300 hover:border-violet-400 hover:bg-white/10'
                    } ${loading ? 'opacity-50 cursor-not-allowed' : ''}`}
                  >
                    <div className="text-center">
                      <div className="text-2xl mb-2">{amenity.icon}</div>
                      <div className="text-sm font-medium">{amenity.label}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Property Images */}
            <div className="space-y-6">
              <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-2">
                <ImageIcon className="w-6 h-6" />
                Property Images *
              </h2>
              <LocationImageGallery
                images={locationImages}
                onImagesChange={handleLocationImagesChange}
                maxImages={10}
                isEditing={true}
              />
              {errors.images && (
                <p className="text-red-400 text-sm mt-2">{errors.images}</p>
              )}
              <p className="text-violet-300 text-sm">
                Upload high-quality images that showcase your property. The first image will be the main preview.
              </p>
            </div>

            {/* Submit Button */}
            <div className="flex gap-4 pt-6 border-t border-white/10">
              <button
                type="button"
                onClick={() => navigate('/dashboard')}
                disabled={loading}
                className="flex-1 px-6 py-3 bg-white/10 hover:bg-white/20 text-white rounded-xl transition-all duration-300 border border-white/20 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Cancel
              </button>
              <PrimaryButton
                type="submit"
                disabled={loading}
                className="flex-1 flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                    Creating Property...
                  </>
                ) : (
                  <>
                    <Save className="w-5 h-5" />
                    Create Property
                  </>
                )}
              </PrimaryButton>
            </div>
          </form>
        </GlassCard>
      </div>
    </div>
  );
};

export default AddPropertyPage;
