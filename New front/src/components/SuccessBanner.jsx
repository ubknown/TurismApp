import React, { useEffect, useState } from 'react';
import { CheckCircle, Mail, X } from 'lucide-react';

const SuccessBanner = ({ 
  message, 
  onClose, 
  autoHideDelay = 6000,
  showEmailIcon = true,
  className = ''
}) => {
  const [isVisible, setIsVisible] = useState(true);
  const [progress, setProgress] = useState(100);

  useEffect(() => {
    console.log('SuccessBanner mounted with message:', message); // Debug log
    
    if (!autoHideDelay) return;

    // Progress bar animation
    const progressInterval = setInterval(() => {
      setProgress(prev => {
        const newProgress = prev - (100 / (autoHideDelay / 100));
        return newProgress <= 0 ? 0 : newProgress;
      });
    }, 100);

    // Auto hide timer
    const hideTimer = setTimeout(() => {
      console.log('Auto-hiding SuccessBanner'); // Debug log
      handleClose();
    }, autoHideDelay);

    return () => {
      clearInterval(progressInterval);
      clearTimeout(hideTimer);
    };
  }, [autoHideDelay]);

  const handleClose = () => {
    console.log('SuccessBanner closing'); // Debug log
    setIsVisible(false);
    setTimeout(() => {
      onClose?.();
    }, 300);
  };

  if (!isVisible) {
    return null; // Simplified - just remove the component
  }

  return (
    <div className={`transform transition-all duration-300 ease-out animate-fade-in-up ${className}`}>
      <div className="bg-gradient-to-r from-violet-500/20 via-indigo-500/20 to-purple-500/20 backdrop-blur-xl border border-violet-400/30 rounded-2xl p-6 shadow-2xl relative overflow-hidden">
        {/* Progress bar */}
        <div className="absolute top-0 left-0 h-1 bg-gradient-to-r from-violet-400 to-indigo-400 transition-all duration-100 ease-linear rounded-t-2xl"
             style={{ width: `${progress}%` }} />
        
        {/* Content */}
        <div className="flex items-start gap-4">
          <div className="flex items-center gap-2 flex-shrink-0">
            <div className="relative">
              <CheckCircle className="w-6 h-6 text-green-400" />
              <div className="absolute inset-0 bg-green-400/20 rounded-full blur-lg" />
            </div>
            {showEmailIcon && (
              <Mail className="w-5 h-5 text-violet-300/80" />
            )}
          </div>
          
          <div className="flex-1 min-w-0">
            <div className="flex items-start justify-between gap-3">
              <div>
                <h3 className="text-white font-semibold text-lg mb-1">
                  Account Created Successfully!
                </h3>
                <p className="text-violet-200/90 text-sm leading-relaxed">
                  {message}
                </p>
              </div>
              
              <button
                onClick={handleClose}
                className="text-violet-300/60 hover:text-violet-200 transition-colors p-1 rounded-lg hover:bg-violet-500/10"
                aria-label="Close notification"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>

        {/* Subtle animated background effect */}
        <div className="absolute inset-0 bg-gradient-to-r from-violet-400/5 to-transparent opacity-50 animate-pulse" />
      </div>
    </div>
  );
};

export default SuccessBanner;
