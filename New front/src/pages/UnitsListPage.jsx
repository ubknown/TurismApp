import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { 
  Search, 
  MapPin, 
  Star, 
  Users, 
  Wifi, 
  Car, 
  Coffee, 
  Mountain,
  Filter,
  SlidersHorizontal,
  Calendar,
  ArrowUpDown,
  ChevronDown
} from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';
import InputField from '../components/InputField';
import CountyDropdown from '../components/CountyDropdown';
import TypeDropdown from '../components/TypeDropdown';
import BookingForm from '../components/BookingForm';
import { useToast } from '../context/ToastContext';
import api from '../services/axios';

const UnitsListPage = () => {
  const navigate = useNavigate();
  const { success, error: showError, clearToastsByType } = useToast();
  const [searchParams, setSearchParams] = useSearchParams();
  const [units, setUnits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState(searchParams.get('search') || '');
  const [filters, setFilters] = useState({
    county: '',
    type: '',
    minPrice: '',
    maxPrice: '',
    capacity: '',
    minRating: '',
    amenities: [],
    checkIn: '',
    checkOut: ''
  });
  const [showFilters, setShowFilters] = useState(false);
  const [showBookingForm, setShowBookingForm] = useState(false);
  const [selectedUnit, setSelectedUnit] = useState(null);
  const [sortBy, setSortBy] = useState('name-asc'); // Default: Alphabetical A-Z

  // ✅ Sorting options configuration
  const sortOptions = [
    { value: 'name-asc', label: 'Alphabetical (A–Z)', icon: '🔤' },
    { value: 'name-desc', label: 'Alphabetical (Z–A)', icon: '🔢' },
    { value: 'price-asc', label: 'Price: Low to High', icon: '💰⬆️' },
    { value: 'price-desc', label: 'Price: High to Low', icon: '💰⬇️' }
  ];

  // ✅ Sorting function
  const sortUnits = (unitsToSort, sortOption) => {
    if (!unitsToSort || unitsToSort.length === 0) return unitsToSort;

    const sorted = [...unitsToSort].sort((a, b) => {
      switch (sortOption) {
        case 'name-asc':
          return (a.name || '').localeCompare(b.name || '');
        case 'name-desc':
          return (b.name || '').localeCompare(a.name || '');
        case 'price-asc':
          const priceA = a.pricePerNight || a.price || 0;
          const priceB = b.pricePerNight || b.price || 0;
          return priceA - priceB;
        case 'price-desc':
          const priceA2 = a.pricePerNight || a.price || 0;
          const priceB2 = b.pricePerNight || b.price || 0;
          return priceB2 - priceA2;
        default:
          return 0;
      }
    });

    return sorted;
  };

  useEffect(() => {
    fetchUnits();
    
    // Auto-refresh when returning to browse page if new units were added
    const handleFocus = () => {
      if (typeof Storage !== 'undefined' && localStorage.getItem('shouldRefreshUnits') === 'true') {
        localStorage.removeItem('shouldRefreshUnits');
        setTimeout(() => {
          fetchUnits();
        }, 500);
      }
    };
    
    window.addEventListener('focus', handleFocus);
    return () => window.removeEventListener('focus', handleFocus);
  }, []); // Only fetch on component mount - filters are applied manually via Search/Apply buttons

  const fetchUnits = async (customFilters = null, customSearchQuery = null) => {
    try {
      setLoading(true);
      
      // ✅ Clear any existing error notifications when starting new fetch
      clearToastsByType('error');
      
      // Use provided filters or current state
      const currentFilters = customFilters || filters;
      const currentSearch = customSearchQuery !== null ? customSearchQuery : searchQuery;
      
      // ✅ Validate dates before making API call
      if (currentFilters.checkIn && currentFilters.checkOut) {
        console.log('🗓️ Validating dates:', currentFilters.checkIn, 'to', currentFilters.checkOut);
        if (!validateDates(currentFilters.checkIn, currentFilters.checkOut)) {
          setLoading(false);
          return; // Don't proceed with search if dates are invalid
        }
        console.log('✅ Date validation passed');
      }
      
      // Build query parameters
      const params = new URLSearchParams();
      if (currentSearch) params.append('search', currentSearch);
      if (currentFilters.county) params.append('county', currentFilters.county);
      if (currentFilters.type) params.append('type', currentFilters.type);
      if (currentFilters.minPrice) params.append('minPrice', currentFilters.minPrice);
      if (currentFilters.maxPrice) params.append('maxPrice', currentFilters.maxPrice);
      if (currentFilters.capacity) params.append('capacity', currentFilters.capacity);
      if (currentFilters.minRating) params.append('minRating', currentFilters.minRating);
      if (currentFilters.amenities.length > 0) params.append('amenities', currentFilters.amenities.join(','));
      
      // ✅ Add date-based availability filters
      if (currentFilters.checkIn) {
        params.append('checkIn', currentFilters.checkIn);
        console.log('📅 Adding checkIn parameter:', currentFilters.checkIn);
      }
      if (currentFilters.checkOut) {
        params.append('checkOut', currentFilters.checkOut);
        console.log('📅 Adding checkOut parameter:', currentFilters.checkOut);
      }
      
      const url = `/api/units/public?${params.toString()}`;
      console.log('🔍 Fetching units from:', url);
      console.log('🔍 Full URL will be:', `http://localhost:8080${url}`);
      
      // 🆕 ADD DETAILED DEBUGGING
      console.log('🔍 Current filter state:', {
        checkIn: currentFilters.checkIn,
        checkOut: currentFilters.checkOut,
        county: currentFilters.county,
        type: currentFilters.type,
        capacity: currentFilters.capacity,
        minPrice: currentFilters.minPrice,
        maxPrice: currentFilters.maxPrice
      });
      
      const response = await api.get(url);
      console.log('📦 Response received:', response.data);
      console.log('📊 Units count:', response.data ? response.data.length : 'null/undefined');
      
      // 🆕 ADD MORE DEBUGGING
      console.log('📊 Response status:', response.status);
      console.log('📊 Response headers:', response.headers);
      if (response.data && Array.isArray(response.data)) {
        console.log('📊 First unit in response:', response.data[0]);
        console.log('📊 All unit IDs:', response.data.map(unit => unit.id));
      }
      
      // Ensure response.data is always an array
      const responseData = Array.isArray(response.data) ? response.data : [];
      
      // ✅ Apply sorting to the fetched units
      const sortedUnits = sortUnits(responseData, sortBy);
      setUnits(sortedUnits);
      
      // ✅ Improved notification logic - prevent stacking and show relevant messages only
      if (responseData.length > 0) {
        // ✅ Units found - clear any error toasts and show success only when meaningful
        clearToastsByType('error');
        const hasActiveFilters = currentSearch || Object.values(currentFilters).some(f => f && f.length > 0);
        if (hasActiveFilters) {
          success('Units Found', `Found ${responseData.length} matching accommodation${responseData.length > 1 ? 's' : ''}`);
        }
      } else {
        // ✅ No units found - clear success toasts and show appropriate error
        clearToastsByType('success');
        const hasActiveFilters = currentSearch || Object.values(currentFilters).some(f => f && f.length > 0);
        if (hasActiveFilters) {
          showError('No Matching Units', 'No accommodations match your search criteria. Try adjusting your filters or search terms.');
        } else {
          console.log('No units found, but no filters applied - this might be a database/backend issue');
        }
      }
    } catch (error) {
      console.error('❌ Error fetching units:', error);
      console.error('❌ Error details:', {
        message: error.message,
        status: error.response?.status,
        data: error.response?.data,
        url: error.config?.url
      });
      // ✅ Clear any success toasts on error and set units to empty
      clearToastsByType('success');
      setUnits([]);
      showError('Failed to Load Units', error.response?.data?.message || 'Could not fetch accommodation units. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleBookingCreated = (booking) => {
    success('Booking Successful', `Booking ID: ${booking.id} has been created successfully`);
    setShowBookingForm(false);
    setSelectedUnit(null);
  };

  const openBookingForm = (unit) => {
    setSelectedUnit(unit);
    setShowBookingForm(true);
  };

  const handleSearch = (e) => {
    e.preventDefault();
    // Update URL search params and trigger fetchUnits with current filters
    setSearchParams({ search: searchQuery });
    fetchUnits(filters, searchQuery);
  };

  // ✅ Fixed function to trigger search with current filter state
  const handleFilterSearch = () => {
    fetchUnits(filters, searchQuery);
  };

  const getAmenityIcon = (amenity) => {
    const icons = {
      wifi: <Wifi className="w-4 h-4" />,
      parking: <Car className="w-4 h-4" />,
      kitchen: <Coffee className="w-4 h-4" />,
      pool: <Mountain className="w-4 h-4" />
    };
    return icons[amenity] || <Coffee className="w-4 h-4" />;
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

  // Helper function to format price with proper currency
  const formatPrice = (unit) => {
    // Try different possible price field names for backwards compatibility
    const price = unit.pricePerNight || unit.price || 0;
    
    // Handle zero or null prices
    if (!price || price === 0) {
      return `Contact for price`;
    }
    
    // Format number with proper thousand separators for Romanian locale
    const formattedPrice = new Intl.NumberFormat('ro-RO', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(price);
    
    return `${formattedPrice} RON/night`;
  };

  // Helper function to get the first available image
  const getUnitImage = (unit) => {
    // First try photoUrls from DTO
    if (unit.photoUrls && unit.photoUrls.length > 0) {
      return unit.photoUrls[0];
    }
    
    // Fallback to images array (old format or alternative field)
    if (unit.images && unit.images.length > 0) {
      // Handle both string URLs and objects with url property
      const firstImage = unit.images[0];
      return typeof firstImage === 'string' ? firstImage : firstImage?.url;
    }
    
    // Return null for placeholder
    return null;
  };

  const UnitCard = ({ unit }) => {
    const unitImage = getUnitImage(unit);
    
    return (
      <GlassCard className="overflow-hidden hover:bg-white/15 transition-all duration-300 group">
        {/* Image */}
        <div className="relative h-48 bg-gradient-to-r from-violet-500 to-purple-600 overflow-hidden">
          {unitImage ? (
            <img 
              src={unitImage} 
              alt={unit.name}
              className="w-full h-full object-cover"
              onError={(e) => {
                // Fallback to gradient background if image fails to load
                e.target.style.display = 'none';
              }}
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center">
              <div className="text-white/70 text-center">
                <div className="text-4xl mb-2">🏠</div>
                <div className="text-sm">No Image Available</div>
              </div>
            </div>
          )}
          <div className="absolute inset-0 bg-black/20"></div>
          <div className="absolute top-4 right-4 bg-white/20 backdrop-blur-sm rounded-full px-4 py-2 shadow-lg">
            <span className="text-white font-bold text-sm whitespace-nowrap">
              {formatPrice(unit)}
            </span>
          </div>
          <div className="absolute bottom-4 left-4">
            <div className="flex items-center gap-2 mb-1">
              <h3 className="text-xl font-bold text-white">{unit.name}</h3>
              {unit.type && (
                <span className="px-2 py-1 text-xs font-medium bg-violet-500/80 text-white rounded-full backdrop-blur-sm">
                  {getTypeLabel(unit.type)}
                </span>
              )}
            </div>
            <div className="text-violet-200 text-sm">
              <div className="flex items-center gap-1">
                <MapPin className="w-4 h-4" />
                {unit.location}
              </div>
              {unit.county && (
                <div className="text-xs text-violet-300 mt-1">
                  {unit.county}
                </div>
              )}
            </div>
          </div>
        </div>

      {/* Content */}
      <div className="p-6">
        <p className="text-violet-200 text-sm mb-4 line-clamp-2">{unit.description}</p>
        
        {/* Stats */}
        <div className="flex items-center gap-4 mb-4 text-sm">
          <div className="flex items-center gap-1 text-violet-200">
            <Users className="w-4 h-4" />
            {unit.capacity} guests
          </div>
          <div className="flex items-center gap-1 text-yellow-400">
            <Star className="w-4 h-4 fill-current" />
            {unit.rating} ({unit.reviewCount})
          </div>
        </div>

        {/* Amenities */}
        <div className="flex items-center gap-2 mb-6">
          {unit.amenities.slice(0, 4).map((amenity, index) => (
            <div 
              key={index}
              className="p-2 rounded-lg bg-white/10 text-violet-300"
              title={amenity}
            >
              {getAmenityIcon(amenity)}
            </div>
          ))}
          {unit.amenities.length > 4 && (
            <span className="text-xs text-violet-300">+{unit.amenities.length - 4} more</span>
          )}
        </div>

        {/* Actions */}
        <div className="flex gap-3">
          <PrimaryButton
            onClick={() => navigate(`/units/${unit.id}`)}
            className="flex-1"
          >
            View Details
          </PrimaryButton>
          <PrimaryButton
            onClick={() => openBookingForm(unit)}
            variant="secondary"
            className="flex-1 flex items-center justify-center gap-2"
          >
            <Calendar className="w-4 h-4" />
            Book Now
          </PrimaryButton>
        </div>
      </div>
    </GlassCard>
    );
  };

  // ✅ Date validation helper
  const validateDates = (checkIn, checkOut) => {
    if (!checkIn || !checkOut) return true; // Allow empty dates
    
    const checkInDate = new Date(checkIn);
    const checkOutDate = new Date(checkOut);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (checkInDate < today) {
      showError('Invalid Check-in Date', 'Check-in date cannot be in the past');
      return false;
    }
    
    if (checkOutDate <= checkInDate) {
      showError('Invalid Check-out Date', 'Check-out date must be after check-in date');
      return false;
    }
    
    return true;
  };

  // ✅ Enhanced date filter handlers without automatic filtering
  const handleCheckInChange = (e) => {
    const newCheckIn = e.target.value;
    setFilters(prev => ({ ...prev, checkIn: newCheckIn }));
    
    // Clear check-out if it becomes invalid
    if (newCheckIn && filters.checkOut && newCheckIn >= filters.checkOut) {
      setFilters(prev => ({ ...prev, checkOut: '' }));
    }
  };

  const handleCheckOutChange = (e) => {
    const newCheckOut = e.target.value;
    setFilters(prev => ({ ...prev, checkOut: newCheckOut }));
  };

  // ✅ Enhanced clear filters function with notification management
  const handleClearFilters = () => {
    clearToastsByType('error'); // Clear any "No Units Found" errors
    const clearedFilters = {
      county: '',
      type: '',
      minPrice: '',
      maxPrice: '',
      capacity: '',
      minRating: '',
      amenities: [],
      checkIn: '',
      checkOut: ''
    };
    setFilters(clearedFilters);
    setSearchQuery('');
    // 🆕 AUTO-FETCH after clearing filters to show all units
    setTimeout(() => {
      fetchUnits(clearedFilters, '');
    }, 100);
  };

  // ✅ Handle sort change without API call - just re-sort current units
  const handleSortChange = (newSortBy) => {
    setSortBy(newSortBy);
    // Re-sort existing units immediately for better UX
    setUnits(prevUnits => sortUnits(prevUnits, newSortBy));
  };

  // ✅ Check if any filters are active (for enabling Apply Filters button)
  const hasActiveFilters = () => {
    return (
      searchQuery ||
      filters.county ||
      filters.type ||
      filters.minPrice ||
      filters.maxPrice ||
      filters.capacity ||
      filters.minRating ||
      filters.checkIn ||
      filters.checkOut ||
      (filters.amenities && filters.amenities.length > 0)
    );
  };

  // ✅ Check if Apply Filters button should be enabled
  const isApplyFiltersEnabled = () => {
    const hasFilters = hasActiveFilters();
    
    // If no filters, disable button
    if (!hasFilters) return false;
    
    // If dates are provided, validate them
    if (filters.checkIn && filters.checkOut) {
      return validateDates(filters.checkIn, filters.checkOut);
    }
    
    // If only one date is provided, that's invalid
    if (filters.checkIn || filters.checkOut) {
      return false;
    }
    
    // Other filters are valid
    return true;
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-violet-400"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen p-4 sm:p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header with better mobile spacing */}
        <div className="mb-6 sm:mb-8 text-center sm:text-left">
          <h1 className="text-3xl sm:text-4xl font-bold text-white mb-2">Accommodation Units</h1>
          <p className="text-violet-200 text-sm sm:text-base">Discover amazing places to stay for your next adventure.</p>
        </div>

        {/* Search and Filters */}
        <div className="mb-8">
          {/* Main search bar with improved layout */}
          <div className="flex flex-col sm:flex-row gap-4 mb-4">
            {/* Search */}
            <form onSubmit={handleSearch} className="flex-1">
              <div className="flex gap-3">
                <div className="flex-1">
                  <InputField
                    type="text"
                    placeholder="Search by name, location, or description..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    icon={Search}
                  />
                </div>
                <PrimaryButton type="submit" className="px-6">
                  Search
                </PrimaryButton>
              </div>
            </form>

            {/* Filter and Sort Controls */}
            <div className="flex flex-col sm:flex-row gap-3 justify-center sm:justify-end">
              {/* Sort Dropdown */}
              <div className="relative">
                <select
                  value={sortBy}
                  onChange={(e) => handleSortChange(e.target.value)}
                  className="appearance-none w-full sm:w-auto px-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300 pr-10 cursor-pointer"
                >
                  {sortOptions.map(option => (
                    <option key={option.value} value={option.value} className="bg-slate-800 text-white">
                      {option.label}
                    </option>
                  ))}
                </select>
                <div className="absolute right-3 top-1/2 transform -translate-y-1/2 pointer-events-none">
                  <ArrowUpDown className="w-4 h-4 text-white/60" />
                </div>
              </div>

              {/* Filter Toggle */}
              <PrimaryButton
                onClick={() => setShowFilters(!showFilters)}
                variant="secondary"
                className={`flex items-center gap-2 px-6 transition-all duration-300 ${
                  showFilters ? 'bg-violet-500/20 border-violet-400' : ''
                }`}
              >
                <SlidersHorizontal className="w-4 h-4" />
                Filters
                {hasActiveFilters() && (
                  <span className="ml-1 w-2 h-2 bg-violet-400 rounded-full"></span>
                )}
              </PrimaryButton>
            </div>
          </div>

          {/* Filter Panel */}
          {showFilters && (
            <GlassCard className="mt-4 p-6">
              <h3 className="text-lg font-semibold text-white mb-6 flex items-center gap-2">
                <Filter className="w-5 h-5" />
                Filter Accommodations
              </h3>
              
              {/* First Row: County, Type, Check-in, Check-out, Guests */}
              <div className="flex flex-wrap gap-4 w-full items-end mb-4">
                {/* County Filter */}
                <div className="flex flex-col min-w-[200px] flex-1">
                  <label className="block text-sm font-medium text-white/80 mb-2 flex items-center">
                    <MapPin className="w-4 h-4 inline mr-1" />
                    County / Județ
                  </label>
                  <CountyDropdown
                    value={filters.county}
                    onChange={(e) => setFilters(prev => ({ ...prev, county: e.target.value }))}
                    name="county"
                    placeholder="All Counties"
                    className="w-full min-w-[200px] h-12 rounded-lg px-3 bg-[#21006b1a] border border-[#3a1858] text-white focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                  />
                </div>

                {/* Type of Accommodation */}
                <div className="flex flex-col min-w-[200px] flex-1">
                  <label className="block text-sm font-medium text-white/80 mb-2 flex items-center">
                    🏠 Type of Accommodation
                  </label>
                  <TypeDropdown
                    value={filters.type}
                    onChange={(e) => setFilters(prev => ({ ...prev, type: e.target.value }))}
                    name="type"
                    placeholder="All Types"
                    className="w-full min-w-[200px] h-12 rounded-lg px-3 bg-[#21006b1a] border border-[#3a1858] text-white focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                  />
                </div>

                {/* Check-in Date */}
                <div className="flex flex-col min-w-[200px] flex-1">
                  <label className="block text-sm font-medium text-white/80 mb-2 flex items-center">
                    <Calendar className="w-4 h-4 inline mr-1" />
                    Check-in
                  </label>
                  <input
                    type="date"
                    value={filters.checkIn}
                    min={new Date().toISOString().split('T')[0]}
                    onChange={handleCheckInChange}
                    className="w-full min-w-[200px] h-12 rounded-lg px-3 bg-[#21006b1a] border border-[#3a1858] text-white focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                  />
                </div>

                {/* Check-out Date */}
                <div className="flex flex-col min-w-[200px] flex-1">
                  <label className="block text-sm font-medium text-white/80 mb-2 flex items-center">
                    <Calendar className="w-4 h-4 inline mr-1" />
                    Check-out
                  </label>
                  <input
                    type="date"
                    value={filters.checkOut}
                    min={filters.checkIn || new Date().toISOString().split('T')[0]}
                    onChange={handleCheckOutChange}
                    className="w-full min-w-[200px] h-12 rounded-lg px-3 bg-[#21006b1a] border border-[#3a1858] text-white focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                  />
                </div>

                {/* Guests */}
                <div className="flex flex-col min-w-[200px] flex-1">
                  <label className="block text-sm font-medium text-white/80 mb-2 flex items-center">
                    <Users className="w-4 h-4 inline mr-1" />
                    Guests
                  </label>
                  <input
                    type="number"
                    placeholder="Number of guests"
                    min="1"
                    value={filters.capacity}
                    onChange={(e) => setFilters(prev => ({ ...prev, capacity: e.target.value }))}
                    className="w-full min-w-[200px] h-12 rounded-lg px-3 bg-[#21006b1a] border border-[#3a1858] text-white placeholder-white/60 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                  />
                </div>
              </div>

              {/* Second Row: Min Price, Max Price, Min Rating, Clear Button */}
              <div className="flex flex-wrap gap-4 w-full items-end">
                {/* Min Price */}
                <div className="flex flex-col min-w-[200px] flex-1">
                  <label className="block text-sm font-medium text-white/80 mb-2 flex items-center">
                    💰 Min Price (RON)
                  </label>
                  <input
                    type="number"
                    placeholder="Min price"
                    min="0"
                    value={filters.minPrice}
                    onChange={(e) => setFilters(prev => ({ ...prev, minPrice: e.target.value }))}
                    className="w-full min-w-[200px] h-12 rounded-lg px-3 bg-[#21006b1a] border border-[#3a1858] text-white placeholder-white/60 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                  />
                </div>

                {/* Max Price */}
                <div className="flex flex-col min-w-[200px] flex-1">
                  <label className="block text-sm font-medium text-white/80 mb-2 flex items-center">
                    💰 Max Price (RON)
                  </label>
                  <input
                    type="number"
                    placeholder="Max price"
                    min="0"
                    value={filters.maxPrice}
                    onChange={(e) => setFilters(prev => ({ ...prev, maxPrice: e.target.value }))}
                    className="w-full min-w-[200px] h-12 rounded-lg px-3 bg-[#21006b1a] border border-[#3a1858] text-white placeholder-white/60 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                  />
                </div>

                {/* Min Rating */}
                <div className="flex flex-col min-w-[200px] flex-1">
                  <label className="block text-sm font-medium text-white/80 mb-2 flex items-center">
                    ⭐ Min Rating
                  </label>
                  <select
                    value={filters.minRating}
                    onChange={(e) => setFilters(prev => ({ ...prev, minRating: e.target.value }))}
                    className="w-full min-w-[200px] h-12 rounded-lg px-3 bg-[#21006b1a] border border-[#3a1858] text-white focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300"
                  >
                    <option value="" className="bg-slate-800 text-white">All Ratings</option>
                    <option value="1" className="bg-slate-800 text-white">1+ Stars</option>
                    <option value="2" className="bg-slate-800 text-white">2+ Stars</option>
                    <option value="3" className="bg-slate-800 text-white">3+ Stars</option>
                    <option value="4" className="bg-slate-800 text-white">4+ Stars</option>
                    <option value="4.5" className="bg-slate-800 text-white">4.5+ Stars</option>
                  </select>
                </div>

                {/* Clear All Filters Button */}
                <div className="flex flex-col min-w-[200px] flex-1">
                  <label className="block text-sm font-medium text-white/80 mb-2 opacity-0">
                    Actions
                  </label>
                  <div className="flex gap-2">
                    <button
                      onClick={handleClearFilters}
                      className="flex-1 min-w-[100px] h-12 rounded-lg px-3 bg-violet-500/20 hover:bg-violet-500/30 border border-violet-500/30 text-white transition-all duration-300 font-medium"
                    >
                      Clear Filters
                    </button>
                    <button
                      onClick={handleFilterSearch}
                      disabled={!isApplyFiltersEnabled()}
                      className={`flex-1 min-w-[100px] h-12 rounded-lg px-3 transition-all duration-300 font-medium ${
                        isApplyFiltersEnabled()
                          ? 'bg-blue-500 hover:bg-blue-600 border border-blue-500 text-white cursor-pointer shadow-lg'
                          : 'bg-gray-500/20 border border-gray-500/30 text-gray-400 cursor-not-allowed'
                      }`}
                    >
                      Apply Filters
                    </button>
                  </div>
                </div>
              </div>
            </GlassCard>
          )}
        </div>

        {/* Results with enhanced formatting */}
        <div className="mb-6 flex flex-col sm:flex-row sm:justify-between sm:items-center gap-2">
          <p className="text-violet-200 text-sm sm:text-base">
            Showing <span className="font-semibold text-white">{units.length}</span> accommodation unit{units.length !== 1 ? 's' : ''}
          </p>
          {units.length > 0 && (
            <div className="text-xs sm:text-sm text-violet-300 flex flex-col sm:flex-row gap-1 sm:gap-3">
              <div className="flex items-center gap-1">
                <ArrowUpDown className="w-3 h-3" />
                <span>Sorted by: {sortOptions.find(opt => opt.value === sortBy)?.label}</span>
              </div>
              {(searchQuery || Object.values(filters).some(f => f)) && (
                <div>
                  {searchQuery && (
                    <span>for "{searchQuery}" </span>
                  )}
                  {Object.values(filters).some(f => f) && (
                    <span>with filters applied</span>
                  )}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Units Grid */}
        {units.length > 0 ? (
          <>
            <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-8">
              {units.map((unit) => (
                <UnitCard key={unit.id} unit={unit} />
              ))}
            </div>

            {/* Load More */}
            <div className="mt-12 text-center">
              <PrimaryButton variant="secondary">
                Load More Units
              </PrimaryButton>
            </div>
          </>
        ) : (
          <div className="text-center py-12">
            <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl p-8 max-w-md mx-auto">
              <div className="w-16 h-16 bg-white/10 rounded-full flex items-center justify-center mx-auto mb-4">
                <Search className="w-8 h-8 text-violet-400" />
              </div>
              <h3 className="text-xl font-semibold text-white mb-2">No Units Found</h3>
              <p className="text-violet-200 mb-4">
                {searchQuery || Object.values(filters).some(f => f) 
                  ? 'No accommodation units match your search criteria. Try adjusting your filters.'
                  : 'No accommodation units are currently available.'
                }
              </p>
              {(searchQuery || Object.values(filters).some(f => f)) && (
                <PrimaryButton
                  onClick={handleClearFilters}
                  variant="secondary"
                >
                  Clear Filters
                </PrimaryButton>
              )}
            </div>
          </div>
        )}
      </div>
      
      {/* Booking Form Modal */}
      {showBookingForm && selectedUnit && (
        <BookingForm
          unitId={selectedUnit.id}
          unitName={selectedUnit.name}
          unitPrice={selectedUnit.pricePerNight || selectedUnit.price || 0}
          onClose={() => setShowBookingForm(false)}
          onBookingCreated={handleBookingCreated}
        />
      )}
    </div>
  );
};

export default UnitsListPage;
