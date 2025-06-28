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
  
  const [photos, setPhotos] = useState([]);
  const [photoPreviews, setPhotoPreviews] = useState([]);
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

  const handlePhotoChange = (e) => {
    const files = Array.from(e.target.files);
    
    // Validate number of files
    if (files.length === 0) {
      setErrors(prev => ({ ...prev, photos: 'At least one photo is required' }));
      return;
    }
    
    if (files.length > 10) {
      setErrors(prev => ({ ...prev, photos: 'Maximum 10 photos allowed' }));
      return;
    }
    
    // Validate file types
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    const invalidFiles = files.filter(file => !validTypes.includes(file.type));
    if (invalidFiles.length > 0) {
      setErrors(prev => ({ ...prev, photos: 'Only JPEG, PNG, and WebP images are allowed' }));
      return;
    }
    
    // Validate file sizes (5MB max each)
    const oversizedFiles = files.filter(file => file.size > 5 * 1024 * 1024);
    if (oversizedFiles.length > 0) {
      setErrors(prev => ({ ...prev, photos: 'Each photo must be less than 5MB' }));
      return;
    }
    
    setPhotos(files);
    setErrors(prev => ({ ...prev, photos: '' }));
    
    // Create previews
    const previews = files.map(file => {
      return new Promise((resolve) => {
        const reader = new FileReader();
        reader.onload = (e) => resolve(e.target.result);
        reader.readAsDataURL(file);
      });
    });
    
    Promise.all(previews).then(setPhotoPreviews);
  };

  const removePhoto = (index) => {
    const newPhotos = photos.filter((_, i) => i !== index);
    const newPreviews = photoPreviews.filter((_, i) => i !== index);
    
    setPhotos(newPhotos);
    setPhotoPreviews(newPreviews);
    
    if (newPhotos.length === 0) {
      setErrors(prev => ({ ...prev, photos: 'At least one photo is required' }));
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

    // Validate photos
    if (photos.length === 0) {
      newErrors.photos = 'At least one photo is required';
    } else if (photos.length > 5) {
      newErrors.photos = 'You can upload a maximum of 5 photos';
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

    // Validate photos
    if (photos.length === 0) {
      setErrors(prev => ({ ...prev, photos: 'At least one photo is required' }));
      showError('Validation Error', 'Please add at least one photo');
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
      
      // Add photos
      photos.forEach((photo, index) => {
        formDataToSend.append('photos', photo);
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

            {/* Photo Upload */}
            <div>
              <label className="block text-sm font-medium text-white mb-2">
                Photos * (1-10 photos)
              </label>
              <input
                type="file"
                multiple
                accept="image/jpeg,image/jpg,image/png,image/webp"
                onChange={handlePhotoChange}
                disabled={loading}
                className="hidden"
                id="photo-upload"
              />
              <label
                htmlFor="photo-upload"
                className={`w-full min-h-[120px] border-2 border-dashed rounded-xl flex flex-col items-center justify-center cursor-pointer transition-all duration-300 ${
                  errors.photos 
                    ? 'border-red-500/50 bg-red-500/5' 
                    : 'border-white/20 bg-white/5 hover:bg-white/10 hover:border-violet-500/50'
                }`}
              >
                <div className="text-center p-6">
                  <div className="w-16 h-16 bg-violet-500/20 rounded-full flex items-center justify-center mx-auto mb-4">
                    <svg className="w-8 h-8 text-violet-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                  <p className="text-white font-medium mb-1">
                    Click to upload photos
                  </p>
                  <p className="text-white/70 text-sm">
                    JPEG, PNG, WebP • Max 5MB each • 1-10 photos
                  </p>
                </div>
              </label>

              {/* Photo Previews */}
              {photoPreviews.length > 0 && (
                <div className="mt-4 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                  {photoPreviews.map((preview, index) => (
                    <div key={index} className="relative group">
                      <img
                        src={preview}
                        alt={`Preview ${index + 1}`}
                        className="w-full h-24 object-cover rounded-lg"
                      />
                      <button
                        type="button"
                        onClick={() => removePhoto(index)}
                        className="absolute -top-2 -right-2 w-6 h-6 bg-red-500 hover:bg-red-600 text-white rounded-full flex items-center justify-center text-sm opacity-0 group-hover:opacity-100 transition-opacity duration-200"
                      >
                        ×
                      </button>
                      {index === 0 && (
                        <div className="absolute bottom-1 left-1 bg-violet-500 text-white text-xs px-2 py-1 rounded">
                          Main
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}

              {errors.photos && (
                <p className="text-red-400 text-sm mt-2">{errors.photos}</p>
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
