import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ArrowLeft, 
  MapPin, 
  Users, 
  DollarSign, 
  Calendar,
  Star,
  Wifi,
  Car,
  Coffee,
  Tv,
  AirVent,
  Bath,
  Loader2,
  Heart,
  Share2
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';
import BookingForm from '../components/BookingForm';
import ReviewSystem from '../components/ReviewSystem';
import api from '../services/axios';

const UnitDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isGuest } = useAuth();
  const { success, error: showError } = useToast();
  const [unit, setUnit] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showBookingForm, setShowBookingForm] = useState(false);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  useEffect(() => {
    fetchUnitDetails();
  }, [id]);

  const fetchUnitDetails = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/api/units/${id}`);
      setUnit(response.data);
    } catch (err) {
      showError('Failed to Load Unit', err.response?.data?.message || 'Could not fetch unit details');
      navigate('/units');
    } finally {
      setLoading(false);
    }
  };

  const handleBookNowClick = () => {
    if (!user) {
      // Store current location to redirect back after login
      localStorage.setItem('redirectAfterLogin', `/units/${id}`);
      navigate('/login');
      return;
    }
    
    if (isGuest()) {
      setShowBookingForm(true);
    }
  };

  // Helper function to get accommodation type label
  const getTypeLabel = (type) => {
    const typeLabels = {
      'HOTEL': 'Hotel',
      'PENSIUNE': 'Pensiune',
      'CABANA': 'Cabana',
      'VILA': 'Vila',
      'APARTAMENT': 'Apartament',
      'CASA_DE_VACANTA': 'Casă de vacanță',
      'HOSTEL': 'Hostel',
      'MOTEL': 'Motel',
      'CAMPING': 'Camping',
      'BUNGALOW': 'Bungalow'
    };
    return typeLabels[type] || type;
  };

  const handleBooking = () => {
    success('Booking Request Sent', 'Your booking request has been submitted successfully');
    setShowBookingForm(false);
  };

  const getAmenityIcon = (amenity) => {
    const icons = {
      'WiFi': Wifi,
      'Parking': Car,
      'Kitchen': Coffee,
      'TV': Tv,
      'Air Conditioning': AirVent,
      'Bathroom': Bath,
    };
    return icons[amenity] || Coffee;
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="w-12 h-12 text-violet-400 animate-spin" />
      </div>
    );
  }

  if (!unit) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <GlassCard className="p-8 text-center">
          <h2 className="text-2xl font-bold text-white mb-4">Unit Not Found</h2>
          <p className="text-white/70 mb-6">The accommodation unit you're looking for doesn't exist.</p>
          <PrimaryButton onClick={() => navigate('/units')}>
            Back to Units
          </PrimaryButton>
        </GlassCard>
      </div>
    );
  }

  const images = unit.photoUrls && unit.photoUrls.length > 0 
    ? unit.photoUrls 
    : ['/api/placeholder/800/600'];

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-white hover:text-violet-400 transition-colors duration-200"
        >
          <ArrowLeft className="w-5 h-5" />
          Back
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-8">
          {/* Image Gallery */}
          <GlassCard className="p-6">
            <div className="space-y-4">
              {/* Main Image */}
              <div className="aspect-video rounded-lg overflow-hidden bg-white/10">
                <img
                  src={images[currentImageIndex]?.startsWith('http') 
                    ? images[currentImageIndex] 
                    : `http://localhost:8080${images[currentImageIndex]}`}
                  alt={unit.name}
                  className="w-full h-full object-cover"
                />
              </div>

              {/* Thumbnail Navigation */}
              {images.length > 1 && (
                <div className="flex gap-2 overflow-x-auto">
                  {images.map((image, index) => (
                    <button
                      key={index}
                      onClick={() => setCurrentImageIndex(index)}
                      className={`flex-shrink-0 w-20 h-16 rounded-lg overflow-hidden border-2 transition-colors duration-200 ${
                        index === currentImageIndex 
                          ? 'border-violet-400' 
                          : 'border-white/20 hover:border-violet-400/50'
                      }`}
                    >
                      <img
                        src={image.startsWith('http') ? image : `http://localhost:8080${image}`}
                        alt={`${unit.name} ${index + 1}`}
                        className="w-full h-full object-cover"
                      />
                    </button>
                  ))}
                </div>
              )}
            </div>
          </GlassCard>

          {/* Unit Info */}
          <GlassCard className="p-6">
            <div className="space-y-6">
              <div>
                <div className="flex items-center gap-3 mb-2">
                  <h1 className="text-3xl font-bold text-white">{unit.name}</h1>
                  {unit.type && (
                    <span className="px-3 py-1 text-sm font-medium bg-violet-500/80 text-white rounded-full backdrop-blur-sm">
                      {getTypeLabel(unit.type)}
                    </span>
                  )}
                </div>
                <div className="flex items-center gap-4 text-white/70 flex-wrap">
                  <div className="flex items-center gap-1">
                    <MapPin className="w-4 h-4" />
                    <span>{unit.location}</span>
                  </div>
                  {unit.county && (
                    <div className="flex items-center gap-1">
                      <span className="text-violet-300">•</span>
                      <span className="font-medium text-violet-300">{unit.county}</span>
                    </div>
                  )}
                  <div className="flex items-center gap-1">
                    <Users className="w-4 h-4" />
                    <span>{unit.capacity} guests</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <Star className="w-4 h-4 text-yellow-400 fill-yellow-400" />
                    <span>{unit.rating?.toFixed(1) || 'New'}</span>
                  </div>
                </div>
              </div>

              <div className="border-t border-white/10 pt-6">
                <h3 className="text-lg font-semibold text-white mb-3">Description</h3>
                <p className="text-white/80 leading-relaxed">{unit.description}</p>
              </div>

              {/* Amenities */}
              {unit.amenities && unit.amenities.length > 0 && (
                <div className="border-t border-white/10 pt-6">
                  <h3 className="text-lg font-semibold text-white mb-4">Amenities</h3>
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                    {unit.amenities.map((amenity, index) => {
                      const IconComponent = getAmenityIcon(amenity);
                      return (
                        <div key={index} className="flex items-center gap-3 p-3 bg-white/5 rounded-lg">
                          <IconComponent className="w-5 h-5 text-violet-400" />
                          <span className="text-white/80">{amenity}</span>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* Host Info */}
              <div className="border-t border-white/10 pt-6">
                <h3 className="text-lg font-semibold text-white mb-3">Hosted by</h3>
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 bg-violet-500/20 rounded-full flex items-center justify-center">
                    <span className="text-violet-400 font-semibold">
                      {unit.owner?.firstName?.[0]}{unit.owner?.lastName?.[0]}
                    </span>
                  </div>
                  <div>
                    <h4 className="font-medium text-white">
                      {unit.owner?.firstName} {unit.owner?.lastName}
                    </h4>
                    <p className="text-white/70 text-sm">Host since {new Date(unit.createdAt).getFullYear()}</p>
                  </div>
                </div>
              </div>
            </div>
          </GlassCard>

          {/* Reviews Section */}
          <ReviewSystem unitId={id} unitName={unit.name} />
        </div>

        {/* Sidebar - Sticky Price, Book Now & Property Details Card */}
        <div className="space-y-6">
          <div className="sticky top-24 z-40">
            <GlassCard className="p-6 shadow-xl">
              <div className="space-y-6">
                {/* Price Section */}
                <div className="text-center">
                  {unit.pricePerNight > 0 && (
                    <div className="text-3xl font-bold text-white mb-2">
                      {unit.pricePerNight} RON
                      <span className="text-lg font-normal text-white/70">/night</span>
                    </div>
                  )}
                  {unit.rating > 0 && unit.reviewCount > 0 && (
                    <div className="flex items-center justify-center gap-1">
                      <Star className="w-4 h-4 text-yellow-400 fill-yellow-400" />
                      <span className="text-white/70">{unit.rating.toFixed(1)} • {unit.reviewCount} reviews</span>
                    </div>
                  )}
                </div>

                {/* Book Now Button */}
                <div className="space-y-4">
                  <PrimaryButton 
                    onClick={handleBookNowClick}
                    className="w-full flex flex-col items-center gap-2"
                  >
                    <Calendar className="w-4 h-4" />
                    Book Now
                  </PrimaryButton>
                  {!user && (
                    <p className="text-white/70 text-sm text-center">
                      Sign in required for booking
                    </p>
                  )}
                </div>

                {/* Property Details Section */}
                <div className="border-t border-white/10 pt-6">
                  <h3 className="text-lg font-semibold text-white mb-4">Property Details</h3>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <span className="text-white/70">Property Type</span>
                      <span className="text-white font-medium">{getTypeLabel(unit.type) || 'Accommodation'}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-white/70">Capacity</span>
                      <span className="text-white font-medium">{unit.capacity} guests</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-white/70">Status</span>
                      <span className={`font-medium ${unit.available ? 'text-green-400' : 'text-red-400'}`}>
                        {unit.available ? 'Available' : 'Not Available'}
                      </span>
                    </div>
                    {unit.totalBookings > 0 && (
                      <div className="flex items-center justify-between">
                        <span className="text-white/70">Total Bookings</span>
                        <span className="text-white font-medium">{unit.totalBookings}</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </GlassCard>
          </div>
        </div>
      </div>
      
      {/* Booking Form Modal */}
      {showBookingForm && (
        <BookingForm
          unitId={unit.id}
          unitName={unit.name}
          unitPrice={unit.pricePerNight}
          onClose={() => setShowBookingForm(false)}
          onBookingCreated={handleBooking}
        />
      )}
    </div>
  );
};

export default UnitDetailsPage;
