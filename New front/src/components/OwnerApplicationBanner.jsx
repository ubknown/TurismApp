import React from 'react';
import { Clock, CheckCircle, XCircle, Info } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const OwnerApplicationBanner = () => {
  const { user, hasOwnerApplicationPending, hasOwnerApplicationApproved, hasOwnerApplicationRejected } = useAuth();

  // Don't show banner if user doesn't have any application
  if (!user || user.ownerStatus === 'NONE') {
    return null;
  }

  const getStatusInfo = () => {
    if (hasOwnerApplicationPending()) {
      return {
        icon: Clock,
        bgColor: 'bg-gradient-to-r from-amber-500/10 to-orange-500/10',
        borderColor: 'border-amber-500/30',
        textColor: 'text-amber-200',
        iconColor: 'text-amber-400',
        title: 'Owner Application Under Review',
        message: 'Your owner application is currently under review. You will receive an email once it is approved or rejected. You can continue using the platform as a guest while waiting.'
      };
    }
    
    if (hasOwnerApplicationApproved()) {
      return {
        icon: CheckCircle,
        bgColor: 'bg-gradient-to-r from-green-500/10 to-emerald-500/10',
        borderColor: 'border-green-500/30',
        textColor: 'text-green-200',
        iconColor: 'text-green-400',
        title: 'Owner Application Approved!',
        message: 'Congratulations! Your owner application has been approved. You now have access to all owner features including property management and analytics.'
      };
    }
    
    if (hasOwnerApplicationRejected()) {
      return {
        icon: XCircle,
        bgColor: 'bg-gradient-to-r from-red-500/10 to-pink-500/10',
        borderColor: 'border-red-500/30',
        textColor: 'text-red-200',
        iconColor: 'text-red-400',
        title: 'Owner Application Not Approved',
        message: 'Unfortunately, your owner application was not approved at this time. You can continue using the platform as a guest. If you have questions, please contact support.'
      };
    }

    return null;
  };

  const statusInfo = getStatusInfo();
  
  if (!statusInfo) {
    return null;
  }

  const { icon: Icon, bgColor, borderColor, textColor, iconColor, title, message } = statusInfo;

  return (
    <div className={`mx-4 mt-4 mb-6 p-4 ${bgColor} ${borderColor} border rounded-xl backdrop-blur-sm`}>
      <div className="flex items-start gap-4">
        <div className={`w-10 h-10 ${bgColor} rounded-full flex items-center justify-center flex-shrink-0 border ${borderColor}`}>
          <Icon className={`w-5 h-5 ${iconColor}`} />
        </div>
        <div className="flex-1">
          <h3 className={`font-semibold text-lg ${textColor} mb-2`}>
            {title}
          </h3>
          <p className={`${textColor} text-sm leading-relaxed`}>
            {message}
          </p>
          {hasOwnerApplicationPending() && (
            <div className={`mt-3 flex items-center gap-2 ${textColor} text-xs`}>
              <Info className="w-4 h-4" />
              <span>Average review time: 1-3 business days</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default OwnerApplicationBanner;
