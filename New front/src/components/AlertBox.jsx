import React from 'react';
import { CheckCircle, XCircle, AlertCircle, Info } from 'lucide-react';

const AlertBox = ({ 
  type = 'info',
  title,
  message,
  onClose,
  className = '',
  ...props 
}) => {
  const types = {
    success: {
      icon: CheckCircle,
      colors: 'bg-green-500/10 border-green-400/30 text-green-300',
      iconColor: 'text-green-400',
    },
    error: {
      icon: XCircle,
      colors: 'bg-red-500/10 border-red-400/30 text-red-300',
      iconColor: 'text-red-400',
    },
    warning: {
      icon: AlertCircle,
      colors: 'bg-yellow-500/10 border-yellow-400/30 text-yellow-300',
      iconColor: 'text-yellow-400',
    },
    info: {
      icon: Info,
      colors: 'bg-blue-500/10 border-blue-400/30 text-blue-300',
      iconColor: 'text-blue-400',
    },
  };

  const { icon: Icon, colors, iconColor } = types[type];

  return (
    <div
      className={`
        ${colors} backdrop-blur-md border rounded-xl p-4 
        transition-all duration-300 animate-fade-in-up
        ${className}
      `}
      {...props}
    >
      <div className="flex items-start gap-3">
        <Icon className={`w-5 h-5 mt-0.5 ${iconColor} flex-shrink-0`} />
        
        <div className="flex-1 min-w-0">
          {title && (
            <h4 className="font-medium mb-1">{title}</h4>
          )}
          {message && (
            <p className="text-sm opacity-90">{message}</p>
          )}
        </div>
        
        {onClose && (
          <button
            onClick={onClose}
            className="text-current opacity-60 hover:opacity-100 transition-opacity duration-200"
          >
            <XCircle className="w-4 h-4" />
          </button>
        )}
      </div>
    </div>
  );
};

export default AlertBox;
