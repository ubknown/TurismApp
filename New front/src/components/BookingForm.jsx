import React, { useState, useEffect } from 'react';
import { Calendar, Users, DollarSign, CreditCard, User, Mail, Phone, Check } from 'lucide-react';
import { useToast } from '../context/ToastContext';
import api from '../services/axios';

const BookingForm = ({ unitId, unitName, unitPrice, onClose, onBookingCreated }) => {
  const { success, error: showError } = useToast();
  const [formData, setFormData] = useState({
    guestName: '',
    guestEmail: '',
    guestPhone: '',
    checkInDate: '',
    checkOutDate: '',
    numberOfGuests: 1,
    specialRequests: ''
  });
  const [loading, setLoading] = useState(false);
  const [totalPrice, setTotalPrice] = useState(0);

  useEffect(() => {
    calculateTotalPrice();
  }, [formData.checkInDate, formData.checkOutDate]);

  const calculateTotalPrice = () => {
    if (formData.checkInDate && formData.checkOutDate) {
      const checkIn = new Date(formData.checkInDate);
      const checkOut = new Date(formData.checkOutDate);
      const timeDiff = checkOut.getTime() - checkIn.getTime();
      const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24));
      
      if (daysDiff > 0) {
        setTotalPrice(daysDiff * unitPrice);
      } else {
        setTotalPrice(0);
      }
    } else {
      setTotalPrice(0);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const validateForm = () => {
    if (!formData.guestName.trim()) {
      showError('Validation Error', 'Guest name is required');
      return false;
    }
    if (!formData.guestEmail.trim() || !formData.guestEmail.includes('@')) {
      showError('Validation Error', 'Please enter a valid email address');
      return false;
    }
    if (!formData.checkInDate) {
      showError('Validation Error', 'Check-in date is required');
      return false;
    }
    if (!formData.checkOutDate) {
      showError('Validation Error', 'Check-out date is required');
      return false;
    }
    if (new Date(formData.checkInDate) >= new Date(formData.checkOutDate)) {
      showError('Validation Error', 'Check-out date must be after check-in date');
      return false;
    }
    if (formData.numberOfGuests < 1) {
      showError('Validation Error', 'Number of guests must be at least 1');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    
    try {
      const bookingData = {
        accommodationUnitId: unitId,
        guestName: formData.guestName.trim(),
        guestEmail: formData.guestEmail.trim(),
        guestPhone: formData.guestPhone?.trim() || null,
        checkInDate: formData.checkInDate,
        checkOutDate: formData.checkOutDate,
        numberOfGuests: parseInt(formData.numberOfGuests),
        specialRequests: formData.specialRequests?.trim() || null,
        totalPrice: totalPrice
      };

      console.log('🔵 Sending booking request:', bookingData);
      
      const response = await api.post('/api/bookings', bookingData);
      
      console.log('✅ Booking response:', response.data);
      
      success('Booking Created', `Booking for ${unitName} has been successfully created`);
      
      if (onBookingCreated) {
        onBookingCreated(response.data);
      }
      
      if (onClose) {
        onClose();
      }
    } catch (error) {
      console.error('❌ Booking error:', error);
      console.error('❌ Error response:', error.response?.data);
      
      // Enhanced error message handling
      let errorMessage = 'Could not create the booking';
      if (error.response?.data) {
        if (typeof error.response.data === 'string') {
          errorMessage = error.response.data;
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message;
        } else if (error.response.data.error) {
          errorMessage = error.response.data.error;
        }
      }
      
      showError('Booking Failed', errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const today = new Date().toISOString().split('T')[0];

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-2 sm:p-4 lg:p-6 z-50">
      {/* Scrollable container */}
      <div className="w-full h-full flex items-center justify-center overflow-y-auto py-4">
        <div className="w-full max-w-sm sm:max-w-md md:max-w-lg lg:max-w-xl xl:max-w-2xl bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl mx-auto">
          {/* Header */}
          <div className="p-4 sm:p-6 border-b border-white/10">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl sm:text-2xl font-bold text-white">Create Booking</h2>
                <p className="text-violet-200 mt-1 text-sm sm:text-base">Book your stay at {unitName}</p>
              </div>
              <button
                type="button"
                onClick={onClose}
                className="text-white/60 hover:text-white transition-colors p-2 rounded-lg hover:bg-white/10"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>

          {/* Form */}
          <div className="max-h-[70vh] overflow-y-auto">
            <form onSubmit={handleSubmit} className="p-4 sm:p-6 space-y-4 sm:space-y-6">
              {/* Guest Information */}
              <div className="space-y-3 sm:space-y-4">
                <h3 className="text-base sm:text-lg font-semibold text-white flex items-center gap-2">
                  <User className="w-4 h-4 sm:w-5 sm:h-5 text-violet-400" />
                  Guest Information
                </h3>
                
                <div>
                  <label className="block text-sm font-medium text-violet-200 mb-2">
                    Guest Name *
                  </label>
                  <input
                    type="text"
                    name="guestName"
                    value={formData.guestName}
                    onChange={handleInputChange}
                    className="glass-input w-full text-sm sm:text-base"
                    placeholder="Enter guest name"
                    required
                  />
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-violet-200 mb-2">
                      Email Address *
                    </label>
                    <input
                      type="email"
                      name="guestEmail"
                      value={formData.guestEmail}
                      onChange={handleInputChange}
                      className="glass-input w-full text-sm sm:text-base"
                      placeholder="guest@example.com"
                      required
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-violet-200 mb-2">
                      Phone Number
                    </label>
                    <input
                      type="tel"
                      name="guestPhone"
                      value={formData.guestPhone}
                      onChange={handleInputChange}
                      className="glass-input w-full text-sm sm:text-base"
                      placeholder="+1 (555) 123-4567"
                    />
                  </div>
                </div>
              </div>

              {/* Booking Details */}
              <div className="space-y-3 sm:space-y-4">
                <h3 className="text-base sm:text-lg font-semibold text-white flex items-center gap-2">
                  <Calendar className="w-4 h-4 sm:w-5 sm:h-5 text-violet-400" />
                  Booking Details
                </h3>
                
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-violet-200 mb-2">
                      Check-in Date *
                    </label>
                    <input
                      type="date"
                      name="checkInDate"
                      value={formData.checkInDate}
                      onChange={handleInputChange}
                      min={today}
                      className="glass-input w-full text-sm sm:text-base"
                      required
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-violet-200 mb-2">
                      Check-out Date *
                    </label>
                    <input
                      type="date"
                      name="checkOutDate"
                      value={formData.checkOutDate}
                      onChange={handleInputChange}
                      min={formData.checkInDate || today}
                      className="glass-input w-full text-sm sm:text-base"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-violet-200 mb-2">
                    Number of Guests *
                  </label>
                  <input
                    type="number"
                    name="numberOfGuests"
                    value={formData.numberOfGuests}
                    onChange={handleInputChange}
                    min="1"
                    max="20"
                    className="glass-input w-full text-sm sm:text-base"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-violet-200 mb-2">
                    Special Requests
                  </label>
                  <textarea
                    name="specialRequests"
                    value={formData.specialRequests}
                    onChange={handleInputChange}
                    rows="3"
                    className="glass-input w-full resize-none text-sm sm:text-base"
                    placeholder="Any special requests or notes..."
                  />
                </div>
              </div>

              {/* Price Summary */}
              {totalPrice > 0 && (
                <div className="bg-white/5 backdrop-blur-md border border-white/10 rounded-xl p-4">
                  <div className="flex items-center justify-between flex-wrap gap-2">
                    <span className="text-violet-200 text-sm sm:text-base">Total Price:</span>
                    <span className="text-xl sm:text-2xl font-bold text-white flex items-center gap-1">
                      <DollarSign className="w-4 h-4 sm:w-5 sm:h-5" />
                      {totalPrice}
                    </span>
                  </div>
                  <p className="text-xs sm:text-sm text-violet-300 mt-1">
                    ${unitPrice}/night × {Math.ceil((new Date(formData.checkOutDate) - new Date(formData.checkInDate)) / (1000 * 3600 * 24))} nights
                  </p>
                </div>
              )}

              {/* Actions */}
              <div className="flex flex-col sm:flex-row gap-3 pt-4">
                <button
                  type="button"
                  onClick={onClose}
                  className="w-full sm:flex-1 glass-button bg-white/5 hover:bg-white/10 text-sm sm:text-base"
                  disabled={loading}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading || totalPrice === 0}
                  className="w-full sm:flex-1 aurora-gradient text-white font-semibold py-3 px-6 rounded-xl hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 text-sm sm:text-base"
                >
                  {loading ? (
                    <div className="flex items-center justify-center gap-2">
                      <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                      Creating...
                    </div>
                  ) : (
                    <div className="flex items-center justify-center gap-2">
                      <Check className="w-4 h-4" />
                      Create Booking
                    </div>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookingForm;
