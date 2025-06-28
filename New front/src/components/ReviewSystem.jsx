import React, { useState, useEffect } from 'react';
import { Star, User, Calendar, MessageSquare, Send, Loader2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import GlassCard from './GlassCard';
import PrimaryButton from './PrimaryButton';
import api from '../services/axios';

const ReviewSystem = ({ unitId, unitName }) => {
  const { user, isGuest } = useAuth();
  const { success, error: showError } = useToast();
  const [reviews, setReviews] = useState([]);
  const [averageRating, setAverageRating] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [canReview, setCanReview] = useState(false);
  
  // Review form state
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');

  useEffect(() => {
    fetchReviews();
    fetchAverageRating();
    if (isGuest()) {
      checkCanReview();
    }
  }, [unitId]);

  const fetchReviews = async () => {
    try {
      const response = await api.get(`/api/reviews/unit/${unitId}`);
      setReviews(response.data);
    } catch (err) {
      console.error('Failed to fetch reviews:', err);
    }
  };

  const fetchAverageRating = async () => {
    try {
      const response = await api.get(`/api/reviews/unit/${unitId}/average`);
      setAverageRating(response.data || 0);
    } catch (err) {
      console.error('Failed to fetch average rating:', err);
    } finally {
      setLoading(false);
    }
  };

  const checkCanReview = async () => {
    try {
      // Check if user has completed bookings for this unit
      const response = await api.get('/api/bookings/my-bookings');
      const userBookings = response.data;
      
      const hasCompletedBooking = userBookings.some(booking => 
        booking.accommodationUnit.id === parseInt(unitId) &&
        new Date(booking.checkOutDate) < new Date()
      );

      // Check if user already reviewed this unit
      const userHasReviewed = reviews.some(review => 
        review.user.email === user.email
      );

      setCanReview(hasCompletedBooking && !userHasReviewed);
    } catch (err) {
      console.error('Failed to check review eligibility:', err);
    }
  };

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    if (!comment.trim()) {
      showError('Invalid Review', 'Please write a comment');
      return;
    }

    setSubmitting(true);
    try {
      await api.post('/api/reviews', {
        unitId: parseInt(unitId),
        rating,
        comment: comment.trim()
      });

      success('Review Submitted', 'Thank you for your feedback!');
      setShowReviewForm(false);
      setComment('');
      setRating(5);
      setCanReview(false);
      
      // Refresh reviews
      fetchReviews();
      fetchAverageRating();
    } catch (err) {
      showError('Submission Failed', err.response?.data?.error || 'Failed to submit review');
    } finally {
      setSubmitting(false);
    }
  };

  const renderStars = (rating, size = 'w-5 h-5') => {
    return Array.from({ length: 5 }, (_, i) => (
      <Star
        key={i}
        className={`${size} ${
          i < rating ? 'text-yellow-400 fill-yellow-400' : 'text-white/30'
        }`}
      />
    ));
  };

  const renderInteractiveStars = (currentRating, onRatingChange, size = 'w-6 h-6') => {
    return Array.from({ length: 5 }, (_, i) => (
      <button
        key={i}
        type="button"
        onClick={() => onRatingChange(i + 1)}
        className={`${size} transition-colors duration-200 ${
          i < currentRating ? 'text-yellow-400 fill-yellow-400' : 'text-white/30 hover:text-yellow-400'
        }`}
      >
        <Star className="w-full h-full" />
      </button>
    ));
  };

  if (loading) {
    return (
      <GlassCard className="p-6">
        <div className="flex items-center justify-center h-32">
          <Loader2 className="w-8 h-8 text-violet-400 animate-spin" />
        </div>
      </GlassCard>
    );
  }

  return (
    <div className="space-y-6">
      {/* Rating Summary */}
      <GlassCard className="p-6">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-xl font-bold text-white flex items-center gap-2">
            <MessageSquare className="w-6 h-6 text-violet-400" />
            Reviews & Ratings
          </h3>
          
          {isGuest() && canReview && (
            <PrimaryButton
              onClick={() => setShowReviewForm(!showReviewForm)}
              size="sm"
            >
              Write Review
            </PrimaryButton>
          )}
        </div>

        <div className="flex items-center gap-4 mb-6">
          <div className="text-3xl font-bold text-white">
            {averageRating.toFixed(1)}
          </div>
          <div className="flex items-center gap-1">
            {renderStars(Math.round(averageRating))}
          </div>
          <div className="text-white/70">
            ({reviews.length} review{reviews.length !== 1 ? 's' : ''})
          </div>
        </div>

        {/* Review Form */}
        {showReviewForm && (
          <form onSubmit={handleSubmitReview} className="border-t border-white/10 pt-6 space-y-4">
            <div>
              <label className="block text-white font-medium mb-2">Your Rating</label>
              <div className="flex items-center gap-1">
                {renderInteractiveStars(rating, setRating)}
              </div>
            </div>

            <div>
              <label className="block text-white font-medium mb-2">Your Review</label>
              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder={`Share your experience staying at ${unitName}...`}
                className="w-full p-3 bg-white/10 backdrop-blur-xl border border-white/20 rounded-lg text-white placeholder-white/50 focus:border-violet-400 focus:outline-none resize-none"
                rows={4}
                required
              />
            </div>

            <div className="flex gap-3">
              <PrimaryButton type="submit" disabled={submitting}>
                {submitting ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Submitting...
                  </>
                ) : (
                  <>
                    <Send className="w-4 h-4" />
                    Submit Review
                  </>
                )}
              </PrimaryButton>
              
              <button
                type="button"
                onClick={() => setShowReviewForm(false)}
                className="px-4 py-2 text-white/70 hover:text-white transition-colors duration-200"
              >
                Cancel
              </button>
            </div>
          </form>
        )}
      </GlassCard>

      {/* Reviews List */}
      <div className="space-y-4">
        {reviews.length === 0 ? (
          <GlassCard className="p-6 text-center">
            <MessageSquare className="w-12 h-12 text-violet-400 mx-auto mb-4" />
            <h4 className="text-lg font-medium text-white mb-2">No Reviews Yet</h4>
            <p className="text-white/70">Be the first to share your experience!</p>
          </GlassCard>
        ) : (
          reviews.map((review) => (
            <GlassCard key={review.id} className="p-6">
              <div className="flex items-start gap-4">
                <div className="bg-violet-500/20 rounded-full p-3">
                  <User className="w-6 h-6 text-violet-400" />
                </div>
                
                <div className="flex-1">
                  <div className="flex items-center justify-between mb-2">
                    <div>
                      <h5 className="font-medium text-white">
                        {review.user.firstName} {review.user.lastName}
                      </h5>
                      <div className="flex items-center gap-2 mt-1">
                        <div className="flex items-center gap-1">
                          {renderStars(review.rating, 'w-4 h-4')}
                        </div>
                        <span className="text-white/50 text-sm flex items-center gap-1">
                          <Calendar className="w-3 h-3" />
                          {new Date(review.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                  </div>
                  
                  <p className="text-white/80 leading-relaxed">
                    {review.comment}
                  </p>
                </div>
              </div>
            </GlassCard>
          ))
        )}
      </div>
    </div>
  );
};

export default ReviewSystem;
