import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Calendar, 
  Clock, 
  MapPin, 
  User, 
  CreditCard, 
  CheckCircle, 
  XCircle, 
  Trash2, 
  Eye,
  Star,
  MessageSquare,
  Send,
  Loader2
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';
import api from '../services/axios';

const BookingsPage = () => {
  const navigate = useNavigate();
  const { isGuest, isOwner, isAdmin } = useAuth();
  const { success, error: showError } = useToast();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showReviewForm, setShowReviewForm] = useState(null);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);

  useEffect(() => {
    fetchBookings();
  }, []);

  const fetchBookings = async () => {
    try {
      setLoading(true);
      let endpoint = '/api/bookings';
      
      // Different endpoints based on role
      if (isGuest()) {
        endpoint = '/api/bookings/my-bookings';
      } else if (isOwner() || isAdmin()) {
        endpoint = '/api/bookings/owner';
      }
      
      const response = await api.get(endpoint);
      // Transform backend data to match frontend expectations
      const transformedBookings = (response.data || []).map(booking => ({
        id: booking.id,
        unitId: booking.accommodationUnit?.id,
        unitName: booking.accommodationUnit?.name || 'Unknown Unit',
        unitLocation: booking.accommodationUnit?.location || 'Unknown Location',
        checkInDate: booking.checkInDate,
        checkOutDate: booking.checkOutDate,
        guestName: booking.guestName,
        guestEmail: booking.guestEmail,
        userEmail: booking.guestEmail, // For compatibility
        totalPrice: calculateTotalPrice(booking),
        nights: calculateNights(booking.checkInDate, booking.checkOutDate),
        status: 'CONFIRMED', // Default status since Booking model doesn't have status
        canReview: isGuest() && new Date(booking.checkOutDate) < new Date() // Can review if past checkout
      }));
      
      setBookings(transformedBookings);
    } catch (error) {
      console.error('Failed to fetch bookings:', error);
      showError('Error', 'Failed to load bookings');
      setBookings([]);
    } finally {
      setLoading(false);
    }
  };

  const calculateTotalPrice = (booking) => {
    if (!booking.accommodationUnit?.pricePerNight || !booking.checkInDate || !booking.checkOutDate) {
      return '0.00';
    }
    const nights = calculateNights(booking.checkInDate, booking.checkOutDate);
    const totalPrice = nights * booking.accommodationUnit.pricePerNight;
    return totalPrice.toFixed(2);
  };

  const calculateNights = (checkIn, checkOut) => {
    const startDate = new Date(checkIn);
    const endDate = new Date(checkOut);
    const timeDiff = endDate.getTime() - startDate.getTime();
    return Math.max(1, Math.ceil(timeDiff / (1000 * 3600 * 24)));
  };

  const handleCancelBooking = async (bookingId) => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) {
      return;
    }

    try {
      await api.delete(`/api/bookings/${bookingId}`);
      success('Booking Cancelled', 'The booking has been successfully cancelled.');
      fetchBookings(); // Refresh the bookings list
    } catch (error) {
      console.error('Failed to cancel booking:', error);
      showError('Error', 'Failed to cancel booking. Please try again.');
    }
  };

  const handleViewUnit = (unitId) => {
    navigate(`/units/${unitId}`);
  };

  const handleSubmitReview = async (booking) => {
    if (!reviewComment.trim()) {
      showError('Invalid Review', 'Please write a comment');
      return;
    }

    setSubmittingReview(true);
    try {
      await api.post('/api/reviews', {
        unitId: booking.unitId,
        rating: reviewRating,
        comment: reviewComment.trim()
      });

      success('Review Submitted', 'Thank you for your feedback!');
      setShowReviewForm(null);
      setReviewComment('');
      setReviewRating(5);
      
      // Update booking to show it's been reviewed
      setBookings(prev => prev.map(b => 
        b.id === booking.id ? { ...b, canReview: false } : b
      ));
    } catch (err) {
      showError('Submission Failed', err.response?.data?.error || 'Failed to submit review');
    } finally {
      setSubmittingReview(false);
    }
  };

  const renderStars = (rating, onRatingChange = null, size = 'w-5 h-5') => {
    return Array.from({ length: 5 }, (_, i) => (
      <button
        key={i}
        type="button"
        onClick={() => onRatingChange && onRatingChange(i + 1)}
        disabled={!onRatingChange}
        className={`${size} transition-colors duration-200 ${
          i < rating ? 'text-yellow-400 fill-yellow-400' : 'text-white/30'
        } ${onRatingChange ? 'hover:text-yellow-400 cursor-pointer' : 'cursor-default'}`}
      >
        <Star className="w-full h-full" />
      </button>
    ));
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      CONFIRMED: { color: 'text-green-300 bg-green-500/20 border-green-500/30', icon: CheckCircle },
      PENDING: { color: 'text-yellow-300 bg-yellow-500/20 border-yellow-500/30', icon: Clock },
      CANCELLED: { color: 'text-red-300 bg-red-500/20 border-red-500/30', icon: XCircle },
    };
    
    const config = statusConfig[status] || statusConfig.PENDING;
    const Icon = config.icon;
    
    return (
      <span className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium border ${config.color}`}>
        <Icon className="w-3 h-3" />
        {status}
      </span>
    );
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen p-6">
        <div className="max-w-7xl mx-auto">
          <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-8">
            <div className="flex items-center justify-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-violet-400"></div>
              <span className="ml-3 text-white">Loading bookings...</span>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-white mb-2">
            {isGuest() ? 'My Bookings' : 'Bookings Management'}
          </h1>
          <p className="text-violet-200">
            {isGuest() 
              ? 'Track your accommodation bookings and reservations'
              : 'Manage bookings for your properties'
            }
          </p>
        </div>

        {/* Bookings List */}
        {bookings.length === 0 ? (
          <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-8 text-center">
            <Calendar className="w-16 h-16 text-violet-400 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-white mb-2">
              {isGuest() ? 'No Bookings Yet' : 'No Bookings Found'}
            </h3>
            <p className="text-violet-200 mb-6">
              {isGuest() 
                ? 'Start exploring our amazing accommodations and make your first booking!'
                : 'No bookings have been made for your properties yet.'
              }
            </p>
            {isGuest() && (
              <button
                onClick={() => navigate('/units')}
                className="px-6 py-3 bg-violet-600 hover:bg-violet-700 text-white font-medium rounded-xl transition-all duration-300 transform hover:scale-105"
              >
                Browse Accommodations
              </button>
            )}
          </div>
        ) : (
          <div className="space-y-6">
            {bookings.map((booking) => (
              <div
                key={booking.id}
                className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-6 hover:bg-white/15 transition-all duration-300"
              >
                <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
                  {/* Booking Info */}
                  <div className="lg:col-span-2">
                    <div className="flex items-start justify-between mb-4">
                      <h3 className="text-lg font-semibold text-white">
                        {booking.unitName || 'Accommodation Unit'}
                      </h3>
                      {getStatusBadge(booking.status)}
                    </div>
                    
                    <div className="space-y-2 text-sm">
                      <div className="flex items-center text-violet-200">
                        <MapPin className="w-4 h-4 mr-2" />
                        {booking.unitLocation || 'Location not specified'}
                      </div>
                      {!isGuest() && (
                        <div className="flex items-center text-violet-200">
                          <User className="w-4 h-4 mr-2" />
                          Guest: {booking.guestName || booking.userEmail}
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Dates */}
                  <div>
                    <h4 className="text-sm font-medium text-white mb-2">Booking Period</h4>
                    <div className="space-y-1 text-sm text-violet-200">
                      <div>Check-in: {formatDate(booking.checkInDate)}</div>
                      <div>Check-out: {formatDate(booking.checkOutDate)}</div>
                      <div className="text-xs text-violet-300">
                        {booking.nights || 1} night{(booking.nights || 1) > 1 ? 's' : ''}
                      </div>
                    </div>
                  </div>

                  {/* Price */}
                  <div>
                    <h4 className="text-sm font-medium text-white mb-2">Total</h4>
                    <div className="flex items-center text-violet-200">
                      <CreditCard className="w-4 h-4 mr-2" />
                      <span className="text-lg font-semibold text-white">
                        ${booking.totalPrice || '0.00'}
                      </span>
                    </div>
                    <div className="text-xs text-violet-300 mt-1">
                      Booking #{booking.id}
                    </div>
                  </div>
                </div>

                {/* Action Buttons */}
                <div className="mt-6 pt-4 border-t border-white/10">
                  <div className="flex flex-wrap gap-2">
                    <button
                      onClick={() => handleViewUnit(booking.unitId)}
                      className="flex items-center gap-1 px-3 py-2 bg-violet-600/20 hover:bg-violet-600/30 text-violet-300 hover:text-violet-200 rounded-lg transition-all duration-300 text-sm"
                      disabled={!booking.unitId}
                    >
                      <Eye className="w-4 h-4" />
                      View Unit
                    </button>
                    
                    {booking.canReview && (
                      <button
                        onClick={() => setShowReviewForm(booking)}
                        className="flex items-center gap-1 px-3 py-2 bg-green-600/20 hover:bg-green-600/30 text-green-300 hover:text-green-200 rounded-lg transition-all duration-300 text-sm"
                      >
                        <MessageSquare className="w-4 h-4" />
                        Write Review
                      </button>
                    )}
                    
                    <button
                      onClick={() => handleCancelBooking(booking.id)}
                      className="flex items-center gap-1 px-3 py-2 bg-red-600/20 hover:bg-red-600/30 text-red-300 hover:text-red-200 rounded-lg transition-all duration-300 text-sm"
                    >
                      <Trash2 className="w-4 h-4" />
                      Cancel
                    </button>
                  </div>

                  {/* Review Form */}
                  {showReviewForm && showReviewForm.id === booking.id && (
                    <div className="mt-4 p-4 bg-white/5 rounded-lg border border-white/10">
                      <h4 className="text-white font-medium mb-3">Write a Review for {booking.unitName}</h4>
                      
                      <div className="space-y-4">
                        <div>
                          <label className="block text-white/80 text-sm mb-2">Rating</label>
                          <div className="flex items-center gap-1">
                            {renderStars(reviewRating, setReviewRating, 'w-6 h-6')}
                          </div>
                        </div>

                        <div>
                          <label className="block text-white/80 text-sm mb-2">Comment</label>
                          <textarea
                            value={reviewComment}
                            onChange={(e) => setReviewComment(e.target.value)}
                            placeholder="Share your experience..."
                            className="w-full p-3 bg-white/10 backdrop-blur-xl border border-white/20 rounded-lg text-white placeholder-white/50 focus:border-violet-400 focus:outline-none resize-none"
                            rows={3}
                            required
                          />
                        </div>

                        <div className="flex gap-2">
                          <button
                            onClick={() => handleSubmitReview(booking)}
                            disabled={submittingReview}
                            className="flex items-center gap-1 px-4 py-2 bg-violet-600 hover:bg-violet-700 text-white rounded-lg transition-all duration-300 text-sm disabled:opacity-50"
                          >
                            {submittingReview ? (
                              <Loader2 className="w-4 h-4 animate-spin" />
                            ) : (
                              <Send className="w-4 h-4" />
                            )}
                            {submittingReview ? 'Submitting...' : 'Submit Review'}
                          </button>
                          
                          <button
                            onClick={() => setShowReviewForm(null)}
                            className="px-4 py-2 text-white/70 hover:text-white transition-colors duration-200 text-sm"
                          >
                            Cancel
                          </button>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default BookingsPage;
