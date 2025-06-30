import React, { useState } from 'react';
import { X, AlertTriangle, Calendar } from 'lucide-react';
import { useToast } from '../context/ToastContext';
import api from '../services/axios';

const BookingCancelButton = ({ booking, onCancelled, userRole = 'guest', compact = false }) => {
  const { success, error: showError } = useToast();
  const [showConfirm, setShowConfirm] = useState(false);
  const [cancelling, setCancelling] = useState(false);

  // Check if booking can be cancelled
  const canCancel = booking.status === 'CONFIRMED' || booking.status === 'PENDING';
  
  if (!canCancel) {
    return null; // Don't show cancel button for completed/cancelled bookings
  }

  const handleCancel = async () => {
    setCancelling(true);
    try {
      console.log('🔵 Cancelling booking:', booking.id);
      
      const response = await api.put(`/api/bookings/${booking.id}/cancel`);
      
      console.log('✅ Booking cancelled:', response.data);
      
      success('Booking Cancelled', 'The booking has been cancelled successfully. Email notifications have been sent.');
      
      // Call parent callback to update the booking list
      if (onCancelled) {
        onCancelled(booking.id, response.data);
      }
      
      setShowConfirm(false);
    } catch (error) {
      console.error('❌ Error cancelling booking:', error);
      
      let errorMessage = 'Could not cancel the booking';
      if (error.response?.data) {
        if (typeof error.response.data === 'string') {
          errorMessage = error.response.data;
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message;
        }
      }
      
      showError('Cancellation Failed', errorMessage);
    } finally {
      setCancelling(false);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  if (compact) {
    return (
      <>
        <button
          onClick={() => setShowConfirm(true)}
          disabled={cancelling}
          className="inline-flex items-center gap-1 px-3 py-1.5 bg-red-500/20 hover:bg-red-500/30 border border-red-400/30 text-red-400 text-sm rounded-lg transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <X className="w-3 h-3" />
          Cancel
        </button>

        {/* Confirmation Modal */}
        {showConfirm && (
          <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
            <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl p-6 max-w-md w-full">
              <div className="flex items-center gap-3 mb-4">
                <div className="p-2 bg-red-500/20 rounded-full">
                  <AlertTriangle className="w-5 h-5 text-red-400" />
                </div>
                <h3 className="text-lg font-semibold text-white">Cancel Booking</h3>
              </div>
              
              <p className="text-violet-200 mb-4">
                Are you sure you want to cancel this booking? This action cannot be undone.
              </p>
              
              <div className="bg-white/5 rounded-lg p-3 mb-6 text-sm">
                <div className="text-white font-medium">{booking.accommodationUnit?.name || 'N/A'}</div>
                <div className="text-violet-200 flex items-center gap-1 mt-1">
                  <Calendar className="w-3 h-3" />
                  {formatDate(booking.checkInDate)} - {formatDate(booking.checkOutDate)}
                </div>
                <div className="text-violet-300 mt-1">
                  Guest: {booking.guestName || booking.guestEmail}
                </div>
              </div>
              
              <div className="flex gap-3">
                <button
                  onClick={() => setShowConfirm(false)}
                  className="flex-1 px-4 py-2 bg-white/10 hover:bg-white/20 border border-white/20 text-white rounded-lg transition-all duration-300"
                >
                  Keep Booking
                </button>
                <button
                  onClick={handleCancel}
                  disabled={cancelling}
                  className="flex-1 px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {cancelling ? 'Cancelling...' : 'Cancel Booking'}
                </button>
              </div>
            </div>
          </div>
        )}
      </>
    );
  }

  return (
    <>
      <button
        onClick={() => setShowConfirm(true)}
        disabled={cancelling}
        className="inline-flex items-center gap-2 px-4 py-2 bg-red-500/20 hover:bg-red-500/30 border border-red-400/30 text-red-400 rounded-lg transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <X className="w-4 h-4" />
        Cancel Booking
      </button>

      {/* Confirmation Modal */}
      {showConfirm && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl p-6 max-w-lg w-full">
            <div className="flex items-center gap-3 mb-4">
              <div className="p-3 bg-red-500/20 rounded-full">
                <AlertTriangle className="w-6 h-6 text-red-400" />
              </div>
              <h3 className="text-xl font-semibold text-white">Cancel Booking</h3>
            </div>
            
            <p className="text-violet-200 mb-6">
              Are you sure you want to cancel this booking? This action cannot be undone and notification emails will be sent to all parties.
            </p>
            
            <div className="bg-white/5 rounded-lg p-4 mb-6">
              <h4 className="text-white font-medium mb-2">Booking Details:</h4>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-violet-200">Property:</span>
                  <span className="text-white">{booking.accommodationUnit?.name || 'N/A'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-violet-200">Guest:</span>
                  <span className="text-white">{booking.guestName || booking.guestEmail}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-violet-200">Check-in:</span>
                  <span className="text-white">{formatDate(booking.checkInDate)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-violet-200">Check-out:</span>
                  <span className="text-white">{formatDate(booking.checkOutDate)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-violet-200">Total:</span>
                  <span className="text-white">{booking.totalPrice?.toLocaleString()} RON</span>
                </div>
              </div>
            </div>
            
            <div className="flex gap-3">
              <button
                onClick={() => setShowConfirm(false)}
                className="flex-1 px-4 py-3 bg-white/10 hover:bg-white/20 border border-white/20 text-white rounded-lg transition-all duration-300"
              >
                Keep Booking
              </button>
              <button
                onClick={handleCancel}
                disabled={cancelling}
                className="flex-1 px-4 py-3 bg-red-500 hover:bg-red-600 text-white rounded-lg transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {cancelling ? 'Cancelling...' : 'Cancel Booking'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default BookingCancelButton;
