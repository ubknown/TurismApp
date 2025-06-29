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
  Wifi,
  Car,
  Coffee,
  Tv,
  Wind,
  Bath,
  Bed,
  Mountain
} from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';
import CountyDropdown from '../components/CountyDropdown';
import TypeDropdown from '../components/TypeDropdown';
import LocationImageGallery from '../components/LocationImageGallery';
import { useToast } from '../context/ToastContext';
import api from '../services/axios';

const AddUnitPage = () => {
  const navigate = useNavigate();
  const { success, error: showError } = useToast();
  
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    county: '',
    address: '',
    capacity: '',
    pricePerNight: '',
    amenities: [],
    type: 'HOTEL' // Default type
  });
  
  const [locationImages, setLocationImages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  const availableAmenities = [
    { id: 'wifi', label: 'WiFi', icon: <Wifi className="w-4 h-4" /> },
    { id: 'parking', label: 'Parking', icon: <Car className="w-4 h-4" /> },
    { id: 'kitchen', label: 'Kitchen', icon: <Coffee className="w-4 h-4" /> },
    { id: 'tv', label: 'TV', icon: <Tv className="w-4 h-4" /> },
    { id: 'air_conditioning', label: 'Air Conditioning', icon: <Wind className="w-4 h-4" /> },
    { id: 'bathroom', label: 'Private Bathroom', icon: <Bath className="w-4 h-4" /> },
    { id: 'balcony', label: 'Balcony', icon: <Mountain className="w-4 h-4" /> },
    { id: 'heating', label: 'Heating', icon: <Home className="w-4 h-4" /> }
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

    if (!formData.name.trim()) {
      newErrors.name = 'Unit name is required';
    }

    if (!formData.type) {
      newErrors.type = 'Type of accommodation is required';
    }

    if (!formData.description.trim()) {
      newErrors.description = 'Description is required';
    }

    if (!formData.county) {
      newErrors.county = 'County selection is required';
    }

    if (!formData.address.trim()) {
      newErrors.address = 'Exact address is required';
    }

    if (!formData.capacity || formData.capacity <= 0) {
      newErrors.capacity = 'Capacity must be a positive number';
    }

    if (!formData.pricePerNight || formData.pricePerNight <= 0) {
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
      showError('Validation Error', 'Please fix the errors below');
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
      
      if (response.status === 201) {
        success('Unit Created', 'Your accommodation unit has been successfully created!');
        navigate('/my-units');
      }
    } catch (error) {
      let errorMessage = 'Failed to create unit. Please try again.';
      
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
          errorMessage = responseMessage || 'Invalid unit data provided';
        }
      } else if (error.response?.status === 403) {
        errorMessage = 'You do not have permission to create units';
      } else if (error.response?.status === 401) {
        errorMessage = 'Please log in to create units';
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
            onClick={() => navigate('/my-units')}
            className="p-2 rounded-xl bg-white/10 hover:bg-white/20 transition-colors"
          >
            <ArrowLeft className="w-6 h-6 text-white" />
          </button>
          <div>
            <h1 className="text-4xl font-bold text-white mb-2">Add New Unit</h1>
            <p className="text-violet-200">Create a new accommodation unit for guests to book.</p>
          </div>
        </div>

        <GlassCard className="p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Basic Information */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-white mb-2">
                  Unit Name *
                </label>
                <div className="relative">
                  <Home className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                    placeholder="e.g., Cozy Downtown Apartment"
                    required
                    disabled={loading}
                  />
                </div>
                {errors.name && <p className="mt-1 text-red-400 text-sm">{errors.name}</p>}
              </div>

              <div>
                <label className="block text-sm font-medium text-white mb-2">
                  Unit Type *
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

            {/* Location Information */}
            <div className="space-y-4">
              <h3 className="text-xl font-semibold text-white flex items-center gap-2">
                <MapPin className="w-5 h-5" />
                Location Information
              </h3>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    County / Județ *
                  </label>
                  <CountyDropdown
                    value={formData.county}
                    onChange={handleInputChange}
                    name="county"
                    required
                    disabled={loading}
                    placeholder="Select County / Județ"
                    includeAllOption={false}
                  />
                  {errors.county && <p className="mt-1 text-red-400 text-sm">{errors.county}</p>}
                </div>

                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    Exact Address *
                  </label>
                  <input
                    type="text"
                    name="address"
                    value={formData.address}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                    placeholder="e.g., Strada Libertății 25, Apartament 3"
                    required
                    disabled={loading}
                  />
                  {errors.address && <p className="mt-1 text-red-400 text-sm">{errors.address}</p>}
                </div>
              </div>
            </div>

            {/* Capacity and Pricing */}
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
                    min="1"
                    max="20"
                    className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                    placeholder="Number of guests"
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
                    min="1"
                    step="0.01"
                    className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                    placeholder="Price per night"
                    required
                    disabled={loading}
                  />
                </div>
                {errors.pricePerNight && <p className="mt-1 text-red-400 text-sm">{errors.pricePerNight}</p>}
              </div>
            </div>

            {/* Description */}
            <div>
              <label className="block text-sm font-medium text-white mb-2">
                Description *
              </label>
              <div className="relative">
                <FileText className="absolute left-3 top-3 w-5 h-5 text-gray-400" />
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleInputChange}
                  rows="4"
                  className="w-full pl-12 pr-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300 resize-none"
                  placeholder="Describe your accommodation unit, what makes it special, nearby attractions, etc."
                  required
                  disabled={loading}
                />
              </div>
              {errors.description && <p className="mt-1 text-red-400 text-sm">{errors.description}</p>}
            </div>

            {/* Amenities */}
            <div>
              <h3 className="text-xl font-semibold text-white mb-4">Amenities</h3>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {availableAmenities.map(amenity => (
                  <div
                    key={amenity.id}
                    onClick={() => !loading && handleAmenityToggle(amenity.id)}
                    className={`p-4 rounded-xl border cursor-pointer transition-all duration-300 ${
                      formData.amenities.includes(amenity.id)
                        ? 'bg-violet-500/30 border-violet-500/50 text-white'
                        : 'bg-white/5 border-white/20 text-violet-200 hover:bg-white/10'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      {amenity.icon}
                      <span className="text-sm font-medium">{amenity.label}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Location Images */}
            <div>
              <label className="block text-sm font-medium text-white mb-4">
                Location Images * (1-10 images)
              </label>
              <LocationImageGallery
                images={locationImages}
                onImagesChange={handleLocationImagesChange}
                maxImages={10}
                isEditing={true}
              />
              {errors.images && (
                <p className="text-red-400 text-sm mt-2">{errors.images}</p>
              )}
            </div>

            {/* Submit Button */}
            <div className="flex gap-4 pt-6">
              <button
                type="button"
                onClick={() => navigate('/my-units')}
                disabled={loading}
                className="flex-1 px-6 py-3 bg-white/10 hover:bg-white/20 disabled:bg-white/5 disabled:cursor-not-allowed text-white font-medium rounded-xl transition-all duration-300"
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
                    Creating Unit...
                  </>
                ) : (
                  <>
                    <Save className="w-5 h-5" />
                    Create Unit
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

export default AddUnitPage;
