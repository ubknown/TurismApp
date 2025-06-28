import React from 'react';

const PrimaryButton = ({ 
  children, 
  variant = 'primary',
  size = 'md',
  className = '',
  disabled = false,
  loading = false,
  ...props 
}) => {
  const variants = {
    primary: 'bg-white text-purple-900 hover:bg-gray-100 hover:shadow-lg',
    secondary: 'bg-white bg-opacity-10 text-white border border-white border-opacity-30 hover:bg-opacity-20 hover:shadow-lg',
    outline: 'bg-transparent text-white border-2 border-white border-opacity-50 hover:bg-white hover:bg-opacity-10 hover:border-opacity-70',
    danger: 'bg-red-500 bg-opacity-20 text-red-300 border border-red-500 border-opacity-30 hover:bg-opacity-30 hover:shadow-lg',
    success: 'bg-green-500 bg-opacity-20 text-green-300 border border-green-500 border-opacity-30 hover:bg-opacity-30 hover:shadow-lg',
  };

  const sizes = {
    sm: 'px-4 py-2 text-sm',
    md: 'px-6 py-3 text-base',
    lg: 'px-8 py-4 text-lg',
  };

  const baseStyles = 'rounded-xl font-medium transition-all duration-300 transform hover:scale-105 active:scale-95 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:ring-opacity-50 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none';

  return (
    <button
      className={`
        ${baseStyles}
        ${variants[variant]}
        ${sizes[size]}
        ${disabled || loading ? 'opacity-50 cursor-not-allowed' : ''}
        ${className}
      `}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? (
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin" />
          {children}
        </div>
      ) : (
        children
      )}
    </button>
  );
};

export default PrimaryButton;
