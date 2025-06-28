import React from 'react';

const LoadingSpinner = ({ 
  size = 'md', 
  color = 'violet', 
  className = '',
  text = null 
}) => {
  const sizes = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
    xl: 'w-16 h-16'
  };

  const colors = {
    violet: 'border-violet-400',
    blue: 'border-blue-400',
    green: 'border-green-400',
    red: 'border-red-400',
    white: 'border-white'
  };

  const spinnerClass = `
    ${sizes[size]} 
    ${colors[color]} 
    border-2 border-t-transparent 
    rounded-full 
    animate-spin
    ${className}
  `;

  if (text) {
    return (
      <div className="flex flex-col items-center gap-3">
        <div className={spinnerClass}></div>
        <p className="text-violet-200 text-sm animate-pulse">{text}</p>
      </div>
    );
  }

  return <div className={spinnerClass}></div>;
};

export default LoadingSpinner;
