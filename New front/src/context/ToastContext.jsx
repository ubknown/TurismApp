import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { CheckCircle, XCircle, AlertCircle, Info } from 'lucide-react';
import { setGlobalToast } from '../utils/globalToast';

const ToastContext = createContext();

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};

const ToastItem = ({ toast, onRemove }) => {
  const icons = {
    success: CheckCircle,
    error: XCircle,
    warning: AlertCircle,
    info: Info
  };

  const Icon = icons[toast.type] || Info;

  const bgColors = {
    success: 'bg-green-500/20 border-green-500/30',
    error: 'bg-red-500/20 border-red-500/30',
    warning: 'bg-yellow-500/20 border-yellow-500/30',
    info: 'bg-blue-500/20 border-blue-500/30'
  };

  const iconColors = {
    success: 'text-green-400',
    error: 'text-red-400',
    warning: 'text-yellow-400',
    info: 'text-blue-400'
  };

  return (
    <div className={`${bgColors[toast.type]} backdrop-blur-xl border rounded-lg p-4 mb-3 animate-slide-in`}>
      <div className="flex items-start gap-3">
        <Icon className={`w-5 h-5 mt-0.5 ${iconColors[toast.type]}`} />
        <div className="flex-1">
          <p className="text-white font-medium">{toast.title}</p>
          {toast.message && (
            <p className="text-white/80 text-sm mt-1">{toast.message}</p>
          )}
        </div>
        <button
          onClick={() => onRemove(toast.id)}
          className="text-white/60 hover:text-white transition-colors"
        >
          <XCircle className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts(prev => prev.filter(toast => toast.id !== id));
  }, []);

  // ✅ Add method to clear all toasts
  const clearAllToasts = useCallback(() => {
    setToasts([]);
  }, []);

  // ✅ Add method to clear toasts by type
  const clearToastsByType = useCallback((type) => {
    setToasts(prev => prev.filter(toast => toast.type !== type));
  }, []);

  // ✅ Enhanced add toast to prevent similar notifications stacking
  const addToast = useCallback(({ type = 'info', title, message, duration = 5000, preventDuplicates = true }) => {
    const id = Date.now() + Math.random();
    const newToast = { id, type, title, message };
    
    setToasts(prev => {
      // Prevent duplicate notifications if preventDuplicates is true
      if (preventDuplicates) {
        const existingToast = prev.find(toast => 
          toast.type === type && toast.title === title
        );
        if (existingToast) {
          // Update existing toast instead of creating duplicate
          return prev.map(toast => 
            toast.id === existingToast.id ? newToast : toast
          );
        }
      }
      return [...prev, newToast];
    });

    if (duration > 0) {
      setTimeout(() => {
        removeToast(id);
      }, duration);
    }

    return id;
  }, [removeToast]);

  const success = useCallback((title, message) => {
    return addToast({ type: 'success', title, message });
  }, [addToast]);

  const error = useCallback((title, message) => {
    return addToast({ type: 'error', title, message });
  }, [addToast]);

  const warning = useCallback((title, message) => {
    return addToast({ type: 'warning', title, message });
  }, [addToast]);

  const info = useCallback((title, message) => {
    return addToast({ type: 'info', title, message });
  }, [addToast]);

  // Set global toast instance for use outside React components
  useEffect(() => {
    setGlobalToast({ success, error, warning, info, clearAllToasts, clearToastsByType });
  }, [success, error, warning, info, clearAllToasts, clearToastsByType]);

  return (
    <ToastContext.Provider value={{ 
      addToast, 
      removeToast, 
      clearAllToasts,
      clearToastsByType,
      success, 
      error, 
      warning, 
      info 
    }}>
      {children}
      
      {/* Toast Container */}
      <div className="fixed top-4 right-4 z-[9999] max-w-sm w-full">
        {toasts.map(toast => (
          <ToastItem 
            key={toast.id} 
            toast={toast} 
            onRemove={removeToast}
          />
        ))}
      </div>
    </ToastContext.Provider>
  );
};
