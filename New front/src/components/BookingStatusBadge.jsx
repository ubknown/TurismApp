import React from 'react';
import { CheckCircle, Clock, XCircle, Calendar } from 'lucide-react';

const BookingStatusBadge = ({ status, size = 'normal' }) => {
  const getStatusConfig = (status) => {
    switch (status?.toUpperCase()) {
      case 'CONFIRMED':
        return {
          label: 'Confirmed',
          icon: CheckCircle,
          bgColor: 'bg-green-500/20',
          textColor: 'text-green-400',
          borderColor: 'border-green-400/30'
        };
      case 'PENDING':
        return {
          label: 'Pending',
          icon: Clock,
          bgColor: 'bg-yellow-500/20',
          textColor: 'text-yellow-400',
          borderColor: 'border-yellow-400/30'
        };
      case 'CANCELLED':
        return {
          label: 'Cancelled',
          icon: XCircle,
          bgColor: 'bg-red-500/20',
          textColor: 'text-red-400',
          borderColor: 'border-red-400/30'
        };
      case 'COMPLETED':
        return {
          label: 'Completed',
          icon: Calendar,
          bgColor: 'bg-blue-500/20',
          textColor: 'text-blue-400',
          borderColor: 'border-blue-400/30'
        };
      default:
        return {
          label: status || 'Unknown',
          icon: Clock,
          bgColor: 'bg-gray-500/20',
          textColor: 'text-gray-400',
          borderColor: 'border-gray-400/30'
        };
    }
  };

  const config = getStatusConfig(status);
  const Icon = config.icon;

  const sizeClasses = {
    small: {
      container: 'px-2 py-1 text-xs',
      icon: 'w-3 h-3'
    },
    normal: {
      container: 'px-3 py-1.5 text-sm',
      icon: 'w-4 h-4'
    },
    large: {
      container: 'px-4 py-2 text-base',
      icon: 'w-5 h-5'
    }
  };

  const currentSize = sizeClasses[size] || sizeClasses.normal;

  return (
    <span className={`
      inline-flex items-center gap-1.5 font-medium rounded-full border transition-all duration-300
      ${config.bgColor} ${config.textColor} ${config.borderColor} ${currentSize.container}
    `}>
      <Icon className={currentSize.icon} />
      {config.label}
    </span>
  );
};

export default BookingStatusBadge;
