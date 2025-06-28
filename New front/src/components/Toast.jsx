import React, { useEffect, useState } from 'react';
import { CheckCircle, XCircle, AlertCircle, Info, X } from 'lucide-react';

const Toast = ({ 
  type = 'info',
  title,
  message,
  onClose,
  autoHide = true,
  autoHideDelay = 5000,
  position = 'top-right',
  className = ''
}) => {
  const [isVisible, setIsVisible] = useState(true);
  const [isLeaving, setIsLeaving] = useState(false);

  const types = {
    success: {
      icon: CheckCircle,
      colors: 'bg-green-500/20 border-green-400/30',
      iconColor: 'text-green-400',
      progressColor: 'bg-green-400'
    },
    error: {
      icon: XCircle,
      colors: 'bg-red-500/20 border-red-400/30',
      iconColor: 'text-red-400',
      progressColor: 'bg-red-400'
    },
    warning: {
      icon: AlertCircle,
      colors: 'bg-yellow-500/20 border-yellow-400/30',
      iconColor: 'text-yellow-400',
      progressColor: 'bg-yellow-400'
    },
    info: {
      icon: Info,
      colors: 'bg-blue-500/20 border-blue-400/30',
      iconColor: 'text-blue-400',
      progressColor: 'bg-blue-400'
    }
  };

  const { icon: Icon, colors, iconColor, progressColor } = types[type];

  const positions = {
    'top-right': 'top-4 right-4',
    'top-left': 'top-4 left-4',
    'bottom-right': 'bottom-4 right-4',
    'bottom-left': 'bottom-4 left-4',
    'top-center': 'top-4 left-1/2 transform -translate-x-1/2',
    'bottom-center': 'bottom-4 left-1/2 transform -translate-x-1/2'
  };

  useEffect(() => {
    if (autoHide && autoHideDelay > 0) {
      const timer = setTimeout(() => {
        handleClose();
      }, autoHideDelay);

      return () => clearTimeout(timer);
    }
  }, [autoHide, autoHideDelay]);

  const handleClose = () => {
    setIsLeaving(true);
    setTimeout(() => {
      setIsVisible(false);
      onClose?.();
    }, 300);
  };

  if (!isVisible) return null;

  const animationClass = isLeaving 
    ? 'animate-fade-out-up' 
    : 'animate-fade-in-down';

  return (
    <div className={`fixed ${positions[position]} z-50 ${animationClass} ${className}`}>
      <div className={`${colors} backdrop-blur-xl border rounded-xl p-4 shadow-2xl max-w-sm w-full relative overflow-hidden`}>
        {/* Auto-hide progress bar */}
        {autoHide && (
          <div className="absolute top-0 left-0 h-1 w-full bg-white/10">
            <div 
              className={`h-full ${progressColor} transition-all duration-100 ease-linear`}
              style={{
                animation: `shrink ${autoHideDelay}ms linear`
              }}
            />
          </div>
        )}

        <div className="flex items-start gap-3">
          <div className="flex-shrink-0">
            <Icon className={`w-5 h-5 ${iconColor}`} />
          </div>
          
          <div className="flex-1 min-w-0">
            {title && (
              <h4 className="text-white font-medium text-sm mb-1">
                {title}
              </h4>
            )}
            {message && (
              <p className="text-white/80 text-sm">
                {message}
              </p>
            )}
          </div>

          <button
            onClick={handleClose}
            className="text-white/60 hover:text-white transition-colors p-1 rounded-lg hover:bg-white/10 flex-shrink-0"
            aria-label="Close notification"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};

// Custom CSS for animations (add to your global CSS)
const toastStyles = `
  @keyframes fade-in-down {
    from {
      opacity: 0;
      transform: translateY(-20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes fade-out-up {
    from {
      opacity: 1;
      transform: translateY(0);
    }
    to {
      opacity: 0;
      transform: translateY(-20px);
    }
  }

  @keyframes shrink {
    from {
      width: 100%;
    }
    to {
      width: 0%;
    }
  }

  .animate-fade-in-down {
    animation: fade-in-down 0.3s ease-out;
  }

  .animate-fade-out-up {
    animation: fade-out-up 0.3s ease-out;
  }
`;

export default Toast;
export { toastStyles };
